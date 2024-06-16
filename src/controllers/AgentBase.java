package controllers;

import java.io.Serial;
import java.io.Serializable;

import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import params.ParOther;
import params.ParRB;
import params.ParWrapper;
import tools.ScoreTuple;
import tools.Types;

/**
 * Class AgentBase implements functionality of the interface {@link PlayAgent}
 * common to all agents (things related to gameNum, maxGameNum, AgentState + m_oPar).
 * 
 * @see PlayAgent
 * @see controllers.MCTS.MCTSAgentT
 */
abstract public class AgentBase implements PlayAgent, Serializable {
	/**
	 * number of performed training games for trainable agents  
	 */
	private int m_GameNum;
	/**
	 * maximum number of training games
	 */
	private int m_MaxGameNum;
	private String m_name;
	private final AgentState m_agentState = AgentState.RAW;	// deprecated, use m_oPar.agentState
	private int epochMax = 0;
	protected long m_numTrnMoves = 0L;			// moves (calls to getNextAction2) done during training
	private long durationTrainingMs = 0L;		// total time in ms used for training
	private long durationEvaluationMs = 0L;		// total time in ms used for evaluation (during training)
	protected ParOther m_oPar;
	protected ParWrapper m_wrPar;
	protected ParRB m_rbPar;					// needed only by trainable agents, but we put it here to have the code
												// only once
	public static String EGV_EXCEPTION_TEXT = "Agents derived from AgentBase have to implement this method: estimateGameValueTuple";

//	private boolean stochasticPolicy = false;

	/**
	 * change the version ID for serialization only if a newer version is no
	 * longer compatible with an older one (older .agt.zip will become
	 * unreadable, or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 12L;

	/**
	 * Default constructor for AgentBase, needed for loading a serialized
	 * version
	 */
	protected AgentBase() {
		this("none");
	}

	public AgentBase(String name) {
		this(name, new ParOther(), new ParRB());
	}

	public AgentBase(String name, ParOther oPar) {
		this(name, oPar, new ParRB(), new ParWrapper());
	}

	public AgentBase(String name, ParOther oPar, ParRB rbPar) {
		this(name, oPar, rbPar, new ParWrapper());
	}
	public AgentBase(String name, ParOther oPar, ParRB rbPar, ParWrapper wrPar) {
		m_name = name;
		m_oPar = new ParOther(oPar);
		m_rbPar = new ParRB(rbPar);
		m_wrPar = new ParWrapper(wrPar);
	}

	/**
	 * This is just to signal that derived classes will be either abstract or
	 * implement getNextAction2(), as required by the interface {@link PlayAgent} as
	 * well. 
	 */
	 abstract public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent);

