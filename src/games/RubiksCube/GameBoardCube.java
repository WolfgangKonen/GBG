package games.RubiksCube;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.GameBoard;
import games.StateObservation;
import games.Arena;
import games.RubiksCube.CSArrayList.CSAListType;
import games.RubiksCube.CSArrayList.TupleInt;
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

	//
	// all members following below are concerned with distance sets D[p]. The concept of distance sets is now 
	// considered to be infeasible, because for large p it takes too long on startup and the distance sets 
	// tend to be incomplete (too much time & too much memory needed)
	//
	
	/**
	 * The array of distance sets for training. (Distance set D[p] contains all states where the minimum number of twists 
	 * to reach the solved state is p. D[p] is deprecated for practical use, since it is difficult to obtain if p 
	 * becomes larger. Therefore we use it merely as a theoretical concept to characterize the set of all states being
	 * truly p twists away from the solved cube.)
	 */
	@Deprecated
	private CSArrayList[] D;		
	/**
	 * The array of distance sets for testing (= evaluation)
	 */
	@Deprecated
	private CSArrayList[] T,		
						  D2=null;	// D2 is (besides D2[0] and D2[1]) only filled by repeated calls to selectByTwists2
	private int[][] realPMat;		// see incRealPMat(...)
	
	/**
	 * If true, select in {@link #chooseStartState(PlayAgent)} from distance set {@link #D}.
	 * If false, use {@link #selectByTwists1(int)}. <br>
	 * Recommended value: false.
	 */
	boolean SELECT_FROM_D = false;  
	/**
	 * If true, select in {@link #chooseStartState()} from distance set {@link #T}.
	 * If false, use {@link #selectByTwists1(int)}. <br>
	 * Recommended value: false.
	 */
	boolean SELECT_FROM_T = false;  
	
	/**
	 * If true, increment the matrix realPMat, which measures the real p of each start state.  
	 * Make a debug printout of realPMat every 10000 training games.
	 * 
	 * @see #chooseStartState(PlayAgent) chooseStartState(PlayAgent) and its helpers selectByTwists1 or selectByTwists2
	 * @see #incrRealPMat(StateObserverCube, int)
	 * @see #printRealPMat()
	 */
	private final boolean DBG_REALPMAT=false;

	
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
        	m_gameGui = new GameBoardCubeGui(this);
        }
        getPMax();		// actualize CubeConfig.pMax, if GUI present
        
        // this part below only for distance sets:
		realPMat 	= new int[CubeConfig.pMax+1][CubeConfig.pMax+2];		// see incRealPMat(...)	
    	D2 		= new CSArrayList[12];
		D2[0] 	= new CSArrayList(CSAListType.GenerateD0);
		D2[1] 	= new CSArrayList(CSAListType.GenerateD1);	
		if (this.SELECT_FROM_T) {
			this.T = generateDistanceSets(rand2);	
		}
		if (this.SELECT_FROM_D) {
			this.D = generateDistanceSets(rand);
			this.checkIntersects();   // print out the intersection sizes of D and T 
		} 
	}
	
	public int getPMax() {
		if (m_Arena.m_xab!=null) {
			// fetch the most actual value from tab "Other Pars"
			CubeConfig.pMax = m_Arena.m_xab.oPar[0].getpMaxRubiks();
			CubeConfig.REPLAYBUFFER = m_Arena.m_xab.oPar[0].getReplayBuffer();
		}
        return CubeConfig.pMax;
	}
	
	public void setPMax(int pMax) {
        if (m_gameGui!=null) {
        	m_gameGui.setPMax(pMax);
        }
	}
	
	/**
	 * Generate the distance sets up to {@link CubeConfig#pMax}. Since it may be very time consuming to generate the 
	 * complete distance set D[p] for larger p, we generate only {@link CubeConfig#Narr}{@code [p]} elements in each 
	 * distance set.
	 * <p>
	 * <b>Caveat:</b> If the {@code Narr} numbers are smaller than the theoretical size of D[p], the whole method is 
	 * questionable: If a distance set is not complete, its usage may lead to wrong conclusions. 
	 * And if the two distance sets preceding D[p] are not complete, the method does not guarantee that 
	 * the inserted states are really in D[p]. That is why this method is now deprecated. 
	 * 
	 * @param rand
	 * @return a list of distance sets
	 */
	@Deprecated
	public CSArrayList[] generateDistanceSets(Random rand) {
		
		System.out.println("\nGenerating distance sets ..");
		boolean silent=false;
		boolean doAssert=true;
//		CSAListType csaType = CSAListType.GenerateNextColSymm;
		CSAListType csaType = CSAListType.GenerateNext;
		ArrayList[] tintList = new ArrayList[12];
    	CSArrayList[] gD 	= new CSArrayList[12];
		gD[0] = new CSArrayList(CSAListType.GenerateD0);
		gD[1] = new CSArrayList(CSAListType.GenerateD1);
		//D[1].assertTwistSeqInArrayList();
		for (int p=2; p<=CubeConfig.pMax; p++) {			// a preliminary set up to pMax - later we need it up to p=11
			if (p>1) silent=true;
			if (p>3) doAssert=false;
			tintList[p] = new ArrayList<TupleInt>();
			//System.out.print("Generating distance set for p="+p+" ..");
			long startTime = System.currentTimeMillis();
			
			gD[p] = new CSArrayList(csaType, gD[p-1], gD[p-2], CubeConfig.Narr[p],
									tintList[p], silent, doAssert, rand);
			
			double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
			//assert(CubeStateMap.countDifferentStates(D[p])==D[p].size()) : "D["+p+"]: size and # diff. states differ!";
			//D[p].assertTwistSeqInArrayList();
			System.out.println("\nCoverage D["+p+"] = "+gD[p].size()+" of "+ CubeConfig.theoCov[p]
					+"    Time="+elapsedTime+" sec");
			//CSArrayList.printTupleIntList(tintList[p]);
			//CSArrayList.printLastTupleInt(tintList[p]);
			int dummy=1;
		}
			
		return gD;
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
			m_so = soN.copy();
		} 
		
		if (m_gameGui!=null) {
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
		String[] twiStr = {"U","L","F"};
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
		String[] twiStr = {"U","L","F"};
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
												// 9 actions to be added to m_so.acts. We need this
												// to see the values for all 9 actions.
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
	 *         If this selector is set to "RANDOM" or if the game is played w/o GUI, p from {1,...,{@link CubeConfig#pMax}} 
	 *         is picked randomly.
	 *      
	 * @see Arena#PlayGame()
	 */
	@Override
	public StateObservation chooseStartState() {
		int p = 1+rand.nextInt(CubeConfig.pMax);		// random p \in {1,2,...,pMax}
		if (m_gameGui!=null) {
			String str=m_gameGui.getScramblingTwists();
			if (!str.equals("RANDOM")) p = Integer.valueOf(str).intValue();
		}
		return chooseStartState(p);
	}
	
	/**
	 * Choose a start state by scrambling the default cube via {@link #selectByTwists1(int) selectByTwists1(p)}.
	 * @param p
	 * @return	a scrambled cube
	 */
	public StateObservation chooseStartState(int p) {		
		clearBoard(true, true);			// m_so is in default start state 
		if (SELECT_FROM_T) {	// this is now deprecated
			int index = rand.nextInt(T[p].size());
			CubeState cS = (CubeState)T[p].get(index);
			m_so = new StateObserverCube(cS);
		} else {
			m_so = selectByTwists1(p);
		}
		
		// StateObserverCubeCleared is important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!). It also resets moveCounter
		m_so = new StateObserverCubeCleared(m_so,p);
		
		//System.out.println("p = "+p+",  "+m_so.getCubeState().twistSeq);
		return m_so;
	}


	/**
	 * Choose a random start state when training for an episode. Return a start state depending 
	 * on {@code pa}'s {@link PlayAgent#getGameNum()} and {@link PlayAgent#getMaxGameNum()} 
	 * by randomly twisting the default cube p times (deprecated: by selecting from the distance sets D[p]). 
	 * <p>
	 * If {@link #SELECT_FROM_D}==false, then use {@link #selectByTwists1(int)}.<br>
	 * Which p to take? - Select randomly p from 1,2,...,{@link CubeConfig#pMax}.
	 * <p>
	 * If {@link #SELECT_FROM_D}==true (<b>deprecated</b>), then select from distance set D[p]. 
	 * Distance sets are created at program startup).<br>
	 * Which p to take? - Set X={@link CubeConfig#Xper}[{@link CubeConfig#pMax}]. 
	 * If the proportion of training games is in the first X[1] percent, set p=1, 
	 * if it is between X[1] and X[2] percent, set p=2, and so on.  In this 
	 * way we realize <b>time-reverse learning</b> (from the cube's end-game to the more complex
	 * cube states) during the training process. The cumulative percentage X is currently 
	 * hard-coded in {@link CubeConfig#Xper}.
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
		if (SELECT_FROM_D) {
			double[] X = CubeConfig.Xper[CubeConfig.pMax];
			
			// which p to take?
			double x = ((double)pa.getGameNum())/pa.getMaxGameNum() + 1e-10;
			for (p=1; p<=CubeConfig.pMax; p++) {
				if (X[p-1]<x && x<X[p]) break;
			}			
			int index = rand.nextInt(D[p].size());
			m_so = new StateObserverCube(D[p].get(index));
		} else {
			p = 1+rand.nextInt(CubeConfig.pMax);
			m_so = selectByTwists1(p);
//			m_so = selectByTwists2(p);
			
			// only debug:
			if (DBG_REALPMAT) {
				if (pa.getGameNum() % 10000 == 0 ) {
					this.printRealPMat(); 
					int dummy=1;
				}
				if (pa.getGameNum()==(pa.getMaxGameNum()-1)) {
					this.printRealPMat();				
					int dummy=1;
				}
			}
		}
		
		// StateObserverCubeCleared is important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!)
		m_so = new StateObserverCubeCleared(m_so,p);
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
	 * 
	 * @see #selectByTwists2(int)
	 */
	protected StateObserverCubeCleared selectByTwists1(int p) {
		StateObserverCubeCleared d_so;
		int index;
		boolean cond;
		//System.out.println("selectByTwists1: p="+p);
		StateObserverCube so = new StateObserverCube(); // default cube
		while (so.isEqual(new StateObserverCube())) {		// do another round, if so is after twisting still default state
			// make p twists and hope that we land in
			// distance set D[p] (which is often not true for p>5)
			switch (CubeConfig.twistType) {
				case ALLTWISTS:
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
				case QUARTERTWISTS:
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
		d_so = new StateObserverCubeCleared(so,p);
		//System.out.println(d_so.getCubeState().twistSeq);
		
		if (DBG_REALPMAT) incrRealPMat(d_so, p);	// increment realPMat		
		
		return d_so; 
	}
	
	/** 
	 * Select a state in distance set D[p] by random twists and maintain
	 * a list D2[k], k=0,...,p, of all already visited states. 
	 * Create it by selecting a random state from D2[p-1], advance it with a random twist
	 * and - if it happens to be in D2[p-1] or D2[p-2] - advance it again (and again). 
	 * <p>
	 * Disadvantages: For large p, the list D2[p-1] and D2[p-2] become too big to be maintained in memory. And if they 
	 * are not complete, the returned state may be not in D[p]. That is why this method is now deprecated.<br>
	 * Advantage: If they are complete, we return a state which is truly in D[p].
	 * <p>
	 * Side effect: The returned object -- assumed to be a cube state p twists away from the solved cube -- is 
	 * added to D2[p].
	 * <p>
	 * In the limit of a large number of calls for every p-1, p-2, ..., this
	 * method will produce with high probability an element truly from D[p].
	 * 
	 * @see #selectByTwists1(int)
	 */
//	 * <b>Details</b>:
//	 * This method is guaranteed to return a state in the true D[p] if and only if
//	 * D2[p-1], D2[p-2], ... are complete. If they are not, <ul>
//	 * <li> certain elements from D[p] may be missed
//	 *      (since its predecessor in D2[p-1] is not there)    -- or -- 
//	 * <li> an element may claim to be in D[p], but truly it belongs to D[p-1] or D[p-2]
//	 *      (not detected, since this element is not present in D2[p-1] or D2[p-2]).
//	 * </ul><p>
	@Deprecated
	private StateObserverCubeCleared selectByTwists2(int p) {
		StateObserverCube d_so;
		int index;
			index = rand.nextInt(D2[p-1].size());
			d_so =new StateObserverCube(D2[p-1].get(index)); // pick randomly cube from D2[p-1]
			for (int k=p; k<=p; k++)  {
				index = rand.nextInt(d_so.getAvailableActions().size());
				d_so.advance(d_so.getAction(index));  	
				if (k>=3) {
					if (D2[k-1].contains(d_so.getCubeState())) {
						k = k-1;
					} else if (D2[k-2].contains(d_so.getCubeState())) {
						k = k-2;
					}
				}
			}	
			// StateObserverCubeCleared is VERY important, so that no actions are 'forgotten' when 
			// trying to solve m_so (!!)
			StateObserverCubeCleared d_soC = new StateObserverCubeCleared(d_so,p);
			if (D2[p]==null) D2[p]=new CSArrayList(); 
			if (!D2[p].contains(d_soC.getCubeState())) D2[p].add(d_soC.getCubeState());
		
			if (DBG_REALPMAT) incrRealPMat(d_soC, p);	// increment realPMat		
		
		return d_soC; 
	}

    /**
     * @return the array of distance sets for training
     */
    public CSArrayList[] getD() {
 	   return D;
    }
   
    /**
     * @return the array of distance sets for testing (= evaluation)
     */
    public CSArrayList[] getT() {
	   return T;
    }
	   
	@Override
	public String getSubDir() {
		String substr="";
		switch (CubeConfig.cubeType) {
		case POCKET: substr = "2x2x2"; break;
		case RUBIKS: substr = "3x3x3"; break;
		}
		switch (CubeConfig.boardVecType) {
		case CUBESTATE: substr += "_CSTATE"; break;
		case CUBEPLUSACTION: substr += "_CPLUS"; break;
		case STICKER: substr += "_STICKER"; break;
		case STICKER2: substr += "_STICKER2"; break;
		}
		switch (CubeConfig.twistType) {
		case ALLTWISTS: substr += "_AT"; break;
		case QUARTERTWISTS: substr += "_QT"; break;
		}
		return substr;
	}
	
    @Override
    public Arena getArena() {
        return m_Arena;
    }
    
    /* ---- METHODS BELOW ARE ONLY FOR DEBUG --- */

    private void checkIntersects() {
    	for (int p=1; p<=CubeConfig.pMax; p++) {
    	    Iterator itD = D[p].iterator();
    	    int intersectCount = 0;
    	    while (itD.hasNext()) {
    		    CubeState twin = (CubeState)itD.next();
    		    if (T[p].contains(twin)) intersectCount++;
            } 
    		System.out.println("checkIntersects: p="+p+", intersect(D[p],T[p])="+intersectCount+", D[p].size="+D[p].size());
    	}   	
    }
    
    /**
     * Find the real pR for state d_so which claims to be in T[p] and increment realPMat 
     * accordingly. realPMat is a (pMax+1) x (pMax+2) matrix with pMax = {@link CubeConfig#pMax}. If a state d_so
     * which claims to be p twists away from the solved state is found to be pR in reality, then realPMat[p][pR] is 
     * incremented. If it is not found in any T[p] ('pR not known'), then realPMat[p][pMax+1] is incremented.
     * <p>
     * The real pR is only guaranteed to be found, if T[p] is complete. That is why this method is deprecated.
     */
    @Deprecated
    void incrRealPMat(StateObserverCube d_so, int p) {
		boolean found=false;
		for (int pR=0; pR<=CubeConfig.pMax; pR++) {
			if (T[pR].contains(d_so.getCubeState())) {
				// the real p is pR
				realPMat[p][pR]++;
				found = true;
				break;
			}
		}
		
		if (!found) realPMat[p][CubeConfig.pMax+1]++;
		// A count in realPMat[X][pMax+1] means: the real p is not known for p=X.
		// This can happen if T[pR] is not the complete set: Then d_so might be truly in 
		// the distance set of pR, but it is not found in T[pR].
	}

    @Deprecated
	public void printRealPMat() {
		DecimalFormat df = new DecimalFormat("  00000");
		for (int i=0; i<realPMat.length; i++) {
			for (int j=0; j<realPMat[i].length; j++) {
				System.out.print(df.format(realPMat[i][j]));
			}
			System.out.println();
		}
		
	}
	
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
