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

public class PropertyUsage extends StatisticalCriterion {

	private Map<String, Integer>	propertyUsageMap;

	public PropertyUsage() {
		logger = Logger.getLogger(PropertyUsage.class);
		logger.trace("logger created");
		textId = "PropertyUsage";

		init();
	}

	@Override
	public void flushLog(PrintStream ps) {
		logger.debug("flushLog");
		ps.println("Result: number of properties used = "
				+ getNumUsedProperties());
		for (Map.Entry<String, Integer> e : propertyUsageMap.entrySet()) {
			ps.println("Result: " + e.getValue() + " triples with property "
					+ e.getKey());
		}

	}



	@Override
	protected void processQueryResults(ResultSet results) {
		logger.trace("process query results");

		while (results.hasNext()) {
			QuerySolution qs = results.next();

			if (qs.contains("?property")
					&& qs.contains("?numUses")) { // valid solution
				String propertyUri = qs.getResource("?property").getURI();
				int numInstances = qs.getLiteral("?numUses").getInt();
				propertyUsageMap.put(propertyUri, numInstances);
			} else // invalid solution
			logger.debug("invalid solution: " + qs);

		}

		logger.debug("propertyUsageMap:\n" + propertyUsageMap.toString());
	}

	@Override
	public void considerTriple(Resource s, Property p, RDFNode o) {
		assert s != null;
		assert p != null;
		assert o != null;

		logger.trace("using triple (" + s + " " + p + " " + o + ")");

		Integer numInstances = propertyUsageMap.get(p.getURI());
		if (numInstances == null)
			numInstances = 0;
		propertyUsageMap.put(p.getURI(), numInstances + 1);

	}

	public int getNumInstances(String propertyUri) {
		if (propertyUsageMap.containsKey(propertyUri))
			return propertyUsageMap.get(propertyUri);
		else return 0;
	}

	public int getNumInstances(Property property) {
		return getNumInstances(property.getURI());
	}

	public int getNumUsedProperties() {
		return propertyUsageMap.size();
	}

	public Set<String> getPropertyUris() {
		return propertyUsageMap.keySet();
	}

	@Override
	public Map<String, Object> getResultMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Map.Entry<String, Integer> e : propertyUsageMap.entrySet()) {
			m.put(e.getKey() + "_propertyUsage", e.getValue());
		}
		return m;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PropertyUsage) {
			PropertyUsage o2 = (PropertyUsage) o;
			return o2.getNumUsedProperties() == this.getNumUsedProperties() &&
					o2.propertyUsageMap.equals(this.propertyUsageMap);
		}
		else return false;
	}

	@Override
	public void init() {
		propertyUsageMap = new HashMap<String, Integer>();
	}

}
