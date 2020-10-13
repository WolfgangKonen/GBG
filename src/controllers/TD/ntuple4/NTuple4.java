package controllers.TD.ntuple4;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import controllers.TD.ntuple2.NTuple2;
import games.BoardVector;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleFuncs;
import params.ParNT;

/**
 * Realization of a single n-tuple for games.
 * Each {@link NTuple4} consists of L ={@link NTuple4#getLength()} positions P[i] ={@link #getPosition(int)} on the game
 * board. Positions are in range 0, ..., B-1 where B is the length of {@link BoardVector}.
 * <p>
 * Position i = 0, ..., L-1 can carry one out of {@code posVals[P[i]]} position values. This is the
 * main difference to {@link NTuple2}, where each position has the same number of position values. <br>
 * [This results in less memory consumption for the game RubiksCube.]
 * <p>
 * Each {@link NTuple4} has a lookup table (LUT) containing for each combination of
 * values a corresponding weight. The length of the LUT is {@code posVals[P[0]]} * ... *  {@code posVals[P[L-1]]}.
 * <p>
 * Game states are transformed to objects of class {@link BoardVector} with
 * {@link XNTupleFuncs#getBoardVector(StateObservation)} before they are passed to 
 * {@link NTuple4} methods.
 * <p>
 *     Example 2x2x2 RubiksCube, STICKER2 representation: There are 7 stickers with 3 faces each. The board vector
 *     is of length 14, where the first 7 cells have 7 possible values (the stickers) and the second 7 cells 3.
 *     If a 4-tuple covers the positions
 *     <pre>
 *       P[i] = [0,8,2,10], </pre>
 *     we have <pre>
 *       posVals[P[i]] = [7,3,7,3]
 *       --> lut.length = 7*3*7*3 = 441  </pre>
 *     and any board vector <pre>
 *       [6 * 6 * * * *
 *        * 2 * 2 * * *] </pre>
 *     activates the highest LUT entry.
 * </p>
 * 
 * @author Wolfgang Konen, TH Koeln, 2020
 *
 * @see NTuple2
 */
public class NTuple4 implements Serializable {

	private final double INIT; // samine// N and A will be initialized with the same number(INIT)

	private boolean TC = false; 		// TC constant is implemented
										// to turn on and off temporal
										// coherence implementation
	private boolean tcImm = true;  		// true: immediate TC update, false: batch update (epochs).
										// Should be always true: calculate tcFactorArray
										// immediately from N and A (recommended case in [Bagh14])
	private boolean tcAccRW = true;		// If true, accumulate in N and A the recommended weight
										// changes \delta*elig (case TCL[r] in [Bagh14]).
										// If false, accumulate in N and A the error signal
										// \delta (case TCL[\delta] in [Bagh14]).
	private boolean tcEXP=true;		// If true, use exponential transfer function g(N/A)
									// If false, use N/A directly for tcFactor
	private double tcBeta=2.7;		// parameter for tcEXP (TCL-EXP in [Bagh14])

	private final double EPS = 0.5; /* random weights init scale */
	Random rand;

	private final int[] nTuple;
	private final int[] posVals; // posVals[i] = # of possible values for cell i of BoardVector
	private final double[] lut;
	private transient double[] tcN = null;
	private transient double[] tcA = null;
	private transient double[] tcFactorArray = null;

	// indexList is needed in update(): if a certain index of the LUT is
	// invoked more than once during a weight update for state s_k (multiple calls to updateNew(), 
	// if there are equivalent states (symmetric to s_k)), then it is updated only *once*. This 
	// is realized by remembering the already visited indices in indexList.
	// It ensures that an update with ALPHA=1.0 changes the LUT in such a way that a subsequent
	// call getScoreI() returns a value identical to the target of that update.
	private transient LinkedList indexList = new LinkedList();

