package rdfsynopsis.eval;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.SparqlDataset;
import rdfsynopsis.statistics.NumTriples;
import rdfsynopsis.statistics.StatisticalCriterion;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class StreamAnalysisLogger extends AbstractAnalysisLogger {

	private SparqlDataset	ds;
	private int				numTriples				= -1;
	private int				numTriplesConsidered	= 0;

	private int		tripleLimit			= 30000;

	public int getTripleLimit() {
		return tripleLimit;
	}

	public StreamAnalysisLogger setTripleLimit(int tripleLimit) {
		this.tripleLimit = tripleLimit;
		return this;
	}
	
	public StreamAnalysisLogger(String title, boolean timeStamp) {
		super(title, timeStamp);
		logger = Logger.getLogger(StreamAnalysisLogger.class);
	}
	
	public SparqlDataset getDs() {
		return ds;
	}

	public StreamAnalysisLogger setDs(SparqlDataset ds) {
		this.ds = ds;
		return this;
	}

	@Override
	public void performAnalysis(PrintStream ps) {
		if (ps == null)
			ps = System.out;
		
		NumTriples nt = new NumTriples();
		nt.processSparqlDataset(ds);
		numTriples = nt.getNumTriples();

		String queryString = "SELECT ?s ?p ?o\n" +
				"WHERE {?s ?p ?o.}\n" +
				"ORDER BY ?s\n" +
				"LIMIT " + tripleLimit + "\n" +
				"OFFSET ";

		for (int offset = 0; offset < numTriples; offset += tripleLimit) {
			
			// Create a new query
			Query query = QueryFactory.create(queryString + offset);
			
			// execute query and obtain results
			QueryExecution qe = ds.query(query);
			logger.debug("SELECT all triples LIMIT " + tripleLimit
					+ " OFFSET " + offset);
			ResultSet results = qe.execSelect();
			
			// process query results
			logger.trace("process query results");			
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				if (qs.contains("?s") && qs.contains("?p") && qs.contains("?o")) { // valid
																					// solution
					Resource s = qs.getResource("?s");
					Resource pRes = qs.getResource("?p");
					Property p = ResourceFactory.createProperty(pRes.getURI());
					RDFNode o = qs.get("?o");
					logger.trace("considering triple (" + s + " " + p + " " + o
							+ ")");
					numTriplesConsidered++;

					for (StatisticalCriterion sc : criteria) {
						sc.considerTriple(s, p, o);
					}

				} else // invalid solution
				logger.debug("invalid solution: " + qs);
			}
			
			// Important - free up resources used running the query
			qe.close();	
		}
		
		// write to log
		String headerLine = "";
		String resultLine = "";
		
		for (StatisticalCriterion sc : criteria) {
			sc.processSparqlDataset(ds);
			//sc.flushLog();
			Map<String,Object> results = sc.getResultMap();
			for (Map.Entry<String, Object> entry : results.entrySet()) {
				headerLine += entry.getKey() + " ";
				resultLine += entry.getValue() + " ";
			}			
		}
		writeLogLine(headerLine);
		writeLogLine(resultLine);
	}

}
