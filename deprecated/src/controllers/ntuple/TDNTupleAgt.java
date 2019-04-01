package controllers.TD.ntuple;

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
import controllers.TD.ntuple2.TDNTuple2Agt;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.StateObservationNondeterministic;
import games.XNTupleFuncs;
import games.ZweiTausendAchtundVierzig.StateObserver2048;

/**
 * The TD-Learning {@link PlayAgent} (Temporal Difference reinforcement learning)
 * <b>with n-tuples</b>. 
 * It has a one-layer (perceptron-like) neural network with or without output-nonlinearity  
 * {@code tanh} to model the value function. 
 * The net follows closely the (pseudo-)code by [SuttonBonde93]. 
 * <p>
 * Some functionality is packed in the superclass 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...) <p>
 * 
 * TDNTupleAgt should not be used anymore, use instead {@link TDNTuple2Agt}
 * 
 * @see PlayAgent
 * @see AgentBase
 * @see TDNTuple2Agt
 * 
 * @author Wolfgang Konen, Samineh Bagheri, Markus Thill, TH Köln, Feb'17
 */
//
// This agent is adapted from project SourceTTT, class TicTacToe.TDSNPlayer.
// 
//
@Deprecated
public class TDNTupleAgt extends AgentBase implements PlayAgent,Serializable {
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
	 * m_epsilon = 0.1 (def.): 10% of the moves are random, and so forth. <br>
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
	// Here: NTupleValueFunc
	private NTupleValueFunc m_Net;

	// Use symmetries (rotation, mirror) in NTuple-System
//	protected boolean USESYMMETRY = true; 	// use now m_ntPar.getUseSymmetry() - don't store/maintain value twice
//	private boolean learnFromRM = false;    // use now m_oPar.useLearnFromRM() - don't store/maintain value twice
	private boolean NORMALIZE = false; 
	private boolean RANDINITWEIGHTS = false;// Init Weights of Value-Function
											// randomly
	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
	public static boolean NEWTARGET=false;
	// if NEW_GNA==true: use the new function getNextAction2,3 in getNextAction;
	// if NEW_GNA==false: use the old function getNextAction1 in getNextAction;
	private static boolean NEW_GNA=true;	
	
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
	private boolean m_randomMove = false;	// whether the last action was random
	private boolean m_DEBG = false;


	/**
	 * Default constructor for {@link TDNTupleAgt}, needed for loading a serialized version
	 */
	public TDNTupleAgt() throws IOException {
		super();
		TDParams tdPar = new TDParams();
		NTParams ntPar = new NTParams();
		OtherParams oPar = new OtherParams();
		initNet(ntPar, tdPar, oPar, null, null, 1000);
	}

//	/**
//	 * Create a new {@link TDNTupleAgt}
//	 * 
//	 * @param tdPar
//	 *            All needed Parameters
//	 * @param ntPar 
//	 * @param maxGameNum
//	 *            Number of Training-Games
//	 * @throws IOException 
//	 */
//	public TDNTupleAgt(String name, TDParams tdPar, NTParams ntPar, int maxGameNum) throws IOException {
//		super(name);
//		initNet(ntPar,tdPar, null, maxGameNum);			
//	}

