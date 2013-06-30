package rdfsynopsis.statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rdfsynopsis.util.Namespace;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class NamespaceLinks extends StatisticalCriterion {
	
	// NS1 -> (NS2 -> (P -> numTriples))
	private Map<String, Map<String, Map<String, Integer>>> nsLinksMap;

	public NamespaceLinks() {
		logger = Logger.getLogger(NamespaceLinks.class);
		logger.trace("logger created");
		textId = "NamespaceLinks";
		
		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		
		for (String ns1 : nsLinksMap.keySet()) {
			Map<String, Map<String, Integer>> ns2Map = nsLinksMap.get(ns1);
			if (ns2Map != null)
			for (String ns2 : ns2Map.keySet()) {
				Map<String, Integer> pMap = ns2Map.get(ns2);
				if (pMap != null)
					for (String p : pMap.keySet()) {
						Integer numLinks = pMap.get(p);
						if (numLinks != null)
						ps.println("Links: " + ns1 + " -> " + ns2 + " with prop. " + p + " ("+ numLinks + ")");				
					}
			}
		}
		
		

	}
	
	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?NS1") && qs.contains("?NS2") && qs.contains("?prop") && qs.contains("?numLinks")) {
				// valid solution
				
				String ns1 = qs.getLiteral("?NS1").getString();
				String ns2 = qs.getLiteral("?NS2").getString();
				String prop = qs.getResource("?prop").getURI();
				Integer numLinks = qs.getLiteral("?numLinks").getInt();
				
				increaseNumLinks(ns1, ns2, prop, numLinks);
				
			} else
			// invalid solution
			logger.debug("invalid solution: " + qs);

		}
	}

	private void increaseNumLinks(String ns1, String ns2, String prop,
			Integer numLinks) {
		Map<String, Map<String, Integer>> ns2Map = nsLinksMap.get(ns1);
		if (ns2Map == null)
			ns2Map = new HashMap<String, Map<String,Integer>>();
		Map<String, Integer> pMap = ns2Map.get(ns2);
		if (pMap == null)
			pMap = new HashMap<String, Integer>();
		Integer nLinks = pMap.get(prop);
		if (nLinks == null)
			nLinks = 0;
		pMap.put(prop, nLinks + numLinks);
		ns2Map.put(ns2, pMap);
		nsLinksMap.put(ns1, ns2Map);
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;		
		
		if (s.isURIResource() && o.isURIResource()) {
			String ns1 = s.getURI().replaceAll("[^/#]*$", "");
			String ns2 = ((Resource)o).getURI().replaceAll("[^/#]*$", "");
			
			increaseNumLinks(ns1, ns2, p.getURI(), 1);
			
			logger.trace("using triple (" + s + " " + p + " " + o + ")");
		}
		
	}
	
	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		return m;
	}

	@Override
	public void init() {
		nsLinksMap = new HashMap<String, Map<String,Map<String,Integer>>>();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NamespaceLinks) {
			NamespaceLinks o2 = (NamespaceLinks) o;
			
			return o2.nsLinksMap.equals(this.nsLinksMap);
		}
		else return false;
	}

}
