package rdfsynopsis.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.InMemoryDataset;
import rdfsynopsis.statistics.ClassHierarchy;
import rdfsynopsis.statistics.ClassUsageCount;
import rdfsynopsis.statistics.DistinctSubjectOnlyBlanks;
import rdfsynopsis.statistics.DistinctSubjectsBlank;
import rdfsynopsis.statistics.NumTriples;
import rdfsynopsis.statistics.OntologyRatio;
import rdfsynopsis.statistics.PredicateVocabularies;
import rdfsynopsis.statistics.PropertyHierarchy;
import rdfsynopsis.statistics.PropertyUsage;
import rdfsynopsis.statistics.PropertyUsagePerSubjectClass;
import rdfsynopsis.statistics.SameAs;
import rdfsynopsis.statistics.SubjectObjectRatio;
import rdfsynopsis.statistics.TypedSubjectRatio;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.VCARD;

public class SimpleTest {
	
	static InMemoryDataset ds = new InMemoryDataset();
	static Logger log = Logger.getLogger(SimpleTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Model m = ds.getModel();
		
		String maxMusterName = "Maximilian Mustermann";
		String petraMusterName = "Petra Mustermann";
		String maxUri = "http://example.com/MaxMustermann";
		String petraUri = "http://example.com/PetraMustermann";
		
		// add two literals, first with datatype String (implicit in addLiteral())
		Resource maxRes = m.createResource(maxUri).addLiteral(VCARD.FN, maxMusterName);
		Resource petraRes = m.createResource(petraUri).addProperty(VCARD.FN, petraMusterName);
		
		petraRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, petraRes);
		
