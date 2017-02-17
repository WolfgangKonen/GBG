package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import params.NTParams;
import params.TDParams;

/**
 * Realization of a single N-tuple for games.<br>
 * Each NTuple consists of {@link NTuple#getLength()} positions on the game
 * board.<br>
 * Each position can carry one out of {@code posVals} position values. For TTT the 
 * values are 0 ("O"), 1 (empty), 2 ("X").<br>
 * Each NTuple has a lookup table (LUT) containing for each combination of
 * values a corresponding weight. The length of the LUT is {@code posVals} ^
 * {@link NTuple#getLength()}.
 * 
 * @author Markus Thill, Samineh Bagheri, Wolfgang Konen, FH Köln, Sep'11
 * 
 */
public class NTuple implements Serializable {

	private double INIT; // samine// N and A have to be initiliazed with the
							// same number(INIT)

	protected static boolean TC = false; // samine//TC constant is implemented
											// to turn on and off temporal
											// coherence implementation
	protected static boolean TcImm = true; // samine// TcImm=true: tcFactor is
											// calculated after every change
											// samine// TcImm=false: tcFactor is
											// stored in array and will be
											// updated after tcIn game
	 protected static int tcIn = 1; //
	 									// samine// tcIn is the number of
											// games that
	// // tcFactor will be updated after that
	// // samine//(it is used only if TcImm is false)
	private double EPS = 0.5; /* random weights init scale */
	Random rand;

	private int[] nTuple;
	private int posVals; // # of possible values for a field of the board
	private double lut[];
	private int trainCounter[];
//	private  double ev[];
//	private  double tcN[];
//	private  double tcA[];
//	private  double tcFactorArray[];
//	private  double tcDampArray[];
	private transient double ev[];
	private transient double tcN[];
	private transient double tcA[];
	private transient double tcFactorArray[];
	private transient double tcDampArray[];

	// /WK/
//	private  double dWArray[];			// recommended weight changes	
//	private  double dWOld[]=null;		// previous recommended weight changes
//	private  int countP[]=null;			// # of weight changes with same direction as previous
//	private  int countM[]=null;			// # of weight changes with opposite direction
	private transient double dWArray[];			// recommended weight changes	
	private transient double dWOld[]=null;		// previous recommended weight changes
	private transient int countP[]=null;			// # of weight changes with same direction as previous
	private transient int countM[]=null;			// # of weight changes with opposite direction
	private boolean DW_DBG=false;		// /WK/ accumulate countP, countM with help of dWOld
	private boolean NEW_WK=false;		// /WK/ experimental: additional dampening of weights with frequent changes 
	private double BETA=0.9; //0.8;		// dampening factor for NEW_WK

	/**
	 * Each NTuple consists of {@link NTuple#getLength()} positions on the game
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
	NTuple(int[] nTuple, int posVals, NTParams ntPar) {

		// samine//
		INIT = ntPar.getINIT();// samine//
		TC = ntPar.getTC();
		TcImm = ntPar.getTCFType();
		rand = new Random();
		this.nTuple = nTuple;
		lut = new double[(int) Math.pow(posVals, nTuple.length)];
		ev  = new double[lut.length];
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
		if (DW_DBG) {
			dWOld = new double[lut.length];
			countP = new int[lut.length];
			countM = new int[lut.length];
		}

		trainCounter = new int[lut.length];
		this.posVals = posVals;
	}

	/**
	 * Get the LUT index for a certain game board.
	 * 
	 * @param board
	 *            the representation of a game board (vector of length 9,
	 *            carrying 0 ("O"), 1 (empty) or 2 ("X"))
	 * @return the corresponding index into the LUT
	 */
	public int getIndex(int[] board) {
		int index = 0;
		int P=1; 		// P = (posVals)^i in i-loop below
		for (int i = 0; i < nTuple.length; i++) {
			// board+1 ->
			// black: 0
			// empty: 1
			// white: 2
			index += P * ( board[nTuple[i]] +1);
			P = P*posVals;
		}
		return index;
	}

	/**
	 * Get the game board corresponding to this LUT index for this NTuple.
	 * 
	 * @param index
	 *            the index into LUT
	 * @return the corresponding game board (vector of length 9, carrying 0
	 *         ("O"), 1 (empty) or 2 ("X")). As a specialty of this function,
	 *         each board cell which is NOT a NTuple position, gets a "-9"
	 *         (those cells are displayed in light gray by class
	 *         {@code NTupleShow}).
	 * 
	 */
//	  {@see NTupleShow#updatePanel()    // for later, when we integrate NTupleShow
	public int[] getBoard(int index) {
		int i;
		int[] board = new int[9];
		for (i = 0; i < 9; i++)
			board[i] = -9; // -9: is not part of the N-tuple
		for (i = 0; i < nTuple.length; i++) {
			board[nTuple[i]] = index % posVals;
			index = (index - (int) board[nTuple[i]]) / posVals;
		}
		return board;
	}

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

