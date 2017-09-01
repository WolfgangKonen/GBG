package params;

import java.io.Serializable;

public class ParOther implements Serializable {
    public static int DEFAULT_QUICK_EVAL_MODE = 0;
    public static int DEFAULT_TRAIN_EVAL_MODE = 0;
    public static int DEFAULT_NUM_EVAL = 500;
    public static int DEFAULT_EPISODE_LENGTH = -1;
    public static int DEFAULT_STOP_TEST = 0;
    public static int DEFAULT_STOP_EVAL = 100;
    public static int DEFAULT_MINIMAX_DEPTH = 10;

    private int quickEvalMode = DEFAULT_QUICK_EVAL_MODE;
    private int trainEvalMode = DEFAULT_TRAIN_EVAL_MODE;
    private int numEval = DEFAULT_NUM_EVAL;
    private int episodeLength = DEFAULT_EPISODE_LENGTH;
    private int stopTest = DEFAULT_STOP_TEST;
    private int stopEval = DEFAULT_STOP_EVAL; 
    private boolean chooseStart01 = false;
    private boolean learnFromRM = false;
    
    private int minimaxDepth = DEFAULT_MINIMAX_DEPTH;
    private boolean minimaxHashmap = true;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;
	
	public ParOther() {	}
    
    public ParOther(OtherParams op) { 
    	this.setFrom(op);
    }
    
	public void setFrom(OtherParams op) {
		this.quickEvalMode = op.getQuickEvalMode();
		this.trainEvalMode = op.getTrainEvalMode();
		this.numEval = op.getNumEval();
		this.episodeLength = op.getEpiLength();
		this.stopTest = op.getStopTest();
		this.stopEval = op.getStopEval();
		this.chooseStart01 = op.useChooseStart01();
		this.learnFromRM = op.useLearnFromRM();
		this.minimaxDepth = op.getMinimaxDepth();
		this.minimaxHashmap = op.useMinimaxHashmap();	
	}

	public int getQuickEvalMode() {
		return quickEvalMode;
	}

	public int getTrainEvalMode() {
		return trainEvalMode;
	}

	/**
	 * @return During training: Call evaluator after this number of training games
	 */
	public int getNumEval() {
		return numEval;
	}

	public int getEpisodeLength() {
		return episodeLength;
	}

	public int getStopTest() {
		return stopTest;
	}

	public int getStopEval() {
		return stopEval;
	}

	public boolean useChooseStart01() {
		return chooseStart01;
	}

	public boolean useLearnFromRM() {
		return learnFromRM;
	}

	public int getMinimaxDepth() {
		return minimaxDepth;
	}

	public boolean useMinimaxHashmap() {
		return minimaxHashmap;
	}

}
