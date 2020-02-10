package gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import games.Arena;
import games.ArenaTrain;
import games.LogManagerGUI;
import games.XArenaButtons;
import tools.Types;

public class XArenaButtonsGui extends JPanel {

	private Arena m_arena = null;
	int numPlayers;
	boolean isNTupShowEnabled = false;
	boolean htmlDisplayActive = false;

	JButton[] mParam;
	JButton[] mTrain;
	JButton MultiTrain;
	JButton Play;
	JButton InspectV;
	JButton NTupShowB;
	JButton Logs; 
	JSlider Delay;				// the sleep slider
	JTextField GameNumT;
	JTextField TrainNumT;
	JLabel GameNumL;
	JLabel TrainNumL;
	JLabel AgentX_L;
	JLabel SleepDurationL;
	JPanel[][] qcol;			// the color stripes
	JComboBox[] choiceAgent;
	JLabel showValOnGB_L;
	JCheckBox showValOnGB;		// show game values on GameBoard
	private LogManagerGUI logManagerGUI = null;
	HtmlDisplay htmlDisplay = null;
	
	// the colors of the TH Koeln logo (used for button coloring):
	Color colTHK1 = new Color(183,29,13);	// dark red
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
	
	public XArenaButtonsGui(Arena arena) {
		m_arena = arena;
		initGui("");
	}

	public XArenaButtonsGui(Arena arena, LayoutManager arg0) {
		super(arg0);
		m_arena = arena;
		initGui("");
	}

	public XArenaButtonsGui(Arena arena, boolean arg0) {
		super(arg0);
		m_arena = arena;
		initGui("");
	}

	public XArenaButtonsGui(Arena arena, LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		m_arena = arena;
		initGui("");
	}

