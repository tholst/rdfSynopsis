package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class ImplicitPropertyHierarchy extends StatisticalCriterion {

	private Map<String, Set<String>>	subSuperPropMap;

	// Save streamed triples for post processing
	// property -> (subject -> {object})
	private Map<String, Map<String, Set<String>>>	triplesPerProperty;

	boolean											updated	= true;

	public ImplicitPropertyHierarchy() {
		logger = Logger.getLogger(ImplicitPropertyHierarchy.class);
		logger.trace("logger created");
		textId = "ImplicitPropertyHierarchy";

		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		update();
		
		ps.println("Implicit Property Hierarchy:");
		for (String subProp : subSuperPropMap.keySet()) {
			ps.println(subProp + "implicit subproperty of");
			Set<String> superProps = subSuperPropMap.get(subProp);
			if (superProps != null)
				for (String superProp : superProps) {
					ps.println("-> "+superProp);
				}
		}
	}

	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");
		
		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?subProp") && qs.contains("?superProp")) {
				// valid solution
				
				String subPropertyUri = qs.getResource("?subProp").getURI();
				String superPropertyUri = qs.getResource("?superProp").getURI();

				addHierarchyEdge(subPropertyUri, superPropertyUri);

			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}

	}

	private void addHierarchyEdge(String subPropertyUri, String superPropertyUri) {
		Set<String> superProps = subSuperPropMap.get(subPropertyUri);
		if (superProps == null)
			superProps = new HashSet<String>();
		superProps.add(superPropertyUri);
		subSuperPropMap.put(subPropertyUri, superProps);
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;

		// add any triple
		Map<String, Set<String>> subjectsPerProperty = triplesPerProperty.get(p
				.getURI());
		if (subjectsPerProperty == null)
			subjectsPerProperty = new HashMap<String, Set<String>>();
		Set<String> objects = subjectsPerProperty.get(s.toString());
		if (objects == null)
			objects = new HashSet<String>();
		objects.add(o.toString());
		subjectsPerProperty.put(s.toString(), objects);
		triplesPerProperty.put(p.getURI(), subjectsPerProperty);

		updated = false;
	}

	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		// TODO implement
		return m;
	}

	@Override
	public void init() {
		subSuperPropMap = new HashMap<String, Set<String>>();
		triplesPerProperty = new HashMap<String, Map<String, Set<String>>>();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ImplicitPropertyHierarchy) {
			ImplicitPropertyHierarchy o2 = (ImplicitPropertyHierarchy) o;
			return o2.subSuperPropMap.equals(this.subSuperPropMap);
		}
		else return false;
	}

	/**
	 * Only needed for triple stream mode.
	 */
	private void update() {
		if (!updated) {

			subSuperPropMap = new HashMap<String, Set<String>>();

			// find parallel property usage
			for (String p : triplesPerProperty.keySet()) {
				
				Map<String, Set<String>> pSubjects = triplesPerProperty.get(p);
				int pNumSubjects = pSubjects.size();
				
				for (String superP : triplesPerProperty.keySet()) {
					if (!p.equals(superP)) {
						
						Map<String, Set<String>> superPSubjects = triplesPerProperty.get(superP);
						int superPNumSubjects = superPSubjects.size();
						
						if (pNumSubjects <= superPNumSubjects) {
							boolean containsAll = true;
							Set<String> pSub = pSubjects.keySet();
							
							// for all subjects: all objects contained?
							for (Iterator<String> i = pSub.iterator(); i.hasNext() && containsAll;) {
								String sub = i.next();
								Set<String> pObjects = pSubjects.get(sub);
								Set<String> superPObjects = superPSubjects.get(sub);
								containsAll = superPObjects != null && superPObjects.containsAll(pObjects);
							}
							
							// add hierarchy statement
							if (containsAll)
								addHierarchyEdge(p, superP);
						}						
					}						
				}				
			}
			updated = true;
		}
	}
}
