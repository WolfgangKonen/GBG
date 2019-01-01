package controllers.TD.ntuple2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.TD.ntuple2.NTupleAgt.EligType;
import games.StateObservation;
import games.XNTupleFuncs;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import params.ParNT;
//import params.NTParams;
//import params.TDParams;
import tools.Types;

/**
 *         Implementation of a learning value-function using n-tuple systems.
 *         A set of n-tuples is generated randomly or by user precept. Random
 *         n-tuples can be just a set of random points or a random walk on the
 *         board. The value-function uses symmetries of the board to allow a
 *         faster training. The output of the value-function is put
 *         through a sigmoid function (tanh) or not depending on the value returned
 *         from {@link #hasSigmoid()}. The learning rate alpha decreases exponentially 
 *         from a start value at the beginning of the training to an end value after a 
 *         certain amount of games.
 * 
 * @see TDNTuple2Agt
 * @see TDNTuple3Agt
 * @see SarsaAgt
 * 
 * @author Markus Thill, Wolfgang Konen (extension TD(lambda)), TH Köln, Feb'17  
 */
public class NTuple2ValueFunc implements Serializable {
	/* Experimental Parameters: */
	// protected double EPS = 0.5; /* random weights init scale */
	// protected double BIAS = 1.0; /* strength of the bias (constant input) */

	/* initial learning rate (typically 1/n) */
	protected double ALPHA = 0.1;

//	/* discount-rate parameter (typically 0.9) */
//	protected double GAMMA = 0.9;			// use now getGamma() - don't store/maintain value twice

//	/* eligibility trace decay parameter (should be <= GAMMA) */
//	protected double LAMBDA = 0.0;			// use now getLambda() - don't store/maintain value twice
	protected double m_AlphaChangeRatio = 0.9998; // 0.998
	protected int epochMax=1;
    protected boolean  rpropLrn=false;
    private transient long numLearnActions = 0L; 		// count the number of calls to update() (learn actions in [Jaskowski16])

//  protected boolean withSigmoid=true; 	// use now hasSigmoid() - don't store/maintain value twice

    // Turns usage of symmetry on or off
//	private boolean useSymmetry = false;	// use now getUSESYMMETRY() - don't store/maintain value twice

	// number of outputs (TD-learning: only 1 output, Q-Learning + Sarsa: multiple outputs)
	private int numOutputs = 1; 
	
	// number of players
	private int numPlayers; 
	
	// number of n-tuples
	private int numTuples = 0;

	private static double[][] dbgScoreArr = new double[100][2];
	
	NTupleAgt tdAgt;		// the 'parent' - used to access the parameters in m_tdPar, m_ntPar
	
	// The generated n-tuples [numOutputs][numPlayers][numTuples]
	private NTuple2 nTuples[][][];
	
	public XNTupleFuncs xnf=null; 
	
	// elements needed for TD(lambda)-update with finite horizon, 
	// see update(int[],int,double,double):
	private int horizon=0;
	private transient LinkedList eList = new LinkedList();		

	private boolean PRINTNTUPLES = false;	// /WK/ control the file printout of n-tuples
	private DecimalFormat frmS = new DecimalFormat("+0.00000;-0.00000");
	
	private static double[] dbg3PArr = new double[3];
	private static double[] dbg3PTer = new double[3];

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	/**
	 * Constructor using a set of n-tuples that are predefined.
	 * 
	 * @param parent
	 * 			The TDNTuple2Agt object where {@code this} is part of. Used to access
	 * 			parameters like withSigmoid, USESYMMETRY and so on.
	 * @param nTuplesI
	 *            The set of n-tuples as an {@code int} array. For each {@code nTuplesI[i]}
	 *            the constructor will construct k {@link NTuple2} objects of the same form,
	 *            one for each player ({@code k=xnf.getNumPlayers()}). Allowed values 
	 *            for the sampling points of an n-tuple: 0,..,numCells.
	 * @param xnf
	 * @param posVals
	 *            Possible values/field of the board (TicTacToe: 3)
	 * @param randInitWeights
	 *            true, if all weights of all n-Tuples shall be initialized
	 *            randomly
	 * @param tcPar
	 * @param numCells
	 * 			  the number of cells on the board (used to check validity of {@code nTuplesI})
	 * @param numOutputs
	 * 			  the number of outputs for the network (1 in TD-learning, numActions in SARSA)
	 * @throws RuntimeException
	 */
	public NTuple2ValueFunc(NTupleAgt parent, int nTuplesI[][], XNTupleFuncs xnf, int posVals,
			boolean randInitWeights, ParNT tcPar, int numCells, int numOutputs) 
					throws RuntimeException {
//		this.useSymmetry = useSymmetry;
		this.xnf = xnf;
		this.numPlayers = xnf.getNumPlayers();
		this.numOutputs = numOutputs;
		this.tdAgt = parent;
		
		if (nTuplesI!=null) {
			this.numTuples = nTuplesI.length;
			initNTuples(nTuplesI, posVals, randInitWeights, tcPar, numCells);
		} else {
			throw new RuntimeException("Error: nTuplesI not initialized");
		}
	}

