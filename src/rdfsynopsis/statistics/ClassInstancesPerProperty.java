package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class ClassInstancesPerProperty extends StatisticalCriterion {
	
	private Map<String, Map<String, Integer>>	classInstancesPerPropertyMap;

	private Map<String, Set<String>>			subjectClassMap;
	private Map<String, Set<String>>			propSubjectMap;

	private boolean								updated	= true;

	public ClassInstancesPerProperty() {
		logger = Logger.getLogger(ClassInstancesPerProperty.class);
		logger.trace("logger created");
		textId = "ClassInstancesPerProperty";
		
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		if (!updated)
			update();
		logger.debug("flushLog");
		
		for (Map.Entry<String, Map<String, Integer>> outerEntry : classInstancesPerPropertyMap
				.entrySet()) {
			for (Map.Entry<String, Integer> innerEntry : outerEntry.getValue()
					.entrySet())
				ps.println("Result for class <" + outerEntry.getKey() + ">: "
						+ innerEntry.getValue() + " instances per property "
						+ innerEntry.getKey());
		}

	}
	
	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?class") && qs.contains("?property") && qs.contains("?numInstances")) {
				// valid solution

				String classUri = qs.getResource("?class").getURI();
				String propertyUri = qs.getResource("?property").getURI();
				Integer numInstances = qs.getLiteral("?numInstances").getInt();
				
				increaseClassInstancesPerProperty(classUri, propertyUri, numInstances);

			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}
	}


	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;

		String subjectUri = s.toString();
		String propertyUri = p.toString();

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

			// add to property's subjects
			Set<String> subjects = propSubjectMap.get(propertyUri);
			if (subjects == null)
				subjects = new HashSet<String>();
			subjects.add(subjectUri);
			propSubjectMap.put(propertyUri, subjects);
			
		}

		updated = false;
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		
		update();
		
		for (Map.Entry<String, Map<String, Integer>> outerEntry : classInstancesPerPropertyMap
				.entrySet()) {
			for (Map.Entry<String, Integer> innerEntry : outerEntry.getValue()
					.entrySet())
				m.put(outerEntry.getKey() + "_classInstancesPerProperty_" + innerEntry.getKey(), innerEntry.getValue());
		}
		return m;
	}

	@Override
	public void init() {
		updated = true;
		classInstancesPerPropertyMap = new HashMap<String, Map<String, Integer>>();
		subjectClassMap = new HashMap<String, Set<String>>();
		propSubjectMap = new HashMap<String, Set<String>>();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ClassInstancesPerProperty) {
			ClassInstancesPerProperty o2 = (ClassInstancesPerProperty) o;
			this.update();
			o2.update();
			
//	        for (Entry<String, Map<String, Integer>> entry : o2.classInstancesPerPropertyMap.entrySet()) {
//	        	String key = entry.getKey();
//	            if (!entry.getValue().equals(this.classInstancesPerPropertyMap.get(key)))
//	        	System.out.println("this key " + key + ": "
//	                    + entry.getValue());
//	        }
//	        for (Entry<String, Map<String, Integer>> entry : this.classInstancesPerPropertyMap.entrySet()) {
//	        	String key = entry.getKey();
//	            if (!entry.getValue().equals(o2.classInstancesPerPropertyMap.get(key)))
//	        	System.out.println("o2 key " + key + ": "
//	                    + entry.getValue());
//	        }
			return o2.classInstancesPerPropertyMap.equals(this.classInstancesPerPropertyMap);
		}
		else return false;
	}
	
	private void increaseClassInstancesPerProperty(String classUri,
			String propertyUri, Integer val) {
		assert classUri != null;
		assert propertyUri != null;

		Map<String, Integer> m = classInstancesPerPropertyMap.get(classUri);
		if (m == null)
			m = new HashMap<String, Integer>();
		Integer numInstances = m.get(propertyUri);
		if (numInstances == null)
			numInstances = 0;
		m.put(propertyUri, numInstances + val);
		classInstancesPerPropertyMap.put(classUri, m);
	}
	
	/**
	 * Only needed for triple stream mode.
	 */
	private void update() {
		if (!updated) {
			classInstancesPerPropertyMap = new HashMap<String, Map<String, Integer>>();

			for (Map.Entry<String, Set<String>> propertyEntry : propSubjectMap
					.entrySet()) {
				String propertyUri = propertyEntry.getKey();
				

				for (String subjectUri : propertyEntry.getValue()) {
					Set<String> subjectClasses = subjectClassMap.get(subjectUri);
					if (subjectClasses != null)
						for (String classUri : subjectClasses) {
							increaseClassInstancesPerProperty(classUri, propertyUri, 1);
						}
				}
			}
		}
	}

}
