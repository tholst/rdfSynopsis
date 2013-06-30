package rdfsynopsis.eval;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class EvaluationSet extends AbstractCompositeEvaluator {

	protected Set<AbstractEvaluator>	children;
	
	public EvaluationSet(String title) {
		this(title, true);
	}

	public EvaluationSet(String title, boolean timeStamp) {
		super(title, timeStamp);
		children = new HashSet<AbstractEvaluator>();
	}

	@Override
	public void evaluate(File outDir, String titleAddition) {
		File childDir = makeTitleDir(title+titleAddition, outDir,timeStamp);
		for (AbstractEvaluator childEvaluator : children) {
			childEvaluator.evaluate(childDir);
		}
	}

	public EvaluationSet addChild(AbstractEvaluator ae) {
		children.add(ae);
		return this;
	}

	public Set<AbstractEvaluator> getChildren() {
		return children;
	}

	public EvaluationSet setChildren(Set<AbstractEvaluator> children) {
		this.children = children;
		return this;
	}

}
