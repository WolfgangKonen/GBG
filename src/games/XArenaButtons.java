package games;

import java.awt.*;
import TournamentSystem.TSAgent;
import gui.XArenaButtonsGui;
import params.*;
import tools.Types;


/**
 * Helper class for {@link Arena} and {@link ArenaTrain}: <ul> 
 * <li> sets the buttons, selectors and text fields,
 * <li> sets initial values for all relevant fields,
 * <li> has the action code for Param-, Train-, MultiTrain-, and Play-button events.
 * </ul>
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class XArenaButtons //extends JPanel		
{
	int trainNumber = 25;
	int gameNumber = 10000;
	int competeNumber = 3;

	XArenaFuncs 		m_xfun;
	public Arena	 	m_arena;	// a reference to the Arena object passed in with the constructor
	public OptionsComp 	winCompOptions = null; 
	public XArenaButtonsGui m_XAB_gui = null;
	int numPlayers;
	public int m_numParamBtn;			// number of the last param button pressed
	public int m_numTrainBtn;			// number of the last train button pressed

	// with changedViaLoad[n]=true we inhibit that a change in item state of
	// m_arena.m_xab.choiceAgent[n] due to agent loading will trigger from the associated
	// ItemListenerHandler an agent-parameter-default-setting (we want the parameters
	// from the agent just loaded to survive in m_arena.m_xab)
	public boolean[] changedViaLoad;

	public ParTD[] tdPar;
	public ParNT[] ntPar;
	public ParOther[] oPar;
	public ParMaxN[] maxnPar;
	public ParMCTS[] mctsPar;
	public ParMC[] mcPar;
	public ParMCTSE[] mctsePar;
	public ParEdax[] edPar;
	public String[] selectedAgents;

	// tournament system remote data input
	private boolean tournamentRemoteDataEnabled = false;
	private String[] selectedAgentTypes = null;

	public XArenaButtons(XArenaFuncs game, Arena arena)
	{

		m_xfun = game;
		m_arena = arena;
		
		if (m_arena.hasGUI()) {
			m_XAB_gui = new XArenaButtonsGui(arena);
			winCompOptions = new OptionsComp(competeNumber); // window with Competition Options
		}

		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		changedViaLoad = new boolean[numPlayers];	// implicitly set to false
		assert (numPlayers<=Types.GUI_PLAYER_NAME.length) 
			: "GUI not configured for "+numPlayers+" players. Increase Types.GUI_PLAYER_NAME and GUI_AGENT_INITIAL";
		tdPar = new ParTD[numPlayers];
		ntPar = new ParNT[numPlayers];
		oPar = new ParOther[numPlayers];
		maxnPar = new ParMaxN[numPlayers];
		mctsPar = new ParMCTS[numPlayers];
		mcPar = new ParMC[numPlayers];
		mctsePar = new ParMCTSE[numPlayers];
		edPar = new ParEdax[numPlayers];
		selectedAgents = new String[numPlayers];

		// for-loop over *decrementing* n so that we set on the last pass (n=0) with the call 
		// to setParamDefaults the initial GameNumT for the Player at position 0 (which is the usual one to train)
		for (int n=numPlayers-1; n>=0; n--) {
			selectedAgents[n] = Types.GUI_AGENT_INITIAL[n];
			
			tdPar[n] = new ParTD(m_arena.hasGUI());
			ntPar[n] = new ParNT(m_arena.hasGUI());
			oPar[n] = new ParOther(m_arena.hasGUI(),m_arena);
			maxnPar[n] = new ParMaxN(m_arena.hasGUI());
			mctsPar[n] = new ParMCTS(m_arena.hasGUI());
			mcPar[n] = new ParMC(m_arena.hasGUI());
			mctsePar[n] = new ParMCTSE(m_arena.hasGUI());
			edPar[n] = new ParEdax(m_arena.hasGUI());
			this.setParamDefaults(n, Types.GUI_AGENT_INITIAL[n], m_arena.getGameName());
			
			try {
				Feature dummyFeature = m_arena.makeFeatureClass(0); 
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
				Evaluator dummyEvaluator = m_arena.makeEvaluator(null, null, 0, 0, 0); 
				// Why is it a dummyEvaluator? - It is an Evaluator of the specific game, so it knows
				// which modes are available, what the tooltip string is and so on. But it is 'dummy'
				// w.r.t. mode, which is here set to 0. Once a mode is selected and an evaluation 
				// process is started, a new Evaluator with the selected mode will be constructed.
				if (dummyEvaluator!=null) {
					oPar[n].setQuickEvalList(dummyEvaluator.getAvailableModes());
					oPar[n].setTrainEvalList(dummyEvaluator.getAvailableModes());
					oPar[n].setQuickEvalMode(dummyEvaluator.getQuickEvalMode());
					oPar[n].setTrainEvalMode(dummyEvaluator.getTrainEvalMode());
					oPar[n].setQuickEvalTooltip(dummyEvaluator.getTooltipString());
					oPar[n].setTrainEvalTooltip(dummyEvaluator.getTooltipString());
				}
			} catch (RuntimeException e){
				System.out.println(e.getMessage());
			}

		} // for

		if (m_arena.hasGUI()) {
			m_XAB_gui.configureGui();
			
			if (m_arena.getGameName().equals("ConnectFour")) {
		    	Color[] colPlayer= new Color[2];
				colPlayer[0] = new Color(238,208,26);	// dark yellow as in resources/yellow.png
				colPlayer[1] = new Color(204,0,0);		// dark red as in resources/red.png    		
		    	colorStripes(colPlayer);
			}
		}
		
		m_arena.setStatusMessage("Init done.");
		
		// infoPanel (StatusMessage) is in Arena.java
		
		// size of window: see Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT

	} // constructor XArenaButtons

	public void initTrain() {
		if (m_XAB_gui != null)
			m_XAB_gui.initTrain();
	}

	/**
	 * @param n				number of agent
	 * @param agentName		name of agent
	 * @param gameName		name of game 
	 */
	public void setParamDefaults(int n, String agentName, String gameName) {
		tdPar[n].setParamDefaults(agentName, gameName);
		ntPar[n].setParamDefaults(agentName, gameName);
		ntPar[n].setFixedCoList(m_arena.makeXNTupleFuncs().fixedNTupleModesAvailable(),
								m_arena.makeXNTupleFuncs().fixedTooltipString());
		mctsPar[n].setParamDefaults(agentName, gameName, numPlayers);
		mctsePar[n].setParamDefaults(agentName, gameName, numPlayers);
		maxnPar[n].setParamDefaults(agentName, gameName, numPlayers);
		oPar[n].setParamDefaults(agentName, gameName);

		switch (agentName) {
		case "TDS":
//		case "TD-Ntuple-2":
		case "TD-Ntuple-3": 
			setGameNumber(10000);				
			break;
		case "Sarsa":
			if ("Nim".equals(gameName)) {
				setGameNumber(10000);
			} else {
				setGameNumber(30000);
			}
			break;
		default:
			setGameNumber(10000);				
		}
		setTrainNumber(25);

		if (m_XAB_gui!=null)
			m_XAB_gui.setGuiParamDefaults(agentName);
	}

	// --- never used ---
