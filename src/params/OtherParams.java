package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serial;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.PlayAgent.AgentState;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.RubiksCube.GameBoardCube;

/**
 * This class realizes other parameter settings for board games. Most parameters
 * are only relevant for trainable agents (like {@link TDAgent}, {@link TDNTuple3Agt}), 
 * but <b>Quick Eval Mode</b> and <b>Wrapper nPly</b> are relevant for all agents
 * <p>
 * These parameters and their [defaults] are:
 * <ul>
 * <li><b>Quick Eval Mode</b>: evaluation mode used during 'Quick Evaluation'
 * <li><b>Train Eval Mode</b>: additional evaluation mode used during training
 * <li><b>numEval</b>: [100] During training: Call the evaluators every NumEval
 * episodes
 * <li><b>Episode length</b>: During training: Maximum number of moves in an episode. 
 * If reached, this episode terminates prematurely. -1: never terminate.
 * <li><b>EpiLength Eval</b>: During evaluation: Maximum number of moves in an episode. 
 * If reached, this episode terminates prematurely. -1: never terminate.
 * <li><b>stopTest</b>: [ 0] whether to perform the stop test during training.
 * If {@literal > 0}, then m_evaluator2 is checked during training whether its
 * goal is reached
 * <li><b>Wrapper nPly</b>: [0] if &gt; 0, wrap the agent in an (Expecti)Max-N wrapper with
 * n plies of look-ahead. CAUTION: n &gt; 5 can dramatically slow down computation. 
 * <li><b>Choose Start 01</b>: [false] During training: Whether to start always from default
 * start state ({@code false}, or to start 50% from default, 50% from a random
 * 1-ply state ({@code true}
 * <li><b>Learn from RM</b>: [false] whether to learn from random moves or not
 * </ul>
 * 
 * @see games.XArenaButtons
 */
// Deprecated meaning of stopEval:
// <li><b>stopEval</b>: [100] During training: How many successful evaluator
// calls are needed to stop training prematurely?
public class OtherParams extends Frame {
	@Serial
	private static final long serialVersionUID = 1L;

	JLabel evalQ_L;
	public JComboBox<String> choiceEvalQ;
	JLabel evalT_L;
	public JComboBox<String> choiceEvalT;

	JLabel numEval_L;
	JLabel epiLeng_L;
	JLabel stopTest_L;
	JLabel stopEval_L;
	JLabel chooseS01L;
	JLabel learnRM_L;
	JLabel rgs_L;
	JLabel wNply_L;
	JLabel wMCTS_L;
	JLabel wMCTSpUCT_L;
	JLabel wMCTSdepth_L;
	JLabel pMin_L;
	JLabel pMax_L;
	JLabel rBuf_L;
	JLabel aState_L;
	public JTextField numEval_T;
	public JTextField epiLeng_T;
	public JTextField stopTest_T;
	public JTextField stopEval_T;
	public JTextField wNply_T;
	public JTextField wMCTS_T;
	public JTextField wMCTSpUCT_T;
	public JTextField wMCTSdepth_T;
	public JTextField pMin_T;
	public JTextField pMax_T;
	public Checkbox chooseS01;
	public Checkbox learnRM;
	public Checkbox rewardIsGameScore;
	public Checkbox replayBuf;
	public JLabel agentState;

	Button ok;
	JPanel oPanel;
	OtherParams m_par;
	Arena m_arena;			// needed to infer the game name

