package games;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import params.*;
import tools.HtmlDisplay;
import tools.Types;
import tools.SolidBorder;


/**
 * Helper class for {@link Arena} and {@link ArenaTrain}: <ul> 
 * <li> sets the buttons, selectors and text fields,
 * <li> sets initial values for all relevant fields,
 * <li> has the action code for Param-, Train-, MultiTrain-, and Play-button events.
 * </ul>
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class XArenaButtons extends JPanel		
{
	private static final long serialVersionUID = 1L;
	XArenaFuncs 		m_xfun;	
	public Arena	 	m_game;	// a reference to the Arena object passed in with the constructor
	public OptionsComp 	winCompOptions = new OptionsComp(); // window with Competition Options
	private LogManagerGUI logManagerGUI = null;
	int numPlayers;
	int m_numParamBtn;			// number of the last param button pressed
	int m_numTrainBtn;			// number of the last train button pressed

	JButton[] mParam;
	JButton[] mTrain;
	JButton MultiTrain;
	JButton Play;
	JButton InspectV;
	//JButton TDparB;			// now in XArenaTabs
	//JButton CMAparB;
	//JButton TCparB;
	//JButton OparB;
	JButton NTupShowB;
	JButton Logs; 
	JSlider Delay;				// the sleep slider
	TextField GameNumT;
	TextField TrainNumT;
	//TextField CompeteNumT;	// now in OptionsComp winCompOptions
	//TextField CompetitionsT;	//
	//Label Competitions_L;		//
	//Label CompeteG_L;			//
	Label GameNumL;
	Label TrainNumL;
	Label AgentX_L;
	Label SleepDurationL;
	Choice[] choiceAgent; 
	JLabel showValOnGB_L;
	Checkbox showValOnGB;		// show game values on gameboard
	TDParams[] tdPar;
	NTParams[] ntPar;
	MaxNParams[] maxnParams;
	MCTSParams[] mctsParams;
	MCParams[] mcParams;
	MCTSExpectimaxParams[] mctsExpectimaxParams;
	OtherParams[] oPar;
	HtmlDisplay htmlDisplay = null;
	boolean htmlDisplayActive = false;
	boolean isNTupShowEnabled = false;

	// the colors of the TH Köln logo (used for button coloring):
	Color colTHK1 = new Color(183,29,13);
	Color colTHK2 = new Color(255,137,0);
	Color colTHK3 = new Color(162,0,162);

	/**
	 * This class is needed for each ActionListener of {@code mParam[i]} and
	 * {@code mTrain[i]} in {@link XArenaButtons} constructor
	 */
	class ActionHandler implements ActionListener
	{
		int x;
		
		ActionHandler(int num1)			
		{		
			x=num1;
		}
		public void actionPerformed(ActionEvent e){}			
	}
	
	class ItemListenerHandler implements ItemListener
	{
		int n;
		
		ItemListenerHandler(int num1)			
		{		
			n=num1;
		}
		public void itemStateChanged(ItemEvent arg0){}			
	}
	
	public XArenaButtons(XArenaFuncs game, Arena arena)
	{
		String AgentX;
		String AgentO;
		SolidBorder bord = new SolidBorder();
		
		m_xfun = game;
		m_game = arena;

		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		mParam = new JButton[numPlayers];
		mTrain = new JButton[numPlayers];
		choiceAgent = new Choice[numPlayers];
		assert (numPlayers<=Types.GUI_PLAYER_NAME.length) 
			: "GUI not configured for "+numPlayers+" players. Increase Types.GUI_PLAYER_NAME and GUI_AGENT_INITIAL";
		tdPar = new TDParams[numPlayers];
		ntPar = new NTParams[numPlayers];
		maxnParams = new MaxNParams[numPlayers];
		mctsParams = new MCTSParams[numPlayers];
		mcParams = new MCParams[numPlayers];
		mctsExpectimaxParams = new MCTSExpectimaxParams[numPlayers];
		oPar = new OtherParams[numPlayers];

		// 
		// initial settings for the GUI
		//
		AgentX = null; //Types.GUI_X_PLAYER;  // "MCTS"; "TDS"; "CMA-ES"; "Minimax" 
		AgentO = null; //Types.GUI_O_PLAYER;  // "Human";"ValIt";
		GameNumT=new TextField("10000", 5); //("10000", 5);
		TrainNumT=new TextField("25", 5);

		MultiTrain=new JButton("MultiTrain");
		MultiTrain.setBorder(bord);
		Play=new JButton("Play");
		Play.setBorder(bord);
		InspectV=new JButton("Inspect V");
		InspectV.setBorder(bord);
		NTupShowB = new JButton("Insp Ntuples");
		NTupShowB.setBorder(bord);
		Logs=new JButton("Logs");
		Logs.setBorder(bord);
		Delay = new JSlider(JSlider.HORIZONTAL, m_game.minSleepDuration,m_game.maxSleepDuration, m_game.currentSleepDuration);
		GameNumL = new Label("Train Games");
		TrainNumL = new Label("Agents trained");
		AgentX_L = new Label("Agent Type: ");
        SleepDurationL = new Label("Sleep duration");
		showValOnGB_L = new JLabel("Show V  ");
		showValOnGB = new Checkbox("",true);

		for (int n=0; n<numPlayers; n++) {
			choiceAgent[n] = new Choice();
			for (String s : Types.GUI_AGENT_LIST) choiceAgent[n].add(s);
			choiceAgent[n].select(Types.GUI_AGENT_INITIAL[n]);	
			if (numPlayers==2) {
				mParam[n]=new JButton("Param "+Types.GUI_2PLAYER_NAME[n]);
				mTrain[n]=new JButton("Train "+Types.GUI_2PLAYER_NAME[n]);
			} else {
				mParam[n]=new JButton("Param "+Types.GUI_PLAYER_NAME[n]);
				mTrain[n]=new JButton("Train "+Types.GUI_PLAYER_NAME[n]);
			}
			mParam[n].setForeground(Color.white);
			mTrain[n].setForeground(Color.white);
			mParam[n].setBackground(colTHK1);		// Color.orange
			mTrain[n].setBackground(colTHK1);		//
			mParam[n].setBorder(bord);
			mTrain[n].setBorder(bord);
			
			choiceAgent[n].setEnabled(false);		
			// Arena does not allow user to modify choice boxes (see ArenaTrain)
			
			// whenever one of the agent choice boxes changes, call setParamDefaults
			// to set the param tabs to sensible defaults for that agent and that game
			choiceAgent[n].addItemListener(
					new ItemListenerHandler(n) { // this constructor will copy n to ItemListenerHandler.n
						public void itemStateChanged(ItemEvent arg0) {
							setParamDefaults(n, choiceAgent[n].getSelectedItem(),
									m_game.getGameName());
						}
					}
			);
			
			tdPar[n] = new TDParams();
			ntPar[n] = new NTParams();
			maxnParams[n] = new MaxNParams();
			mctsParams[n] = new MCTSParams();
			mcParams[n] = new MCParams();
			mctsExpectimaxParams[n] = new MCTSExpectimaxParams();
			oPar[n] = new OtherParams();
			this.setParamDefaults(n, Types.GUI_AGENT_INITIAL[n], m_game.getGameName());
			
			try {
				Feature dummyFeature = m_game.makeFeatureClass(0); 
				// Why object dummyFeature? - By constructing it via the factory pattern method
				// makeFeatureClass we ensure to get a FeatureXX object (with XX being the specific game)
				// and only this object knows the available feature modes. It would not help to have
				// a static method getAvailFeatmode() since we cannot call it here.
				// 
				// Why try-catch? - If the default implementation of makeFeatureClass() is not
				// overridden, it will throw a RuntimeException
				//
				tdPar[n].setFeatList(dummyFeature.getAvailFeatmode());
			} catch (RuntimeException ignored){ }
			
			try {
				Evaluator dummyEvaluator = m_game.makeEvaluator(null, null, 0, 0, 0); 
				oPar[n].setQuickEvalList(dummyEvaluator.getAvailableModes());
				oPar[n].setTrainEvalList(dummyEvaluator.getAvailableModes());
				oPar[n].setQuickEvalMode(dummyEvaluator.getQuickEvalMode());
				oPar[n].setTrainEvalMode(dummyEvaluator.getTrainEvalMode());
			} catch (RuntimeException ignored){ 
				System.out.println(ignored.getMessage());				
			}

		} // for

		this.enableButtons(false);
		GameNumT.setEnabled(false);		// Arena allows no training / multi-training 
		TrainNumT.setEnabled(false);	// (see ArenaTrain for enabling this)	
		Play.setEnabled(true);
		InspectV.setEnabled(true);
		Logs.setEnabled(true);
		// Arena enables only button Play & Inspect V

		MultiTrain.setForeground(Color.white);
		Play.setForeground(Color.white);
		InspectV.setForeground(Color.white);
		Logs.setForeground(Color.white);
		NTupShowB.setForeground(Color.white);
		GameNumT.setForeground(Color.black);
		GameNumL.setForeground(Color.black);
        SleepDurationL.setForeground(Color.black);

		MultiTrain.setBackground(colTHK2);	// Color.lightGray
		Play.setBackground(colTHK2);		// Color.blue
		InspectV.setBackground(colTHK3);
		Logs.setBackground(colTHK3);
		NTupShowB.setBackground(colTHK2);
		GameNumT.setBackground(Color.white);
		//GameNumL.setBackground(Color.white);

		Delay.setMajorTickSpacing(250);
		Delay.setMinorTickSpacing(50);
		Delay.setPaintTicks(true);
		Delay.setPaintLabels(true);
		
		
		NTupShowB.setEnabled(false);	// enable this button only if AgentX is an N-Tuple Player
		
		// Why do we set only m_game.state in the following ActionListeners and delegate
		// the call of the relevant functions to the big state-switch in m_game.run()?
		// (a) Because the new JButtons do only react on disabling them, when the 
		// ActionListener is actually left (!) (b) Because of clearer separation GUI - Control
		
		for (int n=0; n<numPlayers; n++) {
			mParam[n].addActionListener(
					new ActionHandler(n)		// this constructor will copy n to member x
					{
						public void actionPerformed(ActionEvent e)
						{	
							m_numParamBtn = x;
							m_game.taskState = ArenaTrain.Task.PARAM;
						}
					}	
			);
		}

		for (int n=0; n<numPlayers; n++) {
			mTrain[n].addActionListener(
					new ActionHandler(n)		// constructor copies n to member x
					{
						public void actionPerformed(ActionEvent e)
						{	
							m_numTrainBtn = x;
							m_game.taskState = ArenaTrain.Task.TRAIN;
						}
					}	
			);
		}

		MultiTrain.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
//						m_xfun.m_NetIsLinear = tdPar.LinNetType.getState();
//						m_xfun.m_NetHasSigmoid = tdPar.withSigType.getState();
						m_game.taskState = ArenaTrain.Task.MULTTRN;
					}
				}	
		);


		Play.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						if (m_game.taskState==ArenaTrain.Task.INSPECTV) {
							m_game.taskState = ArenaTrain.Task.IDLE;
							// if Play button is pressed while being in InspectV-mode, 
							// store this in taskBefore:
							m_game.taskBefore=ArenaTrain.Task.INSPECTV;
						}
						// toggle m_game.state between PLAY and IDLE
						if (m_game.taskState!=ArenaTrain.Task.PLAY) {
							m_game.taskState = ArenaTrain.Task.PLAY;
							m_game.setStatusMessage("Playing a game ...");
							enableButtons(false);		// disable all buttons ...
							Play.setEnabled(true);		// ... but the Play button
						} else {
							m_game.taskState = ArenaTrain.Task.IDLE;
							m_game.setStatusMessage("Done.");
							enableButtons(true);
						}
						
					}
				}	
		);

		InspectV.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						// toggle m_game.state between INSPECTV and IDLE
						if (m_game.taskState!=ArenaTrain.Task.INSPECTV) {
							m_game.taskState = ArenaTrain.Task.INSPECTV;
							m_game.setStatusMessage("Inspecting the value function ...");
							enableButtons(false);			// disable all buttons ...
							InspectV.setEnabled(true);		// ... but the InspectV button
							Play.setEnabled(true);			// ... and the Play button
						} else {
							m_game.taskState = ArenaTrain.Task.IDLE;
							m_game.setStatusMessage("Done.");
							enableButtons(true);
						}
					}
				}	
		);

		Logs.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
					    if(logManagerGUI == null) {
                            logManagerGUI = new LogManagerGUI(m_game.logManager, m_game.getGameBoard());
                        } else {
                            logManagerGUI.show();
                        }
					}
				}
		);

		Delay.addChangeListener(
				new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JSlider source = (JSlider)e.getSource();
						//if (!source.getValueIsAdjusting()) { //only Update when slider is released
						//	System.out.println("Changed Delay to: " + source.getValue());
							m_game.currentSleepDuration = source.getValue();
						//}
					}
				}
		);

