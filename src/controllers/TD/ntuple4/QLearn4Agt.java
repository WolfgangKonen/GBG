package controllers.TD.ntuple4;

import agentIO.LoadSaveGBG;
import controllers.AgentBase;
import controllers.ExpectimaxNWrapper;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.GameBoard;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleFuncs;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * The Q-learning {@link PlayAgent} <b>with n-tuples</b>.
 * {@link QLearn4Agt} differs from {@link Sarsa4Agt} currently only by a slightly different logic in
 * {@link #adaptAgentQ(int, ScoreTuple, NextState4)}.
 *
 * It has a one-layer (perceptron-like) neural network with or without output-nonlinearity  
 * {@code tanh} to model the Q-function. 
 * The net follows closely the (pseudo-)code by [SuttonBarto98]. 
 * <p>
 * Some functionality is packed in the superclasses 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...) and
 * {@link NTuple4Base} (finishUpdateWeights, increment*Counters, isTrainable, normalize2, ...)
 * <p>
 * {@link QLearn4Agt} is an alternative to {@link TDNTuple4Agt}.
 * The differences between {@link QLearn4Agt} and {@link TDNTuple4Agt} are:
 * <ul>
 * <li> {@link QLearn4Agt} updates the value of a state for a player based on the value/reward
 * 		that the <b>same</b> player achieves in his next turn. It is in this way more similar to 
 * 		{@link TDNTuple4Agt}. (Note that the updates of {@link TDNTuple4Agt} are based on the value/reward of
 * 		the <b>next state</b>. This may require sign change, depending on the number of players.)
 * 		Thus {@link QLearn4Agt} is much simpler to generalize to 1-, 2-, 3-, ..., N-player games
 * 		than {@link TDNTuple4Agt}.
 * <li> Eligible states: {@link QLearn4Agt} updates with ELIST_PP=true, i.e. it has a separate
 * 		{@code eList[p]} per player p. {@link QLearn4Agt} uses only one common {@code eList[0]}.
 * 		Only relevant for LAMBDA &gt; 0. 
 * </ul>
 * The similarities of {@link QLearn4Agt} and {@link TDNTuple4Agt} are:
 * <ul>
 * <li> No eligibility traces, instead LAMBDA-horizon mechanism of [Jaskowski16] (faster and less
 * 		memory consumptive).
 * <li> Option AFTERSTATE (relevant only for nondeterministic games like 2048), which builds the value 
 * 		function on the argument afterstate <b>s'</b> (before adding random element) instead 
 * 		of next state <b>s''</b> (faster learning and better generalization).
 * <li> Has the random move rate bug fixed: Now EPSILON=0.0 means really 'no random moves'.
 * <li> Learning rate ALPHA differently scaled: if ALPHA=1.0, the new value for a
 * 		state just trained will be exactly the target. Therefore, recommended ALPHA values are 
 * 		m*N_s bigger than in {@code TDNTupleAgt}, where m=number of n-tuples, N_s=number of 
 * 		symmetric (equivalent) states. 
 * <li> A change in the update formula: when looping over different equivalent
 * 		states, at most one update per index is allowed (see comment in {@link NTuple4} for
 * 		member {@code indexList}).
 * </ul>
 * 
 * @see PlayAgent
 * @see AgentBase
 * @see NTuple4Base
 * 
 * @author Wolfgang Konen, TH Koeln, Dec'18
 */
public class QLearn4Agt extends NTuple4Base implements PlayAgent, NTuple4Agt,Serializable {

	private EligType m_elig;

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;

	private int numPlayers;
	transient private StateObservation[] sLast;	// last state of player p
	transient private ACTIONS[] aLast;	// last action of player p
	transient private boolean[] randLast;		// whether last action of player p was a random action
	transient private ScoreTuple rLast;

	private int numOutputs;
	private int actionIndexMin;
	private int actionIndexMax;

	private boolean RANDINITWEIGHTS = false;// If true, init weights of value function randomly

	private boolean m_DEBG = false;
	// debug printout in collectReward:
	public static boolean DBG_REWARD=false;

