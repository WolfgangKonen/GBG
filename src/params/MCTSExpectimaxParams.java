package params;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.MCTSExpectimax.MCTSExpectimaxConfig;

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
public class MCTSExpectimaxParams extends Frame implements Serializable
{
	JLabel numIter_L;
	JLabel kUCT_L;
	JLabel treedep_L;
	JLabel rollout_L;
	JLabel maxNodes_L;
	private JTextField numIter_T;
	private JTextField kUCT_T;
	private JTextField treedep_T;
	private JTextField rollout_T;
	private JTextField maxNodes_T;
	private JCheckBox alternativeVersion_CB;
	JPanel mPanel;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;


	public MCTSExpectimaxParams() {
		super("MCTS Parameter");
		numIter_L = new JLabel("Iterations");
		kUCT_L = new JLabel("K (UCT)");
		treedep_L = new JLabel("Tree Depth");
		rollout_L = new JLabel("Rollout Depth");
		maxNodes_L = new JLabel("Max Nodes");
		alternativeVersion_CB = new JCheckBox("alternative Version (~4% faster)", MCTSExpectimaxConfig.DEFAULT_ALTERNATIVEVERSION);
		numIter_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_ITERATIONS+"");
		kUCT_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_K+"");					//
		treedep_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_TREEDEPTH+"");
		rollout_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_ROLLOUTDEPTH+"");
		maxNodes_T = new JTextField(MCTSExpectimaxConfig.DEFAULT_MAXNODES+"");
		mPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class TicTacToeTabs)
		
		numIter_L.setToolTipText("Number of iterations during MCTS search");
		kUCT_L.setToolTipText("Parameter K in UCT rule ");
		treedep_L.setToolTipText("MCTS tree depth");
		rollout_L.setToolTipText("MCTS rollout depth");
		maxNodes_L.setToolTipText("Max number of tree nodes");


		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		mPanel.setLayout(new GridLayout(0,2,10,10));

		JPanel iterPanel = new JPanel(new GridLayout(0,2,10,10));
		iterPanel.add(numIter_L);
		iterPanel.add(numIter_T);
		mPanel.add(iterPanel);

		JPanel kPanel = new JPanel(new GridLayout(0,2,10,10));
		kPanel.add(kUCT_L);
		kPanel.add(kUCT_T);
		mPanel.add(kPanel);

		JPanel tdPanel = new JPanel(new GridLayout(0,2,10,10));
		tdPanel.add(treedep_L);
		tdPanel.add(treedep_T);
		mPanel.add(tdPanel);

		JPanel rdPanel = new JPanel(new GridLayout(0,2,10,10));
		rdPanel.add(rollout_L);
		rdPanel.add(rollout_T);
		mPanel.add(rdPanel);

		JPanel mnPanel = new JPanel(new GridLayout(0,2,10,10));
		mnPanel.add(maxNodes_L);
		mnPanel.add(maxNodes_T);
		mPanel.add(mnPanel);

		mPanel.add(alternativeVersion_CB);

		mPanel.add(new Canvas());	// add two empty rows to balance height of fields
		mPanel.add(new Canvas());

		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		add(mPanel,BorderLayout.CENTER);
		//add(ok,BorderLayout.SOUTH);
				
		pack();
		setVisible(false);
	}
	
	public JPanel getPanel() {
		return mPanel;
	}
	public int getNumIter() {
		return Integer.valueOf(numIter_T.getText());
	}
	public double getK_UCT() {
		return Double.valueOf(kUCT_T.getText());
	}
	public int getTreeDepth() {
		return Integer.valueOf(treedep_T.getText());
	}
	public int getRolloutDepth() {
		return Integer.valueOf(rollout_T.getText());
	}
	public int getMaxNodes() {
		return Integer.valueOf(maxNodes_T.getText());
	}
	public boolean getAlternativeVersion() {
		return alternativeVersion_CB.isSelected();
	}
	public void setNumIter(int value) {
		numIter_T.setText(value+"");
	}
	public void setK_UCT(double value) {
		kUCT_T.setText(value+"");
	}
	public void setTreeDepth(int value) {
		treedep_T.setText(value+"");
	}
	public void setRolloutDepth(int value) {
		rollout_T.setText(value+"");
	}
	public void setMaxNodes(int value) {
		maxNodes_T.setText(value+"");
	}
	public void setAlternativeVersion(boolean value) {
		alternativeVersion_CB.setSelected(value);
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(MCTSExpectimaxParams tp) {
		setK_UCT(tp.getK_UCT());
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setMaxNodes(tp.getMaxNodes());						// /WK/ this was missing
		setAlternativeVersion(tp.getAlternativeVersion());	// /WK/ this was missing
	}
}