package games.RubiksCube;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.GameBoard;
import games.StateObservation;
import games.Arena;
import games.Arena.Task;
import games.Nim.GameBoardNimGui;
import games.RubiksCube.CSArrayList.CSAListType;
import games.RubiksCube.CSArrayList.TupleInt;
import games.RubiksCube.CubeState.Twist;
import games.ArenaTrain;
import tools.Types;
import tools.Types.ACTIONS;

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
	 * {@link #Board} position.
	 */
	protected StateObserverCube m_so;
	/**
	 * the array of distance sets for training
	 */
	private CSArrayList[] D;		
	/**
	 * the array of distance sets for testing (= evaluation)
	 */
	private CSArrayList[] T;		
	private CSArrayList[] D2=null;
	private boolean arenaActReq=false;
	private int[][] realPMat;
	
	/**
	 * If true, select in {@link #chooseStartState(PlayAgent)} from distance set {@link #D}.
	 * If false, use {@link #selectByTwists2(int)}. 
	 */
	private boolean SELECT_FROM_D = true;  
	/**
	 * If true, increment the matrix realPMat, which measures the real p of each start state.  
	 * Make a debug printout of realPMat every 10000 training games.
	 * 
	 * @see #chooseStartState(PlayAgent) chooseStartState(PlayAgent) and its helpers selectByTwists1 or selectByTwists2
	 * @see #incrRealPMat(StateObserverCube, int)
	 * @see #printRealPMat()
	 */
	private boolean DBG_REALPMAT=false;

	
	public GameBoardCube(Arena arena) {
		m_Arena		= arena;
		this.initialize();
		
	}
	
	public void initialize() {
//		long seed = 999;
//		rand 		= new Random(seed);
        rand 		= new Random(System.currentTimeMillis());	
        rand2 		= new Random(2*System.currentTimeMillis());	
		m_so		= new StateObserverCube();	// empty table
		realPMat 	= new int[CubeConfig.pMax+1][CubeConfig.pMax+2];			
    	D2 		= new CSArrayList[12];
		D2[0] 	= new CSArrayList(CSAListType.GenerateD0);
		D2[1] 	= new CSArrayList(CSAListType.GenerateD1);	
		this.T = generateDistanceSets(rand2);
		if (this.SELECT_FROM_D) {
			this.D = generateDistanceSets(rand);
			this.checkIntersects();   // print out the intersection sizes of D and T 
		} 
		
        if (m_Arena.hasGUI()) {
        	m_gameGui = new GameBoardCubeGui(this);
       }
	}
	
	public CSArrayList[] generateDistanceSets(Random rand) {
		
		System.out.println("\nGenerating distance sets ..");
		boolean silent=false;
		boolean doAssert=true;
//		CSAListType csaType = CSAListType.GenerateNextColSymm;
		CSAListType csaType = CSAListType.GenerateNext;
		ArrayList<TupleInt>[] tintList = new ArrayList[12];
    	CSArrayList[] gD 	= new CSArrayList[12];
		gD[0] = new CSArrayList(CSAListType.GenerateD0);
		gD[1] = new CSArrayList(CSAListType.GenerateD1);
		//D[1].assertTwistSeqInArrayList();
		for (int p=2; p<=CubeConfig.pMax; p++) {			// a preliminary set up to pMax - later we need it up to p=11
			if (p>1) silent=true;
			if (p>3) doAssert=false;
			tintList[p] = new ArrayList();
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
		if (m_gameGui!=null)
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
		
		if (m_gameGui!=null)
			m_gameGui.updateBoard(soN, withReset, showValueOnGameboard);

		
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
	 * @return a random start state which is p twists away from the solved cube. 
	 *         p from {1,...,{@link CubeConfig#pMax}} is picked randomly.
	 *      
	 * @see Arena#PlayGame()
	 */
	@Override
	public StateObservation chooseStartState() {
		clearBoard(true, true);			// m_so is in default start state 
		int p = 1+rand.nextInt(CubeConfig.pMax);
		System.out.println("p = "+p);
		int index = rand.nextInt(T[p].size());
		CubeState cS = (CubeState)T[p].get(index);
		m_so = new StateObserverCube(cS);
//		m_so = clearCube(m_so,p);
		m_so = new StateObserverCubeCleared(m_so,p);
		return m_so;
	}


	/**
	 * Choose a random start state when training for an episode. Return a start state depending 
	 * on {@code pa}'s {@link PlayAgent#getGameNum()} and {@link PlayAgent#getMaxGameNum()} 
	 * by randomly selecting from the distance sets D[p]. 
	 * <p>
	 * In more detail: Set X={@link CubeConfig#Xper}[{@link CubeConfig#pMax}]. 
	 * If the proportion of training games is in the first X[1] percent, select from D[1], 
	 * if it is between X[1] and X[2] percent, select from D[2], and so on.  In this 
	 * way we realize <b>time-reverse learning</b> (from the cube's end-game to the more complex
	 * cube states) during the training process. The cumulative percentage X is currently 
	 * hard-coded in {@link CubeConfig#Xper}.
	 * <p>
	 * If {@link #SELECT_FROM_D}==true, then select from {@code this.D} (distance sets created at program startup).<br>
	 * If {@link #SELECT_FROM_D}==false, then use {@link #selectByTwists2(int)}.
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
		double[] X = CubeConfig.Xper[CubeConfig.pMax];
		double x = ((double)pa.getGameNum())/pa.getMaxGameNum() + 1e-10;
		for (p=1; p<=CubeConfig.pMax; p++) {
			if (X[p-1]<x && x<X[p]) break;
		}
		if (SELECT_FROM_D) {
			int index = rand.nextInt(D[p].size());
//			D[p].remove(cS);	// remove elements already picked -- currently NOT used
			m_so = new StateObserverCube(D[p].get(index));
		} else {
//			m_so = selectByTwists1(p);
			m_so = selectByTwists2(p);
			
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
		
		// StateObserverCubeCleared is VERY important, so that no actions are 'forgotten' when 
		// trying to solve m_so (!!)
		m_so = new StateObserverCubeCleared(m_so,p);
		return m_so;
	}
	
	/** 
	 * --- NOT the recommended choice! --> better use selectByTwists2 ---
	 * 
	 * Experimental method to select a start state by doing 1.2*p random twist on the default cube. 
	 * This may be the only way to select a start state being p=8,9,... twists away from the 
	 * solved cube (where the distance D[p] becomes to big). 
	 * But it has the caveat that p random twists do not guarantee to produce a state in D[p]. 
	 * Due to twins etc. the resulting state may be actually in D[p-1], D[p-2], ...
	 */
	private StateObserverCubeCleared selectByTwists1(int p) {
		StateObserverCubeCleared d_so;
		CubeState cS;
		int index;
		d_so = new StateObserverCubeCleared(); // default cube
		// the not-recommended choice: make 1.2*p twists and hope that we land in 
		// distance set D[p] (which is very often not true for p>5)
		int twists = (int)(1.2*p);
		for (int k=0; k<twists; k++)  {
			index = rand.nextInt(d_so.getAvailableActions().size());
			d_so.advance(d_so.getAction(index));  				
		}
		d_so = new StateObserverCubeCleared(d_so,p);
		
		if (DBG_REALPMAT) incrRealPMat(d_so, p);	// increment realPMat		
		
		return d_so; 
	}
	
	/** 
	 * Method to select a start state in distance set D[p] by random twists and maintaining 
	 * a list D2[k], k=0,...,p, of all already visited states. 
	 * A new state in D[p] is created by randomly selecting a state from D2[p-1], advancing it 
	 * and - if it happens to be in D2[p-1] or D2[p-2] - advancing again (and again) .
	 * <p>
	 * <b>Details</b>:
	 * This method is guaranteed to return a state in the true D[p] if and only if
	 * D2[p-1], D2[p-2], ... are complete. If they are not, <ul>
	 * <li> certain elements from D[p] may be missed
	 *      (since its predecessor in D2[p-1] is not there)    -- or -- 
	 * <li> an element may claim to be in D[p], but truly it belongs to D[p-1] or D[p-2]
	 *      (not detected, since this element is not present in D2[p-1] or D2[p-2]).
	 * </ul><p>
	 * But nevertheless, in the limit of a large number of calls for every p-1, p-2, ..., this
	 * method will produce with high probability every element from D[p] and only from D[p].
	 */
	private StateObserverCubeCleared selectByTwists2(int p) {
		StateObserverCube d_so;
		StateObserverCubeCleared d_soC;
		CubeState cS;
		int index;
//			d_so = new StateObserverCube(); // default cube
//			for (int k=1; k<=p; k++)  {
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
			d_soC = new StateObserverCubeCleared(d_so,p);
			if (D2[p]==null) D2[p]=new CSArrayList(); 
			if (!D2[p].contains(d_soC.getCubeState())) D2[p].add(d_soC.getCubeState());
		
			if (DBG_REALPMAT) incrRealPMat(d_soC, p);	// increment realPMat		
		
		return d_soC; 
	}

	// --- obsolete now, we have StateObserverCubeCleared ---
//	private StateObserverCube clearCube(StateObserverCube d_so, int p) {
////		CubeState cS;
////		cS = d_so.getCubeState();
////		cS.minTwists = p;
////		cS.clearLast();
////		d_so = new StateObserverCube(cS);
//		d_so.getCubeState().minTwists = p; 
//		d_so.getCubeState().clearLast(); 	// clear lastTwist and lastTimes (which we do not know 
//											// for the initial state in an episode)	
//		d_so.setAvailableActions();	// then set the available actions which causes all
//									// 9 actions to be added to m_so.acts. We need this
//									// to test all 9 actions when looking for the best
//									// next action.
//		// (If lastTwist were set, 3 actions would be excluded
//		// which we do not want for a start state.) 
//		return d_so;
//	}
	
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
		return null;
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
     * accordingly.
     * The real pR is only guaranteed to be found, if T[p] is complete.
     */
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

	public void printRealPMat() {
		DecimalFormat df = new DecimalFormat("  00000");
		for (int i=0; i<realPMat.length; i++) {
			for (int j=0; j<realPMat[i].length; j++) {
				System.out.print(df.format(realPMat[i][j]));
			}
			System.out.println("");
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
