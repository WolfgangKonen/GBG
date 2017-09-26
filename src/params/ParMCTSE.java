package params;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.MCTSExpectimax.MCTSEChanceNode;
import controllers.MCTSExpectimax.MCTSExpectimaxConfig;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * MCTS (Monte Carlo Tree Search) parameters for board games.<p>
 *  
 * These parameters and their [defaults] are: <ul>
 * <li> <b>Iterations</b>: 	[1000] number of iterations during MCTS search 
 * <li> <b>K (UCT)</b>: 	[1.414] parameter K in UCT rule  
 * <li> <b>Tree Depth</b>: 	[ 10] MCTS tree depth 
 * <li> <b>Rollout Depth</b>[200] MCTS rollout depth  
 * </ul>
 * The defaults are defined in {@link SingleMCTSPlayer}. 
 * 
 * @see MCTSAgentT
 * @see SingleMCTSPlayer
 */
public class ParMCTSE implements Serializable
{
//	private JLabel numIter_L;
//	private JLabel kUCT_L;
//	private JLabel treedep_L;
//	private JLabel rollout_L;
//	private JLabel maxNodes_L;
//	private JLabel numAgents_L;
//	private JTextField numIter_T;
//	private JTextField kUCT_T;
//	private JTextField treedep_T;
//	private JTextField rollout_T;
//	private JTextField maxNodes_T;
//	private JTextField numAgents_T;
//	private JCheckBox alternativeVersion_CB;
//	private JCheckBox enableHeuristics_CB;
//	private JPanel mPanel;
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

//		numIter_L = new JLabel("Iterations");
//		kUCT_L = new JLabel("K (UCT)");
//		treedep_L = new JLabel("Tree Depth");
//		rollout_L = new JLabel("Rollout Depth");
//		maxNodes_L = new JLabel("Max Nodes");
//		numAgents_L = new JLabel("Number Agents");
//		alternativeVersion_CB = new JCheckBox("alternative Version (~4% faster)", MCTSExpectimaxConfig.DEFAULT_ALTERNATIVEVERSION);
//		enableHeuristics_CB = new JCheckBox("enable Heuristics", MCTSExpectimaxConfig.DEFAULT_ENABLEHEURISTICS);
//		numIter_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_ITERATIONS+"");
//		kUCT_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_K+"");					//
//		treedep_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_TREEDEPTH+"");
//		rollout_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_ROLLOUTDEPTH+"");
//		maxNodes_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_MAXNODES+"");
//		numAgents_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_NUMAGENTS+ "");
		
//		numIter_L.setToolTipText("Number of iterations during MCTS search");
//		kUCT_L.setToolTipText("Parameter K in UCT rule ");
//		treedep_L.setToolTipText("MCTS tree depth");
//		rollout_L.setToolTipText("MCTS rollout depth");
//		maxNodes_L.setToolTipText("Max number of tree nodes");
//		numAgents_L.setToolTipText("Number of agents for majority Vote");

	}
	
    public ParMCTSE(MCTSExpectimaxParams tp) { 
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