    public void resetElig() {
		for (int i=0;i<ev.length;i++)
			ev[i] = 0.0;
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
	 * @see NTupleValueFunc#getScoreI(int[])
	 */
	public double getScore(int[] board) {
		double score = lut[getIndex(board)];
		return score;
	}

	/**
	 * Update the weights of this NTuple for one specific board (not using
	 * symmetries)
	 * 
	 * @param board
	 *            the representation of a game board (vector of length 9,
	 *            carrying -1 ("O"), 0 (empty) or +1 ("X"))
	 * @param ALPHA
	 * @param delta
	 * @param e
	 * @param LAMBDA
	 * 
	 * @see NTupleValueFunc#updateWeights(int[], int[], boolean, double, boolean) 
	 */
	public void update(int[] board, double ALPHA, double delta, double e, double LAMBDA) {
		// lut[getIndex(board)] += dW;
		// samine//
		int Index = getIndex(board);

		double tcFactor = 1;

		if (TC == true) {
			if (TcImm == true) {
				tcFactor = (double) Math.abs(tcN[Index]) / tcA[Index];
			} else {
				tcFactor = tcFactorArray[Index];
			}
		}
		double dW = ALPHA*delta* e * tcFactor;

		tcN[Index] += delta;
		tcA[Index] += Math.abs(delta);
		if (!TC || TcImm) {
			if (LAMBDA==0.0) {
				// the old and fast version, but without eligibility traces
				lut[Index] += dW;				
			} else {
				// elig traces active, we have to do it the long way (as long as we do not 
				// keep track of all elig traces > 0, *TODO*)
				double alphaDelta=ALPHA*delta * tcFactor;		// the e-part is now in ev[i]
				for (int i=0; i<lut.length; i++)
					//if (ev[i]!=0.0) 				// DON'T, this extra 'if' slows down!
						lut[i] += alphaDelta*ev[i];
				
				// only debug
//				double a1=dW*tcFactor;
//				double a2=alphaDelta*ev[Index];
//				double evIndex=ev[Index];
			}			
		}
		dWArray[Index] += dW;		// /WK/

		trainCounter[Index]++;   	// /WK/
		
		if (DW_DBG) {
			if (NEW_WK) tcDampArray[Index] *= BETA;
			if (dWOld[Index]*dW<0)  {countM[Index]++; }
			if (dWOld[Index]*dW>0)  {countP[Index]++; }
			dWOld[Index] = dW;
		}

	}

	/**
     * Update eligibility traces {@code ev} 
     * for next pass through loop. Called only in case {@code LAMBDA!=0}.     
	 * 
	 * @param board	board, for which the eligibility traces shall be updated
	 * @param LAMBDA
	 * @param GAMMA
	 * @param e		the derivative of the sigmoid function 
	 */
    public void updateElig(int[] board, double LAMBDA, double GAMMA, double e) {
    	int i,k;
		int Index = getIndex(board);

		for (i=0;i<ev.length;i++) {
			//if (ev[i]!=0.0) 			// DON'T, this extra 'if' slows down!	
				ev[i]=LAMBDA*GAMMA*ev[i];
				// decay all elig traces (could be done more efficiently if an   
		}		// index of all ev[i]>0 were kept)
		
		ev[Index] += e;
		
    }/* end updateElig(int[],...) */

    // update w/o decay (!)
    @Deprecated
    public void updateElig(int[][] equiv, double LAMBDA, double GAMMA, double e) {
 		for (int j = 0; j < equiv.length; j++) {
			int Index = getIndex(equiv[j]);
			ev[Index] += e;
		}
    }/* end updateElig(int[][],...) */

//    /**
//	 * Update the train counters for this NTuple. Each LUT weight has a train
//	 * counter which is incremented by 1 whenever this weight is updated (i. e.
//	 * a TDLearn-step occurs with the Input for this weight being 1).
//	 * 
//	 * @param Input
//	 *            long vector for all N-tuples containing a "1.0" if the
//	 *            corresponding LUT weight gets trained in the TDLearn-step.
//	 * @param k
//	 *            offset within the Input vector for this NTuple (i.e. the
//	 *            inputs for this NTuple start at {@code Input[k]}.
//	 * 
//	 * @see NTupleSet#updateTrainCounter(double[])
//	 */
//	public void updateTrainCounter(double[] Input, int k) {
//		if (Input.length - k < lut.length)
//			throw new RuntimeException("Vector Input too short for current LUT");
//		for (int i = 0; i < lut.length; i++) {
//			if (Input[k + i] == 1.0)
//				trainCounter[i]++;
//		}
//	}

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

	/**
	 * @param k
	 *            index into LUT
	 * @return the kth train counter for this NTuple. Each LUT weight has a
	 *         train counter which is incremented by 1 whenever this weight is
	 *         updated (i. e. a TDLearn-step occurs with the Input for this
	 *         weight being 1).
	 */
	public int getTrainCounter(int k) {
		assert (k >= 0 && k < lut.length) : " k is not a valid LUT index";
		return trainCounter[k];
	}

	public int[] getTrainCounters() {
		return trainCounter;
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
	// Debug only: 
	//
	
	//samine// print "tableN" and "tableA", called by {@link NTupleValueFunc#printTables()}
	public void printTable(){
		PrintWriter tableN = null;
		try {
			tableN = new PrintWriter(new FileWriter("tableN",true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter tableA = null;
		try {
			tableA = new PrintWriter(new FileWriter("tableA",true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
