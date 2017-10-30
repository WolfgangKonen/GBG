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
import java.util.Locale;
import java.util.Random;

import agentIO.TDNTupleAgt_v12;
import params.NTParams;
import params.OtherParams;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import params.TDParams;
import tools.Types;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple.TDNTupleAgt;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.StateObservationNondeterministic;
import games.XNTupleFuncs;
import games.XArenaMenu;
import games.ZweiTausendAchtundVierzig.StateObserver2048;

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
 * The differences of {@link TDNTuple2Agt} to {@link TDNTupleAgt}:
 * <ul>
 * <li> no eligibility traces, instead LAMBDA-horizon mechanism of [Jaskowski16] (faster and less
 * 		memory consumptive)
 * <li> option AFTERSTATE (only for nondeterministic games like 2048), which builds the value 
 * 		function on the argument afterstate <b>s'</b> (before adding random element) instead 
 * 		of next state <b>s''</b> (faster learning and better generalization).
 * <li> fix of the random move rate bug (now EPSILON=0.0 means really 'no ramdom moves')
 * <li> learning rate ALPHA differently scaled: if ALPHA=1.0, the new value for a
 * 		state just trained will be exactly the target. Therefore, recommended ALPHA values are 
 * 		m*N_s bigger than in {@link TDNTupleAgt}, where m=number of n-tuples, N_s=number of 
 * 		symmetric (equivalent) states. 
 * <li> a change in the update formula: when looping over different equivalent
 * 		states, not more than one update per index is allowed (see comment in {@link NTuple2} for 
 * 		member {@code indexList}).
 * </ul>
 * 
 * @see TDNTupleAgt
 * @see PlayAgent
 * @see AgentBase
 * 
 * @author Wolfgang Konen, TH Köln, Aug'17
 */
//
// This agent is adapted from TDNTupleAgt
//
public class TDNTuple2Agt extends AgentBase implements PlayAgent,Serializable {
	private Random rand; // generate random Numbers 
	static transient public PrintStream pstream = System.out;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;

// --- OLD AND WRONG: ---
//	 * Let p=getGameNum()/getMaxGameNum(). As long as
//	 * {@literal p < m_epsilon/(1+m_epsilon) we have progress < 0 and} we get with 
//	 * certainty a random (explorative) move. For p \in [EPS/(1+EPS), 1.0] the random move
//	 * probability drops linearly from 1 to 0. <br>
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
	//samine//
	private boolean TC; //true: using Temporal Coherence algorithm
	private int tcIn; 	//temporal coherence interval: after tcIn games tcFactor will be updates
	private boolean tcImm=true;		//true: immediate TC update, false: batch update (epochs)
	private boolean randomness=false; //true: ntuples are created randomly (walk or points)
	private boolean randWalk=true; 	//true: random walk is used to generate nTuples
									//false: random points is used to generate nTuples//samine//

	// Value function of the agent.
	// Here: NTuple2ValueFunc
	private NTuple2ValueFunc m_Net;

	// Use symmetries (rotation, mirror) in NTuple-System
//	protected boolean USESYMMETRY = true; 	// use now m_ntPar.getUseSymmetry() - don't store/maintain value twice
	private boolean learnFromRM = false;    // use now m_oPar.useLearnFromRM() - don't store/maintain value twice
	private boolean NORMALIZE = false; 
	private boolean RANDINITWEIGHTS = false;// Init Weights of Value-Function
											// randomly
	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
//	public static boolean NEWTARGET=true;	// this is now always true
//	public static boolean WITH_NS=true;		// this is now always true
	/**
	 * If NEW_3P==true, use the new logic for 3-,...,n-player games: Each player has its own
	 * reward function and value function and in each update step n value function updates 
	 * occur.
	 * If NEW_3P==false, use the old logic (prior to 10/2017), which is valid only for 1- and
	 * 2-player games. The target to learn is the value/reward for the 1st player and only one 
	 * value function update is needed (exploits the 1- and 2-player symmetry). But this is not 
	 * generalizable to arbitrary games.
	 */
	public static boolean NEW_3P=true; 	

//	// if NEW_GNA==true: use the new function getNextAction2,3 in getNextAction;
//	//    (the new version returning Types.ACTIONS_VT and allowing deterministic multi-moves)
//	// if NEW_GNA==false: use the old function getNextAction1 in getNextAction;
//	//    (the old version returning Types.ACTIONS and not allowing multi-moves)
//	private static boolean NEW_GNA=true;	// this is now always true
	
	// debug printout in updateWeightsNew, getNextAction, trainAgent:
	public static boolean DBG2_TARGET=false;
	// debug printout in updateWeightsNewTerminal:
	public static boolean DBGF_TARGET=false;
	// debug: repeat always the same sequence in one episode (to check a trivial convergence)
	public static boolean DBG2_FIXEDSEQUENCE=false;
	
	//
	// from TDAgent
	//
	private int numFinishedGames = 0;
	private boolean randomSelect = false;
	
	/**
	 * Members {@link #m_tdPar}, {@link #m_ntPar}, {@link #m_oPar} are needed for 
	 * saving and loading the agent (to restore the agent with all its parameter settings)
	 */
//	private TDParams m_tdPar;
//	private NTParams m_ntPar;
	private ParTD m_tdPar;
	private ParNT m_ntPar;
	private ParOther m_oPar = new ParOther();
	
