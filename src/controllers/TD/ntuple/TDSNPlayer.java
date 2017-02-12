package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import params.TCParams;
import params.TDParams;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;

/**
 *         The TD-Learning-Algorithm for TicTacToe using an N-Tuple-System to
 *         model the value-function. The probability of explorative moves is decreased
 *         linearly.
 * 
 * Some functionality is taken from superclass {@link AgentBase} 
 * (gameNum, maxGameNum, AgentState). Some static functions from {@link TicTDBase} are used.
 * 
 * @see PlayAgent
 * @see TicTDBase
 * @see AgentBase
 * @author Markus Thill, Samineh Bagheri, TH Köln
 */
public class TDSNPlayer extends AgentBase implements PlayAgent {
	private Random rand; // generate random Numbers 
	private TDParams tdPar;

	/**
	 * Controls the amount of explorative moves in
	 * {@link #getBestTable(int[][], int[][], int, boolean, boolean, double[][])}
	 * during training. Let p=getGameNum()/getMaxGameNum(). As long as
	 * p < m_epsilon/(1+m_epsilon) we have progress < 0 and we get with certainty a random
	 * (explorative) move. For p \in [EPS/(1+EPS), 1.0] the random move
	 * probability drops linearly from 1 to 0. <br>
	 * m_epsilon = 0.0: too few exploration, = 0.1 (def.): better exploration.
	 */
	private double m_epsilon = 0.1;
	private double m_EpsilonChangeDelta = 0.001;
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

	// Examples for some NTuples
	//best chosen 40, 4-tuples
	private int nTuple[][]={{6, 7, 4, 0},{4, 5, 8, 7},{4, 3, 0, 1},{4, 5, 2, 1},{6, 3, 0, 1},
			{6, 3, 0, 4},{0, 1, 5, 2},{2, 1, 4, 7},{7, 3, 4, 5},{0, 4, 1, 2},{8, 4, 0, 1},{7, 4, 1, 0},
			{7, 4, 0, 1},{8, 7, 4, 1},{7, 4, 8, 5},{8, 7, 4, 1},{7, 8, 5, 4},{4, 8, 7, 6},{2, 1, 5, 4},
			{3, 0, 1, 4},{8, 7, 3, 4},{8, 4, 3, 0},{4, 1, 2, 5},{6, 3, 0, 4},{1, 2, 5, 8},{1, 4, 3, 7},
			{6, 3, 0, 1},{8, 5, 4, 3},{3, 4, 7, 6},{5, 8, 7, 6},{5, 4, 0, 1},{6, 3, 4, 7},{0, 3, 4, 8},
			{6, 3, 7, 8},{2, 1, 4, 0},{3, 7, 4, 1},{1, 2, 5, 4},{8, 5, 1, 4},{6, 7, 8, 4},{6, 3, 0, 1}
	};
	// private int nTuple[][] = {{0,1,2,3,4,5,6,7,8}};
	// private int nTuple[][] = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 4, 8 } };
	// private int nTuple[][] = {{0,1,2,3,4,5,6,7}, {8,7,6,5,4,3,2,1},
	// 		{8,7,6,5,4,3,2,0}, {0,1,2,3,4,5,6,8}};
	// private int nTuple[][] = {{0,1,2,5,8,7}, {0,3,6,7,8,5}, {0,3,6,1,4,7},
	// 		{2,5,8,7,4,1}, {0,1,2,3,4,5}, {6,7,8,3,4,5}, {0,4,8,7,6,3}};
	// private int nTuple[][] =
	// 		{{0,1,2,3},{4,5,6,7},{8,0,3,6},{1,4,7,2},{5,8,0,4},{6,4,2,0}};
	// private int nTuple[][] = {{0,1,2},{3,4,5},{6,7,8},{0,4,8},{0,1,2,5,8,7},
	//	 	{0,3,6,7,8,5}, {0,3,6,1,4,7}, {2,5,8,7,4,1}, {0,1,2,3,4,5},
	//	 	{6,7,8,3,4,5}, {0,4,8,7,6,3}};
	protected int POSVALUES = 3; 			// Possible values for each board field
	protected boolean USESYMMETRY = true; 	// Use symmetries (rotation, mirror) in NTuple-System
	private boolean RANDINITWEIGHTS = false;// Init Weights of Value-Function
											// randomly
	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
	
