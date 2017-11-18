package controllers.TD.ntuple2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import games.StateObservation;
import games.XNTupleFuncs;
import params.NTParams;
import params.TDParams;

/**
 * Realization of a single N-tuple for games.
 * Each {@link NTuple2} consists of {@link NTuple2#getLength()} positions on the game
 * board.<br>
 * Each position can carry one out of {@code posVals} position values. For TicTacToe the 
 * values are 0 ("O"), 1 (empty), 2 ("X").<p>
 * 
 * Each {@link NTuple2} has a lookup table (LUT) containing for each combination of
 * values a corresponding weight. The length of the LUT is {@code posVals} ^
 * {@link NTuple2#getLength()}.<p>
 * 
 * Game states are transformed to {@code int[]} board vectors with 
 * {@link XNTupleFuncs#getBoardVector(StateObservation)} before they are passed to 
 * {@link NTuple2} methods. 
 * 
 * @author Markus Thill, Samineh Bagheri, Wolfgang Konen, FH Köln, Sep'11
 * 
 */
public class NTuple2 implements Serializable {

	private double INIT; // samine// N and A will be initialized with the same number(INIT)

	protected static boolean TC = false; // samine//TC constant is implemented
											// to turn on and off temporal
											// coherence implementation
	protected static boolean TcImm = true; // samine// TcImm=true: tcFactor is
											// calculated after every change
											// samine// TcImm=false: tcFactor is
											// stored in array and will be
											// updated after tcIn game
	protected static int tcIn = 1; 		//
	 									// samine// tcIn is the number of
										// games that
										// tcFactor will be updated after that
										// (it is used only if TcImm is false)
	private double EPS = 0.5; /* random weights init scale */
	Random rand;

	private int[] nTuple;
	private int posVals; // # of possible values for a field of the board
	private double lut[];
	private transient double tcN[] = null;
	private transient double tcA[] = null;
	private transient double tcFactorArray[] = null;
	private transient double tcDampArray[] = null;
	
	// the following elements are needed in update(): if a certain index of the LUT is 
	// invoked more than once during a weight update for state s_k (multiple calls to updateNew(), 
	// if there are equivalent states (symmetric to s_k)), then it is updated only *once*. This 
	// is realized by remembering the already visited indices in indexList.
	// This ensures that an update with ALPHA=1.0 changes the LUT in such a way that a subsequent
	// call getScoreI() returns a value identical to the target of that update.
	private transient LinkedList indexList = new LinkedList();
//	private transient int trainCounter[] = null;
//	private boolean useIndexList = true;	// true: use indexList in updateNew()
//											// false: use trainCounter in updateNew()
//											// (useIndexList is now always true, since it is faster by a 
//											// factor of 7 and it avoids the memory for trainCounter)

