package games;

import agentIO.LoadSaveGBG;
import controllers.AgentBase;
import controllers.HumanPlayer;
import controllers.MC.MCAgentN;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTSWrapper.ConfigWrapper;
import controllers.PlayAgent;
import games.Hex.HexTile;
import games.Hex.StateObserverHex;
import games.Sim.StateObserverSim;
import games.SimpleGame.StateObserverSG;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import gui.ArenaGui;
import gui.MessageBox;
import starters.GBGLaunch;
import tools.ScoreTuple;
import tools.Types;
import TournamentSystem.TSAgent;
import TournamentSystem.TSAgentManager;
import TournamentSystem.TSTimeStorage;
import TournamentSystem.tools.TSGameDataTransfer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * This class contains the GUI and the task dispatcher for the game. The GUI for
 * buttons and choice boxes is in {@link XArenaButtons}.
 * <p>
 * Run this class for example from {@code main} in {@link games.TicTacToe.ArenaTTT} or
 * {@link games.TicTacToe.ArenaTrainTTT} for the TicTacToe game.
 * 
 * @author Wolfgang Konen, TH Koeln
 */
//abstract public class Arena extends JFrame implements Runnable {
abstract public class Arena implements Runnable {
	public enum Task {
		PARAM, TRAIN, MULTTRN, PLAY, INSPECTV
		// , INSPECTNTUP 
		, COMPETE, SWAPCMP, ALLCMP, TRNEMNT, IDLE
	}

	public XArenaFuncs m_xfun;
	public GBGLaunch m_LauncherObj = null;
	public XArenaTabs m_tabs = null;
	public XArenaButtons m_xab; // the game buttons and text fields
	//	private Progress progress = null; // progress for some functions
	protected GameBoard gb;
	public LoadSaveGBG tdAgentIO; // saving/loading of agents
	public Task taskState = Task.IDLE;
	public Task taskBefore = Task.IDLE;
	// taskBefore is set to INSPECTV if Play button is hit while being in task INSPECTV
	// (see XArenaButtonsGui)

//	public JFrame m_ArenaFrame = null;
	public ArenaGui m_ArenaFrame = null;

	public int minSleepDuration = 0;
	public int maxSleepDuration = 2000;
	public int currentSleepDuration = 0;
	public LogManager logManager;
	private int logSessionid;
	private final boolean withUI;	// whether to start GUI or not

	// TS variables
	private final String TAG = "[Arena] ";
	private TSGameDataTransfer m_spDT = null;
	public TSAgentManager tournamentAgentManager = null;
	public boolean singlePlayerTSRunning = false;

	// decide via withUI whether wit UI or not
	public Arena(String title, boolean withUI) {
		this.withUI = withUI;
		initGame(title);
	}

	// see GBGLaunch
	public void setLauncherObj(GBGLaunch launcher) {
		m_LauncherObj = launcher;
	}
	
	/**
	 * called by constructors
	 */
	protected void initGame(String title) {
		// scale the GUI (window sizes and fonts of all GUI elements)
		if (withUI)
			Types.globalGUIScaling(true); // set to true to enable scaling

		gb = makeGameBoard();
		gb.setActionReq(false);

		m_xfun = new XArenaFuncs(this);
		m_xab = new XArenaButtons(m_xfun, this); // needs a constructed 'gb'
		tdAgentIO = new LoadSaveGBG(this, m_ArenaFrame);

		logManager = new LogManager();
		logManager.setSubDir(gb.getSubDir());

		if (withUI) {
			m_ArenaFrame = new ArenaGui(this, title);
			m_tabs = new XArenaTabs(this);
		} 
	}

	public void init() {
		// this causes Arena.run() to be executed as a separate thread
		Thread playThread = new Thread(this, "Arena playThread");
		playThread.start();
	}

	public void run() {
		String agentN, str;
		int n;
		int numPlayers = gb.getStateObs().getNumPlayers();
		double firstScore;
		String firstPlayer = (numPlayers==2) ? "X" : "P0";
		DecimalFormat frm = new DecimalFormat("#0.000");
		gb.showGameBoard(this, true);

		try {
			m_xfun.m_PlayAgents = m_xfun.fetchAgents(m_xab);
			// Ensure that m_PlayAgents has the agents selected, even if the
			// first thing is to issue a save agent command.
			// Ensure that the most recent parameters for this agent are fetched
			// from the params tab.
		} catch (RuntimeException e) {
			showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;
			setStatusMessage("Done.");
			// return;
		}

		while (true) {
			switch (taskState) {
			case PARAM: 
				n = m_xab.getNumParamBtn();
				agentN = m_xab.getSelectedAgent(n);
				setStatusMessage("Params for "+agentN+ " ...");
				if (m_tabs!=null) 		// this implies withUI==true (see initGame)
					m_tabs.showParamTabs(this,true,n,agentN);
				taskState = Task.IDLE; 
				break;
			case COMPETE:
				enableButtons(false);
				setStatusMessage("Running Single Compete ...");
//				if(numPlayers > 2)
//				{
//					allWinScores = m_xfun.singleCompete3(m_xab, gb);
//					enableButtons(true);
//					str = "Compete finished. Avg. score for all PLayer: ["+ allWinScores[0] + "," + allWinScores[1] + "," + allWinScores[2] + "]";
//					System.out.println(str);
//					setStatusMessage(str);
//					updateBoard();
//					taskState = Task.IDLE;
//					break;
//				}
				firstScore = m_xfun.singleCompete(m_xab, gb);

				enableButtons(true);
				str = "Compete finished. Avg. score for "+firstPlayer+": "+frm.format(firstScore)+" (from range [-1.0,1.0]),"
						+ " win rate: " + frm.format((firstScore+1)/2)+".";
				System.out.println(str);
				setStatusMessage(str);
				updateBoard();
				taskState = Task.IDLE;
				break;
			case SWAPCMP:
				enableButtons(false);
				setStatusMessage("Running Swap Compete ...");

				firstScore = m_xfun.swapCompete(m_xab, gb);

				enableButtons(true);
				str = "Swap Compete finished. Avg. score for X: "+frm.format(firstScore)+" (from range [-1.0,1.0]),"
						+ " win rate: " + frm.format((firstScore+1)/2)+".";
				System.out.println(str);
				setStatusMessage(str);
				updateBoard();
				taskState = Task.IDLE;
				break;
			case ALLCMP:
				enableButtons(false);
				setStatusMessage("Running Compete Both ...");

				firstScore = m_xfun.allCompete(m_xab, gb);

				enableButtons(true);
				str = "Compete All Roles finished. Avg. score for "+firstPlayer+": "+frm.format(firstScore)+" (from range [-1.0,1.0]),"
						+ " win rate: " + frm.format((firstScore+1)/2)+".";
				System.out.println(str);
				setStatusMessage(str);
				updateBoard();
				taskState = Task.IDLE;
				break;
			case TRNEMNT:    // Tournament Code
				tournamentAgentManager.lockToCompete(getGameBoard());
				tournamentAgentManager.setSettingsGUIElementsEnabled(false);

				RunTournament();

				// clean up
				System.out.println(TAG+"Tournament done, cleaning up");
				tournamentAgentManager.unlockAfterComp();
				tournamentAgentManager.setSettingsGUIElementsEnabled(true);
				tournamentAgentManager = null; // delete all data to prepare for next tournament
				taskState = Task.IDLE;
				break;
			case PLAY:
				enableButtons(false,true,false); 
				if (!singlePlayerTSRunning) {
					gb.showGameBoard(this, false);
					gb.clearBoard(false, true);
					
					PlayGame();
					
					gb.enableInteraction(false);
				}
				enableButtons(true);		
				break;
			case INSPECTV:
				enableButtons(false,true,true); 
				gb.showGameBoard(this, false);
				gb.clearBoard(false, true);
				gb.setActionReq(true);
				
				InspectGame();
				
				gb.enableInteraction(false);
				enableButtons(true);			
				break;
			case IDLE:
			default:
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
//				if (gb.getStateObs().isGameOver())
//					enableButtons(true);
			}

			performArenaDerivedTasks();
			// a derived class, e.g. ArenaTrain, may define additional tasks
			// in this method

		} // while (true)
	}
	
