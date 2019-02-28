package params;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.MCTSExpectimax.MCTSEChanceNode;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * The defaults are defined in {@link ParMCTSE}. 
 * 
 * @see MCTSExpectimaxAgt
 * @see ParMCTSE
 */
public class MCTSExpectimaxParams extends Frame implements Serializable
{
	private static final String TIPNUMITERL = "Number of iterations during MCTSE search";
	private static final String TIPKUCTL = "Parameter K in UCT rule ";
	private static final String TIPEPSILONGREEDY = "epsilon in eps-greedy node selection";
	private static final String TIPTREEDEPL = "maximum tree depth";
	private static final String TIPROLLOUTL = "maximum rollout depth (random moves from a leaf)";
	private static final String TIPNORMALIZEL = "<html>Normalize rollout value q(reward) to range [0,1]<br>"
			+ "(special online normalization for 2048)</html>";
	private static final String TIPSELECTORL = "Which selector to use in tree policy";
	private static final String TIPVERBOSET = "<html>0: print nothing,<br>"
			+ "1: one line per MCTS call, <br>"
			+ "2: for each child (=action) one line, <br>"
			+ "&ge; 3: more levels of children, grandchildren, ..."
			+ "</html>";
	// use "<html> ... <br> ... </html>" to get multi-line tooltip text

	private static String[] selTypeString = { "UCT","eps-greedy","roulette wheel" };

	private JLabel numIter_L;
	JLabel selector_L;
	private JLabel kUCT_L;
	private JLabel epsGreedy_L;
	private JLabel normalize_L;
	private JLabel treedep_L;
	private JLabel rollout_L;
	private JLabel maxNodes_L;
	private JLabel numAgents_L;
	private JLabel verbose_L;
	private JTextField numIter_T;
	private JTextField kUCT_T;
	private JTextField epsGreedy_T;
	private JCheckBox normalize;
	private JTextField treedep_T;
	private JTextField rollout_T;
	private JTextField maxNodes_T;
	private JTextField numAgents_T;
	private JTextField verbose_T;
	private JCheckBox alternateVersion_CB;
	private JCheckBox enableHeuristics_CB;
	private JComboBox choiceSelector;
	private JPanel mPanel;

	private HeuristicSettings2048 heuristicSettings2048;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;


	public MCTSExpectimaxParams() {
		super("MCTS Expectimax Parameter");

		heuristicSettings2048 = new HeuristicSettings2048();

		numIter_L = new JLabel("Iterations");
		treedep_L = new JLabel("Tree Depth");
		rollout_L = new JLabel("Rollout Depth");
		verbose_L = new JLabel("Verbosity");
		selector_L = new JLabel("Selector: ");
		kUCT_L = new JLabel("K (UCT)");
		epsGreedy_L = new JLabel("epsilon (greedy)");
		normalize_L = new JLabel("Normalize: ");
		choiceSelector = new JComboBox(selTypeString);
		choiceSelector.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				enableUCTPart();
			}
		});
		maxNodes_L = new JLabel("Max Nodes");
		numAgents_L = new JLabel("Number Agents");
		alternateVersion_CB = new JCheckBox("alternate Version (~4% faster)", ParMCTSE.DEFAULT_ALTERNATEVERSION);
		enableHeuristics_CB = new JCheckBox("enable Heuristics", ParMCTSE.DEFAULT_ENABLEHEURISTICS);
		numIter_T = new JTextField(ParMCTSE.DEFAULT_ITERATIONS+"");
		kUCT_T = new JTextField(ParMCTSE.DEFAULT_K+"");					//
		epsGreedy_T = new JTextField(ParMCTSE.DEFAULT_EPSILONGREEDY+"");					// 
		treedep_T = new JTextField(ParMCTSE.DEFAULT_TREEDEPTH+"");
		rollout_T = new JTextField(ParMCTSE.DEFAULT_ROLLOUTDEPTH+"");
		maxNodes_T = new JTextField(ParMCTSE.DEFAULT_MAXNODES+"");
		numAgents_T = new JTextField(ParMCTSE.DEFAULT_NUMAGENTS+ "");
		verbose_T = new JTextField(ParMCTSE.DEFAULT_VERBOSITY+"");		 
		normalize = new JCheckBox();
		mPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class XArenaTabs)
		
		numIter_L.setToolTipText(TIPNUMITERL);
		treedep_L.setToolTipText(TIPTREEDEPL);
		rollout_L.setToolTipText(TIPROLLOUTL);
		verbose_L.setToolTipText(TIPVERBOSET);
		verbose_T.setToolTipText(TIPVERBOSET);
		selector_L.setToolTipText(TIPSELECTORL);
		kUCT_L.setToolTipText(TIPKUCTL);
		epsGreedy_L.setToolTipText(TIPEPSILONGREEDY);
		normalize_L.setToolTipText(TIPNORMALIZEL);
		maxNodes_L.setToolTipText("Max number of tree nodes");
		numAgents_L.setToolTipText("Number of agents for majority Vote");


		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		mPanel.setLayout(new GridLayout(0,2,10,10));

		JPanel iterPanel = new JPanel(new GridLayout(0,2,10,10));
		iterPanel.add(numIter_L);
		iterPanel.add(numIter_T);
		mPanel.add(iterPanel);

		JPanel sePanel = new JPanel(new GridLayout(0,2,10,10));
		sePanel.add(selector_L);
		sePanel.add(choiceSelector);
		mPanel.add(sePanel);

		JPanel tdPanel = new JPanel(new GridLayout(0,2,10,10));
		tdPanel.add(treedep_L);
		tdPanel.add(treedep_T);
		mPanel.add(tdPanel);

		JPanel kPanel = new JPanel(new GridLayout(0,2,10,10));
		kPanel.add(kUCT_L);
		kPanel.add(kUCT_T);
		mPanel.add(kPanel);

		JPanel rdPanel = new JPanel(new GridLayout(0,2,10,10));
		rdPanel.add(rollout_L);
		rdPanel.add(rollout_T);
		mPanel.add(rdPanel);

		JPanel epPanel = new JPanel(new GridLayout(0,2,10,10));
		epPanel.add(epsGreedy_L);
		epPanel.add(epsGreedy_T);
		mPanel.add(epPanel);

		JPanel vePanel = new JPanel(new GridLayout(0,2,10,10));
		vePanel.add(verbose_L);
		vePanel.add(verbose_T);
		mPanel.add(vePanel);

		JPanel noPanel = new JPanel(new GridLayout(0,2,10,10));
		noPanel.add(normalize_L);
		noPanel.add(normalize);
		mPanel.add(noPanel);

		JPanel mnPanel = new JPanel(new GridLayout(0,2,10,10));
		mnPanel.add(maxNodes_L);
		mnPanel.add(maxNodes_T);
		mPanel.add(mnPanel);

		JPanel naPanel = new JPanel(new GridLayout(0,2,10,10));
		naPanel.add(numAgents_L);
		naPanel.add(numAgents_T);
		mPanel.add(naPanel);

