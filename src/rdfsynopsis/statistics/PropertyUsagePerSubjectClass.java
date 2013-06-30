package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class PropertyUsagePerSubjectClass extends StatisticalCriterion {

	private Map<String, Map<String, Integer>>	propPerSubjectClassMap;
	private Map<String, Integer>				propPerUntypedSubjectsMap;

	private Map<String, Set<String>>			subjectClassMap;
	private Map<String, Map<String, Integer>>	propPerSubjectMap;

	private boolean								updated	= true;

	public PropertyUsagePerSubjectClass() {
		logger = Logger.getLogger(PropertyUsagePerSubjectClass.class);
		logger.trace("logger created");
		textId = "PropertyUsagePerSubjectClass";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		if (!updated)
			update();
		logger.debug("flushLog");
		ps.println("Result: number of of classes found = "
				+ getNumClasses());

		// typed subjects
		for (Map.Entry<String, Map<String, Integer>> outerEntry : propPerSubjectClassMap
				.entrySet()) {
			for (Map.Entry<String, Integer> innerEntry : outerEntry.getValue()
					.entrySet())
				ps.println("Result for class <" + outerEntry.getKey() + ">: "
						+ innerEntry.getValue() + " triples with property "
						+ innerEntry.getKey());
		}

		// untyped subjects
		for (Map.Entry<String, Integer> e : propPerUntypedSubjectsMap
				.entrySet()) {
			ps.println("Result for untyped subjects: " + e.getValue()
					+ " triples with property "
					+ e.getKey());
		}

	}


	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?property")
					&& qs.contains("?numUses")) { // valid solution

				String propertyUri = qs.getResource("?property").getURI();
				Integer numInstances = qs.getLiteral("?numUses").getInt();

				// result for typed subjects?
				if (qs.contains("?class")) {
					String classUri = qs.getResource("?class").getURI();

					increasePropUsagePerSubjectClass(classUri, propertyUri,
							numInstances);
				} else {
					// results for untyped subjects
					increasePropUsagePerUntypedSubjects(propertyUri,
							numInstances);
				}
			} else // invalid solution
			logger.debug("invalid solution: " + qs);

		}

		logger.debug("propPerSubjectClassMap:\n"
				+ propPerSubjectClassMap.toString());
		logger.debug("propPerUntypedSubjectsMap:\n"
				+ propPerUntypedSubjectsMap.toString());
	}

	private void increasePropUsagePerUntypedSubjects(String propertyUri,
			Integer val) {
		Integer numInstances = propPerUntypedSubjectsMap.get(propertyUri);
		if (numInstances == null)
			numInstances = 0;
		propPerUntypedSubjectsMap.put(propertyUri, numInstances + val);
	}

	private void increasePropUsagePerSubjectClass(String classUri,
			String propertyUri, Integer val) {
		assert classUri != null;
		assert propertyUri != null;

		Map<String, Integer> m = propPerSubjectClassMap.get(classUri);
		if (m == null)
			m = new HashMap<String, Integer>();
		Integer numInstances = m.get(propertyUri);
		if (numInstances == null)
			numInstances = 0;
		m.put(propertyUri, numInstances + val);
		propPerSubjectClassMap.put(classUri, m);
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;

		String subjectUri = s.getURI();
		String propertyUri = p.getURI();

		logger.trace("using triple (" + s + " " + p + " " + o + ")");

		// class instance?
		String type = Namespace.RDF.getFullTerm("type");
		if ((propertyUri.equals(type) || propertyUri.equals("a"))
				&& o.isURIResource()) {
			String classUri = ((Resource) o).getURI();

			// add to subject's classes
			Set<String> classes = subjectClassMap.get(subjectUri);
			if (classes == null)
				classes = new HashSet<String>();
			classes.add(classUri);
			subjectClassMap.put(subjectUri, classes);
		} else {

		// count property
		Map<String, Integer> propUsageMap = propPerSubjectMap.get(subjectUri);
		if (propUsageMap == null)
			propUsageMap = new HashMap<String, Integer>();
		Integer numInstances = propUsageMap.get(propertyUri);
		if (numInstances == null)
			numInstances = 0;
		propUsageMap.put(propertyUri, numInstances + 1);
		propPerSubjectMap.put(subjectUri, propUsageMap);
		}

		updated = false;
	}

	/**
	 * Only needed for triple stream mode.
	 * Calculates property usage maps (propPerSubjectClassMap and
	 * propPerUntypedSubjectsMap) based on data structures filled by triple
	 * stream analysis.
	 */
	private void update() {
		if (!updated) {
			propPerSubjectClassMap = new HashMap<String, Map<String, Integer>>();
			propPerUntypedSubjectsMap = new HashMap<String, Integer>();

			for (Map.Entry<String, Map<String, Integer>> subjectEntry : propPerSubjectMap
					.entrySet()) {
				String subjectUri = subjectEntry.getKey();
				Set<String> classes = subjectClassMap.get(subjectUri);

				for (Map.Entry<String, Integer> propEntry : subjectEntry
						.getValue()
						.entrySet()) {
					String propertyUri = propEntry.getKey();
					Integer numInstances = propEntry.getValue();

					// typed or untyped?
					if (classes == null) {
						increasePropUsagePerUntypedSubjects(propertyUri,
								numInstances);
					} else {
						for (String classUri : classes) {
							increasePropUsagePerSubjectClass(classUri,
									propertyUri, numInstances);
						}
					}
				}
			}
		}
	}

	public int getNumClasses() {
		if (!updated)
			update();
		return propPerSubjectClassMap.size();
	}

	public Set<String> getClassUris() {
		if (!updated)
			update();
		return propPerSubjectClassMap.keySet();
	}

	public int getPropertyUsagePerClass(Resource classRes, Property propRes) {
		if (!updated)
			update();
		return getPropertyUsagePerClass(classRes.getURI(), propRes.getURI());
	}

	private int getPropertyUsagePerClass(String classUri, String propUri) {
		if (!updated)
			update();
		if (propPerSubjectClassMap.containsKey(classUri)) {
			Map<String, Integer> propUsageMap = propPerSubjectClassMap
					.get(classUri);
			if (propUsageMap.containsKey(propUri))
				return propUsageMap.get(propUri);
			else return 0;
		} else return 0;
	}

	public int getUntypedPropertyUsage(Property propRes) {
		if (!updated)
			update();
		return getUntypedPropertyUsage(propRes.getURI());
	}

	public int getUntypedPropertyUsage(String propertyUri) {
		if (!updated)
			update();
		if (propPerUntypedSubjectsMap.containsKey(propertyUri))
			return propPerUntypedSubjectsMap.get(propertyUri);
		else return 0;
	}

	public int getUntypedNumProperties() {
		if (!updated)
			update();
		return propPerUntypedSubjectsMap.size();
	}

	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		// typed subjects
		for (Map.Entry<String, Map<String, Integer>> outerEntry : propPerSubjectClassMap
				.entrySet()) {
			for (Map.Entry<String, Integer> innerEntry : outerEntry.getValue()
					.entrySet())
				m.put(outerEntry.getKey() + "_classUsesProperty_" + innerEntry.getKey(), innerEntry.getValue());
		}

		// untyped subjects
		for (Map.Entry<String, Integer> e : propPerUntypedSubjectsMap
				.entrySet()) {
			m.put(e.getKey() + "_usedByUntyped", e.getValue());
		}
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PropertyUsagePerSubjectClass) {
			PropertyUsagePerSubjectClass o2 = (PropertyUsagePerSubjectClass) o;
			this.update();
			o2.update();
			return o2.propPerSubjectClassMap
					.equals(this.propPerSubjectClassMap)
					&&
					o2.propPerUntypedSubjectsMap
							.equals(this.propPerUntypedSubjectsMap);
		}
		else return false;
	}

	@Override
	public void init() {
		updated = true;
		propPerSubjectClassMap = new HashMap<String, Map<String, Integer>>();
		propPerUntypedSubjectsMap = new HashMap<String, Integer>();
		subjectClassMap = new HashMap<String, Set<String>>();
		propPerSubjectMap = new HashMap<String, Map<String, Integer>>();
	}

}
