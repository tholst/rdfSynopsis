package rdfsynopsis.test;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.InMemoryDataset;
import rdfsynopsis.statistics.DistinctSubjectOnlyBlanks;
import rdfsynopsis.statistics.DistinctSubjectsBlank;
import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class BlankNodesTest {

	InMemoryDataset ds;
	Logger log = Logger.getLogger(BlankNodesTest.class);
	Namespace exampleNs = new Namespace("ex", "http://example.com/");
	
	Resource maxRes;
	Resource petraRes;
	Resource stefanRes;
	Resource thomasRes;
	Resource biancaRes;
	Resource marcoRes;
	Resource nataliaRes;
	

	@Before
	public void setUpBefore() throws Exception {
		ds = new InMemoryDataset();
		Model m = ds.getModel();
		
		String maxMusterName = "Maximilian Mustermann";
		String petraMusterName = "Petra Mustermann";
		String stefanMusterName = "Thomas Mustermann";
		String thomasMusterName = "Stefan Mustermann";
		String biancaMusterName = "Bianca Mustermann";
		String marcoMusterName = "Marco Mustermann";
		String nataliaMusterName = "Natalia Mustermann";
		
		String maxUri = "http://example.com/MaxMustermann";
		String petraUri = "http://example.com/PetraMustermann";
		String stefanUri = "http://example.com/StefanMustermann";
		String thomasUri = "http://example.com/ThomasMustermann";
		String biancaUri = "http://example.com/BiancaMustermann";
		String marcoUri = "http://example.com/MarcoMustermann";
		String nataliaUri = "http://example.com/NataliaMustermann";
		
		maxRes = m.createResource(maxUri).addLiteral(VCARD.FN, maxMusterName);
		petraRes = m.createResource(petraUri).addLiteral(VCARD.FN, petraMusterName);
		stefanRes = m.createResource(stefanUri).addLiteral(VCARD.FN, stefanMusterName);
		thomasRes = m.createResource(thomasUri).addLiteral(VCARD.FN, thomasMusterName);
		biancaRes = m.createResource(biancaUri).addLiteral(VCARD.FN, biancaMusterName);
		marcoRes = m.createResource(marcoUri).addLiteral(VCARD.FN, marcoMusterName);
		nataliaRes = m.createResource(nataliaUri).addLiteral(VCARD.FN, nataliaMusterName);
		
		petraRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, petraRes);		
	}

	private void logGraph() {
		// output graph for debugging
		log.debug(ds.toString());
	}

	@Test
	public void noBlanks() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		logGraph();
		dsob.processSparqlDataset(ds);
		dsb.processSparqlDataset(ds);
		
		// no blanks at all
		assertEquals(0, dsob.getNumDistinctSubjectOnlyBlanks());
		assertEquals(0, dsb.getNumDistinctSubjectsBlank());
	}
	
	@Test
	public void objectBlanks() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		Model m = ds.getModel();
		
		// setting up two blanks representing first and last name
		Resource b1 = m.createResource(new AnonId("blank1"));
		b1.addLiteral(VCARD.Given, "Max");
		b1.addLiteral(VCARD.Family, "Mustermann");
		Resource b2 = m.createResource(new AnonId("blank2"));
		b2.addLiteral(VCARD.Given, "Petra");
		b2.addLiteral(VCARD.Family, "Mustermann");		
		maxRes.addProperty(VCARD.NAME, b1);
		petraRes.addProperty(VCARD.NAME, b2);

		logGraph();
		dsob.processSparqlDataset(ds);
		dsb.processSparqlDataset(ds);
		
		// two blanks, no subject only blanks
		assertEquals(0, dsob.getNumDistinctSubjectOnlyBlanks());
		assertEquals(2, dsb.getNumDistinctSubjectsBlank());
	}
	
	@Test
	public void subjectOnlyBlanks() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		Model m = ds.getModel();
		
		// setting up two blanks representing first and last name
		Resource b1 = m.createResource(new AnonId("blank1"));
		b1.addLiteral(VCARD.Given, "Max");
		b1.addLiteral(VCARD.Family, "Mustermann");
		Resource b2 = m.createResource(new AnonId("blank2"));
		b2.addLiteral(VCARD.Given, "Petra");
		b2.addLiteral(VCARD.Family, "Mustermann");		
		maxRes.addProperty(VCARD.NAME, b1);
		petraRes.addProperty(VCARD.NAME, b2);

		// creating a subject only blanks
		Resource group = m.createResource(exampleNs.getFullTerm("Group"));
		Property contains = m.createProperty(exampleNs.getFullTerm("contains"));
		Resource b3 = m.createResource(new AnonId("blank3"));
		b3.addProperty(RDF.type, group);
		b3.addProperty(contains, maxRes);
		b3.addProperty(contains, petraRes);
		b3.addProperty(contains, stefanRes);
		b3.addProperty(contains, biancaRes);
		b3.addProperty(contains, marcoRes);
		Resource b4 = m.createResource(new AnonId("blank4"));
		b4.addProperty(RDF.type, group);
		b4.addProperty(contains, thomasRes);
		b4.addProperty(contains, nataliaRes);
		
		logGraph();
		dsob.processSparqlDataset(ds);
		dsb.processSparqlDataset(ds);
		
		
		// 4 blanks, 2 subject only blank nodes
		assertEquals(2, dsob.getNumDistinctSubjectOnlyBlanks());
		assertEquals(4, dsb.getNumDistinctSubjectsBlank());
	}
	
	@Test
	public void noBlanksTripleStream() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		logGraph();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
			.addCriterion(dsb).addCriterion(dsob);
		tsa.performAnalysis(null);
		
		// no blanks at all
		assertEquals(0, dsob.getNumDistinctSubjectOnlyBlanks());
		assertEquals(0, dsb.getNumDistinctSubjectsBlank());
	}
	
	@Test
	public void objectBlanksTripleStream() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		Model m = ds.getModel();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
			.addCriterion(dsb).addCriterion(dsob);
		
		// setting up two blanks representing first and last name
		Resource b1 = m.createResource(new AnonId("blank1"));
		b1.addLiteral(VCARD.Given, "Max");
		b1.addLiteral(VCARD.Family, "Mustermann");
		Resource b2 = m.createResource(new AnonId("blank2"));
		b2.addLiteral(VCARD.Given, "Petra");
		b2.addLiteral(VCARD.Family, "Mustermann");		
		maxRes.addProperty(VCARD.NAME, b1);
		petraRes.addProperty(VCARD.NAME, b2);

		logGraph();
		tsa.performAnalysis(null);;
		
		// two blanks, no subject only blanks
		assertEquals(0, dsob.getNumDistinctSubjectOnlyBlanks());
		assertEquals(2, dsb.getNumDistinctSubjectsBlank());
	}
	
	@Test
	public void subjectOnlyBlanksTripleStream() {
		DistinctSubjectOnlyBlanks dsob = new DistinctSubjectOnlyBlanks();
		DistinctSubjectsBlank dsb = new DistinctSubjectsBlank();
		Model m = ds.getModel();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
			.addCriterion(dsb).addCriterion(dsob);
		
		// setting up two blanks representing first and last name
		Resource b1 = m.createResource(new AnonId("blank1"));
		b1.addLiteral(VCARD.Given, "Max");
		b1.addLiteral(VCARD.Family, "Mustermann");
		Resource b2 = m.createResource(new AnonId("blank2"));
		b2.addLiteral(VCARD.Given, "Petra");
		b2.addLiteral(VCARD.Family, "Mustermann");		
		maxRes.addProperty(VCARD.NAME, b1);
		petraRes.addProperty(VCARD.NAME, b2);

		// creating a subject only blanks
		Resource group = m.createResource(exampleNs.getFullTerm("Group"));
		Property contains = m.createProperty(exampleNs.getFullTerm("contains"));
		Resource b3 = m.createResource(new AnonId("blank3"));
		b3.addProperty(RDF.type, group);
		b3.addProperty(contains, maxRes);
		b3.addProperty(contains, petraRes);
		b3.addProperty(contains, stefanRes);
		b3.addProperty(contains, biancaRes);
		b3.addProperty(contains, marcoRes);
		Resource b4 = m.createResource(new AnonId("blank4"));
		b4.addProperty(RDF.type, group);
		b4.addProperty(contains, thomasRes);
		b4.addProperty(contains, nataliaRes);
		
		logGraph();
		tsa.performAnalysis(null);
		
		
		// 4 blanks, 2 subject only blank nodes
		assertEquals(2, dsob.getNumDistinctSubjectOnlyBlanks());
		assertEquals(4, dsb.getNumDistinctSubjectsBlank());
	}

}
