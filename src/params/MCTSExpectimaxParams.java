package params;

import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.io.Serializable;

/**
 * This class realizes the parameter settings (GUI tab) for 
 * MCTSE (Monte Carlo Tree Search Expectimax) agent {@link MCTSExpectimaxAgt}.<p>
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

	private static final String[] selTypeString = { "UCT","eps-greedy","roulette wheel" };

	private final JLabel numIter_L;
	JLabel selector_L;
	private final JLabel kUCT_L;
	private final JLabel epsGreedy_L;
	private final JLabel treedep_L;
	private final JLabel rollout_L;
	private final JLabel maxNodes_L;
	private final JLabel numAgents_L;
	private final JLabel verbose_L;
	private final JTextField numIter_T;
	private final JTextField kUCT_T;
	private final JTextField epsGreedy_T;
	private final JCheckBox normalize;
	private final JTextField treedep_T;
	private final JTextField rollout_T;
	private final JTextField maxNodes_T;
	private final JTextField numAgents_T;
	private final JTextField verbose_T;
	private final JCheckBox alternateVersion_CB;
	private final JCheckBox enableHeuristics_CB;
	private final JComboBox<String> choiceSelector;
	private final JPanel mPanel;
	public JCheckBox CBStopOnRoundOver;

	private HeuristicSettings2048 heuristicSettings2048;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
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
		JLabel normalize_L = new JLabel("Normalize: ");
		choiceSelector = new JComboBox<>(selTypeString);
		choiceSelector.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				enableUCTPart();
			}
		});
		choiceSelector.setEnabled(true);

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
		CBStopOnRoundOver = new JCheckBox("StopOnRoundOver", ParMCTSE.DEFAULT_STOPONROUNDOVER);
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

		mPanel.add(CBStopOnRoundOver);
		mPanel.add(new Canvas());

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
		return Integer.parseInt(numIter_T.getText());
	}
	/**
	 * @return 0: "UCT", 1: "eps-greedy", 2: "roulette wheel" 
	 */
	public int getSelectMode() {
		return this.choiceSelector.getSelectedIndex();
	}
	public double getK_UCT() {
		return Double.parseDouble(kUCT_T.getText());
	}
	public boolean getNormalize() {
		return normalize.isSelected();
	}
	public double getEpsGreedy() {
		return Double.parseDouble(epsGreedy_T.getText());
	}
	public int getTreeDepth() {
		return Integer.parseInt(treedep_T.getText());
	}
	public int getRolloutDepth() {
		return Integer.parseInt(rollout_T.getText());
	}
	public int getMaxNodes() {
		return Integer.parseInt(maxNodes_T.getText());
	}
	public int getNumAgents()  {
		return Integer.parseInt(numAgents_T.getText());
	}
	public int getVerbosity() {
		return Integer.parseInt(verbose_T.getText());
	}
	public boolean getAlternateVersion() {
		return alternateVersion_CB.isSelected();
	}
	public boolean getEnableHeuristics() {
		return enableHeuristics_CB.isSelected();
	}
	public boolean getStopOnRoundOver() {
		return CBStopOnRoundOver.isSelected();
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
	public void setStopOnRoundOver(boolean value) {
		CBStopOnRoundOver.setSelected(value);
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
		setStopOnRoundOver(tp.getStopOnRoundOver());

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
		setStopOnRoundOver(tp.getStopOnRoundOver());

		enableUCTPart();
	}

//	/**
//	 * Set sensible parameters for a specific agent and specific game. By "sensible
//	 * parameters" we mean parameter producing good results. Likewise, some parameter
//	 * choices may be enabled or disabled.
//	 * 
//	 * @param agentName currently only "MCTSE" 
//	 * @param gameName the string from {@link games.StateObservation#getName()}
//	 * @param numPlayers
//	 */
//	@Deprecated
//	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
//		switch (agentName) {
//		case "MCTS Expectimax": 
//			switch (gameName) {
//			case "Nim": 
//				numIter_T.setText("10000");		
//				kUCT_T.setText("1.414");
//				break;
//			}
//			this.setAlternateVersion(false);
//			switch (numPlayers) {
//			case 1: 
//				normalize.setSelected(true);
//				break;
//			default:
//				normalize.setSelected(true);
//				break;
//			}
//			break;
//		}
//		choiceSelector.setEnabled(true);
//		enableUCTPart();		
//	}	

}