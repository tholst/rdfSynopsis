package rdfsynopsis.analyzer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rdfsynopsis.dataset.SparqlDataset;
import rdfsynopsis.statistics.StatisticalCriterion;

public abstract class AbstractAnalyzer implements Analyzer {

	protected List<StatisticalCriterion> criteria;
	protected Logger logger;
	protected SparqlDataset	ds;

	/**
	 * perform analysis and output results
	 */
	@Override
	public abstract void performAnalysis(PrintStream ps);

	public AbstractAnalyzer() {
		criteria = new ArrayList<StatisticalCriterion>();
	}

	/**
	 *  define criteria
	 */
	@Override
	public Analyzer addCriterion(StatisticalCriterion sc) {
		criteria.add(sc);
		return this;
	}

	@Override
	public List<StatisticalCriterion> getCriteria() {
		return criteria;
	}

	public SparqlDataset getDs() {
		return ds;
	}

	/**
	 * define dataset
	 * @param ds
	 * @return
	 */
	public Analyzer setDs(SparqlDataset ds) {
		this.ds = ds;
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((criteria == null) ? 0 : criteria.hashCode());
		return result;
	}

	/**
	 * compare analysis results
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractAnalyzer))
			return false;
		AbstractAnalyzer other = (AbstractAnalyzer) obj;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria)) // compare criteria results
			return false;
		return true;
	}

}