	public OtherParams(Arena m_arena) {
		super("Other Parameter");

		this.m_arena = m_arena;

		evalQ_L = new JLabel("Quick Eval Mode");
		this.choiceEvalQ = new JComboBox<>();
		evalT_L = new JLabel("Train Eval Mode");
		this.choiceEvalT = new JComboBox<>();

		numEval_T = new JTextField("500"); 	//
		epiLeng_T = new JTextField("-1"); 	//
		stopTest_T = new JTextField("0"); 	//
		stopEval_T = new JTextField("-1"); 	// the defaults
		wNply_T = new JTextField("0"); 		//
		wMCTS_T = new JTextField("0"); 		//
		wMCTSpUCT_T = new JTextField("1"); 		//
		wMCTSdepth_T = new JTextField("-1"); 		//
		pMin_T = new JTextField("1");		//
		pMax_T = new JTextField("6");		//
		numEval_L = new JLabel("numEval");
		epiLeng_L = new JLabel("Episode Length");
		stopTest_L = new JLabel("stopTest");
		stopEval_L = new JLabel("EpiLength Eval");
		chooseS01L = new JLabel("Choose Start 01");
		learnRM_L = new JLabel("Learn from RM");
		rgs_L = new JLabel("Reward = Score");
		wNply_L = new JLabel("Wrapper nPly");
		wMCTS_L = new JLabel("Wrapper MCTS");
		wMCTSpUCT_L = new JLabel("PUCT for WrapM");
		wMCTSdepth_L = new JLabel("Depth for WrapM");
		pMin_L = new JLabel("pMin");
		pMax_L = new JLabel("pMax");
		rBuf_L = new JLabel("Replay buffer");
		aState_L = new JLabel("Agent state");
		chooseS01 = new Checkbox("", false);
		learnRM = new Checkbox("", false);
		replayBuf = new Checkbox("", false);
		agentState = new JLabel("");
		rewardIsGameScore = new Checkbox("", true);
		ok = new Button("OK");
		m_par = this;
		oPanel = new JPanel();  // put the inner buttons into panel oPanel. This panel
								// can be handed over to a tab of a JTabbedPane
								// (see class XArenaTabs)

		evalQ_L.setToolTipText("'Quick Evaluation' evaluator has this mode");
		evalT_L.setToolTipText(
				"additional evaluator with this mode during training (none if mode is the same as Quick Eval Mode)");
		numEval_L.setToolTipText("During training: Call the evaluators every NumEval episodes");
		epiLeng_L.setToolTipText(
				"<html>During training: Maximum number of moves in an episode. <br>If reached, game terminates prematurely. -1: never terminate.</html>");
		stopTest_L.setToolTipText("During training: If >0 then perform stop test");
		stopEval_L.setToolTipText(
				"<html>During eval &amp play: Maximum number of moves in an episode. <br>If reached, game terminates prematurely. -1: never terminate.</html>");
		chooseS01L.setToolTipText("<html>Choose start state in training: <br>50% default, 50% random 1-ply</html>");
		learnRM_L.setToolTipText("Learn from random moves during training");
		rgs_L.setToolTipText("Use game score as reward (def.) or use some other, game specific reward");
		wNply_L.setToolTipText(
				"<html>Wrapper n-ply look ahead <br>(for play, compete, eval). <br>CAUTION: Numbers >5 can take long!</html>");
		wMCTS_L.setToolTipText(
				"<html>Wrapper MCTS iterations <br>(for play, compete, eval tasks)</html>");
		wMCTSpUCT_L.setToolTipText("PUCT value for MCTS Wrapper");
		wMCTSdepth_L.setToolTipText("max depth value for MCTS Wrapper. -1: no max depth");
		pMin_L.setToolTipText(
				"RubiksCube: min. number of initial twists (during traing and eval)");
		pMax_L.setToolTipText(
				"RubiksCube: max. number of initial twists (during traing and eval)");
		rBuf_L.setToolTipText(
				"RubiksCube: use replay buffer during training");

		// this.setQuickEvalMode(0);
		// this.setTrainEvalMode(0);


		// the following lambda's, where e is an ActionEvent, are a simpler replacement for anonymous action listeners:

		ok.addActionListener( e -> m_par.setVisible(false) );
		wMCTS_T.addActionListener( e -> enableWrapMCTSPart() );

		// only for RubiksCube:
		pMin_T.addActionListener( e ->
				{
					//if pMin is changed by user, set the corresponding element in GameBoardCubeGUI
					if (m_arena.getGameBoard() instanceof GameBoardCube) {
						((GameBoardCube)m_arena.getGameBoard()).setPMin(getpMinRubiks());
				}
		}
		);
		pMax_T.addActionListener(e ->
				{
					//if pMax is changed by user, set the corresponding element in GameBoardCubeGUI
					if (m_arena.getGameBoard() instanceof GameBoardCube) {
						((GameBoardCube)m_arena.getGameBoard()).setPMax(getpMaxRubiks());
				}
		}
		);


		replayBuf.addItemListener(new ItemListener()
				{
					  public void itemStateChanged(ItemEvent e)
					  {
						  //if replayBuf is changed by user, set the corresponding element in CubeConfig
						  // TODO
					  }
				 }
		);

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

		oPanel.add(wMCTS_L);
		oPanel.add(wMCTS_T);

		oPanel.add(wMCTSpUCT_L);
		oPanel.add(wMCTSpUCT_T);

		oPanel.add(wNply_L);
		oPanel.add(wNply_T);

		oPanel.add(wMCTSdepth_L);
		oPanel.add(wMCTSdepth_T);

//		oPanel.add(new Canvas());
//		oPanel.add(new Canvas());

		if (m_arena.getGameName().equals("RubiksCube")) {
			oPanel.add(pMin_L);
			oPanel.add(pMin_T);
			oPanel.add(pMax_L);
			oPanel.add(pMax_T);
		} else {
			oPanel.add(chooseS01L);
			oPanel.add(chooseS01);
			oPanel.add(learnRM_L);
			oPanel.add(learnRM);
		}

		if (m_arena.getGameName().equals("RubiksCube")) {
			oPanel.add(rBuf_L);
			oPanel.add(replayBuf);
		} else {
			oPanel.add(rgs_L);
			oPanel.add(rewardIsGameScore);
		}
		oPanel.add(aState_L);
		oPanel.add(agentState);

		add(oPanel, BorderLayout.CENTER);
		add(ok, BorderLayout.SOUTH);

		pack();
		setVisible(false);

		enableWrapMCTSPart();
		enableStopTest(false);			// stop test is now deprecated

	} // constructor OtherParams()

