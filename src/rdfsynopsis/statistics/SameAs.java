package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SameAs extends StatisticalCriterion {

	private int	numSameAsTriples	= -1;

	public SameAs() {
		logger = Logger.getLogger(SameAs.class);
		logger.trace("logger created");
		textId = "SameAs";
		init();
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?sameAs")) { // result contains number of sameAs
											// triples
				numSameAsTriples = qs.getLiteral("?sameAs").getInt();
				// output query results
				logger.debug("Query result: numSameAsTriples = "
						+ numSameAsTriples);
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
		String sameAs = Namespace.OWL.getFullTerm("sameAs");

		if (numSameAsTriples == -1)
			numSameAsTriples = 0;

		if (p.getURI().equals(sameAs)) {
			logger.trace("using triple (" + s + " " + p + " " + o + ")");
			numSameAsTriples++;
		}

	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of sameAs triples = " + numSameAsTriples);
	}

	public int getNumSameAsTriples() {
		return numSameAsTriples;
	}

	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("SameAsTriples", Integer.valueOf(getNumSameAsTriples()));
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SameAs) {
			SameAs o2 = (SameAs) o;
			return o2.getNumSameAsTriples() == this.getNumSameAsTriples();
		}
		else return false;
	}

	@Override
	public void init() {
		numSameAsTriples = -1;

	}
}
