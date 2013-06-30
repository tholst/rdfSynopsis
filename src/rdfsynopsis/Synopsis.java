package rdfsynopsis;

import org.apache.log4j.Logger;

import rdfsynopsis.analyzer.AbstractAnalyzer;
import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.analyzer.SparqlAnalyzer;
import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.InMemoryDataset;
import rdfsynopsis.dataset.SparqlDataset;
import rdfsynopsis.dataset.SparqlEndpointDataset;
import rdfsynopsis.statistics.ClassHierarchy;
import rdfsynopsis.statistics.ClassInstancesPerProperty;
import rdfsynopsis.statistics.ClassUsageCount;
import rdfsynopsis.statistics.CommonProperties;
import rdfsynopsis.statistics.DistinctSubjectOnlyBlanks;
import rdfsynopsis.statistics.DistinctSubjectsBlank;
import rdfsynopsis.statistics.ImplicitClassHierarchy;
import rdfsynopsis.statistics.ImplicitPropertyHierarchy;
import rdfsynopsis.statistics.NamespaceLinks;
import rdfsynopsis.statistics.NumTriples;
import rdfsynopsis.statistics.OntologyRatioNew;
import rdfsynopsis.statistics.PredicateVocabularies;
import rdfsynopsis.statistics.PropertyHierarchy;
import rdfsynopsis.statistics.PropertyUsage;
import rdfsynopsis.statistics.PropertyUsagePerSubjectClass;
import rdfsynopsis.statistics.SameAs;
import rdfsynopsis.statistics.TriplesPerSubjectClass;
import rdfsynopsis.statistics.TypedSubjectRatio;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Main Class of RDFSynopsis, a software tool for the structural analysis of RDF datasets.
 * 
 * @author thomas
 *
 */
public class Synopsis {

	static Logger			rootLog		= Logger.getRootLogger();

	static final String[]	criteria	= {
										"class usage count",
										"triples per subject class",
										"explicit class hierarchy",
										"implicit class hierarchy",
										"ontology-ratio",
										"typed-subject-ratio",
										"property usage",
										"predicate vocabularies",
										"property usage	per subject class",
										"class instances per property",
										"explicit property hierarchy",
										"implicit property hierarchy",
										"distinct blank subjects",
										"namespace links",
										"distinct subject-only blanks",
										"triples",
										"sameAs",
										"common properties"
										};

