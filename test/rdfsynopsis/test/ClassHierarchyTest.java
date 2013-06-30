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
import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.VCARD;

public class ClassHierarchyTest {

	InMemoryDataset ds;
	Logger log = Logger.getLogger(ClassHierarchyTest.class);
	Namespace exampleNs = new Namespace("ex", "http://example.com/");

	@Before
	public void setUpBefore() throws Exception {
		ds = new InMemoryDataset();
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
	public void noHierarchy() {
		ClassHierarchy ch = new ClassHierarchy();
		ch.processSparqlDataset(ds);
		
		assertEquals(0,ch.getNumClassesInHierarchy());
		assertEquals(0,ch.getClassHierarchyDepth());
		assertEquals(0,ch.getNumClassHierarchyTriples());
	}
	
	@Test
	public void simpleHierarchy() {
		ClassHierarchy ch = new ClassHierarchy();
		Model m = ds.getModel();
		
		Resource manClass = m.createResource(exampleNs.getFullTerm("Man"));
		Resource womanClass = m.createResource(exampleNs.getFullTerm("Woman"));
		m.add(manClass, RDFS.subClassOf, FOAF.Person);
		m.add(womanClass, RDFS.subClassOf, FOAF.Person);
		
		ch.processSparqlDataset(ds);		
		
		assertEquals(3,ch.getNumClassesInHierarchy());
		assertEquals(1,ch.getClassHierarchyDepth());
		assertEquals(2,ch.getNumClassHierarchyTriples());
	}
	
	@Test
	public void mediumHierarchy() {
		ClassHierarchy ch = new ClassHierarchy();
		Model m = ds.getModel();
		
		Resource thingClass = m.createResource(exampleNs.getFullTerm("Thing"));
		Resource airplaneClass = m.createResource(exampleNs.getFullTerm("Airplane"));
		Resource carClass = m.createResource(exampleNs.getFullTerm("Car"));
		Resource vehicleClass = m.createResource(exampleNs.getFullTerm("Vehicle"));
		Resource bikeClass = m.createResource(exampleNs.getFullTerm("Bike"));
		Resource convertibleClass = m.createResource(exampleNs.getFullTerm("Convertible"));
		Resource motorbikeClass = m.createResource(exampleNs.getFullTerm("Motorbike"));
		Resource shipClass = m.createResource(exampleNs.getFullTerm("Ship"));
		Resource yachtClass = m.createResource(exampleNs.getFullTerm("Yacht"));
		Resource suvClass = m.createResource(exampleNs.getFullTerm("SUV"));
		
		m.add(airplaneClass, RDFS.subClassOf, vehicleClass);
		m.add(carClass, RDFS.subClassOf, thingClass);
		m.add(convertibleClass, RDFS.subClassOf, carClass);
		m.add(yachtClass, RDFS.subClassOf, thingClass);
		m.add(motorbikeClass, RDFS.subClassOf, bikeClass);
		m.add(suvClass, RDFS.subClassOf, carClass);
		m.add(bikeClass, RDFS.subClassOf, thingClass);
		m.add(carClass, RDFS.subClassOf, vehicleClass);
		m.add(vehicleClass, RDFS.subClassOf, thingClass);
		m.add(bikeClass, RDFS.subClassOf, vehicleClass);
		m.add(shipClass, RDFS.subClassOf, vehicleClass);
		m.add(yachtClass, RDFS.subClassOf, shipClass);
		
		ch.processSparqlDataset(ds);		
		
		assertEquals(10,ch.getNumClassesInHierarchy());
		assertEquals(3,ch.getClassHierarchyDepth());
		assertEquals(12,ch.getNumClassHierarchyTriples());
	}
	
	@Test
	public void noHierarchyTripleStream() {
		ClassHierarchy ch = new ClassHierarchy();
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(ch);
		tsa.performAnalysis(null);
		
		assertEquals(0,ch.getNumClassesInHierarchy());
		assertEquals(0,ch.getClassHierarchyDepth());
		assertEquals(0,ch.getNumClassHierarchyTriples());
	}
	
	@Test
	public void simpleHierarchyTripleStream() {
		ClassHierarchy ch = new ClassHierarchy();
		Model m = ds.getModel();
		
		Resource manClass = m.createResource(exampleNs.getFullTerm("Man"));
		Resource womanClass = m.createResource(exampleNs.getFullTerm("Woman"));
		m.add(manClass, RDFS.subClassOf, FOAF.Person);
		m.add(womanClass, RDFS.subClassOf, FOAF.Person);
		
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(ch);
		tsa.performAnalysis(null);		
		
		assertEquals(3,ch.getNumClassesInHierarchy());
		assertEquals(1,ch.getClassHierarchyDepth());
		assertEquals(2,ch.getNumClassHierarchyTriples());
	}
	
	@Test
	public void mediumHierarchyTripleStream() {
		ClassHierarchy ch = new ClassHierarchy();
		Model m = ds.getModel();
		
		Resource thingClass = m.createResource(exampleNs.getFullTerm("Thing"));
		Resource airplaneClass = m.createResource(exampleNs.getFullTerm("Airplane"));
		Resource carClass = m.createResource(exampleNs.getFullTerm("Car"));
		Resource vehicleClass = m.createResource(exampleNs.getFullTerm("Vehicle"));
		Resource bikeClass = m.createResource(exampleNs.getFullTerm("Bike"));
		Resource convertibleClass = m.createResource(exampleNs.getFullTerm("Convertible"));
		Resource motorbikeClass = m.createResource(exampleNs.getFullTerm("Motorbike"));
		Resource shipClass = m.createResource(exampleNs.getFullTerm("Ship"));
		Resource yachtClass = m.createResource(exampleNs.getFullTerm("Yacht"));
		Resource suvClass = m.createResource(exampleNs.getFullTerm("SUV"));
		
		m.add(airplaneClass, RDFS.subClassOf, vehicleClass);
		m.add(carClass, RDFS.subClassOf, thingClass);
		m.add(convertibleClass, RDFS.subClassOf, carClass);
		m.add(yachtClass, RDFS.subClassOf, thingClass);
		m.add(motorbikeClass, RDFS.subClassOf, bikeClass);
		m.add(suvClass, RDFS.subClassOf, carClass);
		m.add(bikeClass, RDFS.subClassOf, thingClass);
		m.add(carClass, RDFS.subClassOf, vehicleClass);
		m.add(vehicleClass, RDFS.subClassOf, thingClass);
		m.add(bikeClass, RDFS.subClassOf, vehicleClass);
		m.add(shipClass, RDFS.subClassOf, vehicleClass);
		m.add(yachtClass, RDFS.subClassOf, shipClass);
		
		Analyzer tsa = new TripleStreamAnalyzer(ds)
		.addCriterion(ch);
		tsa.performAnalysis(null);		
		
		assertEquals(10,ch.getNumClassesInHierarchy());
		assertEquals(3,ch.getClassHierarchyDepth());
		assertEquals(12,ch.getNumClassHierarchyTriples());
	}
}
