package controllers.TD.ntuple2;

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
import java.util.List;
import java.util.Locale;
import java.util.Random;

import agentIO.LoadSaveGBG;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ScoreTuple;
import controllers.AgentBase;
import controllers.ExpectimaxWrapper;
import controllers.MaxNWrapper;
import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple2.ZValueMulti;
import controllers.TD.ntuple2.TDNTuple2Agt.UpdateType;
import games.Arena;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.StateObsNondeterministic;
import games.XNTupleFuncs;
import games.XArenaMenu;

/**
 * The alternative TD-Learning {@link PlayAgent} (Temporal Difference reinforcement learning)
 * <b>with n-tuples</b>. 
 * It has a one-layer (perceptron-like) neural network with or without output-nonlinearity  
 * {@code tanh} to model the Q-function. 
 * The net follows closely the (pseudo-)code by [SuttonBarto98]. 
 * <p>
 * Some functionality is packed in the superclasses 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...) and
 * {@link NTupleBase} (finishUpdateWeights, increment*Counters, isTrainable, normalize2, ...)
 * <p>
 * {@link TDNTuple3Agt} is an alternative to {@link TDNTuple2Agt}. 
 * The differences between {@link TDNTuple3Agt} and {@link TDNTuple2Agt} are:
 * <ul>
 * <li> {@link TDNTuple3Agt} updates the value of a state for a player based on the value/reward
 * 		that the <b>same</b> player achieves in his next turn. It is in this way more similar to 
 * 		{@link SarsaAgt}. (Note that the updates of {@link TDNTuple2Agt} are based on the value/reward of 
 * 		the <b>next state</b>. This may require sign change, depending on the number of players.)
 * 		Thus {@link TDNTuple3Agt} is much simpler to generalize to 1-, 2-, 3-, ..., N-player games
 * 		than {@link TDNTuple2Agt}.
 * <li> Eligible states: {@link TDNTuple3Agt} updates with ELIST_PP=true, i.e. it has a separate 
 * 		{@code eList[p]} per player p. {@link TDNTuple2Agt} uses only one common {@code eList[0]}. 
 * 		Only relevant for LAMBDA &gt; 0. 
 * </ul>
 * The similarities of {@link TDNTuple3Agt} and {@link TDNTuple2Agt} are:
 * <ul>
 * <li> No eligibility traces, instead LAMBDA-horizon mechanism of [Jaskowski16] (faster and less
 * 		memory consumptive).
 * <li> Option AFTERSTATE (relevant only for nondeterministic games like 2048), which builds the value 
 * 		function on the argument afterstate <b>s'</b> (before adding random elements) instead 
 * 		of next state <b>s''</b> (afterstates yield faster learning and better generalization).
 * <li> Has the random move rate bug fixed: Now EPSILON=0.0 means really 'no random moves'.
 * <li> Learning rate ALPHA differently scaled: if ALPHA=1.0, the new value for a
 * 		state just trained will be exactly the target. Therefore, recommended ALPHA values are 
 * 		m*N_s bigger than in {@code TDNTupleAgt}, where m=number of n-tuples, N_s=number of 
 * 		symmetric (equivalent) states. 
 * <li> A change in the update formula: when looping over different equivalent
 * 		states, at most one update per index is allowed (see comment in {@link NTuple2} for 
 * 		member {@code indexList}).
 * </ul>
 * 
 * @see PlayAgent
 * @see AgentBase
 * @see NTupleBase
 * 
 * @author Wolfgang Konen, TH Köln, Dec'18
 */
public class TDNTuple3Agt extends NTupleBase implements PlayAgent,NTupleAgt,Serializable {
	
	private NTupleAgt.EligType m_elig;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;

