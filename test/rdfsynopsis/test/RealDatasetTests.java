package rdfsynopsis.test;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
import rdfsynopsis.statistics.OntologyRatio;
import rdfsynopsis.statistics.OntologyRatioNew;
import rdfsynopsis.statistics.PredicateVocabularies;
import rdfsynopsis.statistics.PropertyHierarchy;
import rdfsynopsis.statistics.PropertyUsage;
import rdfsynopsis.statistics.PropertyUsagePerSubjectClass;
import rdfsynopsis.statistics.SameAs;
import rdfsynopsis.statistics.TriplesPerSubjectClass;
import rdfsynopsis.statistics.TypedSubjectRatio;

@RunWith(JUnit4.class)
public class RealDatasetTests {

	static SparqlDataset	peelDs;
	static NumTriples		numTriplesStatisticalCriterion;
	static InMemoryDataset	dblpMemDs;
	static InMemoryDataset	peelMemDs;

	@BeforeClass
	public static void setUp() throws Exception {
		peelDs = new SparqlEndpointDataset("http://localhost:3030/Peel/query");
		peelMemDs = new InMemoryDataset("file:../data/peel.rdf");
		dblpMemDs = new InMemoryDataset(
				"file:../data/dblp-publications-2012.rdf");
	}

	@Test
	public void numTriplesPeel() {
		NumTriples numTriplesStatisticalCriterion = new NumTriples();
		numTriplesStatisticalCriterion.processSparqlDataset(peelDs);
		assertEquals(numTriplesStatisticalCriterion.getNumTriples(), 271369);
	}

	@Test
	public void numTriplesPeelInMem() {
		NumTriples numTriplesStatisticalCriterion = new NumTriples();
		numTriplesStatisticalCriterion.processSparqlDataset(peelDs);
		int triplesPeelDs = numTriplesStatisticalCriterion.getNumTriples();
		numTriplesStatisticalCriterion = new NumTriples();
		numTriplesStatisticalCriterion.processSparqlDataset(peelMemDs);
		int triplesPeelMemDs = numTriplesStatisticalCriterion.getNumTriples();

		assertEquals(triplesPeelDs, triplesPeelMemDs);
	}

	@Test
	public void sparqlVsStreamAnalysis() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new NumTriples())
				.addCriterion(new SameAs())
				.addCriterion(new ClassUsageCount())
				.addCriterion(new ClassHierarchy())
				.addCriterion(new OntologyRatio())
				.addCriterion(new TypedSubjectRatio())
				.addCriterion(new PredicateVocabularies())
				.addCriterion(new PropertyUsagePerSubjectClass())
				.addCriterion(new PropertyUsage())
				.addCriterion(new DistinctSubjectsBlank())
				.addCriterion(new DistinctSubjectOnlyBlanks())
				.addCriterion(new PropertyHierarchy());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new NumTriples())
				.addCriterion(new SameAs())
				.addCriterion(new ClassUsageCount())
				.addCriterion(new ClassHierarchy())
				.addCriterion(new OntologyRatio())
				.addCriterion(new TypedSubjectRatio())
				.addCriterion(new PredicateVocabularies())
				.addCriterion(new PropertyUsagePerSubjectClass())
				.addCriterion(new PropertyUsage())
				.addCriterion(new DistinctSubjectsBlank())
				.addCriterion(new DistinctSubjectOnlyBlanks())
				.addCriterion(new PropertyHierarchy());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_NumTriples() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new NumTriples());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new NumTriples());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_SameAs() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new SameAs());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new SameAs());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_ClassUsageCount() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new ClassUsageCount());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new ClassUsageCount());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_ClassHierarchy() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new ClassHierarchy());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new ClassHierarchy());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_ClassInstancesPerProperty() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new ClassInstancesPerProperty());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new ClassInstancesPerProperty());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_CommonProperties() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new CommonProperties());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new CommonProperties());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_DistinctSubjectOnlyBlanks() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new DistinctSubjectOnlyBlanks());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new DistinctSubjectOnlyBlanks());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_DistinctSubjectsBlank() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new DistinctSubjectsBlank());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new DistinctSubjectsBlank());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_ImplicitClassHierarchy() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new ImplicitClassHierarchy());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new ImplicitClassHierarchy());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_ImplicitPropertyHierarchy() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new ImplicitPropertyHierarchy());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new ImplicitPropertyHierarchy());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_NamespaceLinks() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new NamespaceLinks());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new NamespaceLinks());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_OntologyRatioNew() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new OntologyRatioNew());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new OntologyRatioNew());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_PredicateVocabularies() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new PredicateVocabularies());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new PredicateVocabularies());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_PropertyHierarchy() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new PropertyHierarchy());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new PropertyHierarchy());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_PropertyUsage() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new PropertyUsage());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new PropertyUsage());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_PropertyUsagePerSubjectClass() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new PropertyUsagePerSubjectClass());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new PropertyUsagePerSubjectClass());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_TriplesPerSubjectClass() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new TriplesPerSubjectClass());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new TriplesPerSubjectClass());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_TypedSubjectRatio() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new TypedSubjectRatio());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new TypedSubjectRatio());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

	@Test
	public void compareSQAandTSA_OntologyRatioOld() {
		Analyzer sparqlAnalyzer = new SparqlAnalyzer(peelMemDs)
				.addCriterion(new OntologyRatio());
		sparqlAnalyzer.performAnalysis(null);

		Analyzer streamAnalyzer = new TripleStreamAnalyzer(peelMemDs)
				.addCriterion(new OntologyRatio());
		streamAnalyzer.performAnalysis(null);

		assertEquals(sparqlAnalyzer, streamAnalyzer);
	}

}
