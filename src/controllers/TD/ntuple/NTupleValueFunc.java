package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

import params.NTParams;
import params.TDParams;

/**
 *         Implementation of an learning value-function using a n-Tuple-System.
 *         A set of n-Tuples is generated randomly or by user precept. Random
 *         n-Tuples can be just a set of random points or a random walk on the
 *         board. The value-function uses symmetries of the board to allow a
 *         faster training. The output of the value-function is always put
 *         through a sigmoid-function (tanh) to get the value in the range -1 ..
 *         +1. The learning rate alpha decreases exponentially from a start
 *         value at the beginning of the training to an end value after a certain
 *         amount of games.
 * 
 * @author Markus Thill, Wolfgang Konen (extension TD(lambda)), TH Köln, Feb'17  
 */
public class NTupleValueFunc implements Serializable {
	/* Experimental Parameters: */
	// protected double EPS = 0.5; /* random weights init scale */
	// protected double BIAS = 1.0; /* strength of the bias (constant input) */

	/* initial learning rate (typically 1/n) */
	protected double ALPHA = 0.1;

	/* discount-rate parameter (typically 0.9) */
	protected double GAMMA = 0.9;

	/* eligibility trace decay parameter (should be <= GAMMA) */
	protected double LAMBDA = 0.0;
	protected double m_AlphaChangeRatio = 0.9998; // 0.998
	protected int epochMax=1;
    protected boolean  rpropLrn=false;

	// Needed for generating random n-Tuples
	private Random rand = new Random(42);

	// Number of generated n-Tuples
	private int numTuples = 0;

	// The generated n-Tuples
	private NTuple nTuples[][];

	// Turns usage of symmetry on or off
	private boolean useSymmetry = false;

	private boolean PRINTNTUPLES = false;	// /WK/ control the file printout of n-tuples
	private DecimalFormat frmS = new DecimalFormat("+0.00000;-0.00000");

	/**
	 * Constructor using a set of n-tuples that are predefined.
	 * 
	 * @param nTuples
	 *            The set of n-tuples as an {@code int} array. TicTacToe: Allowed values 
	 *            for the sampling points: 0-8
	 * @param posVals
	 *            Possible values/field of the board (TicTacToe: 3)
	 * @param useSymmetry
	 *            true, if symmetries shall be used
	 * @param randInitWeights
	 *            true, if all weights of all n-Tuples shall be initialized
	 *            randomly
	 */
	public NTupleValueFunc(int nTuples[][], int posVals, boolean useSymmetry,
			boolean randInitWeights, NTParams tcPar, int numCells) 
					throws RuntimeException {
		numTuples = nTuples.length;
		this.useSymmetry = useSymmetry;

		initNTuples(nTuples, posVals, randInitWeights, tcPar, numCells);
	}

	/**
	 * Generate a set of random n-tuples (random POINTS).
	 * 
	 * @param maxTupleLen
	 *            length of each n-tuple 
	 * @param tupleNum
	 *            number of tuples that shall be generated
	 * @param posVals
	 *            possible values/field of the board (TicTacToe: 3)
	 * @param useSymmetry
	 *            true, if symmetries shall be used
	 * @param randInitWeights
	 *            true, if all weights of all n-Tuples shall be initialized
	 *            randomly
	 * @throws IOException may only happen if PRINTNTUPLES=true
	 */
// -- OLD comment was: --	
//	 * @param maxTupleLen
//	 *            max. length of a n-tuple (every generated tuple has a length
//	 *            in the range 2 .. maxTupleLen)	
	public NTupleValueFunc(int maxTupleLen, int tupleNum, int posVals, boolean useSymmetry, 
			boolean randInitWeights, NTParams tcPar, int numCells) 
					throws IOException {
		numTuples = tupleNum;
		this.useSymmetry = useSymmetry;
		int nTuples[][] = new int[numTuples][];
		for (int i = 0; i < numTuples; i++) {
			//int tupleLen = rand.nextInt(maxTupleLen - 1) + 2; // min. length is 2
			//samine// just commented the last line to change tupleLen to the given length
			//to get rid of that 
			int tupleLen=maxTupleLen;													
			int[] tuple = new int[tupleLen];
			boolean isUnique;
			for (int j = 0; j < tupleLen; j++) {
				int sp;
				do {
					isUnique = true;
					sp = rand.nextInt(numCells); 
					for (int k = 0; k < j; k++)
						if (tuple[k] == sp) {
							isUnique = false;
							break;
						}
				} while (!isUnique);

				tuple[j] = sp;
			}
			nTuples[i] = tuple;
		}
		
		if (PRINTNTUPLES) {
			//samine//printing the generated n-tuples
			for(int h=0;h<numTuples;h++)
				print(nTuples[h]);		// may throw IOException
		}

		initNTuples(nTuples, posVals, randInitWeights, tcPar, numCells);
	}