	/**
	 * entry point for command-line use
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// init logging
		rootLog.trace("logging system configured");

		// Parsing Command Line Arguments
		CommandLineArgs cla = new CommandLineArgs();
		JCommander jCmd = new JCommander(cla);
		jCmd.setAllowAbbreviatedOptions(true);
		jCmd.setCaseSensitiveOptions(false);
		jCmd.setColumnSize(80);
		jCmd.setProgramName("rdfSynopsis");
		try {
			jCmd.parse(args);
			validateCommandLineParameters(cla);
		} catch (ParameterException ex) {
			System.err.println(ex.getMessage());
			jCmd.usage();
			System.exit(1);
		}

		// react on arguments
		// Usage
		if (cla.help) {
			jCmd.usage();
			System.exit(0);
		}
		// Criteria
		if (cla.listCriteria) {
			printCriteria();
			System.exit(0);
		}

		// Dataset Access
		SparqlDataset ds;
		if (cla.endpoint != null)
			ds = new SparqlEndpointDataset(cla.endpoint);
		else ds = new InMemoryDataset(cla.datasetFile);

		// Order By Clause
		String obc;
		if (cla.orderBy.equalsIgnoreCase("subject"))
			obc = TripleStreamAnalyzer.BySubject;
		else if (cla.orderBy.equalsIgnoreCase("predicate"))
			obc = TripleStreamAnalyzer.ByPredicate;
		else obc = TripleStreamAnalyzer.ByObject;

		// Analysis Method
		Analyzer a;
		if (cla.sqa) {
			a = new SparqlAnalyzer(ds);
		} else {
			AbstractAnalyzer tsa = new TripleStreamAnalyzer(ds)
					.setOrderByClause(obc)
					.setRandomSampling(cla.random)
					.setTripleLimit(cla.tripleLimit);
			a = tsa;
		}

		// setup criteria
		if (cla.allCriteria || cla.criteria.contains("1"))			a.addCriterion(new ClassUsageCount());
		if (cla.allCriteria || cla.criteria.contains("2"))			a.addCriterion(new TriplesPerSubjectClass());
		if (cla.allCriteria || cla.criteria.contains("3"))			a.addCriterion(new ClassHierarchy());
		if (cla.allCriteria || cla.criteria.contains("4"))			a.addCriterion(new ImplicitClassHierarchy());
		if (cla.allCriteria || cla.criteria.contains("5"))			a.addCriterion(new OntologyRatioNew());
		if (cla.allCriteria || cla.criteria.contains("6"))			a.addCriterion(new TypedSubjectRatio());
		if (cla.allCriteria || cla.criteria.contains("7"))			a.addCriterion(new PropertyUsage());
		if (cla.allCriteria || cla.criteria.contains("8"))			a.addCriterion(new PredicateVocabularies());
		if (cla.allCriteria || cla.criteria.contains("9"))			a.addCriterion(new PropertyUsagePerSubjectClass());
		if (cla.allCriteria || cla.criteria.contains("10"))			a.addCriterion(new ClassInstancesPerProperty());
		if (cla.allCriteria || cla.criteria.contains("11"))			a.addCriterion(new PropertyHierarchy());
		if (cla.allCriteria || cla.criteria.contains("12"))			a.addCriterion(new ImplicitPropertyHierarchy());
		if (cla.allCriteria || cla.criteria.contains("13"))			a.addCriterion(new DistinctSubjectsBlank());
		if (cla.allCriteria || cla.criteria.contains("14"))			a.addCriterion(new NamespaceLinks());
		if (cla.allCriteria || cla.criteria.contains("15"))			a.addCriterion(new DistinctSubjectOnlyBlanks());
		if (cla.allCriteria || cla.criteria.contains("16"))			a.addCriterion(new NumTriples());
		if (cla.allCriteria || cla.criteria.contains("17"))			a.addCriterion(new SameAs());
		if (cla.allCriteria || cla.criteria.contains("18"))			a.addCriterion(new CommonProperties());

		// perform analysis
		a.performAnalysis(System.out);
	}


	/**
	 * --listCriteria
	 * 
	 * "class usage count",
	 * "triples per subject class",
	 * "explicit class hierarchy",
	 * "implicit class hierarchy",
	 * "ontology-ratio",
	 * "typed-subject-ratio",
	 * "property usage",
	 * "predicate vocabularies",
	 * "property usage	per subject class",
	 * "class instances per property",
	 * "explicit property hierarchy",
	 * "implicit property hierarchy",
	 * "distinct blank subjects",
	 * "namespace links",
	 * "distinct subject-only blanks",
	 * "triples",
	 * "sameAs",
	 * "common properties"
	 */
	private static void printCriteria() {
		StringBuilder sb = new StringBuilder();

		sb.append("[id]\tcriterion\n");
		sb.append("------------------------------------------\n");

		for (int i = 0; i < criteria.length; i++) {
			sb.append("[" + (i + 1) + "]\t" + criteria[i] + "\n");
		}

		System.out.println(sb.toString());
	}

	/**
	 * Double check that the command line parameters are valid.
	 */
	protected static void validateCommandLineParameters(CommandLineArgs cla) {

		if (!cla.help && !cla.listCriteria) {
			// parallel use of tsa and sqa
			if (cla.sqa && cla.tsa)
				throw new ParameterException("Choose either SQA or TSA.");
			// no use of tsa and sqa
			if (!cla.sqa && !cla.tsa)
				throw new ParameterException("Choose either SQA or TSA.");

			// parallel use of tsa and sqa
			if ((cla.endpoint != null) && (cla.datasetFile != null))
				throw new ParameterException(
						"Choose either endpoint or file as input.");
			// no use of tsa and sqa
			if ((cla.endpoint == null) && (cla.datasetFile == null))
				throw new ParameterException(
						"Choose either endpoint or file as input.");

			// parallel use of allCriteria and criteria
			if (cla.allCriteria && cla.criteria != null
					&& cla.criteria.size() > 0)
				throw new ParameterException(
						"Ambiguous criteria specification. Did you use \"--allCriteria\" and \"--criteria\" together?");
			// no use of allCriteria and criteria
			if (!cla.allCriteria && cla.criteria == null)
				throw new ParameterException(
						"Please specify analytic criteria.");

			// order by clause
			if (!(cla.orderBy.equalsIgnoreCase("subject")
					|| cla.orderBy.equalsIgnoreCase("predicate")
					|| cla.orderBy.equalsIgnoreCase("object"))) {
				throw new ParameterException(
						"Invalid order by clause. (valid: subject, predicate, object)");
			}
		}
	}
}