	// is set to true in getNextAction2(...), if the next action is a random selected one:
	boolean randomSelect = false;

	// use finalAdaptAgents(...), normaly true. Set only to false if you want to test how agents behave otherwise:
	private boolean FINALADAPTAGENTS=true;


	private int acount=0;
	/**
	 * Default constructor for {@link QLearn4Agt}, needed for loading a serialized version
	 */
	public QLearn4Agt() throws IOException {
		super();
		ParTD tdPar = new ParTD();
		ParNT ntPar = new ParNT();
		ParOther oPar = new ParOther();
		initNet(ntPar, tdPar, oPar, null, null, 1, 1000);
	}

	/**
	 * Create a new {@link QLearn4Agt}
	 *
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param allAvailActions	neede to infer the number of outputs of the n-tuple network
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	public QLearn4Agt(String name, ParTD tdPar, ParNT ntPar, ParOther oPar,
                      int[][] nTuples, XNTupleFuncs xnf, ArrayList<ACTIONS> allAvailActions, int maxGameNum) throws IOException {
		super(name);
		this.numPlayers = xnf.getNumPlayers();
		this.sLast = new StateObservation[numPlayers];
		this.aLast = new ACTIONS[numPlayers];
		this.randLast = new boolean[numPlayers];
		this.setMaxGameNum(maxGameNum);
		processAvailActions(allAvailActions);		// calc members actionIndexMin, actionIndexMax, numOutputs
		initNet(ntPar,tdPar,oPar, nTuples, xnf, numOutputs, maxGameNum);			
	}

	/** 
	 * Infer members actionIndexMin, actionIndexMax, numOutputs from allAvailActions
	 * @param allAvailActions
	 */
	private void processAvailActions(ArrayList<ACTIONS> allAvailActions) {
		ListIterator<ACTIONS> iter = allAvailActions.listIterator();
		this.actionIndexMin = Integer.MAX_VALUE;
		this.actionIndexMax = Integer.MIN_VALUE;
		while (iter.hasNext()) {
			int key = ((ACTIONS) iter.next()).toInt();
			if (key<actionIndexMin) actionIndexMin=key;
			if (key>actionIndexMax) actionIndexMax=key;
		}
		this.numOutputs = (actionIndexMax+1);
		
		if (actionIndexMin < 0) {
			System.err.println("*** Warning: actionIndexMin="+actionIndexMin+" is < 0 ***");
			System.err.println("This does not work for SarsaAgt and all member funcs of NTuple2ValueFunc.");
		}
		
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
			int[][] nTuples, XNTupleFuncs xnf, int numOutputs, int maxGameNum) throws IOException {
		m_tdPar = new ParTD(tdPar);
		m_ntPar = new ParNT(ntPar);
		m_oPar = new ParOther(oPar);		// m_oPar is in AgentBase
		m_elig = (m_tdPar.getEligMode()==0) ? EligType.STANDARD : EligType.RESET;
		rand = new Random(System.currentTimeMillis()); //(System.currentTimeMillis());		(42); 

		int[] posVals = xnf.getPositionValuesVector();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple4ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells,numOutputs);
		
		setNTParams(ntPar);
		setTDParams(tdPar, maxGameNum);
		m_Net.setHorizon();
		
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
		// set horizon cut for older agents (where horCut was not part of ParTD):
		if (this.getParTD().getHorizonCut()==0.0) 
			this.getParTD().setHorizonCut(0.1);
		
		// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
		// (they would stay otherwise at their default values, would not 
		// get the loaded values)
		this.setTDParams(this.getParTD(), this.getMaxGameNum());
		this.setNTParams(this.getParNT());
		this.weightAnalysis(null);
		
		// initialize transient members (in case a further training should take place --> see ValidateAgent) 
		this.sLast = new StateObservation[numPlayers];
		this.aLast = new ACTIONS[numPlayers];
		this.randLast = new boolean[numPlayers];
		this.m_Net.instantiateAfterLoading();   // instantiate transient eList and nTuples
		
