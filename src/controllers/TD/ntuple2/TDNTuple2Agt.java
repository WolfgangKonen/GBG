package controllers.TD.ntuple2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import agentIO.LoadSaveGBG;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import controllers.AgentBase;
import controllers.ExpectimaxWrapper;
import controllers.MaxNWrapper;
import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple2.ZValueMulti;
import controllers.TD.ntuple2.TDNTuple2Agt.UpdateType;
import games.Arena;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.StateObsNondeterministic;
import games.XNTupleFuncs;
import games.CFour.StateObserverC4;
import games.RubiksCube.StateObserverCube;
import games.XArenaMenu;
import games.ZweiTausendAchtundVierzig.StateObserver2048;

//
// !!! NOTE: We currently do not use NTupleBase as a base class for TDNTuple2Agt (although
// !!! we could) because this would invalidate all TDNTuple2Agt agents stored so far on disk
// 

/**
 * The TD-Learning {@link PlayAgent} (Temporal Difference reinforcement learning)
 * <b>with n-tuples</b>. 
 * It has a one-layer (perceptron-like) neural network with or without output-nonlinearity  
 * {@code tanh} to model the value function. 
 * The net follows closely the (pseudo-)code by [SuttonBonde93]. 
 * <p>
 * Some functionality is packed in the superclass 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...)
 * <p>
 * {@link TDNTuple2Agt} replaces the older {@code TDNTupleAgt}. 
 * The differences of {@link TDNTuple2Agt} to {@code TDNTupleAgt} are:
 * <ul>
 * <li> no eligibility traces, instead LAMBDA-horizon mechanism of [Jaskowski16] (faster and less
 * 		memory consumptive)
 * <li> option AFTERSTATE (relevant only for nondeterministic games like 2048), which builds the  
 * 		value function on the argument afterstate <b>s'</b> (before adding random element) instead 
 * 		of next state <b>s''</b> (faster learning and better generalization).
 * <li> has the random move rate bug fixed: Now EPSILON=0.0 means really 'no random moves'.
 * <li> learning rate ALPHA differently scaled: if ALPHA=1.0, the new value for a
 * 		state just trained will be exactly the target. Therefore, recommended ALPHA values are 
 * 		m*N_s bigger than in {@code TDNTupleAgt}, where m=number of n-tuples, N_s=number of 
 * 		symmetric (equivalent) states. 
 * <li> a change in the update formula: when looping over different equivalent
 * 		states, at most one update per index is allowed (see comment in {@link NTuple2} for 
 * 		member {@code indexList}).
 * </ul>
 * 
 * @see PlayAgent
 * @see AgentBase
 * 
 * @author Wolfgang Konen, TH Koeln, 2017-2020
 */
public class TDNTuple2Agt extends AgentBase implements PlayAgent,NTupleAgt,Serializable {
	/**
	 * UpdateType characterizes the weight update mode: <ul>
	 * <li> <b>SINGLE_UPDATE</b>: Each learn action updates only a single value function (that of the current player)
	 * <li> <b>MULTI_UPDATE</b>: Each learn action updates multiple value functions (of all players) 
	 * </ul>
	 * @see TDNTuple2Agt#getUpdateType(StateObservation)
	 */
	enum UpdateType {SINGLE_UPDATE, MULTI_UPDATE};
	
	private NTupleAgt.EligType m_elig;
	
	public Random rand; // generate random Numbers 
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;

	/**
	 * Controls the amount of explorative moves in
	 * {@link #getNextAction2(StateObservation, boolean, boolean)}
	 * during training. <br>
	 * m_epsilon = 0.0: no random moves, <br>
	 * m_epsilon = 0.1 (def.): 10% of the moves are random, and so forth
	 * m_epsilon undergoes a linear change from {@code tdPar.getEpsilon()} 
	 * to {@code tdPar.getEpsilonFinal()}. 
	 * This is realized in {@link #finishUpdateWeights()}.
	 */
	private double m_epsilon = 0.1;
	
	/**
	 * m_EpsilonChangeDelta is the epsilon change per episode.
	 */
	private double m_EpsilonChangeDelta = 0.001;
	
	private double BestScore;
	
//	private boolean TC; 			//obsolete, use now m_Net.getTc()
//	private boolean tcImm=true;		//obsolete, use now m_Net.getTcImm()
	private int tcIn; 	// obsolete (since tcImm always true); was: temporal coherence interval: 
						// if (!tcImm), then after tcIn games tcFactor will be updates
	
	// not needed anymore here, we hand in int[][] nTuples = = ntupfac.makeNTupleSet(...) 
	// to the constructor: 
//	private boolean randomness=false; //true: ntuples are created randomly (walk or points)
//	private boolean randWalk=true; 	//true: random walk is used to generate nTuples
//									//false: random points is used to generate nTuples//samine//

	// Value function of the agent.
	private NTuple2ValueFunc m_Net;
	
	// m_Net3 is only needed for DEBG_NEW_3P: it is a parallel structure learning the value 
	// function for the old TD update scheme (as if VER_3P==false) even in the case
	// VER_3P==true. The purpose is to compare step-by-step with the results from m_Net.
	private NTuple2ValueFunc m_Net3;	
	private double ZScore_NTL2;

	// Use symmetries (rotation, mirror) in NTuple-System
//	protected boolean USESYMMETRY = true; 	// use now m_ntPar.getUseSymmetry() - don't store/maintain value twice
//	private boolean learnFromRM = false;    // use now m_oPar.useLearnFromRM() - don't store/maintain value twice
	
//	/**
//	 * NORMALIZE gets the value tdPar.getNormalize(). If true, perform score range normalization
//	 * in {@link #normalize2(double, StateObservation)}.
//	 */
//	private boolean NORMALIZE = false; 
	// --- commented out, we uses now tdPar.getNormalize() directly ---
	
