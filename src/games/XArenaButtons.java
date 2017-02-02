package games;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import params.MCTSParams;
//import params.RpropParams;
//import params.TCParams;
import params.TDParams;
import params.OptionsComp;
import tools.HtmlDisplay;
import tools.Types;
import params.OtherParams;



/**
 * Helper class for {@link Arena} and {@link ArenaTrain}: <ul> 
 * <li> sets the buttons, selectors and text fields (everything below the TicTacToe board),
 * <li> sets initial values for all relevant fields,
 * <li> has the action code for Param-, Train-, MultiTrain-, Play-, and Compete- button events.
 * </ul>
 * 
 * @author Wolfgang Konen, TH Köln, Apr'08-Nov'16
 */
public class XArenaButtons extends JPanel		
{
	private static final long serialVersionUID = 1L;
	XArenaFuncs 		m_xfun;	
	public Arena	 	m_game;	// a reference to the ArenaTTT object passed in with the constructor
	XArenaButtons		m_xab;
	OptionsComp 		winCompOptions = new OptionsComp();
	//private Random rand;
	int numPlayers;
	int m_numParamBtn;			// number of the last param button pressed
	int m_numTrainBtn;			// number of the last train button pressed

	JButton[] mParam;
	JButton[] mTrain;
	JButton MultiTrain;
	JButton Play;
	JButton InspectV;
	//JButton TDparB;		// now in TicTacToeTabs
	//JButton CMAparB;
	//JButton TCparB;
	//JButton OparB;
	JButton NTupShowB;
	TextField GameNumT;
	TextField TrainNumT;
	TextField CompeteNumT;
	TextField CompetitionsT;
	Label GameNumL;
	Label TrainNumL;
	Label AgentX_L;
	Label Competitions_L;
	Label CompeteG_L;
	Choice[] choiceAgent; 
	TDParams tdPar = new TDParams();
//	RpropParams rpPar = new RpropParams();
//	TCParams tcPar= new TCParams();
//	CMAParams cmaPar = new CMAParams();
//	NTupleShow ntupleShow = null;
	MCTSParams mcPar= new MCTSParams();
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
		GameNumT=new TextField("1000", 5); //("10000", 5);
		TrainNumT=new TextField("25", 5);
		CompeteNumT=new TextField("3", 5);
		CompetitionsT=new TextField("1", 5);

		MultiTrain=new JButton("MultiTrain");
		Play=new JButton("Play");
		InspectV=new JButton("Inspect V");
		NTupShowB = new JButton("Insp Ntuples");
		GameNumL = new Label("Train Games");
		TrainNumL = new Label("Agents trained");
		AgentX_L = new Label("Agent Type: ");
		CompeteG_L = new Label("Games/Comp:");
		Competitions_L = new Label("Competitions:");
		
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
			mParam[n].setForeground(Color.black);
			mTrain[n].setForeground(Color.black);
			mParam[n].setBackground(Color.orange);
			mTrain[n].setBackground(Color.orange);
			
			choiceAgent[n].setEnabled(false);		
			// Arena does not allow user to modify choice boxes (see ArenaTrain)
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
		
		
		//added for NTuple-Systems
		choiceAgent[0].addItemListener(
				new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						if(choiceAgent[0].getSelectedItem().equals("TDS-NTuple")) {
							tdPar.LinNetType.setEnabled(false);
							tdPar.BprNetType.setEnabled(false);
							tdPar.wo_SigType.setEnabled(false);
							tdPar.withSigType.setEnabled(false);
						}
						else {
							tdPar.LinNetType.setEnabled(true);
							tdPar.BprNetType.setEnabled(true);
							tdPar.wo_SigType.setEnabled(true);
							tdPar.withSigType.setEnabled(true);
						}
						if(choiceAgent[0].getSelectedItem().equals("TDS-NTuple")
						  |choiceAgent[0].getSelectedItem().equals("TDS-NTuple-2")
						  |choiceAgent[0].getSelectedItem().equals("TD_NT")) {
							NTupShowB.setEnabled(true);	// enable this button only if AgentX is an N-Tuple Player							
						}
						else {
							NTupShowB.setEnabled(false);
							
						}
					}
				}
		);
		NTupShowB.setEnabled(false);	// enable this button only if AgentX is an N-Tuple Player
		
		// Why do we set only m_game.state in the following ActionListeners and delegate
		// the call of the relevant functions to the big state-switch in TicGame.run()?
		// (a) Because the new JButtons do only react on disabling them, when the 
		// ActionListener is actually left (!) (b) Because of clearer separation GUI - Control
		
		for (int n=0; n<numPlayers; n++) {
			mParam[n].addActionListener(
					new ActionHandler(n)		// constructor copies n to member x
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
		
		JPanel tp = new JPanel();
		tp.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
		
		tp.add(GameNumL);
		tp.add(GameNumT);
		tp.add(new Canvas());
		tp.add(new Canvas());

		tp.add(TrainNumL);
		tp.add(TrainNumT);
		tp.add(MultiTrain);
		tp.add(new Canvas());

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0,4,10,10));		// rows,columns,hgap,vgap
	
		p.add(CompeteG_L);
		p.add(CompeteNumT);
		p.add(Competitions_L);	
		p.add(CompetitionsT); 

		p.add(Play);
		p.add(new Canvas());
		p.add(NTupShowB);		
		p.add(InspectV);

		JPanel ptp = new JPanel();
		ptp.setLayout(new GridLayout(0,1,10,10));	// rows,columns,hgap,vgap
		ptp.add(tp);
		ptp.add(p);
		
		/*  --- These button functions are now in the menu ---
		p.add(Compete);
		p.add(SwapCompete);
		p.add(MultiCompete);
		p.add(new Canvas());
		 */
		
		//JPanel s = new JPanel();
		//s.setLayout(new GridLayout(0,1,10,10));		// rows,columns,hgap,vgap (1 column = allow long messages)
		m_game.setStatusMessage("Init done.");
		
		setLayout(new BorderLayout(10,10));
		add(q,java.awt.BorderLayout.NORTH);
		add(ptp,java.awt.BorderLayout.CENTER);
		//add(s,java.awt.BorderLayout.SOUTH);
		
		// size of window: see LaunchArenaTTT::init() or LaunchAppletTTT::init()

	} // constructor XArenaButtons

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
