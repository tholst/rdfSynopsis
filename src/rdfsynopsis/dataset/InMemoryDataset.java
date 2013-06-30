package rdfsynopsis.dataset;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class InMemoryDataset extends SparqlDataset {

	private Model	model;
	private String toStringFormat;

	public InMemoryDataset() {
		logger = Logger.getLogger(InMemoryDataset.class);
		model = ModelFactory.createDefaultModel();
		toStringFormat = "TTL";
	}

	public InMemoryDataset(String filename) {
		this(filename, null);
	}
	
	public InMemoryDataset(String filename, String lang) {
		this();
		// open file
		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			logger.error("file " + filename + " not found.");
			throw new IllegalArgumentException("File: " + filename
					+ " not found");
		}
		logger.debug("file " + filename + " opened.");

		model.read(in, null, lang);
		logger.info("InMemDS (" + filename + ") loaded. Size is "
				+ model.size());
	}

	public QueryExecution query(Query query) {
		return QueryExecutionFactory.create(query, model);
	}

	public Model getModel() {
		return model;
	}

	public String getToStringFormat() {
		return toStringFormat;
	}

	public void setToStringFormat(String toStringFormat) {
		this.toStringFormat = toStringFormat;
	}

	@Override
	public String toString() {
		
		// output graph for debugging
		StringWriter out = new StringWriter();
		getModel().write(out, toStringFormat);
		return super.toString() + out.toString();

	}
}