	private boolean RANDINITWEIGHTS = false;// If true, init weights of value function randomly

	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
//	public static boolean NEWTARGET=true;	// this is now always true
//	public static boolean WITH_NS=true;		// this is now always true
	/**
	 * {@link #VER_3P} (Version 3P) is a boolean switch to decide whether to use the new logic
	 * valid for 1,2,3-,...,N-player games or to use the older 2-player logic.
	 * <p>
	 * If {@link #VER_3P}==true, use the new logic for 1,2,3-,...,N-player games: 
	 * <ul>
	 * <li> If {@link #MODE_3P}==0: Each player has its own reward function 
	 * and its own value function V(s_t|p^(i)). In each update step N value function updates occur. 
	 * <li> If {@link #MODE_3P}==1: Use the new n-ply logic with n=N as 
	 * described in TR-TDNTuple.pdf: one value function V(s_t|p_t) and each player maximizes
	 * its own game value or minimizes the next opponent's game value. 
	 * <li> If {@link #MODE_3P}==2 [<b>recommended</b>]: Use {@link #MODE_3P}==0 for N=1 and N &gt; 2.  
	 * For N==2, use a logic equivalent to 1-ply {@link #MODE_3P}==1 where the symmetry 
	 * 		V(s_t+1|p_t) = - V(s_t+1|p_t+1)
	 * allows to make only one weight update for V(s_t|p_t) but to have all V(s_t|p^(i)).
	 * </ul>
	 * If {@link #VER_3P}==false, proceed with a 2-player logic (see Javadoc for 
	 * {@link #NEW_2P}).
	 * <p>
	 * More details in document TR_GBG/TR-TDNTuple.pdf. 
	 */
	public static boolean VER_3P=true; 
	/**
	 * The actual value is set from {@link #m_tdPar}{@code .getMode3P()}. <br>
	 * See {@link #VER_3P} for more details.
	 */
	public int MODE_3P=1; 		// 0/1/2, the actual value is set in initNet from m_tdPar.getMode3P()
//	public static boolean OLD_3P=false; // OLD_3P=true  <--> MODE_3P=0
										// OLD_3P=false	<--> MODE_3P=1		
	
	/**
	 * This is a switch for 2-player games with antagonistic rewards. It is relevant only if 
	 * {@link #VER_3P}==false.
	 * <ul>
	 * <li> If {@link #NEW_2P}==false, use the old logic (prior to 10/2017): The target to learn is the 
	 * value/reward for the 1st player and only one value function update is needed (exploits 
	 * the 1- and 2-player symmetry). 
	 * <li> If {@link NEW_2P}==true, use the new logic: The target to learn is the negative of the 
	 * next state's reward+value from the perspective of the opponent.<p>
	 * </ul>
	 * Both techniques are equivalent in results and yield -- if applicable -- better results 
	 * than [{@link #VER_3P}==true, {@link #MODE_3P}==0]. But they are not generalizable to other than 2-player games.
	 * <p>
	 * {@link #NEW_2P}==true is the <b>recommended</b> choice (simpler to explain and to code).
	 */
	public static boolean NEW_2P=true;

//	// if NEW_GNA==true: use the new function getNextAction2,3 in getNextAction;
//	//    (the new version which returns Types.ACTIONS_VT and allows deterministic multi-moves)
//	// if NEW_GNA==false: use the old function getNextAction1 in getNextAction;
//	//    (the old version which returns Types.ACTIONS and does not allow multi-moves)
//	private static boolean NEW_GNA=true;	// this is now always true
	
	private boolean m_DEBG = false;
	static transient public PrintStream pstream = System.out;
	// debug printout in getNextAction, trainAgent:
	public static boolean DBG2_TARGET=false;
	// debug printout in updateWeightsNewTerminal:
	public static boolean DBGF_TARGET=false;
	// debug: repeat always the same sequence in one episode (to check a trivial convergence)
	public static boolean DBG2_FIXEDSEQUENCE=false;
	// debug printout in updateWeightsNew, ZValueMulti, NextState:
	public static boolean DBG_REWARD=false;
	// debug VER_3P=true, MODE_3P=0: m_Net3 is updated in parallel to m_Net (compare 2P and 3P), 
	// extra printout v_old,v_new in NTuple2ValueFunc::updateWeightsNew,updateWeightsNewTerminal 
	public static boolean DBG_OLD_3P=false;
	// debug VER_3P=true, MODE_3P=1:  
	public static boolean DBG_NEW_3P=false;
	// debug for Rubik's Cube:
	public static boolean DBG_CUBE=true;
	// use ternary target in update rule:
	public boolean TERNARY=true;		// remains true only if it is a final-reward-game (see getNextAction3)
	
	// is set to true in getNextAction3(...), if the next action is a random selected one:
	boolean randomSelect = false;
	
	/**
	 * Members {@link #m_tdPar}, {@link #m_ntPar}, {@link AgentBase#m_oPar} are needed for 
	 * saving and loading the agent (to restore the agent with all its parameter settings)
	 */
	private ParTD m_tdPar;
	private ParNT m_ntPar;
	
	//
	// variables needed in various train methods
	//
	private int m_counter = 0;				// episode move counter (trainAgent)
	private boolean m_finished = false;		// whether a training game is finished
	private boolean m_randomMove = false;	// whether the last action was random
    private RandomAgent randomAgent = new RandomAgent("Random");
	
	// info only:
	private int tieCounter = 0;
	private int winXCounter = 0;
	private int winOCounter = 0;


	/**
	 * Default constructor for {@link TDNTuple2Agt}, needed for loading a serialized version
	 */
	public TDNTuple2Agt() throws IOException {
		super();
		ParTD tdPar = new ParTD();
		ParNT ntPar = new ParNT();
		ParOther oPar = new ParOther();
		initNet(ntPar, tdPar, oPar, null, null, 1000);
	}

