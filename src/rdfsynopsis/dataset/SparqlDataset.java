package rdfsynopsis.dataset;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

public abstract class SparqlDataset extends Dataset {	
	public abstract QueryExecution query(Query query);
}