	private void initGui(String title) {
		// 
		// initial settings for the GUI
		//
		numPlayers = m_arena.getGameBoard().getStateObs().getNumPlayers();
		SolidBorder bord = new SolidBorder();
		mParam = new JButton[numPlayers];
		mTrain = new JButton[numPlayers];
		choiceAgent = new JComboBox[numPlayers];
		GameNumT=new JTextField("", 5);		// is set in XArenaButtons.setParamDefaults
		TrainNumT=new JTextField("", 5);	// is set in XArenaButtons.setParamDefaults

		MultiTrain=new JButton("MultiTrain");
		MultiTrain.setBorder(bord);
		Play=new JButton("Play");
		Play.setBorder(bord);
		InspectV=new JButton("Inspect V");
		InspectV.setBorder(bord);
		InspectV.setToolTipText("Inspect the value function for player X");
		NTupShowB = new JButton("Insp Ntuples");
		NTupShowB.setBorder(bord);
		Logs=new JButton("Logs");
		Logs.setBorder(bord);
		Delay = new JSlider(JSlider.HORIZONTAL, m_arena.minSleepDuration,m_arena.maxSleepDuration, m_arena.currentSleepDuration);
		GameNumL = new JLabel("Train Games");
		TrainNumL = new JLabel("Agents trained");
		AgentX_L = new JLabel("Agent Type: ");
        SleepDurationL = new JLabel("Sleep duration");
		showValOnGB_L = new JLabel("Show V  ");
		showValOnGB_L.setToolTipText("Show value function during game play");
		showValOnGB = new JCheckBox("",true);
		showValOnGB.setBackground(Types.GUI_BGCOLOR);

		// add game-specific agent names for certain games (currently ConnectFour, Nim and Othello)
		String gName = m_arena.getGameName();
		int offset = (gName=="ConnectFour" || gName=="Nim") ? 1 : (gName=="Othello") ? 3: 0;
		String[] gui_agent_list = new String[Types.GUI_AGENT_LIST.length+offset];
		for (int i=0; i<Types.GUI_AGENT_LIST.length; i++) gui_agent_list[i] = Types.GUI_AGENT_LIST[i];
		if (gName=="ConnectFour") {
			gui_agent_list[gui_agent_list.length-1] = "AlphaBeta";
		} else if (gName=="Nim") {
			gui_agent_list[gui_agent_list.length-1] = "Bouton";
		}else if (gName=="Othello") {
			gui_agent_list[gui_agent_list.length-3] = "HeurPlayer";
			gui_agent_list[gui_agent_list.length-2] = "BenchPlayer";
			//gui_agent_list[gui_agent_list.length-2] = "Edax";
			gui_agent_list[gui_agent_list.length-1] = "Edax2";
		}
		
		for (int n=numPlayers-1; n>=0; n--) {
			choiceAgent[n] = new JComboBox(gui_agent_list);
			choiceAgent[n].setSelectedItem(Types.GUI_AGENT_INITIAL[n]);
			
			// only applicable agents:
			if (m_arena.getGameBoard().getDefaultStartState().isDeterministicGame()) { // if changes are applied to this if
				choiceAgent[n].removeItem("Expectimax-N");						// repeat them in TSSettingsGUI2 constructor too
				choiceAgent[n].removeItem("MCTS Expectimax");					// the same if is used there for GUI init
			} else {
				choiceAgent[n].removeItem("Max-N");
				choiceAgent[n].removeItem("MCTS");
			}

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
			mParam[n].setVisible(true);
			
			choiceAgent[n].setEnabled(true);
			// Now we allow choice-box selection.
			// OLD: Arena does not allow user to modify choice boxes (see ArenaTrain)
			
			// whenever one of the agent choice boxes changes, call XArenaButtons.setParamDefaults
			// to set the param tabs to sensible defaults for that agent and that game
			// (but this call is inhibited by changedViaLoad[n]==true if a loadAgent-call triggered
			// the choice-box change)
			choiceAgent[n].addItemListener(
					new ItemListenerHandler(n) { // this constructor will copy n to ItemListenerHandler.n
						public void itemStateChanged(ItemEvent arg0) {
							m_arena.m_xab.selectedAgents[n] = (String) choiceAgent[n].getSelectedItem();
							if (m_arena.m_xab.changedViaLoad[n]) {
								// each change in choiceAgent[n] will trigger TWO ItemEvents:
								// 1) a DESELECTED event and 2) a SELECTED event. We reset
								// the switch changedViaLoad[n] only after the 2nd event.
								if (arg0.getStateChange()==ItemEvent.SELECTED)
									m_arena.m_xab.changedViaLoad[n]=false;
							} else {
								// the normal case, if item change was triggered by user:
								m_arena.m_xab.setParamDefaults(n, m_arena.m_xab.selectedAgents[n], m_arena.getGameName());
								if (!m_arena.hasTrainRights()) {
									m_arena.m_xab.tdPar[n].enableAll(false);
									m_arena.m_xab.ntPar[n].enableAll(false);									
								}									
							}
						}
					}
			);
		} // for (n)
		
	} // initGui()
	