	private int numPlayers;
	/**
	 * sLast[curPlayer] stores the last afterstate that curPlayer generated in his previous move
	 * (initially null)
	 */
	transient private StateObservation[] sLast;	// last state of player p
//	transient private Types.ACTIONS[] aLast;	// last action of player p
	transient private boolean[] randLast;		// whether last action of player p was a random action
	transient private ScoreTuple rLast;

	private boolean RANDINITWEIGHTS = false;// If true, init weights of value function randomly

	private boolean m_DEBG = false; //false;true;
	// debug printout in collectReward:
	public static boolean DBG_REWARD=false;
	
	// use ternary target in update rule:
	private boolean TERNARY=true;		// remains true only if it is a final-reward-game (see getNextAction2)
	
	// use finalAdaptAgents(...), normally true. Set only to false if you want to test how agents behave otherwise:
	private boolean FINALADAPTAGENTS=true;
	
	private int acount=0;
	/**
	 * Default constructor for {@link TDNTuple3Agt}, needed for loading a serialized version
	 */
	public TDNTuple3Agt() throws IOException {
		super();
		ParTD tdPar = new ParTD();
		ParNT ntPar = new ParNT();
		ParOther oPar = new ParOther();
		initNet(ntPar, tdPar, oPar, null, null, 1000);
	}

