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

public class TriplesPerSubjectClass extends StatisticalCriterion {

	private Map<String, Integer>		triplesPerClass;
	private Map<String, Integer>		triplesPerSubject;
	private Map<String, Set<String>>	classesPerSubject;
	boolean								updated	= true;

	public TriplesPerSubjectClass() {
		logger = Logger.getLogger(TriplesPerSubjectClass.class);
		logger.trace("logger created");
		textId = "TriplesPerSubjectClass";

		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");

		update();

		for (String c : triplesPerClass.keySet()) {
			ps.println(triplesPerClass.get(c) + " triples with subject class "
					+ c);
		}

	}

	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?class") && qs.contains("?numTriplesPerClass")) {
				// valid solution

				String classUri = qs.getResource("?class").getURI();
				Integer numTriplesPerClass = qs.getLiteral(
						"?numTriplesPerClass").getInt();

				increaseTriplesPerClass(classUri, numTriplesPerClass);

			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}
	}

	private void increaseTriplesPerClass(String classUri,
			Integer numTriplesPerClass) {
		Integer numTriples = triplesPerClass.get(classUri);
		if (numTriples == null)
			numTriples = 0;
		triplesPerClass.put(classUri, numTriples + numTriplesPerClass);
	}

	private void increaseTriplesPerSubject(String subjectUri,
			Integer numTriplesPerSubject) {
		Integer numTriples = triplesPerSubject.get(subjectUri);
		if (numTriples == null)
			numTriples = 0;
		triplesPerSubject.put(subjectUri, numTriples + numTriplesPerSubject);
	}

	private void addSubjectClass(String subjectUri, String classUri) {
		Set<String> subjectClasses = classesPerSubject.get(subjectUri);
		if (subjectClasses == null)
			subjectClasses = new HashSet<String>();
		subjectClasses.add(classUri);
		classesPerSubject.put(subjectUri, subjectClasses);
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;

		String subjectUri = s.getURI();
		String propertyUri = p.getURI();

		logger.trace("using triple (" + s + " " + p + " " + o + ")");

		// class instance?
		String type = Namespace.RDF.getFullTerm("type");
		if ((propertyUri.equals(type) || propertyUri.equals("a"))
				&& o.isURIResource()) {

			String classUri = ((Resource) o).getURI();
			// add to subject's classes
			addSubjectClass(subjectUri, classUri);

		}

		// add to subjects triples
		increaseTriplesPerSubject(subjectUri, 1);

		updated = false;

	}

	@Override
	public Map<String, Object> getResultMap() {
		update();

		Map<String, Object> m = new HashMap<String, Object>();
		return m;
	}

	@Override
	public void init() {
		triplesPerClass = new HashMap<String, Integer>();
		triplesPerSubject = new HashMap<String, Integer>();
		classesPerSubject = new HashMap<String, Set<String>>();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TriplesPerSubjectClass) {
			TriplesPerSubjectClass o2 = (TriplesPerSubjectClass) o;
			return o2.triplesPerClass.equals(this.triplesPerClass);
		}
		else return false;
	}

	/**
	 * Only needed for triple stream mode.
	 */
	private void update() {
		if (!updated) {
			triplesPerClass = new HashMap<String, Integer>();

			for (String subject : triplesPerSubject.keySet()) {
				int numTriples = triplesPerSubject.get(subject);
				Set<String> subjectClasses = classesPerSubject.get(subject);

				if (subjectClasses != null)
					for (String c : subjectClasses) {
						increaseTriplesPerClass(c, numTriples);
					}
			}

			updated = true;
		}
	}

}