//		NTupShowB.addActionListener(
//				new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						m_game.taskState = ArenaTrainTTT.Task.INSPECTNTUP;
//					}
//				}					
//		);

		// 
		// here comes the layout of all elements
		// 		
		JPanel q = new JPanel();
		q.setLayout(new GridLayout(1,0,10,10));
		q.setBackground(Types.GUI_BGCOLOR);
		this.setBackground(Types.GUI_BGCOLOR);
		JPanel q1 = new JPanel();
		q1.setLayout(new GridLayout(0,1,10,10));
		q1.setBackground(Types.GUI_BGCOLOR);
		q1.add(AgentX_L);
		q1.add(new Canvas());
		q1.add(new Canvas());
		q.add(q1);
		
		//
		// here comes the agent columns: Choice / Param- / Train-btn
		//
		JPanel jPanel = new JPanel();
		jPanel.setBackground(Types.GUI_BGCOLOR);
		if (numPlayers==1) q.add(jPanel);
		JPanel[][] qcol = new JPanel[4][numPlayers];	// four color stripes: 2 for Param btn, 2 for Train btn
		for (int n=0; n<numPlayers; n++) {
			for (int c=0; c<4; c++) {
				qcol[c][n] = new JPanel();
				qcol[c][n].setBackground(Types.GUI_PLAYER_COLOR[n]);
				qcol[c][n].setPreferredSize(new Dimension(10,9));
				qcol[c][n].setBorder(bord);
			}
			JPanel qparam = new JPanel();
			qparam.setLayout(new BorderLayout());		// BorderLayout reacts on the preferred size for qcol
			qparam.add(qcol[0][n],BorderLayout.WEST);
			qparam.add(mParam[n],BorderLayout.CENTER);
			qparam.add(qcol[2][n],BorderLayout.EAST);
			JPanel qtrain = new JPanel();
			qtrain.setLayout(new BorderLayout());
			qtrain.add(qcol[1][n],BorderLayout.WEST);
			qtrain.add(mTrain[n],BorderLayout.CENTER);
			qtrain.add(qcol[3][n],BorderLayout.EAST);
			JPanel qplay = new JPanel();
			qplay.setLayout(new GridLayout(0,1,10,10));
			qplay.setBackground(Types.GUI_BGCOLOR);
			qplay.add(choiceAgent[n]);
			qplay.add(qparam);
			qplay.add(qtrain);
			q.add(qplay);	
			mParam[n].setVisible(false);		// see ArenaTrain for making them
			mTrain[n].setVisible(false);		// visible
		}
		if (numPlayers<3) q.add(jPanel);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		p1.setBackground(Types.GUI_BGCOLOR);
		
		p1.add(GameNumL);
		p1.add(GameNumT);
		p1.add(new Canvas());
		p1.add(Logs);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		p2.setBackground(Types.GUI_BGCOLOR);

		p2.add(TrainNumL);
		p2.add(TrainNumT);
		p2.add(MultiTrain);
		p2.add(new Canvas());

		JPanel psv = new JPanel();
		psv.setBackground(Types.GUI_BGCOLOR);
		psv.add(showValOnGB_L);
		psv.add(showValOnGB);
		psv.add(new JLabel("   "));		// add some space to the right
		psv.add(new JLabel("   "));		//
		
		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		p3.setBackground(Types.GUI_BGCOLOR);
	
		p3.add(Play);
		p3.add(psv);
		p3.add(NTupShowB);		
		p3.add(InspectV);

		JPanel ptp = new JPanel();
		ptp.setLayout(new GridLayout(0,1,10,10));	// rows,columns,hgap,vgap
		ptp.setBackground(Types.GUI_BGCOLOR);
		ptp.add(p1);
		ptp.add(p2);
		ptp.add(p3);
		// adding to ptp three objects of equal height helps the layout manager to balance the height distribution
		
		JPanel delayPanel = new JPanel();
        delayPanel.setLayout(new BorderLayout(10,10));
        delayPanel.setBackground(Types.GUI_BGCOLOR);
        Delay.setBackground(Types.GUI_BGCOLOR);
		delayPanel.add(SleepDurationL, java.awt.BorderLayout.WEST);
		delayPanel.add(Delay,java.awt.BorderLayout.CENTER);
		
		//JPanel s = new JPanel();
		//s.setLayout(new GridLayout(0,1,10,10));		// rows,columns,hgap,vgap (1 column = allow long messages)
		m_game.setStatusMessage("Init done.");
		
		setLayout(new BorderLayout(10,10));
		add(q,java.awt.BorderLayout.NORTH);
		add(ptp,java.awt.BorderLayout.CENTER);
		add(delayPanel,java.awt.BorderLayout.SOUTH);
		//add(s,java.awt.BorderLayout.SOUTH);
		
		// infoPanel (StatusMessage) is in Arena.java
		
		// size of window: see Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT

	} // constructor XArenaButtons

	/**
	 * @param n				number of agent
	 * @param agentName		name of agent
	 * @param gameName		name of game 
	 */
	public void setParamDefaults(int n, String agentName, String gameName) {
		tdPar[n].setParamDefaults(agentName, gameName);
		ntPar[n].setParamDefaults(agentName, gameName);
		oPar[n].setParamDefaults(agentName, gameName);
		
		if(agentName.equals("TDS")) {
			switch(gameName) {
			case "TicTacToe":
				GameNumT.setText("10000");				
			}
		}
		if(agentName.equals("TD-Ntuple")) {
			NTupShowB.setEnabled(true);	// enable this button only if agentName is an n-tuple agent
			switch(gameName) {
			case "TicTacToe":
			default: 
				GameNumT.setText("10000");				
			}
		}
		else {
			NTupShowB.setEnabled(false);			
		}
	}
	
	public void helpFunction() {
		if (htmlDisplay==null) {
			//htmlDisplay =new HtmlDisplay("HelpGUI-test.htm");
			htmlDisplay =new HtmlDisplay("HelpGUI-Arena-GBG.htm");
			htmlDisplay.setTitle("Help for Arena in GBG");
			htmlDisplay.setSize(800,600);
			htmlDisplay.setLocation(466, 0);
		}
		htmlDisplayActive = !htmlDisplayActive;
		htmlDisplay.setVisible(htmlDisplayActive);	
	}

	// Known caller outside XArenaButtons: Arena.run()
	void enableButtons(boolean state) {
		Play.setEnabled(state);
		InspectV.setEnabled(state);
		Logs.setEnabled(state);
		for (int n=0; n<numPlayers; n++) {
			mParam[n].setEnabled(state);
			mTrain[n].setEnabled(state);
		}
		MultiTrain.setEnabled(state);
		if (!state) isNTupShowEnabled = NTupShowB.isEnabled();
		NTupShowB.setEnabled(state);
		if (state) NTupShowB.setEnabled(isNTupShowEnabled);
	}
	
