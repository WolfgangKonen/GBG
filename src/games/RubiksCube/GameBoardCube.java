package games.RubiksCube;

import java.util.Random;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.GameBoard;
import games.GameBoardBase;
import games.RubiksCube.gui.*;
import games.StateObservation;
import games.Arena;
import tools.Types;

/**
 * This class implements the GameBoard interface for RubiksCube.
 * Its member {@link GameBoardCubeGui} {@code m_gameGui} has the game board GUI.
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoardCubeGui}'s
 * action handlers
 * 
 * @author Wolfgang Konen, TH Koeln, 2018-2020
 */
public class GameBoardCube extends GameBoardBase implements GameBoard {

	private transient GameBoardCubeGui m_gameGui = null;
	
	protected Random rand;
	protected Random rand2;
	/**
	 * The representation of the state corresponding to the current 
	 * board position.
	 */
        public StateObserverCube stateObserverCube;
	private final StateObserverCube def = new StateObserverCube();

	private String lastScrambleSequence = "";
	private String lastSolutionSequence = "";

	public GameBoardCube(Arena arena) {
		super(arena);
		this.initialize();
		
	}
	
	/**
	 * called by constructor and prior to each training run
	 */
	@Override
	public void initialize() {
		//		long seed = 999;
		// 		rand 		= new Random(seed);
		rand 		= new Random(System.currentTimeMillis());
		rand2 		= new Random(2*System.currentTimeMillis());
		stateObserverCube = new StateObserverCube();	// solved cube

		if (getArena().hasGUI() && m_gameGui==null) {
			if (CubeConfig.visualizationType == CubeConfig.VisualizationType.TWOD) {
				switch (CubeConfig.cubeSize) {
					case POCKET -> m_gameGui = new GameBoardCubeGuiPocket(this);
					case RUBIKS -> m_gameGui = new GameBoardCubeGuiRubik(this);
				}
			} else {
				switch (CubeConfig.cubeSize) {
					case POCKET -> m_gameGui = new GameBoardCubeGuiPocket3D(this);
					case RUBIKS -> m_gameGui = new GameBoardCubeGuiRubik3D(this);
				}
			}
		}
	}

	/**
	 * update game-specific parameters from {@link Arena}'s param tabs
	 */
	@Override
	public void updateParams() {
		if (getArena().m_xab!=null) {
			// fetch the most actual values from tab "Other Pars"
			CubeConfig.pMin = getArena().m_xab.oPar[0].getpMinRubiks();
			CubeConfig.pMax = getArena().m_xab.oPar[0].getpMaxRubiks();
			if (CubeConfig.pMin<1) CubeConfig.pMin=1;
			CubeConfig.REPLAYBUFFER = getArena().m_xab.oPar[0].getReplayBuffer();
			CubeConfig.EvalNmax = getArena().m_xab.oPar[0].getNumEval();

			CubeConfig.stepReward = getArena().m_xab.tdPar[0].getStepReward();
			CubeConfig.REWARD_POSITIVE = getArena().m_xab.tdPar[0].getRewardPositive();
		}
	}

	public String getLastScrambleSequence() {
		return lastScrambleSequence;
	}

	public String getLastSolutionSequence() {
		return lastSolutionSequence;
	}

	public void setLastSequences(String scramble, String solution) {
		this.lastScrambleSequence = scramble;
		this.lastSolutionSequence = solution;
	}

	public void updateLastSequences(StateObserverCube so) {
		if (so != null) {
			this.lastScrambleSequence = so.getScrambleSequence();
			this.lastSolutionSequence = so.getMoveSequence();
		}
	}

	// --- should be obsolete now, we use updateParams and have pMax on CubeConfig.pMax ---
//	public int getPMax() {
//		if (getArena().m_xab!=null) {
//			// fetch the most actual values from tab "Other Pars"
//			CubeConfig.pMin = getArena().m_xab.oPar[0].getpMinRubiks();
//			CubeConfig.pMax = getArena().m_xab.oPar[0].getpMaxRubiks();
//			if (CubeConfig.pMin<1) CubeConfig.pMin=1;
//			CubeConfig.REPLAYBUFFER = getArena().m_xab.oPar[0].getReplayBuffer();
//		}
//        return CubeConfig.pMax;
//	}

	public void setPMin(int pMin) {
		if (m_gameGui!=null) {
			m_gameGui.setPMin(pMin);
		}
	}

	public void setPMax(int pMax) {
        if (m_gameGui!=null) {
        	m_gameGui.setPMax(pMax);
        }
	}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
		if (boardClear) {
			stateObserverCube = new StateObserverCube();			// solved cube
		}
							// considerable speed-up during training (!)
        if (m_gameGui!=null && getArena().taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
	}

	@Override
	public void setStateObs(StateObservation so) {
		if (so!=null) {
			assert (so instanceof StateObserverCube)
					: "StateObservation 'so' is not an instance of StateObserverCube";
			StateObserverCube soN = (StateObserverCube) so;
			stateObserverCube = soN;//.copy();
		}
	}