	//
	// variables needed in various train methods
	//
	private int m_counter = 0;				// count moves in trainAgent
	private boolean m_finished = false;		// whether a training game is finished
	private boolean m_randomMove = false;		// whether the last action was random
	private boolean m_DEBG = false;


	/**
	 * Default constructor for {@link TDNTuple2Agt}, needed for loading a serialized version
	 */
	public TDNTuple2Agt() throws IOException {
		super();
		TDParams tdPar = new TDParams();
		NTParams ntPar = new NTParams();
		OtherParams oPar = new OtherParams();
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
	public TDNTuple2Agt(String name, TDParams tdPar, NTParams ntPar, OtherParams oPar, 
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
	private void initNet(NTParams ntPar, TDParams tdPar, OtherParams oPar,  
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		m_tdPar = new ParTD(tdPar);
		m_ntPar = new ParNT(ntPar);
		m_oPar = new ParOther(oPar);
		rand = new Random(42); //(System.currentTimeMillis());		
		
		int posVals = xnf.getNumPositionValues();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells);

		setNTParams(ntPar);
		
		setTDParams(tdPar, maxGameNum);
		
		setAgentState(AgentState.INIT);
	}

//	/**
//	 * Get the best next action and return it
//	 * 
//	 * @param so			current game state (is returned unchanged)
//	 * @param random		allow epsilon-greedy random action selection	
//	 * @param VTable		the score for each available action (corresponding
//	 * 						to sob.getAvailableActions())
//	 * @param silent
//	 * @return actBest		the best action. If several actions have the same
//	 * 						score, break ties by selecting one of them at random 
//	 * 						
//	 * actBest has member isRandomAction()  (true: if action was selected 
//	 * at random, false: if action was selected by agent).
//	 */
//	@Deprecated
//	public Types.ACTIONS getNextAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
//		// this function selector is just intermediate, as long as we want to test getNextAction2 
//		// against getNextAction1 (the former getNextAction). Once everything works fine with
//		// getNextAction2, we should use only this function and make getNextAction deprecated 
//		// (requires appropriate changes in all other agents implementing interface PlayAgent).
//		if (!NEW_GNA) {
//			return getNextAction1(so, random, VTable, silent);
//		} else {
//			Types.ACTIONS_VT actBestVT = getNextAction2(so, random, silent);
//			double[] vtable = actBestVT.getVTable();
//			for (int i=0; i<vtable.length; i++) VTable[i] = normalize2(vtable[i],so);
//			VTable[vtable.length] = normalize2(actBestVT.getVBest(),so);
//			return actBestVT;
//		}
//	}
//	// this is the old getNextAction function (prior to 09/2017):
//	private Types.ACTIONS getNextAction1(StateObservation so, boolean random, double[] VTable, boolean silent) {
//		int i, j;
//		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
//									// maximized
//		StateObservation NewSO;
//		int count = 1; // counts the moves with same BestScore
//        Types.ACTIONS actBest = null;
//        int iBest;
//		BestScore = -Double.MAX_VALUE;
//       
//		if (so.getNumPlayers()>2)
//			throw new RuntimeException("TDNTuple2Agt.getNextAction1 does not yet "+
//									   "implement case so.getNumPlayers()>2");
//
//		int player = Types.PLAYER_PM[so.getPlayer()]; 	 
//	
//// --- this code is suspicious and not intuitive (random moves even in case m_epsilon==0) ---		
////		randomSelect = false;
////		double progress = (double) getGameNum() / (double) getMaxGameNum();
////		progress = (1 + m_epsilon) * progress - m_epsilon; 	// = progress +
////															// m_EPS*(progress - 1)
////		if (random) {
////			double rd = rand.nextDouble();
////			//System.out.println("rd="+rd);
////			if (rd > progress) {
////				randomSelect = true;
////			}
////		}
//		
//        randomSelect = false;
//		if (random) {
//			randomSelect = (rand.nextDouble() < m_epsilon);
//		}
//		
//		// get the best (or eps-greedy random) action
//        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
//        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
//        double soReward = so.getReward(so); // 0; 
//        double agentScore;
//        //VTable = new double[acts.size()];  
//        // DON'T! The caller has to define VTable with the right length
//        
//        assert actions.length>0 : "Oops, no available action";
//        for(i = 0; i < actions.length; ++i)
//        {
//            actions[i] = acts.get(i);		
//	        
//            NewSO = so.copy();
//            if (getAFTERSTATE()) {
//            	assert (NewSO instanceof StateObservationNondeterministic);
//                ((StateObservationNondeterministic) NewSO).advanceDeterministic(actions[i]); 
//                agentScore = getScore(NewSO);
//                ((StateObservationNondeterministic) NewSO).advanceNondeterministic(); 
//            } else {
//                NewSO.advance(actions[i]);
//                agentScore = getScore(NewSO);
//            }
//            
//            // this alternative implementation has less code, but is much (40%-70%!!) slower: 
////          double agentScore2;
////    		StateObservation NewSO2;
////	        ns = new NextState(so,actions[i]);
////	        agentScore2 = getScore(ns.getAfterState());
////	        NewSO2 = ns.getNextSO();
////	        assert agentScore==agentScore2 : "Oops, agentScore and agentScore2 differ!";
//
////			if (!NEWTARGET) {
////				throw new RuntimeException("NEWTARGET==false no longer supported in TDNTuple2Agt!");
//////				// old target logic:
//////				if (NewSO.isGameOver()) {
//////					CurrentScore = NewSO.getReward(so);
//////				}  else {
//////					// old target logic:
//////					// the score is just the agent's value function for NewSO. In this case
//////					// the agent has to learn in the value function the sum 
//////					// 		"rewards received plus future rewards"
//////					// itself. 
//////					CurrentScore = player * agentScore;
//////											// here we ask this agent for its score estimate on NewSO
//////				}
////			} else {
//				// new target logic:
//				// the score is the reward received for the transition from so to NewSO 
//				// 		(NewSO.getReward(so)-soReward)
//				// plus the estimated future rewards until game over (getScore(NewSO), 
//				// the agent's value function for NewSO)
//				CurrentScore = (NewSO.getReward(so) - soReward) + player * agentScore;				
////			}
//			
//			// just a debug check:
//			if (Double.isInfinite(agentScore)) {
//				System.out.println("getScore(NewSO) is infinite!");
//			}
//			
//			CurrentScore = normalize2(CurrentScore,so);					
//			
//			if (!silent)
//				System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*player-1));
//				//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
//			if (randomSelect) {
//				double rd2 = rand.nextDouble();
//				//System.out.println("rd CS="+rd2);
//				CurrentScore = rd2;
//			}
//
//			//
//			// fill VTable, calculate BestScore and actBest:
//			//
//			VTable[i] = CurrentScore;
//			if (BestScore < CurrentScore) {
//				BestScore = CurrentScore;
//				actBest = actions[i];
//				iBest  = i; 
//				count = 1;
//			} else if (BestScore == CurrentScore) {
//				// If there are 'count' possibilities with the same score BestScore, 
//				// each one has the probability 1/count of being selected.
//				// 
//				// (To understand formula, think recursively from the end: the last one is
//				// obviously selected with prob. 1/count. The others have the probability 
//				//      1 - 1/count = (count-1)/count 
//				// left. The previous one is selected with probability 
//				//      ((count-1)/count)*(1/(count-1)) = 1/count
//				// and so on.) 
//				count++;
//				if (rand.nextDouble() < 1.0/count) {
//					actBest = actions[i];
//					iBest  = i; 
//				}
//			}
//        } // for
//
//        assert actBest != null : "Oops, no best action actBest";
//        actBest.setRandomSelect(randomSelect);
//        
//		if (!silent) {
//			System.out.print("---Best Move: ");
//            NewSO = so.copy();
//            NewSO.advance(actBest);
//			System.out.println(NewSO.stringDescr()+", "+(2*BestScore*player-1));
//		}			
//		if (DBG2_TARGET) {
//			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
//			// the old version, which is only correct for AFTERSTATE==false:
////          NewSO = so.copy();
////          NewSO.advance(actBest);
////          double deltaReward = NewSO.getReward(so) - so.getReward(so);
////			double sc = (deltaReward + player * getScore(NewSO))*MAXSCORE;
//												// this is problematic when AFTERSTATE==true (!)
//			// here we use the NextState version, because computation time does not matter
//			// inside DBG2_TARGET and because this version is correct for both values of 
//			// getAFTERSTATE():
//	        NextState ns = new NextState(so,actBest);
//			double deltaReward = ns.getNextSO().getReward(so) - so.getReward(so);
//			double sc = (deltaReward + player * getScore(ns.getAfterState()))*MAXSCORE;
//			
//			System.out.println("getScore((so,actbest)-afterstate): "+sc+"   ["+so.stringDescr()+"]");
//			int dummy=1;
//		}
//		
//		return actBest;
//	}

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
	// 
	// this private function is needed so that the recursive call inside getNextAction3 can 
	// transfer the referring state refer
	private Types.ACTIONS_VT getNextAction3(StateObservation so, StateObservation refer, 
			boolean random, boolean silent) {
		int i, j;
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		boolean rgs = m_oPar.getRewardIsGameScore();
		StateObservation NewSO;
		int count = 1; // counts the moves with same BestScore
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        int iBest;
		BestScore = -Double.MAX_VALUE;
		double[] VTable;
       
		if (so.getNumPlayers()>2)
			throw new RuntimeException("TDNTuple2Agt.getNextAction2 does not yet "+
									   "implement case so.getNumPlayers()>2");

		int player = Types.PLAYER_PM[refer.getPlayer()]; 	 
	
        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
		
		// get the best (or eps-greedy random) action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        double agentScore;
        VTable = new double[acts.size()];  
        
        assert actions.length>0 : "Oops, no available action";
        for(i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);		
	        
            CurrentScore = g3_Evaluate(so,actions[i],refer,silent);
				
			// just a debug check:
			if (Double.isInfinite(CurrentScore)) {
				System.out.println("getScore(NewSO) is infinite!");
			}
			
			CurrentScore = normalize2(CurrentScore,so);					
			
			//
			// fill VTable, calculate BestScore and actBest:
			//
			VTable[i] = CurrentScore;
			if (BestScore < CurrentScore) {
				BestScore = CurrentScore;
				actBest = actions[i];
				iBest  = i; 
				count = 1;
			} else if (BestScore == CurrentScore) {
				// If there are 'count' possibilities with the same score BestScore, 
				// each one has the probability 1/count of being selected.
				// 
				// (To understand formula, think recursively from the end: the last one is
				// obviously selected with prob. 1/count. The others have the probability 
				//      1 - 1/count = (count-1)/count 
				// left. The previous one is selected with probability 
				//      ((count-1)/count)*(1/(count-1)) = 1/count
				// and so on.) 
				count++;
				if (rand.nextDouble() < 1.0/count) {
					actBest = actions[i];
					iBest  = i; 
				}
			}
        } // for

        assert actBest != null : "Oops, no best action actBest";
		if (!silent) {
			System.out.print("---Best Move: ");
            NewSO = so.copy();
            NewSO.advance(actBest);
			System.out.println(NewSO.stringDescr()+", "+(2*BestScore*player-1));
		}			
		if (DBG2_TARGET) {
			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
			// here we use the NextState version, because computation time does not matter
			// inside DBG2_TARGET and because this version is correct for both values of 
			// getAFTERSTATE():
	        NextState ns = new NextState(so,actBest);
			double deltaReward = ns.getNextSO().getReward(so,rgs) - so.getReward(so,rgs);
			double sc = (deltaReward + player * getScore(ns.getAfterState()))*MAXSCORE;
			
			System.out.println("getScore((so,actbest)-afterstate): "+sc+"   ["+so.stringDescr()+"]");
			int dummy=1;
		}
		
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, BestScore);
		return actBestVT;
	}

    // calculate CurrentScore: 
	// (g3_Evaluate is helper function for getNextAction3)
    private double g3_Evaluate(	StateObservation so, Types.ACTIONS act, 
    							StateObservation refer, boolean silent) {
    	double CurrentScore,agentScore;
		int player = Types.PLAYER_PM[refer.getPlayer()]; 	 
		boolean rgs = m_oPar.getRewardIsGameScore();

        double referReward = refer.getReward(refer,rgs); // 0; 
    	StateObservation NewSO;
    	Types.ACTIONS_VT actBestVT;

		if (randomSelect) {
			CurrentScore = rand.nextDouble();
			return CurrentScore;
		} 
        NewSO = so.copy();
        
//        if (getAFTERSTATE() && !so.isDeterministicGame()) {
//        	assert (NewSO instanceof StateObservationNondeterministic);
//            ((StateObservationNondeterministic) NewSO).advanceDeterministic(act); 
//            agentScore = getScore(NewSO);
//            ((StateObservationNondeterministic) NewSO).advanceNondeterministic(); 
//			CurrentScore = (NewSO.getReward(refer) - referReward) + getGamma()*player*agentScore;
//			return CurrentScore;
//        } 
//        
//    	// the normal part for the case of single moves:
//        NewSO.advance(act);
//        agentScore = getScore(NewSO);

        if (getAFTERSTATE()) {
        	NewSO.advanceDeterministic(act); 	// the afterstate
            agentScore = getScore(NewSO);		// this is V(s')
            NewSO.advanceNondeterministic(); 
        } else { 
        	// the non-afterstate logic for the case of single moves:
            NewSO.advance(act);
            agentScore = getScore(NewSO);      	// this is V(s'')  	
        }
        // both ways of calculating the agent score are the same for deterministic games (s'=s''),
        // but they usually differ for nondeterministic games.

        // the recursive part (only for deterministic games) is for the case of 
        // multi-moves: the player who just moved gets from StateObservation 
        // the signal for one (or more) additional move(s)
        if (so.isDeterministicGame() && so.getNumPlayers()>1 && !NewSO.isGameOver()) {
            int newPlayer =  Types.PLAYER_PM[NewSO.getPlayer()];
            if (newPlayer==player) {
            	actBestVT = getNextAction3(NewSO, refer, false, silent);
            	NewSO.advance(actBestVT);
            	CurrentScore = actBestVT.getVBest();
            	return CurrentScore;
            }
        }
	            
		// new target logic:
		// the score is the reward received for the transition from refer to NewSO 
		// 		(NewSO.getReward(refer)-referReward)
		// plus the estimated future rewards until game over (getScore(NewSO), 
		// the agent's value function for NewSO)
        if (NEW_3P) player=1;
		CurrentScore = (NewSO.getReward(refer,rgs) - referReward) + getGamma()*player*agentScore;				

		if (!silent) {
			System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*player-1));
			//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
		}

		return CurrentScore;
    }

	// DEBUG only: return always the 1st available action (for deterministic training games)
	public Types.ACTIONS getFirstAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
		boolean rgs = m_oPar.getRewardIsGameScore();
        Types.ACTIONS actBest = null;
		StateObservation NewSO;
		
		// get the first action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        actBest = acts.get(0);
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
	 * 
	 * @return	returns true/false, whether the action suggested by last call 
	 * 			to getNextAction() was a random action 
	 */
	@Deprecated
	public boolean wasRandomAction() {
		return randomSelect;
	}
	// TODO: change PlayAgent's interface and other locations to use ACTIONS::isRandomAction()
	// instead of wasRandomAction()

	/**
	 * Return the agent's estimate of the score for that after state.
	 * For 2-player games like TTT the score is V(), the prob. that X (Player +1) wins 
	 * from that after state. Player*V() is the quantity to be maximized by getNextAction2.
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * 
	 * @param so			the current game state;
	 * @return the agent's estimate of the future score for that after state
	 */
	public double getScore(StateObservation so) {
		int[] bvec = m_Net.xnf.getBoardVector(so);
		double score = m_Net.getScoreI(bvec,so.getPlayer());
		return score;
	}

	/**
	 * Train the agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState01()} to get
	 * 					some exploration of different game paths)
// --- epiLength, learnFromRM are now available via the agent's member ParOther m_oPar: ---
//	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
//	 * 					prematurely.  
//	 * @param learnFromRM if true, learn from random moves during training
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so) {
		double[] VTable = null;
		double reward = 0.0, oldReward = 0.0;
		double[] rewardArr, oldRewardArr;
		boolean wghtChange = false;
		boolean upTC=false;
		double Input[], oldInput[];
		String S_old = null;   // only as debug info
		int player;
		Types.ACTIONS actBest;
		StateObservation oldSO;
		StateObservation aftSO;
		int[] curBoard;
		int   curPlayer=so.getPlayer();
		int[] nextBoard = null;
		int   nextPlayer;
		NextState ns = null;

		boolean learnFromRM = m_oPar.useLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		
		rewardArr = new double[so.getNumPlayers()];
		oldRewardArr = new double[so.getNumPlayers()];


		aftSO = so.getPrecedingAfterstate();
    	curBoard = (aftSO==null) ? null : m_Net.xnf.getBoardVector(aftSO);

		//System.out.println("Random test: "+ rand.nextDouble());
		//System.out.println("Random test: "+ rand.nextDouble());
		
		player = Types.PLAYER_PM[so.getPlayer()];

		m_Net.clearEquivList();
		m_Net.setHorizon();
		
		m_counter=0;		// count the number of moves
		m_finished=false;
		while (true) {
			VTable = new double[so.getNumAvailableActions()+1];
	        if (TDNTuple2Agt.DBG2_FIXEDSEQUENCE) {
	        	actBest = this.getFirstAction(so, true, VTable, true);	// DEBUG only
	        } else {
				actBest = this.getNextAction2(so, true, true);	// the normal case 
	        }

	        m_randomMove = actBest.isRandomAction();
//			m_randomMove = this.wasRandomAction();
	        
	        m_numTrnMoves++;		// number of train moves (including random moves)
	               
	        
	        ns = new NextState(so,actBest);	        
	        nextPlayer = ns.getNextSO().getPlayer();
	        

	        if (NEW_3P) {
	        	int refPlayer=ns.getSO().getPlayer();
	        	rewardArr=trainNewTargetNEW_3P(ns,curBoard,learnFromRM,epiLength,oldRewardArr);
	        	reward=rewardArr[refPlayer];
	        	oldReward=oldRewardArr[refPlayer];
	        } else {
//			if (NEWTARGET) {
//				if (WITH_NS) {
					reward=trainNewTargetLogic2(ns,curBoard,learnFromRM,epiLength,oldReward);
//				} else {
//					reward=trainNewTargetLogic(so,oldSO,curBoard,curPlayer,nextBoard,nextPlayer,
//							learnFromRM,epiLength,player,upTC,oldReward);	
//				}
//			} else {
//				throw new RuntimeException("NEWTARGET==false no longer supported in TDNTuple2Agt!");
////				reward=trainOldTargetLogic(so,oldSO,curBoard,curPlayer,nextBoard,nextPlayer,
////						learnFromRM,epiLength,player,upTC);
//			}
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
//			if (WITH_NS) {
				so = ns.getNextSO();	// Only together with trainNewTargetLogic2	or trainNewTargetNEW_3P		
				curBoard = m_Net.xnf.getBoardVector(ns.getAfterState());  
//			} else {
//				//curBoard = nextBoard; 	// BUG!! A later change to nextBoard will change curBoard as well!!
//											// But probably w/o effect, since the next use of nextBoard is
//											//  	nextBoard = ...getBoardVector(), 
//											// and this sets nextBoard to new int[] ... 
//				curBoard = nextBoard.clone();
//			}
			curPlayer= nextPlayer;
			if (NEW_3P) {
				for (int i=0; i<so.getNumPlayers(); i++) oldRewardArr[i] = rewardArr[i];
			} else {
				oldReward= reward;				
			}
			player = Types.PLAYER_PM[so.getPlayer()];   // advance to the next player
			
			if (m_finished) {
				if (m_DEBG)
					if (m_randomMove) {
						pstream.println("Terminated game "+(getGameNum()) + " by random move. Reward = "+reward);						
					} else {
						pstream.println("Terminated game "+(getGameNum()) + ". Reward = "+reward);
					}
				
				break;
			}

		} // while

//		if (NEWTARGET) {		
		// learn for each final state that the value function (estimated further reward)
		// should be zero:
		if (NEW_3P) {
			// do one training step (NEW target) for all players' value functions (NEW_3P)
			for (int i=0; i<so.getNumPlayers(); i++) {
					m_Net.updateWeightsNewTerminal(curBoard, i);
			}			
		} else {
					m_Net.updateWeightsNewTerminal(curBoard, curPlayer);
		}
//		}
		
		try {
			this.finishUpdateWeights();		// adjust learn params ALPHA & m_epsilon
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		incrementGameNum();
		if (this.getGameNum() % 500 == 0) {
			System.out.println("gameNum: "+this.getGameNum());
			int dummy=1;
		}
		//samine// updating tcFactor array after each complete game
		if(getGameNum()%tcIn==0 && TC && !tcImm)
			 m_Net.updateTC();
		
		//if (DEBG) m_Net.printLutSum(pstream);
		if (m_DEBG) m_Net.printLutHashSum(pstream);
		if (PRINTTABLES) {
			if(getGameNum()%10==0 && TC)
				m_Net.printTables();
		}
		
		return false;
		
	} // trainAgent

	/**
	 * This is for the new target logic, *with* NEW_3P (i.e. for all N-player games).
	 * Each player has its own value function and the target for player p_i is
	 * 		r(s_t+1, p_i) + \gamma*V(s_t+1,p_i)
	 * which is the target for the actual value function V(s_t,p_i)
	 * 
	 * @return rewardArr
	 */
	private double[] trainNewTargetNEW_3P(NextState ns,
			int[] curBoard, 
			boolean learnFromRM, int epiLength,  
			double[] oldRewardArr) 
	{
		StateObservation thisSO=ns.getSO();
		StateObservation nextSO=ns.getNextSO();
		int[] nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState());
		int thisPlayer= thisSO.getPlayer();
		int nextPlayer= nextSO.getPlayer();
		double[] rewardArr = ns.getNextRewardArr();
		
		if (nextSO.isGameOver()) {
			m_finished = true;
		}

		m_counter++;
		if (m_counter==epiLength) {
			// TODO:
			//rewardArr=...;
			
			//epiCount++;
			m_finished = true; 
		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move.
			if (m_DEBG)  // only for diagnostics:
				pstream.println("random move");
		} else {
			// do one training step (NEW target) for all players' value functions (NEW_3P)
			for (int i=0; i<thisSO.getNumPlayers(); i++) {
				if (curBoard!=null) {
					m_Net.updateWeightsNew(curBoard, i /*thisPlayer*/, nextBoard, i /*nextPlayer*/,
							rewardArr[i]-oldRewardArr[i],false);
				}
			}
		}
		
		return rewardArr;
		
	} 
	
	/**
	 * This is for the new target logic, but before NEW_3P (i.e. only for 1- and 2-player games)
	 * 
	 * @return reward
	 */
	private double trainNewTargetLogic2(NextState ns,
			int[] curBoard, 
			boolean learnFromRM, int epiLength,  
			double oldReward) 
	{
		double reward;
		
		StateObservation thisSO=ns.getSO();
		StateObservation nextSO=ns.getNextSO();
		int[] nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState());
		int thisPlayer= thisSO.getPlayer();
		int nextPlayer= nextSO.getPlayer();
		
		//reward = fetchReward(nextSO,thisSO,Types.PLAYER_PM[thisSO.getPlayer()]);
		reward = ns.getNextReward();
		
		if (nextSO.isGameOver()) {
			m_finished = true;
		}

		m_counter++;
		if (m_counter==epiLength) {
			reward=estimateGameValue(nextSO);
			//epiCount++;
			m_finished = true; 
		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move.
			if (m_DEBG)  // only for diagnostics:
				pstream.println("random move");
		} else {
			// do one training step (NEW target)
			if (curBoard!=null) {
				m_Net.updateWeightsNew(curBoard, thisPlayer, nextBoard, nextPlayer,
						reward-oldReward,false);
			}
		}
		
		return reward;
		
	} 
	
