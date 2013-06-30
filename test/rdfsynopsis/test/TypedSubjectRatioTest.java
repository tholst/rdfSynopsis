package rdfsynopsis.test;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.InMemoryDataset;
import rdfsynopsis.statistics.TypedSubjectRatio;
import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class TypedSubjectRatioTest {

	InMemoryDataset ds;
	Logger log = Logger.getLogger(TypedSubjectRatioTest.class);
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
		
		// output graph for debugging
		StringWriter out = new StringWriter();
		m.write(out, "TTL");
		log.debug(out.toString());		
	}

	@Test
	public void noOntology() {
		TypedSubjectRatio tsr = new TypedSubjectRatio();
		tsr.processSparqlDataset(ds);
		
		double delta = 0.0000001;
		assertEquals(0.0, tsr.getTypedSubjectRatio(), delta);
	}
	
	@Test
	public void simpleOntology() {
		TypedSubjectRatio tsr = new TypedSubjectRatio();
		Model m = ds.getModel();
		
		Resource manClass = m.createResource(exampleNs.getFullTerm("Man"));
		Resource womanClass = m.createResource(exampleNs.getFullTerm("Woman"));
		
		// class instantiation
		m.add(maxRes, RDF.type, manClass);
		m.add(thomasRes, RDF.type, manClass);
		m.add(petraRes, RDF.type, womanClass);
		m.add(nataliaRes, RDF.type, womanClass);
		
		m.add(maxRes, RDF.type, FOAF.Person);
		m.add(petraRes, RDF.type, FOAF.Person);
		m.add(nataliaRes, RDF.type, FOAF.Person);
		m.add(marcoRes, RDF.type, FOAF.Person);
		
		tsr.processSparqlDataset(ds);		

		// 2 untyped subjects: stefan, bianca
		// 5 typed subjects: max, thomas, petra, natalia, marco
		// TypedSubjectRatio = 5/7
		double delta = 0.0000001;
		assertEquals(5.0/7.0, tsr.getTypedSubjectRatio(), delta);
	}
	
	@Test
	public void noOntologyTripleStream() {
		TypedSubjectRatio tsr = new TypedSubjectRatio();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(tsr);
		tsa.performAnalysis(null);
		
		double delta = 0.0000001;
		assertEquals(0.0, tsr.getTypedSubjectRatio(), delta);
	}
	
	@Test
	public void simpleOntologyTripleStream() {
		TypedSubjectRatio tsr = new TypedSubjectRatio();
		Model m = ds.getModel();
		
		Resource manClass = m.createResource(exampleNs.getFullTerm("Man"));
		Resource womanClass = m.createResource(exampleNs.getFullTerm("Woman"));
		
		// class instantiation
		m.add(maxRes, RDF.type, manClass);
		m.add(thomasRes, RDF.type, manClass);
		m.add(petraRes, RDF.type, womanClass);
		m.add(nataliaRes, RDF.type, womanClass);
		
		m.add(maxRes, RDF.type, FOAF.Person);
		m.add(petraRes, RDF.type, FOAF.Person);
		m.add(nataliaRes, RDF.type, FOAF.Person);
		m.add(marcoRes, RDF.type, FOAF.Person);
		
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(tsr);
		tsa.performAnalysis(null);		

		// 2 untyped subjects: stefan, bianca
		// 5 typed subjects: max, thomas, petra, natalia, marco
		// TypedSubjectRatio = 5/7
		double delta = 0.0000001;
		assertEquals(5.0/7.0, tsr.getTypedSubjectRatio(), delta);
	}
}