	/**
	 * Create a new {@link TDNTuple3Agt}
	 * 
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	public TDNTuple3Agt(String name, ParTD tdPar, ParNT ntPar, ParOther oPar, 
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		super(name);
		this.numPlayers = xnf.getNumPlayers();
		this.sLast = new StateObservation[numPlayers];
//		this.aLast = new Types.ACTIONS[numPlayers];
		this.randLast = new boolean[numPlayers];
		initNet(ntPar,tdPar,oPar, nTuples, xnf, maxGameNum);			
	}

	/**
	 * 
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param numOutputs	the number of outputs of the n-tuple network (=number of all
	 * 						available actions)
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	private void initNet(ParNT ntPar, ParTD tdPar, ParOther oPar,  
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		m_tdPar = new ParTD(tdPar);
		m_ntPar = ntPar;
		m_oPar = new ParOther(oPar);		// m_oPar is in AgentBase
		m_elig = (m_tdPar.getEligMode()==0) ? EligType.STANDARD : EligType.RESET;
		rand = new Random(System.currentTimeMillis()); //(System.currentTimeMillis()); (42); 
		
		int posVals = xnf.getNumPositionValues();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells,1);
		
		setNTParams(ntPar);
		
		setTDParams(tdPar, maxGameNum);
		
		setAgentState(AgentState.INIT);
	}

	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		this.m_Net.xnf.instantiateAfterLoading();
		assert (m_Net.getNTuples()[0].getPosVals()==m_Net.xnf.getNumPositionValues()) : "Error getPosVals()";
		assert (this.getParTD().getHorizonCut()!=0.0) : "Error: horizonCut==0";
		// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
		// (they would stay otherwise at their default values, would not 
		// get the loaded values)
		this.setTDParams(this.getParTD(), this.getMaxGameNum());
		this.setNTParams(this.getParNT());
		this.weightAnalysis(null);
		return true;
	}
	
	/**
	 * Get the best next action and return it 
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent
	 * @return actBest,		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the Q-value for each available
	 * action (as returned by so.getAvailableActions()) and the Q-value for the best action actBest, resp.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i, j;
		double bestValue;
        double value=0;			// the quantity to be maximized
        double otilde, rtilde;
		boolean rgs = this.getParOther().getRewardIsGameScore();
		if (!so.isFinalRewardGame()) this.TERNARY=false;		// we use TD target r + gamma*V
		StateObservation NewSO;
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
    	bestValue = -Double.MAX_VALUE;
		double[] VTable;		
		
        otilde = so.getReward(so,rgs);

    	
    	boolean randomSelect;		// true signals: the next action is a random selected one
    	randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
		
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        List<Types.ACTIONS> bestActions = new ArrayList<>();
        
        VTable = new double[acts.size()];  
        
        assert acts.size()>0 : "Oops, no available action";
        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = so.copy();

    		if (randomSelect) {
    			value = rand.nextDouble();
    		} else {
    	        if (this.getAFTERSTATE()) {
    	        	// if parameter "AFTERSTATE" is checked in ParNT, i.e. we use afterstate logic:
    	        	//
    	        	NewSO.advanceDeterministic(acts.get(i)); 	// generate the afterstate
    	        	value = this.getScore(NewSO,so); // this is V(s') from so-perspective
    	            NewSO.advanceNondeterministic(); 
    	        } else { 
    	        	// the non-afterstate logic for the case of single moves:
    	        	//System.out.println("NewSO: "+NewSO.stringDescr()+", act: "+act.toInt()); // DEBUG
    	            NewSO.advance(acts.get(i));
    	            value = this.getScore(NewSO,so); // this is V(s'') from the perspective of so
    	        }
    	        // both ways of calculating the agent score are the same for deterministic games (s'=s''),
    	        // but they usually differ for nondeterministic games.
            	
    	        rtilde = NewSO.getReward(so,rgs)-otilde;
            	if (TERNARY) {
            		value = NewSO.isGameOver() ? rtilde : getGamma()*value;
            	} else {
        	        value = rtilde + getGamma()*value;
            	}
    	        
    		}
       	
			// just a debug check:
			if (Double.isInfinite(value)) System.out.println("value(NewSO) is infinite!");
			
			value = normalize2(value,so);					
			VTable[i] = value;

			//
			// Calculate the best value and actBest.
			// If there are multiple best actions, select afterwards one of them randomly 
			// (better exploration)
			//
			if (bestValue < value) {
				bestValue = value;
                bestActions.clear();
                bestActions.add(acts.get(i));
			} else if (bestValue == value) {
                bestActions.add(acts.get(i));
			}

        }
        actBest = bestActions.get(rand.nextInt(bestActions.size()));
        // if several actions have the same best value, select one of them randomly

        assert actBest != null : "Oops, no best action actBest";
		if (!silent) {
            NewSO = so.copy();
            NewSO.advance(actBest);
			System.out.println("---Best Move: "+NewSO.stringDescr()+", "+(bestValue));
		}	
		
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, bestValue);
		return actBestVT;
	}	
	

		
	/**
	 * Return the agent's estimate of the score for that after state 
	 * For 2-player games like TTT, the score is V(), the probability that the player to move
	 * wins from that after state. V(s_t|p_t) learns this probability for every t.
	 * V(s_t|p_t) is the quantity to be maximized by getNextAction2.
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * 
	 * @param so			the state for which the value is desired
	 * @return the agent's estimate of the future score for that after state (its value)
	 */
	public double getScore(StateObservation so) {
		int[] bvec = m_Net.xnf.getBoardVector(so);
		double score = m_Net.getScoreI(bvec,so.getPlayer());
		return score;
	}