	// /WK/
	private transient double[] dWArray=null;		// recommended weight changes
	private transient double[] dWOld=null;			// previous recommended weight changes
	private transient int[] countP=null;			// # of weight changes with same direction as previous
	private transient int[] countM=null;			// # of weight changes with opposite direction
	private final boolean DW_DBG=false;		// /WK/ accumulate countP, countM with help of dWOld

	private final double muDampen=0.99;		// dampening for N and A, so that tcFactor slowly returns to 1
											// --- TODO, not yet implemented ---

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	/**
	 * Each NTuple consists of L = {@link NTuple4#getLength()} positions P[i] ={@link #getPosition(int)}
	 * on the game board.<br>
	 * Position i = 0, ..., L-1 can carry one out of {@code posVals[P[i]]} position values.
	 * For example in TicTacToe, these values are 0 ("O"), 1 (empty), 2 ("X") for every cell.
	 * 
	 * @param nTuple
	 *            the position vector, carrying numbers from 0 to B-1 (B: number of cells
	 *            in {@link games.BoardVector})
	 * @param posVals
	 *            posVals[b] is the number of possible values at position b = 0, ..., B-1
	 */
	NTuple4(int[] nTuple, int[] posVals, ParNT ntPar) {

		// samine//
		INIT = ntPar.getTcInit();// samine//
		TC = ntPar.getTc();
		tcImm = ntPar.getTcImm();
		tcAccRW = (ntPar.getTcAccumulMode()==1);
		tcEXP = (ntPar.getTcTransferMode()==1);
		tcBeta = ntPar.getTcBeta();
		rand = new Random();
		this.nTuple = nTuple.clone();
		this.posVals = posVals.clone();
		int L=1;
		for (int i=0; i<nTuple.length; i++) L *= posVals[nTuple[i]];
		lut = new double[L];
		if (TC) {
			tcN = new double[lut.length]; // matrix N in TC
			tcA = new double[lut.length]; // matrix A in TC
			tcFactorArray = new double[lut.length]; // tcFactor=|N|/A
			//tcDampArray = new double[lut.length]; // /WK/ for NEW_WK
			//dWArray = new double[lut.length];	// for accumulating TC (tcImm==false)
			
			// initializing N and A matrices and tcFactor=|N|/A
			for (int i = 0; i < lut.length; i++) {
				tcN[i] = INIT;
				tcA[i] = INIT;
				tcFactorArray[i] = 1.0;
				//tcDampArray[i] = 1.0;			
			}
		}
		
		if (DW_DBG) {
			dWOld = new double[lut.length];
			countP = new int[lut.length];
			countM = new int[lut.length];
		}

//		if (useIndexList==false)
//			trainCounter = new int[lut.length];
	}

	public boolean instantiateAfterLoading() {
		indexList = new LinkedList();
		if (TC) {
			tcN = new double[lut.length]; // matrix N in TC
			tcA = new double[lut.length]; // matrix A in TC
			tcFactorArray = new double[lut.length]; // tcFactor=|N|/A
			//tcDampArray = new double[lut.length]; // /WK/ for NEW_WK
			//dWArray = new double[lut.length];	// for accumulating TC (tcImm==false)
			
			// initializing N and A matrices and tcFactor=|N|/A
			for (int i = 0; i < lut.length; i++) {
				tcN[i] = INIT;
				tcA[i] = INIT;
				tcFactorArray[i] = 1.0;
				//tcDampArray[i] = 1.0;			
			}
		}
		return true;
	}
	
	/**
	 * Get the LUT index for a certain board vector (game board).
	 * 
	 * @param board
	 *            the representation of a game board (in case of TTT: vector of length 9,
	 *            carrying 0 ("O"), 1 (empty) or 2 ("X") in each element)
	 * @return the corresponding index into the LUT
	 */
	private int getIndex(int[] board) {
		int index = 0;
		int Q=1; 		// Q = posVals[P[0]]*...*posVals[P[i]] in i-loop below
		for (int i = 0; i < nTuple.length; i++) {
			assert (board[nTuple[i]]<posVals[nTuple[i]]) : "Assert failed for cell "+nTuple[i]+": "+board[nTuple[i]]+">="+posVals[nTuple[i]];
			index += Q * board[nTuple[i]];
			Q = Q * posVals[nTuple[i]];
		}
		return index;
	}

