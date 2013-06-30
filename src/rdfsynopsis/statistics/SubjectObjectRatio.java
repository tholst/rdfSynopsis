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

public class SubjectObjectRatio extends StatisticalCriterion {

	private int			numDistinctObjectsSubjects	= -1;
	private int			numCommonObjectSubjects		= -1;
	private Set<String>	distinctSubjects;
	private Set<String>	distinctObjects;
	private Set<String>	distinctCommonSubjectObjects;

	public SubjectObjectRatio() {
		logger = Logger.getLogger(SubjectObjectRatio.class);
		logger.trace("logger created");
		textId = "SubjectObjectRatio";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: SubjectObjectRatio = " + getSubjectObjectRatio()
				+ " (|S u O|="
				+ numDistinctObjectsSubjects + "; |S n O|="
				+ numCommonObjectSubjects + ")");
	}

	public double getSubjectObjectRatio() {
		assert numDistinctObjectsSubjects != -1;
		assert numCommonObjectSubjects != -1;
		assert numCommonObjectSubjects <= numDistinctObjectsSubjects;

		if (numDistinctObjectsSubjects == 0)
			return 0.0;
		else return (double) numCommonObjectSubjects
				/ numDistinctObjectsSubjects;
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;
		logger.trace("process query results");

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?numDistinctObjectsSubjects")
					|| qs.contains("?numCommonObjectSubjects")) { // valid
																	// solution

				if (qs.contains("?numDistinctObjectsSubjects"))
					numDistinctObjectsSubjects = qs.getLiteral(
							"?numDistinctObjectsSubjects").getInt();
				else numDistinctObjectsSubjects = 0;

				if (qs.contains("?numCommonObjectSubjects"))
					numCommonObjectSubjects = qs.getLiteral(
							"?numCommonObjectSubjects")
							.getInt();
				else numCommonObjectSubjects = 0;

				logger.debug("Query results: numDistinctObjectsSubjects="
						+ numDistinctObjectsSubjects
						+ "; numCommonObjectSubjects="
						+ numCommonObjectSubjects);
			} else // invalid solution
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

		// handle triple's subject
		if (s.isURIResource()
				&& !distinctCommonSubjectObjects.contains(s.getURI())) {
			logger.trace("using triple's subject (" + s + " " + p + " " + o
					+ ")");
			if (distinctObjects.contains(s.getURI())) {
				distinctCommonSubjectObjects.add(s.getURI());
			} else distinctSubjects.add(s.getURI());
		}

		// handle triple's object
		if (o.isURIResource()) {
			Resource oRes = (Resource) o;
			if (!distinctCommonSubjectObjects.contains(oRes.getURI())) {
				logger.trace("using triple's object (" + s + " " + p + " " + o
						+ ")");
				if (distinctSubjects.contains(oRes.getURI())) {
					distinctCommonSubjectObjects.add(oRes.getURI());
				} else distinctObjects.add(oRes.getURI());
			}
		}

		// update counters
		numCommonObjectSubjects = distinctCommonSubjectObjects.size();
		numDistinctObjectsSubjects = distinctObjects.size()
				+ distinctSubjects.size();

	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("SubjectObjectRatio", Double.valueOf(getSubjectObjectRatio()));
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SubjectObjectRatio) {
			SubjectObjectRatio sor2 = (SubjectObjectRatio) o;
			return Double.doubleToLongBits(sor2.getSubjectObjectRatio()) == 
						Double.doubleToLongBits(this.getSubjectObjectRatio())
					&&
					sor2.getNumCommonSubjectObjectURIs() == 
						this.getNumCommonSubjectObjectURIs()
					&&
					sor2.getNumDistinctSubjectObjectURIs() == 
						this.getNumDistinctSubjectObjectURIs();
		}
		else return false;
	}

	public int getNumCommonSubjectObjectURIs() {
		return numCommonObjectSubjects;
	}

	public int getNumDistinctSubjectObjectURIs() {
		return numDistinctObjectsSubjects;
	}

	@Override
	public void init() {
		numDistinctObjectsSubjects	= -1;
		numCommonObjectSubjects		= -1;
		distinctSubjects = new HashSet<String>();
		distinctObjects = new HashSet<String>();
		distinctCommonSubjectObjects = new HashSet<String>();
	}
}