//	/**
//	 * On the long run, {@link PlayAgent#getScore(StateObservation)} should become deprecated (in favor of
//	 * {@link PlayAgent#getScoreTuple(StateObservation, ScoreTuple) getScoreTuple}).
//	 * But for the moment, we leave a default implementation in AgentBase, which should
//	 * however be overridden by derived classes. The base implementation just throws an exception.
//	 *
//	 * @param sob
//	 *            the state observation object
//	 * @return the agent's estimate of the game value function
//	 */
//	public double getScore(StateObservation sob) {
//		throw new RuntimeException("AgentBase.getScore has to be overridden by derived classes!");
//	}

	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		throw new RuntimeException("Agents derived from AgentBase have to implement this method: getScoreTuple");
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final
	 * reward) <b>for all players</b>. Is called when maximum episode length
	 * (TD) or maximum tree depth for certain agents (Max-N) is reached.
	 * 
	 * <b>Important note</b>: Derived classes that use this method
	 * inside {@code getScoreTuple(so,...)}
	 * (e.g. Max-N, MC or MCTS when reaching the predefined rollout depth)
	 * have to <b>override</b> this function with a function <b>not</b> using
	 * {@code getScoreTuple(so,...)}, otherwise an
	 * infinite loop would result.
	 *
	 * @param sob
	 *            the current game state
	 * @return the agent's estimate of the final reward <b>for all players</b>.
	 *         The return value is a tuple containing
	 *         {@link StateObservation#getNumPlayers()} {@code double}'s.
	 */
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		throw new RuntimeException(EGV_EXCEPTION_TEXT);
	}

	public AgentState getAgentState() {
		return m_oPar.getAgentState();
	}

	public String getAgentFile() {
		return m_oPar.getAgentFile();
	}

	public void setAgentFile(String aFile) {
		m_oPar.setAgentFile(aFile);
	}

	public void setAgentState(AgentState aState) {
		m_oPar.setAgentState(aState);
	}

	public void resetAgent() {  }

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void incrementDurationTrainingMs(long incr) {
		this.durationTrainingMs += incr;
	}
	
	public void incrementDurationEvaluationMs(long incr) {
		this.durationEvaluationMs += incr;
	}
	
	public long getDurationTrainingMs() {
		return this.durationTrainingMs;
	}
	
	public long getDurationEvaluationMs() {
		return this.durationEvaluationMs;
	}

	@Override
	public boolean isWrapper() { return false; }

	/**
	 * @return {@code this} for unwrapped agents (that is, agents not overriding this method)
	 */
	@Override
	public PlayAgent getWrappedPlayAgent() { return this; }

	/**
	 * @see #trainAgent(StateObservation)
	 */
    @Override
	public boolean isTrainable() { return true; }
	
	/**
	 * 'Train' the agent for one complete game episode using self-play. This base training is valid for <b>all</b> agents 
	 * (except {@link HumanPlayer}), whether they are truly adaptable or not. Its purpose is 
	 * to measure <b>moves/second</b> and/or to evaluate an agent multiple times during self-play. 
	 * It is no real training, since the agent itself is not modified. 
	 * <p>
	 * Side effects: Increment m_GameNum by +1. Increments m_numTrnMoves by number of calls to getAction2. Execute a 
	 * self-play episode (with the agent-specififc {@code getNextAction2} and game-specific {@code advance}) to let the caller of this
	 * method measure how long that takes.
	 * <p>
	 * Truly adaptable agents (like {@link TDNTuple3Agt}) should override this method and program their own
	 * adaptation behavior.
	 * 
	 * @param so		the state from which the episode is played 
	 * @return			true, if agent raised a stop condition (only CMAPlayer)
	 */
	@Override
	public boolean trainAgent(StateObservation so) {
    	return trainAgent(so,this);
	}

	/**
	 * 'Train' the agent for one complete game episode. This base training is valid for <b>all</b> agents
	 * except {@link HumanPlayer}), whether they are truly adaptable or not. Its purpose is
	 * to measure <b>moves/second</b> and/or to evaluate an agent multiple times during self-play.
	 * It is no real training, since the agent itself is not modified.
	 * <p>
	 * Side effects: Increment m_GameNum and {@code acting_pa}'s gameNum by +1.
	 * <p>
	 * This method is used by the wrappers: They call it with {@code this} being the wrapped agent (it has the internal
	 * parameters) and {@code acting_pa} being the wrapper.
	 *
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @param acting_pa the agent to be called when an action is requested ({@code getNextAction2})
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)
	 */
	@Override
	public boolean trainAgent(StateObservation so, PlayAgent acting_pa) {
		Types.ACTIONS  a_t;
		StateObservation s_t = so.copy();
		boolean m_finished=false;
		int moveCounter = 0;
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;
		
		do {
	        m_numTrnMoves++;		// number of train moves
			moveCounter++;
	        
			a_t = acting_pa.getNextAction2(s_t.partialState(), true, false, true);	// choose action a_t (agent-specific behavior)
			s_t.advance(a_t, null);		// advance the state (game-specific behavior)

			if(s_t.isRoundOver()&&!s_t.isGameOver()&&s_t.isRoundBasedGame())
				s_t.initRound();
			if (s_t.isGameOver()) m_finished=true;
			if (moveCounter>=epiLength) m_finished=true;
		} while(!m_finished);

		incrementGameNum();
		acting_pa.setGameNum(this.getGameNum());
		if (this.getGameNum() % 100 == 0) {
			System.err.println("[AgentBase.trainAgent] WARNING: only dummy training (for time measurements)");
		}

		return false;		
	} 

	/**
	 * Normalize game score or reward from range [oldMin,oldMax] to range
	 * [newMin,newMax]
	 */
	protected double normalize(double reward, double oldMin, double oldMax, double newMin, double newMax) {
		reward = (reward - oldMin) * (newMax - newMin) / (oldMax - oldMin) + newMin;
		return reward;
	}

    @Override
	public String printTrainStatus() {
		return this.getClass().getSimpleName()+"::printTrain"; // dummy stub
	}

    @Override
	public boolean isRetrained() {
		return false; 	// dummy stub
	}

    @Override
	public boolean instantiateAfterLoading() {		// needed by LoadSaveTD.saveTDAgent
		// older agents may have only this.m_agentState set:
		if (this.m_agentState!=null && m_oPar.getAgentState()==null)
			m_oPar.setAgentState(this.m_agentState);

		// older agents may not have the wrapper depth parameter, so it is 0. Set it in this case to -1:
		if (this.getParOther().getWrapperMCTS_depth()==0) this.getParOther().setWrapperMCTS_depth(-1);
		// older agents may not have the wrapper p_UCT parameter, so it is 0. Set it in this case to 1.0:
		if (this.getParOther().getWrapperMCTS_PUCT()==0) this.getParOther().setWrapperMCTS_PUCT(1.0);
		// older agents may not have the agent state, so it is null. Set it to TRAINED:
		if (this.getParOther().getAgentState()==null) this.getParOther().setAgentState(AgentState.TRAINED);

		// older agents may not have ParWrapper. If so, set a default ParWrapper and copy the wrapper params
		// from ParOther over to it.
		if (this.getParWrapper()==null) {
			this.setDefaultParWrapper(getParOther());
		}

		return true;
	}
	
    @Override
	public void fillParamTabsAfterLoading(int n, Arena m_arena) {
		setWrapperParamsOfromWr(this.getParWrapper());
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
		m_arena.m_xab.setWrapperParFrom(n, this.getParWrapper());
		m_arena.m_xab.setRBParFrom(n, this.getParReplay());
	}
	
    @Override
	public byte getSize() {
		return 1;  		// dummy stub (for size of agent, see LoadSaveTD.saveTDAgent)
	} 

    @Override
	public int getGameNum() {
		return m_GameNum;
	}

    @Override
	public void setGameNum(int num) {
		m_GameNum = num;
	}

	public void incrementGameNum() {
		m_GameNum++;
	}

    @Override
	public int getMaxGameNum() {
		return m_MaxGameNum;
	}

    @Override
	public void setMaxGameNum(int num) {
		m_MaxGameNum = num;
	}

    @Override
	public long getNumLrnActions() {
		if (isWrapper()) return getWrappedPlayAgent().getNumLrnActions();
		return m_numTrnMoves; // dummy stub for agents which are no wrapper and do not override this method
	}

    @Override
	public long getNumTrnMoves() {
		if (isWrapper()) return getWrappedPlayAgent().getNumTrnMoves();
		return m_numTrnMoves; // dummy stub for agents which are no wrapper and do not override this method
	}

    @Override
	public int getMoveCounter() {
		return 0;
	}

	/**
	 * @return During training: Call the Evaluator after this number of training games
	 */
    @Override
	public int getNumEval() {
		return m_oPar.getNumEval();
	}

    @Override
	public void setNumEval(int num) {
		m_oPar.setNumEval(num);
	}

    @Override
	public void setStopEval(int num) {
		m_oPar.setStopEval(num);
	}

	/**
	 * Set the wrapper part + stopEval in {@link ParOther} m_oPar from {@code otherPar}
	 * (needed to store {@link ParOther} from environment in the agent)
	 */
    @Override
	public void setWrapperParamsO(ParOther otherPar) {
		m_oPar.setWrapperNPly(otherPar.getWrapperNPly());
		m_oPar.setWrapperMCTS_PUCT(otherPar.getWrapperMCTS_PUCT());
		m_oPar.setWrapperMCTS_depth(otherPar.getWrapperMCTS_depth());
		m_oPar.setWrapperMCTSIterations(otherPar.getWrapperMCTSIterations());
		m_oPar.setStopEval(otherPar.getStopEval());
	}

	/**
	 * Just for safety, make the wrapper params in {@link ParOther} m_oPar the same as the
	 * wrapper params in {@link ParWrapper} wrPar. Should be obsolete in principle, because we do not
	 * use any longer the wrapper params in {@link ParOther}. But we keep them for some time to be able
	 * to load older agents.
	 * @param wrPar wrapper params
	 */
	@Override
	public void setWrapperParamsOfromWr(ParWrapper wrPar) {
		m_oPar.setWrapperNPly(wrPar.getWrapperNPly());
		m_oPar.setWrapperMCTS_PUCT(wrPar.getWrapperMCTS_PUCT());
		m_oPar.setWrapperMCTS_depth(wrPar.getWrapperMCTS_depth());
		m_oPar.setWrapperMCTSIterations(wrPar.getWrapperMCTS_iterations());
	}

	@Override
	public void setParOther(ParOther op) { m_oPar.setFrom(op); 	}

	@Override
	public void setParReplay(ParRB prb) {
		m_rbPar.setFrom(prb);
	}

	@Override
	public void setParWrapper(ParWrapper pwr) {
		m_wrPar.setFrom(pwr);
	}

	/**
	 * Set defaults for m_oPar (needed in {@link Arena#loadAgent} when
	 * loading older agents, where m_oPar=null in the saved version).
	 */
	public void setDefaultParOther() {
		m_oPar = new ParOther();
	}

	/**
	 * Set defaults for m_rbPar (needed in {@link Arena#loadAgent} when
	 * loading older agents, where m_rbPar=null in the saved version).
	 */
	public void setDefaultParReplay() {
		m_rbPar = new ParRB();
	}

	/**
	 * Set defaults for {@code m_wrPar} (called from {@link Arena#loadAgent} and {@link AgentBase#instantiateAfterLoading()} when
	 * loading older agents, where {@code m_wrPar=null} in the saved version).
	 * Take over the wrapper parameters from {@link ParOther} {@code oPar} (usually = {@code this.m_oPar}).
	 */
	public void setDefaultParWrapper(ParOther oPar) {
		assert m_wrPar==null : "Oops, m_wrPar is not null!";
		m_wrPar = new ParWrapper();		// takes over some defaults from ConfigWrapper

		//m_wrPar.setParamDefaults(m_name,gameName); // currently, we do not have gameName here

		// since m_wrPar was null when calling this method, take over wrapper parameters from ParOther oPar
		m_wrPar.setWrapperNPly(oPar.getWrapperNPly());
		m_wrPar.setWrapperMCTS_iterations(oPar.getWrapperMCTSIterations());
		m_wrPar.setWrapperMCTS_PUCT(oPar.getWrapperMCTS_PUCT());
		m_wrPar.setWrapperMCTS_depth(oPar.getWrapperMCTS_depth());

		if (m_wrPar.getWrapperNPly()>0) m_wrPar.setWrapperMode(1); 				// Max-N wrapper
		if (m_wrPar.getWrapperMCTS_iterations()>0) m_wrPar.setWrapperMode(2); 	// MCTSWrapperAgent

	}

	@Override
	public ParOther getParOther() {
		return m_oPar;
	}
	@Override
	public ParRB getParReplay() {
		return m_rbPar;
	}
	@Override
	public ParWrapper getParWrapper() {
		return m_wrPar;
	}

	/**
	 * Check whether all agents in paVector are valid (non-null) and trained.
	 * Otherwise, a RuntimeException is thrown.
	 * 
	 * @param paVector
	 *            vector of all agents in {@link games.Arena}
	 * @param numPlayers
	 *            number of players
	 */
	public static void validTrainedAgents(PlayAgent[] paVector, int numPlayers) throws RuntimeException {
		PlayAgent pa;
		String nStr;
		for (int n = 0; n < paVector.length; n++) {
			pa = paVector[n];
			nStr = (numPlayers == 2)
				 ?  Types.GUI_2PLAYER_NAME[n]
				 :	Types.GUI_PLAYER_NAME[n];
			if (pa == null) {
				throw new RuntimeException("Cannot execute command. Agent for player " + nStr + " is null!");
			}
 			if (pa.getAgentState() != AgentState.TRAINED) {
				throw new RuntimeException(
						"Cannot execute command. Agent " + pa.getName() + " for player " + nStr + " is not trained!");
			}
		}
	}

	/**
	 * Check whether pa is a valid (non-null) agent.
	 * Otherwise, a RuntimeException is thrown.
	 *
	 * @param pa
	 *            vector of all agents in {@link games.Arena}
	 * @param numPlayers
	 *            number of players
	 */
	public static void validSaveAgent(PlayAgent pa, int n, int numPlayers) throws RuntimeException {
		String nStr = (numPlayers == 2)
				?  Types.GUI_2PLAYER_NAME[n]
				:	Types.GUI_PLAYER_NAME[n];
		if (pa == null) {
			throw new RuntimeException("Cannot execute command. Agent for player " + nStr + " is null!");
		}
// the check on TRAINED is not wanted here: we want to save agents in state INIT (agent stubs for GitHub)
//			if (pa.getAgentState() != AgentState.TRAINED) {
//				throw new RuntimeException(
//						"Cannot execute command. Agent " + pa.getName() + " for player " + nStr + " is not trained!");
//			}
	}

	public int getEpochMax() {
		return epochMax;
	}

	public void setEpochMax(int epochMax) {
		this.epochMax = epochMax;
	}

    @Override
	public String stringDescr() { return getClass().getSimpleName(); }
	
    @Override
	public String stringDescr2() {
		return getClass().getName() + ":";
	}

	// --- never used ---
//	@Override
//	public boolean isStochastic() {
//		return stochasticPolicy;
//	}
//
//	@Override
//	public void setStochastic(boolean hasStochasticPolicy) {
//		stochasticPolicy = hasStochasticPolicy;
//	}
}
