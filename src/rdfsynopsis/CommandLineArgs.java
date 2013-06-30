package rdfsynopsis;

import java.util.List;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {
	
	/**
Usage: rdfSynopsis [options]
  Options:
    -all, --allCriteria
       Use all available criteria for analysis.
       Default: true
    -c, --criteria
       A space-separated list of criteria to use for analysis, e.g, "-c 3 5 7"
    -ep, --endpoint
       The SPARQL endpoint URL that shall be analyzed.
    -f, --file
       The RDF dataset file that shall be analyzed.
    -h, --help
       Print this usage information.
       Default: false
    -mnq, --maximumNumberQueries
       The maximum number of queries to perform a partial analysis; "-1" means
       "infinite". (TSA only)
       Default: -1
    -ob, --orderBy
       One of the following variables used to define an order in the triple
       stream: subject, predicate, object (TSA only)
       Default: subject
    -o, --outFile
       The filename used to store analysis results. (NA)
    -rand, --randomSampling
       Use a "random sampled" triple stream. (TSA only)
       Default: true
    -rf, --resultFormat
       One of the following result output formats: text,... (NA)
       Default: text
    -sqa, --specificQuery
       Use one specific SPARQL query per criterion. (SQA)
       Default: true
    -tl, --tripleLimit
       The maximum number of triples requested per query. (TSA only)
       Default: 50000
    -tsa, --tripleStream
       Use generic SPARQL queries to create a triple stream. (TSA)
       Default: false
	 */
	
	// print usage
	@Parameter(names = {"-h","--help"}, help = true, description="Print this usage information.")
	boolean help;
	// print list of criteria
	@Parameter(names = {"-lc","--listCriteria"}, help = true, description="Print list of analytical criteria.")
	boolean listCriteria = false;
	
	// Specific Query Approach (SQA) vs. Triple Stream Approach (TSA) 
	@Parameter(names = {"-sqa","--specificQuery"}, required = false, description="Use one specific SPARQL query per criterion. (SQA)")
	boolean sqa = false;	
	@Parameter(names = {"-tsa","--tripleStream"}, required = false, description="Use generic SPARQL queries to create a triple stream. (TSA)")
	boolean tsa = false;
	
	// Triple Stream arguments
	@Parameter(names = {"-rand","--randomSampling"}, required = false, description="Use a \"random sampled\" triple stream. (TSA only)")
	boolean random = true;
	@Parameter(names = {"-ob","--orderBy"}, required = false, description="One of the following variables used to define an order in the triple stream: subject, predicate, object (TSA only)")
	String orderBy = "subject";
	@Parameter(names = {"-tl","--tripleLimit"}, required = false, description="The maximum number of triples requested per query. (TSA only)")
	Integer tripleLimit = 50000;
	@Parameter(names = {"-mnq","--maximumNumberQueries"}, required = false, description="The maximum number of queries to perform a partial analysis; \"-1\" means \"infinite\". (TSA only, NA)")
	Integer maxNumQueries = -1;	
	
	// Remote Endpoint vs. Local File
	@Parameter(names = {"-ep","--endpoint"}, required = false, description="The SPARQL endpoint URL that shall be analyzed.")
	String endpoint;	
	@Parameter(names = {"-f","--file"}, required = false, description="The RDF dataset file that shall be analyzed.")
	String datasetFile;
	
	// Result output options
	@Parameter(names = {"-rf","--resultFormat"}, required = false, description="One of the following result output formats: text,... (NA)")
	String outputFormat = "text";	
	@Parameter(names = {"-o","--outFile"}, required = false, description="The filename used to store analysis results. (NA)")
	String outputFile = null;
	
	// Analytical Criteria
	@Parameter(names = {"-all","--allCriteria"}, required = false, description="Use all available criteria for analysis.")
	boolean allCriteria = false;
	@Parameter(names = {"-c","--criteria"}, variableArity = true, description="A space-separated list of criteria to use for analysis, e.g, \"-c 3 5 7\"")
	public List<String> criteria;

}