//	public void helpFunction() {
//		if (m_XAB_gui != null)
//			m_XAB_gui.helpFunction();
//	}

	// Known caller outside XArenaButtons: Arena.run()
	void enableButtons(boolean state, boolean playEnabled, boolean inspectVEnabled) {
		if (m_XAB_gui != null)
			m_XAB_gui.enableButtons(state,playEnabled,inspectVEnabled);
	}
	
//	/** 
//	 *  This helper function is used by {@link ArenaTrainTTT#InspectNtup()}
//	 */
//	public void nTupShowAction() {
//		if (ntupleShow!=null) {
//			ntupleShow.setVisible(!ntupleShow.isVisible());
//			// place window Ntuple Show on the right side of the main window, below TD params window
//			int x = m_arena.getLocation().x + m_arena.getWidth() + 8;
//			int y = m_arena.getLocation().y;
//			if (m_arena.m_TicFrame!=null) {
//				x = m_arena.m_TicFrame.getLocation().x + m_arena.m_TicFrame.getWidth() + 1;
//				y = m_arena.m_TicFrame.getLocation().y + tdPar.getHeight();
//			}
//			ntupleShow.setLocation(x,y);
//		}
//		
//	}

	public int getNumPlayers() {
		return this.numPlayers;
	}
	
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
		if (m_XAB_gui == null) return false;
		return m_XAB_gui.getShowValueOnGameBoard();
	}

	public String getSelectedAgent(int i){
		if (!tournamentRemoteDataEnabled) {
			if (m_XAB_gui!=null)		// bug fix to get the initial choice box selection
				this.selectedAgents[i] = (String) m_XAB_gui.getChoiceAgent(i).getSelectedItem();
			return this.selectedAgents[i];
		} else {
			return selectedAgentTypes[i];
		}
	}

	public void setSelectedAgent(int i, String str){
		this.selectedAgents[i] = str;
		if (m_XAB_gui!=null)
			m_XAB_gui.getChoiceAgent(i).setSelectedItem(str);
	}

	/**
	 * externally set playing agents
	 * @param team standard agents chosen by user
	 */
	public void enableTournamentRemoteData(TSAgent[] team) {
		tournamentRemoteDataEnabled = true;

		String[] types;
		if (numPlayers==1) {
		    types = new String[]{team[0].getAgentType()};
        } else {
            types = new String[2];
            types[0] = team[0].getAgentType();
            types[1] = team[1].getAgentType();

            // create a dummy standard agent to be set into the GUI to let HDD and standard agents play together
            if (team[0].isHddAgent() && !team[1].isHddAgent()) {
                types[0] = Types.GUI_AGENT_LIST[0]; // random agent to hide HDD agent
            }
            if (!team[0].isHddAgent() && team[1].isHddAgent()) {
                types[1] = Types.GUI_AGENT_LIST[0];
            }
        }

		selectedAgentTypes = types;
	}
	
	public void colorStripes(Color[] stripeColor) {
		if (m_XAB_gui!=null) 
			m_XAB_gui.colorStripes(stripeColor);
	}


	/**
	 * disable externally set agents
	 */
	public void disableTournamentRemoteData() {
		tournamentRemoteDataEnabled = false;
		selectedAgentTypes = null;
	}

	public void destroy() {
		if (m_XAB_gui!=null) {
			m_XAB_gui.destroy();
		}
		if (winCompOptions != null) {
			winCompOptions.setVisible(false);
			winCompOptions.dispose();
		}
	}

	public Point getLocation() {
		if (m_XAB_gui!=null)
			return m_XAB_gui.getLocation();
		return null;
	}

	public int getWidth() {
		if (m_XAB_gui!=null)
			return m_XAB_gui.getWidth();
		return 0;
	}

	public int getX() {
		if (m_XAB_gui!=null)
			return m_XAB_gui.getX();
		return 0;
	}

	public int getTrainNumber() {
		if (m_XAB_gui!=null)
			trainNumber = m_XAB_gui.getTrainNumber(); // be sure to get latest change from GUI (!)
		return trainNumber;
	}

	public void setTrainNumber(int trainNumber) {
		this.trainNumber = trainNumber;
		if (m_XAB_gui!=null)
			m_XAB_gui.setTrainNumberText(""+trainNumber);
	}
	
	// to allow for text = "1/25" (first run out of 25)
	public void setTrainNumberText(int trainNumber, String text) {
		this.trainNumber = trainNumber;
		if (m_XAB_gui!=null)
			m_XAB_gui.setTrainNumberText(text);
	}
	
	public int getGameNumber() {
		if (m_XAB_gui!=null)
			gameNumber = m_XAB_gui.getGameNumber(); // be sure to get latest change from GUI (!)
		return gameNumber;
	}
	
	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
		if (m_XAB_gui!=null)
			m_XAB_gui.setGameNumber(gameNumber);
	}

	// --- never used ---