//		mPanel.add(new Canvas());
		
		mPanel.add(alternateVersion_CB);
		mPanel.add(enableHeuristics_CB);	

		add(mPanel,BorderLayout.CENTER);
				
		enableUCTPart();
		
		pack();
		setVisible(false);
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
	
	public JPanel getPanel() {
		return mPanel;
	}
	public int getNumIter() {
		return Integer.valueOf(numIter_T.getText());
	}
	/**
	 * @return 0: "UCT", 1: "eps-greedy", 2: "roulette wheel" 
	 */
	public int getSelectMode() {
		return this.choiceSelector.getSelectedIndex();
	}
	public double getK_UCT() {
		return Double.valueOf(kUCT_T.getText());
	}
	public boolean getNormalize() {
		return normalize.isSelected();
	}
	public double getEpsGreedy() {
		return Double.valueOf(epsGreedy_T.getText()).doubleValue();	
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
	public int getNumAgents()  {
		return Integer.valueOf(numAgents_T.getText());
	}
	public int getVerbosity() {
		return Integer.valueOf(verbose_T.getText()).intValue();
	}
	public boolean getAlternateVersion() {
		return alternateVersion_CB.isSelected();
	}
	public boolean getEnableHeuristics() {
		return enableHeuristics_CB.isSelected();
	}
	public HeuristicSettings2048 getHeuristicSettings2048() {
		return heuristicSettings2048;
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
	public void setMaxNodes(int value) {
		maxNodes_T.setText(value+"");
	}
	public void setNumAgents(int value) { 
		numAgents_T.setText(value+""); 
	}
	public void setAlternateVersion(boolean value) {
		alternateVersion_CB.setSelected(value);
	}
	public void setEnableHeuristics(boolean value) {
		enableHeuristics_CB.setSelected(value);
	}
	public void setHeuristicSettings2048(HeuristicSettings2048 heuristicSettings2048) {
		this.heuristicSettings2048 = heuristicSettings2048;
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(MCTSExpectimaxParams tp) {
		setK_UCT(tp.getK_UCT());
		setEpsGreedy(tp.getEpsGreedy());
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setVerbosity(tp.getVerbosity());
		setNormalize(tp.getNormalize());
		setMaxNodes(tp.getMaxNodes());						
		setNumAgents(tp.getNumAgents());					
		setAlternateVersion(tp.getAlternateVersion());	
		setEnableHeuristics(tp.getEnableHeuristics());
		setSelectMode(tp.getSelectMode());
		enableUCTPart();
	}
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  of the re-loaded agent
	 */
	public void setFrom(ParMCTSE tp) {
		setK_UCT(tp.getK_UCT());
		setEpsGreedy(tp.getEpsGreedy());
		setNumIter(tp.getNumIter());
		setRolloutDepth(tp.getRolloutDepth());
		setTreeDepth(tp.getTreeDepth());
		setVerbosity(tp.getVerbosity());
		setNormalize(tp.getNormalize());
		setMaxNodes(tp.getMaxNodes());					
		setNumAgents(tp.getNumAgents());					
		setAlternateVersion(tp.getAlternateVersion());	
		setEnableHeuristics(tp.getEnableHeuristics());
		setSelectMode(tp.getSelectMode());
		enableUCTPart();
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName currently only "MCTSE" 
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 * @param numPlayers
	 */
	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
		switch (agentName) {
		case "MCTS Expectimax": 
			switch (gameName) {
			case "Nim": 
				numIter_T.setText("10000");		
				kUCT_T.setText("1.414");
				break;
			}
			this.setAlternateVersion(false);
			switch (numPlayers) {
			case 1: 
				normalize.setSelected(true);
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

}