package params;

import java.io.Serializable;

/**
 * 
 * @see MCTSParams
 */
public class ParMCTS implements Serializable {
    public static final int DEFAULT_ROLLOUT_DEPTH =200;
    public static final int DEFAULT_TREE_DEPTH = 10;
    public static final int DEFAULT_NUM_ITERS = 1000;
    public static final double DEFAULT_K = Math.round(1000*Math.sqrt(2))/1000.0;
	/**
	 * epsGreedy = probability that a random action is taken (instead of a 
	 * greedy action). This is *only* relevant, if function egreedy() is used in
	 * MCTS treePolicy, which is the case if selectMode==1 ("eps-greedy").
	 */
    public static final double DEFAULT_EPSILONGREEDY = 0.05;
    public static final int DEFAULT_VERBOSITY = 0;
    public static final int DEFAULT_SELECT_MODE = 0;	// 0:[UCT], 1:[eps-greedy], 2:[roulette wheel]
    private int numIters = DEFAULT_NUM_ITERS;
	private int rolloutDepth = DEFAULT_ROLLOUT_DEPTH;
    private int treeDepth = DEFAULT_TREE_DEPTH;
    private double kUCT = DEFAULT_K;
    private double epsGreedy = DEFAULT_EPSILONGREEDY;
    private int verbose = DEFAULT_VERBOSITY; 
    private boolean useNormalize = true;
    private int selectMode = DEFAULT_SELECT_MODE;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public ParMCTS() {	}
    
    public ParMCTS(MCTSParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(MCTSParams tp) {
		this.kUCT = tp.getK_UCT();
		this.epsGreedy = tp.getEpsGreedy();
		this.numIters = tp.getNumIter();
		this.rolloutDepth = tp.getRolloutDepth();
		this.treeDepth = tp.getTreeDepth();
		this.verbose = tp.getVerbosity();
		this.useNormalize = tp.getNormalize();
		this.selectMode = tp.getSelectMode();
	}

    public int getNumIter() {
		return numIters;
	}
	public int getSelectMode() {
		return selectMode;
	}
	public double getK_UCT() {
		return kUCT;
	}
	public double getEpsGreedy() {
		return epsGreedy;
	}
	public int getTreeDepth() {
		return treeDepth;
	}
	public int getRolloutDepth() {
		return rolloutDepth;
	}
	public int getVerbosity() {
		return verbose;
	}

	public boolean getNormalize() {
		return useNormalize;
	}

	public void setNumIter(int numIters) {
		this.numIters = numIters;
	}

	public void setSelectMode(int selectMode) {
		this.selectMode=selectMode;
	}

	public void setRolloutDepth(int rolloutDepth) {
		this.rolloutDepth = rolloutDepth;
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
	}

	public void setkUCT(double kUCT) {
		this.kUCT = kUCT;
	}

	public void setEpsGreedy(double value) {
		this.epsGreedy = value;
	}

	public void setVerbose(int verbose) {
		this.verbose = verbose;
	}
}
