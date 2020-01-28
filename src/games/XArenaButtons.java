package games;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import TournamentSystem.TSAgent;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import gui.XArenaButtonsGui;
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
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class XArenaButtons extends JPanel		
{
	int trainNumber = 25;
	int gameNumber = 10000;

	private static final long serialVersionUID = 1L;
	XArenaFuncs 		m_xfun;
	public Arena	 	m_arena;	// a reference to the Arena object passed in with the constructor
	public OptionsComp 	winCompOptions = new OptionsComp(); // window with Competition Options
	public XArenaButtonsGui m_XAB_gui = null;
	int numPlayers;
	public int m_numParamBtn;			// number of the last param button pressed
	public int m_numTrainBtn;			// number of the last train button pressed

	// with changedViaLoad[n]=true we inhibit that a change in item state of
	// m_arena.m_xab.choiceAgent[n] due to agent loading will trigger from the associated
	// ItemStateListener an agent-parameter-default-setting (we want the parameters
	// from the agent just loaded to survive in m_arena.m_xab)
	public boolean[] changedViaLoad = null;

	public TDParams[] tdPar;
	public NTParams[] ntPar;
	public MaxNParams[] maxnParams;
	public MCTSParams[] mctsParams;
	public MCParams[] mcParams;
	public MCTSExpectimaxParams[] mctseParams;
	public OtherParams[] oPar;
	public EdaxParams[] edParams;
	public String[] selectedAgents;

	// tournament system remote data input
	private boolean tournamentRemoteDataEnabled = false;
	private String selectedAgentTypes[] = null;

	public XArenaButtons(XArenaFuncs game, Arena arena)
	{
		String AgentX;
		String AgentO;
		
		m_xfun = game;
		m_arena = arena;
		
		if (m_arena.withUI)
			m_XAB_gui = new XArenaButtonsGui(arena);

		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		changedViaLoad = new boolean[numPlayers];	// implicitly set to false
		assert (numPlayers<=Types.GUI_PLAYER_NAME.length) 
			: "GUI not configured for "+numPlayers+" players. Increase Types.GUI_PLAYER_NAME and GUI_AGENT_INITIAL";
		tdPar = new TDParams[numPlayers];
		ntPar = new NTParams[numPlayers];
		maxnParams = new MaxNParams[numPlayers];
		mctsParams = new MCTSParams[numPlayers];
		mcParams = new MCParams[numPlayers];
		mctseParams = new MCTSExpectimaxParams[numPlayers];
		oPar = new OtherParams[numPlayers];
		edParams = new EdaxParams[numPlayers];
		selectedAgents = new String[numPlayers];
		AgentX = null;
		AgentO = null;

		// for-loop over *decrementing* n so that we set on the last pass (n=0) with the call 
		// to setParamDefaults the initial GameNumT for the Player at position 0 (which is the usual one to train)
		for (int n=numPlayers-1; n>=0; n--) {
			selectedAgents[n] = Types.GUI_AGENT_INITIAL[n];
			
			tdPar[n] = new TDParams();
			ntPar[n] = new NTParams();
			maxnParams[n] = new MaxNParams();
			mctsParams[n] = new MCTSParams();
			mcParams[n] = new MCParams();
			mctseParams[n] = new MCTSExpectimaxParams();
			oPar[n] = new OtherParams();
			edParams[n] = new EdaxParams();
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
				oPar[n].setQuickEvalList(dummyEvaluator.getAvailableModes());
				oPar[n].setTrainEvalList(dummyEvaluator.getAvailableModes());
				oPar[n].setQuickEvalMode(dummyEvaluator.getQuickEvalMode());
				oPar[n].setTrainEvalMode(dummyEvaluator.getTrainEvalMode());
				oPar[n].setQuickEvalTooltip(dummyEvaluator.getTooltipString());
				oPar[n].setTrainEvalTooltip(dummyEvaluator.getTooltipString());
			} catch (RuntimeException ignored){ 
				System.out.println(ignored.getMessage());				
			}

		} // for

		if (m_arena.withUI)
			m_XAB_gui.configureGui();
		
		
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
		ntPar[n].setFixedCoList(m_arena.makeXNTupleFuncs().getAvailFixedNTupleModes(),
								m_arena.makeXNTupleFuncs().fixedTooltipString());
		mctsParams[n].setParamDefaults(agentName, gameName, numPlayers);
		mctseParams[n].setParamDefaults(agentName, gameName, numPlayers);
		maxnParams[n].setParamDefaults(agentName, gameName, numPlayers);
		oPar[n].setParamDefaults(agentName, gameName);

		switch (agentName) {
		case "TDS":
		case "TD-Ntuple-2": 
		case "TD-Ntuple-3": 
			setGameNumber(10000);				
			break;
		case "Sarsa":
			switch (gameName) {
			case "Nim": 
				setGameNumber(10000);		
				break;
			default:
				setGameNumber(30000);		
			}
			break;
		default:
			setGameNumber(10000);				
		}
		setTrainNumber(25);

		if (m_XAB_gui!=null)
			m_XAB_gui.setParamDefaults(n, agentName, gameName);
	}
	
	public void helpFunction() {
		if (m_XAB_gui != null)
			m_XAB_gui.helpFunction();
	}

	// Known caller outside XArenaButtons: Arena.run()
	void enableButtons(boolean state) {
		if (m_XAB_gui != null)
			m_XAB_gui.enableButtons(state);
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
		if (!tournamentRemoteDataEnabled)
//			return (String) m_XAB_gui.getChoiceAgent(i).getSelectedItem();
			return this.selectedAgents[i];
		else {
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
	public void enableTournamentRemoteData(TSAgent team[]) {
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

	/**
	 * disable externally set agents
	 */
	public void disableTournamentRemoteData() {
		tournamentRemoteDataEnabled = false;
		selectedAgentTypes = null;
	}

	public int getTrainNumber() {
		return trainNumber;
	}

	public void setTrainNumber(int trainNumber) {
		this.trainNumber = trainNumber;
		if (this.m_XAB_gui!=null)
			m_XAB_gui.setTrainNumberText(""+trainNumber);
	}
	public void setTrainNumberText(int trainNumber, String text) {
		this.trainNumber = trainNumber;
		if (this.m_XAB_gui!=null)
			m_XAB_gui.setTrainNumberText(text);
	}
	
	public int getGameNumber() {
		return gameNumber;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
		if (this.m_XAB_gui!=null)
			m_XAB_gui.setGameNumber(gameNumber);
	}
	
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
		edParams[n].setFrom( parEdax );
	}
	public void setMaxNParFrom(int n, ParMaxN parMaxN) {
		maxnParams[n].setFrom( parMaxN );
	}
	public void setMaxNDepthFrom(int n, int depth) {
		maxnParams[n].setMaxnDepth(depth);
	}
	public void setMcParFrom(int n, ParMC parMC) {
		mcParams[n].setFrom( parMC );
	}
	public void setMctsParFrom(int n, ParMCTS parMcts) {
		mctsParams[n].setFrom( parMcts );
	}
	public void setMctseParFrom(int n, ParMCTSE parMctse) {
		mctseParams[n].setFrom( parMctse );
	}
} // class XArenaButtons	