		// output graph for debugging
		StringWriter out = new StringWriter();
		m.write(out, "TTL");
		log.debug(out.toString());		
	}
	
	@Test
	public void triples() {
		NumTriples nt = new NumTriples();
		nt.processSparqlDataset(ds);
		assertEquals(4, nt.getNumTriples());		
	}
	
	@Test
	public void sameAs() {
		SameAs sa = new SameAs();
		sa.processSparqlDataset(ds);
		assertEquals(sa.getNumSameAsTriples(), 0);		
	}
	
	@Test
	public void classUsageCount() {
		ClassUsageCount cuc = new ClassUsageCount();
		cuc.processSparqlDataset(ds);
		
		// no classes counted
		assertEquals(0,cuc.getNumUsedClasses());
		
		// empty class set
		Set<String> classUris = cuc.getClassUris();
		assertEquals(0, classUris.size());
		
		// usage counter zero for any class
		assertEquals(0, cuc.getNumInstances(FOAF.Person));
		assertEquals(0, cuc.getNumInstances(FOAF.Agent));
		assertEquals(0, cuc.getNumInstances(FOAF.Document));
		assertEquals(0, cuc.getNumInstances(RDF.Property));
		assertEquals(0, cuc.getNumInstances(RDF.Statement));
		assertEquals(0, cuc.getNumInstances(RDFS.Class));
		assertEquals(0, cuc.getNumInstances(RDFS.Resource));
		assertEquals(0, cuc.getNumInstances(OWL.Class));
	}
	
	@Test
	public void classHierarchy() {
		ClassHierarchy ch = new ClassHierarchy();
		ch.processSparqlDataset(ds);
		assertEquals(ch.getClassHierarchyDepth(), 0);
		assertEquals(ch.getNumClassHierarchyTriples(), 0);
	}
	
	@Test
	public void ontologyRatio() {
		OntologyRatio or = new OntologyRatio();
		or.processSparqlDataset(ds);
		double delta = 0.0000001;
		assertEquals(0.0, or.getOntologyRatio(), delta);
	}
	
	@Test
	public void typedSubjectRatio() {
		TypedSubjectRatio tsr = new TypedSubjectRatio();
		tsr.processSparqlDataset(ds);
		double delta = 0.0000001;
		assertEquals(tsr.getTypedSubjectRatio(), 0.0, delta);
	}
	
	@Test
	public void propertyUsage() {
		PropertyUsage pu = new PropertyUsage();
		pu.processSparqlDataset(ds);
		
		// two distinct properties used
		assertEquals(2, pu.getNumUsedProperties());
		
		// URI set has size 2
		assertEquals(2, pu.getPropertyUris().size());
		
		// property counter 2 for used properties
		// and 0 for other properties
		assertEquals(2, pu.getNumInstances(FOAF.knows));
		assertEquals(2, pu.getNumInstances(VCARD.FN));
		assertEquals(0, pu.getNumInstances(FOAF.depicts));
		assertEquals(0, pu.getNumInstances(RDF.type));
		assertEquals(0, pu.getNumInstances(VCARD.BDAY));
		assertEquals(0, pu.getNumInstances(DC.date));
		assertEquals(0, pu.getNumInstances(RDFS.subClassOf));
		assertEquals(0, pu.getNumInstances(RDFS.subPropertyOf));
	}
	
	@Test
	public void predicateVocabularies() {
		PredicateVocabularies pv = new PredicateVocabularies();
		pv.processSparqlDataset(ds);
		
		// number of used predicate vocabularies is 2
		assertEquals(2, pv.getNumPredicateVocabularies());
		
		// set of vocabularies has size 2 and 
		// contains FOAF and VCARD URIs
		Set<String> predVocabs = pv.getPredicateVocabularies();
		assertEquals(predVocabs.size(), 2);
		assertTrue(predVocabs.contains(FOAF.getURI()));
		assertTrue(predVocabs.contains(VCARD.getURI()));
		
		// used vocabularies have usage 2
		assertEquals(2, pv.getVocabUsage(FOAF.getURI()));
		assertEquals(2, pv.getVocabUsage(VCARD.getURI()));
		
		// unused vocabularies have usage 0
		assertEquals(0, pv.getVocabUsage(DC.getURI()));
		assertEquals(0, pv.getVocabUsage(RDF.getURI()));
		assertEquals(0, pv.getVocabUsage(RDFS.getURI()));
		assertEquals(0, pv.getVocabUsage(OWL.getURI()));
	}
	
	@Test
	public void propertyUsagePerSubjectClass() {
		PropertyUsagePerSubjectClass pupsc = new PropertyUsagePerSubjectClass();
		pupsc.processSparqlDataset(ds);
				
		// set of used classes is empty
		Set<String> classUris = pupsc.getClassUris();
		assertEquals(0, classUris.size());
		
		// property usage for untyped subjects is 2 for VCARD.FN 
		// and FOAF.knows each; and zero for other properties
		assertEquals(2, pupsc.getUntypedPropertyUsage(VCARD.FN));
		assertEquals(2, pupsc.getUntypedPropertyUsage(FOAF.knows));
		assertEquals(0, pupsc.getUntypedPropertyUsage(VCARD.BDAY));
		assertEquals(0, pupsc.getUntypedPropertyUsage(FOAF.depiction));
		assertEquals(0, pupsc.getUntypedPropertyUsage(RDF.type));
		
		// property usage for any class and any property is zero
		assertEquals(0, pupsc.getPropertyUsagePerClass(FOAF.Person, FOAF.knows));
		assertEquals(0, pupsc.getPropertyUsagePerClass(FOAF.Person, VCARD.FN));
		assertEquals(0, pupsc.getPropertyUsagePerClass(FOAF.Person, FOAF.birthday));
		assertEquals(0, pupsc.getPropertyUsagePerClass(VCARD.TELTYPES, VCARD.EMAIL));
		assertEquals(0, pupsc.getPropertyUsagePerClass(RDFS.Class, RDFS.subClassOf));
		
	}
	
	@Test
	public void propertyHierarchy() {
		PropertyHierarchy ph = new PropertyHierarchy();
		ph.processSparqlDataset(ds);
		assertEquals(ph.getPropertyHierarchyDepth(), 0);
		assertEquals(ph.getNumPropertyHierarchyTriples(), 0);
	}
	
//	@Test
//	public void subjectObjectRatio() {
//		SubjectObjectRatio sor = new SubjectObjectRatio();
//		sor.processSparqlDataset(ds);
//		
//		// Mächtigkeit der Schnittmenge von Subjekt- und Objekt-URIs durch
//		// die Mächtigkeit der Vereinigung derselben
//		// hier jeweils 2, ratio deswegen 1
//		double delta = 0.0000001;
//		assertEquals(2, sor.getNumCommonSubjectObjectURIs()); //intersection
//		assertEquals(2, sor.getNumDistinctSubjectObjectURIs()); //union
//		assertEquals(sor.getSubjectObjectRatio(), 1.0, delta);
//	}
	
	@Test
	public void distinctSubjectsBlank() {
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		dsb.processSparqlDataset(ds);
		
		// no bnodes in subject position
		assertEquals(0,dsb.getNumDistinctSubjectsBlank());
	}
	
