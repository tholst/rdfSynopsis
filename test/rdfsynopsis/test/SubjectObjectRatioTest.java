package rdfsynopsis.test;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import rdfsynopsis.analyzer.Analyzer;
import rdfsynopsis.analyzer.TripleStreamAnalyzer;
import rdfsynopsis.dataset.InMemoryDataset;
import rdfsynopsis.statistics.SubjectObjectRatio;
import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class SubjectObjectRatioTest {

	InMemoryDataset ds;
	Logger log = Logger.getLogger(SubjectObjectRatioTest.class);
	Namespace exampleNs = new Namespace("ex", "http://example.com/");
	
	Resource maxRes;
	Resource petraRes;
	Resource stefanRes;
	Resource thomasRes;
	Resource biancaRes;
	Resource marcoRes;
	Resource nataliaRes;
	
	Resource hamburgRes;
	Resource berlinRes;
	Resource hammRes;
	Resource augsburgRes;
	

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
		
		String hamburgUri = "http://sws.geonames.org/2911298";
		String berlinUri = "http://sws.geonames.org/2950159";
		String hammUri = "http://sws.geonames.org/2911240";
		String augsburgUri = "http://sws.geonames.org/2954172";
		
		maxRes = m.createResource(maxUri).addLiteral(VCARD.FN, maxMusterName);
		petraRes = m.createResource(petraUri).addLiteral(VCARD.FN, petraMusterName);
		stefanRes = m.createResource(stefanUri).addLiteral(VCARD.FN, stefanMusterName);
		thomasRes = m.createResource(thomasUri).addLiteral(VCARD.FN, thomasMusterName);
		biancaRes = m.createResource(biancaUri).addLiteral(VCARD.FN, biancaMusterName);
		marcoRes = m.createResource(marcoUri).addLiteral(VCARD.FN, marcoMusterName);
		nataliaRes = m.createResource(nataliaUri).addLiteral(VCARD.FN, nataliaMusterName);
		
		hamburgRes = m.createResource(hamburgUri);
		berlinRes = m.createResource(berlinUri);
		hammRes = m.createResource(hammUri);
		augsburgRes = m.createResource(augsburgUri);
		
		// output graph for debugging
		log.debug(ds.toString());		
	}

	@Test
	public void noObjectURIs() {
		SubjectObjectRatio sor = new SubjectObjectRatio();
		sor.processSparqlDataset(ds);
		
		double delta = 0.0000001;
		// no URIs that are both used as subject and object
		assertEquals(0, sor.getNumCommonSubjectObjectURIs());
		// 7 person URIs, cities not included in any triple
		assertEquals(7, sor.getNumDistinctSubjectObjectURIs());
		// 0/7.0 = 0.0
		assertEquals(0.0, sor.getSubjectObjectRatio(), delta);
	}
	
	@Test
	public void noCommonURIs() {
		SubjectObjectRatio sor = new SubjectObjectRatio();
		
		petraRes.addProperty(FOAF.based_near, augsburgRes);
		maxRes.addProperty(FOAF.based_near, hamburgRes);
		stefanRes.addProperty(FOAF.based_near, augsburgRes);
		thomasRes.addProperty(FOAF.based_near, berlinRes);
		biancaRes.addProperty(FOAF.based_near, hammRes);
		marcoRes.addProperty(FOAF.based_near, berlinRes);
		nataliaRes.addProperty(FOAF.based_near, hamburgRes);
		
		sor.processSparqlDataset(ds);		

		double delta = 0.0000001;
		// no URIs that are both used as subject and object
		assertEquals(0, sor.getNumCommonSubjectObjectURIs());
		// 7 person + 4 city URIs
		assertEquals(11, sor.getNumDistinctSubjectObjectURIs());
		// 0/11.0 = 0.0
		assertEquals(0.0, sor.getSubjectObjectRatio(), delta);
	}
	
	@Test
	public void simpleCommonURIs() {
	SubjectObjectRatio sor = new SubjectObjectRatio();
		
		// person foaf:based_near city
		petraRes.addProperty(FOAF.based_near, augsburgRes);
		maxRes.addProperty(FOAF.based_near, hamburgRes);
		stefanRes.addProperty(FOAF.based_near, augsburgRes);
		thomasRes.addProperty(FOAF.based_near, berlinRes);
		biancaRes.addProperty(FOAF.based_near, hammRes);
		marcoRes.addProperty(FOAF.based_near, berlinRes);
		nataliaRes.addProperty(FOAF.based_near, hamburgRes);
		
		// person foaf:knows person
		petraRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, petraRes);
		
		maxRes.addProperty(FOAF.knows, marcoRes);
		marcoRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, maxRes);
		marcoRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, marcoRes);
		marcoRes.addProperty(FOAF.knows, biancaRes);
		biancaRes.addProperty(FOAF.knows, marcoRes);
		thomasRes.addProperty(FOAF.knows, biancaRes);
		biancaRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, stefanRes);
		stefanRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, nataliaRes);
		nataliaRes.addProperty(FOAF.knows, thomasRes);
		biancaRes.addProperty(FOAF.knows, stefanRes);
		stefanRes.addProperty(FOAF.knows, biancaRes);
		
		sor.processSparqlDataset(ds);		

		double delta = 0.0000001;
		// 7 person URIs that are both used as subject and object
		assertEquals(7, sor.getNumCommonSubjectObjectURIs());
		// 7 person + 4 city URIs
		assertEquals(11, sor.getNumDistinctSubjectObjectURIs());
		// 7/11
		assertEquals(7.0/11.0, sor.getSubjectObjectRatio(), delta);
	}
	
	@Test
	public void noObjectURIsTripleStream() {
		SubjectObjectRatio sor = new SubjectObjectRatio();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(sor);
		tsa.performAnalysis(null);
		
		double delta = 0.0000001;
		// no URIs that are both used as subject and object
		assertEquals(0, sor.getNumCommonSubjectObjectURIs());
		// 7 person URIs, cities not included in any triple
		assertEquals(7, sor.getNumDistinctSubjectObjectURIs());
		// 0/7.0 = 0.0
		assertEquals(0.0, sor.getSubjectObjectRatio(), delta);
	}
	
	@Test
	public void noCommonURIsTripleStream() {
		SubjectObjectRatio sor = new SubjectObjectRatio();
		
		petraRes.addProperty(FOAF.based_near, augsburgRes);
		maxRes.addProperty(FOAF.based_near, hamburgRes);
		stefanRes.addProperty(FOAF.based_near, augsburgRes);
		thomasRes.addProperty(FOAF.based_near, berlinRes);
		biancaRes.addProperty(FOAF.based_near, hammRes);
		marcoRes.addProperty(FOAF.based_near, berlinRes);
		nataliaRes.addProperty(FOAF.based_near, hamburgRes);
		
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(sor);
		tsa.performAnalysis(null);		

		double delta = 0.0000001;
		// no URIs that are both used as subject and object
		assertEquals(0, sor.getNumCommonSubjectObjectURIs());
		// 7 person + 4 city URIs
		assertEquals(11, sor.getNumDistinctSubjectObjectURIs());
		// 0/11.0 = 0.0
		assertEquals(0.0, sor.getSubjectObjectRatio(), delta);
	}
	
	@Test
	public void simpleCommonURIsTripleStream() {
	SubjectObjectRatio sor = new SubjectObjectRatio();
		
		// person foaf:based_near city
		petraRes.addProperty(FOAF.based_near, augsburgRes);
		maxRes.addProperty(FOAF.based_near, hamburgRes);
		stefanRes.addProperty(FOAF.based_near, augsburgRes);
		thomasRes.addProperty(FOAF.based_near, berlinRes);
		biancaRes.addProperty(FOAF.based_near, hammRes);
		marcoRes.addProperty(FOAF.based_near, berlinRes);
		nataliaRes.addProperty(FOAF.based_near, hamburgRes);
		
		// person foaf:knows person
		petraRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, petraRes);
		
		maxRes.addProperty(FOAF.knows, marcoRes);
		marcoRes.addProperty(FOAF.knows, maxRes);
		maxRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, maxRes);
		marcoRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, marcoRes);
		marcoRes.addProperty(FOAF.knows, biancaRes);
		biancaRes.addProperty(FOAF.knows, marcoRes);
		thomasRes.addProperty(FOAF.knows, biancaRes);
		biancaRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, stefanRes);
		stefanRes.addProperty(FOAF.knows, thomasRes);
		thomasRes.addProperty(FOAF.knows, nataliaRes);
		nataliaRes.addProperty(FOAF.knows, thomasRes);
		biancaRes.addProperty(FOAF.knows, stefanRes);
		stefanRes.addProperty(FOAF.knows, biancaRes);
		
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(sor);
		tsa.performAnalysis(null);		

		double delta = 0.0000001;
		// 7 person URIs that are both used as subject and object
		assertEquals(7, sor.getNumCommonSubjectObjectURIs());
		// 7 person + 4 city URIs
		assertEquals(11, sor.getNumDistinctSubjectObjectURIs());
		// 7/11
		assertEquals(7.0/11.0, sor.getSubjectObjectRatio(), delta);
	}
}
