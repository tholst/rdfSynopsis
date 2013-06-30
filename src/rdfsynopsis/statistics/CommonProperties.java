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

public class CommonProperties extends StatisticalCriterion {



	private Map<String, Set<String>>			classCommonPropertyMap;

	private Map<String, Map<String, Integer>>	classInstancesPerPropertyMap;
	private Map<String, Set<String>>			classInstanceMap;

	private Map<String, Set<String>>			subjectClassMap;
	private Map<String, Set<String>>			propSubjectMap;

	private boolean								updated	= true;

	public CommonProperties() {
		logger = Logger.getLogger(CommonProperties.class);
		logger.trace("logger created");
		textId = "CommonProperties";
		
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		if (!updated)
			update();
		logger.debug("flushLog");
		
		for (Map.Entry<String, Set<String>> outerEntry : classCommonPropertyMap
				.entrySet()) {
			for (String commonProperty : outerEntry.getValue())
			ps.println("Result for class <" + outerEntry.getKey() + ">: "
					+" common property " + commonProperty);
	
		}

	}
	
	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?class") && qs.contains("?property")) {
				// valid solution

				String classUri = qs.getResource("?class").getURI();
				String propertyUri = qs.getResource("?property").getURI();
				
				addCommonProperty(classUri, propertyUri);

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
			
			// add to classes' instances
			Set<String> instances = classInstanceMap.get(classUri);
			if (instances == null)
				instances = new HashSet<String>();
			instances.add(subjectUri);
			classInstanceMap.put(classUri, instances);
			
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
		
		for (Map.Entry<String, Set<String>> outerEntry : classCommonPropertyMap
				.entrySet()) {
			for (String commonProperty : outerEntry.getValue())
				m.put(outerEntry.getKey() + "_classWithCommonProperty_" + commonProperty, 1);
		}
		return m;
	}

	@Override
	public void init() {
		updated = true;
		classInstancesPerPropertyMap = new HashMap<String, Map<String, Integer>>();
		subjectClassMap = new HashMap<String, Set<String>>();
		classInstanceMap = new HashMap<String, Set<String>>();
		propSubjectMap = new HashMap<String, Set<String>>();
		classCommonPropertyMap  = new HashMap<String, Set<String>>();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CommonProperties) {
			CommonProperties o2 = (CommonProperties) o;
			this.update();
			o2.update();
			return o2.classCommonPropertyMap.equals(this.classCommonPropertyMap);
		}
		else return false;
	}
	
	private void addCommonProperty(String classUri,	String propertyUri) {
		assert classUri != null;
		assert propertyUri != null;
		
		// add to class' common properties
		Set<String> commonProperties = classCommonPropertyMap.get(classUri);
		if (commonProperties == null)
			commonProperties = new HashSet<String>();
		commonProperties.add(propertyUri);
		classCommonPropertyMap.put(classUri, commonProperties);
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
			classCommonPropertyMap  = new HashMap<String, Set<String>>();

			// count number of class instances per property
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
			
			// choose properties that all instances of a class have
			for (Map.Entry<String, Map<String, Integer>> outerEntry : classInstancesPerPropertyMap
					.entrySet()) {
				String classUri = outerEntry.getKey();
				Integer numClassInstances = classInstanceMap.get(classUri).size();
				for (Map.Entry<String, Integer> innerEntry : outerEntry.getValue() 
						.entrySet()) {
					String propertyUri = innerEntry.getKey();
					Integer numInstancesPerProperty = innerEntry.getValue();
					if(numInstancesPerProperty.equals(numClassInstances))
						addCommonProperty(classUri, propertyUri);

				}
			}
			updated = true;
		}
	}


}