	/**
	 * Create a new {@link TDNTupleAgt}
	 * 
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	public TDNTupleAgt(String name, TDParams tdPar, NTParams ntPar, OtherParams oPar, 
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		super(name);
		initNet(ntPar,tdPar, oPar, nTuples, xnf, maxGameNum);			
	}

	/**
	 * This constructor is only needed during the one-time transformation of a 
	 * v12-agent-file to a v13-agent file. <p>
	 * 
	 * Create a new {@link TDNTupleAgt} just by copying all members from 
	 * {@link TDNTupleAgt_v12} which is assumed to be in the old v12 version. (We need 
	 * just a class with a new name for proper serialization.) 
	 * 
	 * @param tdagt			agent of the old v12 version 
	 */
	public TDNTupleAgt(TDNTupleAgt_v12 tdagt) {
		super(tdagt.getName());
		m_tdPar = new ParTD(tdagt.getTDParams());
		m_ntPar = new ParNT(tdagt.getNTParams());
		m_oPar = new ParOther();
		rand = new Random(42); //(System.currentTimeMillis());		

		m_Net = tdagt.getNTupleValueFunc();
		
		setTDParams(tdagt.getTDParams(), tdagt.getMaxGameNum());
		
		setNTParams(m_ntPar);

		this.setAgentState(tdagt.getAgentState());		
		this.setMaxGameNum(tdagt.getMaxGameNum());
		this.setEpochMax(tdagt.getEpochMax());
		this.setNumEval(tdagt.getNumEval());
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
		
		m_Net = new NTupleValueFunc(this, nTuples, xnf, posVals, 
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
//	@Override
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
//		int player = Types.PLAYER_PM[so.getPlayer()]; 	 
//		//int[][] Table = so.getTable();
//		
//		// --- this code is not understandable and wrong ---		
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
//        double soReward = 0; //so.getGameScore(so);
//        //VTable = new double[acts.size()];  
//        // DON'T! The caller has to define VTable with the right length
//        
//        for(i = 0; i < actions.length; ++i)
//        {
//            actions[i] = acts.get(i);
//            NewSO = so.copy();
//            NewSO.advance(actions[i]);
//			
//			if (!NEWTARGET) {
//				// old target logic:
//				if (NewSO.isGameOver()) {
////					switch (so.getNumPlayers()) {
////					case 1: 
////						CurrentScore = NewSO.getGameScore();
////						break;
////					case 2: 
////						CurrentScore = (-1)*NewSO.getGameScore();		// CORRECT
////						// NewSO.getGameScore() returns -1, if 'player', that is the
////						// one who *made* the move to 'so', has won. If we multiply
////						// this by (-1), we get a reward +1 for a X(player=+1)- 
////						// win and *also* a reward +1 for an O(player=-1)-win.
////						// And a reward 0 for a tie.
////						//
////						//CurrentScore = (-player)*NewSO.getGameScore(); // WRONG!!
////						// NewSO.getGameScore() returns -1, if 'player', that is the
////						// one who *made* the move to 'so', has won. If we multiply
////						// this by (-player), we get a reward +1 for a X(player=+1)- 
////						// win and a reward -1 for an O(player=-1)-win.
////						// And a reward 0 for a tie.
////						break;
////					default: 
////						throw new RuntimeException("TDNTupleAgt.trainAgent does not yet "+
////								"implement case so.getNumPlayers()>2");
////					}
//					
//					// the whole switch-statement above can be replaced with the simpler  
//					// logic of NewSO.getGameScore(StateObservation referingState), where  
//					// referingState is 'so', the state before NewSO. [This should be  
//					// extensible to 3- or 4-player games (!) as well, if we put the 
//					// proper logic into method getGameScore(referingState).]  
//					CurrentScore = NewSO.getGameScore(so);
//				}  else {
//					// old target logic:
//					// the score is just the agent's value function for NewSO. In this case
//					// the agent has to learn in the value function the sum 
//					// 		"rewards received plus future rewards"
//					// itself. 
//					CurrentScore = player * getScore(NewSO);
//											// here we ask this agent for its score estimate on NewSO
//					
//				}
//			} else {
//				// new target logic:
////				if (NewSO.isGameOver()) {
////					CurrentScore = NewSO.getGameScore(so) - soReward;					
////				}  else {
//					// new target logic:
//					// the score is the reward received for the transition from so to NewSO 
//					// 		(NewSO.getGameScore(so)-soReward)
//					// plus the estimated future rewards until game over (getScore(NewSO), 
//					// the agent's value function for NewSO)
//					CurrentScore = ((NewSO.getGameScore(so) - soReward) + player * getScore(NewSO));				
////				}
//			}
//			
//			// just a debug check:
//			if (Double.isInfinite(getScore(NewSO))) {
//				double s = getScore(NewSO);
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
//		if (TDNTuple2Agt.DBG2_TARGET) {
//            NewSO = so.copy();
//            NewSO.advance(actBest);
//            double deltaReward = NewSO.getGameScore(so) - so.getGameScore(NewSO);
//			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
//			double sc = (deltaReward + player * getScore(NewSO))*MAXSCORE;
//			System.out.println("getScore(NewSO): "+sc+"   ["+so.stringDescr()+"]");
//			int dummy=1;
//		}
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
		StateObservation NewSO;
		int count = 1; // counts the moves with same BestScore
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        int iBest;
		BestScore = -Double.MAX_VALUE;
		double[] VTable;
       
		if (so.getNumPlayers()>2)
			throw new RuntimeException("TDNTupleAgt.getNextAction2 does not yet "+
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
		
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, BestScore);
		return actBestVT;
	}

    // calculate CurrentScore: 
	// (g3_Evaluate is helper function for getNextAction3)
    private double g3_Evaluate(	StateObservation so, Types.ACTIONS act, 
    							StateObservation refer, boolean silent) {
    	double CurrentScore,agentScore;
		int player = Types.PLAYER_PM[refer.getPlayer()]; 	 
        double referReward = refer.getGameScore(refer); // 0; 
    	StateObservation NewSO;
    	Types.ACTIONS_VT actBestVT;

		if (randomSelect) {
			CurrentScore = rand.nextDouble();
			return CurrentScore;
		} 
        
    	// the normal part for the case of single moves:
        NewSO = so.copy();
        NewSO.advance(act);
        agentScore = getScore(NewSO);
        //
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
		// 		(NewSO.getGameScore(refer)-referReward)
		// plus the estimated future rewards until game over (getScore(NewSO), 
		// the agent's value function for NewSO)
		CurrentScore = (NewSO.getGameScore(refer) - referReward) + getGamma()*player*agentScore;				

		if (!silent) {
			System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*player-1));
			//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
		}

