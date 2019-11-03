package controllers;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JOptionPane;

import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import controllers.TD.TDAgent;
import games.Arena;
import games.StateObservation;
import games.XArenaMenu;
import params.ParOther;
import tools.MessageBox;
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
	private AgentState m_agentState = AgentState.RAW;
	private int epochMax = 0;
	protected long m_numTrnMoves = 0L;
	private long durationTrainingMs = 0L;		// total time in ms used for training
	private long durationEvaluationMs = 0L;		// total time in ms used for evaluation (during training)
	protected ParOther m_oPar = new ParOther();
	public static String EGV_EXCEPTION_TEXT = "Agents derived from AgentBase have to implement this method: estimateGameValueTuple";

	/**
	 * change the version ID for serialization only if a newer version is no
	 * longer compatible with an older one (older .agt.zip will become
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	/**
	 * Default constructor for AgentBase, needed for loading a serialized
	 * version
	 */
	protected AgentBase() {
		this("none");
	}

	public AgentBase(String name) {
		m_name = name;
	}

	public AgentBase(String name, ParOther oPar) {
		m_name = name;
		m_oPar = new ParOther(oPar);
	}

	/**
	 * This is just to signal that derived classes will be either abstract or
	 * implement getNextAction2(), as required by the interface {@link PlayAgent} as
	 * well. 
	 */
	 abstract public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent);

	/**
	 * This is just to signal that derived classes will be either abstract or
	 * implement getScore(), as required by the interface {@link PlayAgent} as
	 * well. 
	 * 
	 * @param sob
	 *            the state observation object
	 * @return the agent's estimate of the game value function
	 */
	abstract public double getScore(StateObservation sob);

	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		throw new RuntimeException("Agents derived from AgentBase have to implement this method: getScoreTuple");
	}

	/**
	 * Return the estimated game value for {@link StateObservation} sob. The
	 * default behavior is to return {@link #getScore(StateObservation)}.
	 * <p>
	 * 
	 * <b>Important note</b>: Derived classes that use
	 * {@link #estimateGameValue} inside {@link #getScore(StateObservation)}
	 * (e.g. Max-N, MC or MCTS when reaching the predefined rollout depth)
	 * have to <b>override</b> this function with a function <b>not</b> using
	 * {@link #getScore(StateObservation)}, otherwise an infinite loop would
	 * result.
	 * 
	 * @param sob
	 *            the state observation object
	 * @return {@link #getScore(StateObservation)}, that is whatever the derived
	 *         class implements for {@link #getScore(StateObservation)}.
	 */
	@Deprecated
	public double estimateGameValue(StateObservation sob) {
//		return getScore(sob);
		return this.estimateGameValueTuple(sob, null).scTup[sob.getPlayer()];
	};

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final
	 * reward) <b>for all players</b>. Is called when maximum episode length
	 * (TD) or maximum tree depth for certain agents (Max-N) is reached.
	 * 
	 * <b>Important note</b>: Derived classes that use
	 * {@link #estimateGameValueTuple} inside {@link #getScore(StateObservation)}
	 * (e.g. Max-N, MC or MCTS when reaching the predefined rollout depth)
	 * have to <b>override</b> this function with a function <b>not</b> using
	 * {@link #getScore(StateObservation)}, otherwise an infinite loop would
	 * result.
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
		return m_agentState;
	}

	public void setAgentState(AgentState aState) {
		m_agentState = aState;
	}

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
	
	public boolean trainAgent(StateObservation so) {
		m_GameNum++;
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

	public String printTrainStatus() {
		return this.getClass().getSimpleName()+"::printTrain"; // dummy stub
	}

	public boolean isTrainable() {
		return false; 	// dummy stub
	}

	public boolean isRetrained() {
		return false; 	// dummy stub
	}

	public boolean instantiateAfterLoading() { 
		return true; 	// dummy stub, see LoadSaveTD.saveTDAgent
	}
	
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		; 				// dummy stub, see XArenaMenu.loadAgent
	}
	
	public byte getSize() {
		return 1;  		// dummy stub (for size of agent, see LoadSaveTD.saveTDAgent)
	} 

	public int getGameNum() {
		return m_GameNum;
	}

	public int getMaxGameNum() {
		return m_MaxGameNum;
	}

	public void setMaxGameNum(int num) {
		m_MaxGameNum = num;
	}

	public void setGameNum(int num) {
		m_GameNum = num;
	}

	public void incrementGameNum() {
		m_GameNum++;
	}

	public long getNumLrnActions() {
		return 0L; // dummy stub for agents which are not trainable
	}

	public long getNumTrnMoves() {
		return m_numTrnMoves;
	}

	public int getMoveCounter() {
		return 0;
	}

	/**
	 * @return During training: Call the Evaluator after this number of training
	 *         games
	 */
	public int getNumEval() {
		return m_oPar.getNumEval();
	}

	public void setNumEval(int num) {
		m_oPar.setNumEval(num);
	}

	public ParOther getParOther() {
		return m_oPar;
	}
	/**
	 * Set defaults for m_oPar (needed in {@link XArenaMenu#loadAgent} when
	 * loading older agents, where m_oPar=null in the saved version).
	 */
	public void setDefaultParOther() {
		m_oPar = new ParOther();
	}

	/**
	 * Check whether pa is a valid (non-null) and trained agent, of the same
	 * type as requested by agentName
	 * 
	 * @param paVector
	 *            vector of all agents in {@link games.Arena}
	 * @param numPlayers
	 *            number of players
	 * @return true, if each agent is valid. Otherwise a RuntimeException is
	 *         thrown.
	 */
	public static boolean validTrainedAgents(PlayAgent[] paVector, int numPlayers) throws RuntimeException {
		PlayAgent pa;
		String nStr;
		for (int n = 0; n < paVector.length; n++) {
			pa = paVector[n];
			nStr = Types.GUI_PLAYER_NAME[n];
			if (numPlayers == 2)
				nStr = Types.GUI_2PLAYER_NAME[n];
			if (pa == null) {
				throw new RuntimeException("Cannot execute command. Agent for player " + nStr + " is null!");
			}
			if (pa.getAgentState() != AgentState.TRAINED) {
				throw new RuntimeException(
						"Cannot execute command. Agent " + pa.getName() + " for player " + nStr + " is not trained!");
			}
		}
		return true;
	}

	/**
	 * Check whether pa is a valid (non-null) and trained agent, of the same
	 * type as requested by agentName
	 * 
	 * @param pa
	 * @param agentName
	 * @param Player
	 *            needed for message forming
	 * @param parent
	 *            component to be disabled if a message dialog is shown
	 * @return
	 */
	public static boolean validTrainedAgent(PlayAgent pa, String agentName, int Player, java.awt.Component parent) {
		if (pa == null) {
			MessageBox.show(parent, "Cannot execute command. " + agentName + " is null!", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (pa.getAgentState() != AgentState.TRAINED) {
			MessageBox.show(parent, "Cannot execute command. " + agentName + " is not trained!", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		String s_p = (Player == +1 ? "X" : "O");
		String pa_string = pa.getClass().getName();
		if (agentName.equals("TDS") & !(pa instanceof TDAgent)) {
			MessageBox.show(parent,
					"Cannot execute command. " + "Current " + s_p + " agent is not a TDSPlayer: " + pa_string + ".",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	public int getEpochMax() {
		return epochMax;
	}

	public void setEpochMax(int epochMax) {
		this.epochMax = epochMax;
	}

	public String stringDescr() {
		String cs = getClass().getSimpleName();
		return cs;
	}
	
	public String stringDescr2() {
		return getClass().getName() + ":";
	}

}
