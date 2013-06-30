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

public class ImplicitClassHierarchy extends StatisticalCriterion {



	private Map<String, Set<String>>	subSuperClassMap;

	// Save class instances for post processing (TSA)
	// class -> ({instance})
	private Map<String, Set<String>>	classInstances;

	boolean											updated	= true;

	public ImplicitClassHierarchy() {
		logger = Logger.getLogger(ImplicitClassHierarchy.class);
		logger.trace("logger created");
		textId = "ImplicitClassHierarchy";
		
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		update();
		
		ps.println("Implicit Class Hierarchy:");
		for (String subClass : subSuperClassMap.keySet()) {
			ps.println(subClass + " implicit subclass of");
			Set<String> superClasses = subSuperClassMap.get(subClass);
			if (superClasses != null)
				for (String superClass : superClasses) {
					ps.println("-> "+superClass);
				}
		}
	}

	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");
		
		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?subClass") && qs.contains("?superClass")) {
				// valid solution
				
				String subClassUri = qs.getResource("?subClass").getURI();
				String superClassUri = qs.getResource("?superClass").getURI();

				addHierarchyEdge(subClassUri, superClassUri);

			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}

	}

	private void addHierarchyEdge(String subClassUri, String superClassUri) {
		Set<String> superClasses = subSuperClassMap.get(subClassUri);
		if (superClasses == null)
			superClasses = new HashSet<String>();
		superClasses.add(superClassUri);
		subSuperClassMap.put(subClassUri, superClasses);
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;
		String type = Namespace.RDF.getFullTerm("type");
		String resource = Namespace.RDFS.getFullTerm("Resource");

		// add class instances
		if (p.getURI().equals(type) && !((Resource)o).toString().equals(resource)) {
			String classUri = ((Resource)o).toString();
			Set<String> instances = classInstances.get(classUri);
			if (instances == null)
				instances = new HashSet<String>();
			instances.add(s.toString());
			classInstances.put(classUri, instances);
		}
		updated = false;
	}

	@Override
	public Map<String, Object> getResultMap() {
		update();
		Map<String, Object> m = new HashMap<String, Object>();
		// TODO implement
		return m;
	}

	@Override
	public void init() {
		subSuperClassMap = new HashMap<String, Set<String>>();
		classInstances = new HashMap<String, Set<String>>();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ImplicitClassHierarchy) {
			ImplicitClassHierarchy o2 = (ImplicitClassHierarchy) o;
			return o2.subSuperClassMap.equals(this.subSuperClassMap);
		}
		else return false;
	}

	/**
	 * Only needed for triple stream mode.
	 */
	private void update() {
		if (!updated) {

			subSuperClassMap = new HashMap<String, Set<String>>();

			// find parallel class usage
			for (String c : classInstances.keySet()) {				
				Set<String> cInstances = classInstances.get(c);				
				for (String superC : classInstances.keySet()) {
					if (!c.equals(superC)) {						
						Set<String> superCInstances = classInstances.get(superC);						
						if (superCInstances.containsAll(cInstances)) {
							addHierarchyEdge(c, superC);
						}						
					}						
				}				
			}
			updated = true;
		}
	}

}