	// /WK/
	private transient double dWArray[]=null;		// recommended weight changes	
	private transient double dWOld[]=null;			// previous recommended weight changes
	private transient int countP[]=null;			// # of weight changes with same direction as previous
	private transient int countM[]=null;			// # of weight changes with opposite direction
	private boolean DW_DBG=false;		// /WK/ accumulate countP, countM with help of dWOld
	private boolean NEW_WK=false;		// /WK/ experimental: additional dampening of weights with frequent changes 
	private double BETA=0.9; //0.8;		// dampening factor for NEW_WK

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	/**
	 * Each NTuple consists of {@link NTuple2#getLength()} positions on the game
	 * board.<br>
	 * Each position can carry one out of {@code posVals} values. Here the
	 * values are 0 ("O"), 1 (empty), 2 ("X").
	 * 
	 * @param nTuple
	 *            the position vector, carrying numbers from 0 to 8 (cells of
	 *            TicTacToe board)
	 * @param posVals
	 *            number of possible values at each position
	 */
	NTuple2(int[] nTuple, int posVals, NTParams ntPar) {

		// samine//
		INIT = ntPar.getINIT();// samine//
		TC = ntPar.getTc();
		TcImm = ntPar.getTcImm();
		rand = new Random();
		this.nTuple = nTuple.clone();
		this.posVals = posVals;
		lut = new double[(int) Math.pow(posVals, nTuple.length)];
		
		if (TC) {
			// samine//
			tcN = new double[lut.length]; // matrix N in TC
			tcA = new double[lut.length]; // matrix A in TC
			tcFactorArray = new double[lut.length]; // tcFactor=|N|/A
			tcDampArray = new double[lut.length]; // /WK/ for NEW_WK
			dWArray = new double[lut.length];	// for accumulating TC (tcImm==false)
			
			// initializing N and A matrices and tcFactor=|N|/A
			for (int i = 0; i < lut.length; i++) {
				tcN[i] = INIT;
				tcA[i] = INIT;
				tcFactorArray[i] = 1.0;
				tcDampArray[i] = 1.0;			
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

	/**
	 * Get the LUT index for a certain game board.
	 * 
	 * @param board
	 *            the representation of a game board (in case of TTT: vector of length 9,
	 *            carrying 0 ("O"), 1 (empty) or 2 ("X") in each element)
	 * @return the corresponding index into the LUT
	 */
	public int getIndex(int[] board) {
		int index = 0;
		int P=1; 		// P = (posVals)^i in i-loop below
		for (int i = 0; i < nTuple.length; i++) {
			index += P * ( board[nTuple[i]]);
			P = P*posVals;
		}
		return index;
	}

//	/**
//	 * Get the game board corresponding to this LUT index for this NTuple.<br>
//	 * (--- currently not used inside GBG [still specific to TTT] ---)
//	 * 
//	 * @param index
//	 *            the index into LUT
//	 * @return the corresponding game board (vector of length 9, carrying 0
//	 *         ("O"), 1 (empty) or 2 ("X")). As a specialty of this function,
//	 *         each board cell which is NOT a NTuple position, gets a "-9"
//	 *         (those cells are displayed in light gray by class
//	 *         {@code NTupleShow}).
//	 * 
//	 */
////	  {@see NTupleShow#updatePanel()    // for later, when we integrate NTupleShow
//	@Deprecated
//	public int[] getBoard(int index) {
//		int i;
//		int[] board = new int[9];
//		for (i = 0; i < 9; i++)
//			board[i] = -9; // -9: is not part of the N-tuple
//		for (i = 0; i < nTuple.length; i++) {
//			board[nTuple[i]] = index % posVals;
//			index = (index - (int) board[nTuple[i]]) / posVals;
//		}
//		return board;
//	}

//	/**
//	 * Given a long weight vector (all N-tuples), set from this weight vector
//	 * the weights of this NTuple.
//	 * 
//	 * @param wv
//	 *            long weight vector (usually the weights from
//	 *            {@link RLGame.TD_Lin})
//	 * @param off
//	 *            offset, i.e. the weights for this NTuple start at
//	 *            {@code wv[off]}
//	 * 
//	 * @see NTupleSet#copyWeights(double[])
//	 */
//	public void setWeights(double[] wv, int off) {
//		if ((wv.length - off) < lut.length)
//			throw new RuntimeException("Length mismatch: wv.length="
//					+ wv.length + ", offset=" + off + ", lut.length="
//					+ lut.length);
//		for (int i = 0; i < lut.length; i++)
//			lut[i] = wv[i + off];
//	}

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
	 * @see NTuple2ValueFunc#getScoreI(int[],int)
	 */
	public double getScore(int[] board) {
		int Index = getIndex(board);
		double score = lut[Index];
		
//		final double MAXSCORE = 3932156; 
//		System.out.println(Index + " ["+score*MAXSCORE+"]");  //debug
		
		return score;
	}

//	/**
//	 * Update the weights of this NTuple for one specific board (not using
//	 * symmetries)
//	 * 
//	 * @param board
//	 *            the representation of a game board (vector of length 9,
//	 *            carrying -1 ("O"), 0 (empty) or +1 ("X"))
//	 * @param alphaM the step size ALPHA (divided by m=numTuples, if NEWTARGET)
//	 * @param delta  target minus V(s_t)
//	 * @param e		 derivative of sigmoid (1 if no sigmoid)
//	 * @param LAMBDA
//	 * 
//	 * @see NTuple2ValueFunc#update(int[], int, double, double) 
//	 * @see NTuple2ValueFunc#updateWeights(int[], int, int[], int, boolean, double, boolean) 
//	 */
//	public void update(int[] board, double alphaM, double delta, double e, double LAMBDA) {
//		// lut[getIndex(board)] += dW;
//		// samine//
//		int Index = getIndex(board);
//
//		double tcFactor = setTcFactor(Index,delta);
//
//		double dW = alphaM*delta* e * tcFactor;
//
//		if (!TC || TcImm) {
//			if (LAMBDA==0.0) {
//				// the old and fast version, but without eligibility traces
//				lut[Index] += dW;				
//			} else {
//				// elig traces active, we have to do it the long way (as long as we do not 
//				// keep track of all elig traces > 0, *TODO*)
//				double alphaDelta=alphaM*delta * tcFactor;		// the e-part is now in ev[i]
//				for (int i=0; i<lut.length; i++)
//					//if (ev[i]!=0.0) 				// DON'T, this extra 'if' slows down!
//						lut[i] += alphaDelta*ev[i];
//				
//				// only debug
////				double a1=dW*tcFactor;
////				double a2=alphaDelta*ev[Index];
////				double evIndex=ev[Index];
//			}			
//		}
//		dWArray[Index] += dW;		// /WK/
//
//		trainCounter[Index]++;   	// /WK/
//		
//		if (DW_DBG) {
//			if (NEW_WK) tcDampArray[Index] *= BETA;
//			if (dWOld[Index]*dW<0)  {countM[Index]++; }
//			if (dWOld[Index]*dW>0)  {countP[Index]++; }
//			dWOld[Index] = dW;
//		}
//
//	}

	/**
	 * Update the weights of this NTuple for one specific board (not using symmetries). 
	 * <p>
	 * **New** function according to [Jaskowski16]. It uses the LAMBDA-horizon mechanism
	 * and it maintains for each n-tuple and each update step an index list such that 
	 * reoccurring LUT-indices are changed only once.
	 * 
	 * @param board
	 *            the representation of a game board (in case of TTT: vector of length 9,
	 *            carrying -1 ("O"), 0 (empty) or +1 ("X"))
	 * @param alphaM the step size ALPHA (divided by numTuples*numEquiv, if NEWTARGET)
	 * @param delta  target minus V(s_t)
	 * @param e		 derivative of sigmoid (1 if no sigmoid) * LAMBDA^(t-k)
	 * 
	 * @see NTuple2ValueFunc#update(int[], int, double, double) 
	 */
//	 * @param LAMBDA -- obsolete now --
//	 * @see NTuple2ValueFunc#updateWeights(int[], int, int[], int, boolean, double, boolean) 
	public void updateNew(int[] board, double alphaM, double delta, double e /*, double LAMBDA*/) {
		int Index = getIndex(board);
		Integer IndexI = new Integer(Index);

		double tcFactor = setTcFactor(Index,delta);	// returns 1 if TC==false
				
		double dW = alphaM*delta* e * tcFactor;

//		if (useIndexList) {		// useIndexList==true is the recommended choice
			if (!TC || TcImm) {
				if (!indexList.contains(IndexI)) lut[Index] += dW;				
			}		
			indexList.add(new Integer(IndexI));
//		} 
//		else {
//			if (!TC || TcImm) {
//				if (trainCounter[Index]==0) lut[Index] += dW;				
//			}
//			trainCounter[Index]++;   				
//		}

		if (TC)
			dWArray[Index] += dW;		// /WK/
	}

	private double setTcFactor(int Index, double delta) {
		double tcFactor = 		1;

		if (TC == true) {
			tcN[Index] += delta;
			tcA[Index] += Math.abs(delta);

			if (TcImm == true) {
				tcFactor = (double) Math.abs(tcN[Index]) / tcA[Index];
			} else {
				tcFactor = tcFactorArray[Index];
			}
		}
		return tcFactor;
	}
	
	// currently not used
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

	/**
	 * (only called by NTupleShow.updatePanel())
	 * 
	 * @param k
	 *            index into LUT
	 * @return the kth tcFactor (TC) for this NTuple
	 */
	public double getTCFactor(int k) {
		if (!TC) return -1.0;		// no TC learning
		assert (k >= 0 && k < tcFactorArray.length) : " k is not a valid tcFactorArray index";
		return tcFactorArray[k];
		//return (double) Math.abs(tcN[k]) / tcA[k];
		//return tcN[k];
	}

	public double getTC_A(int k) {
		if (!TC) return -1.0;		// no TC learning
		assert (k >= 0 && k < tcFactorArray.length) : " k is not a valid tcFactorArray index";
		return tcA[k];
	}

	public double[] getWeights() {
		return lut;
	}

	public void clearIndices() {
//		if (useIndexList) {
			indexList.clear();			
//		} else {
//			// very slow!!!
//			for (int k=0; k<trainCounter.length; k++)
//				trainCounter[k] = 0;			
//		}
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

	public void updateTC() {
		if (TC == true) {

			//System.out.println("updateTCfactor");
			//for (int i = 0; i < lut.length; i++)
			//	tcFactorArray[i] = (double) Math.abs(tcN[i]) / tcA[i];
// /WK/
			for (int i = 0; i < lut.length; i++) {
				tcFactorArray[i] = (double) Math.abs(tcN[i]) / tcA[i];
				if (NEW_WK) tcFactorArray[i] *= tcDampArray[i]; 
				lut[i] += tcFactorArray[i]* dWArray[i];
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
