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
import params.ParNT;
import params.ParTD;
import params.TDParams;
import tools.Types;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
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
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...)
 * <p>
 * The differences of {@link TDNTuple2Agt} to {@link TDNTupleAgt}:
 * <ul>
 * <li> no eligibility traces, instead LAMBDA-horizon mechanism of [Jaskowski16] (faster and less
 * 		memory consumptive)
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
// This agent is adapted from project SourceTTT, class TicTacToe.TDSNPlayer
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
	 * {@link #getNextAction(StateObservation, boolean, double[], boolean)}
	 * during training. <br>
	 * m_epsilon = 0.0: no random moves, <br>
	 * m_epsilon = 0.1 (def.): 10% of the moves are random, and so forth
	 */
	private double m_epsilon = 0.1;
	
	/**
	 * m_EpsilonChangeDelta is not used anymore. We use a sigmoidal change from 
	 * {@code tdPar.getEpsilonFinal()} to {@code tdPar.getEpsilonFinal()}, 
	 * see {@link #finishUpdateWeights()}.
	 */
	@Deprecated
	private double m_EpsilonChangeDelta = 0.001;
	private double MaxScore;
	//samine//
	private boolean TC; //true: using Temporal Coherence algorithm
	private int tcIn; 	//temporal coherence interval: after tcIn games tcFactor will be updates
	private boolean tcImm=true;		//true: immediate TC update, false: batch update (epochs)
	private boolean randomness=false; //true: ntuples are created randomly (walk or points)
	private boolean randWalk=true; 	//true: random walk is used to generate nTuples
									//false: random points is used to generate nTuples//samine//

	// Value function of the agent.
	// Here: NTupleValueFunc
	private NTuple2ValueFunc m_Net;

	protected boolean USESYMMETRY = true; 	// Use symmetries (rotation, mirror) in NTuple-System
	private boolean NORMALIZE = false; 
	private boolean RANDINITWEIGHTS = false;// Init Weights of Value-Function
											// randomly
	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
	public static boolean NEWTARGET=true;
	public static boolean WITH_NS=true;
	
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
	 * Members {@link #m_tdPar} {@link #m_ntPar} are only needed for saving and loading
	 * the agent (to restore the agent with all its parameter settings)
	 */
//	private TDParams m_tdPar;
//	private NTParams m_ntPar;
	private ParTD m_tdPar;
	private ParNT m_ntPar;
	
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
		initNet(ntPar, tdPar, null, null, 1000);
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
	public TDNTuple2Agt(String name, TDParams tdPar, NTParams ntPar, int[][] nTuples, 
			XNTupleFuncs xnf, int maxGameNum) throws IOException {
		super(name);
		initNet(ntPar,tdPar, nTuples, xnf, maxGameNum);			
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
	private void initNet(NTParams ntPar, TDParams tdPar, int[][] nTuples, 
			XNTupleFuncs xnf, int maxGameNum) throws IOException {
		m_tdPar = new ParTD(tdPar);
		m_ntPar = new ParNT(ntPar);
		rand = new Random(42); //(System.currentTimeMillis());		
		
		int posVals = xnf.getNumPositionValues();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells);

		setNTParams(ntPar);
		
		setTDParams(tdPar, maxGameNum);
		
		setAgentState(AgentState.INIT);
	}

	/**
	 * Get the next best action and return it
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param VTable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
	 * @param silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random 
	 * 						
	 * actBest has member isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).
	 */
	public Types.ACTIONS getNextAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
		int i, j;
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		StateObservation NewSO;
		int count = 1; // counts the moves with same MaxScore
        Types.ACTIONS actBest = null;
        int iBest;
		MaxScore = -Double.MAX_VALUE;
       
		if (so.getNumPlayers()>2)
			throw new RuntimeException("TDNTuple2Agt.getNextAction does not yet "+
									   "implement case so.getNumPlayers()>2");

		int player = Types.PLAYER_PM[so.getPlayer()]; 	 
	
// --- this code is not understandable and wrong ---		
//		randomSelect = false;
//		double progress = (double) getGameNum() / (double) getMaxGameNum();
//		progress = (1 + m_epsilon) * progress - m_epsilon; 	// = progress +
//															// m_EPS*(progress - 1)
//		if (random) {
//			double rd = rand.nextDouble();
//			//System.out.println("rd="+rd);
//			if (rd > progress) {
//				randomSelect = true;
//			}
//		}
		
        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
		
		// get the best (or eps-greedy random) action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        double soReward = so.getGameScore(so); // 0; 
        double agentScore;
        NextState ns;
        //VTable = new double[acts.size()];  
        // DON'T! The caller has to define VTable with the right length
        
        assert actions.length>0 : "Oops, no available action";
        for(i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);		
	        
            NewSO = so.copy();
            if (getAFTERSTATE()) {
            	assert (NewSO instanceof StateObservationNondeterministic);
                ((StateObservationNondeterministic) NewSO).advanceDeterministic(actions[i]); 
                agentScore = getScore(NewSO);
                ((StateObservationNondeterministic) NewSO).advanceNondeterministic(); 
            } else {
                NewSO.advance(actions[i]);
                agentScore = getScore(NewSO);
            }
            
            // this alternative implementation has less code, but is much (40%-70%!!) slower 
//          double agentScore2;
//    		StateObservation NewSO2;
//	        ns = new NextState(so,actions[i]);
//	        agentScore2 = getScore(ns.getAfterState());
//	        NewSO2 = ns.getNextSO();
//	        assert agentScore==agentScore2 : "Oops, agentScore and agentScore2 differ!";

			if (!NEWTARGET) {
				throw new RuntimeException("NEWTARGET==false no longer supported in TDNTuple2Agt!");
//				// old target logic:
//				if (NewSO.isGameOver()) {
//					CurrentScore = NewSO.getGameScore(so);
//				}  else {
//					// old target logic:
//					// the score is just the agent's value function for NewSO. In this case
//					// the agent has to learn in the value function the sum 
//					// 		"rewards received plus future rewards"
//					// itself. 
//					CurrentScore = player * agentScore;
//											// here we ask this agent for its score estimate on NewSO
//					
//				}
			} else {
				// new target logic:
				// the score is the reward received for the transition from so to NewSO 
				// 		(NewSO.getGameScore(so)-soReward)
				// plus the estimated future rewards until game over (getScore(NewSO), 
				// the agent's value function for NewSO)
				CurrentScore = (NewSO.getGameScore(so) - soReward) + player * agentScore;				
			}
			
			// just a debug check:
			if (Double.isInfinite(agentScore)) {
				System.out.println("getScore(NewSO) is infinite!");
			}
			
			if (NORMALIZE) {
				// Normalize to [-1,+1] (the appropriate range for tanh-sigmoid):
				//
				// (this will have no effect for TicTacToe or similar games where the 
				// min./max. game score are -1/+1 anyway)
				CurrentScore = normalize(CurrentScore,so.getMinGameScore(),
								   		 so.getMaxGameScore(),-1.0,1.0);					
			}
			
			if (!silent)
				System.out.println(NewSO.toString()+", "+(2*CurrentScore*player-1));
				//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
			if (randomSelect) {
				double rd2 = rand.nextDouble();
				//System.out.println("rd CS="+rd2);
				CurrentScore = rd2;
			}
			VTable[i] = CurrentScore;
			if (MaxScore < CurrentScore) {
				MaxScore = CurrentScore;
				actBest = actions[i];
				iBest  = i; 
				count = 1;
			} else if (MaxScore == CurrentScore) {
				// If there are 'count' possibilities with the same score MaxScore, 
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
			System.out.println(NewSO.stringDescr()+", "+(2*MaxScore*player-1));
		}			
		if (DBG2_TARGET) {
			final double MAXSCORE = ((so instanceof StateObserver2048) ? 3932156 : 1);
//          NewSO = so.copy();
//          NewSO.advance(actBest);
//          double deltaReward = NewSO.getGameScore(so) - so.getGameScore(so);
//			double sc = (deltaReward + player * getScore(NewSO))*MAXSCORE;
												// this is problematic when AFTERSTATE==true (!)
			// here we use the NextState version, because computation time does not matter
			// inside DBG2_TARGET and this version is correct for both values of getAFTERSTATE():
	        ns = new NextState(so,actBest);
			double deltaReward = ns.getNextSO().getGameScore(so) - so.getGameScore(so);
			double sc = (deltaReward + player * getScore(ns.getAfterState()))*MAXSCORE;
			System.out.println("getScore((so,actbest)-afterstate): "+sc+"   ["+so.stringDescr()+"]");
			int dummy=1;
		}
		return actBest;
	}

	// DEBUG only: return always the 1st available action (for deterministic training games)
	public Types.ACTIONS getFirstAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
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
            double deltaReward = NewSO.getGameScore(so) - so.getGameScore(so);
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
	public boolean wasRandomAction() {
		return randomSelect;
	}

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
	 * Train the agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState01()} to get
	 * 					some exploration of different game paths)
	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
	 * 					prematurely.  
	 * @param learnFromRM if true, learn from random moves during training
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so, int epiLength, boolean learnFromRM) {
		double[] VTable = null;
		double reward = 0.0, oldReward = 0.0;
		boolean wghtChange = false;
		boolean upTC=false;
		double Input[], oldInput[];
		String S_old = null;   // only as debug info
		int player;
		Types.ACTIONS actBest;
		StateObservation oldSO;
		int[] curBoard = m_Net.xnf.getBoardVector(so);
		int   curPlayer=so.getPlayer();
		int[] nextBoard = null;
		int   nextPlayer;
		NextState ns = null;
		
		// only debug / assertion:
		int[] curBoard2 = null;
		int[] nextBoard2 = null;
		int[] ns_nextBoard = null;
		int[] so_nextBoard = null;


		//System.out.println("Random test: "+ rand.nextDouble());
		//System.out.println("Random test: "+ rand.nextDouble());
		
		player = Types.PLAYER_PM[so.getPlayer()];

        if (getAFTERSTATE()) {
        	curBoard = null;
        }

		m_Net.clearEquivList();
		m_Net.setHorizon();
		
		m_counter=0;		// count the number of moves
		m_finished=false;
		while (true) {
			VTable = new double[so.getNumAvailableActions()+1];
	        if (TDNTuple2Agt.DBG2_FIXEDSEQUENCE) {
	        	actBest = this.getFirstAction(so, true, VTable, true);	// DEBUG only
	        } else {
				actBest = this.getNextAction(so, true, VTable, true);	// the normal case        	
	        }

	        m_randomMove = actBest.isRandomAction();
	               
	        // this part will become obsolete if we switch to the NextState-logic:
	        // (we keep it only for a while as cross-check)
			oldSO = so.copy();
	        if (getAFTERSTATE()) {
            	assert (so instanceof StateObservationNondeterministic);
                ((StateObservationNondeterministic) so).advanceDeterministic(actBest); 
				nextBoard = m_Net.xnf.getBoardVector(so);
                ((StateObservationNondeterministic) so).advanceNondeterministic(); 
	        	
	        } else {
				so.advance(actBest);
				nextBoard = m_Net.xnf.getBoardVector(so);
	        }
	        if (getAFTERSTATE()) {
	        	/* a debug assertion which we can only do in the (AFTERSTATE==true)-case, otherwise the random tile 
	        	 * differs */
	        	if (ns!=null && curBoard!=null) {
		        	// the after state before the current state as board vector
			        curBoard2 = m_Net.xnf.getBoardVector(ns.getAfterState()); 
			        for (int i=0; i<nextBoard.length; i++)
			        	assert curBoard[i]==curBoard2[i] : "Ooops, curBoard2 differs from curBoard";	        	
	        	}
	        }
	        
	        ns = new NextState(oldSO,actBest);
	        
	        // this part will become obsolete if we switch to the NextState-logic:
	        // (we keep it only for a while as cross-check)
	        if (getAFTERSTATE()) {
	        	/* a debug assertion which we can only do in the (AFTERSTATE==true)-case, otherwise the random tile 
	        	 * differs */
		        nextBoard2 = m_Net.xnf.getBoardVector(ns.getAfterState()); // the afterstate as board vector
		        for (int i=0; i<nextBoard.length; i++)
		        	assert nextBoard[i]==nextBoard2[i] : "Ooops, nextBoard2 differs from nextBoard";
		        /* Note that we cannot perform a check that so and ns.getNextSO are the same states, since they both 
		         * contain a nondeterministic advance which usually differs in the random tile. */
	        }	        
	        
			nextPlayer= so.getPlayer();
			//if (DEBG) printVTable(pstream,VTable);
			if (m_DEBG) printTable(pstream,nextBoard);
			if (NEWTARGET) {
				if (WITH_NS) {
					reward=trainNewTargetLogic2(ns,curBoard,learnFromRM,epiLength,oldReward);
				} else {
					reward=trainNewTargetLogic(so,oldSO,curBoard,curPlayer,nextBoard,nextPlayer,
							learnFromRM,epiLength,player,upTC,oldReward);	
				}
			} else {
				throw new RuntimeException("NEWTARGET==false no longer supported in TDNTuple2Agt!");
//				reward=trainOldTargetLogic(so,oldSO,curBoard,curPlayer,nextBoard,nextPlayer,
//						learnFromRM,epiLength,player,upTC);
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
			if (WITH_NS) {
				so = ns.getNextSO();	// Only together with trainNewTargetLogic2			
				curBoard = m_Net.xnf.getBoardVector(ns.getAfterState());  
			} else {
				//curBoard = nextBoard; 	// BUG!! A later change to nextBoard will change curBoard as well!!
											// But probably w/o effect, since the next use of nextBoard is
											//  	nextBoard = ...getBoardVector(), 
											// and this sets nextBoard to new int[] ... 
				curBoard = nextBoard.clone();
			}
			curPlayer= nextPlayer;
			oldReward= reward;
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

		if (NEWTARGET) {
			// learn for each final state that the value function (estimated further reward)
			// should be zero:
			m_Net.updateWeightsNewTerminal(curBoard, curPlayer);
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
			// only for diagnostics
			if (m_DEBG)
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
		
		reward = fetchReward(so,oldSO,player);
		
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
			// no training, go to next move.
			// only for diagnostics
			if (m_DEBG)
				pstream.println("random move");
		} else {
			// do one training step (NEW target)
			if (curBoard!=null) {
				m_Net.updateWeightsNew(curBoard, curPlayer, nextBoard, nextPlayer,
						reward-oldReward,upTC);
			}
		}
		
		return reward;
		
	} 
	
	/**
	 * Fetch the reward for StateObservation so (relative to oldSO)
	 * 
	 * @param so		actual state
	 * @param oldSO		referring state
	 * @param player	the player to move in state oldSO. 
	 * 					1-player games: always +1; 2-player games: +1 or -1
	 * @return reward (total gameScore) for actual state {@code so} 
	 */
	private double fetchReward(StateObservation so, StateObservation oldSO, int player) {
		double reward = player*so.getGameScore(oldSO);

		if (NORMALIZE) {
//			// Normalize to [0,+1] (the appropriate range for Fermi-fct-sigmoid)
//			// or to [-1,+1] (the appropriate range for tanh-sigmoid):
//			double lower = (m_Net.FERMI_FCT ? 0.0 : -1.0);
//			double upper = (m_Net.FERMI_FCT ? 1.0 :  1.0);
			
			// since we have - in contrast to TDAgent - here only one sigmoid
			// choice, namely tanh, we can take fixed [min,max] = [-1,+1]. 
			// If we would later extend to several sigmoids, we would have to 
			// adapt here:
			
			reward = normalize(reward,so.getMinGameScore(),
							   so.getMaxGameScore(),-1,+1);
		}
		return reward;
	}

//	private double trainOldTargetLogic(
//			StateObservation so, StateObservation oldSO, 
//			int[] curBoard, int curPlayer,
//			int[] nextBoard, int nextPlayer, boolean learnFromRM, 
//			int epiLength, int player, boolean upTC) 
//	{
//		double reward;
//		
//		if (so.isGameOver()) {
//			// Fetch the reward for StateObservation so (relative to oldSO):
//			reward = player*so.getGameScore(oldSO);
//
//			if (NORMALIZE) {
////				// Normalize to [0,+1] (the appropriate range for Fermi-fct-sigmoid)
////				// or to [-1,+1] (the appropriate range for tanh-sigmoid):
////				double lower = (m_Net.FERMI_FCT ? 0.0 : -1.0);
////				double upper = (m_Net.FERMI_FCT ? 1.0 :  1.0);
//				
//				// since we have - in contrast to TDAgent - here only one sigmoid
//				// choice, namely tanh, we can take fixed [min,max] = [-1,+1]. 
//				// If we would later extend to several sigmoids, we would have to 
//				// adapt here:
//				
//				reward = normalize(reward,so.getMinGameScore(),
//								   so.getMaxGameScore(),-1,+1);
//			}
//			m_finished = true;
//		} else {
//			//it is irrelevant what we put into reward here, because it will 
//			//not be used in m_Net.updateWeights when m_finished is not true.
//			//
//			// ??? has to be re-thought for the case of 2048 and other 1-player games!!!
//			reward = 0.0;
//		}
//		m_counter++;
//		if (m_counter==epiLength) {
//			reward=estimateGameValue(so);
//			//epiCount++;
//			m_finished = true; 
//		}
//		if (m_randomMove && !m_finished && !learnFromRM) {
//			// no training, go to next move,
////			// but update eligibility traces for next pass
////			m_Net.calcScoresAndElig(nextBoard,nextPlayer); 
//			// only for diagnostics
//			if (m_DEBG)
//				pstream.println("random move");
//
//		} else {
//			// do one training step			
//			if (curBoard!=null) {
//				m_Net.updateWeights(curBoard, curPlayer, nextBoard, nextPlayer,
//						m_finished, reward,upTC);
//			}
//
////-- accumulation logic not yet implemented for TDNTupleAgt --
////
////			// this is the accumulation logic: if eMax>0, then form 
////			// mini batches and apply the weight changes only at the end
////			// of such mini batches
////			int eMax = super.getEpochMax();
////			if (eMax==0) {
////				wghtChange=true;
////			} else {
////				if (m_finished) numFinishedGames++;
////				wghtChange = (m_finished && (numFinishedGames % eMax) == 0);
////			}
////			
////			// either no random move or game is finished >> target signal is
////			// meaningful!
////			m_Net.updateWeights(reward, Input, m_finished, wghtChange);
////			// contains afterwards a m_Net.calcScoresAndElig(Input);
////
////			oldInput = Input;
//		}
//		
//		return reward;
//	}
	
	/**
	 * Class NextState bundles the different states in state advancing and two different modes of 
	 * state advancing.
	 * <p>
	 * If {@link TDNTuple2Agt#getAFTERSTATE()}==false, then {@code ns=new NextState(so,actBest)}  
	 * simply advances {@code so} and lets ns.getAfterState() and ns.getNextSO() return the same next 
	 * state so.advance(actBest). 
	 * <p>
	 * If {@link TDNTuple2Agt#getAFTERSTATE()}==true, then {@code ns=new NextState(so,actBest)}  
	 * advances so in two steps: ns.getAfterState() returns the <b>afterstate</b> (after the 
	 * deterministic advance (e.g. the merge in case of 2048)) and ns.getNextSO() returns the 
	 * next state (after adding the nondeterministic part (e.g. adding the random tile in case 
	 * of 2048)).
	 * <p> 
	 * Note that the part with {@code getAFTERSTATE()==true} is only available for nondeterministic games 
	 * where {@link StateObservation} {@code so} is actually an instance of 
	 * {@link StateObservationNondeterministic}. Otherwise, an assertion will fire.
	 * 
	 * @see TDNTuple2Agt#getNextAction(StateObservation, boolean, double[], boolean)
	 * @see TDNTuple2Agt#trainAgent(StateObservation, int, boolean)
	 */
	class NextState {
		StateObservation sobs;
		StateObservation afterState;
		StateObservation nextSO;
		double thisReward;
		double nextReward;
		
		/**
		 * Advance state <b>s</b> = {@code so} by action {@code actBest}. Store the afterstate 
		 * <b>s'</b> and the next state <b>s''</b>. 
		 */
		NextState(StateObservation so, Types.ACTIONS actBest) {
			sobs = so.copy();
	        if (getAFTERSTATE()) {
            	assert (so instanceof StateObservationNondeterministic);
            	// implement it in such a way that StateObservation so is *not* changed -> that is why 
            	// we copy *first* to afterState, then advance:
                afterState = so.copy();
                ((StateObservationNondeterministic) afterState).advanceDeterministic(actBest);
                nextSO = afterState.copy();
                ((StateObservationNondeterministic) nextSO).advanceNondeterministic(); 
	        	
	        } else {
                nextSO = so.copy();
                nextSO.advance(actBest);
				afterState = nextSO.copy();
	        }

			nextReward = fetchReward(nextSO,sobs,Types.PLAYER_PM[sobs.getPlayer()]);

	        //**TODO**
//	        oldReward = sobs.getGameScore();
		}

		/**
		 * @return the original state <b>s</b> = {@code so}
		 */
		public StateObservation getSO() {
			return sobs;
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

		// OLD method: linear change
		// m_epsilon = m_epsilon - m_EpsilonChangeDelta;

		// 
		double a0 = m_tdPar.getEpsilon();
		double a1 = m_tdPar.getEpsilonFinal();
		// double x = (double) getGameNum() / (getMaxGameNum()) * 10.0;
		double x = (double) ((getGameNum() - 1000.0) / getMaxGameNum())*5;
		double fx = (((a0 - a1) / 2.0 )* (1.0 - Math.tanh(x)) )+ a1;
		
		m_epsilon = fx;
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
	
	public NTuple2ValueFunc getNTupleValueFunc() {
		return m_Net;
	}
	
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ", USESYMMETRY:" + (this.m_ntPar.getUSESYMMETRY()?"true":"false")
						+ ", NORMALIZE:" + (NORMALIZE?"true":"false")
						+ ", " + "sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
						+ ", lambda:" + m_Net.getLambda();
		return str;
	}
		
	public String printTrainStatus() {
		DecimalFormat frm = new DecimalFormat("#0.0000");
		String cs = ""; //getClass().getName() + ": ";   // optional class name
		String str = cs + "alpha="+frm.format(m_Net.getAlpha()) 
				   + ", epsilon="+frm.format(getEpsilon())
				   //+ ", lambda:" + m_Net.getLambda()
				   + ", "+getGameNum() + " games";
		return str;
	}

	public ParTD getTDParams() {
		return m_tdPar;
	}
	public ParNT getNTParams() {
		return m_ntPar;
	}
	public boolean getAFTERSTATE() {
		return m_ntPar.getAFTERSTATE();
	}


	// Debug only: 
	//
	private void printTable(PrintStream pstream, int[] board) {
		String s = NTuple2.stringRep(board);
		pstream.println(s + " : MaxScore= "+MaxScore);
	}

	private void printVTable(PrintStream pstream, double[] VTable) {
		for (int i=0; i<VTable.length; i++) {
			pstream.print(VTable[i]+", ");
		}
		System.out.println("");
	}
}