	/**
	 * Create a new {@link TDNTuple2Agt}
	 * 
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	public TDNTuple2Agt(String name, ParTD tdPar, ParNT ntPar, ParOther oPar, 
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		super(name);
		initNet(ntPar,tdPar,oPar, nTuples, xnf, maxGameNum);			
	}

	/**
	 * 
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	private void initNet(ParNT ntPar, ParTD tdPar, ParOther oPar,  
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		m_tdPar = new ParTD(tdPar);
		m_ntPar = new ParNT(ntPar);
		m_oPar = new ParOther(oPar);		// m_oPar is in AgentBase
		MODE_3P = m_tdPar.getMode3P();
		m_elig = (m_tdPar.getEligMode()==0) ? EligType.STANDARD : EligType.RESET;
		rand = new Random(System.currentTimeMillis());		//(42); 
		
		int posVals = xnf.getNumPositionValues();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells,1);
		if (DBG_OLD_3P || DBG_NEW_3P) {
			m_Net3 = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
					RANDINITWEIGHTS,ntPar,numCells,1);
		}
		
		setNTParams(ntPar);
		setTDParams(tdPar, maxGameNum);
		m_Net.setHorizon();
		
		setAgentState(AgentState.INIT);
	}

	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		this.m_Net.xnf.instantiateAfterLoading();
		// set horizon cut for older agents (where horCut was not part of ParTD):
		if (this.getParTD().getHorizonCut()==0.0) 
			this.getParTD().setHorizonCut(0.1);
		// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
		// (they would stay otherwise at their default values, would not 
		// get the loaded values)
		this.setTDParams(this.getParTD(), this.getMaxGameNum());
		this.setNTParams(this.getParNT());
		this.weightAnalysis(null);
		return true;
	}
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setTdParFrom(n, this.getParTD() );
		m_arena.m_xab.setNtParFrom(n, this.getParNT() );
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
	}
	
	/**
	 * Get the best next action and return it 
	 * (NEW version: ACTIONS_VT and recursive part for multi-moves)
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		return getNextAction3(so, so, random, silent);
	}
	
	// this function was just needed for EvaluatorCube if we want to avoid symmetries
	// definitely during evaluation. But it is no longer really needed, since we avoid 
	// symmetries throughout when doing Rubiks Cube.
	@Deprecated
	public Types.ACTIONS_VT getNextAction2SYM(StateObservation so, boolean random, boolean silent, boolean useSymmetry) {
		boolean oldSymmetry = m_ntPar.getUSESYMMETRY();
		m_ntPar.setUSESYMMETRY(useSymmetry);
		Types.ACTIONS_VT actBest = getNextAction3(so, so, random, silent);
		m_ntPar.setUSESYMMETRY(oldSymmetry);
		return actBest;
	}
	
	// 
	// This function is needed so that the recursive call inside getNextAction3 (see 
	// ZValueMulti) can transfer the referring state refer. 
	// (function GETNEXTACTIONNPLY in TR-TDNTuple.pdf, Algorithm 1)
	// This function is package-visible since ZValueMulti may call it.
	Types.ACTIONS_VT getNextAction3(StateObservation so, StateObservation refer, 
			boolean random, boolean silent) {
		int i, j;
		double BestScore3;
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		boolean rgs = m_oPar.getRewardIsGameScore();
		if (!so.isFinalRewardGame()) this.TERNARY=false;		// we have to use TD target
		StateObservation NewSO;
	    int iBest;
		int count = 1; // counts the moves with same BestScore3
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
    	BestScore3 = -Double.MAX_VALUE;
		double[] VTable;		
    	ZValue zvalueS;
		ZValueMulti zvalueM = new ZValueMulti(this); 
    	int nply = (MODE_3P==1) ? so.getNumPlayers() : 1;
		if (nply==1) {
//			zvalueS = new ZValueSingle(this);
			zvalueS = new ZValueSingleNPly(this,nply);
		} else {
			zvalueS = new ZValueSingleNPly(this,nply);
		}
	
        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
//			if (rand.nextDouble() < m_epsilon) 
//				return randomAgent.getNextAction2(so, true, silent);				
		}
		
		// get the best (or eps-greedy random) action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        List<Types.ACTIONS> bestActions = new ArrayList<>();
        double agentScore;
        
        VTable = new double[acts.size()];  
        
		UpdateType UPDATE = getUpdateType(so);
		
//		if (acts.size()==0) {
//			int dummy=1;
//		}
        assert acts.size()>0 : "Oops, no available action";
        for(i = 0; i < acts.size(); ++i)
        {
            if (VER_3P && UPDATE==UpdateType.SINGLE_UPDATE) {
            	// this has n-ply recursion, but does not reflect multi-moves:
            	//
            	nply = (MODE_3P==1) ? so.getNumPlayers() : 1;
//              CurrentScore = g3_Eval_NPly(so,acts.get(i),refer,nply,silent);   // OLD
                CurrentScore = zvalueS.calculate(so,acts.get(i),refer,silent);   

//              if (DBG_NEW_3P && so.getNumPlayers()==1) 
//                sanityCheck1P(so,acts,i,refer,silent,CurrentScore);
                
            } else {	// i.e. !VER_3P OR (UPDATE==UpdateType.MULTI_UPDATE)
            	// this has recursive multi-move recursion:
//              CurrentScore = g3_Evaluate(so,acts.get(i),refer,silent);		// OLD
                CurrentScore = zvalueM.calculate(so,acts.get(i),refer,silent);
           }
            
			// just a debug check:
			if (Double.isInfinite(CurrentScore)) {
				System.out.println("getScore(NewSO) is infinite!");
			}
			
			CurrentScore = normalize2(CurrentScore,so);					
			VTable[i] = CurrentScore;
			
			//
			// Calculate the best score BestScore3 and actBest.
			// If there are multiple best actions, select afterwards one of them randomly 
			// (better exploration)
			//
			if (BestScore3 < CurrentScore) {
				BestScore3 = CurrentScore;
                bestActions.clear();
                bestActions.add(acts.get(i));
			} else if (BestScore3 == CurrentScore) {
                bestActions.add(acts.get(i));
			}

        } // for (i)
        actBest = bestActions.get(rand.nextInt(bestActions.size()));
        // if several actions have the same best score, select one of them randomly

        assert actBest != null : "Oops, no best action actBest";
		if (!silent) {
			int playerPM = calculatePlayerPM(refer); 	 
			System.out.print("---Best Move: ");
            NewSO = so.copy();
            NewSO.advance(actBest);
			System.out.println(NewSO.stringDescr()+", "+(2*BestScore3*playerPM-1));
		}			
		if (DBG2_TARGET) {
			int playerPM = calculatePlayerPM(refer); 	 
			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
			// here we use the NextState version, because computation time does not matter
			// inside DBG2_TARGET and because this version is correct for both values of 
			// getAFTERSTATE():
	        NextState ns = new NextState(this,so,actBest);
			double deltaReward = ns.getNextSO().getReward(so,rgs) - so.getReward(so,rgs);
			double sc = (deltaReward + playerPM * getScore(ns.getAfterState()))*MAXSCORE;
			
			System.out.println("getScore((so,actbest)-afterstate): "+sc+"   ["+so.stringDescr()+"]");
			int dummy=1;
		}
		
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, BestScore3);
		return actBestVT;
	} // getNextAction3

    /**
     * This function returns normally (if VER_3P==true) just 1.
     * Only in the (deprecated) case VER_3P==NEW_2P==false, it returns +1 or -1, depending on refer's player being 0 or 1, resp.
     * In case VER_3P==false, NEW_2P==true, it returns -1.
     */
	public int calculatePlayerPM(StateObservation refer) {
        if (VER_3P) {
        	return 1;
        } else {
        	return (NEW_2P) ? -1 : Types.PLAYER_PM[refer.getPlayer()];
        }	
	}
    

		
	// DEBUG only: return always the 1st available action (for deterministic training games)
	public Types.ACTIONS_VT getFirstAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
		boolean rgs = m_oPar.getRewardIsGameScore();
        Types.ACTIONS_VT actBest = null;
		StateObservation NewSO;
		
		// get the first action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        actBest = (Types.ACTIONS_VT) acts.get(0);
        actBest.setRandomSelect(randomSelect);
        
