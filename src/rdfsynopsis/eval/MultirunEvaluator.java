package rdfsynopsis.eval;

import java.io.File;

public class MultirunEvaluator extends AbstractEvaluator {

	protected AbstractEvaluator	evaluator;
	protected int				numRuns;

	public MultirunEvaluator(String title, AbstractEvaluator evaluator,
			int numRuns, boolean timeStamp) {
		super(title,timeStamp);
		this.evaluator = evaluator;
		this.numRuns = numRuns;
	}

	public MultirunEvaluator(String title, boolean timeStamp) {
		this(title, null, 0, timeStamp);
	}

	public AbstractEvaluator getEvaluator() {
		return evaluator;
	}

	public MultirunEvaluator setEvaluator(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
		return this;
	}

	public int getNumRuns() {
		return numRuns;
	}

	public MultirunEvaluator setNumRuns(int numRuns) {
		this.numRuns = numRuns;
		return this;
	}

	@Override
	public void evaluate(File outDir, String titleAddition) {
		if (evaluator != null && numRuns > 0) {
			File childDir = makeTitleDir(title + titleAddition, outDir,timeStamp);
			for (int i = 1; i < numRuns+1; i++) {
				evaluator.evaluate(childDir, "_run"+i);				
			}
		} else logger.warn("Evaluation not executed! "
				+ (evaluator == null ? "No evaluator set! " : "")
				+ (numRuns <= 0 ? "NumRuns == " + numRuns : "")
				+ " Correctly initialized?");
	}
}
