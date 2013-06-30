package rdfsynopsis.analyzer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import rdfsynopsis.dataset.SparqlDataset;
import rdfsynopsis.eval.PartialStreamAnalysisLogger;
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

public class TripleStreamAnalyzer extends AbstractAnalyzer {
	
	public final static String BySubject = "?subject";
	public final static String ByPredicate = "?predicate";
	public final static String ByObject = "?object";

	private int				numTriples				= -1;
	private int				numTriplesConsidered	= 0;

	private int									tripleLimit				= 30000;
	private boolean 							randomSampling = false;
	private String orderByClause = BySubject;
	
	
	public String getOrderByClause() {
		return orderByClause;
	}

	public TripleStreamAnalyzer setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
		return this;
	}

	public boolean isRandomSampling() {
		return randomSampling;
	}

	public TripleStreamAnalyzer setRandomSampling(boolean randomSampling) {
		this.randomSampling = randomSampling;
		return this;
	}

	public int getTripleLimit() {
		return tripleLimit;
	}

	public AbstractAnalyzer setTripleLimit(int tripleLimit) {
		this.tripleLimit = tripleLimit;
		return this;
	}
	
	public TripleStreamAnalyzer() {
		this(null);
	}

	public TripleStreamAnalyzer(SparqlDataset ds) {
		logger = Logger.getLogger(TripleStreamAnalyzer.class);
		this.ds = ds;
	}

	@Override
	public void performAnalysis(PrintStream ps) {
		if (ps == null)
			ps = System.out;
		
		// perform triples sparql query
		NumTriples nt = new NumTriples();
		nt.processSparqlDataset(ds);
		numTriples = nt.getNumTriples();
		
		// precompute list of offsets
		List<Integer> offsets = new ArrayList<Integer>((int) Math.ceil((double) numTriples/tripleLimit));
		for (int offset = 0; offset < numTriples; offset += tripleLimit) {
			offsets.add(offset);
		}
		
		// optional randomization of offsets
		if (randomSampling)
		        Collections.shuffle(offsets);


		String queryString = "SELECT ?subject ?predicate ?object\n" +
				"WHERE {?subject ?predicate ?object.}\n" +
				"ORDER BY "+ orderByClause +"\n" +
				"LIMIT " + tripleLimit + "\n" +
				"OFFSET ";

		for (Integer offset : offsets) {
			
			// Create a new query
			Query query = QueryFactory.create(queryString + offset);
			
			// execute query and obtain results
			QueryExecution qe = ds.query(query);
			logger.trace("TripleStream: limit=" + tripleLimit
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

					// let all criteria filter the triples
					for (StatisticalCriterion sc : criteria) {
						sc.considerTriple(s, p, o);
					}

				} else // invalid solution
				logger.debug("invalid solution: " + qs);
			}
			
			// Important - free up resources used running the query
			qe.close();	
		}

		// flush logs for all criteria
		logger.debug("flushing logs of statistical criteria");
		for (StatisticalCriterion sc : criteria) {
			sc.flushLog(ps);
		}
		ps.flush();

	}

}