	private int numCells=9; // specific for TicTacToe
	/**
	 * Create a new TDSN-Player
	 * 
	 * @param tdPar
	 *            All needed Parameters
	 * @param tcPar 
	 * @param maxGameNum
	 *            Number of Training-Games
	 * @throws IOException 
	 */
	TDSNPlayer(TDParams tdPar, TCParams tcPar, int maxGameNum) throws IOException {
		//super(2);
		this.tdPar = tdPar;
		initNet(tcPar,tdPar, maxGameNum);
		setAgentState(AgentState.INIT);		
	}

	/**
	 * Init the N-Tuple-System and set the TD-Params
	 * @param tcPar 
	 * @param init 
	 * 
	 * @param tdPar
	 *            All needed Parameters
	 * @param maxGameNum
	 *            Number of Training-Games
	 * @throws IOException 
	 */
	private void initNet(TCParams tcPar, TDParams tdPar, int maxGameNum) throws IOException {
		//samine//
		tcIn=tcPar.getTcInterval();
		TC=tcPar.getTC();
		USESYMMETRY=tcPar.getUseSymmetry();
		randomness=tcPar.getRandomness();
		tcImm=tcPar.getTCFType();
		randWalk=tcPar.getRandWalk();
		int numTuple=tcPar.getNtupleNumber();
		int maxTuple=tcPar.getNtupleMax();
		//samine//
		if(randomness==true){
			
			if(randWalk==true){
				//random walk
				int WALK=1;
				m_Net = new NTupleValueFunc(maxTuple,numTuple ,POSVALUES, WALK,
						 USESYMMETRY,RANDINITWEIGHTS,tcPar,numCells);
			}else{
				//random point
				m_Net = new NTupleValueFunc( maxTuple,numTuple ,POSVALUES,
						 USESYMMETRY,RANDINITWEIGHTS,tcPar,numCells);
			}
			
		}else{
			//given ntuples
			m_Net = new NTupleValueFunc(nTuple, POSVALUES, USESYMMETRY,
					RANDINITWEIGHTS,tcPar,numCells);
		}
		
		setTDParams(tdPar, maxGameNum);
		//samine//
		// m_EPS = eps;
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
		rand = new Random(System.currentTimeMillis());		
	}

