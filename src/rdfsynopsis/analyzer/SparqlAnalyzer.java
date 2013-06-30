package rdfsynopsis.analyzer;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import rdfsynopsis.dataset.SparqlDataset;
import rdfsynopsis.statistics.StatisticalCriterion;

public class SparqlAnalyzer extends AbstractAnalyzer {
	
	public SparqlAnalyzer(SparqlDataset ds) {
		logger = Logger.getLogger(SparqlAnalyzer.class);
		this.ds = ds;
	}	
	
	@Override
	public void performAnalysis(PrintStream ps) {
		if (ps == null)
			ps = System.out;
		
		for (StatisticalCriterion sc : criteria) {
			// execute specific SPARQL query
			sc.processSparqlDataset(ds);
			// output results
			sc.flushLog(ps);
		}
		ps.flush();
	}

}
