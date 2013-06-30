package rdfsynopsis.eval;

import java.io.PrintStream;
import java.util.Map;

import org.apache.log4j.Logger;

import rdfsynopsis.dataset.SparqlDataset;
import rdfsynopsis.statistics.StatisticalCriterion;

public class SparqlAnalysisLogger extends AbstractAnalysisLogger {

	private SparqlDataset	ds;

	public SparqlDataset getDs() {
		return ds;
	}

	public SparqlAnalysisLogger setDs(SparqlDataset ds) {
		this.ds = ds;
		return this;
	}

	public SparqlAnalysisLogger(String title, boolean timeStamp) {
		super(title, timeStamp);
		logger = Logger.getLogger(SparqlAnalysisLogger.class);
	}

	@Override
	public void performAnalysis(PrintStream ps) {
		if (ps == null)
			ps = System.out;
		
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