	/**
	 * Update the play board and the associated VBoard.
	 * 
	 * @param so	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean withReset, boolean showValueOnGameboard)  {
		setStateObs(so);    // asserts that so is StateObserverCube

		updateParams();
		
		if (m_gameGui!=null) {
			this.setPMin(getArena().m_xab.oPar[0].getpMinRubiks());  		// update GUI's pMin from oPar
			this.setPMax(getArena().m_xab.oPar[0].getpMaxRubiks());  		// update GUI's pMax from oPar
			m_gameGui.updateBoard((StateObserverCube)so, withReset, showValueOnGameboard);
		}

		
	}

	public void HGameMove(int x, int y)
	{
		String[] twiStr = {"U","L","F","D","R","B"};
		System.out.println(twiStr[x]+(y+1));
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert stateObserverCube.isLegalAction(act) : "Desired action is not legal";
		stateObserverCube.advance(act, null);			// perform action (optionally add random elements)
		System.out.println(stateObserverCube.stringDescr());
		(getArena().getLogManager()).addLogEntry(act, stateObserverCube, getArena().getLogSessionID());
		updateBoard(null,false,false);
		setActionReq(true);			// ask Arena for next action
	}
	
	public void InspectMove(int x, int y)
	{
		String[] twiStr = {"U","L","F","D","R","B"};
		System.out.println(twiStr[x]+(y+1));
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!stateObserverCube.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			getArena().setStatusMessage("Desired action is not legal");
			return;
		} else {
			getArena().setStatusMessage("Inspecting the value function ...");
		}
		stateObserverCube.advance(act, null);			// perform action (optionally add random elements from game
									// environment - not necessary in RubiksCube)
		stateObserverCube.getCubeState().clearLast();		// clear lastTwist and lastTimes of the CubeState,
		stateObserverCube.setAvailableActions();				// then set the available actions which causes all
												// numAllActions actions to be added to m_so.acts. We need this
												// to see the values for all numAllActions actions.
												// (If lastTwist were set, 3 actions would be excluded
												// which we do not want during INSPECTV.) 
		updateBoard(null,false,false);
		setActionReq(true);
	}
	
	public StateObservation getStateObs() {
		return stateObserverCube;
	}

	/**
	 * @return the 'empty-board' start state
	 * @param cmpRand
	 */
	@Override
	public StateObservation getDefaultStartState(Random cmpRand) {
		clearBoard(true, true, null);
		return stateObserverCube;
	}

	/**
	 * Choose a random start state when playing a game.
	 * 
	 * @return a random start state by twisting the solved cube p times. 
	 *         If the game is played with GUI, p is set according to selector "Scrambling twists" of the game board.
	 *         If this selector is set to "RANDOM" or if the game is played w/o GUI, p from
	 *         { {@link CubeConfig#pMin}, ..., {@link CubeConfig#pMax} } is picked randomly.
	 *      
	 * @see Arena#PlayGame()
	 */
	@Override
	public StateObservation chooseStartState() {
		int p = CubeConfig.pMin+rand.nextInt(CubeConfig.pMax-CubeConfig.pMin+1);	// random p \in {pMin,...,pMax}
		if (m_gameGui!=null) {
			String str=m_gameGui.getScramblingTwists();
			if (!str.equals("RANDOM")) p = Integer.parseInt(str);
		}
		return chooseStartState(p);
	}
	
	/**
	 * Choose a start state by scrambling the default cube via {@link #selectByTwists1(int) selectByTwists1(p)}.
	 * @param p		number of twists
	 * @return	a scrambled cube
	 */
	public StateObservation chooseStartState(int p) {		
		clearBoard(true, true, null);			// m_so is in default start state
		stateObserverCube = selectByTwists1(p);

		// StateObserverCubeCleared is important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!). It also resets moveCounter
		//m_so = new StateObserverCubeCleared(m_so,p);
		stateObserverCube = (StateObserverCube) stateObserverCube.clearedCopy();
		
		//System.out.println("p = "+p+",  "+m_so.getCubeState().twistSeq);
		return stateObserverCube;
	}