		return CurrentScore;
    }

	// DEBUG only: return always the 1st available action (for deterministic training games)
	public Types.ACTIONS getFirstAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
        Types.ACTIONS actBest = null;
		StateObservation NewSO;
		
		// get the first action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        actBest = acts.get(0);
        
		if (TDNTuple2Agt.DBG2_TARGET) {
            NewSO = so.copy();
            NewSO.advance(actBest);
            double deltaReward = NewSO.getGameScore(so) - so.getGameScore(NewSO);
			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
			double sc = (deltaReward + getScore(NewSO))*MAXSCORE;
			System.out.println("getScore(NewSO): "+sc+"   ["+so.stringDescr()+"]");
			int dummy=1;
		}
		return actBest;
	}

//	/**
//	 * 
//	 * @return	returns true/false, whether the action suggested by last call 
//	 * 			to getNextAction() was a random action 
//	 */
//	@Deprecated
//	public boolean wasRandomAction() {
//		return randomSelect;
//	}

	/**
	 * Return the agent's estimate of the score for that after state.
	 * For 2-player games like TTT the score is V(), the prob. that X (Player +1) wins 
	 * from that after state. Player*V() is the quantity to be maximized by getNextAction.
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
	 * Train the Agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (see function
	 * 					soSelectStartState in {@link games.XArenaFuncs} to get
	 * 					some exploration of different game paths)
// --- epiLength, learnFromRM are now available via the agent's member ParOther m_oPar ---
//	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
//	 * 					prematurely.  
//	 * @param learnFromRM if true, learn from random moves during training
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so /*, int epiLength, boolean learnFromRM*/) {
//		double[] VTable = null;
		double reward = 0.0, oldReward = 0.0;
		boolean wghtChange = false;
		boolean upTC=false;
		double Input[], oldInput[];
		String S_old = null;   // only as debug info
		int player;
		Types.ACTIONS_VT actBest;
		StateObservation oldSO;
		int[] curBoard = m_Net.xnf.getBoardVector(so);
		int   curPlayer=so.getPlayer();
		int[] nextBoard = null;
		int   nextPlayer;

		boolean learnFromRM = m_oPar.useLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();

		//System.out.println("Random test: "+ rand.nextDouble());
		//System.out.println("Random test: "+ rand.nextDouble());
		
		player = Types.PLAYER_PM[so.getPlayer()];

		if (m_Net.getLambda()!=0.0) {
			m_Net.resetElig(); // reset the eligibility traces before starting a new game
			m_Net.calcScoresAndElig(curBoard,curPlayer);
		}

		//oldInput = m_feature.prepareFeatVector(so);
		//S_old = so.stringDescr();   
		//S_old = tableToString(-Player, table);
		m_counter=0;		// count the number of moves
		m_finished=false;
		while (true) {
//			VTable = new double[so.getNumAvailableActions()+1];
//			actBest = this.getNextAction(so, true, VTable, true);
			actBest = this.getNextAction2(so, true, true);
			//actBest = this.getNextAction(so, false, VTable, true);  // Debug only
			m_randomMove = actBest.isRandomAction();
			oldSO = so.copy();
			so.advance(actBest);
			nextBoard = m_Net.xnf.getBoardVector(so);
			nextPlayer= so.getPlayer();
//			if (m_DEBG) printVTable(pstream,actBest.getVTable());
			if (m_DEBG) printTable(pstream,nextBoard);
			if (NEWTARGET) {
				reward=trainNewTargetLogic(so,oldSO,curBoard,curPlayer,nextBoard,nextPlayer,
						learnFromRM,epiLength,player,upTC,oldReward);				
			} else {
				reward=trainOldTargetLogic(so,oldSO,curBoard,curPlayer,nextBoard,nextPlayer,
						learnFromRM,epiLength,player,upTC);
			}
			if (TDNTuple2Agt.DBG2_TARGET) {
				final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
				System.out.print("R_t+1, r_t+1: "+reward*MAXSCORE
						+", "+(int)((reward-oldReward)*MAXSCORE+0.5) );
				System.out.println("|   est. final score: "+(reward+this.getScore(so))*MAXSCORE);
				if (reward==-1 && oldReward==-1) {
					int dummy=1;
				}
			}

			curBoard = nextBoard; 
			curPlayer= nextPlayer;
			oldReward= reward;
			
			if (m_finished) {
				if (m_DEBG)
					if (m_randomMove) {
						pstream.println("Terminated game "+(getGameNum()) + " by random move. Reward = "+reward);						
					} else {
						pstream.println("Terminated game "+(getGameNum()) + ". Reward = "+reward);
					}
				
				break;
			}

			player = Types.PLAYER_PM[so.getPlayer()];   // advance to the next player
		} // while

		if (NEWTARGET) {
			// learn for each final state that the value function (estimated further reward)
			// should be zero:
			m_Net.updateWeightsNewTerminal(curBoard, curPlayer,upTC);
		}
		
		try {
			this.finishUpdateWeights();		// adjust learn params ALPHA & m_epsilon
		} catch (Throwable e) {
			// TODO Auto-generated catch block
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
	 * 
	 * @return reward
	 */
	private double trainNewTargetLogic(
			StateObservation so, StateObservation oldSO, 
			int[] curBoard, int curPlayer,
			int[] nextBoard, int nextPlayer, boolean learnFromRM,
			int epiLength, int player, boolean upTC, double oldReward) 
	{
		double reward;
		
		// Fetch the reward for StateObservation so (relative to oldSO):
		reward = player*so.getGameScore(oldSO);

		reward = normalize2(reward,so);

		if (so.isGameOver()) {
			m_finished = true;
		}

		m_counter++;
		if (m_counter==epiLength) {
			reward=estimateGameValue(so);
			//epiCount++;
			m_finished = true; 
		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move,
			// but update eligibility traces for next pass
			m_Net.calcScoresAndElig(nextBoard,nextPlayer); 
			// only for diagnostics
			if (m_DEBG)
				pstream.println("random move");

		} else {
			// do one training step (NEW target)
			m_Net.updateWeightsNew(curBoard, curPlayer, nextBoard, nextPlayer,
					reward-oldReward,upTC);
			// contains an updateElig(nextBoard,...) in the end, if LAMBDA>0
		}
		
		return reward;
		
	} 
	
	private double trainOldTargetLogic(
			StateObservation so, StateObservation oldSO, 
			int[] curBoard, int curPlayer,
			int[] nextBoard, int nextPlayer, boolean learnFromRM, 
			int epiLength, int player, boolean upTC) 
	{
		double reward;
		
		if (so.isGameOver()) {
			// Fetch the reward from StateObservation:
//			switch (so.getNumPlayers()) {
//			case 1: 
//				reward = so.getGameScore();
//				break;
//			case 2: 
//				reward = (-player)*so.getGameScore();
//				// so.getGameScore() returns -1, if 'player', that is the
//				// one who *made* the move to 'so', has won. If we multiply
//				// this by (-player), we get a reward +1 for a X(player=+1)- 
//				// win and a reward -1 for an O(player=-1)-win.
//				// And a reward 0 for a tie.
//				break;
//			default: 
//				throw new RuntimeException("TDNTupleAgt.trainAgent not yet "+
//						"implementing case so.getNumPlayers()>2");
//			}
			
			// the whole switch-statement above can be replaced with the simpler  
			// logic of so.getGameScore(StateObservation referingState), where  
			// referingState is 'oldSO', the state before so. [This should be  
			// extensible to 3- or 4-player games (!) as well, if we put the 
			// proper logic into method getGameScore(referingState).]  
			reward = player*so.getGameScore(oldSO);

			reward = normalize2(reward,so);
			
			m_finished = true;
		} else {
			//it is irrelevant what we put into reward here, because it will 
			//not be used in m_Net.updateWeights when m_finished is not true.
			//
			// ??? has to be re-thought for the case of 2048 and other 1-player games!!!
			reward = 0.0;
		}
		m_counter++;
		if (m_counter==epiLength) {
			reward=estimateGameValue(so);
			//epiCount++;
			m_finished = true; 
		}
		//Input = m_feature.prepareFeatVector(so);
		if (m_randomMove && !m_finished && !learnFromRM) {
			// no training, go to next move,
			// but update eligibility traces for next pass
			m_Net.calcScoresAndElig(nextBoard,nextPlayer); 
			// only for diagnostics
			if (m_DEBG)
				pstream.println("random move");

		} else {
			// do one training step
			
			m_Net.updateWeights(curBoard, curPlayer, nextBoard, nextPlayer,
					m_finished, reward,upTC);
			// contains an updateElig(nextBoard,...) in the end, if LAMBDA>0

//-- accumulation logic not yet implemented for TDNTupleAgt --
//
//			// this is the accumulation logic: if eMax>0, then form 
//			// mini batches and apply the weight changes only at the end
//			// of such mini batches
//			int eMax = super.getEpochMax();
//			if (eMax==0) {
//				wghtChange=true;
//			} else {
//				if (m_finished) numFinishedGames++;
//				wghtChange = (m_finished && (numFinishedGames % eMax) == 0);
//			}
//			
//			// either no random move or game is finished >> target signal is
//			// meaningful!
//			m_Net.updateWeights(reward, Input, m_finished, wghtChange);
//			// contains afterwards a m_Net.calcScoresAndElig(Input);
//
//			oldInput = Input;
		}
		
		return reward;
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
//		USESYMMETRY=ntPar.getUseSymmetry();
//		m_Net.setUseSymmetry(ntPar.getUseSymmetry());   // WK: needed when loading agent
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
	
	public NTupleValueFunc getNTupleValueFunc() {
		return m_Net;
	}
	
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ": USESYMMETRY:" + (this.m_ntPar.getUSESYMMETRY()?"true":"false")
						+ ", NORMALIZE:" + (NORMALIZE?"true":"false")
						+ ", " + "sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
						+ ", lambda:" + m_Net.getLambda()
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

	// Debug only: 
	//
	private void printTable(PrintStream pstream, int[] board) {
		String s = NTuple.stringRep(board);
		pstream.println(s + " : BestScore= "+BestScore);
	}

	private void printVTable(PrintStream pstream, double[] VTable) {
		for (int i=0; i<VTable.length; i++) {
			pstream.print(VTable[i]+", ");
		}
		System.out.println("");
	}
}