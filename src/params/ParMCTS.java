package params;

import java.io.Serializable;

/**
 * 
 * @see MCTSParams
 */
public class ParMCTS implements Serializable {
    public static int DEFAULT_ROLLOUT_DEPTH =200;
    public static int DEFAULT_TREE_DEPTH = 10;
    public static int DEFAULT_NUM_ITERS = 1000;
    public static double DEFAULT_K = Math.sqrt(2);
    public static int DEFAULT_VERBOSITY = 0;
    private int numIters = DEFAULT_NUM_ITERS;
	private int rolloutDepth = DEFAULT_ROLLOUT_DEPTH;
    private int treeDepth = DEFAULT_TREE_DEPTH;
    private double kUCT = DEFAULT_K;
    private int verbose = DEFAULT_VERBOSITY; 

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
		this.numIters = tp.getNumIter();
		this.rolloutDepth = tp.getRolloutDepth();
		this.treeDepth = tp.getTreeDepth();
		this.verbose = tp.getVerbosity();
	}

    public int getNumIter() {
		return numIters;
	}
	public double getK_UCT() {
		return kUCT;
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

	public void setNumIter(int numIters) {
		this.numIters = numIters;
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

	public void setVerbose(int verbose) {
		this.verbose = verbose;
	}
}