	public void enableChoosePart(boolean enable) {
		chooseS01.setEnabled(enable);
		chooseS01L.setEnabled(enable);
	}

	public void enableRgsPart(boolean enable) {
		rgs_L.setEnabled(enable);
		rewardIsGameScore.setEnabled(enable);
	}

	public void enableStopTest(boolean enable) {
		stopTest_T.setEnabled(enable);
		stopTest_L.setEnabled(enable);
	}

	private void enableWrapMCTSPart() {
		if(this.getWrapperMCTSIterations()>0){
			wMCTSdepth_L.setEnabled(true);
			wMCTSdepth_T.setEnabled(true);
			wMCTSpUCT_L.setEnabled(true);
			wMCTSpUCT_T.setEnabled(true);
		} else {
			wMCTSdepth_L.setEnabled(false);
			wMCTSdepth_T.setEnabled(false);
			wMCTSpUCT_L.setEnabled(false);
			wMCTSpUCT_T.setEnabled(false);
		}
	}



	public JPanel getPanel() {
		return oPanel;
	}

	public int getQuickEvalMode() {
		String s = (String) choiceEvalQ.getSelectedItem();
		if (s == null)
			return 0; 	// return a dummy, if choiceEvalQ is empty (happens in
					  	// case of 'new OtherParams()')
		return Integer.parseInt(s);
	}

	public int getTrainEvalMode() {
		String s = (String) choiceEvalT.getSelectedItem();
		if (s == null)
			return 0; 	// return a dummy, if choiceEvalT is empty (happens in
						// case of 'new OtherParams()')
		return Integer.parseInt(s);
	}

	public int getStopTest() {
		return Integer.parseInt(stopTest_T.getText());
	}

	public int getStopEval() {
		int elen = Integer.parseInt(stopEval_T.getText());
		if (elen == -1)	elen = Integer.MAX_VALUE;
		return elen;
	}

	public int getNumEval() {
		return Integer.parseInt(numEval_T.getText());
	}

	public int getWrapperNPly() {
		return Integer.parseInt(wNply_T.getText());
	}

