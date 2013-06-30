package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rdfsynopsis.util.HierarchyGraph;
import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class PropertyHierarchy extends StatisticalCriterion {

	private HierarchyGraph<String>	propertyHierachyGraph;
	private int						numSubpropertyStatements;

	public PropertyHierarchy() {
		logger = Logger.getLogger(PropertyHierarchy.class);
		logger.trace("logger created");
		textId = "PropertyHierarchy";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.print(propertyHierachyGraph);
		ps.println("Result: number of properties in hierarchy = " + getNumPropertiesInHierarchy());
		ps.println("Result: max depth in hierarchy = " + getPropertyHierarchyDepth());
		ps.println("Result: property hierarchy = " + propertyHierachyGraph);
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?subprop") && qs.contains("?superprop")) {
				// valid solution

				String subPropertyUri = qs.getResource("?subprop").getURI();
				String superPropertyUri = qs.getResource("?superprop").getURI();

				propertyHierachyGraph.addHierarchyEdge(subPropertyUri, superPropertyUri);
				numSubpropertyStatements++;

			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}

		// debug result output
		logger.debug("Property Hierarchy:\n" + propertyHierachyGraph);
	}

	public int getPropertyHierarchyDepth() {
		return propertyHierachyGraph.getMaxHierarchyDepth();
	}

	public int getNumPropertyHierarchyTriples() {
		return numSubpropertyStatements;
	}

	public int getNumPropertiesInHierarchy() {
		return propertyHierachyGraph.getNumNodes();
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;
		String subPropertyOf = Namespace.RDFS.getFullTerm("subPropertyOf");
		
		if (p.getURI().equals(subPropertyOf)) {
			assert o instanceof Resource;
			
			logger.trace("using triple (" + s + " " + p + " " + o + ")");
			
			propertyHierachyGraph.addHierarchyEdge(s.getURI(), ((Resource)o).getURI());
			numSubpropertyStatements++;
		}
		
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("numSubpropertyStatements", Integer.valueOf(getNumPropertyHierarchyTriples()));
		m.put("propertyHierarchyDepth", Integer.valueOf(getPropertyHierarchyDepth()));
		m.put("propertiesInHierarchy", Integer.valueOf(getNumPropertiesInHierarchy()));
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PropertyHierarchy) {
			PropertyHierarchy o2 = (PropertyHierarchy) o;
			return o2.getNumPropertiesInHierarchy() == this.getNumPropertiesInHierarchy() &&
					o2.getNumPropertyHierarchyTriples() == this.getNumPropertyHierarchyTriples() &&
					o2.getPropertyHierarchyDepth() == this.getPropertyHierarchyDepth() &&
					o2.propertyHierachyGraph.equals(this.propertyHierachyGraph);
		}
		else return false;
	}

	@Override
	public void init() {
		numSubpropertyStatements = 0;
		propertyHierachyGraph = new HierarchyGraph<String>();
		
	}

}
