package games.RubiksCube;

import java.util.Random;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.GameBoard;
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
public class GameBoardCube implements GameBoard {

	private transient GameBoardCubeGui m_gameGui = null;
	
	protected Arena  m_Arena;		// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	protected Random rand2;
	/**
	 * The representation of the state corresponding to the current 
	 * board position.
	 */
	protected StateObserverCube m_so;
	private boolean arenaActReq=false;
	private final StateObserverCube def = new StateObserverCube();

	public GameBoardCube(Arena arena) {
		m_Arena		= arena;
		this.initialize();
		
	}
	
	/**
	 * called by constructor and prior to each training run
	 */
	public void initialize() {
//		long seed = 999;
//		rand 		= new Random(seed);
        rand 		= new Random(System.currentTimeMillis());	
        rand2 		= new Random(2*System.currentTimeMillis());	
		m_so		= new StateObserverCube();	// empty table
		
        if (m_Arena.hasGUI() && m_gameGui==null) {
			switch (CubeConfig.cubeType) {
				case POCKET -> m_gameGui = new GameBoardCubeGui2x2(this);
				case RUBIKS -> m_gameGui = new GameBoardCubeGui3x3(this);
			}

        }
        getPMax();		// actualize CubeConfig.pMin and CubeConfig.pMax, if GUI present
        
	}
	