	void initNTuples(int[][] nTuplesI, int posVals, boolean randInitWeights,
			ParNT ntPar, int numCells) {
		if (numOutputs==0) 
			throw new RuntimeException("initNTuples: numOutputs is 0!");
		this.nTuples = new NTuple2[numOutputs][numPlayers][numTuples];
		for (int i = 0; i < numTuples; i++) {
			for (int j=0; j<nTuplesI[i].length; j++) {
				int v = nTuplesI[i][j];
				if (v<0 || v>=numCells) 
					throw new RuntimeException("Invalid cell number "+v+" in n-tuple no. "+i);
			}
			for (int o=0; o<numOutputs; o++) {
				for (int k=0; k<numPlayers; k++) {
					this.nTuples[o][k][i] = new NTuple2(nTuplesI[i], posVals, ntPar);
					if (randInitWeights) {
						this.nTuples[o][k][i].initWeights(true);
					}				
				}				
			}
		}
	}

	/**
	 * @return The list of n-Tuples
	 */
	public NTuple2[] getNTuples() {
		NTuple2 list[] = new NTuple2[numOutputs * numPlayers * numTuples];
		for (int j = 0, k = 0; j < nTuples[0][0].length; j++)
			for (int i = 0; i < nTuples[0].length; i++)
				for (int o = 0; o < nTuples.length; o++)
					list[k++] = nTuples[o][i][j];
		return list;
	}

	public void finishUpdateWeights() {
		ALPHA = ALPHA * m_AlphaChangeRatio;
	}

	/**
	 * Get the action-value function Q for action {@code act} and state {@code board} in 
	 * int[]-representation. {@code act} determines which output cell {@code o} of the 
	 * network is used. 
	 * 
	 * @param board 
	 * 			  the state as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board} (0,...,N-1)
	 * @param act
	 *            the action to perform on {@code board}
	 * @return
	 */
	public double getQFunc(int[] board, int player, Types.ACTIONS act) {
		int i, j;
		int o = act.toInt();
		double score = 0.0; 
		int[][] equiv = null;
		int[] equivAction;

		// Get equivalent boards (including self)
		equiv = getSymBoards2(board, getUSESYMMETRY());
		equivAction = xnf.symmetryActions(act.toInt());

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++) {
				score += nTuples[equivAction[j]][player][i].getScore(equiv[j]);
			}
		}

		return (hasSigmoid() ? Math.tanh(score) : score);
	}
	
	/**
	 * Get the value for this state in int[]-representation
	 * 
	 * @param board 
	 * 			  the state as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board} (0,...,N-1)
	 * @return
	 */
	public double getScoreI(int[] board, int player) {
		int i, j;
		double score = 0.0; 
		int[][] equiv = null;

		// Get equivalent boards (including self)
		equiv = getSymBoards2(board, getUSESYMMETRY());
		//equiv = getSymBoards2(board, false);    // DON'T, at least for TTT clearly inferior

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++) {
//				System.out.print("g(i,j)=("+i+","+j+"):  ");		//debug
				score += nTuples[0][player][i].getScore(equiv[j]);
			}
		}
		//if (getUSESYMMETRY()) score /= equiv.length; // DON'T, at least for TTT clearly inferior
		//if (TDNTuple2Agt.NEWTARGET) score /= equiv.length; 

		return (hasSigmoid() ? Math.tanh(score) : score);
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
	 * @return the equivalent board vectors
	 */
	private int[][] getSymBoards2(int[] board, boolean useSymmetry) {
		int i;
		int[][] equiv = null;
		if (useSymmetry) {
			equiv = xnf.symmetryVectors(board);

		} else {
			equiv = new int[1][];
			equiv[0] = board.clone();			
		}
		
		return equiv;
	}

	private int[] getSymActions(int output, boolean useSymmetry) {
		int[] equivActions;
		if (useSymmetry) {
			equivActions = xnf.symmetryActions(output);

		} else {
			equivActions = new int[1];
			equivActions[0] = output;			
		}
		
		return equivActions;
	}