	/**
	 * Choose a random start state when training for an episode. Return a start state depending 
	 * on {@code pa}'s {@link PlayAgent#getGameNum()} and {@link PlayAgent#getMaxGameNum()} 
	 * by randomly twisting the default cube p times (deprecated: by selecting from the distance sets D[p]). 
	 * <p>
	 * Use {@link #selectByTwists1(int)}.<br>
	 * Which p to take? - Select randomly p from 1,2,...,{@link CubeConfig#pMax}.
	 * <p>
	 *
	 * @param 	pa the agent to be trained, we need it here only for its {@link PlayAgent#getGameNum()} 
	 * 			and {@link PlayAgent#getMaxGameNum()}
	 * @return 	the start state for the next training episode
	 *      
	 * @see PlayAgent#trainAgent(StateObservation)   
	 * @see TDNTuple3Agt#trainAgent(StateObservation) 
	 */
	@Override
	public StateObservation chooseStartState(PlayAgent pa) {
		int p;
		clearBoard(true, true, null);			// m_so is in default start state
		p = 1+rand.nextInt(CubeConfig.pMax);
		// since rand.nextInt(K) selects from {0,...,K-1}, we have p from {1,...,pMax}
		stateObserverCube = selectByTwists1(p);

		// StateObserverCubeCleared is important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!)
		//m_so = new StateObserverCubeCleared(m_so,p);
		stateObserverCube = (StateObserverCube) stateObserverCube.clearedCopy();
		return stateObserverCube;
	}
	
	/** 
	 * Method to select a start state by doing p random twist on the default cube. 
	 * This may be the only way to select a start state being p=8,9,... twists away from the 
	 * solved cube (where the distance set D[p] becomes to big). 
	 * <p>
	 * But it has the caveat that p random twists do not guarantee to produce a state in D[p]. 
	 * Due to twins etc. the resulting state may be actually in D[p-1], D[p-2] and below.
	 * However, it works quickly for arbitrary p.
	 */
	// Modify in GameBoardCube.java, selectByTwists1 method
	protected StateObserverCube selectByTwists1(int p) {
		int index;
		boolean cond;
		StateObserverCube so = new StateObserverCube();
		StringBuilder scrambleSequence = new StringBuilder();
		int attempts = 0;

		while (so.isEqual(def)) {
			attempts++;
			if (attempts % 1000 == 0) {
				System.err.println("[selectByTwists1] no cube different from default found -- may be p=0?? p=" + p);
			}

			// Clear the previous scramble attempt
			scrambleSequence.setLength(0);

			switch (CubeConfig.twistType) {
				case HTM:
					for (int k = 0; k < p; k++) {
						do {
							index = rand.nextInt(so.getAvailableActions().size());
							cond = !CubeConfig.TWIST_DOUBLETS && (index / 3 == so.getCubeState().lastTwist.ordinal() - 1);
						} while (cond);

						// Record the move in the scramble sequence
						Types.ACTIONS action = so.getAction(index);
						int iAction = action.toInt();
						int j = iAction % 3;
						int i = (iAction - j) / 3;
						String[] faces = {"U", "L", "F", "D", "R", "B"};
						String[] modifiers = {"", "2", "'"};  // Empty string for single turn, 2 for double, ' for counter-clockwise
						scrambleSequence.append(faces[i]).append(modifiers[j]).append(" ");

						so.advance(action, null);
					}
					break;
				case QTM:
					for (int k = 0; k < p; k++) {
						do {
							index = rand.nextInt(so.getAvailableActions().size());
							cond = !CubeConfig.TWIST_DOUBLETS && (index / 3 == so.getCubeState().lastTwist.ordinal() - 1 &&
                                                                (index % 3 + 1) != so.getCubeState().lastTimes);
						} while (cond);

						// Record the move in the scramble sequence
						Types.ACTIONS action = so.getAction(index);
						int iAction = action.toInt();
						int j = iAction % 3;
						int i = (iAction - j) / 3;
						String[] faces = {"U", "L", "F", "D", "R", "B"};
						String[] modifiers = {"", "2", "'"};
						scrambleSequence.append(faces[i]).append(modifiers[j]).append(" ");

						so.advance(action, null);
					}
					break;
			}
		}
		so.getCubeState().minTwists = p;

		// Store the scramble sequence
		so.setScrambleSequence(scrambleSequence.toString());

		so = (StateObserverCube) so.clearedCopy();
		return so;
	}
	
	@Override
	public String getSubDir() {
		String substr = switch (CubeConfig.cubeSize) {
			case POCKET -> "2x2x2";
			case RUBIKS -> "3x3x3";
		};
		switch (CubeConfig.boardVecType) {
			case CUBESTATE -> substr += "_CSTATE";
			case CUBEPLUSACTION -> substr += "_CPLUS";
			case STICKER -> substr += "_STICKER";
			case STICKER2 -> substr += "_STICKER2";
		}
		switch (CubeConfig.twistType) {
			case HTM -> substr += "_AT";
			case QTM -> substr += "_QT";
		}
		return substr;
	}
	

    /* ---- METHODS BELOW ARE ONLY FOR DEBUG --- */

	@Override
	public void enableInteraction(boolean enable) {
		if (m_gameGui!=null)
			m_gameGui.enableInteraction(enable);
	}

	@Override
	public void showGameBoard(Arena arena, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(arena, alignToMain);
	}

	@Override
	public void toFront() {
		if (m_gameGui!=null)
			m_gameGui.toFront();
	}

	@Override
	public void destroy() {
    	if (m_gameGui!=null)
    		m_gameGui.destroy();
	}

  
}