		if (DBG2_TARGET) {
            NewSO = so.copy();
            NewSO.advance(actBest);
            double deltaReward = NewSO.getReward(so,rgs) - so.getReward(so,rgs);
			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
			double sc = (deltaReward + getScore(NewSO))*MAXSCORE;
			System.out.println("getScore(NewSO): "+sc+"   ["+so.stringDescr()+"]");
			int dummy=1;
		}
		return actBest;
	}

	/**
	 * Return the agent's estimate of the score for that after state 
	 * (old version, {@link VER_3P}==false).
	 * For 2-player games like TTT, the score is V(), the probability that 
	 * X (Player +1) wins from that after state. V(s_t|p_t) learns this probability for every t.
	 * p_t*V(s_t) is the quantity to be maximized by getNextAction2.
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * 
	 * @param so			the state for which the value is desired
	 * @return the agent's estimate of the future score for that after state
	 */
	public double getScore(StateObservation so) {
		int[] bvec = m_Net.xnf.getBoardVector(so).bvec;
		double score = m_Net.getScoreI(bvec,so.getPlayer());
		return score;
	}

	/**
	 * Return the agent's estimate of the score for that after state 
	 * (both versions, {@link VER_3P}==true/false).
	 * Return V(s_t|p_refer), that is the value function from the perspective of the player
	 * who moves in state {@code refer}. 
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * 
	 * @param so	the state s_t for which the value is desired
	 * @param refer	the referring state
	 * @return		V(s_t|p_refer), the agent's estimate of the future score for s_t
	 * 				from the perspective of the player in state {@code refer}
	 */
	public double getScore(StateObservation so, StateObservation refer) {
		double score;
    	if (VER_3P) {
    		int[] bvec = m_Net.xnf.getBoardVector(so).bvec;
    		score = m_Net.getScoreI(bvec,refer.getPlayer());
    		//score = getScore(so,refer.getPlayer());	
    	} else {
    		score = getScore(so);			        		
    	}
    	return score;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called by the n-ply wrappers ({@link MaxNWrapper}, {@link ExpectimaxWrapper}). 
	 * @param so	the state s_t for which the value is desired
	 * 
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of 
	 * 				the future score for s_t from the perspective of player i
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		ScoreTuple sc = new ScoreTuple(so);
		int[] bvec = m_Net.xnf.getBoardVector(so).bvec;
		switch (so.getNumPlayers()) {
		case 1: 
			sc.scTup[0] = m_Net.getScoreI(bvec,so.getPlayer());
			break;
		case 2:
			int player = so.getPlayer();
			int opponent = (player==0) ? 1 : 0;
			sc.scTup[player] = m_Net.getScoreI(bvec,player);
			sc.scTup[opponent] = -sc.scTup[player];
			break;
		default: 
	    	if (VER_3P) {
	    		for (int i=0; i<so.getNumPlayers(); i++) 
	    			sc.scTup[i] = m_Net.getScoreI(bvec,i);
	    			// CAUTION: This might not work for all i, if n-tuples were not trained
	    			// for all i in this state 'so' (MODE_3P==1). 
	    			// It should work however in the cases MODE_3P==0 and MODE_3P==2.
	    	} else {
	    		throw new RuntimeException("Cannot create ScoreTuple if VER_3P==false");			        		
	    	}
	    	break;
		}
		
		// In any case: add the reward obtained so far, since the net predicts
		// with getScoreI only the expected future reward.
		boolean rgs = m_oPar.getRewardIsGameScore();
		for (int i=0; i<so.getNumPlayers(); i++) 
			sc.scTup[i] += so.getReward(i, rgs);
    	return sc;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. <br>
	 * Is only called when training an agent in multi-update mode AND the maximum episode length
	 * is reached. 
	 * 
	 * @param sob			the current game state
	 * @return				the agent's estimate of the final game value <b>for all players</b>. 
	 * 						The return value is a tuple containing  
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s. 
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ScoreTuple sc = new ScoreTuple(sob);
//		sc = this.getScoreTuple(sob);		// NO recursion!!
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] = sob.getReward(i, rgs);
			// this is valid, but it may be a bad estimate in games where the reward is only 
			// meaningful for game-over-states.
		return sc;
	}

	/**
	 * Train the agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
// --- epiLength, learnFromRM are now available via the agent's member ParOther m_oPar: ---
//	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
//	 * 					prematurely.  
//	 * @param learnFromRM if true, learn from random moves during training
	public boolean trainAgent(StateObservation so) {
		double[] VTable = null;
		double reward = 0.0, oldReward = 0.0;
		double reward2=0.0;
		ScoreTuple rewardTuple, oldRewardTuple;
		boolean wghtChange = false;
		boolean upTC=false;
		double Input[], oldInput[];
		double Z = 0,ZZ=0,Z_nply;
		String S_old = null;   // only as debug info
		Types.ACTIONS_VT actBest;
		StateObservation oldSO;
		StateObservation afterSO;
		int[] curBoard;
		int   curPlayer=so.getPlayer();
		int[] nextBoard = null;
		int   nextPlayer;
		NextState ns = null;

		boolean learnFromRM = m_oPar.getLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;
		
		UpdateType UPDATE = getUpdateType(so);
		
		rewardTuple = new ScoreTuple(so);
		oldRewardTuple = new ScoreTuple(so);


		afterSO = so.getPrecedingAfterstate();
    	curBoard = (afterSO==null) ? null : m_Net.xnf.getBoardVector(afterSO).bvec;

		//System.out.println("Random test: "+ rand.nextDouble());
		//System.out.println("Random test: "+ rand.nextDouble());
		
		m_Net.clearEligList();
		m_Net.setHorizon();
		if (DBG_OLD_3P || DBG_NEW_3P) {
			m_Net3.clearEligList();
			m_Net3.setHorizon();
		}
		
		m_counter=0;		// count the number of moves
		m_finished=false;
		while (true) {
	        if (TDNTuple2Agt.DBG2_FIXEDSEQUENCE) {
				VTable = new double[so.getNumAvailableActions()+1];
	        	actBest = this.getFirstAction(so, true, VTable, true);	// DEBUG only
	        } else {
				actBest = this.getNextAction2(so, true, true);	// the normal case 
	        }

	        m_randomMove = actBest.isRandomAction();
	        
	        m_numTrnMoves++;		// number of train moves (including random moves)
	               
	        ns = new NextState(this,so,actBest);	        // this contains advance
	        nextPlayer = ns.getNextSO().getPlayer();
	        
	        if (DBG_CUBE) {
	        	if (so instanceof StateObserverCube) {
	        		if (((StateObserverCube)so).getMinEpisodeLength()==3) {
	        			int dummy=1;
	        		}
	        	}
	        }
	        
	        if (DBG_NEW_3P && !m_randomMove) {
	        	nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState()).bvec;
	        	Z = - getGamma() * m_Net.getScoreI(nextBoard,nextPlayer);
	        	ZZ = - getGamma() * getScore(ns.getAfterState(),ns.getAfterState());
	        }
	        
	        if (DBG_REWARD) {
	        	System.out.println("nextSO: ["+ns.getNextSO().stringDescr()+"] for player "+nextPlayer);
	        }

	        if (VER_3P) 
	        {
	        	switch (UPDATE) {
	        	case MULTI_UPDATE:
	    			// do one training step (NEW target) for all players' value functions (VER_3P)
		        	int refPlayer=ns.getSO().getPlayer();
		        	rewardTuple=trainMultiUpdate_3P(ns,curBoard,learnFromRM,epiLength,oldRewardTuple);
		        	reward=rewardTuple.scTup[refPlayer];
//		        	if (DBG_OLD_3P) {
//		        		double reward3=trainNewTargetLogic2(ns,curBoard,learnFromRM,epiLength,oldReward,m_Net3);
//		        	}
//		        	if (DBG_REWARD) {
//						reward2=trainNewTargetLogic2(ns,curBoard,learnFromRM,epiLength,oldReward);	 
//						System.out.println("reward,reward2: "+reward+","+reward2);
//		        	}
		        	oldReward=oldRewardTuple.scTup[refPlayer];	        		
	        		break;	// out of switch
	        	case SINGLE_UPDATE:
	    			// do one training step (NEW target) only for current player
	        		Z_nply = actBest.getVBest();
	        		reward=trainSingleUpdate_3P(ns,Z_nply,curBoard,learnFromRM,epiLength,oldReward,m_Net);
	        		if (DBG_NEW_3P) dbg_Z_NEW_3P(ns,curBoard,learnFromRM,epiLength,reward,oldReward,Z,ZZ,Z_nply);
	        		break;	// out of switch
	        	} // switch
	        } 
	        else // i.e. VER_3P==false:
	        { 
				reward=trainNewTargetLogic2(ns,curBoard,learnFromRM,epiLength,oldReward,m_Net);
	        }
	        
			if (DBG2_TARGET) {
				final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
				System.out.print("R_t+1, r_t+1: "+reward*MAXSCORE
						+", "+(int)((reward-oldReward)*MAXSCORE+0.5) );
				System.out.println("|   est. final score: "+(reward+this.getScore(ns.getAfterState()))*MAXSCORE);
				if (reward==-1 && oldReward==-1) {
					int dummy=1;
				}
			}

			/* prepare for next pass through while-loop or for terminal update */ 
			so = ns.getNextSO();	// Only together with trainNewTargetLogic2	or trainNewTargetNEW_3P		
			curBoard = m_Net.xnf.getBoardVector(ns.getAfterState()).bvec;  
			curPlayer= nextPlayer;
			if (VER_3P && UPDATE==UpdateType.MULTI_UPDATE) {
				oldRewardTuple.scTup = rewardTuple.scTup.clone();
			} else {
				oldReward= reward;				
			}
			
			if (m_finished) {
				if (m_DEBG)
					if (m_randomMove) {
						pstream.println("Terminated game "+(getGameNum()) + " by random move. Reward = "+reward);						
					} else {
						pstream.println("Terminated game "+(getGameNum()) + ". Reward = "+reward);
					}
				
				break;		// out of while
			}

		} // while

		// learn for each final state that its value function (estimated further reward)
		// should be zero:
		if (VER_3P)  {
        	switch (UPDATE) {
        	case MULTI_UPDATE:
    			// do one training step (NEW target) for all players' value functions (VER_3P)
    			for (int i=0; i<so.getNumPlayers(); i++) {
    					m_Net.updateWeightsNewTerminal(curBoard, i,so, true);
    			}			
            	if (DBG_OLD_3P || DBG_NEW_3P) {
            		m_Net3.updateWeightsNewTerminal(curBoard, curPlayer, so, false);
            	}
            	break;
        	case SINGLE_UPDATE:
        		if (!TERNARY) {
        			// do one training step (NEW target) only for current player
        			// [Note: curPlayer is the player who generated curBoard, the afterstate preceding the 
        			// terminal state. This is because we reach the current point after a *break* out of the 
        			// while-loop, *before* curPlayer is updated to nextPlayer.] 
        			m_Net.updateWeightsNewTerminal(curBoard, curPlayer, so, false);
        		}
        		break;
        	} // switch
        } 
        else // i.e. VER_3P==false:
        { 
			// do one training step (NEW target) for current player 
			m_Net.updateWeightsNewTerminal(curBoard, curPlayer, so, false);
		}
		
		try {
			this.finishUpdateWeights();		// adjust learn params ALPHA & m_epsilon
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		//System.out.println("episode: "+getGameNum()+", moveNum="+m_counter);
		incrementGameNum();
		if (this.getGameNum() % 500 == 0) {
			System.out.println("gameNum: "+this.getGameNum());
		}
		
		// is now irrelevant since tcImm is always true
		if(getGameNum()%tcIn==0 && m_Net.getTc() && !m_Net.getTcImm()) {
			System.out.println("we should not get here!");
			m_Net.updateTC();
			if (DBG_OLD_3P || DBG_NEW_3P) m_Net3.updateTC();
		}
					
		//if (DEBG) m_Net.printLutSum(pstream);
		if (m_DEBG) m_Net.printLutHashSum(pstream);
		if (PRINTTABLES) {
			if(getGameNum()%10==0 && m_Net.getTc() )
				m_Net.printTables();
		}
		
		return false;
		
	} // trainAgent

	/**
	 * This is for the 3P target logic, i.e. {@link #VER_3P}==true and {@code MODE_3P}==0
	 * or [{@code MODE_3P}==2 AND N!=2 ] (since it is sub-optimal for 2-player games).
	 * <p>
	 * Each player has its own value function and the target for player p_i is <br>
	 * 		r(s_t+1, p_i) + \gamma*V(s_t+1,p_i) <br>
	 * which is the target for the actual value function V(s_t,p_i) and we perform a 
	 * multiple update (MULTI_UPDATE) of these value functions.
	 * 
	 * @return rewardTuple the tuple of rewards r(s_t+1, p_i) of {@code ns} for each player p_i
	 */
	private ScoreTuple trainMultiUpdate_3P(NextState ns,
			int[] curBoard, 
			boolean learnFromRM, int epiLength,  
			ScoreTuple oldRewardTuple) 
	{
		StateObservation thisSO=ns.getSO();
		StateObservation nextSO=ns.getNextSO();
		int[] nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState()).bvec;
		int thisPlayer= thisSO.getPlayer();
		int nextPlayer= nextSO.getPlayer();
		ScoreTuple rewardTuple;
		
		assert (VER_3P==true);
		
		rewardTuple = ns.getNextRewardTupleCheckFinished(epiLength); // updates m_counter, m_finished
