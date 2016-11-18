package games.TicTacToe;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import params.TDParams;
import tools.Types;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import controllers.TD.TD_Lin;
import controllers.TD.TD_NNet;
import controllers.TD.TD_func;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;
import games.TicTacToe.TicTDBase;


/**
 * @deprecated
 * This class is deprecated since it mixes TicTacToe-specific functionality with
 * general TD(lambda) functionality. For better abstraction it is recommended to
 * use class {@link TDAgent} together with the (small) TTT-specific classes
 * {@link TDPlayerTTT} and {@link FeatureTTT}. <p> 
 * 
 * The TD-Learning {@link PlayAgent} for TicTacToe. It has either a linear net
 * {@link TD_Lin} or a BP neural net {@link TD_NNet} to model the value
 * function. The net follows closely the (pseudo-)code by [SuttonBonde93]. It
 * can be trained with different feature sets:
 * <ul>
 * <li>0: Levkovich's features
 * <li>1,2: thin, thick feature set (6 or 10 features)
 * <li>3: thick feature set + board position (19 features)
 * <li>4: extended = thick + extra features (13 features)
 * <li>9: raw = only board position (9 features)
 * </ul>
 * The internal learning rate ALPHA for the net input layer weights is alpha/n,
 * where n=(size of feature vector) and alpha is the constructors' first
 * parameter.
 * 
 * Some functionality is packed in the superclass {@link TicTDBase} and *its*
 * superclass {@link AgentBase} (gameNum, maxGameNum, AgentState)
 * 
 * @see PlayAgent
 * @see TicTDBase
 * @see AgentBase
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class TDPlayerTT2 extends TicTDBase implements PlayAgent,Serializable {
	protected TD_func m_Net;
	private double m_epsilon = 0.1;
	private double m_EpsilonChangeDelta = 0.001;
	// size of feature input vector for each featmode
	// (featmode def'd in TicTDBase. If featmode==8, use
	// TicTDBase.getInputSize8())
	private int inpSize[] = { 6, 6, 10, 19, 13, 19, 0, 0, 0, 9 };
	protected int hiddenSize = 15; // size of hidden layer (only for TD_NNet)
	private Random rand;
//	private int[][] m_trainTable = null;
//	private double[][] m_deltaTable = null;
	private int numFinishedGames = 0;
	private boolean randomSelect = false;
	private boolean m_hasLinearNet;
	private boolean m_hasSigmoid;
	
	/**
	 * Member {@link #m_tdPar} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private TDParams m_tdPar;
	private static final long serialVersionUID = 123L;
	
	//public int epiCount=0;

	/**
	 * Default constructor for TDPlayerTTT, needed for loading a serialized version
	 */
	public TDPlayerTT2() {
		super();
		TDParams tdPar = new TDParams();
		initNet(tdPar, 1000);
	}

	/**
	 * Construct new {@link TDPlayerTTT}, setting everything from tdPar and set default
	 * maxGameNum=1000
	 * 
	 * @param tdPar
	 */
	public TDPlayerTT2(String name, TDParams tdPar) {
		super(name, tdPar.getFeatmode());
		initNet(tdPar, 1000);
	}

	/**
	 * Construct new {@link TDPlayerTTT}, setting everything from tdPar and from maxGameNum
	 * 
	 * @param tdPar
	 * @param maxGameNum
	 */
	public TDPlayerTT2(String name, TDParams tdPar, int maxGameNum) {
		super(name, tdPar.getFeatmode());
		initNet(tdPar, maxGameNum);
	}

	/**
	 * 
	 * @param tdPar
	 * @param maxGameNum
	 */
	private void initNet(TDParams tdPar, int maxGameNum) {
		m_tdPar = new TDParams();
		m_tdPar.setFrom(tdPar);
		super.setFeatmode(tdPar.getFeatmode());
		super.setEpochMax(tdPar.getEpochs());
		if (getFeatmode() > 9) {
			m_Net = null;
		} else {
			if (tdPar.hasLinearNet()) {
				m_Net = new TD_Lin(getInputSize(super.getFeatmode()),
						tdPar.hasSigmoid());
			} else {
				m_Net = new TD_NNet(getInputSize(super.getFeatmode()),
						hiddenSize, tdPar.hasSigmoid());
			}
			// set alpha,beta,gamma,lambda & epochMax,rpropLrn from the TDpars
			// tab
			this.setTDParams(tdPar, maxGameNum);
		}
		// m_EPS=eps;
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal())
				/ maxGameNum;
		m_hasSigmoid = tdPar.hasSigmoid();
		m_hasLinearNet = tdPar.hasLinearNet();
		rand = new Random(System.currentTimeMillis());
		setAgentState(AgentState.INIT);
	}

	/**
	 * Get the next best action and return it
	 * 
	 * @param sob			current game state (is returned unchanged)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param VTable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
	 * @param silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random 
	 * 						
	 * Side effect: sets member randomSelect (true: if action was selected 
	 * at random, false: if action was selected by agent).
	 * See {@link #wasRandomAction()}.
	 */
	public Types.ACTIONS getNextAction(StateObservation sob, boolean random, double[] VTable, boolean silent) {
		int i, j;
		double MaxScore = -Double.MAX_VALUE;
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		StateObserverTTT NewSO;
		int count = 1; // counts the moves with same MaxScore
        Types.ACTIONS actBest = null;
        int iBest;
        
        assert (sob instanceof StateObserverTTT)
		: "StateObservation 'sob' is not an instance of StateObserverTTT";
		StateObserverTTT so = (StateObserverTTT) sob;
		int Player = so.getPlayerPM(); 	 
		//int[][] Table = so.getTable();
        randomSelect = false;
		{
			if (random) {
				if (rand.nextDouble() < m_epsilon) {
					randomSelect = true;
				}
			}
			
			// get the best (or eps-greedy random) action
	        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
	        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
	        //VTable = new double[acts.size()];  // DON'T! The caller has to define VTable with the right length
	        for(i = 0; i < actions.length; ++i)
	        {
	            actions[i] = acts.get(i);
	            NewSO = so.copy();
	            NewSO.advance(actions[i]);
				
//				CurrentScore = Player * getScore(NewSO);
//				if (NewSO.win()) {
//					CurrentScore = Player * (Player + 1.0) / 2.0;   
//					// 0 / 1  version for O / X - win
//				}
				// ???? questionable: what happens in case of a tie and 
	            //
	            // --- here comes the bug fix (/WK/Nov'16): ---
				if (NewSO.isGameOver()) {
					CurrentScore = (-Player)*NewSO.getGameScore();
					// so.getGameScore() returns -1, if 'player', that is the
					// one who *made* the move to 'so', has won. If we multiply
					// this by (-player), we get a reward +1 for a X(player=+1)- 
					// win and a reward -1 for an O(player=-1)-win.
					// And a reward 0 for a tie.
						
					// Fetch game score and normalize it to the range [0,1], since 
					// TD_NNet may build a value function with a sigmoid function
					// mapping to [0,1]. Then it can use only rewards in [0,1].
					// Normalize to +1 (X-win), 0.5 (tie), 0.0 (O-win) for 2-player game:
					CurrentScore = normalize(CurrentScore,so.getMinGameScore(),
									   		 so.getMaxGameScore(),0.0,1.0);
				}  
				else {
					CurrentScore = Player * getScore(NewSO);
				}
				
				if (!silent)
					print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
				if (randomSelect) {
					CurrentScore = rand.nextDouble();
				}
				VTable[i] = CurrentScore;
				if (MaxScore < CurrentScore) {
					MaxScore = CurrentScore;
					actBest = actions[i];
					iBest  = i; 
					count = 1;
				} else if (MaxScore == CurrentScore) {
					count++;	        
				}
	        } // for
	        if (count>1) {  // more than one action with MaxScore: 
	        	// break ties by selecting one of them randomly
	        	int selectJ = (int)(rand.nextDouble()*count);
	        	for (i=0, j=0; i < actions.length; ++i) 
	        	{
	        		if (VTable[i]==MaxScore) {
	        			if (j==selectJ) actBest = actions[i];
	        			j++;
	        		}
	        	}
	        }
	        assert actBest != null : "Oops, no best action actBest";
			if (!silent) {
				System.out.print("---Best Move: ");
	            NewSO = so.copy();
	            NewSO.advance(actBest);
				print_V(Player, NewSO.getTable(), 2 * MaxScore * Player - 1);
			}			
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
	 * Return the agent's score for that after state.
	 * 
	 * @param sob			the current game state;
	 * @return V(), the prob. that X (Player +1) wins from that after state.
	 *         Player*V() is the quantity to be maximized by getNextAction.
	 */
	public double getScore(StateObservation sob) {
		assert (sob instanceof StateObserverTTT)
		: "StateObservation 'sob' is not an instance of StateObserverTTT";
		StateObserverTTT so = (StateObserverTTT) sob;
		int Player = -so.getPlayerPM(); 	// Player is the player who made the move 
									 	// while so has the player who moves next 
		int[][] Table = so.getTable();
		double score = m_Net.getScore((double[]) prepareInputVector(Player,
				Table).clone());
		return score;
	}


	/**
	 * Train the agent (the net) for one complete game episode. Side effect:
	 * AgentBase.incrementGameNum().
	 * 
	 * @param Player
	 *            +1 or -1, player who makes the next move. If Player=+1, the
	 *            initial board position is empty, if Player=-1, the initial
	 *            board position has an 'X' set at a random location (so X is
	 *            always the one who starts the game)
	 * @return true, if agent raised a stop condition (currently only CMAPlayer)
	 */
	public boolean trainAgent(StateObservation sob) {
		return trainAgent(sob, Integer.MAX_VALUE);
	}
	public boolean trainAgent(StateObservation sob, int epiLength) {
		int[][] table = new int[3][3];
		double[] VTable = null;
		double reward = 0.0;
		boolean randomMove;
		boolean finished = false;
		boolean wghtChange = false;
		boolean DEBG = false;
		double Input[], oldInput[];
		String S_old, I_old = null;   // only as debug info
		int del1 = 0;
		StateObserverTTT so;
		int Player;
		Types.ACTIONS actBest;
		boolean isNtuplePlayer = getFeatmode() == 8
				|| this.getClass().getName().equals("TicTacToe.TD_NTPlayer");

//		if (Player == -1) {
//			// set a random "X" in the empty table:
//			int i = (int) (rand.nextDouble() * 3);
//			int j = (int) (rand.nextDouble() * 3);
//			table[i][j] = 1;
//		}
//		so = new StateObserverTTT(table,Player);
		assert (sob instanceof StateObserverTTT) : "Input 'sob' is not of class StateObserverTTT";
		so = (StateObserverTTT) sob;
		Player = so.getPlayerPM();
		table = so.getTable();			// /WK/ Nov'16: bug fix

		m_Net.resetElig(); // reset the elig traces before starting a new game
							// /WK/ NEW/02/2015
		oldInput = prepareInputVector(-Player, table);
		S_old = tableToString(-Player, table);
		if (!isNtuplePlayer)
			I_old = inputToString(oldInput);
		m_Net.calcScoresAndElig(oldInput);
		if (DEBG) {
			for (int i = 0; i < oldInput.length; ++i)
				System.out.print((int) ((3 + oldInput[i]) % 3));
			System.out.println();
		}
		int counter=0;		// count the number of moves
		while (true) {
			VTable = new double[so.getNumAvailableActions()+1];
			actBest = this.getNextAction(so, true, VTable, true);
			randomMove = this.wasRandomAction();
			so.advance(actBest);
			table = so.getTable();
			if (so.win()) {
				reward = (Player + 1.0) / 2.0; // +1 for white win, 0 for black
												// win
				finished = true;
			} else if (so.getNumAvailableActions() == 0) { // tie
				reward = 0.5;
				finished = true;
			} else {
				reward = 0.0;
			}
			counter++;
			if (counter==epiLength) {
				reward=rewardEstimate(so);
				//epiCount++;
				finished = true; 
			}
			Input = prepareInputVector(Player, table);
			if (randomMove && !finished) {
				// no training, go to next move
				m_Net.calcScoresAndElig(Input); // calculate score, write it to
												// old_y[k] for
												// next pass & update
												// eligibilities (NEW
												// WK/02/2015)
				// only for diagnostics
				if (DEBG)
					System.out.println("random move");
				del1 = 0;

			} else {
				// do one training step
				
				// this is the accumulation logic: if eMax>0, then form 
				// mini batches and apply the weight changes only at the end
				// of such mini batches
				int eMax = super.getEpochMax();
				if (eMax==0) {
					wghtChange=true;
				} else {
					if (finished) numFinishedGames++;
					wghtChange = (finished && (numFinishedGames % eMax) == 0);
				}
				
				// either no random move or game is finished >> target signal is
				// meaningful!
				m_Net.updateWeights(reward, Input, finished, wghtChange);
				// contains afterwards a m_Net.calcScoresAndElig(Input);

				// only for diagnostics
				del1 = 1;
				updateTrainCounter(oldInput);
				// Why does updateTrainCounter operate on oldInput (and not on Input)? -
				// Because oldInput is what enters in the gradient for the current learn
				// step (see ev[][] in m_Net.TDLearn, which was filled with updateElig based
				// on oldInput). Only with this we see that the weight for 'XXX' or 'OOO' 
				// were indeed never learned and that their trainCounter stays at 0.

				oldInput = Input;
			}

			// ----- (only for diagnostics) -----
			// don't do this for featmode==5 or ==8, since its feature space
			// (121*3^9 = 2.4E6 states for featmode==5!!) is too big
			//
			// TODO: clarify whether we still need this
			//
			int[] counters = getCounters(S_old);
			hmC.put(S_old, new CounterC(counters[0] + 1, counters[1] + del1));
			if (getFeatmode() != 5 && !isNtuplePlayer) {
				HashMap<String, ScoreC> hsc = hmX.get(I_old);
				if (hsc == null)
					hsc = new HashMap<String, ScoreC>();
				hsc = add_hm_state(hsc, S_old); // increments S_old's
												// ScoreC-counter
				hmX.put(I_old, hsc);
			}
			S_old = tableToString(Player, table); 	// save S_old & I_old for next
			if (!isNtuplePlayer)					// pass
				I_old = inputToString(Input); 		// through while-loop
			// ----- (end diagnostics) -----

			if (finished) {
				if (DEBG)
					if (randomMove)
						System.out.println("Terminated by random move");
				break;
			}
			if (DEBG) {
				for (int i = 0; i < Input.length; ++i)
					System.out.print((int) ((3 + Input[i]) % 3));
				System.out.println();
			}

			Player = so.getPlayerPM();   // advance to the next player
		}
		m_Net.finishUpdateWeights(); // adjust learn params ALPHA & BETA
		m_epsilon = m_epsilon - m_EpsilonChangeDelta;
		incrementGameNum();
		return false;
	}

	protected void updateTrainCounter(double[] oldInput) {
//		if (super.getFeatmode() == 8)
//			super.ntupleSet.updateTrainCounter(oldInput);
	}
	
	public void setTDParams(TDParams tdPar, int maxGameNum) {
		m_Net.setLambda(tdPar.getLambda());
		m_Net.setGamma(tdPar.getGamma());
		if (super.getFeatmode() == 8) {
			m_Net.setAlpha(tdPar.getAlpha());
		} else {
			// set ALPHA and BETA in TD_NNet (TD_Lin) inverse proportional to
			// the fan-in,
			// i.e. divide by the number of neurons on the input side of the
			// weights:
			m_Net.setAlpha( tdPar.getAlpha() / inpSize[getFeatmode()] );
		}
		m_Net.setBeta(tdPar.getAlpha() / hiddenSize); 	// only relevant for
														// TD_NNet
		m_Net.setAlphaChangeRatio(Math.pow(
				tdPar.getAlphaFinal() / tdPar.getAlpha(), 1.0 / maxGameNum));
		//m_Net.setEpochs(tdPar.getEpochs());  // now we use epochs over whole games
		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		m_Net.setRpropInitDelta( tdPar.getAlpha() / inpSize[getFeatmode()] );
	}

	public void setAlpha(double alpha) {
		m_Net.setAlpha(alpha);
	}

	public double getAlpha() {
		// only for debug & testing
		// super.counStates(1);
		int verbose1 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		super.analyze_hmC(getGameNum(), verbose1);
		int verbose2 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		super.analyze_hmX(verbose2);

		return m_Net.getAlpha();
	}

	public double getEpsilon() {
		return m_epsilon;
	}
	
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ", " + (m_hasLinearNet?"LIN":"BP")
						+ ", " + (m_hasSigmoid?"with sigmoid":"w/o sigmoid")
						+ ", lambda:" + m_Net.getLambda()
						+ ", features:" + getFeatmode();
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

// (obsolete, was only relevant for Nimm3)	
//	public int[][] getTrainTable() {
//		return m_trainTable;
//	}
//
//	public double[][] getDeltaTable() {
//		return m_deltaTable;
//	}

	private int getInputSize(int featmode) {
			return inpSize[featmode];
	}

	public int getHiddenSize() {
		return hiddenSize;
	}

	public TDParams getTDParams() {
		return m_tdPar;
	}
}