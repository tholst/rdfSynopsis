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

public class TypedSubjectRatio extends StatisticalCriterion {

	private int	numSubjects		= -1;
	private int	numTypedSubjects	= -1;
	private Set<String> subjectUris;
	private Set<String> onlyTypedSubjectUris;
	private Set<String> typedSubjectUris;

	public TypedSubjectRatio() {
		logger = Logger.getLogger(TypedSubjectRatio.class);
		logger.trace("logger created");
		textId = "TypedSubjectRatio";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: TypedSubjectRatio = " + getTypedSubjectRatio() + " (|S|="
				+ numSubjects + "; |tS|=" + numTypedSubjects + ")");
	}

	public double getTypedSubjectRatio() {
		assert numSubjects != -1;
		assert numTypedSubjects != -1;
		assert numTypedSubjects <= numSubjects;
		
		if (numSubjects == 0)
			return 0.0;
		else return (double) numTypedSubjects / numSubjects;
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;
		logger.trace("process query results");

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?numSubjects")
					|| qs.contains("?numTypedSubjects")) { // valid solution

				if (qs.contains("?numSubjects"))
					numSubjects = qs.getLiteral("?numSubjects").getInt();
				else numSubjects = 0;

				if (qs.contains("?numTypedSubjects"))
					numTypedSubjects = qs.getLiteral("?numTypedSubjects")
							.getInt();
				else numTypedSubjects = 0;

				logger.debug("Query results: numSubjects=" + numSubjects
						+ "; numTypedSubjects=" + numTypedSubjects);
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

		logger.trace("using triple (" + s + " " + p + " " + o + ")");				

		String type = Namespace.RDF.getFullTerm("type");
		if (p.getURI().equals(type) || p.getURI().equals("a")) {

			// only add if class is NOT rdfs:Resource
			if (o.isURIResource() && !((Resource) o).getURI().equals(Namespace.RDFS.getFullTerm("Resource")) )
				// add to typedSubjects only if already occured in non-type triple
				if (subjectUris.contains(s.getURI()))
					typedSubjectUris.add(s.getURI());
				else onlyTypedSubjectUris.add(s.getURI());
					
		} else {
			// add to typed subject if previously occured in type triple
			if (onlyTypedSubjectUris.contains(s.getURI()))
				typedSubjectUris.add(s.getURI());
			subjectUris.add(s.getURI());
		}
		
		numSubjects = subjectUris.size();
		numTypedSubjects = typedSubjectUris.size();
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("TypedSubjectRatio", Double.valueOf(getTypedSubjectRatio()));
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TypedSubjectRatio) {
			TypedSubjectRatio o2 = (TypedSubjectRatio) o;
			return Double.doubleToLongBits(o2.getTypedSubjectRatio()) == 
					Double.doubleToLongBits(this.getTypedSubjectRatio());
		}
		else return false;
	}

	@Override
	public void init() {
		numSubjects		= -1;
		numTypedSubjects	= -1;
		subjectUris = new HashSet<String>();
		onlyTypedSubjectUris = new HashSet<String>();
		typedSubjectUris = new HashSet<String>();
		
	}

}

