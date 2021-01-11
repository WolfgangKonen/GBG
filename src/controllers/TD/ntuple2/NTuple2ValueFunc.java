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

import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import games.BoardVector;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleFuncs;
import params.ParNT;
import tools.Types;

/**
 *         Implementation of a trainable value-function using n-tuple systems.
 *         A set of n-tuples is generated randomly or by user precept. Random
 *         n-tuples can be just a set of random points or a random walk on the
 *         board. The value-function uses symmetries of the board to allow a
 *         faster training. The output of the value-function is put
 *         through a sigmoid function (tanh) or not depending on the value returned
 *         from {@link #hasSigmoid()}. The learning rate alpha decreases exponentially 
 *         from a start value at the beginning of the training to an end value after a 
 *         certain amount of games.
 * 
 * @see TDNTuple3Agt
 * @see SarsaAgt
 * 
 * @author Markus Thill, Wolfgang Konen (extension TD(lambda)), TH Koeln, 2017-2020  
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
	private final int numOutputs;
	
	// number of players
	private final int numPlayers;
	
	// number of n-tuples
	private final int numTuples;

	private final static double[][] dbgScoreArr = new double[100][2];
	
	NTupleAgt tdAgt;		// the 'parent' - used to access the parameters in m_tdPar, m_ntPar
	
	// The generated n-tuples [numOutputs][numPlayers][numTuples]
	private NTuple2 nTuples[][][];
	
	public XNTupleFuncs xnf;
	
	// elements needed for TD(lambda)-update with finite horizon, 
	// see update(int[],int,double,double):
	private int horizon=0;
	private transient LinkedList<EligStates>[] eList;

	private final boolean PRINTNTUPLES = false;	// /WK/ control the file printout of n-tuples (when loading agents)
	private final DecimalFormat frmS = new DecimalFormat("+0.00000;-0.00000");
	
	private final static double[] dbg3PArr = new double[3];
	private final static double[] dbg3PTer = new double[3];

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
	 * 			  The agent object where {@code this} is part of. Used to access
	 * 			  m_tdPar, m_ntPar and their parameters like withSigmoid, USESYMMETRY and so on.
	 * @param nTuplesI
	 *            The set of n-tuples as an {@code int} array. For each {@code nTuplesI[i]}
	 *            the constructor will construct k {@link NTuple2} objects of the same form,
	 *            one for each player ({@code k=xnf.getNumPlayers()}). Allowed values 
	 *            for the sampling points of an n-tuple: 0,..,numCells.
	 * @param xnf
	 * @param posVals
	 *            number of possible values per board cell (TicTacToe: 3)
	 * @param randInitWeights
	 *            true, if all weights of all n-Tuples shall be initialized
	 *            randomly. Otherwise they are initialized with 0 (which allows to count the active weigths) 
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
		this.eList = new LinkedList[this.numPlayers];
		for (int ie=0; ie<eList.length; ie++) eList[ie] = new LinkedList<EligStates>();
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

	public boolean instantiateAfterLoading() {
		this.eList = new LinkedList[this.numPlayers];
		for (int ie=0; ie<eList.length; ie++) eList[ie] = new LinkedList<EligStates>();
		for (int i = 0; i < numTuples; i++) {
			for (int o=0; o<numOutputs; o++) {
				for (int k=0; k<numPlayers; k++) {
					this.nTuples[o][k][i].instantiateAfterLoading();
				}				
			}
		}
		return true;
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
	 * @param curSOWB
	 * 			  the state as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board} (0,...,N-1)
	 * @param act
	 *            the action to perform on {@code board}
	 * @return
	 */
	public double getQFunc(StateObsWithBoardVector curSOWB, int player, Types.ACTIONS act) {
		int i, j;
		double score = 0.0;
		BoardVector[] equiv;
		int[] equivAction;

		// Get equivalent boards (including self)
		equiv = getSymBoards2(curSOWB, getUSESYMMETRY(), getNSym());
		equivAction = xnf.symmetryActions(act.toInt());

		for (i=0; i<equivAction.length; i++) {
			if (equivAction[i]>=nTuples.length) {
				System.err.println("Warning: equivAction["+i+"]="+equivAction[i]+" is not smaller than nTuples.length="+nTuples.length+" !!!");
				// this should normally not happen. If it happens, we are out for an OutOfBoundException in the 
				// following lines ...
			}
		}
		
		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++) {
				score += nTuples[equivAction[j]][player][i].getScore(equiv[j].bvec);
			}
		}

		return (hasSigmoid() ? Math.tanh(score) : score);
	}
	
	/**
	 * Get the value for this state in int[]-representation
	 * 
	 * @param curSOWB
	 * 			  the state as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board} (0,...,N-1)
	 * @return
	 */
	public double getScoreI(StateObsWithBoardVector curSOWB, int player) {
		int i, j;
		double score = 0.0; 
		BoardVector[] equiv;

		// Get equivalent boards (including self)
		equiv = getSymBoards2(curSOWB, getUSESYMMETRY(), getNSym());
		//equiv = getSymBoards2(board, false);    // DON'T, at least for TTT clearly inferior

		for (i = 0; i < numTuples; i++) {
			for (j = 0; j < equiv.length; j++) {
				//System.out.println("g(i,j)=("+i+","+j+"):  ");		//debug
				score += nTuples[0][player][i].getScore(equiv[j].bvec);
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
	 * @param curSOWB
	 *            board as 1D-integer vector (position value for each board cell) 
	 * @param useSymmetry if false, return a vector of BoardVectors with only one element
	 * 			(the board itself in BoardVector[0])
	 * @param nSym the number of symmetry vectors to use (if = 0, use all symmetries)
	 * @return the equivalent board vectors
	 */
	private BoardVector[] getSymBoards2(StateObsWithBoardVector curSOWB, boolean useSymmetry, int nSym) {
		BoardVector[] equiv;
		
		assert nSym >= 0 : "Ooops, nSym="+nSym+" is negative!";
		assert nSym <= xnf.getNumSymmetries() 
				: "Oops, nSym="+nSym+" is larger than xnf.getNumSymmetries()="+xnf.getNumSymmetries()+"!";
		
		
		if (useSymmetry) {
			if (nSym==0) nSym=xnf.getNumSymmetries();
			if (tdAgt instanceof SarsaAgt && nSym < xnf.getNumSymmetries())
				// in the SarsaAgt case we can only handle the case n=getNumSymmetries() (use all symmetries). 
				// This is because symmetryActions currently assumes that all symmetries are taken.
				// (symmetryActions is ONLY needed by SarsaAgt - and perhaps later by QLearnAgt)
				throw new RuntimeException("[NTuple2ValueFunc] Sorry, cannot handle case SarsaAgt and 0 < nSym < s (symmetryActions not yet adapted).");

			equiv = xnf.symmetryVectors(curSOWB,nSym);
		} else {
			equiv = new BoardVector[1];
			equiv[0] = curSOWB.getBoardVector();			
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
	 * Update the weights of the n-tuple system in case of {@link TDNTuple3Agt}. The difference
	 * to former {@code updateWeights} is that the target is now: reward + GAMMA*valueFunction(next), irrespective of the 
	 * former parameter {@code finished}.
	 * The value function estimated by {@code this} has a different meaning: it 
	 * estimates the sum of future rewards.
	 * 
	 * @param curSOWB
	 *            the current board
	 * @param curPlayer
	 *            the player whose value function is updated (the p in V(s_t|p) )
	 * @param nextSOWB
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
	public void updateWeightsNew(StateObsWithBoardVector curSOWB, int curPlayer, 
			StateObsWithBoardVector nextSOWB, int nextPlayer,
			double reward, double target, /*boolean upTC,*/ StateObservation thisSO) {
		double v_old = getScoreI(curSOWB,curPlayer); // old value
		// delta is the error signal:
		double delta = (target - v_old);
		// derivative of tanh ( if hasSigmoid()==true):
		double e = (hasSigmoid() ? (1.0 - v_old * v_old) : 1.0);

		update(curSOWB, curPlayer, 0, delta, e, false, false);
		
		if (NTupleBase.DBG_REWARD || NTupleBase.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getScoreI(curSOWB,curPlayer);
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
	 * @param curSOWB
	 *            the board for which the NN is to be adapted. This is in case of {@link TDNTuple3Agt} the 
	 *            afterstate generated by curPlayer <b>one round earlier</b>, i.e. {@code sLast[curPlayer]}
	 * @param curPlayer
	 *            the player who generated the current board
	 * @param vLast
	 *            the value V(s) for s=curBoard and player curPlayer (NN estimate)
	 * @param target
	 *            the target to learn, usually (reward + GAMMA * value of the actual afterstate) for
	 *            non-terminal states.   
	 * @param reward
	 *            only for debug info: the delta reward given for the transition into next board 
	 * @param thisSO
	 * 			  only for debug info: access to the current state's stringDescr()
	 */
	public void updateWeightsTD(StateObsWithBoardVector curSOWB, int curPlayer, 
			double vLast, double target, double reward, StateObservation thisSO) {
		// delta is the error signal:
		double delta = (target - vLast);
		// derivative of tanh ( if hasSigmoid()==true):
		double e = (hasSigmoid() ? (1.0 - vLast * vLast) : 1.0);

		update(curSOWB, curPlayer, 0, delta, e, false, true);
		
		if (NTupleBase.DBG_REWARD || NTupleBase.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getScoreI(curSOWB,curPlayer);
			System.out.println("updateWeightsTD[p="+curPlayer+", "+thisSO.stringDescr()
			+"] qLast,v_new:"+vLast*MAXSCORE+", "+v_new*MAXSCORE+", T="+target*MAXSCORE+", R="+reward);
			dbg3PArr[curPlayer]=v_new*MAXSCORE;
		}
	}

	/**
	 * Update the weights of the n-tuple system in case of Q-learning ({@link SarsaAgt} or QLearnAgt). 
	 * 
	 * @param lastSOWB
	 *            the board for which the NN is to be adapted. This is in case of {@link SarsaAgt} the 
	 *            state of lastPlayer <b>one round earlier</b>, i.e. {@code sLast[lastPlayer]}
	 * @param lastPlayer
	 *            the player to move on lastBoard
	 * @param lastAction
	 *            the action taken on lastBoard
	 * @param qLast
	 *            the Q-value Q(s,a) for s=lastBoard and a=lastAction
	 * @param reward
	 *            the delta reward given for taking action a in state s
	 * @param target
	 *            the target to learn, usually (reward + GAMMA * value of the after-state) for
	 *            non-terminal states.   
	 * @param thisSO
	 * 			  only for debug info: access to the current state's stringDescr()
	 */
	public void updateWeightsQ(StateObsWithBoardVector lastSOWB, int lastPlayer, Types.ACTIONS lastAction,
			double qLast, double reward, double target, StateObservation thisSO) {
		// delta is the error signal:
		double delta = (target - qLast);
		// derivative of tanh ( if hasSigmoid()==true):
		double e = (hasSigmoid() ? (1.0 - qLast * qLast) : 1.0);

		int o = lastAction.toInt();
		update(lastSOWB, lastPlayer, o, delta, e, true, true);
		
		if (NTupleBase.DBG_REWARD || NTupleBase.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getQFunc(lastSOWB,lastPlayer,lastAction);
			System.out.println("updateWeightsNew[p="+lastPlayer+", "+thisSO.stringDescr()
			+"] qLast,v_new:"+qLast*MAXSCORE+", "+v_new*MAXSCORE+", T="+target*MAXSCORE+", R="+reward);
			dbg3PArr[lastPlayer]=v_new*MAXSCORE;
		}
	}

	/**
	 * Update the weights of the n-Tuple-system in case of {@link TDNTuple3Agt} for a terminal state towards target 0.
	 * 
	 * @param curSOWB 	the current board
	 * @param curPlayer the player whose value function is updated (the p in V(s_t|p) )
	 * @param thisSO	only needed when debugging
	 * @param isNEW_3P	only needed when debugging
	 */
	public void updateWeightsNewTerminal(StateObsWithBoardVector curSOWB, int curPlayer,StateObservation thisSO, boolean isNEW_3P) {
		double v_old = getScoreI(curSOWB,curPlayer); // old value
		// delta is the error signal (here with target signal = 0.0):
		double delta = (0.0 - v_old);
		// derivative of tanh ( if hasSigmoid()==true)
		double e = (hasSigmoid() ? (1.0 - v_old * v_old) : 1.0);

		update(curSOWB, curPlayer, 0, delta, e, false, false);

		if (NTupleBase.DBGF_TARGET || NTupleBase.DBG_REWARD || NTupleBase.DBG_OLD_3P) {
			final double MAXSCORE = 1; // 1; 3932156;
			double v_new = getScoreI(curSOWB,curPlayer);
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
	 * update the symmetric boards (equivalent states), if wanted 
	 * (if {@link #getUSESYMMETRY()}{@code =true}).
	 * <p>
	 * The value added to all active weights is alphaM*delta*e   (in case LAMBDA==0)
	 * 
	 * @param curSOWB
	 *            board, for which the weights shall be updated,
	 *            as 1D-integer vector (position value for each board cell) 
	 * @param player
	 *            the player who has to move on {@code board}
	 * @param output
	 *            the output unit for which we provide the delta (the action key)
	 *            (for TD this is always 0, but Sarsa and Q-learning have several output units)
	 * @param delta
	 * 			  the delta signal that we propagate back
	 * @param e   derivative of tanh (if hasSigmoid()==true)
	 * @param QMODE :<br>  
	 * 			  {@code true}, if called via {@code updateWeightsQ} (Q-learning via {@link SarsaAgt});<br> 
	 * 			  {@code false}, if called from TD-learning (via  {@code updateWeightsNew*}, {@link TDNTuple3Agt},
	 * 			  or via {@code updateWeightsTD}, {@link TDNTuple3Agt})	
	 * @param ELIST_PP (eList-per-player, whether to keep separate eligibility lists per player):<br>   
	 * 			  {@code true}, if called from 'new' TD-learning {@link SarsaAgt} or {@link TDNTuple3Agt};<br> 
	 * 			  {@code false}, if called from 'old' TD-learning (via {@link TDNTuple3Agt})
	 */
	protected void update(StateObsWithBoardVector curSOWB, int player, int output, double delta, double e,
						boolean QMODE, boolean ELIST_PP) {
		int i, j, out;
		double alphaM, lamFactor;

		// Get equivalent boards (including self) and corresponding actions
		BoardVector[] equiv = getSymBoards2(curSOWB, getUSESYMMETRY(), getNSym());
		int[] equivAction = (QMODE ? getSymActions(output, getUSESYMMETRY()) : null); 
		// equivAction only needed for QMODE==true

		alphaM = ALPHA / (numTuples*equiv.length); 

		// construct new EligStates object, add it at head of LinkedList eList[ie] and remove 
		// from the list the element 'beyond horizon' t_0 = t-horizon (if any).
		// The LinkedList to use is either always the same one (ie=0, if ELIST_PP==false) or 
		// the list kept for each specific player 'player' (if ELIST_PP==true):
		int ie = (ELIST_PP ? player : 0);
		EligStates elem = new EligStates(equiv,equivAction,e);
		eList[ie].addFirst(elem);
		assert (horizon>0) : "[NTuple2ValueFunc.update] Error: horizon is 0 !";
		if (eList[ie].size()>horizon) eList[ie].pollLast();
		
		// iterate over all list elements in horizon  (at most h+1 elements from t down to t_0):
		ListIterator<EligStates> iter = eList[ie].listIterator();		
		lamFactor=1;  // holds 1, LAMBDA, LAMBDA^2,... in successive passes through while-loop
		while(iter.hasNext()) {
			elem=iter.next();
			equiv=elem.equiv;
			equivAction=elem.equivAction;
//			printEquivs(equiv,equivAction);		// debug (TTT only)
			//System.out.println(eList[ie].size()+" "+lamFactor+"   ["+ equiv[0]+"]");	// debug
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
					nTuples[out][player][i].updateNew(equiv[j].bvec, alphaM, delta, e);
				}
			}
			lamFactor *= getLambda(); 
		}
		numLearnActions++;
	}


	/**
	 * Is called only in case {@code (TC && !tcImm)}, but {@code !tcImm} is not recommended
	 * 
	 * @see TDNTuple3Agt#trainAgent(StateObservation)
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

	public boolean getTc() {
		return this.nTuples[0][0][0].getTc();
	}

	public boolean getTcImm() {
		return this.nTuples[0][0][0].getTcImm();
	}

	public boolean getUSESYMMETRY() {
		return tdAgt.getParNT().getUSESYMMETRY();
	}

	public int getNSym() {
		return tdAgt.getParNT().getNSym();
	}

	public boolean hasSigmoid() {
		return tdAgt.getParTD().hasSigmoid();
	}
	
	public boolean hasRpropLrn() {
		return tdAgt.getParTD().hasRpropLrn();
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


	public void clearEligList() {
		for (int ie=0; ie<eList.length; ie++)
			eList[ie].clear();
	}
	
	public void clearEligList(NTupleAgt.EligType m_elig) {
		switch(m_elig) {
		case STANDARD: 
			// do nothing
			break;
		case RESET: 
			clearEligList();
			break;
		}
	}

	
	// class EligStates is needed in update(int[],int,int,double,double,boolean)
	private class EligStates implements Serializable {
		BoardVector[] equiv;
		int[] equivAction;
		double sigDeriv;
		
		EligStates(BoardVector[] equiv, int[] equivAction, double sigDeriv) {
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
				pstream.println();
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
				pstream.println();
			}
			for (int p=0; p<nTuples.length; p++) {
				pstream.print("LUT ABS player "+p+": ");
				for (int j = 0; j < nTuples[p].length; j++)
					pstream.print(frmS.format(nTuples[o][p][j].lutSumAbs())+"|");
				pstream.println();
			}
		}
		
	}
	
	// debug TicTacToe only: print equivalent boards & equivalent actions
	private void printEquivs(int[][] equiv, int[] equivAction) {
		int j,k,m,n;
		String[] symb = {"o","-","X"};
		for (j = 0; j < equiv.length; j++) {
			for (k=0,m=0; m<3; m++) {
				for (n=0; n<3; n++,k++) 
					System.out.print(symb[equiv[j][k]]);
				System.out.println();
			}
			System.out.println("Action = "+equivAction[j]);
		}
		
	}
	
	/**
	 * Print the configuration of all n-tuples to file {@code agents/theNtuple.txt}.
	 * <p> 
	 * For each n-tuple we print
	 * <pre>   {0,4,15,22, ...} </pre>
	 * i. e. the list of cells covered by this n-tuple.
	 * 
	 * @throws IOException
	 */
	public void printNTuples() throws IOException {
		String str;
		PrintWriter theNtuple = new PrintWriter(new FileWriter("agents/theNtuple.txt",false));
		theNtuple.println("{");
		for (int i=0; i<this.nTuples[0][0].length; i++) {
			str = "  {"+this.nTuples[0][0][i].getPosition(0);
			theNtuple.print(str);
			//System.out.print(str);
			for (int j=1; j<this.nTuples[0][0][i].getLength(); j++) {
				str = ", "+this.nTuples[0][0][i].getPosition(j);
				theNtuple.print(str);
				//System.out.print(str);
			}
			str = (i==(this.nTuples[0][0].length-1)) ? "}" : "},";
			theNtuple.println(str);
			//System.out.println(str);
		}		
		theNtuple.println("}");
		theNtuple.close();
	}


	/**
	 * Analyze the weight distribution and - if TCL is active - the {@code tcFactorArray} distribution 
	 * by calculating certain quantiles (percentiles). For example, quantile[25]=0.134 means 
	 * that 25% of the data are smaller than or equal to 0.134.
	 * <p>
	 * Some remarks: <ul>
	 * <li> Quantiles are calculated only over <b>active</b> weights. This is necessary, because 
	 * 		normally only less than 10% of the weights are active: otherwise quantile[25] and 
	 *  	other would tend to be zero.
	 * <li> {@code tcFactorArray} is only available directly after training. If an agent is stored
	 * 		to disk, {@code tcFactorArray} is not stored (to minimize file size, it is only 
	 * 		necessary during training). When reloading an agent from disk, we will have 
	 * 		{@code tcFactorArray=null}, even if TCL is active.
	 * <li> The analysis results are printed to {@code System.out} in the form
	 * <pre>
	 *             per       LUT     / tcFactor
	 *   Quantile [000] = -1.6982614 / NA 
	 *   ...       ...                              </pre>
	 * 		where {@code NA} indicates that {@code tcFactorArray} is not available.
	 * <li> As a side effect, this method writes the configuration of all n-tuples to file
	 * 		{@code agents/theNtuple.txt}, see {@link #printNTuples()}.
	 * </ul>
	 * @param per the quantiles to calculate. If null, then use DEFAULT_PER 
	 * 			= {0,25,50,75,100}, where the numbers are given in percent. (Note that quantile[0]
	 * 			and quantile[100] specify minimum and maximum of the data.)
	 * @return {@code double res[][]} with <ul>
	 * 		<li> {@code res[0][]}: a copy of {@code per}
	 * 		<li> {@code res[1][]}: the corresponding quantiles of the LUT weights (from all n-tuples)
	 * 		<li> {@code res[2][]}: the corresponding quantiles of {@code tcFactorArray} (from all n-tuples)
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
		
		// data is an array big enough to hold all LUT data. It will be filled below with all 
		// active LUT weights (i.e. LUT  != 0.0). This distinction between active and 
		// inactive weights works of course only, if LUTs are initialized with 0.0.
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
		int pActive = (int) Math.round(((double)nActive)/count*100);
		  
		double[] data2 = new double[nActive];	// data2 is an array holding all *active* LUT data.
		double[] tcdat2 = new double[nActive];
		System.arraycopy(data, 0, data2, 0, nActive);
		System.arraycopy(tcdat, 0, tcdat2, 0, nActive);
		data = data2;
		tcdat = tcdat2;

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
			if (per[i]==0) {	// this is because class Percentile cannot calculate quantile[0], the min of all data
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
		System.out.println("[NTuple2ValueFunc.weightAnalysis] " + tdAgt.getClass().getSimpleName() + " ("
				+count+" weights, "+nActive+" active ("+pActive+"%)): ");
		//if (tcf==null) System.out.println("WARNING: tcFactorArray is null");
		System.out.print("             per       LUT    ");
		System.out.println((tcf==null) ? "" : " / tcFactor");
		for (i=0; i<per.length; i++) {
			System.out.print("   Quantile [" + form.format(per[i]) + "] = "	
					+df.format(res[1][i]) );			
			System.out.println((tcf==null) ? "" : " / " +df.format(res[2][i]));
		}

		// print the n-tuple layout to file
		if (PRINTNTUPLES) {
			try {
				this.printNTuples();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		return res;
	}

	/**
	 * @return  res[0]: number of weights, res[1]: number of active weights
	 */
	public int[] activeWeights() {
		int count = 0;
		NTuple2[] ntuples = this.getNTuples();
		double[] lut;
		int i, pos;
		
		for (i=0; i<ntuples.length; i++) {
			count += ntuples[i].getLutLength();
		}
		
		for (i=0,pos=0; i<ntuples.length; i++) {
			lut = ntuples[i].getWeights();
			for (int j=0; j<lut.length; j++) {
				if (lut[j]!=0) {
					pos++;
				}
			}
		}
		int nActive=pos;
		int pActive = (int) Math.round(((double)nActive)/count*100);
		  
		System.out.println("[NTuple2ValueFunc.activeWeights] " + tdAgt.getClass().getSimpleName() + " ("
				+count+" weights, "+nActive+" active ("+pActive+"%)): ");
		
		int[] res = {count,nActive}; //new int[2];
		return res;
	}

	

}