	public void configureGui() {
		SolidBorder bord = new SolidBorder();
		this.enableButtons(true);
//--- this is now handled via hasTrainRights() ---
//		GameNumT.setEnabled(false);		// Arena allows no training / multi-training 
//		TrainNumT.setEnabled(false);	// (see ArenaTrain for enabling this)	
		Play.setEnabled(true);
		InspectV.setEnabled(true);
		Logs.setEnabled(true);

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
		
		// Why do we set only m_arena.taskState in the following ActionListeners and delegate
		// the call of the relevant functions to the big state-switch in m_arena.run()?
		// (a) Because the new JButtons do only react on disabling them, when the 
		// ActionListener is actually left (!) (b) Because of clearer separation GUI - Control
		
		for (int n=0; n<numPlayers; n++) {
			mParam[n].addActionListener(
					new ActionHandler(n)		// constructor copies n to member x
					{
						public void actionPerformed(ActionEvent e)
						{	
							m_arena.m_xab.m_numParamBtn = x;
							m_arena.taskState = Arena.Task.PARAM;
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
							m_arena.m_xab.m_numTrainBtn = x;
							// toggle m_arena.state between TRAIN and IDLE
							if (m_arena.taskState!=ArenaTrain.Task.TRAIN) {
								m_arena.taskState = ArenaTrain.Task.TRAIN;
								enableButtons(false);			// disable all buttons ...
								mTrain[x].setEnabled(true);		// ... but the TRAIN button
							} else {
								m_arena.taskState = ArenaTrain.Task.IDLE;
								enableButtons(true);
							}
						}
					}	
			);
		}

		MultiTrain.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						// toggle m_arena.state between MULTTRN and IDLE
						if (m_arena.taskState!=ArenaTrain.Task.MULTTRN) {
							m_arena.taskState = ArenaTrain.Task.MULTTRN;
							m_arena.setStatusMessage("Multitrain for agent X ...");
							enableButtons(false);			// disable all buttons ...
							MultiTrain.setEnabled(true);	// ... but the MultiTrain button
						} else {
							m_arena.taskState = ArenaTrain.Task.IDLE;
							m_arena.setStatusMessage("Done.");
							enableButtons(true);
						}
					}
				}	
		);


		Play.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						if (m_arena.taskState==ArenaTrain.Task.INSPECTV) {
							m_arena.taskState = ArenaTrain.Task.IDLE;
							// if Play button is pressed while being in InspectV-mode, 
							// store this in taskBefore:
							m_arena.taskBefore=ArenaTrain.Task.INSPECTV;
						}
						// toggle m_arena.state between PLAY and IDLE
						if (m_arena.taskState!=ArenaTrain.Task.PLAY) {
							m_arena.taskState = ArenaTrain.Task.PLAY;
							m_arena.setStatusMessage("Playing a game ...");
							enableButtons(false);		// disable all buttons ...
							Play.setEnabled(true);		// ... but the Play button
						} else {
							m_arena.taskState = ArenaTrain.Task.IDLE;
							m_arena.setStatusMessage("Done.");
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
						// toggle m_arena.state between INSPECTV and IDLE
						if (m_arena.taskState!=ArenaTrain.Task.INSPECTV) {
							m_arena.taskState = ArenaTrain.Task.INSPECTV;
							m_arena.setStatusMessage("Inspecting the value function ...");
							enableButtons(false);			// disable all buttons ...
							InspectV.setEnabled(true);		// ... but the InspectV button
							Play.setEnabled(true);			// ... and the Play button
						} else {
							m_arena.taskState = ArenaTrain.Task.IDLE;
							m_arena.setStatusMessage("Done.");
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
                            logManagerGUI = new LogManagerGUI(m_arena.logManager, m_arena.getGameBoard());
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
							m_arena.currentSleepDuration = source.getValue();
						//}
					}
				}
		);