		return true;
	}
	
	/**
	 * Get the best next action and return it 
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the Q value for each available
	 * action (as returned by so.getAvailableActions()) and the Q value for the best action actBest.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i, j;
		double bestQValue;
        double qValue=0;			// the quantity to be maximized
		StateObservation NewSO;
        ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
    	bestQValue = -Double.MAX_VALUE;
		double[] VTable;		
		
        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
		
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        List<ACTIONS> bestActions = new ArrayList<>();
        
        VTable = new double[acts.size()];  
        
        assert acts.size()>0 : "Oops, no available action";
        for(i = 0; i < acts.size(); ++i)
        {
    		if (randomSelect) {
    			qValue = rand.nextDouble();
    		} else {
    			//
    			// TODO: currently we cannot mirror in Q-learning the afterstate logic 
    			// that we have optionally in TDNTuple4Agt
    			
        		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(so, m_Net.xnf);
            	qValue = m_Net.getQFunc(curSOWB,so.getPlayer(),acts.get(i));
            	
            	// It is a bit funny, that the decision is made based only on qValue, not 
            	// on the reward we might receive for action a=acts.get(i). So an action leading to 
            	// a win (high reward) will not be taken on first encounter. But subsequently, via 
            	// finalAdaptAgents, the reward will push up the qValue for such a winning (so,a) 
            	// and lets the winning action be selected on next pass through this state. 
            	// The advantage of this is, that we do not need to execute all actions (to test 
            	// if they have a reward) in this loop --> so we are faster.
            	
    		}
       	
			// just a debug check:
			if (Double.isInfinite(qValue)) System.out.println("getQFunc(so,a) is infinite!");
			
			qValue = normalize2(qValue,so);					
			VTable[i] = qValue;

			//
			// Calculate the best Q value and actBest.
			// If there are multiple best actions, select afterwards one of them randomly 
			// (better exploration)
			//
			if (bestQValue < qValue) {
				bestQValue = qValue;
                bestActions.clear();
                bestActions.add(acts.get(i));
			} else if (bestQValue == qValue) {
                bestActions.add(acts.get(i));
			}

        }
        assert bestActions.size()>0; 
        actBest = bestActions.get(rand.nextInt(bestActions.size()));
        // if several actions have the same best Q value, select one of them randomly

        assert actBest != null : "Oops, no best action actBest";
		NewSO = so.copy();
		NewSO.advance(actBest);

		ScoreTuple scBest = so.getStoredBestScoreTuple();
				// This is the previous tuple, only relevant in case N>=3. If so.getStoredBestScoreTuple() encounters
				// null pointers, it returns an all-zeros-tuple with length so.getNumPlayers().
		scBest.scTup[so.getPlayer()] = bestQValue;
		if (so.getNumPlayers()==2) {			// the following holds for 2-player, zero-sum games:
			int opponent = 1-so.getPlayer();
			scBest.scTup[opponent] = -bestQValue;
		}
		// --- old version, before 2021-09-10 ---
//		ScoreTuple prevTuple = new ScoreTuple(so);	// a surrogate for the previous tuple, needed only in case N>=3
//		// /WK/ a quick hack for Blackjack, should be replaced later by s.th. more general !!!
//		ScoreTuple scBest = new ScoreTuple(1); //this.getScoreTuple(NewSO, prevTuple);
		if (!silent) {
			System.out.println("---Best Move: "+NewSO.stringDescr()+", "+(bestQValue));
		}

		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, bestQValue,scBest);
		return actBestVT;
	}	
	

		
	/**
	 * Return the agent's estimate of the score for that after state 
	 * For 2-player games like TTT, the score is V(), the probability that 
	 * X (Player +1) wins from that after state. V(s_t|p_t) learns this probability for every t.
	 * p_t*V(s_t) is the quantity to be maximized by getNextAction2.
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * 
	 * @param so			the state for which the value is desired
	 * @return the agent's estimate of the future score for that after state
	 */
	public double getScore(StateObservation so) {
		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(so,m_Net.xnf);
		double score = m_Net.getScoreI(curSOWB,so.getPlayer());
		return score;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called by the n-ply wrappers ({@link MaxN2Wrapper}, {@link ExpectimaxNWrapper}).
	 * @param so	the state s_t for which the value is desired
	 * 
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of 
	 * 				the future score for s_t from the perspective of player i
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		throw new RuntimeException("getScoreTuple(StateObservation so) not available for SarsaAgt");			        		
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
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ScoreTuple sc = new ScoreTuple(sob);
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] = sob.getReward(i, rgs);
			// this is valid, but it may be a bad estimate in games where the reward is only 
			// meaningful for game-over-states.
		return sc;
	}

	/**
	 * 
	 * @param nextPlayer	the player to move in state {@code s_next=ns.getNextSO()}
	 * @param R				the (cumulative) reward tuple received when moving into {@code s_next}
	 * @param ns			the {@code NextState} object holds the afterstate {@code ns.getAfterState()}
	 * 						and the next state {@code s_next=ns.getNextSO()}
	 * @return {@code a_next}, the action to perform in state {@code s_next} when following the 
	 * 						(epsilon-greedy) policy derived from Q
	 */
	private ACTIONS adaptAgentQ(int nextPlayer, ScoreTuple R, NextState4 ns) {
		ACTIONS a_next=null;
		StateObservation s_after = ns.getAfterState();
		StateObservation s_next = ns.getNextSO();
		ArrayList<ACTIONS> acts = s_next.getAvailableActions();
		int[] curBoard;
		double qValue,mValue=-Double.MAX_VALUE,qLast,qLastNew,target;
		boolean learnFromRM = m_oPar.getLearnFromRM();

		if (s_next.isGameOver()) {
			a_next = null;
			mValue = 0.0;
		} else {
			for(int i = 0; i < acts.size(); ++i)
			{
				if (randomSelect) {
					qValue = rand.nextDouble();
				} else {
					//
					// TODO: currently we cannot mirror in Q-learning the afterstate logic
					// that we have optionally in TDNTuple4Agt
					a_next = acts.get(i);
					StateObsWithBoardVector nextSOWB = new StateObsWithBoardVector(s_next,m_Net.xnf);	// WK: NEW: next state instead of afterstate
					qValue = m_Net.getQFunc(nextSOWB,nextPlayer,a_next);

				}

				//
				// Calculate mValue as the best Q value reachable for s_next.
				//
				if (mValue < qValue) {
					mValue = qValue;
				}

			}
			a_next = getNextAction2(s_next.partialState(),true,true);
//			int[] nextBoard = m_Net.xnf.getBoardVector(s_after);
		}
		
		if (sLast[nextPlayer]!=null) {
			assert aLast[nextPlayer] != null : "Ooops, aLast[nextPlayer] is null!";
			double r_next = R.scTup[nextPlayer] - rLast.scTup[nextPlayer];  // delta reward
			target = r_next + getGamma()*mValue;

        	// note that curBoard is NOT the board vector of state ns.getSO(), but of state
        	// sLast[curPlayer] (one round earlier!)
    		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(sLast[nextPlayer], m_Net.xnf);
			curBoard = curSOWB.getBoardVector().bvec; 
        	qLast = m_Net.getQFunc(curSOWB,nextPlayer,aLast[nextPlayer]);
        	
        	// if last action of nextPlayer was a random move: 
    		if (randLast[nextPlayer] && !learnFromRM && !s_next.isGameOver()) {
    			// no training, go to next move.
    			if (m_DEBG) System.out.println("random move");
    			
    			m_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET
    				
    		} else {
//            	nextBoard = m_Net.xnf.getBoardVector(s_after);
    			m_Net.updateWeightsQ(curSOWB, nextPlayer, aLast[nextPlayer], qLast,
    					r_next,target,ns.getSO());
    		}
    		
    		//debug only:
			if (m_DEBG) {
	    		if (s_next.isGameOver()) {
	            	qLastNew = m_Net.getQFunc(curSOWB,nextPlayer,aLast[nextPlayer]);
	            	int dummy=1;
	    		}
	    		String s1 = sLast[nextPlayer].stringDescr();
	    		String s2 = s_next.stringDescr();
	    		if (target<0.0) {//(target==-1.0) { //(s_next.stringDescr()=="XoXX-oXo-") {
	            	qLastNew = m_Net.getQFunc(curSOWB,nextPlayer,aLast[nextPlayer]);
	            	int actionKey = aLast[nextPlayer].toInt();
	            	System.out.println(s1+" "+s2+","+qLast+"->"+qLastNew+" target="+target
	            			+" player="+(nextPlayer==0 ? "X" : "o")+" aLast="+actionKey);
	            	if (++acount % 50 ==0) {
	            		int dummy=1;
	            	}
	    		}
	    		if (s_next.stringDescr()=="XooX-o-XX") {
	    			System.out.println(this.getGameNum()+" target="+target);
	    			int dummy=1;
	    		}
			}

		}  // if(sLast[..]!=null)
		return a_next;
	}
	
	/**
	 * This function is called when {@code ns.getNextSO()} is terminal. 
	 * It takes the final delta reward r (from the perspective of each player other than {@code nextPlayer})  
	 * and adapts the value of the last state of each other player to this r.
	 * [This method is irrelevant for 1-player games where we have no other players.]
	 * 
	 * @param nextPlayer the player to move in {@code ns.getNextSO()} 
	 * @param R          the reward tuple for {@code ns.getNextSO()}
	 * @param ns		 the next state object when applying action {@code a_t} in state {@code s_t}
	 */
	private void finalAdaptAgents(int nextPlayer, ScoreTuple R, NextState4 ns) {
		double target,qLast,qLastNew;
		int[] curBoard, nextBoard;
//		StateObservation s_after = ns.getAfterState();
		StateObservation s_next = ns.getNextSO();
		
		for (int n=0; n<numPlayers; n++) {
			if (n!=nextPlayer) {
				if (sLast[n]!=null ) { 
					assert aLast[n] != null : "Ooops, aLast[n] is null!";
					target = R.scTup[n] - rLast.scTup[n]; 		// delta reward
			        // TODO: think whether the subtraction rlast.scTup[n] is right for every n
					//		 (or whether we need to correct rLast before calling finalAdaptAgents)
		    		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(sLast[n], m_Net.xnf);
					curBoard = curSOWB.getBoardVector().bvec; 
		        	qLast = m_Net.getQFunc(curSOWB,n,aLast[n]);
		        	
	    			m_Net.updateWeightsQ(curSOWB, n, aLast[n], qLast,
	    					R.scTup[n],target,ns.getSO());

	    			//debug only:
	    			if (m_DEBG) {
		        		if (s_next.isGameOver()) {
		                	qLastNew = m_Net.getQFunc(curSOWB,n,aLast[n]);
		                	int dummy=1;
		        		}
		        		String s1 = sLast[n].stringDescr();
		        		String s2 = s_next.stringDescr();
		        		if (target!=0.0) {//(target==-1.0) { 
		                	qLastNew = m_Net.getQFunc(curSOWB,n,aLast[n]);
		                	int actionKey = aLast[n].toInt();
		                	System.out.println(s1+" "+s2+","+qLast+"->"+qLastNew+" target="+target
		                			+" player="+(n==0 ? "X" : "o")+" aLast="+actionKey);
		                	if (++acount % 50 ==0) {
		                		int dummy=1;
		                	}
		        		}
	    			}
				}
			}
		}
		
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
		Types.ACTIONS a_next;
		int   nextPlayer=so.getPlayer();
		NextState4 ns = null;
		ScoreTuple R = new ScoreTuple(so);
		rLast = new ScoreTuple(so);

//		boolean learnFromRM = m_oPar.useLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;
				
		int t=0;
		StateObservation s_t = so.copy();
		Types.ACTIONS_VT a_t_vt = getNextAction2(s_t.partialState(), true, true);
		ACTIONS a_t = (ACTIONS) a_t_vt;
		for (int n=0; n<numPlayers; n++) {
			sLast[n] = (n==nextPlayer ? s_t : null);	// nextPlayer is X=so.getPlayer()
			aLast[n] = (n==nextPlayer ? a_t : null);	// sLast[X]=so is the state on which X has to act 
		}
		do {
	        m_numTrnMoves++;		// number of train moves (including random moves)
	               
	        // take action a_t and observe reward & next state 
	        ns = new NextState4(this,s_t,a_t_vt);
	        nextPlayer = ns.getNextSO().getPlayer();
	        R = ns.getNextRewardTupleCheckFinished(epiLength);
	        
	        a_next = adaptAgentQ(nextPlayer, R, ns);
	        
	        // We *cannot* differentiate between the afterstate and next state in SARSA learning, 
	        // because the action to perform in state s_t has to be based on the next state s_t
	        // (and not the afterstate before it, where e.g. in 2048 the random tile was not yet added, 
	        // because the action to chose may be influenced by the random tile).
	        // For deterministic games, ns.getAfterState() and ns.getNextSO() are the same.
	        //
//	        sLast[nextPlayer] = ns.getAfterState(); 		// the afterstate generated by curPlayer
	        sLast[nextPlayer] = s_t = ns.getNextSO(); 		// WK: NEW the *next* state generated by curPlayer
	        aLast[nextPlayer] = a_t = a_next;
	        rLast.scTup[nextPlayer] = R.scTup[nextPlayer];
	        randLast[nextPlayer] = (a_next==null ? false : a_next.isRandomAction());
			 					  //a_next is null if ns.getNextSO() is terminal  	
			t++;
			
		} while(!s_t.isGameOver());
		
		if (FINALADAPTAGENTS) 
			finalAdaptAgents(nextPlayer, R, ns);
		
		
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


    @Override
	public String stringDescr() {
		m_Net.setHorizon();
		String cs = getClass().getSimpleName();
		String str = cs + ": USESYMMETRY:" + (m_ntPar.getUSESYMMETRY()?"true":"false")
						+ ", NORMALIZE:" + (m_tdPar.getNormalize()?"true":"false")
						+ ", sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
						+ ", lambda:" + m_Net.getLambda()
						+ ", horizon:" + m_Net.getHorizon()
						+ ", AFTERSTATE:" + (m_ntPar.getAFTERSTATE()?"true":"false")
						+ ", learnFromRM: " + (m_oPar.getLearnFromRM()?"true":"false");
		return str;
	}
		
    @Override
	public String stringDescr2() {
		String cs = getClass().getSimpleName();
		String str = cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
						+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal()
						+ ", gamma: " + m_tdPar.getGamma();
		return str;
	}
		
	// Callback function from constructor NextState(NTupleAgt,StateObservation,ACTIONS). 
	// It sets various elements of NextState ns (nextReward, nextRewardTuple).
	// It is part of SarsaAgt (and not part of NextState), because it uses various elements
	// private to SarsaAgt (DBG_REWARD, referringState, normalize2)
	public void collectReward(NextState4 ns) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ns.nextRewardTuple = new ScoreTuple(ns.refer);
		for (int i=0; i<ns.refer.getNumPlayers(); i++) {
			ns.nextRewardTuple.scTup[i] = normalize2(ns.nextSO.getReward(i,rgs),ns.nextSO);
		}

		// for completeness, ns.nextReward is not really needed in SarsaAgt
		ns.nextReward = normalize2(ns.nextSO.getReward(ns.nextSO.getPlayer(),rgs),ns.refer);

		if (DBG_REWARD && ns.nextSO.isGameOver()) {
			System.out.print("Rewards: ");
			System.out.print(ns.nextRewardTuple.toString());
//			System.out.print("Reward: "+ns.nextReward);
			System.out.println("   ["+ns.nextSO.stringDescr()+"]  " + ns.nextSO.getGameScore(ns.nextSO.getPlayer())
							 + " for player " + ns.nextSO.getPlayer());
		}
	}
	
}