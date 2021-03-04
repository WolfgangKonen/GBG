package params;

import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;

import javax.swing.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * MCTSE (Monte Carlo Tree Search Expectimax) parameters for board games.<p>
 *  
 * These parameters and their [defaults] are: <ul>
 * <li> <b>Iterations</b>: 	[3500] number of iterations during MCTSE search 
 * <li> <b>K (UCT)</b>: 	[1.414] parameter K in UCT rule  
 * <li> <b>Tree Depth</b>: 	[ 10] MCTSE tree depth 
 * <li> <b>Rollout Depth</b>[150] MCTSE rollout depth  
 * <li> <b>Max Nodes</b>	[500] max number of nodes that expand() can create  
 * <li> <b>Number Agents</b>[  1] number of agents for majority vote  
 * </ul>
 * The defaults are defined in this class. 
 * 
 * @see MCTSExpectimaxAgt
 * @see MCTSExpectimaxParams
 */
public class ParMCTSE implements Serializable
{
	public static final int DEFAULT_VERBOSITY = 0; 
    public static final int DEFAULT_ITERATIONS = 3500;                  //Number of games played for all available actions
    public static final int DEFAULT_ROLLOUTDEPTH = 150;                 //Number of times advance() is called for every random rollout
    public static final int DEFAULT_TREEDEPTH = 10;
    public static final double DEFAULT_K = Math.round(1000*Math.sqrt(2))/1000.0;
	/**
	 * epsGreedy = probability that a random action is taken (instead of a 
	 * greedy action). This is *only* relevant, if function egreedy() is used in
	 * MCTSE treePolicy, which is the case if selectMode==1 ("eps-greedy").
	 */
    public static final double DEFAULT_EPSILONGREEDY = 0.05;
    public static final int DEFAULT_MAXNODES = 500;                     //Max number of nodes expand() can create
    public static final boolean DEFAULT_ALTERNATEVERSION = false;      //Use AltChanceNode instead of MCTSEChanceNode
    public static final boolean DEFAULT_ENABLEHEURISTICS = false;
    public static final boolean DEFAULT_NORMALIZE = true;
	public static final boolean DEFAULT_STOPONROUNDOVER = true;
    public static final int DEFAULT_NUMAGENTS = 1;                      //number Agents for majority vote
    public static final int DEFAULT_SELECT_MODE = 0;	// 0:[UCT], 1:[eps-greedy], 2:[roulette wheel]
    private int numIters = DEFAULT_ITERATIONS;
	private int rolloutDepth = DEFAULT_ROLLOUTDEPTH;
    private int treeDepth = DEFAULT_TREEDEPTH;
    private int verbosity = DEFAULT_VERBOSITY;
    private int selectMode = DEFAULT_SELECT_MODE;
    private double kUCT = DEFAULT_K;
    private double epsGreedy = DEFAULT_EPSILONGREEDY;
    private boolean useNormalize = DEFAULT_NORMALIZE;
    private int maxNodes = DEFAULT_MAXNODES; 
    private int numAgents = DEFAULT_NUMAGENTS;
    private boolean alternateVersion = DEFAULT_ALTERNATEVERSION;
    private boolean enableHeuristics = DEFAULT_ENABLEHEURISTICS;
	private boolean stopOnRoundOver = DEFAULT_STOPONROUNDOVER;

	private HeuristicSettings2048 heuristicSettings2048;

    /**
     * This member is only constructed when the constructor {@link #ParMCTSE(boolean) ParMCTSE(boolean withUI)}
     * is called with {@code withUI=true}. It holds the GUI for {@link ParMCTSE}.
     */
    private transient MCTSExpectimaxParams meparams = null;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 1L;


	public ParMCTSE() {
		heuristicSettings2048 = new HeuristicSettings2048();
	}
	
	public ParMCTSE(boolean withUI) {
		heuristicSettings2048 = new HeuristicSettings2048();
		if (withUI)
			meparams = new MCTSExpectimaxParams();
	}
	
    public ParMCTSE(ParMCTSE tp) { 
		heuristicSettings2048 = new HeuristicSettings2048();
    	this.setFrom(tp);
    }
    
