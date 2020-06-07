package params;

import java.io.Serializable;

import javax.swing.JPanel;

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
     * This member is only constructed when the constructor {@link #ParMCTS(boolean) ParMCTS(boolean withUI)} 
     * called with {@code withUI=true}. It holds the GUI for {@link ParMCTS}.
     */
    private transient MCTSParams msparams = null;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public ParMCTS() {	}
    
	public ParMCTS(boolean withUI) {
		if (withUI)
			msparams = new MCTSParams();
	}
	
    public ParMCTS(ParMCTS tp) { 
    	this.setFrom(tp);
    }
    
    public ParMCTS(MCTSParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(ParMCTS tp) {
		this.kUCT = tp.getK_UCT();
		this.epsGreedy = tp.getEpsGreedy();
		this.numIters = tp.getNumIter();
		this.rolloutDepth = tp.getRolloutDepth();
		this.treeDepth = tp.getTreeDepth();
		this.verbose = tp.getVerbosity();
		this.useNormalize = tp.getNormalize();
		this.selectMode = tp.getSelectMode();
		
		if (msparams!=null)
			msparams.setFrom(this);
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
		
		if (msparams!=null)
			msparams.setFrom(this);
	}

	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromMCTSParams() {
		if (msparams!=null)
			this.setFrom(msparams);
	}
	
	public JPanel getPanel() {
		if (msparams!=null)		
			return msparams.getPanel();
		return null;
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
		if (msparams!=null)
			msparams.setNumIter(numIters);
	}

	public void setSelectMode(int selectMode) {
		this.selectMode=selectMode;
		if (msparams!=null)
			msparams.setSelectMode(selectMode);
	}

	public void setRolloutDepth(int rolloutDepth) {
		this.rolloutDepth = rolloutDepth;
		if (msparams!=null)
			msparams.setRolloutDepth(rolloutDepth);
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
		if (msparams!=null)
			msparams.setTreeDepth(treeDepth);
	}

	public void setK_UCT(double kUCT) {
		this.kUCT = kUCT;
		if (msparams!=null)
			msparams.setK_UCT(kUCT);
	}

	public void setEpsGreedy(double value) {
		this.epsGreedy = value;
		if (msparams!=null)
			msparams.setEpsGreedy(value);
	}

	public void setVerbosity(int verbose) {
		this.verbose = verbose;
		if (msparams!=null)
			msparams.setVerbosity(verbose);
	}
	
	public void setNormalize(boolean bNorm) {
		this.useNormalize=bNorm;
		if (msparams!=null)
			msparams.setNormalize(bNorm);
	}
	
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. If with UI, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName currently only "MCTS" 
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 * @param numPlayers
	 */
	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
		switch (agentName) {
		case "MCTS": 
			switch (gameName) {
			case "Nim": 
			case "Nim3P": 
				this.setNumIter(10000);		
				this.setK_UCT(1.414);
				break;
			}
			switch (numPlayers) {
			case 1: 
				this.setNormalize(false);
				break;
			default:
				this.setNormalize(true);
				break;
			}
			break;
		}
		if (msparams!=null)
			msparams.setFrom(this);
	}	

}