//		rewardTuple = ns.getNextRewardTuple();
//		if (nextSO.isGameOver()) {
//			m_finished = true;
//		}
//		m_counter++;
//		if (m_counter==epiLength) {
//			rewardTuple=estimateGameValueTuple(nextSO);
//			m_finished = true; 
//		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move.
			if (m_DEBG)  pstream.println("random move");
			
			m_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET
				
		} else {
			// do one training step (NEW target) for all players' value functions (VER_3P)
			for (int i=0; i<thisSO.getNumPlayers(); i++) {
				if (curBoard!=null) {
					double rw = rewardTuple.scTup[i]-oldRewardTuple.scTup[i];
					// target is (reward + GAMMA * value of the after-state) for non-final states
					double target;
					if (TERNARY) {
						target = (m_finished) ? rw : getGamma() * m_Net.getScoreI(nextBoard,i);
					} else {
						target = rw + getGamma() * m_Net.getScoreI(nextBoard,i);
					}
					m_Net.updateWeightsNew(curBoard, i /*thisPlayer*/, nextBoard, i /*nextPlayer*/,
							rw,target,thisSO);
				}
			}
		}
		
		return rewardTuple;
		
	} // trainMultiUpdate_3P
	
	/**
	 * This is for the 3P target logic, i.e. {@link #VER_3P}==true and {@code MODE_3P}==1,
	 * that is the N-ply logic for all N-player games in its new form. 
	 * <p>
	 * Do only an update for the value function of {@code ns.getSO()}'s player 
	 * (i.e. SINGLE_UPDATE, only a single value function is updated)
	 * <p>
	 * This version is the recommended one for (N=2)-player games.
	 * 
	 * @param ns
	 * @param  target 
	 * 				the target for the weight update, which is here the Z-value returned
	 * 				from {@link ZValueSingleNPly}
	 * @param curBoard
	 * @param learnFromRM
	 * @param epiLength
	 * @param oldReward
	 * 
	 * @return reward, which is {@code ns.getNextReward()}
	 */
	private double trainSingleUpdate_3P(NextState ns, double target,
			int[] curBoard, 
			boolean learnFromRM, int epiLength,  
			double oldReward, NTuple2ValueFunc my_Net) 
	{
		double reward;
		
		StateObservation thisSO=ns.getSO();
		StateObservation nextSO=ns.getNextSO();
		int[] nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState()).bvec;
		int thisPlayer= thisSO.getPlayer();
		int nextPlayer= nextSO.getPlayer();
		
		assert (VER_3P==true);

		reward = ns.getNextRewardCheckFinished(epiLength);	// updates m_counter, m_finished
