package rdfsynopsis.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.statistics.StatisticalCriterion;

public abstract class AbstractAnalysisLogger extends AbstractEvaluator
		implements Analyzer {

	protected List<StatisticalCriterion>	criteria;
	private PrintWriter						printer;

	public AbstractAnalysisLogger(String title, boolean timeStamp) {
		super(title, timeStamp);
		criteria = new ArrayList<StatisticalCriterion>();
	}

	@Override
	public void evaluate(File outDir, String titleAddition) {
		// initialize criteria
		for (StatisticalCriterion sc : criteria) {
			sc.init();
		}
		
		try {
			File outFile = makeTitleFile(title + titleAddition, outDir,".csv",timeStamp);
			printer = new PrintWriter(outFile);
			performAnalysis(null);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} finally {
			if (printer != null)
				printer.close();
		}
	}
	
	protected void writeLogLine(String line) {
		logger.trace("New log line: "+line);
		printer.println(line);
	}

	@Override
	public abstract void performAnalysis(PrintStream ps);

	@Override
	public AbstractAnalysisLogger addCriterion(StatisticalCriterion sc) {
		criteria.add(sc);
		return this;
	}

	@Override
	public List<StatisticalCriterion> getCriteria() {
		return criteria;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((criteria == null) ? 0 : criteria.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAnalysisLogger other = (AbstractAnalysisLogger) obj;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
			return false;
		return true;
	}

}
