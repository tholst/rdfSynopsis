package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class PredicateVocabularies extends StatisticalCriterion {

	private Map<String, Integer>	predicateVocabularyUsageMap;

	public PredicateVocabularies() {
		logger = Logger.getLogger(PredicateVocabularies.class);
		logger.trace("logger created");
		textId = "PredicateVocabularies";
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of predicate vocabularies used = "
				+ getNumPredicateVocabularies());
		for (Map.Entry<String, Integer> e : predicateVocabularyUsageMap.entrySet()) {
			ps.println("Result: " + e.getValue() + " triples with predicate vocabulary "
					+ e.getKey());
		}

	}


	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");


		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?predVocab")
					&& qs.contains("?numUses")) { // valid solution
				String propertyNS = qs.getLiteral("?predVocab").getString();
				int numUses = qs.getLiteral("?numUses").getInt();
				
				predicateVocabularyUsageMap.put(propertyNS,numUses);
				
			} else // invalid solution
			logger.debug("invalid solution: " + qs);

		}
		
		logger.debug("propertyUsageMap:\n" + predicateVocabularyUsageMap.toString());
	}
	
	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;

		logger.trace("using triple (" + s + " " + p + " " + o + ")");
					
		Integer numInstances = predicateVocabularyUsageMap.get(p.getNameSpace());
		if (numInstances == null)
			numInstances = 0;
		predicateVocabularyUsageMap.put(p.getNameSpace(),numInstances+1);
			
	}

	public long getVocabUsage(String namespaceUri) {
		if (predicateVocabularyUsageMap.containsKey(namespaceUri))
			return predicateVocabularyUsageMap.get(namespaceUri).longValue();
		else return 0;
	}

	public int getNumPredicateVocabularies() {
		return predicateVocabularyUsageMap.size();
	}

	public Set<String> getPredicateVocabularies() {
		return predicateVocabularyUsageMap.keySet();
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Map.Entry<String, Integer> e : predicateVocabularyUsageMap.entrySet()) {
			m.put(e.getKey()+"_predVocabUsage", e.getValue());
		}
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PredicateVocabularies) {
			PredicateVocabularies o2 = (PredicateVocabularies) o;
			return o2.getNumPredicateVocabularies() == this.getNumPredicateVocabularies() &&
					o2.getPredicateVocabularies().equals(this.getPredicateVocabularies()) &&
					o2.predicateVocabularyUsageMap.equals(this.predicateVocabularyUsageMap);
		}
		else return false;
	}

	@Override
	public void init() {
		predicateVocabularyUsageMap = new HashMap<String, Integer>();
		
	}
}
