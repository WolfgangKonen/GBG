package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

import controllers.TD.ntuple2.TDNTuple2Agt;
import games.StateObservation;
import games.XNTupleFuncs;
import params.NTParams;
import params.TDParams;

/**
 *         Implementation of a learning value-function using n-tuple systems.
 *         A set of n-tuples is generated randomly or by user precept. Random
 *         n-tuples can be just a set of random points or a random walk on the
 *         board. The value-function uses symmetries of the board to allow a
 *         faster training. The output of the value-function is always put
 *         through a sigmoid function (tanh) to get the value in the range -1 ..
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
    protected boolean withSigmoid=true;

	// Number of n-tuples
	private int numTuples = 0;

	private int numPlayers; 
	
	// The generated n-tuples
	private NTuple nTuples[][];
	
	public XNTupleFuncs xnf=null; 

	// Turns usage of symmetry on or off
	private boolean useSymmetry = false;

	private boolean PRINTNTUPLES = false;	// /WK/ control the file printout of n-tuples
	private DecimalFormat frmS = new DecimalFormat("+0.00000;-0.00000");

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	/**
	 * Constructor using a set of n-tuples that are predefined.
	 * 
	 * @param nTuplesI
	 *            The set of n-tuples as an {@code int} array. For each {@code nTuplesI[i]}
	 *            the constructor will construct k {@link NTuple} objects of the same form,
	 *            one for each player ({@code k=xnf.getNumPlayers()}). Allowed values 
	 *            for the sampling points of an n-tuple: 0,..,numCells.
	 * @param xnf
	 * @param posVals
	 *            Possible values/field of the board (TicTacToe: 3)
	 * @param useSymmetry
	 *            true, if symmetries shall be used
	 * @param randInitWeights
	 *            true, if all weights of all n-Tuples shall be initialized
	 *            randomly
	 * @param tcPar
	 * @param numCells
	 * @throws RuntimeException
	 */
	public NTupleValueFunc(int nTuplesI[][], XNTupleFuncs xnf, int posVals, boolean useSymmetry,
			boolean randInitWeights, NTParams tcPar, int numCells) 
					throws RuntimeException {
		this.useSymmetry = useSymmetry;
		this.xnf = xnf;
		this.numPlayers = xnf.getNumPlayers();
		
		if (nTuplesI!=null) {
			this.numTuples = nTuplesI.length;
			initNTuples(nTuplesI, posVals, randInitWeights, tcPar, numCells);
		} else {
			throw new RuntimeException("Error: nTuplesI not initialized");
		}
	}

	void initNTuples(int[][] nTuplesI, int posVals, boolean randInitWeights,
			NTParams ntPar, int numCells) {
		this.nTuples = new NTuple[numPlayers][numTuples];
		for (int i = 0; i < numTuples; i++) {
			for (int j=0; j<nTuplesI[i].length; j++) {
				int v = nTuplesI[i][j];
				if (v<0 || v>=numCells) 
					throw new RuntimeException("Invalid cell number "+v+" in n-tuple no. "+i);
			}
			for (int k=0; k<numPlayers; k++) {
				this.nTuples[k][i] = new NTuple(nTuplesI[i], posVals, ntPar);
				if (randInitWeights) {
					this.nTuples[k][i].initWeights(true);
				}				
			}
		}
	}

	/**
	 * @return The list of n-Tuples
	 */
	public NTuple[] getNTuples() {
		NTuple list[] = new NTuple[numTuples * numPlayers];
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

	public void calcScoresAndElig(int[] curTable, int curPlayer) {
    	double v_old = getScoreI(curTable,curPlayer);	
		// derivative of tanh ( if withSigmoid==true)
		double e = (withSigmoid ? (1.0 - v_old * v_old) : 1.0);
        //updateElig(curTable,curPlayer,e);	 // terribly SLOW for games like 2048 if LAMBDA==0
		if (LAMBDA!=0.0) 
			updateElig(curTable,curPlayer,e);	

	}

	public void finishUpdateWeights() {
		ALPHA = ALPHA * m_AlphaChangeRatio;
	}

	// this is only needed for TTT
	public int countPieces(int board[]) {
		int count = 0;
		for (int i = 0; i < board.length; i++)
			if (board[i] != 1)		// remember: board[i]==1 for empty cells (!)
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
	 * Get the value for this state in int[]-representation
	 * 
	 * @param board 
	 * 			  the state as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board}
	 * @return
	 */
	public double getScoreI(int[] board, int player) {
		int i, j;
		double score = 0.0;
		int[][] equiv = null;

		// assertion *only* for TTT:
		//assert(player == countPieces(board) % 2);

		// Get equivalent boards (including self)
		equiv = getSymBoards2(board, useSymmetry);
		//equiv = getSymBoards2(board, false);    // DON'T, at least for TTT clearly inferior

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++)
				score += nTuples[player][i].getScore(equiv[j]);
		}
		//if (useSymmetry) score /= equiv.length; // DON'T, at least for TTT clearly inferior
		if (TDNTupleAgt.NEWTARGET) score /= equiv.length; 

		return (withSigmoid ? Math.tanh(score) : score);
	}

	/**
	 * Get the equivalent positions to one board. The first one 
	 * is the board itself. The other can be generated
	 * with mirroring and rotation (depending on the game, see 
	 * {@code xnf.symmetryVectors(board)}).
	 * 
	 * @param board
	 *            board as 1D-integer vector (position value for each board cell) 
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
	 *            the current board
	 * @param curPlayer
	 *            the player who has to move on current board
	 * @param nextBoard
	 *            the following board
	 * @param nextPlayer
	 *            the player who has to move on next board
	 * @param finished
	 *            true, if game is over
	 * @param reward
	 *            reward given for a terminated game (-1,0,+1)
	 */
	public void updateWeights(int[] curBoard, int curPlayer, int[] nextBoard, int nextPlayer,
			boolean finished, double reward, boolean upTC) {
		double v_old = getScoreI(curBoard,curPlayer); // Old Value
		double tg; // Target-Signal
		// tg contains reward OR GAMMA * value of the after-state
		tg = (finished ? reward : GAMMA * getScoreI(nextBoard,nextPlayer));
		// delta is the error signal
		double delta = (tg - v_old);
		// derivative of tanh ( if withSigmoid==true)
		double e = (withSigmoid ? (1.0 - v_old * v_old) : 1.0);

		double dW = ALPHA * delta * e;

		// update every single N-Tuple LUT
		// temporal coherence// update N and A matrices
		// samine//

		update(curBoard, curPlayer, delta, e);
		//update(curBoard, dW, dW);   // /WK/
		
		if (LAMBDA!=0.0) updateElig(nextBoard, nextPlayer, e);

	}

	/**
	 * Update the weights of the n-Tuple-System. The only difference to {@code updateWeights}
	 * is that the target is different: reward + GAMMA*valueFunction(next), irrespective of 
	 * {@code finished}.
	 * The value function estimated by {@code this} has a different meaning: it 
	 * estimates the sum of future rewards.
	 * 
	 * @param curBoard
	 *            the current board
	 * @param curPlayer
	 *            the player who has to move on current board
	 * @param nextBoard
	 *            the following board
	 * @param nextPlayer
	 *            the player who has to move on next board
	 * @param reward
	 *            reward given for a terminated game (-1,0,+1)
	 */
	public void updateWeightsNew(int[] curBoard, int curPlayer, int[] nextBoard, int nextPlayer,
			double reward, boolean upTC) {
		double v_old = getScoreI(curBoard,curPlayer); // Old Value
		double tg; // Target-Signal
		// tg is 0 for a final state OR (reward + GAMMA * value of the after-state)
		tg = reward + GAMMA * getScoreI(nextBoard,nextPlayer);
		// delta is the error signal
		double delta = (tg - v_old);
		// derivative of tanh ( if withSigmoid==true)
		double e = (withSigmoid ? (1.0 - v_old * v_old) : 1.0);

		double dW = ALPHA * delta * e;

		// update every single N-Tuple LUT
		// temporal coherence// update N and A matrices
		// samine//

		update(curBoard, curPlayer, delta, e);
		//update(curBoard, dW, dW);   // /WK/
		
		if (LAMBDA!=0.0) updateElig(nextBoard, nextPlayer, e);

	}

	public void updateWeightsNewTerminal(int[] curBoard, int curPlayer, boolean upTC) {
		double v_old = getScoreI(curBoard,curPlayer); // Old Value
		double tg = 0.0; // Target signal is 0 (!)
		// delta is the error signal
		double delta = (tg - v_old);
		// derivative of tanh ( if withSigmoid==true)
		double e = (withSigmoid ? (1.0 - v_old * v_old) : 1.0);

		double dW = ALPHA * delta * e;

		update(curBoard, curPlayer, delta, e);

		if (TDNTuple2Agt.DBG2_TARGET) {
			final double MAXSCORE = 1; //3932156;
			double v_new = getScoreI(curBoard,curPlayer);
			System.out.println("getScore(***finalSO):"+v_old*MAXSCORE+", "+v_new*MAXSCORE);
			int dummy=1;
		}
	}

	/**
	 * Update all n-Tuple LUTs. Simply add dW to all relevant weights. Also
	 * update the symmetric boards, if wanted.
	 * 
	 * @param board
	 *            board, for which the weights shall be updated,
	 *            as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board}
	 * @param delta
	 *            ALPHA*delta*e is the value added to all weights (LAMBDA==0)
	 */
	private void update(int[] board, int player, double delta, double e) {
		int i, j;
		int[][] equiv = null;

		// assertion *only* for TTT:
		//assert(player == countPieces(board) % 2);

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
	 *            board, for which the eligibility traces shall be updated, 
	 *            as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board}
	 * @param e
	 *            the derivative of the sigmoid function
	 */
	private void updateElig(int[] board, int player, double e) {
		int i, j;
		int[][] equiv = null;

		// assertion *only* for TTT:
		//assert(player == countPieces(board) % 2);

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
				for (k = 0; k < numPlayers; k++)
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
	public void setUseSymmetry(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}

	public boolean hasSigmoid() {
		return withSigmoid;
	}

	public void setSigmoid(boolean withSigmoid) {
		this.withSigmoid = withSigmoid;
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
	
	public XNTupleFuncs getXnf() {
		return xnf;
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
