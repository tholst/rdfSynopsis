package rdfsynopsis.util;

public class Namespace {

	public static final Namespace RDF = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static final Namespace RDFS = new Namespace("rdfs",	"http://www.w3.org/2000/01/rdf-schema#");
	public static final Namespace OWL = new Namespace("owl", "http://www.w3.org/2002/07/owl#");
	public static final Namespace XSD = new Namespace("xsd", "http://www.w3.org/2001/XMLSchema#");
	public static final Namespace FOAF = new Namespace("foaf", "http://xmlns.com/foaf/0.1/");
	public static final Namespace SKOS = new Namespace("skos", "http://www.w3.org/2004/02/skos/core#");

	private String prefix;
	private String fullURI;

	public Namespace(String prefix, String fullURI) {
		this.prefix = prefix;
		this.fullURI = fullURI;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getFullURI() {
		return fullURI;
	}

	public String getSparqlPrefix() {
		return createSparqlPrefix(prefix, fullURI);
	}
	
	public String getPrefixedTerm(String term) {
		return prefix + ":" + term;
	}
	
	public String getFullTerm(String term) {
		return fullURI + term;
	}
	
	public static String createSparqlPrefix(String prefix, String fullURI) {
		return "PREFIX " + prefix + ": <" + fullURI + ">";
	}
}
