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

import params.NTParams;
import params.TDParams;
import tools.Types;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.XNTupleFuncs;

/**
 * The TD-Learning {@link PlayAgent} (Temporal Difference reinforcement learning)
 * <b>with n-tuples</b>. 
 * It has a one-layer (perceptron-like) network with output-nonlinearty tanh 
 * to model the value function. 
 * The net follows closely the (pseudo-)code by [SuttonBonde93]. 
 * <p>
 * Some functionality is packed in the superclass 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...)
 * 
 * @see PlayAgent
 * @see AgentBase
 * 
 * @author Wolfgang Konen, TH Köln, Feb'17
 */
//
// This agent is adapted from project SourceTTT, class TicTacToe.TDSNPlayer
//
public class TDNTupleAgt extends AgentBase implements PlayAgent,Serializable {
	private Random rand; // generate random Numbers 
	static transient public PrintStream pstream = System.out;

	/**
	 * Controls the amount of explorative moves in
	 * {@link #getNextAction(StateObservation, boolean, double[], boolean)}
	 * during training. Let p=getGameNum()/getMaxGameNum(). As long as
	 * p < m_epsilon/(1+m_epsilon) we have progress < 0 and we get with certainty a random
	 * (explorative) move. For p \in [EPS/(1+EPS), 1.0] the random move
	 * probability drops linearly from 1 to 0. <br>
	 * m_epsilon = 0.0: too few exploration, = 0.1 (def.): better exploration.
	 */
	private double m_epsilon = 0.1;
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
	private NTupleValueFunc m_Net;

	protected boolean USESYMMETRY = true; 	// Use symmetries (rotation, mirror) in NTuple-System
	private boolean NORMALIZE = false; 
	private boolean RANDINITWEIGHTS = false;// Init Weights of Value-Function
											// randomly
	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
	
	private int numCells=9; 			// specific for TicTacToe
	private int POSVALUES = 3; 			// Possible values for each board field
	
	//
	// from TDAgent
	//
	private int numFinishedGames = 0;
	private boolean randomSelect = false;
	
	/**
	 * Members {@link #m_tdPar} {@link #m_ntPar} are only needed for saving and loading
	 * the agent (to restore the agent with all its parameter settings)
	 */
	private TDParams m_tdPar;
	private NTParams m_ntPar;
	
	//public int epiCount=0;

