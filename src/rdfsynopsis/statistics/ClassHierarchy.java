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

public class ClassHierarchy extends StatisticalCriterion {

	private HierarchyGraph<String>	classHierachyGraph;
	private int						numSubclassStatements;

	public ClassHierarchy() {
		logger = Logger.getLogger(ClassHierarchy.class);
		logger.trace("logger created");
		textId = "ClassHierarchy";
		
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of classes in hierarchy = " + getNumClassesInHierarchy());
		ps.println("Result: max depth in hierarchy = " + getClassHierarchyDepth());
		ps.println("Result: class hierarchy = " + classHierachyGraph);

	}
	
	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?subclass") && qs.contains("?superclass")) {
				// valid solution

				String subClassUri = qs.getResource("?subclass").getURI();
				String superClassUri = qs.getResource("?superclass").getURI();

				classHierachyGraph.addHierarchyEdge(subClassUri, superClassUri);
				numSubclassStatements++;

			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}

		// debug result output
		logger.debug("Class Hierarchy:\n" + classHierachyGraph);
	}

	public int getClassHierarchyDepth() {
		return classHierachyGraph.getMaxHierarchyDepth();
	}

	public int getNumClassHierarchyTriples() {
		return numSubclassStatements;
	}

	public int getNumClassesInHierarchy() {
		return classHierachyGraph.getNumNodes();
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;
		String subClassOf = Namespace.RDFS.getFullTerm("subClassOf");		
		
		if (p.getURI().equals(subClassOf) && !s.toString().equals(((Resource)o).toString())
				&& ! ((Resource)o).toString().equals(Namespace.RDFS.getFullTerm("Resource"))) {
			assert o instanceof Resource;
			
			logger.trace("using triple (" + s + " " + p + " " + o + ")");
			
			classHierachyGraph.addHierarchyEdge(s.getURI(), ((Resource)o).getURI());
			numSubclassStatements++;
		}
		
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("numSubclassStatements", Integer.valueOf(numSubclassStatements));
		m.put("classHierarchyDepth", Integer.valueOf(getClassHierarchyDepth()));
		m.put("classesInHierarchy", Integer.valueOf(getNumClassesInHierarchy()));
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ClassHierarchy) {
			ClassHierarchy o2 = (ClassHierarchy) o;
			return o2.getClassHierarchyDepth() == this.getClassHierarchyDepth() &&
					o2.getNumClassesInHierarchy() == this.getNumClassesInHierarchy() &&
					o2.getNumClassHierarchyTriples() == this.getNumClassHierarchyTriples() &&
					o2.classHierachyGraph.equals(this.classHierachyGraph);
		}
		else return false;
	}

	@Override
	public void init() {
		numSubclassStatements = 0;
		classHierachyGraph = new HierarchyGraph<String>();
	}

}