    public ParMCTSE(MCTSExpectimaxParams tp) { 
		heuristicSettings2048 = new HeuristicSettings2048();
    	this.setFrom(tp);
    }
    
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(MCTSExpectimaxParams tp) {
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setK_UCT(tp.getK_UCT());
		this.setEpsGreedy(tp.getEpsGreedy());
		setMaxNodes(tp.getMaxNodes());						
		setNumAgents(tp.getNumAgents());					
		setVerbosity(tp.getVerbosity());
		setAlternateVersion(tp.getAlternateVersion());	
		setEnableHeuristics(tp.getEnableHeuristics());
		setHeuristicSettings2048(tp.getHeuristicSettings2048());
		this.setSelectMode(tp.getSelectMode());
		this.useNormalize = tp.getNormalize();
		this.stopOnRoundOver = tp.getStopOnRoundOver();

		if (meparams!=null)
			meparams.setFrom(this);
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(ParMCTSE tp) {
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setK_UCT(tp.getK_UCT());
		this.setEpsGreedy(tp.getEpsGreedy());
		setMaxNodes(tp.getMaxNodes());						
		setNumAgents(tp.getNumAgents());					
		setVerbosity(tp.getVerbosity());
		setAlternateVersion(tp.getAlternateVersion());	
		setEnableHeuristics(tp.getEnableHeuristics());
		setHeuristicSettings2048(tp.getHeuristicSettings2048());
		this.setSelectMode(tp.getSelectMode());
		this.useNormalize = tp.getNormalize();
		this.stopOnRoundOver = tp.getStopOnRoundOver();

		if (meparams!=null)
			meparams.setFrom(this);
	}
	
	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromMCTSEParams() {
		if (meparams!=null)
			this.setFrom(meparams);
	}
	
	public JPanel getPanel() {
		if (meparams!=null)		
			return meparams.getPanel();
		return null;
	}

	public int getNumIter() {
		return this.numIters;
	}
	public int getSelectMode() {
		return selectMode;
	}
	public double getK_UCT() {
		return this.kUCT;
	}
	public boolean getNormalize() {
		return useNormalize;
	}
	public double getEpsGreedy() {
		return epsGreedy;
	}
	public int getTreeDepth() {
		return this.treeDepth;
	}
	public int getRolloutDepth() {
		return this.rolloutDepth;
	}
	public int getMaxNodes() {
		return this.maxNodes;
	}
	public int getNumAgents()  {
		return this.numAgents;
	}
	public boolean getAlternateVersion() {
		return this.alternateVersion;
	}
	public boolean getEnableHeuristics() {
		return this.enableHeuristics;
	}
	public HeuristicSettings2048 getHeuristicSettings2048() {
		return this.heuristicSettings2048;
	}
	public int getVerbosity() {
		return this.verbosity;
	}
	public boolean getStopOnRoundOver() {
		return stopOnRoundOver;
	}

	public void setNumIter(int value) {
		numIters = value;
		if (meparams!=null)
			meparams.setNumIter(value);
	}
	public void setK_UCT(double value) {
		kUCT = value;
		if (meparams!=null)
			meparams.setK_UCT(value);
	}
	public void setNormalize(boolean state) {
		useNormalize = state;
		if (meparams!=null)
			meparams.setNormalize(state);
	}
	public void setTreeDepth(int value) {
		treeDepth = value;
		if (meparams!=null)
			meparams.setTreeDepth(value);
	}
	public void setSelectMode(int selectMode) {
		this.selectMode=selectMode;
		if (meparams!=null)
			meparams.setSelectMode(selectMode);
	}
	public void setRolloutDepth(int value) {
		rolloutDepth = value;
		if (meparams!=null)
			meparams.setRolloutDepth(value);
	}
	public void setMaxNodes(int value) {
		maxNodes = value;
		if (meparams!=null)
			meparams.setMaxNodes(value);
	}
	public void setNumAgents(int value) { 
		numAgents = value; 
		if (meparams!=null)
			meparams.setNumAgents(value);
	}
	public void setAlternateVersion(boolean value) {
		alternateVersion = value;
		if (meparams!=null)
			meparams.setAlternateVersion(value);
	}
	public void setEnableHeuristics(boolean value) {
		enableHeuristics = value;
		if (meparams!=null)
			meparams.setEnableHeuristics(value);
	}
	public void setHeuristicSettings2048(HeuristicSettings2048 heuristicSettings2048) {
		this.heuristicSettings2048 = heuristicSettings2048;
		if (meparams!=null)
			meparams.setHeuristicSettings2048(heuristicSettings2048);
	}
	public void setEpsGreedy(double value) {
		this.epsGreedy = value;
		if (meparams!=null)
			meparams.setEpsGreedy(value);
	}
	public void setVerbosity(int verbosity) {
		this.verbosity = verbosity;
		if (meparams!=null)
			meparams.setVerbosity(verbosity);
	}

	public void setStopOnRoundOver(boolean stopOnRoundOver) {
		this.stopOnRoundOver = stopOnRoundOver;
		if (meparams!=null)
			meparams.setStopOnRoundOver(stopOnRoundOver);
	}
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. If with UI, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName currently only "MCTSE" 
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 * @param numPlayers	number of players in this game
	 */
	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
		if ("MCTS Expectimax".equals(agentName)) {
			switch (gameName) {
				case "Nim", "Nim3P" -> {
					this.setNumIter(10000);
					this.setK_UCT(1.414);
				}
			}
			this.setAlternateVersion(false);
			if (numPlayers == 1) {
				this.setNormalize(true);
			} else {
				this.setNormalize(true);
			}
		}
		if (meparams!=null)
			meparams.setFrom(this);
	}	

}