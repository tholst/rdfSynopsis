package rdfsynopsis.analyzer;

import java.io.PrintStream;
import java.util.List;

import rdfsynopsis.statistics.StatisticalCriterion;

public interface Analyzer {

	// perform analysis and output results
	public abstract void performAnalysis(PrintStream ps);

	// define criteria
	public abstract Analyzer addCriterion(StatisticalCriterion sc);

	public abstract List<StatisticalCriterion> getCriteria();

	// compare analysis results
	public abstract boolean equals(Object o);

}