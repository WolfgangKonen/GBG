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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;

/**
 * MCTS (Monte Carlo Tree Search) parameters for board games.<p>
 *  
 * These parameters and their [defaults] are: <ul>
 * <li> <b>Iterations</b>: 	[1000] number of iterations during MCTS search 
 * <li> <b>K (UCT)</b>: 	[1.414] parameter K in UCT rule  
 * <li> <b>Tree Depth</b>: 	[ 10] MCTS tree depth 
 * <li> <b>Rollout Depth</b>[200] MCTS rollout depth  
 * </ul>
 * The defaults are defined in {@link ParMCTS}. 
 * 
 * @see MCTSAgentT
 * @see SingleMCTSPlayer
 * @see ParMCTS
 */
public class MCTSParams extends Frame implements Serializable
{
	private static final String TIPNUMITERL = "Number of iterations during MCTS search";
	private static final String TIPKUCTL = "Parameter K in UCT rule ";
	private static final String TIPEPSILONGREEDY = "epsilon in eps-greedy node selection";
	private static final String TIPTREEDEPL = "maximum tree depth";
	private static final String TIPROLLOUTL = "maximum rollout depth (random moves from a leaf)";
	private static final String TIPNORMALIZEL = "Normalize rollout value q(reward) to range [0,1]";
	private static final String TIPSELECTORL = "Which selector to use in tree policy";
	private static final String TIPVERBOSET = "<html>0: print nothing,<br>"
			+ "1: one line per MCTS call, <br>"
			+ "2: for each child (=action) one line, <br>"
			+ "&ge; 3: more levels of children, grandchildren, ..."
			+ "</html>";
	// use "<html> ... <br> ... </html>" to get multi-line tooltip text
	
	private static String[] selTypeString = { "UCT","eps-greedy","roulette wheel" };
	
	JLabel numIter_L;
	JLabel selector_L;
	JLabel kUCT_L;
	JLabel epsGreedy_L;
	JLabel treedep_L;
	JLabel rollout_L;
	JLabel verbose_L;
	JLabel normalize_L;
	JTextField numIter_T;
	JTextField kUCT_T;
	JTextField epsGreedy_T;
	JTextField treedep_T;
	JTextField rollout_T;
	JTextField verbose_T;
	JCheckBox normalize;
	JComboBox choiceSelector;
	JPanel mPanel;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public MCTSParams() {
		super("MCTS Parameter");
		numIter_L = new JLabel("Iterations");
		treedep_L = new JLabel("Tree Depth");
		rollout_L = new JLabel("Rollout Depth");
		verbose_L = new JLabel("Verbosity");
		selector_L = new JLabel("Selector: ");
		kUCT_L = new JLabel("K (UCT)");
		epsGreedy_L = new JLabel("epsilon (greedy)");
		normalize_L = new JLabel("Normalize: ");
		numIter_T = new JTextField(ParMCTS.DEFAULT_NUM_ITERS+"");			
		treedep_T = new JTextField(ParMCTS.DEFAULT_TREE_DEPTH+"");		 
		rollout_T = new JTextField(ParMCTS.DEFAULT_ROLLOUT_DEPTH+"");		 
		verbose_T = new JTextField(ParMCTS.DEFAULT_VERBOSITY+"");		 
		kUCT_T = new JTextField(ParMCTS.DEFAULT_K+"");					// 
		epsGreedy_T = new JTextField(ParMCTS.DEFAULT_EPSILONGREEDY+"");					// 
		normalize = new JCheckBox();
		choiceSelector = new JComboBox(selTypeString);
		choiceSelector.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				enableUCTPart();
			}
		});

		mPanel = new JPanel();		// put the inner buttons into panel mPanel. This panel
									// can be handed over to a tab of a JTabbedPane object
									// (see class XArenaTabs)
		
		numIter_L.setToolTipText(TIPNUMITERL);
		treedep_L.setToolTipText(TIPTREEDEPL);
		rollout_L.setToolTipText(TIPROLLOUTL);
		verbose_L.setToolTipText(TIPVERBOSET);
		verbose_T.setToolTipText(TIPVERBOSET);
		kUCT_L.setToolTipText(TIPKUCTL);
		epsGreedy_L.setToolTipText(TIPEPSILONGREEDY);
		normalize_L.setToolTipText(TIPNORMALIZEL);
		selector_L.setToolTipText(TIPSELECTORL);
		
		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		mPanel.setLayout(new GridLayout(0,4,10,10));		
		
		mPanel.add(numIter_L);
		mPanel.add(numIter_T);
		mPanel.add(selector_L);
		mPanel.add(choiceSelector);
		
		mPanel.add(treedep_L);
		mPanel.add(treedep_T);
		mPanel.add(kUCT_L);
		mPanel.add(kUCT_T);
		
		mPanel.add(rollout_L);
		mPanel.add(rollout_T);
		mPanel.add(epsGreedy_L);
		mPanel.add(epsGreedy_T);

		mPanel.add(verbose_L);			
		mPanel.add(verbose_T);
		mPanel.add(normalize_L);
		mPanel.add(normalize);