//	public int getCompeteNumber() {
//		if (winCompOptions!=null)
//			competeNumber = winCompOptions.getNumGames(); // be sure to get latest change from GUI (!)
//		return competeNumber;
//	}
//
//	public void setCompeteNumber(int competeNumber) {
//		competeNumber = competeNumber;
//		if (winCompOptions!=null)
//			winCompOptions.setNumGames(competeNumber);
//	}
	
	//
	// setter functions to make the param members accessible from PlayAgent.fillParamTabsAfterLoading
	//
	public void setTdParFrom(int n, ParTD parTD) {
		tdPar[n].setFrom( parTD );
	}
	public void setNtParFrom(int n, ParNT parNT) {
		ntPar[n].setFrom( parNT );
	}
	public void setOParFrom(int n, ParOther parOther) {
		oPar[n].setFrom( parOther );
	}
	public void setEdaxParFrom(int n, ParEdax parEdax) {
		edPar[n].setFrom( parEdax );
	}
	public void setMaxNParFrom(int n, ParMaxN parMaxN) {
		maxnPar[n].setFrom( parMaxN );
	}
	public void setMaxNDepthFrom(int n, int depth) {
		maxnPar[n].setMaxNDepth(depth);
	}
	public void setMcParFrom(int n, ParMC parMC) {
		mcPar[n].setFrom( parMC );
	}
	public void setMctsParFrom(int n, ParMCTS parMcts) {
		mctsPar[n].setFrom( parMcts );
	}
	public void setMctseParFrom(int n, ParMCTSE parMctse) {
		mctsePar[n].setFrom( parMctse );
	}
} // class XArenaButtons	