//		reward = ns.getNextReward();
//
//		if (nextSO.isGameOver()) {
//			m_finished = true;
//		}
//
//		m_counter++;
//		if (m_counter==epiLength) {
//			reward=estimateGameValue(nextSO);
//			m_finished = true; 
//		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move.
			if (m_DEBG)  pstream.println("random move");
			
			my_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET
				
		} else {
			// do one training step (NEW target)
			// target is passed as argument from ZValueSingleNPly  (n-ply look-ahead)
			if (curBoard!=null) {
				my_Net.updateWeightsNew(curBoard, thisPlayer, nextBoard, nextPlayer,
						reward-oldReward,target,thisSO);
			}
		}
		
		return reward;
		
	} // trainSingleUpdate_3P
	
	/**
	 * This is the new target logic for {@link VER_3P}=false (i.e. only for 1- and 2-player games)
	 * 
	 * @return reward
	 */
	private double trainNewTargetLogic2(NextState ns,
			int[] curBoard, 
			boolean learnFromRM, int epiLength,  
			double oldReward, NTuple2ValueFunc my_Net) 
	{
		double reward;
		
		StateObservation thisSO=ns.getSO();
		StateObservation nextSO=ns.getNextSO();
		int[] nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState()).bvec;
		int thisPlayer= thisSO.getPlayer();
		int nextPlayer= nextSO.getPlayer();
		
		reward = ns.getNextRewardCheckFinished(epiLength);	// updates m_counter, m_finished
