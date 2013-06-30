package rdfsynopsis.dataset;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

@RunWith(JUnit4.class)
public class SparqlEndpointDataset extends SparqlDataset {
	
	private String sparqlEndpointUri;

	public SparqlEndpointDataset(String sparqlEndpointUri) {
		logger = Logger.getLogger(SparqlEndpointDataset.class);
		this.sparqlEndpointUri = sparqlEndpointUri;
	}

	public QueryExecution query(Query query) {
		return QueryExecutionFactory.sparqlService(sparqlEndpointUri, query);
	}
}
