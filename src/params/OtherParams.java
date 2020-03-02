package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Feature;

/**
 * This class realizes other parameter settings for board games. Most parameter
 * are only relevant for trainable agents (like {@link TDAgent}, {@link TDNTuple3Agt}), 
 * but <b>Quick Eval Mode</b> and <b>Wrapper nPly</b> are relevant for all agents
 * <p>
 * These parameters and their [defaults] are:
 * <ul>
 * <li><b>Quick Eval Mode</b>: evaluation mode used during 'Quick Evaluation'
 * <li><b>Train Eval Mode</b>: additional evaluation mode used during training
 * <li><b>numEval</b>: [100] During training: Call the evaluators every NumEval
 * episodes
 * <li><b>stopTest</b>: [ 0] whether to perform the stop test during training.
 * If {@literal> 0}, then m_evaluator2 is checked during training whether its
 * goal is reached
 * <li><b>stopEval</b>: [100] During training: How many successful evaluator
 * calls are needed to stop training prematurely?
 * <li><b>Wrapper nPly</b>: [0] if &gt; 0, wrap the agent in an (Expecti)Max-N wrapper with
 * n plies of look-ahead. CAUTION: n &gt; 5 can dramatically slow down computation. 
 * <li><b>Choose Start 01</b>: [false] During training: Whether to start always from default
 * start state ({@code false}, or to start 50% from default, 50% from a random
 * 1-ply state ({@code true}
 * <li><b>Learn from RM</b>: [false] whether to learn from random moves or not
 * </ul>
 * 
 * @see TDAgent
 * @see TDNTuple3Agt
 * @see games.XArenaButtons
 */
public class OtherParams extends Frame {
	private static final long serialVersionUID = 1L;

	JLabel evalQ_L;
	public JComboBox choiceEvalQ;
	JLabel evalT_L;
	public JComboBox choiceEvalT;

	JLabel numEval_L;
	JLabel epiLeng_L;
	JLabel stopTest_L;
	JLabel stopEval_L;
	// JLabel batchL;
	JLabel chooseS01L;
	JLabel learnRM_L;
	JLabel rgs_L;
	JLabel wNply_L;
	// JLabel miniDepth_L;
	// JLabel miniUseHm_L;
	public JTextField numEval_T;
	public JTextField epiLeng_T;
	public JTextField stopTest_T;
	public JTextField stopEval_T;
	public JTextField wNply_T;
	public Checkbox chooseS01;
	public Checkbox learnRM;
	public Checkbox rewardIsGameScore;

	Button ok;
	JPanel oPanel;
	OtherParams m_par;