//	/**
//	 * this is the NEWTARGET version, but WITH_NS=false (that is without using NextState)
//   *	
//	 * @return reward
//	 */
//	private double trainNewTargetLogic(
//			StateObservation so, StateObservation oldSO, 
//			int[] curBoard, int curPlayer,
//			int[] nextBoard, int nextPlayer, boolean learnFromRM, 
//			int epiLength, int player, boolean upTC, double oldReward) 
//	{
//		double reward;
//		
//		reward = fetchReward(so,oldSO,player);
//		
//		if (so.isGameOver()) {
//			m_finished = true;
//		}
//
//		m_counter++;
//		if (m_counter==epiLength) {
//			reward=estimateGameValue(so);
//			//epiCount++;
//			m_finished = true; 
//		}
//		
//		if (m_randomMove && !learnFromRM) {
//			// no training, go to next move.
//			// only for diagnostics
//			if (m_DEBG)
//				pstream.println("random move");
//		} else {
//			// do one training step (NEW target)
//			if (curBoard!=null) {
//				m_Net.updateWeightsNew(curBoard, curPlayer, nextBoard, nextPlayer,
//						reward-oldReward,upTC);
//			}
//		}
//		
//		return reward;
//		
//	} 
	
	/**
	 * Fetch the reward for StateObservation so (relative to refer). <p>
	 * 
	 * Now deprecated: This function was needed for the version before NEW_3P, but it is 
	 * neither very logical ('player*reward') nor is it extensible to more than 2 players.
	 * 
	 * @param so		actual state
	 * @param refer		referring state
	 * @param player	the player to move in state refer. 
	 * 					1-player games: always +1; 2-player games: +1 or -1
	 * @return player*reward (total gameScore) for actual state {@code so} 
	 */
	@Deprecated
	private double fetchReward(StateObservation so, StateObservation refer, int player) 
	{
		boolean rgs = m_oPar.getRewardIsGameScore();
		double reward = player*so.getReward(refer,rgs);
		return normalize2(reward,so);
	}
	
	private double normalize2(double score, StateObservation so) {
		if (NORMALIZE) {
			// Normalize to [-1,+1] (the appropriate range for tanh-sigmoid):
			//			
			// since we have - in contrast to TDAgent - here only one sigmoid
			// choice, namely tanh, we can take fixed [min,max] = [-1,+1]. 
			// If we would later extend to several sigmoids, we would have to 
			// adapt here:		
			score = normalize(score,so.getMinGameScore(),
							   		so.getMaxGameScore(),-1.0,+1.0);
		}
		return score;
	}
	
	/**
	 * Class NextState bundles the different states in state advancing and two different modes of 
	 * state advancing.
	 * <p>
	 * If {@link TDNTuple2Agt#getAFTERSTATE()}==false, then {@code ns=new NextState(so,actBest)}  
	 * simply advances {@code so} and lets ns.getAfterState() and ns.getNextSO() return the same next 
	 * state so.advance(actBest). 
	 * <p>
	 * If {@link TDNTuple2Agt#getAFTERSTATE()}==true, then {@code ns=new NextState(so,actBest)}  
	 * advances {@code so} in two steps: ns.getAfterState() returns the <b>afterstate s'</b> (after  
	 * the deterministic advance (e.g. the merge in case of 2048)) and ns.getNextSO() returns  
	 * the next state <b>s''</b> (after adding the nondeterministic part (e.g. adding the random 
	 * tile in case of 2048)).
	 * <p>
	 * The nondeterministic part is done once at the time of constructing an object of class
	 * {@code NextState}, so multiple calls to {@code ns.getNextSO()} are guaranteed to return
	 * the  same state.
	 * <p> 
	 * For deterministic games, the behavior is identical to the case with 
	 * {@code getAFTERSTATE()==false}: ns.getAfterState() and ns.getNextSO() return the same  
	 * next state so.advance(actBest).
	 * 
	 * @see TDNTuple2Agt#getAFTERSTATE()
	 * @see TDNTuple2Agt#trainAgent(StateObservation)
	 */
	class NextState {
		StateObservation refer;
		StateObservation afterState;
		StateObservation nextSO;
		double thisReward;			// TODO
		double nextReward;
		
		/**
		 * an array of size {@code N = nextSO.getNumPlayers()} holding  the reward of 
		 * {@code nextSO} from the perspective of player 0,1,...N-1
		 */
		private double nextRewardArr[] = null;	
		
		/**
		 * Advance state <b>s</b> = {@code so} by action {@code actBest}. Store the afterstate 
		 * <b>s'</b> and the next state <b>s''</b>. 
		 */
		NextState(StateObservation so, Types.ACTIONS actBest) {
			refer = so.copy();
	        if (getAFTERSTATE()) {
	        	/* assertion not needed anymore, advanceDeterministic is also part of StateObservation */ 
            	// assert (so instanceof StateObservationNondeterministic);
	        	
            	// implement it in such a way that StateObservation so is *not* changed -> that is why 
            	// we copy *first* to afterState, then advance:
                afterState = so.copy();
                afterState.advanceDeterministic(actBest);
                nextSO = afterState.copy();
                nextSO.advanceNondeterministic(); 
	        	
	        } else {
                nextSO = so.copy();
                nextSO.advance(actBest);
				afterState = nextSO.copy();
	        }

			nextReward = fetchReward(nextSO,refer,Types.PLAYER_PM[refer.getPlayer()]);
			
			if (NEW_3P) {
				boolean rgs = m_oPar.getRewardIsGameScore();
				int nPlayers= refer.getNumPlayers();
				int player=refer.getPlayer();
				nextRewardArr = new double[nPlayers];
				for (int i=0; i<nPlayers; i++) {
					nextRewardArr[player] = normalize2(nextSO.getReward(player,rgs),nextSO);
					player=(player+1) % nPlayers;
				}
			}

		}

		/**
		 * @return the original state <b>s</b> = {@code so}
		 */
		public StateObservation getSO() {
			return refer;
		}

		/**
		 * @return the afterstate <b>s'</b>
		 */
		public StateObservation getAfterState() {
			return afterState;
		}

		/**
		 * @return the next state <b>s''</b> (random part from environment added).
		 */
		public StateObservation getNextSO() {
			return nextSO;
		}

		@Deprecated
		public double getThisReward() {
			return thisReward;
		}

		public double getNextReward() {
			return nextReward;
		}

		/**
		 * Returns an array of size {@code N = nextSO.getNumPlayers()}
		 * 
		 * @return the reward of {@code nextSO} from the perspective of player 0,1,...N-1
		 */
		public double[] getNextRewardArr() {
			return nextRewardArr;
		}

}
	/**
	 * Adjust {@code ALPHA} and adjust {@code m_epsilon}.
	 */
	public void finishUpdateWeights() {
		// m_Net.setAlpha(0.0009*Math.exp(-0.03*(getGameNum()+1))+0.0001);
		// m_Net.setAlpha(-(1.0/Math.pow(4000.0,4))*(0.1e-2-0.1e-3)*Math.pow(getGameNum(),4)+0.1e-2);

		// double a0 = 1;
		// double a1 = 0.1;
		// double x = (double)getGameNum()/getMaxGameNum()*8.0;
		// double fx = (a0-a1)/2.0*(1-Math.tanh(x-4)) + a1;
		// m_Net.setAlpha(fx);

		m_Net.finishUpdateWeights(); // adjust learn param ALPHA

		// linear decrease of m_epsilon (re-activated 08/2017)
		m_epsilon = m_epsilon - m_EpsilonChangeDelta;

		// the suspicious version before 08/2017
//		double a0 = m_tdPar.getEpsilon();
//		double a1 = m_tdPar.getEpsilonFinal();
//		// double x = (double) getGameNum() / (getMaxGameNum()) * 10.0;
//		double x = (double) ((getGameNum() - 1000.0) / getMaxGameNum())*5;
//		double fx = (((a0 - a1) / 2.0 )* (1.0 - Math.tanh(x)) )+ a1;
//		m_epsilon = fx;

		if (PRINTTABLES) {
			try {
				print(m_epsilon);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void print(double m_epsilon2) throws IOException {
		PrintWriter epsilon = new PrintWriter(new FileWriter("epsilon",true));
		epsilon.println("" +m_epsilon2);
		epsilon.close();
	}

	public void setTDParams(TDParams tdPar, int maxGameNum) {
//		m_Net.setLambda(tdPar.getLambda());
//		m_Net.setGamma(tdPar.getGamma());
//		m_Net.setSigmoid(tdPar.hasSigmoid());
//		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		//m_Net.setRpropInitDelta( tdPar.getAlpha() / inpSize[m_feature.getFeatmode()] );

		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

		NORMALIZE=tdPar.getNormalize();
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
	}

	public void setTDParams(ParTD tdPar, int maxGameNum) {
//		m_Net.setLambda(tdPar.getLambda());
//		m_Net.setGamma(tdPar.getGamma());
//		m_Net.setSigmoid(tdPar.hasSigmoid());
//		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		//m_Net.setRpropInitDelta( tdPar.getAlpha() / inpSize[m_feature.getFeatmode()] );

		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

		NORMALIZE=tdPar.getNormalize();
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
	}

	public void setNTParams(NTParams ntPar) {
		tcIn=ntPar.getTcInterval();
		TC=ntPar.getTc();
//		USESYMMETRY=ntPar.getUseSymmetry();
//		m_Net.setUseSymmetry(ntPar.getUseSymmetry());   // WK: needed when loading agent
		tcImm=ntPar.getTcImm();		
		randomness=ntPar.getRandomness();
		randWalk=ntPar.getRandomWalk();
	}

	public void setNTParams(ParNT ntPar) {
		tcIn=ntPar.getTcInterval();
		TC=ntPar.getTc();
//		USESYMMETRY=ntPar.getUSESYMMETRY();
//		m_Net.setUseSymmetry(ntPar.getUSESYMMETRY());   // WK: needed when loading agent
		tcImm=ntPar.getTcImm();		
		randomness=ntPar.getRandomness();
		randWalk=ntPar.getRandomWalk();
		m_Net.setTdAgt(this);						 // WK: needed when loading an older agent
	}

	/**
	 * Set defaults for m_oPar 
	 * (needed in {@link XArenaMenu#loadAgent} when loading older agents, where 
	 * m_oPar=null in the saved version).
	 */
	public void setDefaultOtherPar() {
		m_oPar = new ParOther();
	}

	public void setAlpha(double alpha) {
		m_Net.setAlpha(alpha);
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
	
	public long getNumLrnActions() {
		return m_Net.getNumLearnActions();
	}

	public void resetNumLearnActions() {
		m_Net.resetNumLearnActions();
	}
	
	public int getNumEval()
	{	
		return m_oPar.getNumEval();
	}
	
	public NTuple2ValueFunc getNTupleValueFunc() {
		return m_Net;
	}
	
	public String stringDescr() {
		m_Net.setHorizon();
		String cs = getClass().getName();
		String str = cs + ": USESYMMETRY:" + (m_ntPar.getUSESYMMETRY()?"true":"false")
						+ ", NORMALIZE:" + (NORMALIZE?"true":"false")
						+ ", sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
						+ ", lambda:" + m_Net.getLambda()
						+ ", horizon:" + m_Net.getHorizon()
						+ ", AFTERSTATE:" + (m_ntPar.getAFTERSTATE()?"true":"false")
						+ ", learnFromRM: " + (m_oPar.useLearnFromRM()?"true":"false");
		return str;
	}
		
	public String stringDescr2() {
		String cs = getClass().getName();
		String str = cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
						+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal();
		return str;
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
		return str;
	}

	public ParTD getTDParams() {
		return m_tdPar;
	}
	public ParNT getNTParams() {
		return m_ntPar;
	}
	public ParOther getOtherPar() {
		return m_oPar;
	}
	public boolean getAFTERSTATE() {
		return m_ntPar.getAFTERSTATE();
	}
	public boolean getLearnFromRM() {
		return m_oPar.useLearnFromRM();
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
}