//		NTupShowB.addActionListener(
//				new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						m_arena.taskState = ArenaTrainTTT.Task.INSPECTNTUP;
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
		q1.add(AgentX_L);			// "Agent Type:"
		q1.add(new Canvas());
		q1.add(new Canvas());
		q.add(q1);
		
		//
		// here comes the agent columns: Choice / Param- / Train-btn
		//
		JPanel jPanel = new JPanel();
		jPanel.setBackground(Types.GUI_BGCOLOR);
		if (numPlayers==1) q.add(jPanel);
		qcol = new JPanel[4][numPlayers];	// four color stripes: 2 for Param btn, 2 for Train btn
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
			if (m_arena.hasTrainRights()) {
				qplay.add(qtrain);
			} 
			q.add(qplay);	
			mParam[n].setVisible(true);		// see ArenaTrain for making them
			mTrain[n].setVisible(false);		// visible
		}
		if (numPlayers<3) q.add(jPanel);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		p1.setBackground(Types.GUI_BGCOLOR);
		
		if (m_arena.hasTrainRights()) {
			p1.add(GameNumL);
			p1.add(GameNumT);			
//			p1.add(new Canvas());		// WK: 	comment this out to let player-1 choicebox 
//			p1.add(new Canvas());		//		appear in full length (always)
		} else {
			for (int i=0; i<4; i++) p1.add(new Canvas());
		}

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		p2.setBackground(Types.GUI_BGCOLOR);

		p2.add(TrainNumL);
		p2.add(TrainNumT);
		p2.add(MultiTrain);
		p2.add(NTupShowB);		

		JPanel psv = new JPanel();
		psv.setBackground(Types.GUI_BGCOLOR);
		psv.setAlignmentX(CENTER_ALIGNMENT); // does not work
		psv.add(showValOnGB_L);
		psv.add(showValOnGB);
		
		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		p3.setBackground(Types.GUI_BGCOLOR);
	
		p3.add(Play);
		p3.add(psv);
		p3.add(Logs);
		p3.add(InspectV);

		JPanel ptp = new JPanel();
		ptp.setLayout(new GridLayout(0,1,10,10));	// rows,columns,hgap,vgap
		ptp.setBackground(Types.GUI_BGCOLOR);
		ptp.add(p1);
		if (m_arena.hasTrainRights()) {
			ptp.add(p2);			
		}
		ptp.add(p3);
		// adding to ptp three objects of equal height helps the layout manager to balance the height distribution
		
		JPanel delayPanel = new JPanel();
        delayPanel.setLayout(new BorderLayout(10,10));
        delayPanel.setBackground(Types.GUI_BGCOLOR);
        Delay.setBackground(Types.GUI_BGCOLOR);
		delayPanel.add(SleepDurationL, java.awt.BorderLayout.WEST);
		delayPanel.add(Delay,java.awt.BorderLayout.CENTER);
		
		setLayout(new BorderLayout(10,10));
		add(q,java.awt.BorderLayout.NORTH);
		add(ptp,java.awt.BorderLayout.CENTER);
		add(delayPanel,java.awt.BorderLayout.SOUTH);		
	} // configureGui()
	
	public void initTrain() {
		int n = choiceAgent.length;
		for (int i=0; i<n; i++) {
			choiceAgent[i].setEnabled(true);
			mParam[i].setEnabled(true);
			mParam[i].setText("Param "+ ((n==2)?Types.GUI_2PLAYER_NAME[i]:Types.GUI_PLAYER_NAME[i])); 
			mParam[i].setVisible(true);
			mTrain[i].setVisible(true);
		}
		GameNumT.setEnabled(true);	
		TrainNumT.setEnabled(true);		
		this.enableButtons(true);
	}

	public void colorStripes(Color[] stripeColor) {
		for (int n=0; n<qcol[0].length; n++) {
			for (int c=0; c<4; c++) {
				qcol[c][n].setBackground(stripeColor[n]);
				qcol[c][n].setPreferredSize(new Dimension(10,9));
			}
		}
	}
	
	public void enableButtons(boolean state) {
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
	
	public void destroy() {
		if (logManagerGUI!=null) logManagerGUI.close();
		if (htmlDisplay!=null) htmlDisplay.dispose();
	}

	public boolean getShowValueOnGameBoard() {
		return showValOnGB.isSelected();
	}
	
	public JComboBox getChoiceAgent(int i) {
		return choiceAgent[i];
	}
	
	public int getTrainNumber() {
		return Integer.parseInt(TrainNumT.getText());
	}
	public void setTrainNumberText(String text) {
		TrainNumT.setText(text);
	}
	
	public int getGameNumber() {
		return Integer.parseInt(GameNumT.getText());
	}
	public void setGameNumber(int gameNumber) {
		GameNumT.setText(""+gameNumber);
	}
	
	public void setGuiParamDefaults(int n, String agentName, String gameName) {		
		switch (agentName) {
		case "TD-Ntuple-2": 
		case "TD-Ntuple-3": 
		case "Sarsa":
			NTupShowB.setEnabled(true);	// enable this button only if agentName is an n-tuple agent
			break;
		default:
			NTupShowB.setEnabled(false);			
		}
	}
	
} // class XArenaButtonsGui