	public int getPMax() {
		if (m_Arena.m_xab!=null) {
			// fetch the most actual values from tab "Other Pars"
			CubeConfig.pMin = m_Arena.m_xab.oPar[0].getpMinRubiks();
			CubeConfig.pMax = m_Arena.m_xab.oPar[0].getpMaxRubiks();
			if (CubeConfig.pMin<1) CubeConfig.pMin=1;
			CubeConfig.REPLAYBUFFER = m_Arena.m_xab.oPar[0].getReplayBuffer();
		}
        return CubeConfig.pMax;
	}

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
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			m_so = new StateObserverCube();			// solved cube
		}
							// considerable speed-up during training (!)
        if (m_gameGui!=null && m_Arena.taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
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
							boolean withReset, boolean showValueOnGameboard) {
		StateObserverCube soN = null;
		if (so!=null) {
	        assert (so instanceof StateObserverCube)
			: "StateObservation 'so' is not an instance of StateObserverCube";
	        soN = (StateObserverCube) so;
			m_so = soN;//.copy();
		} 
		
		if (m_gameGui!=null) {
			this.setPMin(m_Arena.m_xab.oPar[0].getpMinRubiks());  		// update pMin from oPar
			this.setPMax(m_Arena.m_xab.oPar[0].getpMaxRubiks());  		// update pMax from oPar
			m_gameGui.updateBoard(soN, withReset, showValueOnGameboard);
		}

		
	}

	/**
	 * @return  true: if an action is requested from Arena or ArenaTrain
	 * 			false: no action requested from Arena, next action has to come 
	 * 			from GameBoard (e.g. user input / human move) 
	 */
	@Override
	public boolean isActionReq() {
		return arenaActReq;
	}

	/**
	 * @param	actReq true : GameBoard requests an action from Arena 
	 * 			(see {@link #isActionReq()})
	 */
	@Override
	public void setActionReq(boolean actReq) {
		arenaActReq=actReq;
	}

	protected void HGameMove(int x, int y)
	{
		String[] twiStr = {"U","L","F","D","R","B"};
		System.out.println(twiStr[x]+(y+1));
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act);			// perform action (optionally add random elements)
		System.out.println(m_so.stringDescr());
		(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
		updateBoard(null,false,false);
		arenaActReq = true;			// ask Arena for next action
	}
	
	protected void InspectMove(int x, int y)
	{
		String[] twiStr = {"U","L","F","D","R","B"};
		System.out.println(twiStr[x]+(y+1));
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!m_so.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			m_Arena.setStatusMessage("Desired action is not legal");
			return;
		} else {
			m_Arena.setStatusMessage("Inspecting the value function ...");
		}
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in RubiksCube)
		m_so.getCubeState().clearLast();		// clear lastTwist and lastTimes of the CubeState,
		m_so.setAvailableActions();				// then set the available actions which causes all
												// numAllActions actions to be added to m_so.acts. We need this
												// to see the values for all numAllActions actions.
												// (If lastTwist were set, 3 actions would be excluded
												// which we do not want during INSPECTV.) 
		updateBoard(null,false,false);
		arenaActReq = true;		
	}
	
	public StateObservation getStateObs() {
		return m_so;
	}

	/**
	 * @return the 'empty-board' start state
	 */
	@Override
	public StateObservation getDefaultStartState() {
		clearBoard(true, true);
		return m_so;
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
		clearBoard(true, true);			// m_so is in default start state 
		m_so = selectByTwists1(p);

		// StateObserverCubeCleared is important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!). It also resets moveCounter
		//m_so = new StateObserverCubeCleared(m_so,p);
		m_so = (StateObserverCube) m_so.clearedCopy();
		
		//System.out.println("p = "+p+",  "+m_so.getCubeState().twistSeq);
		return m_so;
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
		clearBoard(true, true);			// m_so is in default start state 
		p = 1+rand.nextInt(CubeConfig.pMax);
		// since rand.nextInt(K) selects from {0,...,K-1}, we have p from {1,...,pMax}
		m_so = selectByTwists1(p);

		// StateObserverCubeCleared is important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!)
		//m_so = new StateObserverCubeCleared(m_so,p);
		m_so = (StateObserverCube) m_so.clearedCopy();
		return m_so;
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
	//protected StateObserverCubeCleared selectByTwists1(int p) {
	protected StateObserverCube selectByTwists1(int p) {
		//StateObserverCubeCleared d_so;
		int index;
		boolean cond;
		//System.out.println("selectByTwists1: p="+p);
		StateObserverCube so = new StateObserverCube(); // default cube
		int attempts=0;
		while (so.isEqual(def)) {		// do another round, if so is after twisting still default state
			attempts++;
			if (attempts % 1000==0) {
				System.err.println("[selectByTwists1] no cube different from default found -- may be p=0?? p="+p);
			}
			// make p twists and hope that we land in
			// distance set D[p] (which is often not true for p>5)
			switch (CubeConfig.twistType) {
				case HTM:
					for (int k=0; k<p; k++)  {
						do {
							index = rand.nextInt(so.getAvailableActions().size());
							cond = (CubeConfig.TWIST_DOUBLETS) ? false : (index/3 == so.getCubeState().lastTwist.ordinal()-1);
							// If doublets are forbidden (i.e. TWIST_DOUBLETS==false), then boolean cond stays true as long as
							// the drawn action (index) has the same twist type (e.g. U) as lastTwist. We need this because
							// doublet U1U1 can be reached redundantly by single twist U2, but we want to make non-redundant twists.
						} while (cond);
						so.advance(so.getAction(index));
					}
					break;
				case QTM:
					for (int k=0; k<p; k++)  {
						do {
							index = rand.nextInt(so.getAvailableActions().size());
							cond = (CubeConfig.TWIST_DOUBLETS) ? false : (index/3 == so.getCubeState().lastTwist.ordinal()-1 &&
									(index%3+1) != so.getCubeState().lastTimes);
							// if doublets are forbidden, boolean cond stays true as long as the drawn action (index) has
							// the same twist type (e.g. U) as lastTwist, but the opposite 'times' as lastTimes (only 1 and 3
							// are possible here). This is because doublet U1U3 would leave the cube unchanged
						} while (cond);
						so.advance(so.getAction(index));
					}
					break;
			}
		}
		so.getCubeState().minTwists=p;

		//so = new StateObserverCubeCleared(so,p);
		so = (StateObserverCube) so.clearedCopy();
		//System.out.println(d_so.getCubeState().twistSeq);
		

		return so;
	}
	
	@Override
	public String getSubDir() {
		String substr = switch (CubeConfig.cubeType) {
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
	
    @Override
    public Arena getArena() {
        return m_Arena;
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