	/**
	 * Generate a set of random n-tuples (random WALK).
	 * 
	 * @param maxTupleLen
	 *            length of each n-tuple 
	 * @param tupleNum
	 *            Number of tuples that shall be generated
	 * @param posVals
	 *            Possible values/field of the board (TicTacToe: 3)
	 * @param WALK
	 * 			  dummy parameter to distinguish this constructor from the random POINTS constructor            
	 * @param useSymmetry
	 *            true, if symmetries shall be used
	 * @param randInitWeights
	 *            true, if all weights of all n-Tuples shall be initialized
	 *            randomly
	 * @throws IOException may only happen if PRINTNTUPLES=true
	 */
	// -- OLD comment was: --	
//	 * @param maxTupleLen
//	 *            max. length of a n-tuple (every generated tuple has a length
//	 *            in the range 2 .. maxTupleLen)
	public NTupleValueFunc(int maxTupleLen, int tupleNum, int posVals, int WALK, 
			boolean useSymmetry, boolean randInitWeights, NTParams ntPar, int numCells) 
					throws IOException {
		numTuples = tupleNum;
		this.useSymmetry = useSymmetry;
		int nTuples[][] = new int[numTuples][];

		for (int i = 0; i < numTuples; i++) {
			int numWalkSteps = maxTupleLen;
			int[] nTuple;
			int[] tmpTuple = new int[numWalkSteps];
			int tupleLen = 1;
			int lastMove = rand.nextInt(numCells); // start position
			tmpTuple[0] = lastMove;

			for (int j = 0; j < numWalkSteps - 1; j++) {
				boolean madeMove = false;
				do {
					int sp = rand.nextInt(6); 	// For TicTacToe only
											  	// /WK/ why 6 and not 9 ??
					switch (sp) {
					case 0:
						if (lastMove / 3 != 2) {
							madeMove = true;
							lastMove = lastMove + 3;
						}
						break;
					case 1:
						if (lastMove / 3 != 0) {
							madeMove = true;
							lastMove = lastMove - 3;
						}
						break;
					case 2:
						if (lastMove % 3 != 2) {
							madeMove = true;
							lastMove = lastMove + 1;
						}
						break;
					case 3:
						if (lastMove % 3 != 0) {
							madeMove = true;
							lastMove = lastMove - 1;
						}
						break;
					case 4:
						if (lastMove / 3 != 2 && lastMove % 3 != 2) {
							madeMove = true;
							lastMove = lastMove + 4;
						}
						break;
					case 5:
						if (lastMove / 3 != 0 && lastMove % 3 != 0) {
							madeMove = true;
							lastMove = lastMove - 4;
						}
						break;
					}
				} while (!madeMove);
				int k;
				for (k = 0; k < tupleLen && tmpTuple[k] != lastMove; k++)
					;
				if (k == tupleLen) {
					tmpTuple[tupleLen] = lastMove;
					tupleLen++;
				}
			}
			// TODO: remove??
			if (tupleLen == numWalkSteps) {
				nTuple = new int[tupleLen];
				for (int j = 0; j < tupleLen; j++)
					nTuple[j] = tmpTuple[j];
				nTuples[i] = nTuple;
			} else
				i--;			
		}
		
		if (PRINTNTUPLES) {
			for(int h=0;h<tupleNum;h++)
				   print(nTuples[h]);		// may throw IOException
		}
		
		initNTuples(nTuples, posVals, randInitWeights, ntPar, numCells);
	}