	/**
	 * Initialize the weights
	 * 
	 * @param random
	 *            If {@code true}, the weights are randomly drawn from the
	 *            interval [-EPS,EPS], where EPS=0.5 is set in source code.<br>
	 *            If {@code false}, all weights are set to 0.0.
	 */
	public void initWeights(boolean random) {
		for (int i = 0; i < lut.length; i++)
			lut[i] = (random ? EPS * (rand.nextDouble() * 2 - 1) : 0.0);
	}

    /**
	 * Get the score of this NTuple for one specific board (not using
	 * symmetries)
	 * 
	 * @param board
	 *            the representation of a game board (vector of length 9,
	 *            carrying 0 ("O"), 1 (empty) or 2 ("X"))
	 * @return the LUT weight for this game board
	 * 
	 * @see NTuple4ValueFunc#getScoreI(StateObsWithBoardVector,int)
	 */
	public double getScore(int[] board) {
		int Index = getIndex(board);
		double score = lut[Index];
		
//		final double MAXSCORE = 3932156; 
//		System.out.println(Index + " ["+score*MAXSCORE+"]");  //debug
		
		return score;
	}

	/**
	 * Update the weights of this NTuple for one specific board (not using symmetries). 
	 * <p>
	 * **New** function according to [Jaskowski16]. It uses the LAMBDA-horizon mechanism
	 * and it maintains for each n-tuple and each update step an index list such that 
	 * re-occurring LUT-indices are changed only once.
	 * 
	 * @param board
	 *            the representation of a game board (in case of TTT: vector of length 9,
	 *            carrying -1 ("O"), 0 (empty) or +1 ("X"))
	 * @param alphaM the step size ALPHA (divided by numTuples*numEquiv)
	 * @param delta  target minus V(s_t)
	 * @param e		 derivative of sigmoid (1 if no sigmoid) * LAMBDA^(t-k)
	 * 
	 * @see NTuple4ValueFunc#update(StateObsWithBoardVector, int, int, double, double, boolean, boolean)
	 */
	public void updateNew(int[] board, double alphaM, double delta, double e /*, double LAMBDA*/) {
		int index = getIndex(board);
		Integer indexI = new Integer(index);

		double tcFactor = getTcFactor(index);	// returns 1 if (!TC)
				
		double rW = delta * e;					// recommended weight change
		double dW = alphaM * rW * tcFactor;
		
		if (TC) { 			// update tcFactorArray *after* dW (do nothing if (!TC))
			if (tcAccRW) {
				setTcFactor(index,rW);			// accumulate recommended weight change	
			} else {
				setTcFactor(index,delta);		// accumulate error signal
			}			
		}

//		if (useIndexList) {		// useIndexList==true is the recommended choice
			if (!TC || (TC && tcImm)) {
				if (!indexList.contains(indexI)) 
					lut[index] += dW;				
			}		
			indexList.add(indexI);
//		} 

//		if (TC)
//			dWArray[Index] += dW;		// /WK/
	}

	/**
	 * If TC, update the accumumlators tcN and tcA with accum, then set tcFactorArray[index]
	 * according to the TC transfer type (for next weight update)
	 * 
	 * @param index
	 * @param accum
	 * @return
	 */
	private double setTcFactor(int index, double accum) {
		if (TC) {
			tcN[index] += accum;
			tcA[index] += Math.abs(accum);

			if (tcImm) {
				double arg = (double) Math.abs(tcN[index]) / tcA[index];
				if (tcEXP) {
					arg = Math.exp(tcBeta*(arg-1));
				}
				tcFactorArray[index] = arg;
			} 
			return tcFactorArray[index];
		} else {
			return 1;
		}
	}
	
