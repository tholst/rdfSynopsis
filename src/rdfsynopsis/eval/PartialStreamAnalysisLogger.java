package rdfsynopsis.eval;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

public class PartialStreamAnalysisLogger extends AbstractAnalysisLogger {
	
	public final static String BySubject = "?subject";
	public final static String ByPredicate = "?predicate";
	public final static String ByObject = "?object";

	private SparqlDataset						ds;
	private int									numTriples				= -1;
	private int									numTriplesConsidered	= 0;
	private int									tripleLimit				= 30000;
	private Map<String, Map<Integer, Object>>	offsetResultSeries;
	private boolean 							randomSampling = false;
	private String orderByClause = BySubject;
	
	

	public String getOrderByClause() {
		return orderByClause;
	}

	public PartialStreamAnalysisLogger setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
		return this;
	}

	public boolean isRandomSampling() {
		return randomSampling;
	}

	public PartialStreamAnalysisLogger setRandomSampling(boolean randomSampling) {
		this.randomSampling = randomSampling;
		return this;
	}

	public int getTripleLimit() {
		return tripleLimit;
	}

	public PartialStreamAnalysisLogger setTripleLimit(int tripleLimit) {
		this.tripleLimit = tripleLimit;
		return this;
	}

	public PartialStreamAnalysisLogger(String title, boolean timeStamp) {
		super(title, timeStamp);
		logger = Logger.getLogger(PartialStreamAnalysisLogger.class);
	}
	
	public PartialStreamAnalysisLogger(String title) {
		this(title, true);
	}

	public SparqlDataset getDs() {
		return ds;
	}

	public PartialStreamAnalysisLogger setDs(SparqlDataset ds) {
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

		numTriplesConsidered = 0;
		offsetResultSeries = new LinkedHashMap<String, Map<Integer, Object>>();
		long startTime = System.currentTimeMillis();
		
		List<Integer> offsets = new ArrayList<Integer>((int) Math.ceil((double) numTriples/tripleLimit));
		for (int offset = 0; offset < numTriples; offset += tripleLimit) {
			offsets.add(offset);
		}
		
		if (randomSampling)
		        Collections.shuffle(offsets);
//		System.out.println(offsets);
		
		// write header and first line
//		logLine(0, 0L);

		String queryString = "SELECT ?subject ?predicate ?object\n" +
				"WHERE {?subject ?predicate ?object.}\n" +
				"ORDER BY "+ orderByClause +"\n" +
				"LIMIT " + tripleLimit + "\n" +
				"OFFSET ";

		for (Integer offset : offsets) {
			
			// Create a new query
			Query query = QueryFactory.create(queryString + offset);
			logger.debug("query is \n" + queryString + offset);
			
			// execute query and obtain results
			QueryExecution qe = ds.query(query);
			logger.info("TripleStream: limit=" + tripleLimit
					+ ", offset=" + offset + " at " + System.currentTimeMillis()/1000);
			ResultSet results = qe.execSelect();
			
			// process query results
			logger.trace("process query results");			
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				if (qs.contains("?subject") && qs.contains("?predicate") && qs.contains("?object")) { // valid
																					// solution
					Resource s = qs.getResource("?subject");
					Resource pRes = qs.getResource("?predicate");
					Property p = ResourceFactory.createProperty(pRes.getURI());
					RDFNode o = qs.get("?object");
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
			
			// log results
			logLine(offset, System.currentTimeMillis() - startTime);
		}

		// finally write Log
		writeResultLog();
	}

	private void logLine(int offset, long time) {
		// log time
		addResultValue(offset, "Offset", Integer.valueOf(offset));
		addResultValue(offset, "TimeMS", Long.valueOf(time));

		// log statistical results
		for (StatisticalCriterion sc : criteria) {
			Map<String, Object> results = sc.getResultMap();
			for (Map.Entry<String, Object> entry : results.entrySet()) {
				addResultValue(offset, entry.getKey(), entry.getValue());
			}
		}
	}

	private void addResultValue(int offset, String resultKey,
			Object resultValue) {
		Map<Integer, Object> ps = offsetResultSeries.get(resultKey);
		if (ps == null)
			ps = new LinkedHashMap<Integer, Object>();
		ps.put(offset, resultValue);
		offsetResultSeries.put(resultKey, ps);
	}

	private void writeResultLog() {
		String headerLine = "";
		// build header
		for (String resultKey : offsetResultSeries.keySet()) {
			headerLine += resultKey + ", ";
		}
		writeLogLine(headerLine);

		// iterate over percentages
		for (Map.Entry<Integer, Object> entry : offsetResultSeries.get(
				"Offset").entrySet()) {
			int offset = entry.getKey();
			String resultLine = "";

			for (Map.Entry<String, Map<Integer, Object>> resultSeries : offsetResultSeries
					.entrySet()) {
				Object val = resultSeries.getValue().get(offset);
				resultLine += val + ", ";
			}

			writeLogLine(resultLine);
		}
	}

}