//	@Test
//	public void linksPerNsPropNs() {
//		LinksPerNsPropNs lpnpn = new LinksPerNsPropNs();
//		lpnpn.processSparqlDataset(ds);
//		
//		// no links at all
//		assertEquals(0, lpnpn.getNumLinks());
//		
//		// generic version always yields 0
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",FOAF.knows,null));
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",null,null));
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",null,"http://dbtune.org/bbc/peel/artist/"));
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",FOAF.knows,"http://dbtune.org/bbc/peel/artist/"));
//		assertEquals(0, lpnpn.getNumLinksGeneric(null,FOAF.knows,"http://dbtune.org/bbc/peel/artist/"));
//		assertEquals(0, lpnpn.getNumLinksGeneric(null,RDF.type,"http://dbtune.org/bbc/peel/artist/"));
//	}
	
	@Test
	public void distinctSubjectOnlyBlanks() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		dsob.processSparqlDataset(ds);
		assertEquals(0,dsob.getNumDistinctSubjectOnlyBlanks());
	}
	
	@Test
	public void triplesTripleStream() {
		NumTriples nt = new NumTriples();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(nt);
		tsa.performAnalysis(null);
		assertEquals(4, nt.getNumTriples());		
	}
	
	@Test
	public void sameAsTripleStream() {
		SameAs sa = new SameAs();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(sa);
		tsa.performAnalysis(null);
		assertEquals(0, sa.getNumSameAsTriples());		
	}
	
	@Test
	public void classUsageCountTripleStream() {
		ClassUsageCount cuc = new ClassUsageCount();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(cuc);
		tsa.performAnalysis(null);
		
		// no classes counted
		assertEquals(0,cuc.getNumUsedClasses());
		
		// empty class set
		Set<String> classUris = cuc.getClassUris();
		assertEquals(0, classUris.size());
		
		// usage counter zero for any class
		assertEquals(0, cuc.getNumInstances(FOAF.Person));
		assertEquals(0, cuc.getNumInstances(FOAF.Agent));
		assertEquals(0, cuc.getNumInstances(FOAF.Document));
		assertEquals(0, cuc.getNumInstances(RDF.Property));
		assertEquals(0, cuc.getNumInstances(RDF.Statement));
		assertEquals(0, cuc.getNumInstances(RDFS.Class));
		assertEquals(0, cuc.getNumInstances(RDFS.Resource));
		assertEquals(0, cuc.getNumInstances(OWL.Class));
	}
	
	@Test
	public void classHierarchyTripleStream() {
		ClassHierarchy ch = new ClassHierarchy();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(ch);
		tsa.performAnalysis(null);
		assertEquals(ch.getClassHierarchyDepth(), 0);
		assertEquals(ch.getNumClassHierarchyTriples(), 0);
	}
	
	@Test
	public void ontologyRatioTripleStream() {
		OntologyRatio or = new OntologyRatio();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(or);
		tsa.performAnalysis(null);
		double delta = 0.0000001;
		assertEquals(0.0, or.getOntologyRatio(), delta);
	}
	
	@Test
	public void typedSubjectRatioTripleStream() {
		TypedSubjectRatio tsr = new TypedSubjectRatio();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(tsr);
		tsa.performAnalysis(null);
		double delta = 0.0000001;
		assertEquals(tsr.getTypedSubjectRatio(), 0.0, delta);
	}
	
	@Test
	public void propertyUsageTripleStream() {
		PropertyUsage pu = new PropertyUsage();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(pu);
		tsa.performAnalysis(null);
		
		// two distinct properties used
		assertEquals(2, pu.getNumUsedProperties());
		
		// URI set has size 2
		assertEquals(2, pu.getPropertyUris().size());
		
		// property counter 2 for used properties
		// and 0 for other properties
		assertEquals(2, pu.getNumInstances(FOAF.knows));
		assertEquals(2, pu.getNumInstances(VCARD.FN));
		assertEquals(0, pu.getNumInstances(FOAF.depicts));
		assertEquals(0, pu.getNumInstances(RDF.type));
		assertEquals(0, pu.getNumInstances(VCARD.BDAY));
		assertEquals(0, pu.getNumInstances(DC.date));
		assertEquals(0, pu.getNumInstances(RDFS.subClassOf));
		assertEquals(0, pu.getNumInstances(RDFS.subPropertyOf));
	}
	
	@Test
	public void predicateVocabulariesTripleStream() {
		PredicateVocabularies pv = new PredicateVocabularies();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(pv);
		tsa.performAnalysis(null);
		
		// number of used predicate vocabularies is 2
		assertEquals(pv.getNumPredicateVocabularies(), 2);
		
		// set of vocabularies has size 2 and 
		// contains FOAF and VCARD URIs
		Set<String> predVocabs = pv.getPredicateVocabularies();
		assertEquals(predVocabs.size(), 2);
		assertTrue(predVocabs.contains(FOAF.getURI()));
		assertTrue(predVocabs.contains(VCARD.getURI()));
		
		// used vocabularies have usage 2
		assertEquals(2, pv.getVocabUsage(FOAF.getURI()));
		assertEquals(2, pv.getVocabUsage(VCARD.getURI()));
		
		// unused vocabularies have usage 0
		assertEquals(0, pv.getVocabUsage(DC.getURI()));
		assertEquals(0, pv.getVocabUsage(RDF.getURI()));
		assertEquals(0, pv.getVocabUsage(RDFS.getURI()));
		assertEquals(0, pv.getVocabUsage(OWL.getURI()));
	}
	
	@Test
	public void propertyUsagePerSubjectClassTripleStream() {
		PropertyUsagePerSubjectClass pupsc = new PropertyUsagePerSubjectClass();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(pupsc);
		tsa.performAnalysis(null);
				
		// set of used classes is empty
		Set<String> classUris = pupsc.getClassUris();
		assertEquals(0, classUris.size());
		
		// property usage for untyped subjects is 2 for VCARD.FN 
		// and FOAF.knows each; and zero for other properties
		assertEquals(2, pupsc.getUntypedPropertyUsage(VCARD.FN));
		assertEquals(2, pupsc.getUntypedPropertyUsage(FOAF.knows));
		assertEquals(0, pupsc.getUntypedPropertyUsage(VCARD.BDAY));
		assertEquals(0, pupsc.getUntypedPropertyUsage(FOAF.depiction));
		assertEquals(0, pupsc.getUntypedPropertyUsage(RDF.type));
		
		// property usage for any class and any property is zero
		assertEquals(0, pupsc.getPropertyUsagePerClass(FOAF.Person, FOAF.knows));
		assertEquals(0, pupsc.getPropertyUsagePerClass(FOAF.Person, VCARD.FN));
		assertEquals(0, pupsc.getPropertyUsagePerClass(FOAF.Person, FOAF.birthday));
		assertEquals(0, pupsc.getPropertyUsagePerClass(VCARD.TELTYPES, VCARD.EMAIL));
		assertEquals(0, pupsc.getPropertyUsagePerClass(RDFS.Class, RDFS.subClassOf));
		
	}
	
	@Test
	public void propertyHierarchyTripleStream() {
		PropertyHierarchy ph = new PropertyHierarchy();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(ph);
		tsa.performAnalysis(null);
		assertEquals(ph.getPropertyHierarchyDepth(), 0);
		assertEquals(ph.getNumPropertyHierarchyTriples(), 0);
	}
	
	@Test
	public void subjectObjectRatioTripleStream() {
		SubjectObjectRatio sor = new SubjectObjectRatio();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(sor);
		tsa.performAnalysis(null);
		
		// Mächtigkeit der Schnittmenge von Subjekt- und Objekt-URIs durch
		// die Mächtigkeit der Vereinigung derselben
		// hier jeweils 2, ratio deswegen 1
		double delta = 0.0000001;
		assertEquals(2, sor.getNumCommonSubjectObjectURIs()); //intersection
		assertEquals(2, sor.getNumDistinctSubjectObjectURIs()); //union
		assertEquals(sor.getSubjectObjectRatio(), 1.0, delta);
	}
	
	@Test
	public void distinctSubjectsBlankTripleStream() {
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(dsb);
		tsa.performAnalysis(null);
		
		// no bnodes in subject position
		assertEquals(0,dsb.getNumDistinctSubjectsBlank());
	}
	
//	@Test
//	public void linksPerNsPropNsTripleStream() {
//		LinksPerNsPropNs lpnpn = new LinksPerNsPropNs();
//		Analyzer tsa = new TripleStreamAnalyzer(ds)
//	.addCriterion(lpnpn);
//	tsa.performAnalysis();
//		
//		// no links at all
//		assertEquals(0, lpnpn.getNumLinks());
//		
//		// generic version always yields 0
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",FOAF.knows,null));
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",null,null));
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",null,"http://dbtune.org/bbc/peel/artist/"));
//		assertEquals(0, lpnpn.getNumLinksGeneric("http://example.com/",FOAF.knows,"http://dbtune.org/bbc/peel/artist/"));
//		assertEquals(0, lpnpn.getNumLinksGeneric(null,FOAF.knows,"http://dbtune.org/bbc/peel/artist/"));
//		assertEquals(0, lpnpn.getNumLinksGeneric(null,RDF.type,"http://dbtune.org/bbc/peel/artist/"));
//	}
	
	@Test
	public void distinctSubjectOnlyBlanksTripleStream() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(dsob);
		tsa.performAnalysis(null);
		assertEquals(0,dsob.getNumDistinctSubjectOnlyBlanks());
	}

}