	// currently not used
	@Deprecated
	public void weightDecay(double factor) {
		for (int k=0; k<lut.length; k++)
			lut[k] *= factor;
	}
	
	/**
	 * @param k
	 *            index into LUT
	 * @return the kth weight for this NTuple
	 */
	public double getWeight(int k) {
		assert (k >= 0 && k < lut.length) : " k is not a valid LUT index";
		return lut[k];
	}

	public double[] getWeights() {
		return lut;
	}
	
	public double[] getTcFactorArray() {
		return tcFactorArray;
	}
	
	public double getTcFactor(int Index) {
		return (TC) ? tcFactorArray[Index] : 1.0;
	}
	
	public boolean getTc() {
		return TC;
	}

	public boolean getTcImm() {
		return tcImm;
	}

	public void clearIndices() {
		indexList.clear();
	}

	public int getCountP(int k) {
		if (!DW_DBG) return 0;
		assert (k >= 0 && k < lut.length) : " k is not a valid LUT index";
		return countP[k];
	}

	public int getCountM(int k) {
		if (!DW_DBG) return 0;
		assert (k >= 0 && k < lut.length) : " k is not a valid LUT index";
		return countM[k];
	}

	public int getPosition(int i) {
		assert (i >= 0 && i < nTuple.length) : " i is not a valid N-tuple position";
		return nTuple[i];
	}

	/**
	 * @return the number of positions for this NTuple
	 */
	public int getLength() {
		return nTuple.length;
	}

	/**
	 * @return the length of the LUT for this NTuple
	 */
	public int getLutLength() {
		return lut.length;
	}

	public int getPosVals(int i) {
		return posVals[i];
	}

	/**
	 * Is called only in case {@code (TC && !tcImm)}, but {@code !tcImm} is not recommended
	 * 
	 * @see TDNTuple4Agt#trainAgent(StateObservation)
	 */
	@Deprecated
	public void updateTC() {
		if (TC == true) {

			//System.out.println("updateTCfactor");
			//for (int i = 0; i < lut.length; i++)
			//	tcFactorArray[i] = (double) Math.abs(tcN[i]) / tcA[i];

			for (int i = 0; i < lut.length; i++) {
				tcFactorArray[i] = Math.abs(tcN[i]) / tcA[i];
				lut[i] += tcFactorArray[i]* dWArray[i];				// ??correct to update lut here?? TODO
				dWArray[i]=0.0;
			}
		}
	}
	
	//
	// The following functions are needed for debug only: 
	//
	
	//samine// print "tableN" and "tableA", called by {@link NTupleValueFunc#printTables()}
	public void printTable(){
		PrintWriter tableN = null;
		try {
			tableN = new PrintWriter(new FileWriter("tableN",true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter tableA = null;
		try {
			tableA = new PrintWriter(new FileWriter("tableA",true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tableN.println("" +Arrays.toString(tcN));
	    tableN.close();

		tableA.println("" +Arrays.toString(tcA));
		tableA.close();
		
	}
	
	static public void print(int[][] equiv) {
		for (int i1=0; i1<equiv.length; i1++) {
			System.out.println(stringRep(equiv[i1])); 
		}
		System.out.println("***end***");			
	}
	
	// only valid for TicTacToe!
	static public String stringRep(int[] board) {
		String[] a = {"O","-","X"};
		String s = "";
			for (int j=0; j<board.length; j++) {
				if (j%3==0 && j>0) s = s + ("|");
				s = s + (a[board[j]+1]);
			}
		return s;
	}

	public int lutHashSum() {
		int hs=0; 
		for (int i=0; i<lut.length; i++) if (lut[i]>0) hs += i;
		return (hs%100);
	}
	public double lutSum() {
		double ls=0; 
		for (int i=0; i<lut.length; i++) ls += lut[i];
		return ls;
	}
	public double lutSumAbs() {
		double ls=0; 
		for (int i=0; i<lut.length; i++) ls += Math.abs(lut[i]);
		return ls;
	}
}
