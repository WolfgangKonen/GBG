package games;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import params.*;
//import params.RpropParams;
//import params.TCParams;
import tools.HtmlDisplay;
import tools.Types;


/**
 * Helper class for {@link Arena} and {@link ArenaTrain}: <ul> 
 * <li> sets the buttons, selectors and text fields,
 * <li> sets initial values for all relevant fields,
 * <li> has the action code for Param-, Train-, MultiTrain-, Play-, and Compete- button events.
 * </ul>
 * 
 * @author Wolfgang Konen, TH K�ln, Apr'08-Nov'16
 */
public class XArenaButtons extends JPanel		
{
	private static final long serialVersionUID = 1L;
	XArenaFuncs 		m_xfun;	
	public Arena	 	m_game;	// a reference to the ArenaTTT object passed in with the constructor
	XArenaButtons		m_xab;
	public OptionsComp 		winCompOptions = new OptionsComp();
	//private Random rand;
	int numPlayers;
	int m_numParamBtn;			// number of the last param button pressed
	int m_numTrainBtn;			// number of the last train button pressed

	JButton[] mParam;
	JButton[] mTrain;
	JButton MultiTrain;
	JButton Play;
	JButton InspectV;
	//JButton TDparB;			// now in TicTacToeTabs
	//JButton CMAparB;
	//JButton TCparB;
	//JButton OparB;
	JButton NTupShowB;
	JSlider Delay;				// The Sleep Slider
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
	TDParams tdPar = new TDParams();
	NTParams ntPar = new NTParams();
//	RpropParams rpPar = new RpropParams();
//	TCParams tcPar= new TCParams();
//	CMAParams cmaPar = new CMAParams();
//	NTupleShow ntupleShow = null;
	MCTSParams mctsParams = new MCTSParams();
	MCParams mcParams = new MCParams();
	OtherParams oPar = new OtherParams(5);
	HtmlDisplay htmlDisplay = null;
	boolean htmlDisplayActive = false;
	boolean isNTupShowEnabled = false;

