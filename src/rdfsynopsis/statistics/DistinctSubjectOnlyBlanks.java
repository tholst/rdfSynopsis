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

public class DistinctSubjectOnlyBlanks extends StatisticalCriterion {

	private int			numSubjectOnlyBlanks	= -1;
	private Set<String>	subjectOnlyBlanks;
	private Set<String>	objectBlanks;

	public DistinctSubjectOnlyBlanks() {
		logger = Logger.getLogger(DistinctSubjectOnlyBlanks.class);
		logger.trace("logger created");
		textId = "DistinctSubjectOnlyBlanks";
		init();
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?numSubjectOnlyBlanks")) { // result contains
														// number of blanks
				numSubjectOnlyBlanks = qs.getLiteral("?numSubjectOnlyBlanks")
						.getInt();
				// output query results
				logger.debug("Query result: numSubjectOnlyBlanks = "
						+ numSubjectOnlyBlanks);
			} else
			// result doesn't contain number of triples
			logger.debug("invalid solution: " + qs);

		} else
		// result set empty
		logger.error("ResultSet was empty, query not executed?");
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;
		
		if (numSubjectOnlyBlanks == -1)
			numSubjectOnlyBlanks = 0;

		// subject
		if (s.isAnon() && !objectBlanks.contains(s.getId().getLabelString())) {
			logger.trace("using triple's subject (" + s + " " + p + " " + o + ")");

			subjectOnlyBlanks.add(s.getId().getLabelString());
			numSubjectOnlyBlanks = subjectOnlyBlanks.size();
		}

		// object
		if (o.isAnon()) {
			Resource oRes = (Resource)o;
			logger.trace("using triple's object (" + s + " " + p + " " + o + ")");
			
			objectBlanks.add(oRes.getId().getLabelString());
			subjectOnlyBlanks.remove(oRes.getId().getLabelString());
			numSubjectOnlyBlanks = subjectOnlyBlanks.size();
		}

	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of distinct subject only blank nodes = "
				+ numSubjectOnlyBlanks);
	}

	public int getNumDistinctSubjectOnlyBlanks() {
		return numSubjectOnlyBlanks;
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("DistinctSubjectOnlyBlanks", Integer.valueOf(getNumDistinctSubjectOnlyBlanks()));
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DistinctSubjectOnlyBlanks) {
			DistinctSubjectOnlyBlanks o2 = (DistinctSubjectOnlyBlanks) o;
			return o2.getNumDistinctSubjectOnlyBlanks() == this.getNumDistinctSubjectOnlyBlanks();
		}
		else return false;
	}

	@Override
	public void init() {
		numSubjectOnlyBlanks = -1;
		subjectOnlyBlanks = new HashSet<String>();
		objectBlanks = new HashSet<String>();
	}
}