	void initNTuples(int[][] nTuples, int posVals, boolean randInitWeights,
			NTParams ntPar, int numCells) {
		this.nTuples = new NTuple[2][numTuples];
		for (int i = 0; i < numTuples; i++) {
			for (int j=0; j<nTuples[i].length; j++) {
				int v = nTuples[i][j];
				if (v<0 || v>=numCells) 
					throw new RuntimeException("Invalid cell number "+v+" in n-tuple no. "+i);
			}
			this.nTuples[0][i] = new NTuple(nTuples[i], posVals, ntPar);
			this.nTuples[1][i] = new NTuple(nTuples[i], posVals, ntPar);
			if (randInitWeights) {
				this.nTuples[0][i].initWeights(true);
				this.nTuples[1][i].initWeights(true);
			}
		}
	}

	/**
	 * @return The List of N-Tuples
	 */
	public NTuple[] getNTuples() {
		NTuple list[] = new NTuple[numTuples * 2];
		for (int j = 0, k = 0; j < nTuples[0].length; j++)
			for (int i = 0; i < nTuples.length; i++)
				list[k++] = nTuples[i][j];
		return list;
	}

	public void resetElig() {
		for (int j = 0, k = 0; j < nTuples[0].length; j++)
			for (int i = 0; i < nTuples.length; i++)
				nTuples[i][j].resetElig();
	}

	public void calcScoresAndElig(int[] curTable) {
    	double v_old = getScoreI(curTable);	
    	double e = (1.0 - v_old * v_old);   // derivative of tanh
        updateElig(curTable,e);	
	}

	public void finishUpdateWeights() {
		ALPHA = ALPHA * m_AlphaChangeRatio;
	}

	/**
	 * rotate the given board once
	 * 
	 * @param board
	 * @return the rotated board
	 */
	private int[] rotate(int[] board) {
		// Currently only for TicTacToe
		int[] newBoard = new int[9];
//		for (int i = 2, k = 0; i >= 0; i--)
//			for (int j = 0; j < 9; j += 3)
//				newBoard[k++] = board[i + j];
		int[] ri = {6,3,0,7,4,1,8,5,2};
		for (int k=0; k<9; k++) newBoard[k] =board[ri[k]];
		return newBoard;
	}

	/**
	 * Mirror the board at the center column
	 * 
	 * @param board
	 * @return the mirrored board
	 */
	private int[] mirror(int[] board) {
		// Mirror Board,. Currently only for Tic-Tac-Toe
		int[] newBoard = new int[9];
		for (int i = 2, k = 0; i >= 0; i--)
			for (int j = 0; j < 3; j++)
				newBoard[k++] = board[i * 3 + j];
		return newBoard;
	}

	public int countPieces(int board[]) {
		int count = 0;
		for (int i = 0; i < board.length; i++)
			if (board[i] != 0)		// remember: board[i]==1 for empty cells (!)
				count++;
		return count;
	}

	/* OLD interface: get the value for this state */
//	public double getScore(double[] board) {
//		int[] boardI = new int[board.length];
//		for (int i=0; i<board.length; i++)
//			boardI[i] = (int) board[i];
//		return getScoreI(boardI);
//	}
	
