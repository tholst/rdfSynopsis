package rdfsynopsis.test;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.InMemoryDataset;
import rdfsynopsis.statistics.ClassHierarchy;
import rdfsynopsis.statistics.OntologyRatio;
import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.VCARD;

public class OntologyRatioTest {

	InMemoryDataset ds;
	Logger log = Logger.getLogger(OntologyRatioTest.class);
	Namespace exampleNs = new Namespace("ex", "http://example.com/");
	
	Resource maxRes;
	Resource petraRes;

	@Before
	public void setUpBefore() throws Exception {
		ds = new InMemoryDataset();
		Model m = ds.getModel();
		
		String maxMusterName = "Maximilian Mustermann";
		String petraMusterName = "Petra Mustermann";
		String maxUri = "http://example.com/MaxMustermann";
		String petraUri = "http://example.com/PetraMustermann";
		
		// add two literals, first with datatype String (implicit in addLiteral())
		maxRes = m.createResource(maxUri).addLiteral(VCARD.FN, maxMusterName);
		petraRes = m.createResource(petraUri).addProperty(VCARD.FN, petraMusterName);
		
		petraRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, petraRes);
		
		// output graph for debugging
		StringWriter out = new StringWriter();
		m.write(out, "TTL");
		log.debug(out.toString());		
	}

	@Test
	public void noOntology() {
		OntologyRatio or = new OntologyRatio();
		or.processSparqlDataset(ds);
		
		double delta = 0.0000001;
		assertEquals(0.0, or.getOntologyRatio(), delta);
	}
	
	@Test
	public void simpleOntology() {
		OntologyRatio or = new OntologyRatio();
		Model m = ds.getModel();
		
		Resource manClass = m.createResource(exampleNs.getFullTerm("Man"));
		Resource womanClass = m.createResource(exampleNs.getFullTerm("Woman"));
		
		// class instantiation
		m.add(maxRes, RDF.type, manClass);
		m.add(petraRes, RDF.type, womanClass);
		m.add(maxRes, RDF.type, FOAF.Person);
		m.add(petraRes, RDF.type, FOAF.Person);		
		
		// class hierarchy
		m.add(manClass, RDFS.subClassOf, FOAF.Person);
		m.add(womanClass, RDFS.subClassOf, FOAF.Person);		

		// class definitions
		m.add(womanClass, RDF.type, RDFS.Class);
		m.add(womanClass, RDF.type, OWL.Class);
		m.add(manClass, RDF.type, RDFS.Class);
		m.add(manClass, RDF.type, OWL.Class);
		m.add(FOAF.Person, RDF.type, RDFS.Class);
		
		// property definitions
		m.add(RDF.type, RDF.type, RDF.Property);
		m.add(RDFS.subClassOf, RDF.type, RDF.Property);
		
		or.processSparqlDataset(ds);		

		// 2 non-term resources: ex:MaxMustermann, ex:PetraMustermann
		// 5 term resources: ex:Woman, ex:Man, foaf:Person, rdf:type, rdfs:subClassOf
		// OntologyRatio = 5/7
		double delta = 0.0000001;
		assertEquals(5.0/7.0, or.getOntologyRatio(), delta);
	}
	
	@Test
	public void noOntologyTripleStream() {
		OntologyRatio or = new OntologyRatio();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(or);
		tsa.performAnalysis(null);
		
		double delta = 0.0000001;
		assertEquals(0.0, or.getOntologyRatio(), delta);
	}
	
	@Test
	public void simpleOntologyTripleStream() {
		OntologyRatio or = new OntologyRatio();
		Model m = ds.getModel();
		
		Resource manClass = m.createResource(exampleNs.getFullTerm("Man"));
		Resource womanClass = m.createResource(exampleNs.getFullTerm("Woman"));
		
		// class instantiation
		m.add(maxRes, RDF.type, manClass);
		m.add(petraRes, RDF.type, womanClass);
		m.add(maxRes, RDF.type, FOAF.Person);
		m.add(petraRes, RDF.type, FOAF.Person);		
		
		// class hierarchy
		m.add(manClass, RDFS.subClassOf, FOAF.Person);
		m.add(womanClass, RDFS.subClassOf, FOAF.Person);		

		// class definitions
		m.add(womanClass, RDF.type, RDFS.Class);
		m.add(womanClass, RDF.type, OWL.Class);
		m.add(manClass, RDF.type, RDFS.Class);
		m.add(manClass, RDF.type, OWL.Class);
		m.add(FOAF.Person, RDF.type, RDFS.Class);
		
		// property definitions
		m.add(RDF.type, RDF.type, RDF.Property);
		m.add(RDFS.subClassOf, RDF.type, RDF.Property);
		
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(or);
		tsa.performAnalysis(null);		

		// 2 non-term resources: ex:MaxMustermann, ex:PetraMustermann
		// 5 term resources: ex:Woman, ex:Man, foaf:Person, rdf:type, rdfs:subClassOf
		// OntologyRatio = 5/7
		double delta = 0.0000001;
		assertEquals(5.0/7.0, or.getOntologyRatio(), delta);
	}
}