//		reward = ns.getNextReward();
//		
//		if (nextSO.isGameOver()) {
//			m_finished = true;
//		}
//
//		m_counter++;
//		if (m_counter==epiLength) {
//			reward=estimateGameValue(nextSO);
//			m_finished = true; 
//		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move.
			if (m_DEBG)  pstream.println("random move");
			
			my_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET
				
		} else {
			// do one training step (NEW target)
			if (curBoard!=null) {
				double target;
				int sign=1;
				if (NEW_2P==true) sign=-1;
				// target is (reward + GAMMA * value of the after-state) for non-final states
				target = reward-oldReward + sign*getGamma() * my_Net.getScoreI(nextBoard,nextPlayer);
				my_Net.updateWeightsNew(curBoard, thisPlayer, nextBoard, nextPlayer,
						reward-oldReward,target,thisSO);
				if (DBG_NEW_3P) ZScore_NTL2=target;
			}
		}
		
		return reward;
		
	} 
	
	/**
	 * Fetch the reward for StateObservation so (relative to refer). <p>
	 * 
	 * Now deprecated: This function was needed in the old version (VER_3P=NEW_2P=false, prior
	 * to 10/2017), but it is neither very logical ('player*reward') nor is it extensible 
	 * to more than 2 players.
	 * 
	 * @param so		actual state
	 * @param refer		referring state
	 * @param playerPM	the player to move in state refer. 
	 * 					1-player games: always +1; 2-player games: +1 or -1
	 * @return playerPM*reward for actual state {@code so} 
	 */
	@Deprecated
	double fetchReward(StateObservation so, StateObservation refer, int playerPM) 
	{
		boolean rgs = m_oPar.getRewardIsGameScore();
		double reward = playerPM*so.getReward(refer,rgs);
		return normalize2(reward,so);
	}
	
	/**
	 * Fetch the reward for state {@code so} from the perspective of player in state 
	 * {@code refer}. This function is needed for {@link #VER_3P}=false, {@link #NEW_2P}=true.<p>
	 * 
	 * @param so		actual state
	 * @param refer		referring state
	 * @return reward for actual state {@code so} 
	 */
	double fetchReward2P(StateObservation so, StateObservation refer) 
	{
		boolean rgs = m_oPar.getRewardIsGameScore();
		double reward = so.getReward(refer,rgs);
		return normalize2(reward,so);
	}
	
	/**
	 * 
	 * @param score 
	 * @param so	needed for accessing getMinGameScore(), getMaxGameScore()
	 * @return normalized score to [-1,+1] (the appropriate range for tanh-sigmoid) if 
	 * 		switch {@link #m_tdPar}{@code .getNormalize()} is set.
	 */
	private double normalize2(double score, StateObservation so) {
		if (m_tdPar.getNormalize()) {
			// since we have - in contrast to TDAgent - here only one sigmoid
			// choice, namely tanh, we can take fixed [min,max] = [-1,+1]. 
			// If we would later extend to several sigmoids, we would have to 
			// adapt normalize2 accordingly:		
			score = normalize(score,so.getMinGameScore(),
							   		so.getMaxGameScore(),-1.0,+1.0);
		}
		return score;
	}
	
	/**
	 * Adjust {@code ALPHA} and adjust {@code m_epsilon}.
	 */
	public void finishUpdateWeights() {

		m_Net.finishUpdateWeights(); // adjust learn param ALPHA
		if (DBG_OLD_3P || DBG_NEW_3P) 
			m_Net3.finishUpdateWeights();

		// linear decrease of m_epsilon (re-activated 08/2017)
		m_epsilon = m_epsilon - m_EpsilonChangeDelta;

		if (PRINTTABLES) {
			try {
				print(m_epsilon);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void print(double m_epsilon2) throws IOException {
		PrintWriter epsilon = new PrintWriter(new FileWriter("epsilon",true));
		epsilon.println("" +m_epsilon2);
		epsilon.close();
	}

	public void setTDParams(ParTD tdPar, int maxGameNum) {
		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

//		NORMALIZE=tdPar.getNormalize();
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
	}

	public void setNTParams(ParNT ntPar) {
//		TC=ntPar.getTc();					// obsolete, use now m_Net.getTc()
//		tcImm=ntPar.getTcImm();				// obsolete, use now m_Net.getTcImm()
		tcIn=ntPar.getTcInterval();			// obsolete (since m_Net.getTcImm() always true)
		// not needed anymore here, we hand in int[][] nTuples = = ntupfac.makeNTupleSet(...) 
		// to the constructor: 
//		randomness=ntPar.getRandomness();
//		randWalk=ntPar.getRandomWalk();
		m_Net.setTdAgt(this);						 // WK: needed when loading an older agent
	}

	public void setAlpha(double alpha) {
		m_Net.setAlpha(alpha);
	}
	
	public void setFinished(boolean m) {
		this.m_finished=m;
	}

	public double getAlpha() {
		// only for debug & testing
		// super.counStates(1);
		int verbose1 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		//super.analyze_hmC(getGameNum(), verbose1);
		int verbose2 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		//super.analyze_hmX(verbose2);

		return m_Net.getAlpha();
	}

	public double getEpsilon() {
		return m_epsilon;
	}
	
	public double getGamma() {
		return m_tdPar.getGamma();
	}
	
	/**
	 * the number of calls to {@link NTuple2ValueFunc#update(int[], int, int, double, double, boolean, boolean)}
	 */
	@Override
	public long getNumLrnActions() {
		return m_Net.getNumLearnActions();
	}

	public void resetNumLearnActions() {
		m_Net.resetNumLearnActions();
	}
	
	@Override
	public int getMoveCounter() {
		return m_counter;
	}

	public NTuple2ValueFunc getNTupleValueFunc() {
		return m_Net;
	}
	

	public String stringDescr() {
		m_Net.setHorizon();
		String cs = getClass().getName();
		String str = cs + ": USESYMMETRY:" + (m_ntPar.getUSESYMMETRY()?"true":"false")
						+ ", NORMALIZE:" + (m_tdPar.getNormalize()?"true":"false")
						+ ", sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
						+ ", lambda:" + m_Net.getLambda()
						+ ", horizon:" + m_Net.getHorizon()
						+ ", AFTERSTATE:" + (m_ntPar.getAFTERSTATE()?"true":"false")
						+ ", learnFromRM: " + (m_oPar.getLearnFromRM()?"true":"false");
		return str;
	}
		
	public String stringDescr2() {
		String cs = getClass().getName();
		String str = cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
						+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal()
						+ ", gamma: " + m_tdPar.getGamma() +", MODE_3P: "+ m_tdPar.getMode3P()
						+ ", "+stringDescrNTuple();		
		return str;
	}
		
	/**
	 * @return a short description of the n-tuple configuration
	 */
	protected String stringDescrNTuple() {
		if (m_ntPar.getRandomness()) {
			return "random "+m_ntPar.getNtupleNumber() +" "+m_ntPar.getNtupleMax()+"-tuple";
		} else {
			int mode = m_ntPar.getFixedNtupleMode();
			return "fixed n-tuple, mode="+mode;
		}
	}
	
	public String printTrainStatus() {
		DecimalFormat frm = new DecimalFormat("#0.0000");
		DecimalFormat frme= new DecimalFormat();
		frme = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		frme.applyPattern("0.0E00");  

		String cs = ""; //getClass().getName() + ": ";   // optional class name
		String str = cs + "alpha="+frm.format(m_Net.getAlpha()) 
				   + ", epsilon="+frm.format(getEpsilon())
				   //+ ", lambda:" + m_Net.getLambda()
				   + ", "+getGameNum() + " games"
				   + " ("+frme.format(getNumLrnActions()) + " learn actions)";
		if (this.m_Net.getNumPlayers()==2) 
			str = str + ", (winX/tie/winO)=("+winXCounter+"/"+tieCounter+"/"+winOCounter+")";
		winXCounter=tieCounter=winOCounter=0;
		return str;
	}

	@Override
	public boolean isTrainable() { return true; }

	public ParTD getParTD() {
		return m_tdPar;
	}
	public ParNT getParNT() {
		return m_ntPar;
	}
	// getParOther() is in AgentBase
	
	public boolean getAFTERSTATE() {
		return m_ntPar.getAFTERSTATE();
	}
	public boolean getLearnFromRM() {
		return m_oPar.getLearnFromRM();
	}

	/**
	 * Return the weight update mode: <ul>
	 * <li> If {@link #MODE_3P}==0, do weight update for each player's V (MULTI)
	 * <li> If {@link #MODE_3P}==1, do weight update only for p_t in V(s_t) (SINGLE, nply=N)
	 * <li> If {@link #MODE_3P}==2: <ul>
	 * 		<li> If N==2, do weight update only for p_t in V(s_t) (SINGLE, nply=1)
	 * 		<li> else do weight update for each player's V (MULTI)
	 * 		</ul>
	 * </ul>
	 * See {@link #VER_3P} for more details.
	 * @param so	to get N=={@code so.getNumPlayers()}
	 * @return the update type
	 */
	public UpdateType getUpdateType(StateObservation so) {
		if (MODE_3P==1 || (MODE_3P==2 && so.getNumPlayers()==2))
			return UpdateType.SINGLE_UPDATE;	
		return UpdateType.MULTI_UPDATE;
	}
	
	// Callback function from NextState (because the counter needs to be on the agent-global
	// level)
	public void incrementMoveCounter() {
		m_counter++;
	}
	
	// Callback function from NextState (because the counters need to be on the agent-global
	// level)
	// *** This method currently works only for N<=2 players ***
	public void incrementWinCounters(double reward, NextState ns) {
		if (reward== 0.0) tieCounter++;
		if (reward==+1.0 & ns.refer.getPlayer()==0) winXCounter++;
		if (reward==+1.0 & ns.refer.getPlayer()==1) winOCounter++;
		if (reward==-1.0 & ns.refer.getPlayer()==0) winOCounter++;
		if (reward==-1.0 & ns.refer.getPlayer()==1) winXCounter++;
	}
	
	// Callback function from constructor NextState(NTupleAgt,StateObservation,ACTIONS). 
	// It sets various elements of NextState ns (nextReward, nextRewardTuple).
	// It is part of TDNTuple2Agt (and not part of NextState), because it uses various elements
	// private to TDNTuple2Agt (VER_3P, MODE_3P, NEW_2P, DBG_REWARD, normalize2, fetchReward*).
	// Returns a modified object NextState ns.
	public void collectReward(NextState ns) {
		UpdateType UPDATE = getUpdateType(ns.refer);

		if (VER_3P) {
        	switch (UPDATE) {
        	case MULTI_UPDATE:
				boolean rgs = m_oPar.getRewardIsGameScore();
				ns.nextRewardTuple = new ScoreTuple(ns.refer);
				for (int i=0; i<ns.refer.getNumPlayers(); i++) {
					ns.nextRewardTuple.scTup[i] = normalize2(ns.nextSO.getReward(i,rgs),ns.nextSO);
				}
				break;
        	case SINGLE_UPDATE:
				ns.nextReward = fetchReward2P(ns.nextSO,ns.refer);
	        	// this is r(s_{t+1}|p_t). It is equivalent to (-1)*r(s_{t+1}|p_{t+1}) for 
				// 2-player games.
				break;
			}
		} else {	// i.e. VER_3P==false
	        if (NEW_2P) {
				ns.nextReward = fetchReward2P(ns.nextSO,ns.refer);
	        	// this is r(s_{t+1}|p_t). It is equivalent to (-1)*r(s_{t+1}|p_{t+1}).
	        } else {
				ns.nextReward = fetchReward(ns.nextSO,ns.refer,Types.PLAYER_PM[ns.refer.getPlayer()]);
	        }
		}

		if (DBG_REWARD && ns.nextSO.isGameOver()) {
			if (VER_3P && MODE_3P==0) {
				System.out.print("Rewards: ");
				System.out.print(ns.nextRewardTuple.toString());
			} else {
				System.out.print("Reward: "+ns.nextReward);
			}
			System.out.println("   ["+ns.nextSO.stringDescr()+"]  " + ns.nextSO.getGameScore(ns.nextSO) 
							 + " for player " + ns.nextSO.getPlayer());
		}
	}
	
	// Debug only: 
	//
	private void printTable(PrintStream pstream, int[] board) {
		String s = NTuple2.stringRep(board);
		pstream.println(s + " : BestScore= "+BestScore);
	}

	private void printVTable(PrintStream pstream, double[] VTable) {
		for (int i=0; i<VTable.length; i++) {
			pstream.print(VTable[i]+", ");
		}
		System.out.println("");
	}
	
	private void sanityCheck1P(StateObservation so, ArrayList<ACTIONS> acts, int i,
			StateObservation refer, boolean silent, double CurrentScore)
	{
		// This is just a sanity check for  the special case of 1-player games (N_P=nply=1), 
		// e.g. 2048: Do ZValueMulti.calculate() and ZValueSingle.calculate() return the 
		// same value ?
		//  
		// (Does this work, if the nondeterministic part of advance may return something 
		// different? - Yes, it does, since we do only 1 ply and the nondeterministic part 
		// does not influence the value of CurrentScore, neither reward nor value 
		// function (at least in the game 2048). ) 
		ZValueSingleNPly zvalueS = new ZValueSingleNPly(this,so.getNumPlayers()); 
		ZValueMulti zvalueM = new ZValueMulti(this); 
	    double CurrentScoreOLD = zvalueM.calculate(so,acts.get(i),refer,silent);
	    double delta = CurrentScoreOLD-CurrentScore;
	    if (Math.abs(delta)>0.0) {
	        System.out.println("CurrentScore, CurrentScoreOLD, delta="+CurrentScore+",  "+CurrentScoreOLD+",  "+delta);
	        int dummy=1;
	        // We should never get here, if everything is OK. But if we get here, the following two 
	        // lines allow debugging the differences in both calls for the specific setting:
	        CurrentScore = zvalueS.calculate(so,acts.get(i),refer,silent);   
	        CurrentScoreOLD = zvalueM.calculate(so,acts.get(i),refer,silent);
	    }
	}
	
	private void dbg_Z_NEW_3P(NextState ns, int[] curBoard,boolean learnFromRM, int epiLength, 
							  double reward, double oldReward,double Z, double ZZ, double Z_nply) 
	{
		double reward2=0.0;
		reward2=trainNewTargetLogic2(ns,curBoard,learnFromRM,epiLength,oldReward,m_Net3);	 
		//System.out.println("reward,reward2: "+reward+",  "+reward2);
		if (!m_randomMove) {
			//System.out.println("Z_nply, ZScore_3: "+Z_nply+",  "+ZScore_NTL2);
			double rw = reward-oldReward;
			Z+=rw; ZZ+=rw;
			//System.out.println("Z_nply, Z, ZZ: "+Z_nply+",  "+Z+",   "+ZZ);
			if (Math.abs(Z_nply-Z)>1e-6 || Math.abs(Z_nply-Z)>1e-6) {
				int dummy=1;								
			}
		}
		if (reward!=reward2) {
			int dummy=1;
		}
	}
	
	/**
	 * see {@link NTuple2ValueFunc#weightAnalysis(double[])}
	 */
	public double[][] weightAnalysis(double[] per) {
		System.out.println("Training duration: "+(double)getDurationTrainingMs()/1000+" sec");
		System.out.println("Eval for training: "+(double)getDurationEvaluationMs()/1000+" sec");

		return m_Net.weightAnalysis(per);
	}
	
}