	public void destroy() {
		if (withUI) {
			m_ArenaFrame.setVisible(false);
			m_ArenaFrame.dispose();
			m_xab.destroy();
			m_tabs.destroy();
		}
		gb.destroy();
		m_xfun.destroy();
		if (this.m_LauncherObj==null) {
			System.exit(0);				
		} else {
			this.m_LauncherObj.launcherState = GBGLaunch.LaunchTask.STARTSELECTOR;
		}
	}

	protected void updateBoard() {
		gb.updateBoard(null, false, false);
	}

	/**
	 * Inspect the value function in m_PlayAgent[0] of {@link #m_xfun}. This
	 * agent will be initially the one set in {@link XArenaFuncs}'s constructor
	 * (usually {@link controllers.MCTS.MCTSAgentT}) or the agent last trained via
	 * button "Train X"
	 */
	protected void InspectGame() {
		String agentX = m_xab.getSelectedAgent(0);
		StateObservation so;
		Types.ACTIONS_VT actBest;
		PlayAgent paX;
		PlayAgent[] paVector, qaVector;
		int numPlayers = gb.getStateObs().getNumPlayers();

		boolean DBG_HEX = false;

		try {
			paVector = m_xfun.fetchAgents(m_xab);
			AgentBase.validTrainedAgents(paVector, numPlayers);
			qaVector = m_xfun.wrapAgents(paVector, m_xab, gb.getStateObs());
			paX = qaVector[0];
		} catch (RuntimeException e) {
			showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;
			setStatusMessage("Done.");
			return;
		}

		String sMsg = "Inspecting the value function of "+ agentX +" (X) ...";
		setStatusMessage(sMsg);
		System.out.println("[InspectGame] " + sMsg);
		
		if (paX instanceof HumanPlayer) {
			showMessage("No value function known for 'Human'!",
							"Warning", JOptionPane.WARNING_MESSAGE);
		}

		gb.clearBoard(true, true);
		gb.updateBoard(null, true, true); // update with reset
		gb.enableInteraction(true); // needed for CFour

		boolean stored_USELASTMCTS = ConfigWrapper.USELASTMCTS;
		ConfigWrapper.USELASTMCTS = false;
		so = gb.getStateObs().clearedCopy();

		while (taskState == Task.INSPECTV) {
			if (gb.isActionReq()) {
				gb.setActionReq(false);
				gb.enableInteraction(false);
				so = gb.getStateObs().clearedCopy();
				//so = gb.getStateObs();
				if (!so.isGameOver()&&so.isRoundOver()) so.initRound();

				// clearedCopy realizes a special treatment needed for RubiksCube: getNextAction2, which is called
				// below, skips from the available actions the inverse of the last action to avoid cycles of 2.
				// This boosts performance when playing or evaluating RubiksCube. But it is wrong on InspectV,
				// here we want to cover *ALL* available actions. By clearing the cube state we clear the last action
				// (set it to unknown).
				// For all other games, clearedCopy is identical to copy.

				if (DBG_HEX && (so instanceof StateObserverHex)) {
					StateObserverHex soh = (StateObserverHex) so;
					// int Index = this.getHexIndex(soh.getBoard());
					// System.out.println("Index: "+Index);
					System.out.println("[" + soh.stringDescr() + "]");
				}
				boolean DBG_SIM=false;
				if (DBG_SIM && so instanceof StateObserverSim) {
					StateObserverSim sos = (StateObserverSim) so;
					System.out.println(sos.stringDescr2());
				}


				if (so.isLegalState() && !so.isGameOver()) {
					actBest = paX.getNextAction2(so.partialState(), false, true);
					if (actBest != null) 	// a HumanAgent will return
											// actBest=null
						so.storeBestActionInfo(actBest);

					gb.updateBoard(so, true, true);
//					if (so.isRoundOver()) {
//						so.initRound();
//						assert !so.isRoundOver() : "Error: initRound() did not reset round-over-flag";
//					}
					if (so instanceof StateObserverSG && actBest.getVTable()!=null) {
						DecimalFormat form = new DecimalFormat("0.0000");
						double[] optimHit = {4.888888889,4.666666667,4.333333333,3.888888889,
								3.333333333,2.666666667,1.888888889,1,0 };
						System.out.println("StartState: "+so.stringDescr());
						System.out.println("optim HIT: "+form.format(optimHit[((StateObserverSG)so).get_sum()-1]));
						System.out.println("agent HIT: "+form.format(actBest.getVTable()[0])
											+", STAND: "+form.format(actBest.getVTable()[1]));
						break; // out of while, i.e. finish INSPECTV
					}
 				} else {
					if (so.stopInspectOnGameOver()) {
						gb.updateBoard(so, true, true);
						// not a valid play position >> show the board settings,
						// i.e.
						// the game-over position, but clear the values:
						gb.clearBoard(false, true);
//						this.enableButtons(true);
				
						break; // out of while, i.e. finish INSPECTV
					} else {
						// we get here e.g. in case RubiksCube where the initial state
						// in INSPECTV is usually the default (game-over, solved-cube)
						// state: In this case we only clear the action values, but
						// stay in INSPECTV
						gb.clearBoard(false, true);
					}
				}
				gb.enableInteraction(true);
			} else {
				try {
					Thread.sleep(100);
					// wait until an action in GameBoard gb occurs (see
					// ActionListener in GameBoardTTT::InitBoard() or
					// MyMouseListener
					// in GameBoardHex), which will set gb.isActionReq() to true
					// again.
				} catch (Exception e) {
				}
			}
			if (so.isRoundOver()) {
				gb.updateBoard(so, true, true);
				if (!so.isGameOver()) so.initRound();
				assert !so.isRoundOver() : "Error: initRound() did not reset round-over-flag";
			}

		} // while(taskState == Task.INSPECTV) [will be left only by the break
		  // above or when taskState changes]

		ConfigWrapper.USELASTMCTS = stored_USELASTMCTS;

		// We arrive here under three conditions:
		// 1) InspectV pressed again --> taskState changed to Task.IDLE
		// 2) game over && so.stopInspectOnGameOver()==true --> break in while
		// above
		// 3) Play button pressed --> taskState changed to Task.PLAY
		// In case 1)+2) we want to be in IDLE mode, but in case 3) we want to
		// stay in PLAY mode.
		// That is the reason for the if-clause in the next line:

		if (taskState != Task.PLAY) {
			// game over or InspectV pressed again --> leave the INSPECTV task
			taskState = Task.IDLE;
			setStatusMessage("Done.");
			// gb.enableInteraction(false);
		}
	}

