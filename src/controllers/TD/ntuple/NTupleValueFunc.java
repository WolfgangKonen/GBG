package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

import games.StateObservation;
import games.XNTupleFuncs;
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
	
	public XNTupleFuncs xnf=null; 

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
	public NTupleValueFunc(int nTuples[][], XNTupleFuncs xnf, int posVals, boolean useSymmetry,
			boolean randInitWeights, NTParams tcPar, int numCells) 
					throws RuntimeException {
		this.useSymmetry = useSymmetry;
		this.xnf = xnf;
		
		if (nTuples!=null) {
			this.numTuples = nTuples.length;
			initNTuples(nTuples, posVals, randInitWeights, tcPar, numCells);
		}
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

		// Get equivalent boards (including self)
		equiv = getSymBoards2(board, useSymmetry);
		//equiv = getSymBoards2(board, false);    // DON'T, at least for TTT clearly inferior

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++)
				score += nTuples[player][i].getScore(equiv[j]);
		}
		//if (useSymmetry) score /= equiv.length; // DON'T, at least for TTT clearly inferior

		return Math.tanh(score);
	}

	/**
	 * Get the equivalent positions to one board. The first one 
	 * is the board itself. The other can be generated
	 * with mirroring and rotation (depending on the game, see 
	 * {@code xnf.symmetryVectors(board)}).
	 * 
	 * @param board
	 * @param useSymmetry if false, return a 2D array with only one row 
	 * 			(the board itself in int[0][])
	 * @return the equivalent positions
	 */
	private int[][] getSymBoards2(int[] board, boolean useSymmetry) {
		int i;
		int[][] equiv = null;
		if (useSymmetry) {
			equiv = xnf.symmetryVectors(board);

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

		// Get equivalent boards (including self)
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

		// Get equivalent boards (including self)
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