	/**
	 * Return the agent's estimate of the score for that after state 
	 * Return V(s_t|p_refer), that is the value function from the perspective of the player
	 * who moves in state {@code refer}. 
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * 
	 * @param so	the state s_t for which the value is desired
	 * @param refer	the referring state
	 * @return		V(s_t|p_refer), the agent's estimate of the future score for s_t
	 * 				from the perspective of the player in state {@code refer}
	 */
	public double getScore(StateObservation so, StateObservation refer) {
		double score;
		int[] bvec = m_Net.xnf.getBoardVector(so);
		score = m_Net.getScoreI(bvec,refer.getPlayer());
    	return score;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called by the n-ply wrappers ({@link MaxNWrapper}, {@link ExpectimaxWrapper}). 
	 * 
	 * @param so	the state s_t for which the value is desired
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of 
	 * 				the future score for s_t from the perspective of player i
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so) {
		ScoreTuple sc = new ScoreTuple(so);
		int[] bvec = m_Net.xnf.getBoardVector(so);
		switch (so.getNumPlayers()) {
		case 1: 
			sc.scTup[0] = m_Net.getScoreI(bvec,so.getPlayer());
			break;
		case 2:
			int player = so.getPlayer();
			int opponent = (player==0) ? 1 : 0;
//			sc.scTup[player] = m_Net.getScoreI(bvec,player);	// wrong before 2019-03-10
//			sc.scTup[opponent] = -sc.scTup[player];
			// 
			// This is an important bug fix (2019-03-10) for TDNTuple3Agt: 
			// If we want to get the score tuple for state 'so' where 
			// 'player' has to move, we may *NOT* ask for m_Net.getScoreI(bvec,player), 
			// because the net did never learn this, it was trained on getScore(so,refer), where
			// refer is the player who *created* 'so' (the opponent). We construct the score 
			// tuple by starting with m_Net.getScoreI(bvec,opponent), the value that bvec has 
			// for opponent, and infer from this the player's value by negation:
			// 
			sc.scTup[opponent] = m_Net.getScoreI(bvec,opponent);  
			sc.scTup[player] = 	-sc.scTup[opponent];
			break;
		default: 	
			throw new RuntimeException("getScoreTuple(StateObservation so) not available for TDNTuple3Agt and numPlayer>2");
			//
			// here we would have to add new logic in the case of 3-,4-,...,N-player games (!)
			//
		}
		
		// In any case: add the reward obtained so far, since the net predicts
		// with getScoreI only the expected future reward.
		boolean rgs = m_oPar.getRewardIsGameScore();
		for (int i=0; i<so.getNumPlayers(); i++) 
			sc.scTup[i] += so.getReward(i, rgs);
    	return sc;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. <br>
	 * Is only called when training an agent in multi-update mode AND the maximum episode length
	 * is reached. 
	 * 
	 * @param sob			the current game state
	 * @return				the agent's estimate of the final game value <b>for all players</b>. 
	 * 						The return value is a tuple containing  
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s. 
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ScoreTuple sc = new ScoreTuple(sob);
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] = sob.getReward(i, rgs);
			// this is valid, but it may be a bad estimate in games where the reward is only 
			// meaningful for game-over-states.
		return sc;
	}

	/**
	 * Adapt the n-tuple weights for state {@code sLast[curPlayer]}, the last afterstate that current
	 * player generated, towards the target derived from {@code s_next}.
	 * 
	 * @param curPlayer		the player to move in state {@code ns.getSO()}
	 * @param R				the (cumulative) reward tuple received when moving into {@code s_next}
	 * @param ns			the {@code NextState} object holds the afterstate {@code ns.getAfterState()}
	 * 						and the next state {@code s_next=ns.getNextSO()} which are observed when the 
	 * 						selected action is applied to {@code ns.getSO()}
	 */
	private void adaptAgentV(int curPlayer, ScoreTuple R, NextState ns) {
		StateObservation s_after = ns.getAfterState();
		StateObservation s_next = ns.getNextSO();
		int[] curBoard;
		double v_next,vLast,vLastNew,target;
		boolean learnFromRM = m_oPar.useLearnFromRM();
		
		// calculate v_next which is the estimated game value for the afterstate generated by
		// curPlayer from state ns.getSO(), seen from the perspective of curPlayer
		if (s_next.isGameOver()) {
			v_next = 0.0;
		} else {
			int[] nextBoard = m_Net.xnf.getBoardVector(s_after);
        	v_next = m_Net.getScoreI(nextBoard,curPlayer);
		}
		
		if (sLast[curPlayer]!=null) {
			// delta reward from curPlayer's perspective when moving into s_next
			double r_next = R.scTup[curPlayer] - rLast.scTup[curPlayer];  
        	if (TERNARY) {
        		target = s_next.isGameOver() ? r_next : getGamma()*v_next;
        	} else {
    			target = r_next + getGamma()*v_next;        		
        	}
//			if (target==-1.0) {
//				int dummy=0;
//			}
//			if (Math.abs(v_next)>0.7) {
//				int dummy=0;
//			}
        	
        	// note that curBoard is NOT the board vector of state ns.getSO(), but of state
        	// sLast[curPlayer] (one round earlier!)
			curBoard = m_Net.xnf.getBoardVector(sLast[curPlayer]); 
        	vLast = m_Net.getScoreI(curBoard,curPlayer);
        	
        	// if last action of curPlayer was a random move: 
    		if (randLast[curPlayer] && !learnFromRM && !s_next.isGameOver()) {
    			// no training, go to next move.
    			if (m_DEBG) System.out.println("random move");
    			
    			m_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET
    				
    		} else {
    			m_Net.updateWeightsTD(curBoard, curPlayer, vLast, target,r_next,ns.getSO());
    		}
    		
    		//debug only:
			if (m_DEBG) {
	    		if (s_next.isGameOver()) {
	            	vLastNew = m_Net.getScoreI(curBoard,curPlayer);
	            	int dummy=1;
	    		}
            	ScoreTuple sc1 = s_next.getGameScoreTuple();
            	ScoreTuple sc2 = this.getScoreTuple(s_next);
	    		String s1 = sLast[curPlayer].stringDescr();
	    		String s2 = s_next.stringDescr();
	    		if (target!=0.0) {//(target==-1.0) { //(s_next.stringDescr()=="XoXX-oXo-") {
	            	vLastNew = m_Net.getScoreI(curBoard,curPlayer);
	            	System.out.println(s1+" "+s2+","+vLast+"->"+vLastNew+" target="+target
	            			+" player="+(curPlayer==0 ? "X" : "O"));
	            	if (++acount % 50 ==0) {
	            		int dummy=1;
	            	}
	    		}
			}

		}  // if(sLast[..]!=null)
	
		// --- not needed here, see else-branch in finalAdaptAgents, where we do the same ---
//		if (s_next.isGameOver()) {
//			// if s_next is terminal, adapt towards target 0 the afterstate that precedes s_next 
//			curBoard = m_Net.xnf.getBoardVector(s_after); 
//        	vLast = m_Net.getScoreI(curBoard,curPlayer);
//        	target = 0.0;
//			m_Net.updateWeightsTD(curBoard, curPlayer, vLast, target,R.scTup[curPlayer],ns.getSO());
//		}
	}
	
	/**
	 * This function is called when {@code ns.getNextSO()} is terminal. It does two things:
	 * <ol>
	 * <li> It takes the final delta reward r (from the perspective of each player other than {@code curPlayer})  
	 * 		and adapts the value of the last state of each other player to this r.
	 * 		[This first part is irrelevant for 1-player games where we have no other players.]
	 * <li> It adapts {@code ns.getAfterState()}, the afterstate preceding the terminal state, towards 0. 
	 * 		[This second part is only relevant if TERNARY==false in source code. This happens currently only 
	 * 		for non-final-reward games like 2048.]
	 * </ol>
	 * 
	 * @param curPlayer the player to move in @{@code ns.getSO()} = the player who generates {@code ns.getNextSO()}
	 * @param R         the reward tuple for {@code ns.getNextSO()}
	 * @param ns		the next state object when applying action {@code a_t} in state {@code s_t}
	 */
	private void finalAdaptAgents(int curPlayer, ScoreTuple R, NextState ns) {
		double target,vLast,vLastNew;
		int[] curBoard, nextBoard;
		StateObservation s_after = ns.getAfterState();
		StateObservation s_next = ns.getNextSO(); 
		
		for (int n=0; n<numPlayers; n++) {
			if (n!=curPlayer) {
				// adapt the value of the last state sLast[n] of each player other than curPlayer
				// towards the reward received when curPlayer did his terminal move. 
				// [This if-branch is irrelevant for 1-player games.]
				// 
				if (sLast[n]!=null ) { 
					target = R.scTup[n] - rLast.scTup[n]; 		// delta reward
					curBoard = m_Net.xnf.getBoardVector(sLast[n]); 
		        	vLast = m_Net.getScoreI(curBoard,n);
		        	
	    			m_Net.updateWeightsTD(curBoard, n, vLast, target, R.scTup[n], ns.getSO());

	    			//debug only:
	    			if (m_DEBG) {
	    	    		if (s_next.isGameOver()) {
	    	            	vLastNew = m_Net.getScoreI(curBoard,n);
	    	            	ScoreTuple sc1 = s_next.getGameScoreTuple();
	    	            	ScoreTuple sc2 = this.getScoreTuple(s_next);
	    	            	int dummy=1;
	    	    		}
	    	    		String s1 = sLast[n].stringDescr();
	    	    		String s2 = s_next.stringDescr();
	    	    		if (target!=0.0) { //(target==-1.0) { //(s_next.stringDescr()=="XoXX-oXo-") {
	    	            	vLastNew = m_Net.getScoreI(curBoard,n);
	    	            	System.out.println(s1+" "+s2+","+vLast+"->"+vLastNew+" target="+target
	    	            			+" player="+(n==0 ? "X" : "O")+" (f)"+this.getGameNum());
	    	            	if (++acount % 50 ==0) {
	    	            		int dummy=1;
	    	            	}
	    	    		}
	    			}
				}
			} else { // if n==curPlayer
				// The following is equivalent to TDNTuple2Agt's call of m_Net.updateWeightsNewTerminal():
				//
				// If s_next is terminal, adapt the value of the *afterstate* that 
				// curPlayer observed after he did his final move. Adapt it towards 
				// target 0. (This is only relevant for TERNARY==false, since 
				// only then the value of this afterstate is used in getNextAction2.)
				curBoard = m_Net.xnf.getBoardVector(s_after); 	// WK/04/2019: NEW use afterstate, *not* next state
	        	vLast = m_Net.getScoreI(curBoard,curPlayer);
	        	
    			m_Net.updateWeightsTD(curBoard, curPlayer, vLast, 0.0, R.scTup[curPlayer], s_next);
				
			}
		} // for
		
	}
	
	/**
	 * Train the agent for one complete game episode <b>using self-play</b>. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so) {
		double[] VTable = null;
		double reward = 0.0;
		Types.ACTIONS_VT actBest;
		Types.ACTIONS a_next, a_t;
		int   nextPlayer, curPlayer=so.getPlayer();
		NextState ns = null;
		ScoreTuple R = new ScoreTuple(so);
		rLast = new ScoreTuple(so);

		boolean learnFromRM = m_oPar.useLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;
				
		int t=0;
		StateObservation s_t = so.copy();
		for (int n=0; n<numPlayers; n++) {
			sLast[n] = null;
		}
		int kk= (so.getPlayer()-1+numPlayers)%numPlayers;	// p=0,N=1 --> kk=0; p=0,N=2 --> kk=1
		sLast[kk] = so.getPrecedingAfterstate();
		// The player kk who generated 'so' gets so's preceding afterstate as its sLast.
		// (This is important for RubiksCube, in order to learn from the first move on in 
		// this deterministic single-player game: The player who generated 'so' is so.getPlayer()
		// itself, thus kk=0, and the preceding afterstate is 'so' itself.)
		
		m_counter=0;		// /WK/bug fix 2019-05-21
		m_finished=false;	// /WK/bug fix 2019-05-21
		do {
	        m_numTrnMoves++;		// number of train moves (including random moves)
	        
	        // choose action a_t, using epsilon-greedy policy based on V
			a_t = getNextAction2(s_t, true, true);
	               
	        // take action a_t and observe reward & next state 
	        ns = new NextState(this,s_t,a_t);	
	        curPlayer = ns.getSO().getPlayer();
	        nextPlayer = ns.getNextSO().getPlayer();
	        R = ns.getNextRewardTupleCheckFinished(epiLength);	// this sets m_finished
	        
	        adaptAgentV(curPlayer, R, ns);
	        
	        // we differentiate between the afterstate (on which we learn) and the 
	        // next state s_t, which may have environment random elements added and from which 
	        // we advance. 
	        // (for deterministic games, ns.getAfterState() and ns.getNextSO() are the same)
	        sLast[curPlayer] = ns.getAfterState();
	        randLast[curPlayer] = a_t.isRandomAction();
	        rLast.scTup[curPlayer] = R.scTup[curPlayer];
	        s_t = ns.getNextSO();
			t++;
			
//			if (m_finished) break; 		// out of while  (/WK/bug fix 2019-05-21)
//		} while(!s_t.isGameOver());
		} while(!m_finished);			// simplification: m_finished is set by ns.getNextRewardTupleCheckFinished
		
		if (FINALADAPTAGENTS) 
			finalAdaptAgents(curPlayer, R, ns);
		
		
		try {
			this.finishUpdateWeights();		// adjust learn params ALPHA & m_epsilon
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		//System.out.println("episode: "+getGameNum()+", moveNum="+m_counter);
		incrementGameNum();
		if (this.getGameNum() % 500 == 0) {
			System.out.println("gameNum: "+this.getGameNum());
		}
		
		return false;
		
	} // trainAgent


	public String stringDescr() {
		m_Net.setHorizon();
		String cs = getClass().getSimpleName();
		String str = cs + ": USESYM:" + (m_ntPar.getUSESYMMETRY()?"true":"false")
						+ ", P:" + (m_Net.getXnf().getNumPositionValues())
						+ ", NORMALIZE:" + (m_tdPar.getNormalize()?"true":"false")
						+ ", sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
						+ ", lambda:" + m_Net.getLambda()
						+ ", horizon:" + m_Net.getHorizon()
						+ ", AFTERSTATE:" + (m_ntPar.getAFTERSTATE()?"true":"false")
						+ ", learnFromRM: " + (m_oPar.useLearnFromRM()?"true":"false");
		return str;
	}
		
	public String stringDescr2() {
		String cs = getClass().getSimpleName();
		String str = cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
						+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal()
						+ ", gamma: " + m_tdPar.getGamma()
						+ ", "+stringDescrNTuple();		// see NTupleBase
		return str;
	}
		
	// Callback function from constructor NextState(NTupleAgt,StateObservation,ACTIONS). 
	// It sets various elements of NextState ns (nextReward, nextRewardTuple).
	// It is part of TDNTuple3Agt (and not part of NextState), because it uses various elements
	// private to TDNTuple3Agt (DBG_REWARD, referringState, normalize2)
	public void collectReward(NextState ns) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ns.nextRewardTuple = new ScoreTuple(ns.refer);
		for (int i=0; i<ns.refer.getNumPlayers(); i++) {
			ns.nextRewardTuple.scTup[i] = normalize2(ns.nextSO.getReward(i,rgs),ns.nextSO);
		}

		// for completeness, ns.nextReward is not really needed in TDNTuple3Agt
		ns.nextReward = normalize2(ns.nextSO.getReward(ns.nextSO,rgs),ns.refer);

		if (DBG_REWARD && ns.nextSO.isGameOver()) {
			System.out.print("Rewards: ");
			System.out.print(ns.nextRewardTuple.toString());
//			System.out.print("Reward: "+ns.nextReward);
			System.out.println("   ["+ns.nextSO.stringDescr()+"]  " + ns.nextSO.getGameScore(ns.nextSO) 
							 + " for player " + ns.nextSO.getPlayer());
		}
	}
	
}