	// only needed in case DBG_HEX for debugging the TDAgent in case 2x2 Hex
	private int getHexIndex(HexTile[][] board) {
		if (board[0].length != 2)
			throw new RuntimeException("getHexIndex only available for 2x2 board");

		int index = 0; // index into LUT = inputVector
		for (int i = 0, k = 1; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				int posValue = board[i][j].getPlayer() + 1;
				index += k * posValue;
				k *= 3;
			}
		}

		return index;
	}

	/**
	 * Play a game (using the agents selected in the combo boxes). One or
	 * multiple of them may be "Human". For 2-player games it is a game
	 * "X vs. O".
	 */
	public void PlayGame() {
		//System.out.println(TAG+"public void PlayGame()");
		PlayGame(null);
	}

	/**
	 * Play a game. Is called either via {@link Arena}'s Play button or from the Tournament System (TS). In 
	 * the TS-case, the game is a single-player game, since only then the tournament is a game play of 
	 * each agent and the results are recorded. <br>
	 * [In both cases we have {@code this.taskState == Task.PLAY}.]
	 * 
	 * @param spDT if {@code null} then {@code #PlayGame(TSGameDataTransfer)} was called via
	 * Play button, if not, it was called from the Tournament System.
	 */
	public void PlayGame(TSGameDataTransfer spDT) {
		m_spDT = spDT;
		final int numPlayers = gb.getStateObs().getNumPlayers();
		StateObservation so, startSO;
		Types.ACTIONS_VT actBest;
		MCTSAgentT p2 = null;
		boolean DEBG = false; // false;true;
		if (DEBG) p2= new MCTSAgentT("MCTS", null, m_xab.mctsPar[0], m_xab.oPar[0]); // only
																														// DEBG
		PlayAgent pa;
		int nEmpty = 0, cumEmpty = 0, highestTile=0;
		double gameScore = 0.0;
		PStats pstats;
		ArrayList<PStats> psList = new ArrayList<>();

		boolean showValue = m_xab.getShowValueOnGameBoard();
		boolean showValue_U = false;		// showValue for gb.updateBoard

		PlayAgent[] qaVector = fetchPlayAgents(numPlayers);
		if (qaVector==null) return;

		String[] agentVec = buildPlayMessages(qaVector,numPlayers);

		so = selectPlayStartState(showValue);

		assert qaVector.length == so.getNumPlayers() : "Number of agents does not match so.getNumPlayers()!";
		startSO = so.copy();
		pstats = new PStats(1, so.getMoveCounter(), so.getPlayer(), -1, gameScore, nEmpty, cumEmpty, highestTile);
		psList.add(pstats);

		logSessionid = logManager.newLoggingSession(so);

		//
		// start the Play-Game loop
		//
		try {
			while (taskState == Task.PLAY) 	// game play interruptible by hitting
											// 'Play' again
			{
				if (gb.isActionReq()) {
					so = gb.getStateObs();
					//if (!(so instanceof StateObserverSim))	// /WK/ test whether the following updateBoard is obsolete
					//	gb.updateBoard(so, false, showValue);
					pa = qaVector[so.getPlayer()];
					if (pa instanceof controllers.HumanPlayer) {
						gb.setActionReq(false);
						showValue_U = showValue;	// leave the previously shown values if it is HumanPlayer
					} else {
						gb.enableInteraction(false);

						if (DEBG) {
							int N_EMPTY = 4;
							actBest = getNextAction_DEBG(so.partialState(), pa, p2, N_EMPTY);
						} else {
							//long startT = System.currentTimeMillis();
							long startTNano = System.nanoTime();
							actBest = pa.getNextAction2(so.partialState(), false, false);
							//long endT = System.currentTimeMillis();
							long endTNano = System.nanoTime();
							//System.out.println("pa.getNextAction2(so.partialState(), false, true); processTime: "+(endT-startT)+"ms");
							//System.out.println("pa.getNextAction2(so.partialState(), false, true); processTime: "+(endTNano-startTNano)+"ns | "+(endTNano-startTNano)/(1*Math.pow(10,6))+"ms");
							if (m_spDT!=null)
								m_spDT.nextTimes[0].addNewTimeNS(endTNano-startTNano);
						}
						if (actBest==null) {
							// an exception occurred and was caught:
							System.out.println("Cannot play a game with "+pa.getName());
							taskState = Task.IDLE;
							setStatusMessage("Done.");
							return;
						}

						so.storeBestActionInfo(actBest);
						so.advance(actBest);
						logManager.addLogEntry(actBest, so, logSessionid);
						if (m_spDT==null) {		// the normal play (non-TS, i.e. no tournament)
							try {
								Thread.sleep(currentSleepDuration);
								// waiting time between agent-agent actions
							} catch (Exception e) {
								System.out.println("Thread 1");
							}
							showValue_U = showValue;
						} else {				// the TS-play case (single-player games):
							showValue_U = false;
						}

						// gather information for later printout to agents/gameName/csv/playStats.csv.
						// This is mostly for diagnostics in game 2048, but useful for other games as well.
						if (so instanceof StateObserver2048) {
							StateObserver2048 so2048 = (StateObserver2048) so;
							nEmpty = so2048.getNumEmptyTiles();
							cumEmpty += nEmpty;
							highestTile = so2048.getHighestTileValue();
							gameScore = so2048.getGameScore(so2048.getPlayer()) * so2048.MAXSCORE;
						} else {
							gameScore = so.getGameScore(so.getPlayer());
						}
						pstats = new PStats(1, so.getMoveCounter(), so.getPlayer(), actBest.toInt(), gameScore,
								nEmpty, cumEmpty, highestTile);
						psList.add(pstats);
						gb.enableInteraction(true);
					} // else (pa instanceof ...)

					if (!so.isRoundOver()) gb.updateBoard(so, false, showValue_U);
				} // if(gb.isActionReq())
				else {
					try {
						Thread.sleep(100);
						//
						// wait until an action in GameBoard gb occurs (see
						// ActionListener in InitBoard(), usually calling a
						// function HGameMove),
						// which will set gb.isActionReq() to true again.
					} catch (Exception e) {
						System.out.println("Thread 3");
					}
				}
				//if (gb.isActionReq()) gb.updateBoard(so, false, showValue_U);
				so = gb.getStateObs();
				if (so.isRoundOver()) {
					// this updateBoard on round-over is needed to fix the issue in Poker, where a human player ending
					// a round would miss the 'continue' button (because it comes from the else-branch (!gb.isActionReq())
					// above. With this updateBoard-call we are sure to pause in GameBoardGUI until 'continue' is pressed.
					// (To avoid calling updateBoard twice, we tag the call above with 'if (!so.isRoundOver()).)
					gb.updateBoard(so, false, showValue_U);

					if (!so.isGameOver()) {
						so.initRound();
						assert !so.isRoundOver() : "Error: initRound() did not reset round-over-flag";
					}
				}

				//
				// test two conditions to break out of the while-loop
				//
				if (so.isGameOver()) {
					String gostr = this.gameOverString(so,agentVec);
					this.gameOverMessages(so,numPlayers,gostr,showValue);

					break; // this is the 1st condition to break out of while loop
				} // if isGameOver

				if (so.getMoveCounter() > m_xab.oPar[0].getEpisodeLength()) {
					double gScore = so.getGameScore(so.getPlayer());
					if (so instanceof StateObserver2048)
						gScore *= StateObserver2048.MAXSCORE;
					showMessage("Game stopped (epiLength) with score " + gScore, "Game Over",
							JOptionPane.INFORMATION_MESSAGE);

					break; // this is the 2nd condition to break out of while loop
				} // if (so.getMoveCounter()...)

			} 	// while(taskState == Task.PLAY) [will be left only by one
				// of the last two break(s) above OR when taskState changes]
		} catch (RuntimeException e) {
			// a possible RuntimeException is raised when an agent for nondeterministic games
			// (ExpectimaxNAgent, MCTSExpectimax) is called with a (deterministic)
			// StateObservation object:
			e.printStackTrace();
			showMessage(e.getClass().getName() + ":" + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;
			setStatusMessage("Done.");
			return;
		}

		// game over - leave the Task.PLAY task:
		PStats.printPlayStats(psList, startSO, qaVector, this);
//		PStats.printHighTileStats(psList, startSO, qaVector, this);
		logManager.endLoggingSession(logSessionid);
		taskState = Task.IDLE;
		setStatusMessage("Done.");
	} // PlayGame(TSGameDataTransfer spDT)

	// fetch the agents in a way general for 1-, 2- and N-player games
	private PlayAgent[] fetchPlayAgents(int numPlayers) {
		PlayAgent[] paVector, qaVector;
		try {
			if (m_spDT == null) {    // regular non-TS game
				paVector = m_xfun.fetchAgents(m_xab);
				AgentBase.validTrainedAgents(paVector, numPlayers);
				qaVector = m_xfun.wrapAgents(paVector, m_xab, gb.getStateObs());
			} else {            // TS game
				if (m_spDT.standardAgentSelected) {
					// GBG standard agent
					paVector = m_xfun.fetchAgents(m_xab);
					AgentBase.validTrainedAgents(paVector, numPlayers);
					qaVector = m_xfun.wrapAgents(paVector, m_xab, gb.getStateObs());
				} else {
					// HDD agent
					paVector = m_spDT.getPlayAgents();
					qaVector = m_xfun.wrapAgents_TS(paVector, m_xab, gb.getStateObs());
				}
			}
		} catch (RuntimeException e) {
			showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;
			setStatusMessage("Done.");
			return null;
		}
		return qaVector;
	}


	// Build the messages shown at start of game play (helper for PlayGame(...))
	// Returns the string vector of all agent names.
	private String[] buildPlayMessages(PlayAgent[] qaVector, int numPlayers){
		String[] agentVec = new String[numPlayers];
		StringBuilder sMsg;

		if (m_spDT == null)
			gb.toFront();

		switch (numPlayers) {
			case (1) -> {
				agentVec[0] = m_xab.getSelectedAgent(0);
				sMsg = new StringBuilder("Playing a game ... [ " + agentVec[0] + " ]");
				int wrappedNPly = m_xab.oPar[0].getWrapperNPly();
				if (wrappedNPly > 0)
					sMsg = new StringBuilder("Playing a game ... [ " + agentVec[0] + ", nPly=" + wrappedNPly + " ]");
			}
			case (2) -> {
				agentVec[0] = qaVector[0].getName();
				agentVec[1] = qaVector[1].getName();
				sMsg = new StringBuilder("Playing a game ... [" + agentVec[0] + " (X) vs. " + agentVec[1] + " (O)]");
			}
			default -> {
				sMsg = new StringBuilder("Playing a game ... [");
				for (int n = 0; n < numPlayers; n++) {
					agentVec[n] = qaVector[n].getName();
					sMsg.append(agentVec[n]).append("(").append(n).append(")");
					if (n < numPlayers - 1)
						sMsg.append(", ");
				}
				sMsg.append("]");
			}
		}
		setStatusMessage(sMsg.toString());
		System.out.println(sMsg);

		return agentVec;
	}

	// Select the start state (helper for PlayGame(...))
	// The call to the gb methods causes gb.m_so to be set to the same state.
	private StateObservation selectPlayStartState(boolean showValue) {
		StateObservation so;
		boolean withReset;
		if (taskBefore == Task.INSPECTV) {
			// if taskBefore==INSPECTV, start from the board left by InspectV
			so = gb.getStateObs();
			withReset=true;
		} else {
			// if taskBefore!=INSPECTV, select here the start state:
			//gb.clearBoard(true, true); // reset game board to default start state
			so = gb.getDefaultStartState();
			//if (m_xab.oPar[0].getChooseStart01()) {
			// this is not recommended: an innocent start of Play where chooseStart01 in ParOther remains set
			// from a previous training will let the first agent make (silently) an unfortunate 1st move in
			//  half of the played games
			if (gb.getArena().getGameName().equals("RubiksCube")) {
				// On the other hand, chooseStartState is mandatory for the game RubiksCube (but may be a possible
				// option also for other games as well):
				// do not start from the default start state (solved cube), but
				// choose randomly a different one:
				so = gb.chooseStartState();
			}
			withReset=false;
		}
		gb.updateBoard(so, withReset, showValue);
		taskBefore = Task.IDLE;
		System.out.println("StartState: "+so.stringDescr());
		gb.setActionReq(true);
		gb.enableInteraction(true); // needed for CFour
		so.resetMoveCounter();

		if (m_spDT!=null) { // set random start moves for TS
			if (m_spDT.rndmStartMoves>0) {
				so = m_spDT.startSO;
				System.out.println(TAG+"RandomStartState set: "+so);
			}
		}

		return so;
	}

	// Display the game-over messages (helper for PlayGame(...))
	// and do a final call to gb.updateBoard(...).
	private void gameOverMessages(StateObservation so, int numPlayers, String gostr, boolean showValue){
		// for (agentVec[0]=="Human")-case: ensure to show the "Solved
		// in..." text in leftInfo:
		gb.updateBoard(so, false, showValue);
//					this.enableButtons(true);

		switch (numPlayers) {
			case 1:
				if (!singlePlayerTSRunning)
					showMessage(gostr, "Game Over", JOptionPane.INFORMATION_MESSAGE);
				break;
			case 2:
				gb.updateBoard(so, false, showValue);                // /WK/ really needed?
				showMessage(gostr, "Game Over", JOptionPane.INFORMATION_MESSAGE);

				gb.updateBoard(so, false, showValue);        // /WK/ really needed?
				if (withUI) m_ArenaFrame.repaint();            // /WK/ really needed?
				break;
			case 3:
				showMessage(gostr, "Game Over", JOptionPane.INFORMATION_MESSAGE);
				break;
			case 4:
				showMessage(gostr, "Game Over", JOptionPane.INFORMATION_MESSAGE);
				break;
			default:
				throw new RuntimeException("Case numPlayers = " + numPlayers + " not handled!");
		}
	}

	/**
	 * Build the game-over string (helper for {@link #PlayGame()}, to be shown in MessageBox).
	 * <p>
	 * If a game should require a special string here, it may override this function.
	 * 
	 * @param so			the game-over state 
	 * @param agentVec		the names of all agents
	 * @return	the game-over string
	 */
	public String gameOverString(StateObservation so, String[] agentVec) {
		ScoreTuple sc = so.getGameScoreTuple();

		// just debug C4:
//		System.out.println("[gameOverString] "+sc);
//		System.out.println("[gameOverString] isWin:"+((StateObserverC4)so).win());

		int numPlayers = so.getNumPlayers();
		StringBuilder goStr= new StringBuilder();
		int winner = 0;
		switch (numPlayers) {
			case 1 -> {
				double gScore = so.getGameScore(so.getPlayer());
				if (so instanceof StateObserver2048)
					gScore *= StateObserver2048.MAXSCORE;
				goStr = new StringBuilder("Game over: Score " + gScore);
				if (m_spDT != null)
					m_spDT.nextTeam[0].addSinglePlayScore(gScore);
			}
			case 2 -> {
				winner = sc.argmax();
				goStr = new StringBuilder(switch (winner) {
					case (0) -> "X (" + agentVec[0] + ") wins";
					case (1) -> "O (" + agentVec[1] + ") wins";
					default -> goStr.toString();
				});
				if (sc.max() == 0.0)
					goStr = new StringBuilder("Tie");
			}
			case 3 -> {
				// There are 4 possible game-over cases for 3 players:
				// 1) a single player wins			--> goStr = "Px (agtName) wins"
				// 2) a coalition of 2 player wins	--> goStr = "Px & Py win"
				// 3) a tie between two players, the remaining player has lost.
				//									--> goStr = "Tie between Px & Py"
				// 4) a tie between all players		--> goStr = "Tie
				System.out.println(sc.toString());        // just debug info

				// count the winners:
				int numWinners = 0;
				for (int i = 0; i < sc.scTup.length; i++) {
					if (sc.scTup[i] == 1) {
						if (numWinners == 0) goStr.append("P").append(i);
						else goStr.append(" & P").append(i);
						winner = i;
						numWinners++;
					}
				}
				goStr.append((numWinners == 1) ? " (" + agentVec[winner] + ") wins" : " win");

				// count the ties:
				if (sc.max() == 0.0) {
					goStr = new StringBuilder("Tie between ");
					int numTies = 0;
					for (int i = 0; i < sc.scTup.length; i++) {
						if (sc.scTup[i] == 0) {
							if (numTies == 0) goStr.append("P").append(i);
							else goStr.append(" & P").append(i);
							numTies++;
						}
					}
					if (numTies == 3) goStr = new StringBuilder("Tie");    // it's an all-player tie
				} // if
			}
			case 4 -> {
				// There are 4 possible game-over cases for 3 players:
				// 1) a single player wins			--> goStr = "Px (agtName) wins"
				// 2) a coalition of 2 player wins	--> goStr = "Px & Py win"
				// 3) a tie between two players, the remaining player has lost.
				//									--> goStr = "Tie between Px & Py"
				// 4) a tie between all players		--> goStr = "Tie


				// 1) a single player wins 			--> gotStr = "Px (agtName) wins"
				// 2) a coalition of 2 player wins
				// 3) a coalition of 3 player wins
				// 4) a tie between two players, the two remaining player have lost
				// 5) a tie between three players, the remaining player has lost
				// 6) a tie between all players
				// Selecting the number of winners
				int numWinners = 0;
				for (int i = 0; i < sc.scTup.length; i++) {
					if (sc.scTup[i] == 1) {
						if (numWinners == 0) goStr.append("P").append(i);
						else goStr.append(" & P").append(i);
						winner = i;
						numWinners++;
					}
				}
				goStr.append((numWinners == 1) ? " (" + agentVec[winner] + ") wins" : " win");
				if (sc.max() == 0.0) {
					goStr = new StringBuilder("Tie between ");
					int numTies = 0;
					for (int i = 0; i < sc.scTup.length; i++) {
						if (sc.scTup[i] == 0) {
							if (numTies == 0) goStr.append("P").append(i);
							else goStr.append(" & P").append(i);
							numTies++;
						}
					}
					if (numTies == 4) goStr = new StringBuilder("Tie");    // it's an all-player tie
				}

			}
			default -> throw new RuntimeException("Case numPlayers = " + numPlayers + " in gameOverString() not handled!");
		}
		
		return goStr.toString();
	}
	
	void RunTournament() {
		JFrame progressBarJF = new JFrame();
		progressBarJF.setSize(300, 100);
		progressBarJF.setTitle("TS Progress...");
		JPanel progressBarJP = new JPanel();
		// generate new JProgressBar object
		JProgressBar tsProgressBar = new JProgressBar(0, tournamentAgentManager.getTSProgress()[1]);
		tsProgressBar.setValue(0);
		// show the actual value as text in percent
		tsProgressBar.setStringPainted(true);
		progressBarJP.add(tsProgressBar);
		progressBarJF.add(progressBarJP);
		progressBarJF.setVisible(true);

		long start = System.currentTimeMillis();

		while (tournamentAgentManager.hasNextGame()) {
			TSAgent[] nextTeam = tournamentAgentManager.getNextCompetitionTeam(); // get next Agents
			//System.out.println("DEBUG: "+nextTeam[0].getAgentType() + " vs. "+nextTeam[1].getAgentType());
			TSTimeStorage[] nextTimes = tournamentAgentManager.getNextCompetitionTimeStorage(); // get timestorage for next game
			int rndmStartMoves = tournamentAgentManager.results.numberOfRandomStartMoves;
			StateObservation startSo = tournamentAgentManager.getNextStartState();
            TSGameDataTransfer data = new TSGameDataTransfer(nextTeam, nextTimes, rndmStartMoves, startSo);
			// let team compete...
			int roundWinningAgent = m_xfun.competeDispatcherTS(gb, m_xab, data);
			// enter winner
			if (roundWinningAgent > 40) {
				System.out.println(TAG+"ERROR :: competeDispatcherTS returned error value "+roundWinningAgent);
				if (roundWinningAgent == 44) {
					// hdd and standard agent mix, leave while, end tournament
					break;
				}
				if (roundWinningAgent == 43) {
					// a RuntimeException was thrown, leave while, end tournament
					break;
				}
			}
			else {
				tournamentAgentManager.enterGameResultWinner(roundWinningAgent); // 0=winAgent1 | 1=tie | 2=winAgent2
				//System.gc(); // call to keep system memory usage low but creates MASSIVE time delays every episode

				// iprogress bar
				int[] iprogress = tournamentAgentManager.getTSProgress();
				tsProgressBar.setValue(iprogress[0]);
				System.out.println(TAG+"TS Progress "+ Arrays.toString(iprogress));
			}
		}

		long end = System.currentTimeMillis();
		tournamentAgentManager.results.durationTSMS = end - start;

		progressBarJF.dispatchEvent(new WindowEvent(progressBarJF, WindowEvent.WINDOW_CLOSING)); // close progressbar window
		//tournamentAgentManager.printGameResults(); // print some stats to the console

		tournamentAgentManager.makeStats(); // calc data and create result stats window
		tournamentAgentManager.printGameResults(); // print some stats to the console

		if (tournamentAgentManager.getAutoSaveAfterTS()) {
			try {
				tdAgentIO.saveTSResult(tournamentAgentManager.results, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	} // RunTournament()

	/**
	 * For debugging 2048 during {@link #PlayGame()}: This function is only
	 * called if switch DEBG in source code of {@link #PlayGame()} is set to
	 * true and if the number of empty tiles is below a threshold. - It calls
	 * MCTSAgentT p2.getNextAction2() repeatedly and prints the vtable results
	 * on console. It calls PlayAgent pa (usually {@link MCAgentN}) repeatedly as well.
	 * In addition, it prints the best i and the number of rollouts (iterations)
	 * in which the game terminates.
	 * 
	 * Currently this function is only for StateObserver2048 so. Otherwise it
	 * returns the 'normal' pa.getNextAction2().
	 * 
	 * @param so		StateObserver2048 state
	 * @param pa		the 1st agent
	 * @param p2		the 2nd agent
	 * @param N_EMPTY	threshold for number of empty tiles: return the 'normal' pa.getNextAction2(),
	 *                  if number of empty tiles is greater or equal to N_EMPTY
	 * @return the chosen action from the last call of pa.getNextAction2()
	 */
	Types.ACTIONS_VT getNextAction_DEBG(StateObservation so, PlayAgent pa, MCTSAgentT p2, int N_EMPTY) {
		Types.ACTIONS_VT actBest=null;
		double MAXSCORE = 3932156;
		double[] vtable;
		int nEmpty;
		if (so instanceof StateObserver2048) {
			nEmpty = ((StateObserver2048) so).getNumEmptyTiles();
		} else {
			return pa.getNextAction2(so.partialState(), false, true);
		}
		if (nEmpty >= N_EMPTY)
			return pa.getNextAction2(so.partialState(), false, true);

		for (int k = 0; k < 3; k++) {
			actBest = p2.getNextAction2(so.partialState(), false, true);
			vtable = actBest.getVTable();
			System.out.print("p2 [" + p2.getName() + "]: ");
			double vbest = -Double.MAX_VALUE;
			int ibest = -1;
			for (int i = 0; i < so.getNumAvailableActions(); i++) {
				System.out.print(String.format("%.3f", vtable[i] * MAXSCORE) + " ");
				if (vtable[i] > vbest) {
					vbest = vtable[i];
					ibest = i;
				}
			}
			int nRolloutFinished = p2.getNRolloutFinished();
			int nIterations = p2.getNIterations();
			System.out.println(";  Best = " + ibest + ", Finished=" + nRolloutFinished + "/" + nIterations);
		}
		for (int k = 0; k < 2; k++) {
			actBest = pa.getNextAction2(so.partialState(), false, true);
			vtable = actBest.getVTable();
			System.out.print("pa [" + pa.getName() + "]: ");
			double vbest = -Double.MAX_VALUE;
			int ibest = -1;
			for (int i = 0; i < so.getNumAvailableActions(); i++) {
				System.out.print(String.format("%.3f", vtable[i] * MAXSCORE) + " ");
				if (vtable[i] > vbest) {
					vbest = vtable[i];
					ibest = i;
				}
			}
			if (pa instanceof MCAgentN) {
				int nRolloutFinished = ((MCAgentN) pa).getNRolloutFinished();
				int nIterations = ((MCAgentN) pa).getNIterations();
				System.out.println(";  Best = " + ibest + ", Finished=" + nRolloutFinished + "/" + nIterations);
			} else {
				System.out.println(";  Best = " + ibest);
			}
		}
		return actBest;
	}

	// We moved this method from XArenaMenu to Arena, because it can be used by batch 
	// facility GBGBatch as well. Therefore it should not be part of the GUI (which
	// XArenaMenu is)
	/**
	 * @param index   number of the player (agent)
	 */
	public boolean saveAgent(int index, String savePath) {
		int numPlayers = getGameBoard().getStateObs().getNumPlayers();
		boolean bstatus = false;
		try {
			// fetching the agents ensures that the actual parameters from the tabs
			// are all taken (!)
			m_xfun.m_PlayAgents = m_xfun.fetchAgents(m_xab);
			AgentBase.validTrainedAgents(m_xfun.m_PlayAgents,numPlayers);

		} catch (RuntimeException e) {
			this.showMessage( e.getMessage(), 
					"Error", JOptionPane.ERROR_MESSAGE);
			this.setStatusMessage("Done");
			return bstatus;
		}

		PlayAgent td = this.m_xfun.m_PlayAgents[index];
		String str;
		try {
			if (savePath==null) {
				this.tdAgentIO.saveGBGAgent(td);		// open file chooser dialog
			} else {
				this.tdAgentIO.saveGBGAgent(td,savePath);
			}
			str = "Saved Agent!";
			bstatus = true;
		} catch(IOException e) {
			str = e.getMessage();
		}
		this.setStatusMessage(str);
		System.out.println("[SaveAgent] "+str);
		return bstatus;
	}

	// We moved this method from XArenaMenu to Arena, because it can be used by batch 
	// facility GBGBatch as well. Therefore it should not be part of the GUI (which
	// XArenaMenu is)
	/**
	 * Load agent stored in <b>filePath</b> to player <b>n</b>
	 * 
	 * @param n   number of the player (agent) (0,...,numPlayers-1)
	 * @param filePath with ending {@code .agt.zip}
	 * @return true on success, false else
	 */
	public boolean loadAgent(int n, String filePath) {
		int numPlayers = this.getGameBoard().getStateObs().getNumPlayers();
		String str="";
		PlayAgent td=null;
		boolean res;
		try {
			td = this.tdAgentIO.loadGBGAgent(filePath);			
		} catch(Exception e) {
			str = e.getMessage();			
//		} catch(ClassNotFoundException e) {
//			str = e.getMessage();			
		} 
		
		if (td == null) {
			str = str + "\n No Agent loaded!";
			if (filePath==null) 	// MessageBox only when called via menu, NOT in batch mode (GBGBatch)
				this.showMessage("ERROR: " + str,
					"Load Error", JOptionPane.ERROR_MESSAGE);
			res = false;
		} else {
			// enable / disable certain parameter settings according to 
			// the agent name and the active game (before setting the specific
			// parameters in certain tabs with 'setFrom' from the agent loaded):
			this.m_xab.setParamDefaults(n, td.getName(), this.getGameName());
			// with changedViaLoad[n]=true we inhibit that a possible change in item state of 
			// this.m_xab.choiceAgent[n] will trigger from the associated 
			// ItemListenerHandler an agent-parameter-default-setting (we want the parameters
			// from the agent just loaded to survive in this.m_xab):
			this.m_xab.changedViaLoad[n] = true;
			
			td.fillParamTabsAfterLoading(n, this); 
			// td.fillParamTabsAfterLoading replaces the old, lengthy if ... else if ...
			
			if (td.isTrainable()) {
				// If it is one of the trainable agents: set maxGameNum and 
				// numEval according to the settings in the loaded agent
				// (at least maxGameNum is relevant for training): 
				this.m_xab.setGameNumber(td.getMaxGameNum());
				this.m_xab.oPar[n].setNumEval(td.getNumEval());
			}
//			if (td instanceof TDNTuple2Agt && TDNTuple2Agt.VER_3P) {
//				this.m_xab.tdPar[n].enableMode3P(true);
//				this.m_xab.tdPar[n].enableNPly(false);
//			} else {
//				this.m_xab.tdPar[n].enableNPly(false);
//			}
			
			// if called via Arena, then disable all actionable elements in all param tabs
			// "TD pars" and "NT par" (allow only viewing of parameters)
			if (!this.hasTrainRights()) {
				this.m_xab.tdPar[n].enableAll(false);
				this.m_xab.ntPar[n].enableAll(false);
			}

			
			// set selector according to class loaded:
			this.m_xab.setSelectedAgent(n, td.getName());
			
			this.m_xfun.m_PlayAgents[n] = td;
			String strAgent = (numPlayers==2) ? "Agent-"+Types.GUI_2PLAYER_NAME[n] :
												"Agent-"+Types.GUI_PLAYER_NAME[n];
			str = "Agent "+td.getName()+" succesfully loaded to "
				+ strAgent + "!";
			res = true;
		}
		this.setStatusMessage(str);
		System.out.println("[LoadAgent] "+str);
		return res;
	}

	public boolean hasGUI() {
		return withUI;
	}
	
	/**
	 * @return true, if there is at least one human agent in the game
	 */
	public boolean hasHumanAgent() {
		PlayAgent[] paVector2 = m_xfun.fetchAgents(m_xab);
		for (PlayAgent playAgent : paVector2) {
			if (playAgent.getName().equals("Human"))
				return true;
		}
		return false;
	}

	public boolean hasTrainRights() {
		return false;
	}
	
	public GameBoard getGameBoard() {
		return gb;
	}

	public LogManager getLogManager() {
		return logManager;
	}

	public int getLogSessionID() {
		return logSessionid;
	}

//	public StatusBar getStatusBar() {
//		return statusBar;
//	}

	public void setStatusMessage(String msg) {
		if (m_ArenaFrame!=null)
			m_ArenaFrame.setStatusMessage(msg);
	}

	public void setTitle(String title) {
		if (m_ArenaFrame!=null)
			m_ArenaFrame.setTitle(title);
	}

	// overridden by ArenaTrain
	public int getGuiArenaHeight() {
		return Types.GUI_ARENA_HEIGHT;
	}
	
//	// @Override
//	public void setProgress(tools.Progress p) {
//		this.progress = p;
//	}

	public void enableButtons(boolean state) {
		m_xab.enableButtons(state, state, state);
	}

//	public void enableButtons(boolean state, boolean playEnabled) {
//		m_xab.enableButtons(state, playEnabled, state);
//	}

	public void enableButtons(boolean state, boolean playEnabled, boolean inspectVEnabled) {
		m_xab.enableButtons(state, playEnabled, inspectVEnabled);
	}

	abstract public String getGameName();

	/**
	 * Factory pattern method: make a new GameBoard
	 *
	 * @return the game board
	 */
	abstract public GameBoard makeGameBoard();

	/**
	 * Factory pattern method: make a new Evaluator
	 *
	 * @param pa
	 *            the agent to evaluate
	 * @param gb
	 *            the game board
	 * @param stopEval
	 *            the number of successful evaluations needed to reach the
	 *            evaluator goal (may be used during training to stop it
	 *            prematurely)
	 * @param mode
	 *            which evaluator mode. 
	 * @param verbose
	 *            how verbose or silent the evaluator is
	 * @return the evaluator
	 */
	abstract public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose);

	/**
	 * Factory pattern method: make a new {@link Feature} tailored to a specific
	 * game. <br>
	 * (We delegate this task to derived classes ArenaXYZ, since they usually
	 * require a game-tailored FeatureXYZ.)
	 * <p>
	 * 
	 * If the derived class does not override {@code #makeFeatureClass(int)},
	 * the default behavior is to throw a {@link RuntimeException}.
	 * 
	 * @param featmode
	 *            feature mode
	 * @return an object implementing {@link Feature}
	 */
	public Feature makeFeatureClass(int featmode) {
		throw new RuntimeException("No Feature class available for game " + this.getGameName() + " (needed for TDS)");
	}

	/**
	 * Factory pattern method: make a new {@link XNTupleFuncs} tailored to a
	 * specific game. <br>
	 * (We delegate this task to derived classes ArenaXYZ, since they usually
	 * require a game-tailored XNTupleFuncsXYZ.)
	 * <p>
	 * 
	 * If the derived class does not override {@code #makeXNTupleFuncs()}, the
	 * default behavior is to throw a {@link RuntimeException}.
	 * 
	 * @return an object implementing {@link XNTupleFuncs}
	 */
	public XNTupleFuncs makeXNTupleFuncs() {
		throw new RuntimeException(
				"No XNTupleFuncs class available for game " + this.getGameName() + " (needed for TD-Ntuple)");
	}

	/**
	 * This method is called from {@link #run()} and it has to be overridden by
	 * classes derived from {@link Arena} (e.g. {@link ArenaTrain}).
	 * <p>
	 * 
	 * It allows to add additional tasks to the task switch. May be an empty
	 * method if no tasks have to be added.
	 * <p>
	 * 
	 * This method will use member {@code taskState} from {@link Arena}. It
	 * performs several actions appropriate for the derived class and -
	 * importantly - changes taskState back to IDLE (when appropriate).
	 * 
	 * @see ArenaTrain
	 */
	abstract public void performArenaDerivedTasks();

	public void showMessage(String message, String title, int msgCode) {
		if (withUI) {
			MessageBox.show(this.m_ArenaFrame,message,title,msgCode);
		} else {
			if (msgCode==JOptionPane.ERROR_MESSAGE) {
				System.err.println("["+title+"] "+message);
			} else {
				System.out.println("["+title+"] "+message);
			}
		}
	}
	

}