	/**
	 * Get the value for this state
	 * 
	 * @param board the state as 1D-integer vector (position value for each board cell) 
	 * @return
	 */
	public double getScoreI(int[] board) {
		int i, j;
		double score = 0.0;
		int[][] equiv = null;

		// Get player
		int player = countPieces(board) % 2;

// -- deprecated --
//		// Get equivalent Boards
//		if (useSymmetry) {
//			equiv = getSymBoards(board);
//		}
//
//		for (i = 0; i < numTuples; i++) {
//			score += nTuples[player][i].getScore(board);
//			if (useSymmetry)
//				for (j = 0; j < equiv.length; j++)
//					score += nTuples[player][i].getScore(equiv[j]);
//		}
		
		// Get equivalent boards
		equiv = getSymBoards2(board, useSymmetry);

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++)
				score += nTuples[player][i].getScore(equiv[j]);
		}

		return Math.tanh(score);
	}

	/**
	 * Get the seven equivalent Positions to one board. These can be generated
	 * with mirroring and rotation.
	 * 
	 * @param board
	 * @return The seven equivalent Positions
	 */
	@Deprecated
	private int[][] getSymBoards(int[] board) {
		int i;
		int[][] equiv = new int[7][];
		equiv[0] = rotate(board);
		for (i = 1; i < 3; i++)
			equiv[i] = rotate(equiv[i - 1]);
		equiv[i] = mirror(board);
		for (++i; i < 7; i++)
			equiv[i] = rotate(equiv[i - 1]);
		return equiv;
	}

	/**
	 * Get the eight equivalent positions to one board. The first one 
	 * is the board itself. The other can be generated
	 * with mirroring and rotation.
	 * 
	 * @param board
	 * @param useSymmetry if false, return a 2D array with only one row 
	 * 			(the board itself in int[0][])
	 * @return The eight equivalent positions
	 */
	private int[][] getSymBoards2(int[] board, boolean useSymmetry) {
		int i;
		int[][] equiv = null;
		if (useSymmetry) {
			equiv = new int[8][];
			equiv[0] = board;
			for (i = 1; i < 4; i++)
				equiv[i] = rotate(equiv[i - 1]);
			equiv[i] = mirror(board);
			for (i=5; i < 8; i++)
				equiv[i] = rotate(equiv[i - 1]);
		} else {
			equiv = new int[1][];
			equiv[0] = board;			
		}
		
//		if (DEBG) {
//			NTuple.print(equiv);
//			int dummy=1;
//		}
		
		return equiv;
	}

	/**
	 * Update the weights of the n-Tuple-System.
	 * 
	 * @param curBoard
	 *            the current Board
	 * @param nextBoard
	 *            the following board
	 * @param finished
	 *            true, if game is over
	 * @param reward
	 *            reward given for a terminated game (-1,0,+1)
	 */
	public void updateWeights(int[] curBoard, int[] nextBoard,
			boolean finished, double reward, boolean upTC) {
		double v_old = getScoreI(curBoard); // Old Value
		double tg; // Target-Signal
		// tg contains reward OR GAMMA * value of the after-state
		tg = (finished ? reward : GAMMA * getScoreI(nextBoard));
		// delta is the error signal
		double delta = (tg - v_old);
		// derivative of tanh
		double e = (1.0 - v_old * v_old);

		double dW = ALPHA * delta * e;

		// update every single N-Tuple LUT
		// temporal coherence// update N and A matrices
		// samine//

		update(curBoard, delta, e);
		//update(curBoard, dW, dW);   // /WK/
		
		if (LAMBDA!=0.0) updateElig(nextBoard, e);

	}

	/**
	 * Update all n-Tuple LUTs. Simply add dW to all relevant weights. Also
	 * update the symmetric boards, if wanted.
	 * 
	 * @param board
	 *            board, for which the weights shall be updated
	 * @param delta
	 *            ALPHA*delta*e is the value added to all weights (LAMBDA==0)
	 */
	private void update(int[] board, double delta, double e) {
		int i, j;
		int[][] equiv = null;

		// Get player
		int player = countPieces(board) % 2;

// -- deprecated --
//		// Get equivalent Boards
//		if (useSymmetry) {
//			equiv = getSymBoards(board);
//		}
//
//		for (i = 0; i < numTuples; i++) {
//			nTuples[player][i].update(board, ALPHA, delta, e, LAMBDA);
//			if (useSymmetry)
//				for (j = 0; j < equiv.length; j++)
//					nTuples[player][i].update(equiv[j], ALPHA, delta, e, LAMBDA);
//		}
		
		// Get equivalent Boards
		equiv = getSymBoards2(board,useSymmetry);

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++)
				nTuples[player][i].update(equiv[j], ALPHA, delta, e, LAMBDA);
		}
	}

	/**
	 * Update all n-Tuple eligibility traces. 
	 * (Only the traces for the active player are updated/decayed.)
	 * 
	 * @param board
	 *            board, for which the eligibility traces shall be updated
	 * @param e
	 *            the derivative of the sigmoid function
	 */
	private void updateElig(int[] board, double e) {
		int i, j;
		int[][] equiv = null;

		// Get player
		int player = countPieces(board) % 2;

//		// Get equivalent Boards
//		if (useSymmetry) {
//			equiv = getSymBoards(board);
//		}
//
//		for (i = 0; i < numTuples; i++) {
//			nTuples[player][i].updateElig(board, LAMBDA, GAMMA, e);
//			if (useSymmetry)
//				for (j = 0; j < equiv.length; j++)
//					nTuples[player][i].updateElig(equiv[j], LAMBDA, GAMMA, e);
//		}

		// Get all 8 equivalent Boards (including board)
		equiv = getSymBoards2(board,useSymmetry);

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++)
				nTuples[player][i].updateElig(equiv[j], LAMBDA, GAMMA, e);
		}
	}

	// samine// updating TCfactor for all ntuples after every tcIn games
	public void updateTC() {
		int i, k;
			for (i = 0; i < numTuples; i++) {
				for (k = 0; k < 2; k++)
					nTuples[k][i].updateTC();
			}

	}
	
	public void setAlpha(double newStartAlpha) {
		ALPHA = newStartAlpha;
	}

	public void setGamma(double newGamma) {
		GAMMA = newGamma;
	}

	public void setLambda(double newLambda) {
		LAMBDA = newLambda;
	}

	public void setAlphaChangeRatio(double newAlphaChangeRatio) {
		m_AlphaChangeRatio = newAlphaChangeRatio;
	}

	public void setEpochs(int epochs) {
		epochMax = epochs;
	}
	public void setRpropLrn(boolean hasRpropLrn) {
		rpropLrn = hasRpropLrn;
	}
	public void setRpropInitDelta(double initDelta) {
		// dummy
	}

	public double getAlpha() {
		return ALPHA;
	}

	public double getLambda() {
		return LAMBDA;
	}

	public double getAlphaChangeRatio() {
		return m_AlphaChangeRatio;
	}

	//
	// Debug only: 
	//
	
	private void print(int[] is) throws IOException {
		PrintWriter randNtuple = new PrintWriter(new FileWriter("randNtuple",true));
		randNtuple.println("" +Arrays.toString(is));
		randNtuple.close();
	}

	//samine// print "tableN" and "tableA"
	public void printTables(){
		 nTuples[0][0].printTable();
	}
	
	public void printLutHashSum(PrintStream pstream) {
		for (int p=0; p<nTuples.length; p++) {
			pstream.print("LUT hash sum player "+p+": ");
			for (int j = 0; j < nTuples[p].length; j++)
				pstream.print(" " + (nTuples[p][j].lutHashSum()) + "|");
			pstream.println("");
		}		
	}
	
	public void printLutSum(PrintStream pstream) {
		for (int p=0; p<nTuples.length; p++) {
			pstream.print("LUT sum player "+p+": ");
			for (int j = 0; j < nTuples[p].length; j++)
				pstream.print(frmS.format(nTuples[p][j].lutSum())+"|");
			pstream.println("");
		}
		for (int p=0; p<nTuples.length; p++) {
			pstream.print("LUT ABS player "+p+": ");
			for (int j = 0; j < nTuples[p].length; j++)
				pstream.print(frmS.format(nTuples[p][j].lutSumAbs())+"|");
			pstream.println("");
		}
		
	}
}
