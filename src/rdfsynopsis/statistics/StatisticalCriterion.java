package rdfsynopsis.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import rdfsynopsis.dataset.SparqlDataset;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class StatisticalCriterion {

	Logger logger;
	String textId;
	
	// print current measurements
	abstract public void flushLog(PrintStream ps);
	
	// process query results (SQA)
	abstract void processQueryResults(ResultSet results);
	
	// filter triple (TSA)
	public abstract void considerTriple(Resource s, Property p, RDFNode o);
	
	// turn analysis results into (String->Value) Map
	public abstract Map<String,Object> getResultMap();
	
	// init criterion for new analysis
	public abstract void init();
	
	@Override
	public abstract boolean equals(Object arg0);
	
	/**
	 * Load <textId>.sparql File and combine with prefixes.sparql
	 * @return prefixes + sparql query
	 */
	protected String getQueryString() {
		String prefixes = "";
		String text = "";
		File path = new File("src"+ File.separator + "rdfsynopsis"+ File.separator + "statistics"+ File.separator + "SPARQL");
		
		try {
			text = new Scanner( new File(path, textId+".sparql"), "UTF-8" ).useDelimiter("\\Z").next();
			prefixes = new Scanner( new File(path,"prefixes.sparql"), "UTF-8" ).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return prefixes+"\n"+text;
	}
	
	 /**
	  * query dataset (SQA)
	  * @param ds
	  */
	public void processSparqlDataset(SparqlDataset ds) {
		logger.trace("in processSparqlDataset");
		
		// delegate query formulation to subclass
		String queryString = getQueryString();
		logger.debug("Query is:\n"+queryString);
		
		// Create a new query
		Query query = QueryFactory.create(queryString);
		
		// execute query and obtain results
		QueryExecution qe = ds.query(query);
		ResultSet results = qe.execSelect();
		// delegate processing of query results to subclass
		processQueryResults(results);
	
		// Important - free up resources used running the query
		qe.close();		
	}
	
	
}