	/**
	 * Get the next best move and return its board position in Table
	 * 
	 * @param Table
	 *            on input the current board position; on output the best
	 *            follow-up board position
	 * @param Player
	 *            +1: White, X; -1: Black, o; Player who makes next move
	 * @return 3x3 double array holding the score for each allowed move (NaN for
	 *         already occupied fields)
	 */
	public double[][] getNextMove(int[][] Table, int Player, boolean silent) {
		int[][] bestTable = new int[3][3];
		double[][] VTable = new double[3][3];
		getBestTable(Table, bestTable, Player, false, silent, VTable);
		TicTDBase.copyTable(bestTable, Table);
		return VTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#getNextMove(int[][], boolean)
	 */
	public void getNextMove(int[][] Table, boolean silent) {
		// Not needed
	}

	/**
	 * Get the next best move but leave Table unchanged
	 * 
	 * @param Table
	 *            the current board position;
	 * @param Player
	 *            +1: White, X; -1: Black, o; Player who makes next move
	 * @return 3x3 double array holding the score for each allowed move (NaN for
	 *         already occupied fields)
	 */
	public double[][] getNextVTable(int[][] Table, int Player, boolean silent) {
		int[][] bestTable = new int[3][3];
		double[][] VTable = new double[3][3];
		getBestTable(Table, bestTable, Player, false, silent, VTable);
		return VTable;
	}

	/**
	 * Based on board position Table, try all possible moves (NewTable) for
	 * player Player and select in BestTable that next board position which
	 * gives the highest score*Player
	 * 
	 * @param Table
	 *            the current board position
	 * @param BestTable
	 *            the resulting next board position
	 * @param Player
	 *            +1: White, X; -1: Black, o; Player who makes the next move
	 * @param random
	 *            true: random selection will occur with a certain probability,
	 *            false: random selection turned off
	 * @param silent
	 *            true: print nothing, false: print on System.out all after
	 *            states, their score, the corresponding input vector and the
	 *            resulting best move (best after state)
	 * @param VTable
	 *            The resulting Scores for all legal Moves (non-legal / occupied
	 *            Fields: NaN)
	 * @return true: if move was selected at random, false: if move was selected
	 *         by agent
	 */
	private boolean getBestTable(int[][] Table, int[][] BestTable, int Player,
			boolean random, boolean silent, double[][] VTable) {

		boolean randomSelect = false;
		double progress = (double) getGameNum() / (double) getMaxGameNum();
		progress = (1 + m_epsilon) * progress - m_epsilon; // = progress +
		// m_EPS*(progress - 1)
		if (random) {
			if (rand.nextDouble() > progress) {
				randomSelect = true;
			}
		}

		int i, j; // Loop-Variable
		double MaxScore = 0; // Need to initialize??
		double CurrentScore = 0; // NetScore*Player, the quantity to be
									// maximized

		int[][] NewTable = new int[3][3]; // Temp. used
		boolean first = true; // For first Loop-iteration
		int count = 1; // counts the moves with same MaxScore

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				VTable[i][j] = Double.NaN; // mark as 'no score available'
				if (Table[i][j] != 0) {
					continue;
				}

				TicTDBase.copyTable(Table, NewTable);
				NewTable[i][j] = Player; // make move

				CurrentScore = Player * getScore(Player, NewTable); // Get Score

				if (TicTDBase.Win(NewTable, Player))
					CurrentScore = +1; // -1/ 1 version

				if (randomSelect) {
					// Whole VTable is filled with rand-Nums
					// Then the highest is picked
					CurrentScore = rand.nextDouble() * 2 - 1; // -1 / 1 -Version
				}
				VTable[i][j] = CurrentScore;

				// First Loop-pass?
				if (first) {
					MaxScore = CurrentScore;
					TicTDBase.copyTable(NewTable, BestTable);
					first = false;
					count = 1;
				} else {
					// A better Value was found
					if (MaxScore < CurrentScore) {
						MaxScore = CurrentScore;
						TicTDBase.copyTable(NewTable, BestTable);
						count = 1;
					} else if (MaxScore == CurrentScore) {
						// if there are 'count' possibilities with the same
						// score MaxScore, each one
						// has the probability 1/count of being selected:
						count++;
						if (rand.nextDouble() < 1.0 / count)
							TicTDBase.copyTable(NewTable, BestTable);
					}
				}
			}
		}
		if (!silent) {
			System.out.print("---Best Move: ");
			TicTDBase.print_V(Player, BestTable, 2 * MaxScore * Player - 1);
		}
		return randomSelect;
	}

	/**
	 * Return the agent's score for that after state.
	 * 
	 * @param Player
	 *            +1: X, White, -1: o, Black; player who made the move
	 * @param Table
	 *            current board position (after state)
	 * @return V(), the prob. that X (Player +1) wins from that after state.
	 *         Player*V() is the quantity to be maximized by getBestTable.
	 */
	public double getScore(int Player, int[][] Table) {
		// Player not needed
		int curTable[] = new int[9];

		for (int i = 0, k = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				curTable[k++] = Table[i][j];
		return m_Net.getScoreI(curTable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#trainNet(int)
	 */
	public boolean trainNet(int Player) {
		int[][] table = new int[3][3];
		boolean upTC=false;
		// Playing-Field
		int[][] BestTable = new int[3][3]; // For getting best After-State
		double[][] VTable = new double[3][3]; // For getting best After-State
		double reward = 0.0; // Reward from environment (0,0.5,1)
		int MoveNum = 0; // Counter for num. of Moves for one game
		boolean randomMove; // Set, if random Move was selected by method
		boolean finished = false; // Set, if Win or Tie is found
		boolean DEBG = false; //what is this for?
		int curBoard[] = new int[9];
		int nextBoard[] = new int[9];

		if (Player == -1) {
			// set a random "X" in the empty table:
			int i = (int) (rand.nextDouble() * 3);
			int j = (int) (rand.nextDouble() * 3);
			table[i][j] = 1;
			MoveNum++; // was this forgotten in TDSPlayer?
		}

		if (m_Net.LAMBDA!=0.0) {
			m_Net.resetElig(); // reset the elig traces before starting a new game
			//
			// now update the elig traces according to the start state in table:
			for (int i = 0, k = 0; i < 3; i++)
				for (int j = 0; j < 3; j++,k++) 
					curBoard[k] = table[i][j];
		
			m_Net.calcScoresAndElig(curBoard);
		}
			
		while (!finished) {
			randomMove = getBestTable(table, BestTable, Player, true, true,
					VTable);
			if (TicTDBase.Win(BestTable, Player)) {
				// reward = (Player + 1.0) / 2.0; // +1 for white win, 0 for
				// black win
				reward = Player; // +1 for white win, -1 for black win
				//samine//update c
				finished = true;
			} else if (MoveNum == 8) { // tie
				// reward = 0.5;
				reward = 0.0;
				//samine//update c
				finished = true;
			}

			// convert state and after-state boards into one-dimensional array
			for (int i = 0, k = 0; i < 3; i++)
				for (int j = 0; j < 3; j++) {
					curBoard[k] = table[i][j];
					nextBoard[k++] = BestTable[i][j];
				}
			
			if (randomMove && !finished) {
				// no training, go to next move
				m_Net.calcScoresAndElig(nextBoard); // but update eligibilities for next pass
				if (DEBG)
					System.out.println("random move");
				
			} else {
				// Debugging
				// double s = ((NTuple)m_Net).putRealVal(curTable, table, Player);

				m_Net.updateWeights(curBoard, nextBoard,
						finished, reward,upTC);
				// contains an updateElig(nextBoard,...) in the end, if LAMBDA>0
			}
			
			TicTDBase.copyTable(BestTable, table);
			MoveNum++;
			Player *= (-1);
		}
		try {
			this.finishMarkMoves(null);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		incrementGameNum();
		//samine// updating tcFactor array after each complete game
		if(getGameNum()%tcIn==0 && TC && !tcImm)
			 m_Net.updateTC();
		
		if (PRINTTABLES) {
			if(getGameNum()%10==0 && TC)
				m_Net.printTables();
		}
		
		return false;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see RLgame.PlayAgent#markMove(int[][], int)
//	 */
//	public void markMove(int[][] table, int player) {
//		markMove(table, player, false);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see RLgame.PlayAgent#markMove(int[][], int, boolean)
//	 */
//	public void markMove(int[][] table, int player, boolean humanMoveDone) {
//		// Needed only during (human) Play.
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#finishMarkMoves(int[][])
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
		double a0 = tdPar.getEpsilon();
		double a1 = tdPar.getEpsilonFinal();
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
	
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ", USESYMMETRY:" + (USESYMMETRY?"true":"false")
						+ ", " + "sigmoid: tanh"
						+ ", lambda:" + m_Net.getLambda();
		return str;
	}
	


	public NTuple[] getNTuples() {
		return m_Net.getNTuples();
	}

	public void setAlpha(double alpha) {
		m_Net.setAlpha(alpha);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#setTDParams(RLgame.TDParams, int)
	 */
	public void setTDParams(TDParams tdPar, int maxGameNum) {
		m_Net.setLambda(tdPar.getLambda());
		m_Net.setGamma(tdPar.getGamma());

		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#getAlpha()
	 */
	public double getAlpha() {
		return m_Net.getAlpha();
	}
	public double getLambda() {
		return m_Net.getLambda();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#getEpsilon()
	 */
	public double getEpsilon() {
		return m_epsilon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#getDeltaTable()
	 */
	public double[][] getDeltaTable() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see RLgame.PlayAgent#getTrainTable()
	 */
	public int[][] getTrainTable() {
		return null;
	}

}
