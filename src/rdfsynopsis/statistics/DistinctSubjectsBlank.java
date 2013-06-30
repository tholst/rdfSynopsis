package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class DistinctSubjectsBlank extends StatisticalCriterion {

	private int numSubjectsBlank = -1;
	private Set<String> blankSubjects;

	public DistinctSubjectsBlank() {
		logger = Logger.getLogger(DistinctSubjectsBlank.class);
		logger.trace("logger created");
		textId = "DistinctSubjectsBlank";
		init();
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?numBlanks")) { // result contains number of blanks
				numSubjectsBlank = qs.getLiteral("?numBlanks").getInt();
				// output query results
				logger.debug("Query result: numBlanks = " + numSubjectsBlank);
			} else
				// result doesn't contain number of triples
				logger.error("ResultSet was empty, query not executed?");

		} else
			// result set empty
			logger.error("ResultSet was empty, query not executed?");
	}
	
	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;
		
		if (numSubjectsBlank == -1)
			numSubjectsBlank = 0;
		
		if (s.isAnon()) {
			logger.trace("using triple (" + s + " " + p + " " + o + ")");
			blankSubjects.add(s.getId().getLabelString());
			numSubjectsBlank = blankSubjects.size();
		}
		
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of distinct blank nodes = " + numSubjectsBlank);
	}

	public int getNumDistinctSubjectsBlank() {
		return numSubjectsBlank;
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("DistinctSubjectsBlank", Integer.valueOf(getNumDistinctSubjectsBlank()));
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DistinctSubjectsBlank) {
			DistinctSubjectsBlank o2 = (DistinctSubjectsBlank) o;
			return o2.getNumDistinctSubjectsBlank() == this.getNumDistinctSubjectsBlank();
		}
		else return false;
	}

	@Override
	public void init() {
		numSubjectsBlank = -1;
		blankSubjects = new HashSet<String>();		
	}

}