	public OtherParams(/* int batchMax */) {
		super("Other Parameter");

		evalQ_L = new JLabel("Quick Eval Mode");
		this.choiceEvalQ = new JComboBox();
		evalT_L = new JLabel("Train Eval Mode");
		this.choiceEvalT = new JComboBox();

		numEval_T = new JTextField("500"); //
		epiLeng_T = new JTextField("-1"); //
		stopTest_T = new JTextField("0"); //
		stopEval_T = new JTextField("100"); // the defaults
		// miniDepth_T = new JTextField("10"); //
		wNply_T = new JTextField("0"); //
		numEval_L = new JLabel("numEval");
		epiLeng_L = new JLabel("Episode Length");
		stopTest_L = new JLabel("stopTest");
		stopEval_L = new JLabel("stopEval");
		chooseS01L = new JLabel("Choose Start 01");
		learnRM_L = new JLabel("Learn from RM");
		rgs_L = new JLabel("Reward = Score");
		wNply_L = new JLabel("Wrapper nPly");
		chooseS01 = new Checkbox("", false);
		learnRM = new Checkbox("", false);
		rewardIsGameScore = new Checkbox("", true);
		ok = new Button("OK");
		m_par = this;
		oPanel = new JPanel(); // put the inner buttons into panel oPanel. This
								// panel
								// can be handed over to a tab of a JTabbedPane
								// (see class TicTacToeTabs)

		evalQ_L.setToolTipText("'Quick Evaluation' evaluator has this mode");
		evalT_L.setToolTipText(
				"additional evaluator with this mode during training (none if mode is the same as Quick Eval Mode)");
		numEval_L.setToolTipText("During training: Call the evaluators every NumEval episodes");
		epiLeng_L.setToolTipText(
				"During training: Maximum number of moves in an episode. If reached, game terminates prematurely. -1: never terminate.");
		stopTest_L.setToolTipText("During training: If >0 then perform stop test");
		stopEval_L.setToolTipText(
				"During training: How many successfull evaluator calls are needed to stop training prematurely?");
		chooseS01L.setToolTipText("Choose start state in training: 50% default, 50% random 1-ply");
		learnRM_L.setToolTipText("Learn from random moves during training");
		rgs_L.setToolTipText("Use game score as reward (def.) or use some other, game specific reward");
		wNply_L.setToolTipText(
				"Wrapper n-ply look ahead (for play, compete, eval). CAUTION: Numbers >5 can take VERY long!");

		// this.setQuickEvalMode(0);
		// this.setTrainEvalMode(0);

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_par.setVisible(false);
			}
		});

		setLayout(new BorderLayout(10, 0)); // rows,columns,hgap,vgap
		oPanel.setLayout(new GridLayout(0, 4, 10, 10));

		oPanel.add(evalQ_L);
		oPanel.add(choiceEvalQ);
		oPanel.add(evalT_L);
		oPanel.add(choiceEvalT);

		oPanel.add(numEval_L);
		oPanel.add(numEval_T);
		oPanel.add(epiLeng_L);
		oPanel.add(epiLeng_T);

		oPanel.add(stopTest_L);
		oPanel.add(stopTest_T);
		oPanel.add(stopEval_L);
		oPanel.add(stopEval_T);

		oPanel.add(wNply_L);
		oPanel.add(wNply_T);
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		oPanel.add(chooseS01L);
		oPanel.add(chooseS01);
		oPanel.add(learnRM_L);
		oPanel.add(learnRM);

		oPanel.add(rgs_L);
		oPanel.add(rewardIsGameScore);
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		add(oPanel, BorderLayout.CENTER);
		add(ok, BorderLayout.SOUTH);

		pack();
		setVisible(false);

	} // constructor OtherParams()

	public void enableChoosePart(boolean enable) {
		chooseS01.setEnabled(enable);
		chooseS01L.setEnabled(enable);
	}
	
	public JPanel getPanel() {
		return oPanel;
	}

	public int getQuickEvalMode() {
		String s = (String) choiceEvalQ.getSelectedItem();
		if (s == null)
			return 0; // return a dummy, if choiceEvalQ is empty (happens in
						// case of 'new OtherParams()')
		return Integer.valueOf(s).intValue();
	}

	public int getTrainEvalMode() {
		String s = (String) choiceEvalT.getSelectedItem();
		if (s == null)
			return 0; // return a dummy, if choiceEvalT is empty (happens in
						// case of 'new OtherParams()')
		return Integer.valueOf(s).intValue();
	}

	public int getStopTest() {
		return Integer.valueOf(stopTest_T.getText()).intValue();
	}

	public int getStopEval() {
		return Integer.valueOf(stopEval_T.getText()).intValue();
	}

	public int getNumEval() {
		return Integer.valueOf(numEval_T.getText()).intValue();
	}

	public int getWrapperNPly() {
		return Integer.valueOf(wNply_T.getText()).intValue();
	}

	public int getEpisodeLength() {
		int elen = Integer.valueOf(epiLeng_T.getText()).intValue();
		if (elen == -1)	elen = Integer.MAX_VALUE;
		return elen;
	}

	public boolean getChooseStart01() {
		return chooseS01.getState();
	}

	public boolean getLearnFromRM() {
		return learnRM.getState();
	}

	public boolean getRewardIsGameScore() {
		return rewardIsGameScore.getState();
	}

	public void setQuickEvalMode(int qEvalmode) {
		// If the mode list has not been initialized, add the selected mode to
		// the list
		if (choiceEvalQ.getItemCount() == 0) {
			choiceEvalQ.addItem(Integer.toString(qEvalmode));
		}
		choiceEvalQ.setSelectedItem(qEvalmode + "");
	}

	public void setQuickEvalList(int[] modeList) {
		for (int i : modeList)
			choiceEvalQ.addItem(Integer.toString(i));
	}

	public void setQuickEvalTooltip(String str) {
		choiceEvalQ.setToolTipText(str);
	}

	public void setTrainEvalMode(int tEvalmode) {
		// If the mode list has not been initialized, add the selected mode to
		// the list
		if (choiceEvalT.getItemCount() == 0) {
			choiceEvalT.addItem(Integer.toString(tEvalmode));
		}
		choiceEvalT.setSelectedItem(tEvalmode + "");
	}

	public void setTrainEvalList(int[] modeList) {
		for (int i : modeList)
			choiceEvalT.addItem(Integer.toString(i));
	}

	public void setTrainEvalTooltip(String str) {
		choiceEvalT.setToolTipText(str);
	}

	public void setStopTest(int value) {
		stopTest_T.setText(value + "");
	}

	public void setStopEval(int value) {
		stopEval_T.setText(value + "");
	}

	public void setNumEval(int value) {
		numEval_T.setText(value + "");
	}

	public void setWrapperNPly(int value) {
		wNply_T.setText(value + "");
	}

	public void setEpisodeLength(int value) {
		if (value == Integer.MAX_VALUE) value=-1;
		epiLeng_T.setText(value + "");
	}

	public void setChooseStart01(boolean bChooseStart01) {
		chooseS01.setState(bChooseStart01);
	}

	public void setLearnFromRM(boolean bLearnFromRM) {
		learnRM.setState(bLearnFromRM);
	}

	public void setRewardIsGameScore(boolean bRGS) {
		rewardIsGameScore.setState(bRGS);
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded
	 * agent
	 * 
	 * @param op
	 *            ParOther of the re-loaded agent
	 */
	public void setFrom(ParOther op) {
		this.setQuickEvalMode(op.getQuickEvalMode());
		this.setTrainEvalMode(op.getTrainEvalMode());
		this.setNumEval(op.getNumEval());
		this.setEpisodeLength(op.getEpisodeLength());
		this.setStopTest(op.getStopTest());
		this.setStopEval(op.getStopEval());
		this.setWrapperNPly(op.getWrapperNPly());
		this.chooseS01.setState(op.getChooseStart01());
		this.learnRM.setState(op.getLearnFromRM());
		this.rewardIsGameScore.setState(op.getRewardIsGameScore());
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "Sarsa" (for {@link SarsaAgt})
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	@Deprecated
	public void setParamDefaults(String agentName, String gameName) {
		// Currently we have here only the sensible defaults for one game ("RubiksCube"):
		switch (gameName) {
		case "RubiksCube": 
			chooseS01.setState(true);		// always select a non-solved cube as start state
			enableChoosePart(false);
			epiLeng_T.setText("50"); 		// the maximum episode length (when playing a game)
			break;
		default:	//  all other
			break;
		}
		switch (agentName) {
		case "Sarsa":
		case "TD-NTuple-3":
			learnRM.setState(true);
			break;
		default: 
			learnRM.setState(false);
			break;
		}
	}
	
} // class OtherParams