//		mPanel.add(new Canvas());
//		mPanel.add(new Canvas());

		mPanel.add(new Canvas());	// add empty row to balance height of fields
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		add(mPanel,BorderLayout.CENTER);
		
		enableUCTPart();
				
		pack();
		setVisible(false);
	} // constructor MCTSParams()	
	
	public JPanel getPanel() {
		return mPanel;
	}
	
	private void enableUCTPart() {
		String selString = (String) choiceSelector.getSelectedItem();
		boolean selUCT = selString.equals(selTypeString[0]); 		// "UCT"
		this.kUCT_L.setEnabled(selUCT);
		this.kUCT_T.setEnabled(selUCT);
		boolean selEpsGreedy = selString.equals(selTypeString[1]); 	// "eps-greedy"
		this.epsGreedy_L.setEnabled(selEpsGreedy);
		this.epsGreedy_T.setEnabled(selEpsGreedy);
	}
	

	public int getNumIter() {
		return Integer.valueOf(numIter_T.getText()).intValue();
	}
	/**
	 * @return 0: "UCT", 1: "eps-greedy", 2: "roulette wheel" 
	 */
	public int getSelectMode() {
		return this.choiceSelector.getSelectedIndex();
	}
	public double getK_UCT() {
		//return Double.valueOf(kUCT_T.getText()).intValue();			// BUG: always 1.0 (when real k is 1.3, 1.4, ... )!!!
		return Double.valueOf(kUCT_T.getText()).doubleValue();	
	}
	public double getEpsGreedy() {
		return Double.valueOf(epsGreedy_T.getText()).doubleValue();	
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
	public boolean getNormalize() {
		return normalize.isSelected();
	}
	public void setNumIter(int value) {
		numIter_T.setText(value+"");
	}
	public void setSelectMode(int value) {
		this.choiceSelector.setSelectedIndex(value);
	}
	public void setK_UCT(double value) {
		kUCT_T.setText(value+"");
	}
	public void setEpsGreedy(double value) {
		epsGreedy_T.setText(value+"");
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
	public void setNormalize(boolean state) {
		normalize.setSelected(state);
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(MCTSParams tp) {
		setK_UCT(tp.getK_UCT());
		setEpsGreedy(tp.getEpsGreedy());
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setVerbosity(tp.getVerbosity());
		setNormalize(tp.getNormalize());
		setSelectMode(tp.getSelectMode());
//		System.out.println("numIter= "+tp.getNumIter());
//		System.out.println("k_UCT= "+tp.getK_UCT());
		enableUCTPart();
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(ParMCTS tp) {
		setK_UCT(tp.getK_UCT());
		setEpsGreedy(tp.getEpsGreedy());
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setVerbosity(tp.getVerbosity());
		setNormalize(tp.getNormalize());
		setSelectMode(tp.getSelectMode());
		enableUCTPart();
	}
	
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
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
				numIter_T.setText("10000");		
				kUCT_T.setText("1.414");
				break;
			}
			switch (numPlayers) {
			case 1: 
				normalize.setSelected(false);
				break;
			default:
				normalize.setSelected(true);
				break;
			}
			break;
		}
		choiceSelector.setEnabled(true);
		enableUCTPart();		
	}	

} // class MCTSParams
