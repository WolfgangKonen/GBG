package params;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.MCTSExpectimax.MCTSEChanceNode;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;

import javax.swing.*;
import java.awt.*;
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
    public static final int DEFAULT_ITERATIONS = 3500;                  //Number of Games played for every available Action
    public static final int DEFAULT_ROLLOUTDEPTH = 150;                 //Number of times advance() is called for every Iteration
    public static final int DEFAULT_TREEDEPTH = 10;
    public static final double DEFAULT_K = Math.round(1000*Math.sqrt(2))/1000.0;;
    public static final int DEFAULT_MAXNODES = 500;                     //Max number of nodes expand() can create
    public static final boolean DEFAULT_ALTERNATEVERSION = false;      //Use AltChanceNode instead of MCTSEChanceNode
    public static final boolean DEFAULT_ENABLEHEURISTICS = false;
    public static final int DEFAULT_NUMAGENTS = 1;                      //number Agents for majority vote
    /**
     *  egreedyEpsilon = probability that a random action is taken (instead
     *  greedy action). This is *only* relevant, if function egreedy() is
     *  used as variant to uct() (which is currently *not* the case).
     */
    public static final double EGREEDYEPSILON = 0.05;
    private int numIters = DEFAULT_ITERATIONS;
	private int rolloutDepth = DEFAULT_ROLLOUTDEPTH;
    private int treeDepth = DEFAULT_TREEDEPTH;
    private double kUCT = DEFAULT_K;
    private int maxNodes = DEFAULT_MAXNODES; 
    private int numAgents = DEFAULT_NUMAGENTS;
    private boolean alternateVersion = DEFAULT_ALTERNATEVERSION;
    private boolean enableHeuristics = DEFAULT_ENABLEHEURISTICS;
    private int verbosity = DEFAULT_VERBOSITY;
    private boolean useNormalize = true;

	private HeuristicSettings2048 heuristicSettings2048;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;


	public ParMCTSE() {
		heuristicSettings2048 = new HeuristicSettings2048();
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
		setMaxNodes(tp.getMaxNodes());						
		setNumAgents(tp.getNumAgents());					
		setVerbosity(tp.getVerbosity());
		setAlternateVersion(tp.getAlternateVersion());	
		setEnableHeuristics(tp.getEnableHeuristics());
		setHeuristicSettings2048(tp.getHeuristicSettings2048());
		this.useNormalize = tp.getNormalize();
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
		setMaxNodes(tp.getMaxNodes());						
		setNumAgents(tp.getNumAgents());					
		setVerbosity(tp.getVerbosity());
		setAlternateVersion(tp.getAlternateVersion());	
		setEnableHeuristics(tp.getEnableHeuristics());
		setHeuristicSettings2048(tp.getHeuristicSettings2048());
		this.useNormalize = tp.getNormalize();
	}
	
	public int getNumIter() {
		return this.numIters;
	}
	public double getK_UCT() {
		return this.kUCT;
	}
	public boolean getNormalize() {
		return useNormalize;
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
	public void setNumIter(int value) {
		numIters = value;
	}
	public void setK_UCT(double value) {
		kUCT = value;
	}
	public void setTreeDepth(int value) {
		treeDepth = value;
	}
	public void setRolloutDepth(int value) {
		rolloutDepth = value;
	}
	public void setMaxNodes(int value) {
		maxNodes = value;
	}
	public void setNumAgents(int value) { 
		numAgents = value; 
	}
	public void setAlternateVersion(boolean value) {
		alternateVersion = value;
	}
	public void setEnableHeuristics(boolean value) {
		enableHeuristics = value;
	}
	public void setHeuristicSettings2048(HeuristicSettings2048 heuristicSettings2048) {
		this.heuristicSettings2048 = heuristicSettings2048;
	}
	public void setVerbosity(int verbosity) {
		this.verbosity = verbosity;
	}
	
}