	/**
	 * Default constructor for {@link TDNTupleAgt}, needed for loading a serialized version
	 */
	public TDNTupleAgt() throws IOException {
		super();
		TDParams tdPar = new TDParams();
		NTParams ntPar = new NTParams();
		initNet(ntPar, tdPar, null, null, 1000);
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
	public TDNTupleAgt(String name, TDParams tdPar, NTParams ntPar, int[][] nTuples, 
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
		m_tdPar = new TDParams();
		m_tdPar.setFrom(tdPar);
		m_ntPar = new NTParams();
		m_ntPar.setFrom(ntPar);
		rand = new Random(42); //(System.currentTimeMillis());		
		//samine//
		tcIn=ntPar.getTcInterval();
		TC=ntPar.getTC();
		USESYMMETRY=ntPar.getUseSymmetry();
		NORMALIZE=tdPar.getUseNormalize();
		tcImm=ntPar.getTCFType();
		
		randomness=ntPar.getRandomness();
		randWalk=ntPar.getRandWalk();
		int numTuple=ntPar.getNtupleNumber();
		int maxTupleLen=ntPar.getNtupleMax();
		
		m_Net = new NTupleValueFunc(nTuples, xnf, POSVALUES, USESYMMETRY,
				RANDINITWEIGHTS,ntPar,numCells);
		
		setTDParams(tdPar, maxGameNum);
		//samine//
		// m_EPS = eps;
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
		
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
	 * Side effect: sets member randomSelect (true: if action was selected 
	 * at random, false: if action was selected by agent).
	 * See {@link #wasRandomAction()}.
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
       
		int player = so.getPlayerPM(); 	 
		//int[][] Table = so.getTable();
		
		randomSelect = false;
		double progress = (double) getGameNum() / (double) getMaxGameNum();
		progress = (1 + m_epsilon) * progress - m_epsilon; 	// = progress +
															// m_EPS*(progress - 1)
		if (random) {
			double rd = rand.nextDouble();
			//System.out.println("rd="+rd);
			if (rd > progress) {
				randomSelect = true;
			}
		}
//        randomSelect = false;
//		if (random) {
//			randomSelect = (rand.nextDouble() < m_epsilon);
//		}
		
		// get the best (or eps-greedy random) action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        //VTable = new double[acts.size()];  
        // DON'T! The caller has to define VTable with the right length
        
        for(i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
            NewSO = so.copy();
            NewSO.advance(actions[i]);
			
			if (NewSO.isGameOver()) {
				switch (so.getNumPlayers()) {
				case 1: 
					CurrentScore = NewSO.getGameScore();
					break;
				case 2: 
					CurrentScore = (-1)*NewSO.getGameScore();		// CORRECT
					// NewSO.getGameScore() returns -1, if 'player', that is the
					// one who *made* the move to 'so', has won. If we multiply
					// this by (-1), we get a reward +1 for a X(player=+1)- 
					// win and *also* a reward +1 for an O(player=-1)-win.
					// And a reward 0 for a tie.
					//
					//CurrentScore = (-player)*NewSO.getGameScore(); // WRONG!!
					// NewSO.getGameScore() returns -1, if 'player', that is the
					// one who *made* the move to 'so', has won. If we multiply
					// this by (-player), we get a reward +1 for a X(player=+1)- 
					// win and a reward -1 for an O(player=-1)-win.
					// And a reward 0 for a tie.
					break;
				default: 
					throw new RuntimeException("TDNTupleAgt.trainAgent does not yet "+
							"implement case so.getNumPlayers()>2");
				}
			}  else {
				// 
				// TODO: clarify whether this should be normalized to [0,1] as well (!!!)
				//       If so, correct this for TDAgent as well (!!)
				//
				CurrentScore = player * getScore(NewSO);
										// here we ask this agent for its score estimate on NewSO
			}
			
			if (NORMALIZE) {
				// Normalize to [-1,+1] (the appropriate range for tanh-sigmoid):
				//
				// (this will have no effect for TicTacToe or other games where the 
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
			System.out.println(NewSO.toString()+", "+(2*MaxScore*player-1));
			//print_V(Player, NewSO.getTable(), 2 * MaxScore * Player - 1);
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
	 * @param so			the current game state;
	 * @return V(), the prob. that X (Player +1) wins from that after state.
	 *         Player*V() is the quantity to be maximized by getNextAction.
	 */
	public double getScore(StateObservation so) {
		int[] bvec = m_Net.xnf.getBoardVector(so);
		double score = m_Net.getScoreI(bvec);
		return score;
	}

	/**
	 * @see #trainAgent(StateObservation, int)
	 */
	public boolean trainAgent(StateObservation sob) {
		return trainAgent(sob, Integer.MAX_VALUE);
	}
	/**
	 * Train the Agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState01()} to get
	 * 					some exploration of different game paths)
	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
	 * 					prematurely.  
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so, int epiLength) {
		//int[][] table = new int[3][3];
		double[] VTable = null;
		double reward = 0.0;
		boolean randomMove;
		boolean finished = false;
		boolean wghtChange = false;
		boolean upTC=false;
		boolean DEBG = false;
		double Input[], oldInput[];
		String S_old, I_old = null;   // only as debug info
		int player;
		Types.ACTIONS actBest;
		int[] curBoard = m_Net.xnf.getBoardVector(so);
		int[] nextBoard = null;

		//System.out.println("Random test: "+ rand.nextDouble());
		//System.out.println("Random test: "+ rand.nextDouble());
		
		player = so.getPlayerPM();

		if (m_Net.LAMBDA!=0.0) {
			m_Net.resetElig(); // reset the eligibility traces before starting a new game
			m_Net.calcScoresAndElig(curBoard);
		}

		//oldInput = m_feature.prepareFeatVector(so);
		//S_old = so.toString();   
		//S_old = tableToString(-Player, table);
		int counter=0;		// count the number of moves
		while (true) {
			VTable = new double[so.getNumAvailableActions()+1];
			actBest = this.getNextAction(so, true, VTable, true);
			//actBest = this.getNextAction(so, false, VTable, true);  // Debug only
			randomMove = this.wasRandomAction();
			so.advance(actBest);
			nextBoard = m_Net.xnf.getBoardVector(so);
			//if (DEBG) printVTable(pstream,VTable);
			if (DEBG) printTable(pstream,nextBoard);
			if (so.isGameOver()) {
				// Fetch a reward and normalize it to the range [0,1], since 
				// TD_NNet may build a value function with a sigmoid function
				// mapping to [0,1]. Then it can use only rewards in [0,1].
				switch (so.getNumPlayers()) {
				case 1: 
					reward = so.getGameScore();
					break;
				case 2: 
					reward = (-player)*so.getGameScore();
					// so.getGameScore() returns -1, if 'player', that is the
					// one who *made* the move to 'so', has won. If we multiply
					// this by (-player), we get a reward +1 for a X(player=+1)- 
					// win and a reward -1 for an O(player=-1)-win.
					// And a reward 0 for a tie.
					break;
				default: 
					throw new RuntimeException("TDPlayer.trainAgent not yet "+
							"implementing case so.getNumPlayers()>2");
				}
				// Normalize to +1 (X-win), 0.5 (tie), 0.0 (O-win) for 2-player game:
				//reward = normalize(reward,so.getMinGameScore(),
				//				   so.getMaxGameScore(),0.0,1.0);
				finished = true;
			} else {
				reward = 0.0;
			}
			counter++;
			if (counter==epiLength) {
				reward=estimateGameValue(so);
				//epiCount++;
				finished = true; 
			}
			//Input = m_feature.prepareFeatVector(so);
			if (randomMove && !finished) {
				// no training, go to next move
				m_Net.calcScoresAndElig(nextBoard); // but update eligibilities for next pass
				// only for diagnostics
				if (DEBG)
					pstream.println("random move");

			} else {
				// do one training step
				
				m_Net.updateWeights(curBoard, nextBoard,
						finished, reward,upTC);
				// contains an updateElig(nextBoard,...) in the end, if LAMBDA>0

// -- accumulation logic not yet implemented for TDNTupleAgt --
//
//				// this is the accumulation logic: if eMax>0, then form 
//				// mini batches and apply the weight changes only at the end
//				// of such mini batches
//				int eMax = super.getEpochMax();
//				if (eMax==0) {
//					wghtChange=true;
//				} else {
//					if (finished) numFinishedGames++;
//					wghtChange = (finished && (numFinishedGames % eMax) == 0);
//				}
//				
//				// either no random move or game is finished >> target signal is
//				// meaningful!
//				m_Net.updateWeights(reward, Input, finished, wghtChange);
//				// contains afterwards a m_Net.calcScoresAndElig(Input);
//
//				oldInput = Input;
			}

			curBoard = nextBoard; 
			
			if (finished) {
				if (DEBG)
					if (randomMove) {
						pstream.println("Terminated game "+(getGameNum()) + " by random move. Reward = "+reward);						
					} else {
						pstream.println("Terminated game "+(getGameNum()) + ". Reward = "+reward);
					}
				
				break;
			}

			player = so.getPlayerPM();   // advance to the next player
		} // while

		try {
			this.finishMarkMoves(null);		// adjust learn params ALPHA & m_epsilon
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//m_epsilon = m_epsilon - m_EpsilonChangeDelta;
		
		incrementGameNum();
		//samine// updating tcFactor array after each complete game
		if(getGameNum()%tcIn==0 && TC && !tcImm)
			 m_Net.updateTC();
		
		//if (DEBG) m_Net.printLutSum(pstream);
		if (DEBG) m_Net.printLutHashSum(pstream);
		if (PRINTTABLES) {
			if(getGameNum()%10==0 && TC)
				m_Net.printTables();
		}
		
		return false;
	}
	
	/**
	 * call m_Net.finishUpdateWeights (adjust ALPHA) and adjust m_epsilon
	 */
	public void finishMarkMoves(int[][] table) {
		// m_Net.setAlpha(0.0009*Math.exp(-0.03*(getGameNum()+1))+0.0001);
		// m_Net.setAlpha(-(1.0/Math.pow(4000.0,4))*(0.1e-2-0.1e-3)*Math.pow(getGameNum(),4)+0.1e-2);

		// double a0 = 1;
		// double a1 = 0.1;
		// double x = (double)getGameNum()/getMaxGameNum()*8.0;
		// double fx = (a0-a1)/2.0*(1-Math.tanh(x-4)) + a1;
		// m_Net.setAlpha(fx);

		m_Net.finishUpdateWeights(); // adjust learn param ALPHA

		// TODO: wieder in alten Wert ändern
		// m_epsilon = m_epsilon - m_EpsilonChangeDelta;
		// m_epsilon = m_epsilon*m_EpsilonChangeDelta;

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
		epsilon.close();// TODO Auto-generated method stub
	}

	public void setTDParams(TDParams tdPar, int maxGameNum) {
		m_Net.setLambda(tdPar.getLambda());
		m_Net.setGamma(tdPar.getGamma());

		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		//m_Net.setRpropInitDelta( tdPar.getAlpha() / inpSize[m_feature.getFeatmode()] );
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
	
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ", USESYMMETRY:" + (USESYMMETRY?"true":"false")
						+ ", NORMALIZE:" + (NORMALIZE?"true":"false")
						+ ", " + "sigmoid: tanh"
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

	public TDParams getTDParams() {
		return m_tdPar;
	}

	// Debug only: 
	//
	private void printTable(PrintStream pstream, int[] board) {
		String s = NTuple.stringRep(board);
		pstream.println(s + " : MaxScore= "+MaxScore);
	}

	private void printVTable(PrintStream pstream, double[] VTable) {
		for (int i=0; i<VTable.length; i++) {
			pstream.print(VTable[i]+", ");
		}
		System.out.println("");
	}
}