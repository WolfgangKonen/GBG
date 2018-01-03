package params;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.MCTSExpectimax.MCTSEChanceNode;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.MCTSExpectimax.MCTSExpectimaxConfig;
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
 * The defaults are defined in {@link MCTSExpectimaxConfig}. 
 * 
 * @see MCTSExpectimaxAgt
 * @see MCTSExpectimaxConfig
 */
public class ParMCTSE implements Serializable
{
    private int numIters = MCTSExpectimaxConfig.DEFAULT_ITERATIONS;
	private int rolloutDepth = MCTSExpectimaxConfig.DEFAULT_ROLLOUTDEPTH;
    private int treeDepth = MCTSExpectimaxConfig.DEFAULT_TREEDEPTH;
    private double kUCT = MCTSExpectimaxConfig.DEFAULT_K;
    private int maxNodes = MCTSExpectimaxConfig.DEFAULT_MAXNODES; 
    private int numAgents = MCTSExpectimaxConfig.DEFAULT_NUMAGENTS;
    private boolean alternativeVersion = MCTSExpectimaxConfig.DEFAULT_ALTERNATIVEVERSION;
    private boolean enableHeuristics = MCTSExpectimaxConfig.DEFAULT_ENABLEHEURISTICS;

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
		setMaxNodes(tp.getMaxNodes());						// /WK/ this was missing
		setNumAgents(tp.getNumAgents());					// /WK/ this was missing
		setAlternativeVersion(tp.getAlternativeVersion());	// /WK/ this was missing
		setEnableHeuristics(tp.getEnableHeuristics());
		setHeuristicSettings2048(tp.getHeuristicSettings2048());
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
		setMaxNodes(tp.getMaxNodes());						// /WK/ this was missing
		setNumAgents(tp.getNumAgents());					// /WK/ this was missing
		setAlternativeVersion(tp.getAlternativeVersion());	// /WK/ this was missing
		setEnableHeuristics(tp.getEnableHeuristics());
		setHeuristicSettings2048(tp.getHeuristicSettings2048());
	}
	
	public int getNumIter() {
		return this.numIters;
	}
	public double getK_UCT() {
		return this.kUCT;
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
	public boolean getAlternativeVersion() {
		return this.alternativeVersion;
	}
	public boolean getEnableHeuristics() {
		return this.enableHeuristics;
	}
	public HeuristicSettings2048 getHeuristicSettings2048() {
		return this.heuristicSettings2048;
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
	public void setAlternativeVersion(boolean value) {
		alternativeVersion = value;
	}
	public void setEnableHeuristics(boolean value) {
		enableHeuristics = value;
	}
	public void setHeuristicSettings2048(HeuristicSettings2048 heuristicSettings2048) {
		this.heuristicSettings2048 = heuristicSettings2048;
	}
	
}