	/**
	 * This class is needed for each ActionListener of {@code mParam[i]} and
	 * {@code mTrain[i]} in {@link XArenaButtons} constructor
	 *
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
		
		m_xfun = game;
		m_game = arena;
		m_xab = this; 

		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		mParam = new JButton[numPlayers];
		mTrain = new JButton[numPlayers];
		choiceAgent = new Choice[numPlayers];
		assert (numPlayers<=Types.GUI_PLAYER_NAME.length) 
			: "GUI not configured for "+numPlayers+" players. Increase Types.GUI_PLAYER_NAME and GUI_AGENT_INITIAL";
		
		// 
		// initial settings for the GUI
		//
		AgentX = Types.GUI_X_PLAYER;  // "MCTS"; "TDS"; "CMA-ES"; "Minimax" 
		AgentO = Types.GUI_O_PLAYER;  // "Human";"ValIt";
		GameNumT=new TextField("10000", 5); //("10000", 5);
		TrainNumT=new TextField("25", 5);
		//CompeteNumT=new TextField("3", 5);
		//CompetitionsT=new TextField("1", 5);

		MultiTrain=new JButton("MultiTrain");
		Play=new JButton("Play");
		InspectV=new JButton("Inspect V");
		NTupShowB = new JButton("Insp Ntuples");
		Delay = new JSlider(JSlider.HORIZONTAL, m_game.minSleepDuration,m_game.maxSleepDuration, m_game.currentSleepDuration);
		GameNumL = new Label("Train Games");
		TrainNumL = new Label("Agents trained");
		AgentX_L = new Label("Agent Type: ");
		//CompeteG_L = new Label("Games/Comp:");
		//Competitions_L = new Label("Competitions:");
        SleepDurationL = new Label("Sleep duration");
		
		for (int n=0; n<numPlayers; n++) {
			choiceAgent[n] = new Choice();
			for (String s : Types.GUI_AGENT_LIST) choiceAgent[n].add(s);
			choiceAgent[n].select(Types.GUI_AGENT_INITIAL[n]);	
			this.setParamDefaults(Types.GUI_AGENT_INITIAL[n], m_game.getGameName());
			if (numPlayers==2) {
				mParam[n]=new JButton("Param "+Types.GUI_2PLAYER_NAME[n]);
				mTrain[n]=new JButton("Train "+Types.GUI_2PLAYER_NAME[n]);
			} else {
				mParam[n]=new JButton("Param "+Types.GUI_PLAYER_NAME[n]);
				mTrain[n]=new JButton("Train "+Types.GUI_PLAYER_NAME[n]);
			}
			mParam[n].setForeground(Color.black);
			mTrain[n].setForeground(Color.black);
			mParam[n].setBackground(Color.orange);
			mTrain[n].setBackground(Color.orange);
			
			choiceAgent[n].setEnabled(false);		
			// Arena does not allow user to modify choice boxes (see ArenaTrain)
			
			// whenever one of the agent choice boxes changes, call setParamDefaults
			// to set the param tabs to sensible defaults for that agent and that game
			choiceAgent[n].addItemListener(
					new ItemListenerHandler(n) { // this constructor will copy n to ItemListenerHandler.n
						public void itemStateChanged(ItemEvent arg0) {
							setParamDefaults(choiceAgent[n].getSelectedItem(),
									m_game.getGameName());
						}
					}
			);

		}
		this.enableButtons(false);
		GameNumT.setEnabled(false);		// Arena allows no training / multi-training 
		TrainNumT.setEnabled(false);	// (see ArenaTrain for enabling this)	
		Play.setEnabled(true);
		InspectV.setEnabled(true);
		// Arena enables only button Play & Inspect V

		MultiTrain.setForeground(Color.black);
		Play.setForeground(Color.white);
		InspectV.setForeground(Color.white);
//		TDparB.setForeground(Color.white);
//		CMAparB.setForeground(Color.white);
//		OparB.setForeground(Color.white);
		NTupShowB.setForeground(Color.white);
		GameNumT.setForeground(Color.black);
		GameNumL.setForeground(Color.black);
        SleepDurationL.setForeground(Color.black);

//		MultiTrain.setBackground(Color.lightGray);
		Play.setBackground(Color.blue);
		InspectV.setBackground(Color.blue);
//		TDparB.setBackground(Color.blue);
//		CMAparB.setBackground(Color.blue);
//		TCparB.setBackground(Color.GREEN);//samine//
//		OparB.setBackground(Color.blue);
		NTupShowB.setBackground(Color.blue);
		GameNumT.setBackground(Color.white);
		//GameNumL.setBackground(Color.white);

		Delay.setMajorTickSpacing(250);
		Delay.setMinorTickSpacing(50);
		Delay.setPaintTicks(true);
		Delay.setPaintLabels(true);
		
		
		NTupShowB.setEnabled(false);	// enable this button only if AgentX is an N-Tuple Player
		
		// Why do we set only m_game.state in the following ActionListeners and delegate
		// the call of the relevant functions to the big state-switch in TicGame.run()?
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
							enableButtons(false);
							InspectV.setEnabled(true);	
						} else {
							m_game.taskState = ArenaTrain.Task.IDLE;
							m_game.setStatusMessage("Done.");
							enableButtons(true);
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
		JPanel q1 = new JPanel();
		q1.setLayout(new GridLayout(0,1,10,10));
		q1.add(AgentX_L);
		q1.add(new Canvas());
		q1.add(new Canvas());
		q.add(q1);
		
		if (numPlayers==1) q.add(new JPanel());
		for (int n=0; n<numPlayers; n++) {
			JPanel qplay = new JPanel();
			qplay.setLayout(new GridLayout(0,1,10,10));
			qplay.add(choiceAgent[n]);
			qplay.add(mParam[n]);
			qplay.add(mTrain[n]);
			q.add(qplay);	
			mParam[n].setVisible(false);		// see ArenaTrain for making them
			mTrain[n].setVisible(false);		// visible
		}
		if (numPlayers<3) q.add(new JPanel());
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		
		p1.add(GameNumL);
		p1.add(GameNumT);
		p1.add(new Canvas());
		p1.add(new Canvas());

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap

		p2.add(TrainNumL);
		p2.add(TrainNumT);
		p2.add(new Canvas());
		p2.add(new Canvas());

		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
	
		/*  --- These button functions are now in the OptionsComp winCompOptions ---
		p3.add(CompeteG_L);
		p3.add(CompeteNumT);
		p3.add(Competitions_L);	
		p3.add(CompetitionsT); 
		*/

		p3.add(Play);
		p3.add(MultiTrain);
		p3.add(NTupShowB);		
		p3.add(InspectV);

		JPanel ptp = new JPanel();
		ptp.setLayout(new GridLayout(0,1,10,10));	// rows,columns,hgap,vgap
		ptp.add(p1);
		ptp.add(p2);
		ptp.add(p3);
		// adding to ptp three objects of equal height helps the layout manager to balance the height distribution
		
		/*  --- These button functions are now in the menu ---
		p.add(Compete);
		p.add(SwapCompete);
		p.add(MultiCompete);
		p.add(new Canvas());
		 */


		JPanel delayPanel = new JPanel();
        delayPanel.setLayout(new BorderLayout(10,10));
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

	public void setParamDefaults(String agentName, String gameName) {
		tdPar.setParamDefaults(agentName, gameName);
		ntPar.setParamDefaults(agentName, gameName);
		
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
//		TDparB.setEnabled(state);
//		TCparB.setEnabled(state);//samine//
//		CMAparB.setEnabled(state);
//		OparB.setEnabled(state);
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

	public String getSelectedAgent(int i){
		return choiceAgent[i].getSelectedItem();
	}
	public void setSelectedAgent(int i, String str){
		choiceAgent[i].select(str);
	}
	
} // class XArenaButtons	
