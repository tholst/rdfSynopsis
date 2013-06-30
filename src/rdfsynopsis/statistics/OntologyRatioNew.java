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

public class OntologyRatioNew extends StatisticalCriterion {

	private int			numDefClasses		= 0;
	private int			numDefProperties	= 0;
	private int			numTypedResources	= 0;
	private int			numDefConcepts	= 0;
	private Set<String>	defClasses;
	private Set<String>	defProperties;
	private Set<String>	defConcepts;
	private Set<String>	typedResources;

	public OntologyRatioNew() {
		logger = Logger.getLogger(OntologyRatioNew.class);
		logger.trace("logger created");
		textId = "OntologyRatioNew";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: ontologyRatio = " + getOntologyRatio() + " (|CL|="
				+ numDefClasses + "; |CO|="	+ numDefConcepts + "; |P|=" + numDefProperties + "; |I|="
				+ (numTypedResources) + ")");
	}

	public double getOntologyRatio() {
		assert numDefClasses != -1;
		assert numDefConcepts != -1;
		assert numDefProperties != -1;
		assert numTypedResources != -1;

		if (numTypedResources == 0)
			return 0.0;
		else return (double) (numDefClasses + numDefConcepts +numDefProperties)
				/ numTypedResources;
	}


	@Override
	protected void processQueryResults(ResultSet results) {
		assert results != null;
		logger.trace("process query results");

		if (results.hasNext()) { // result set not empty
			QuerySolution qs = results.next();

			if (qs.contains("?numDefClasses")
					|| qs.contains("?numDefConcepts")
					|| qs.contains("?numDefProperties")
					|| qs.contains("?numTypedResources")) { // valid solution

				if (qs.contains("?numDefClasses"))
					numDefClasses = qs.getLiteral("?numDefClasses").getInt();
				else numDefClasses = 0;

				if (qs.contains("?numDefConcepts"))
					numDefClasses = qs.getLiteral("?numDefConcepts").getInt();
				else numDefClasses = 0;

				if (qs.contains("?numDefProperties"))
					numDefProperties = qs.getLiteral("?numDefProperties")
							.getInt();
				else numDefProperties = 0;

				if (qs.contains("?numTypedResources"))
					numTypedResources = qs.getLiteral("?numTypedResources")
							.getInt();
				else numTypedResources = 0;

				logger.debug("Query results: numDefClasses=" + numDefClasses
						+ "; numDefConcepts=" + numDefConcepts
						+ "; numDefProperties=" + numDefProperties + "; " +
						"numTypedResources=" + numTypedResources);
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

		String type = Namespace.RDF.getFullTerm("type");
		if ((p.getURI().equals(type) || p.getURI().equals("a"))) {
			logger.trace("using triple (" + s + " " + p + " " + o + ")");

			// add typed subject (not type rdfs:Resource)
			if (!o.isURIResource() || !((Resource) o).getURI().equals(Namespace.RDFS.getFullTerm("Resource")))
				typedResources.add(s.toString());

			if (o.isURIResource()) {
				Resource oRes = (Resource) o;

				// handle def classes
				if (oRes.getURI().equals(Namespace.RDFS.getFullTerm("Class"))
						|| oRes.getURI().equals(Namespace.OWL.getFullTerm("Class")))
					defClasses.add(s.toString());
				
				// handle def concepts
				if (oRes.getURI().equals(Namespace.SKOS.getFullTerm("Concept")))
					defConcepts.add(s.toString());

				// handle def properties
				if (oRes.getURI().equals(Namespace.RDF.getFullTerm("Property")))
					defProperties.add(s.toString());
			}
			
			// update counters
			numDefClasses = defClasses.size();
			numDefConcepts = defConcepts.size();
			numDefProperties = defProperties.size();
			numTypedResources = typedResources.size();
		}

	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("OntologyRatio", Double.valueOf(getOntologyRatio()));
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof OntologyRatioNew) {
			OntologyRatioNew o2 = (OntologyRatioNew) o;
			return Double.doubleToLongBits(o2.getOntologyRatio()) == Double.doubleToLongBits(this.getOntologyRatio());
		}
		else return false;
	}

	@Override
	public void init() {
		numDefClasses		= 0;
		numDefProperties	= 0;
		numDefConcepts		= 0;
		numTypedResources	= 0;
		defClasses = new HashSet<String>();
		defConcepts = new HashSet<String>();
		defProperties = new HashSet<String>();
		typedResources = new HashSet<String>();		
	}

}