//	/** 
//	 *  This helper function is used by {@link ArenaTrainTTT#InspectNtup()}
//	 */
//	public void nTupShowAction() {
//		if (ntupleShow!=null) {
//			ntupleShow.setVisible(!ntupleShow.isVisible());
//			// place window Ntuple Show on the right side of the main window, below TD params window
//			int x = m_game.getLocation().x + m_game.getWidth() + 8;
//			int y = m_game.getLocation().y;
//			if (m_game.m_TicFrame!=null) {
//				x = m_game.m_TicFrame.getLocation().x + m_game.m_TicFrame.getWidth() + 1;
//				y = m_game.m_TicFrame.getLocation().y + tdPar.getHeight();
//			}
//			ntupleShow.setLocation(x,y);
//		}
//		
//	}

	public int getNumTrainBtn() {
		return m_numTrainBtn;
	}

	public int getNumParamBtn() {
		return m_numParamBtn;
	}
	
	// needed currently only in GameBoardCube
	public int getEpisodeLength(int i) {
		return oPar[i].getEpisodeLength();
	}

	public boolean getShowValueOnGameBoard() {
		return showValOnGB.getState();
	}
	public String getSelectedAgent(int i){
		return choiceAgent[i].getSelectedItem();
	}
	public void setSelectedAgent(int i, String str){
		choiceAgent[i].select(str);
	}
	
} // class XArenaButtons	
