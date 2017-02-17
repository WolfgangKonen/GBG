package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Panel;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;

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
public class MCTSParams extends Frame implements Serializable
{
	private static final long serialVersionUID = 1L;
	JLabel numIter_L;
	JLabel kUCT_L;
	JLabel treedep_L;
	JLabel rollout_L;
	JLabel verbose_L;
	public JTextField numIter_T;
	public JTextField kUCT_T;
	public JTextField treedep_T;
	public JTextField rollout_T;
	public JTextField verbose_T;
	JPanel mPanel;

//	Button ok;
//	MCTSParams m_par;
	
	public MCTSParams() {
		super("MCTS Parameter");
		numIter_L = new JLabel("Iterations");
		kUCT_L = new JLabel("K (UCT)");
		treedep_L = new JLabel("Tree Depth");
		rollout_L = new JLabel("Rollout Depth");
		verbose_L = new JLabel("Verbosity");
		numIter_T = new JTextField(SingleMCTSPlayer.DEFAULT_NUM_ITERS+"");			
		kUCT_T = new JTextField(SingleMCTSPlayer.DEFAULT_K+"");					// 
		treedep_T = new JTextField(SingleMCTSPlayer.DEFAULT_TREE_DEPTH+"");		 
		rollout_T = new JTextField(SingleMCTSPlayer.DEFAULT_ROLLOUT_DEPTH+"");		 
		verbose_T = new JTextField(SingleMCTSPlayer.DEFAULT_VERBOSITY+"");		 
//		ok = new Button("OK");
//		m_par = this;
		mPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class TicTacToeTabs)
		
		numIter_L.setToolTipText("Number of iterations during MCTS search");
		kUCT_L.setToolTipText("Parameter K in UCT rule ");
		treedep_L.setToolTipText("MCTS tree depth");
		rollout_L.setToolTipText("MCTS rollout depth");
		verbose_L.setToolTipText("0: print nothing, 1: one line per MCTS call, 2: for each action one line");
		
//		ok.addActionListener(
//				new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						m_par.setVisible(false);
//					}
//				}					
//		);

//		this.choiceBatch = new Choice();
//		for (int i=1; i<=batchMax; i++) choiceBatch.add(i+""); 
//		choiceBatch.select(batchMax+"");

		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		mPanel.setLayout(new GridLayout(0,4,10,10));		
		
		mPanel.add(numIter_L);
		mPanel.add(numIter_T);
		mPanel.add(kUCT_L);
		mPanel.add(kUCT_T);
		
		mPanel.add(treedep_L);
		mPanel.add(treedep_T);
		mPanel.add(rollout_L);
		mPanel.add(rollout_T);
		
		mPanel.add(verbose_L);			
		mPanel.add(verbose_T);
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		mPanel.add(new Canvas());	// add two empty rows to balance height of fields
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		add(mPanel,BorderLayout.CENTER);
		//add(ok,BorderLayout.SOUTH);
				
		pack();
		setVisible(false);
	} // constructor MCTSParams()	
	
	public JPanel getPanel() {
		return mPanel;
	}
	public int getNumIter() {
		return Integer.valueOf(numIter_T.getText()).intValue();
	}
	public double getK_UCT() {
		return Double.valueOf(kUCT_T.getText()).intValue();
	}
	public int getTreeDepth() {
		return Integer.valueOf(treedep_T.getText()).intValue();
	}
	public int getRolloutDepth() {
		return Integer.valueOf(rollout_T.getText()).intValue();
	}
	public int getVerbosity() {
		return Integer.valueOf(verbose_T.getText()).intValue();
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
	public void setVerbosity(int value) {
		verbose_T.setText(value+"");
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(MCTSParams tp) {
		setK_UCT(tp.getK_UCT());
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setVerbosity(tp.getVerbosity());
	}
} // class MCTSParams