	public int getWrapperMCTSIterations() {
		return Integer.parseInt(wMCTS_T.getText());
	}

	public double getWrapperMCTS_PUCT() {
		return Double.parseDouble(wMCTSpUCT_T.getText());
	}

	public int getWrapperMCTS_depth() {
		return Integer.parseInt(wMCTSdepth_T.getText());
	}

	public int getpMinRubiks() {
		return Integer.parseInt(pMin_T.getText());
	}

	public int getpMaxRubiks() {
		return Integer.parseInt(pMax_T.getText());
	}

	public boolean getReplayBuffer() {
		return replayBuf.getState();
	}

	public double getIncAmount() {
		// dummy stub
		return 0.0;
	}

	public int getEpisodeLength() {
		int elen = Integer.parseInt(epiLeng_T.getText());
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

	public AgentState getAgentState() {
		AgentState as =
				switch(agentState.getText()) {
					case "RAW","" -> AgentState.RAW;
					case "INIT" -> AgentState.INIT;
					case "TRAINED" -> AgentState.TRAINED;
					default -> throw new IllegalStateException("Unexpected value: " + agentState.getText());
				};
		return as;
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

	public void setWrapperMCTSIterations(final int value) {
		wMCTS_T.setText(value + "");
	}

	public void setWrapperMCTS_PUCT(final double value) {
		wMCTSpUCT_T.setText(value + "");
	}

	public void setWrapperMCTS_depth(final int value) {
		wMCTSdepth_T.setText(value + "");
	}

	public void setpMinRubiks(int value) {
		pMin_T.setText(value + "");
	}

	public void setpMaxRubiks(int value) {
		pMax_T.setText(value + "");
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

	public void setReplayBuffer(boolean bReplayBuf) {
		replayBuf.setState(bReplayBuf);
	}

	public void setRewardIsGameScore(boolean bRGS) {
		rewardIsGameScore.setState(bRGS);
	}

	public void setAgentState(AgentState as) {
		switch (as) {
			case RAW -> agentState.setText("RAW");
			case INIT -> agentState.setText("INIT");
			case TRAINED -> agentState.setText("TRAINED");
		}
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded
	 * agent
	 * 
	 * @param op
	 *            ParOther of the re-loaded agent
	 */
	public void setFrom(ParOther op) {
		int elen;
		this.setQuickEvalMode(op.getQuickEvalMode());
		this.setTrainEvalMode(op.getTrainEvalMode());
		this.setNumEval(op.getNumEval());
		elen = (op.getEpisodeLength()==Integer.MAX_VALUE) ? -1 : op.getEpisodeLength();
		this.setEpisodeLength(elen);
		elen = (op.getStopEval()==Integer.MAX_VALUE) ? -1 : op.getStopEval();
		this.setStopEval(elen);
		this.setStopTest(op.getStopTest());
		this.setWrapperNPly(op.getWrapperNPly());
		this.setWrapperMCTSIterations(op.getWrapperMCTSIterations());
		this.setWrapperMCTS_PUCT(op.getWrapperMCTS_PUCT());
		this.setWrapperMCTS_depth(op.getWrapperMCTS_depth());
		this.setpMinRubiks(op.getpMinRubiks());
		this.setpMaxRubiks(op.getpMaxRubiks());
		this.chooseS01.setState(op.getChooseStart01());
		this.learnRM.setState(op.getLearnFromRM());
		this.replayBuf.setState(op.getReplayBuffer());
		this.rewardIsGameScore.setState(op.getRewardIsGameScore());
		this.setAgentState(op.getAgentState());

		// only for RubiksCube:
		// if pMax is changed via fillParamTabsAfterLoading, set also the corresponding element in GameBoardCubeGUI
		if (m_arena.getGameBoard() instanceof GameBoardCube) {
			((GameBoardCube)m_arena.getGameBoard()).setPMin(getpMinRubiks());
			((GameBoardCube)m_arena.getGameBoard()).setPMax(getpMaxRubiks());
		}

		enableWrapMCTSPart();

	}
	
} // class OtherParams