//	/**
//	 * Update the weights of the n-Tuple-System.
//	 * 
//	 * @param curBoard
//	 *            the current board
//	 * @param curPlayer
//	 *            the player who has to move on current board
//	 * @param nextBoard
//	 *            the following board
//	 * @param nextPlayer
//	 *            the player who has to move on next board
//	 * @param finished
//	 *            true, if game is over
//	 * @param reward
//	 *            reward given for a terminated game (-1,0,+1)
//	 */
//	public void updateWeights(int[] curBoard, int curPlayer, int[] nextBoard, int nextPlayer,
//			boolean finished, double reward, boolean upTC) {
//		double v_old = getScoreI(curBoard,curPlayer); // Old Value
//		double tg; // Target-Signal
//		// tg contains reward OR GAMMA * value of the after-state
//		tg = (finished ? reward : getGamma() * getScoreI(nextBoard,nextPlayer));
//		// delta is the error signal
//		double delta = (tg - v_old);
//		// derivative of tanh ( if hasSigmoid()==true)
//		double e = (hasSigmoid() ? (1.0 - v_old * v_old) : 1.0);
//
//		update(curBoard, curPlayer, delta, e);
//		
//	}

	/**
	 * Update the weights of the n-Tuple-System. The difference to former {@code updateWeights}
	 * is that the target is now: reward + GAMMA*valueFunction(next), irrespective of the 
	 * former parameter {@code finished}.
	 * The value function estimated by {@code this} has a different meaning: it 
	 * estimates the sum of future rewards.
	 * 
	 * @param curBoard
	 *            the current board
	 * @param curPlayer
	 *            the player whose value function is updated (the p in V(s_t|p) )
	 * @param nextBoard
	 *            the following board (not really needed)
	 * @param nextPlayer
	 *            the player on next board (only needed for debug)
	 * @param reward
	 *            the delta reward given for the transition into nextBoard
	 * @param target
	 *            the target to learn, usually (reward + GAMMA * value of the after-state) for
	 *            non-terminal states. But the target can be as well (r + GAMMA * V) for an
	 *            n-ply look-ahead or (r - GAMMA * V(opponent)).  
	 * @param thisSO
	 * 			  only for debug info: access to the current state's stringDescr()
	 */
	public void updateWeightsNew(int[] curBoard, int curPlayer, int[] nextBoard, int nextPlayer,
			double reward, double target, /*boolean upTC,*/ StateObservation thisSO) {
		double v_old = getScoreI(curBoard,curPlayer); // old value
		// delta is the error signal:
		double delta = (target - v_old);
		// derivative of tanh ( if hasSigmoid()==true):
		double e = (hasSigmoid() ? (1.0 - v_old * v_old) : 1.0);

		update(curBoard, curPlayer, 0, delta, e, false);
		
		if (TDNTuple2Agt.DBG_REWARD || TDNTuple2Agt.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getScoreI(curBoard,curPlayer);
			if (curPlayer==nextPlayer) {
				System.out.println("updateWeightsNew[p="+curPlayer+", "+thisSO.stringDescr()
				+"] v_old,v_new:"+v_old*MAXSCORE+", "+v_new*MAXSCORE+", T="+target*MAXSCORE+", R="+reward);
				dbg3PArr[curPlayer]=v_new*MAXSCORE;
			} else {
				System.out.println("updateWeights_2P[p="+curPlayer+", "+thisSO.stringDescr()
				+"] v_old,v_new:"+v_old*MAXSCORE+", "+v_new*MAXSCORE+", T="+target*MAXSCORE+", R="+reward);				
				dbg3PArr[2]=v_new*MAXSCORE;				
			}
			if (Math.abs(reward)>0.5) {
				int dummy=1;
			}
			if (dbg3PArr[2]!=0.0) {
				if (Math.abs(dbg3PArr[0]-dbg3PArr[2])>1e-6) {
					int dummy=1;
				}
				if (Math.abs(dbg3PArr[0]+dbg3PArr[1])>1e-6) {
					int dummy=1;
				}
				dbg3PArr[2]=0.0;
			}
			int dummy=1;
		}
	}

	/**
	 * Update the weights of the n-tuple system in case of *new* TD-learning ({@link TDNTuple3Agt}). 
	 * 
	 * @param curBoard
	 *            the current board
	 * @param curPlayer
	 *            the player to move on current board
	 * @param vLast
	 *            the value V(s) for s=curBoard and player curPlayer
	 * @param reward
	 *            the delta reward given for the transition into next board
	 * @param target
	 *            the target to learn, usually (reward + GAMMA * value of the after-state) for
	 *            non-terminal states. But the target can be as well (r + GAMMA * V) for an
	 *            n-ply look-ahead or (r - GAMMA * V(opponent)).  
	 * @param thisSO
	 * 			  only for debug info: access to the current state's stringDescr()
	 */
	public void updateWeightsTD(int[] curBoard, int curPlayer, 
			double vLast, double reward, double target, StateObservation thisSO) {
		// delta is the error signal:
		double delta = (target - vLast);
		// derivative of tanh ( if hasSigmoid()==true):
		double e = (hasSigmoid() ? (1.0 - vLast * vLast) : 1.0);

		update(curBoard, curPlayer, 0, delta, e, false);
		
		if (TDNTuple2Agt.DBG_REWARD || TDNTuple2Agt.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getScoreI(curBoard,curPlayer);
			System.out.println("updateWeightsNew[p="+curPlayer+", "+thisSO.stringDescr()
			+"] qLast,v_new:"+vLast*MAXSCORE+", "+v_new*MAXSCORE+", T="+target*MAXSCORE+", R="+reward);
			dbg3PArr[curPlayer]=v_new*MAXSCORE;
		}
	}

	/**
	 * Update the weights of the n-tuple system in case of Q-learning ({@link SarsaAgt} or QLearnAgt). 
	 * 
	 * @param curBoard
	 *            the current board
	 * @param nextPlayer
	 *            the player to move on current board
	 * @param lastAction
	 *            the action taken in current board
	 * @param qLast
	 *            the Q-value Q(s,a) for s=curBoard and a=lastAction
	 * @param reward
	 *            the delta reward given for taking action a in state s
	 * @param target
	 *            the target to learn, usually (reward + GAMMA * value of the after-state) for
	 *            non-terminal states.   
	 * @param thisSO
	 * 			  only for debug info: access to the current state's stringDescr()
	 */
	public void updateWeightsQ(int[] curBoard, int nextPlayer, Types.ACTIONS lastAction,
			double qLast, double reward, double target, StateObservation thisSO) {
		// delta is the error signal:
		double delta = (target - qLast);
		// derivative of tanh ( if hasSigmoid()==true):
		double e = (hasSigmoid() ? (1.0 - qLast * qLast) : 1.0);

		int o = lastAction.toInt();
		update(curBoard, nextPlayer, o, delta, e, true);
		
		if (TDNTuple2Agt.DBG_REWARD || TDNTuple2Agt.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getQFunc(curBoard,nextPlayer,lastAction);
			System.out.println("updateWeightsNew[p="+nextPlayer+", "+thisSO.stringDescr()
			+"] qLast,v_new:"+qLast*MAXSCORE+", "+v_new*MAXSCORE+", T="+target*MAXSCORE+", R="+reward);
			dbg3PArr[nextPlayer]=v_new*MAXSCORE;
		}
	}

	/**
	 * Update the weights of the n-Tuple-system for a terminal state (target is 0).
	 * 
	 * @param curBoard 	the current board
	 * @param curPlayer the player whose value function is updated (the p in V(s_t|p) )
	 * @param thisSO	only needed when debugging
	 * @param isNEW_3P	only needed when debugging
	 */
	public void updateWeightsNewTerminal(int[] curBoard, int curPlayer,StateObservation thisSO, boolean isNEW_3P) {
		double v_old = getScoreI(curBoard,curPlayer); // old value
		// delta is the error signal (here with target signal = 0.0):
		double delta = (0.0 - v_old);
		// derivative of tanh ( if hasSigmoid()==true)
		double e = (hasSigmoid() ? (1.0 - v_old * v_old) : 1.0);

		update(curBoard, curPlayer, 0, delta, e, false);

		if (TDNTuple2Agt.DBGF_TARGET || TDNTuple2Agt.DBG_REWARD || TDNTuple2Agt.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getScoreI(curBoard,curPlayer);
			if (isNEW_3P) {
				System.out.println("updateWeights(***finalSO)[p="+curPlayer+", "+thisSO.stringDescr()
				+"] v_old,v_new:"+v_old*MAXSCORE+", "+v_new*MAXSCORE);
				dbg3PTer[curPlayer]=v_new*MAXSCORE;
			} else {
				System.out.println("updateWeights(**final_2P)[p="+curPlayer+", "+thisSO.stringDescr()
				+"] v_old,v_new:"+v_old*MAXSCORE+", "+v_new*MAXSCORE);
				dbg3PTer[2]=v_new*MAXSCORE;				
			}
			if (!isNEW_3P) {
				int dummy=1;				
			}
			int dummy=1;
		}
	}

	/**
	 * Update all n-Tuple LUTs. Simply add dW to all relevant weights. Also
	 * update the symmetric boards (equivalent states), if wanted (if {@link #getUSESYMMETRY()}{@code =true}).
	 * 
	 * @param board
	 *            board, for which the weights shall be updated,
	 *            as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board}
	 * @param output
	 *            the output unit for which we provide the delta (the action key)
	 *            (for TD this is always 0, but Sarsa and Q-learning have several output units)
	 * @param delta
	 * 			  the delta signal we propagate back
	 * @param e   derivative of tanh ( if hasSigmoid()==true)
	 * @param QMODE   
	 * 			  whether called via {@code updateWeightsQ} (Sarsa, Q-learning) 
	 * 			  or via {@code updateWeightsNew*} (TD-learning)
	 * <p>
	 * The value added to all active weights is alphaM*delta*e   (in case LAMBDA==0)
	 */
	private void update(int[] board, int player, int output, double delta, double e, 
						boolean QMODE) {
		int i, j, out;
		double alphaM, sigDeriv, lamFactor;

		// Get equivalent boards (including self) and corresponding actions
		int[][] equiv = getSymBoards2(board,getUSESYMMETRY());
		int[] equivAction = (QMODE ? getSymActions(output, getUSESYMMETRY()) : null); 
		// equivAction only needed for QMODE==true

		alphaM = ALPHA / (numTuples*equiv.length); 

		// construct new EligStates object, add it at head of LinkedList eList and remove 
		// from the list the element 'beyond horizon' t_0 = t-horizon (if any):
		EligStates elem = new EligStates(equiv,equivAction,e);
		eList.addFirst(elem);
		if (eList.size()>horizon) eList.pollLast();
		
		// iterate over all list elements in horizon  (h+1 elements from t down to t_0):
		ListIterator<EligStates> iter = eList.listIterator();		
		lamFactor=1;  // holds 1, LAMBDA, LAMBDA^2,... in successive passes through while-loop
		while(iter.hasNext()) {
			elem=iter.next();
			equiv=elem.equiv;
			equivAction=elem.equivAction;
//			printEquivs(equiv,equivAction);		// debug (TTT only)
			//System.out.println(eList.size()+" "+lamFactor+"   ["+ equiv[0]+"]");	// debug
			assert (lamFactor >= tdAgt.getParTD().getHorizonCut()) 
					: "Error: lamFactor < ParTD.getHorizonCut";
			e = lamFactor*elem.sigDeriv;
			for (i = 0; i < numTuples; i++) {
				nTuples[output][player][i].clearIndices();
				for (j = 0; j < equiv.length; j++) {
					// this assertion is only valid for TicTacToe, where each action should be 
					// on an empty field which is coded as '1' here:
					//assert (equiv[j][equivAction[j]]==1) : "Oops, action TicTacToe not viable";
					
					out = (QMODE ? equivAction[j] : output);
//					System.out.print("(i,j)=("+i+","+j+"):  ");		//debug
					nTuples[out][player][i].updateNew(equiv[j], alphaM, delta, e);
				}
			}
			lamFactor *= getLambda(); 
		}
		numLearnActions++;
	}

	// debug TicTacToe only: print equivalent boards & equivalent actions
	private void printEquivs(int[][] equiv, int[] equivAction) {
		int j,k,m,n;
		String[] symb = {"o","-","X"};
		for (j = 0; j < equiv.length; j++) {
			for (k=0,m=0; m<3; m++) {
				for (n=0; n<3; n++,k++) 
					System.out.print(symb[equiv[j][k]]);
				System.out.println("");
			}
			System.out.println("Action = "+equivAction[j]);
		}
		
	}

	/**
	 * Is called only in case (TC && !tcImm), but !tcImm is not recommended
	 * 
	 * @see TDNTuple2Agt#trainAgent(StateObservation)
	 */
	@Deprecated
	public void updateTC() {
		int i, k;
			for (int o=0; o < numOutputs; o++) {
				for (i = 0; i < numTuples; i++) {
					for (k = 0; k < numPlayers; k++)
						nTuples[o][k][i].updateTC();
				}
			}

	}
	
	public void setAlpha(double newStartAlpha) {
		ALPHA = newStartAlpha;
	}

	public void setAlphaChangeRatio(double newAlphaChangeRatio) {
		m_AlphaChangeRatio = newAlphaChangeRatio;
	}

	public void setEpochs(int epochs) {
		epochMax = epochs;
	}
	public void setTdAgt(NTupleAgt tdAgt) {
		this.tdAgt = tdAgt;
	}
	
	public double getAlpha() {
		return ALPHA;
	}

	public double getLambda() {
		return tdAgt.getParTD().getLambda();
	}

	public double getGamma() {
		return tdAgt.getParTD().getGamma();
	}

	public double getAlphaChangeRatio() {
		return m_AlphaChangeRatio;
	}
	
	public XNTupleFuncs getXnf() {
		return xnf;
	}

	public boolean hasSigmoid() {
		return tdAgt.getParTD().hasSigmoid();
	}
	
	public boolean hasRpropLrn() {
		return tdAgt.getParTD().hasRpropLrn();
	}

	public boolean getUSESYMMETRY() {
		return tdAgt.getParNT().getUSESYMMETRY();
	}

	public void clearEligList() {
		eList.clear();
	}
	
	public void clearEligList(NTupleAgt.EligType m_elig) {
		switch(m_elig) {
		case STANDARD: 
			// do nothing
			break;
		case RESET: 
			eList.clear();
			break;
		}
	}

	
	public int getHorizon() {
		return horizon;
	}

	public void setHorizon() {
		if (getLambda()==0.0) {
			horizon=1;
		} else {
			horizon = 1+(int) (Math.log(tdAgt.getParTD().getHorizonCut())/Math.log(getLambda()));
		}		
	}

	public long getNumPlayers() {
		return numPlayers;
	}

	public long getNumLearnActions() {
		return numLearnActions;
	}

	public void resetNumLearnActions() {
		this.numLearnActions = 0L;
	}


	// class EligStates is needed in update(int[],int,int,double,double,boolean)
	private class EligStates implements Serializable {
		int[][] equiv;
		int[] equivAction;
		double sigDeriv;
		
		EligStates(int[][] equiv, int[] equivAction, double sigDeriv) {
			this.equiv=equiv.clone();
			this.equivAction=(equivAction==null ? null : equivAction.clone());
			this.sigDeriv=sigDeriv;
		}
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
		 nTuples[0][0][0].printTable();
	}
	
	public void printLutHashSum(PrintStream pstream) {
		for (int o=0; o < numOutputs; o++) {
			pstream.print("\nLUT output unit "+o+": ");
			for (int p=0; p<nTuples.length; p++) {
				pstream.print("LUT hash sum player "+p+": ");
				for (int j = 0; j < nTuples[p].length; j++)
					pstream.print(" " + (nTuples[o][p][j].lutHashSum()) + "|");
				pstream.println("");
			}		
		}
	}
	
	public void printLutSum(PrintStream pstream) {
		for (int o=0; o < numOutputs; o++) {
			pstream.print("\nLUT output unit "+o+": ");
			for (int p=0; p<nTuples.length; p++) {
				pstream.print("LUT sum player "+p+": ");
				for (int j = 0; j < nTuples[p].length; j++)
					pstream.print(frmS.format(nTuples[o][p][j].lutSum())+"|");
				pstream.println("");
			}
			for (int p=0; p<nTuples.length; p++) {
				pstream.print("LUT ABS player "+p+": ");
				for (int j = 0; j < nTuples[p].length; j++)
					pstream.print(frmS.format(nTuples[o][p][j].lutSumAbs())+"|");
				pstream.println("");
			}
		}
		
	}
	
	/**
	 * Analyze the weight distribution and - if TCL is active - the tcFactorArray distribution 
	 * by calculating certain quantiles (percentiles)
	 * @param per the quantiles to calculate. If null, then use DEFAULT_PER 
	 * 			= {0,25,50,75,100}, where the numbers are given in percent.
	 * @return res[][] with <ul>
	 * 		<li> res[0][]: a copy of {@code per}
	 * 		<li> res[1][]: the corresponding quantiles of the weights (from all LUTs)
	 * 		<li> res[2][]: the corresponding quantiles of tcFactorArray (from all LUTs)
	 * </ul>
	 */
	public double[][] weightAnalysis(double[] per) {
		double[] DEFAULT_PER = {0,25,50,75,100};
		if (per==null) per = DEFAULT_PER;
		double[][] res = new double[3][per.length];  	// res[0]: per, res[1]: quantiles of data
														// res[2]: quantiles of tcdat;
		System.arraycopy(per, 0, res[0], 0, per.length);
		
		int count = 0;
		NTuple2[] ntuples = this.getNTuples();
		for (int i=0; i<ntuples.length; i++) {
			count += ntuples[i].getLutLength();
		}
		double[] data = new double[count];
		double[] tcdat = new double[count];
		double[] lut;
		double[] tcf=null;
		int i, pos=0;
		
		// --- this is fast, but has the disadvantage that many zeros are included --> 
		// --- 25%- 50%- and 75%-quantiles tend to be exactly zero
		// --- (the zeros come from the fact that many weights are never visited in training)
//		for (i=0,len=0; i<ntuples.length; i++) {
//			len = ntuples[i].getLutLength();
//			lut = ntuples[i].getWeights();
//			assert lut.length==len : "length assertion error";
//			System.arraycopy(lut, 0, data, pos, len);
//			pos += len; 
//		}

		// --- this is slower, but quantiles for active weights are more meaningful
		// --- and not always 0.0 (!)
		for (i=0; i<ntuples.length; i++) {
			lut = ntuples[i].getWeights();
			tcf = ntuples[i].getTcFactorArray();
			for (int j=0; j<lut.length; j++) {
				if (lut[j]!=0) {
					 if (tcf!=null) tcdat[pos] = tcf[j];
					data[pos++] = lut[j];
				}
			}
		}
		int nActive=pos;
		int pActive = (int) (((double)nActive)/count*100);
		double[] data2 = new double[nActive];
		double[] tcdat2 = new double[nActive];
		System.arraycopy(data, 0, data2, 0, nActive);
		System.arraycopy(tcdat, 0, tcdat2, 0, nActive);
		data = data2;
		tcdat = tcdat2;
		if (tcf==null) System.out.println("WARNING: tcFactorArray is null");

		// --- only testing / debug ---
//		double[] data = {0,1,2,3,4,5,6,7,8,9};
//		int length2 = 1000;
//		double[] data2 = new double[length2];
//		System.arraycopy(data, 0, data2, 0, length2);
//		data = data2;
		
		Percentile p = new Percentile(); // from commons-math3-3.6.1.jar, see https://commons.apache.org/proper/commons-math/javadocs/api-3.0/org/apache/commons/math3/stat/descriptive/rank/Percentile.html
		Min m = new Min(); 
		Percentile p2 = new Percentile();
		Min m2 = new Min(); 
		
		p.setData(data);	
		p2.setData(tcdat);
		for (i=0; i<per.length; i++) {
			if (per[i]==0) {
				res[1][i] = m.evaluate(data);
				res[2][i] = m2.evaluate(tcdat);
			} else { 
				res[1][i] = p.evaluate(per[i]);
				res[2][i] = p2.evaluate(per[i]);
			}
		}

		DecimalFormat form = new DecimalFormat("000");
		DecimalFormat df = new DecimalFormat();				
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		df.applyPattern("+0.0000000;-0.0000000");  
		System.out.println("weight analysis " + tdAgt.getClass().getSimpleName() + " ("
				+count+" weights, "+nActive+" active ("+pActive+"%)): ");
		for (i=0; i<per.length; i++) {
			System.out.println("   Quantile [" + form.format(per[i]) + "] = "	
					+df.format(res[1][i]) + " / " + ((tcf==null)?"NA":df.format(res[2][i])) );			
		}
		
		return res;
	}

	

}

