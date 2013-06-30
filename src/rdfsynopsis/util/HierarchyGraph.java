package rdfsynopsis.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rdfsynopsis.statistics.ClassHierarchy;

public class HierarchyGraph<V> {

	private class HierarchyNode<T> {

		private T						nodeVal;
		private HierarchyNode<T>		topNode;
		private Set<HierarchyNode<T>>	subNodes;
		private Set<HierarchyNode<T>>	superNodes;
		private int						hierarchyLevel;

		public HierarchyNode(T nodeVal) {
			super();
			assert nodeVal != null;

			this.nodeVal = nodeVal;
			this.hierarchyLevel = 0;
			this.topNode = this;
			this.subNodes = new HashSet<HierarchyNode<T>>();
			this.superNodes = new HashSet<HierarchyNode<T>>();
		}

		private void addSuperNode(HierarchyNode<T> superNode) {
			superNodes.add(superNode);
		}

		void addSubNode(HierarchyNode<T> subNode) {
			subNodes.add(subNode);

			subNode.addSuperNode(this);
			subNode.update(topNode, hierarchyLevel);
		}

		private void update(HierarchyNode<T> newTopNode, int superNodeLevel) {
			// only update if new top node has changed level
			if (hierarchyLevel <= superNodeLevel) {
				hierarchyLevel = superNodeLevel + 1;
				topNode = newTopNode;
				// recursively to subnodes
				for (HierarchyNode<T> subNode : subNodes) {
					subNode.update(newTopNode, hierarchyLevel);
				}
			}
		}

		private String printHierarchy(int level, boolean withDuplicates) {
			String s = "";
			for (int i = 0; i < level; i++) {
				s += " ";
			}
			s += nodeVal + "(" + this.hierarchyLevel + ")";

			if (subNodes.size() > 0) {
				if (withDuplicates || this.hierarchyLevel == level) {
					s += " <--\n";
					for (HierarchyNode<T> subNode : subNodes)
						s += subNode.printHierarchy(level + 1, withDuplicates);
					return s;
				} else return s + " (see below for subclasses).\n";
			}
			return s + "\n";
		}

		@Override
		public String toString() {
			return printHierarchy(hierarchyLevel, true);
		}
	}

	// all classes occurring in subClassOf triples
	private Map<V, HierarchyNode<V>>	nodes;

	// all nodes at top of hierarchy,
	// i.e., node that have no super node
	private Map<V, HierarchyNode<V>>	topNodes;

	private Logger						logger;

	public HierarchyGraph() {
		logger = Logger.getLogger(HierarchyGraph.class);
		logger.trace("logger created");

		nodes = new HashMap<V, HierarchyNode<V>>();
		topNodes = new HashMap<V, HierarchyNode<V>>();
	}

	public void addHierarchyEdge(V subNodeVal, V superNodeVal) {
		logger.trace("Begin Add Hierachry Edge");

		HierarchyNode<V> subNode = getHierarchyNode(subNodeVal);
		HierarchyNode<V> superNode = getHierarchyNode(superNodeVal);

		topNodes.remove(subNodeVal);
		superNode.addSubNode(subNode);
		logger.trace("End Add Hierachry Edge");
	}

	private HierarchyNode<V> getHierarchyNode(V nodeVal) {
		HierarchyNode<V> hierarchyNode;
		if (nodes.containsKey(nodeVal)) {
			hierarchyNode = nodes.get(nodeVal);
		} else {
			hierarchyNode = new HierarchyNode<V>(nodeVal);
			nodes.put(nodeVal, hierarchyNode);
			topNodes.put(nodeVal, hierarchyNode);
		}

		return hierarchyNode;
	}

	public int getMaxHierarchyDepth() {
		int maxDepth = 0;

		for (HierarchyNode<V> node : nodes.values())
			if (maxDepth < node.hierarchyLevel)
				maxDepth = node.hierarchyLevel;

		return maxDepth;
	}

	public int getNumNodes() {
		return nodes.size();
	}

	public boolean isWellFormed() {
		// TODO implement
		return false;
	}

	@Override
	public String toString() {
		String res = "";
		for (HierarchyNode<V> node : topNodes.values()) {
			res += node + "\n";
		}
		return res;
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof HierarchyGraph) {
			HierarchyGraph o2 = (HierarchyGraph) o;
			return (o2.getMaxHierarchyDepth() == this.getMaxHierarchyDepth()) &&
					o2.nodes.equals(this.nodes) &&
					o2.topNodes.equals(this.topNodes);
		}
		else return false;
	}
}
