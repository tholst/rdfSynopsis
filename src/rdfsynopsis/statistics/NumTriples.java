package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class NumTriples extends StatisticalCriterion {

	private int numTriples = -1;

	public NumTriples() {
		logger = Logger.getLogger(NumTriples.class);
		logger.trace("logger created");
		textId = "NumTriples";
		init();
	}
	

	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?numTriples")) { // result contains number of triples
				numTriples = qs.getLiteral("?numTriples").getInt();
				// output query results
				logger.debug("Query result: numTriples = " + numTriples);
			} else
				// result doesn't contain number of triples
				logger.error("ResultSet was empty, query not executed?");

		} else
			// result set empty
			logger.error("ResultSet was empty, query not executed?");
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of triples = " + numTriples);
	}

	public int getNumTriples() {
		return numTriples;
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		// initialize
		if (numTriples == -1)
			numTriples = 0;
		// count triple
		numTriples++;
		
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("numTriples", Integer.valueOf(numTriples));
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NumTriples) {
			NumTriples o2 = (NumTriples) o;
			return o2.getNumTriples() == this.getNumTriples();
		}
		else return false;
	}

	@Override
	public void init() {
		numTriples = -1;		
	}
}
