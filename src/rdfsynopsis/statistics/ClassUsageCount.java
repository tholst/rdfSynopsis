package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class ClassUsageCount extends StatisticalCriterion {
	
	private Map<String, Integer> classUsageMap;
	
	
	public ClassUsageCount() {
		logger = Logger.getLogger(ClassUsageCount.class);
		logger.trace("logger created");
		textId = "ClassUsageCount";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of classes used = " + getNumUsedClasses());
		for (Map.Entry<String, Integer> e : classUsageMap.entrySet()) {
			ps.println("Result: " + e.getValue() + " instances  for class " + e.getKey());
		}


	}


	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");
		
		for (;results.hasNext();) {
			QuerySolution qs = results.next();
			
			if (qs.contains("?class") && qs.contains("?numInstances")) { // valid solution
				String classUri = qs.getResource("?class").getURI();
				int numInstances = qs.getLiteral("?numInstances").getInt();
				classUsageMap.put(classUri, numInstances);
			} else // invalid solution
				logger.debug("invalid solution: " + qs);
					
		}
		
		logger.debug("classesUsageMap:\n"+classUsageMap.toString());
	}
	

	
	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;
		String type = Namespace.RDF.getFullTerm("type");
		
		if (p.getURI().equals(type) || p.getURI().equals("a")) {
			assert o instanceof Resource;
			logger.trace("using triple (" + s + " " + p + " " + o + ")");
			
			String classUri = ((Resource)o).getURI();
			
			Integer numInstances = classUsageMap.get(classUri);
			if (numInstances == null)
				numInstances = 0;
			classUsageMap.put(classUri,numInstances+1);
		}
		
	}

	public long getNumInstances(String classUri) {
		if (classUsageMap.containsKey(classUri))
			return classUsageMap.get(classUri).longValue();
		else return 0;
	}
	
	public long getNumInstances(Resource classRes) {
		return getNumInstances(classRes.getURI());
	}

	public int getNumUsedClasses() {
		return classUsageMap.size();
	}

	public Set<String> getClassUris() {
		return classUsageMap.keySet();
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Map.Entry<String, Integer> e : classUsageMap.entrySet()) {
			m.put(e.getKey()+"_classUsage", e.getValue());
		}
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ClassUsageCount) {
			ClassUsageCount o2 = (ClassUsageCount) o;
			return o2.getNumUsedClasses() == this.getNumUsedClasses() &&
					o2.classUsageMap.equals(this.classUsageMap);
		}
		else return false;
	}

	@Override
	public void init() {		
		classUsageMap = new HashMap<String, Integer>();
	}
	

}
