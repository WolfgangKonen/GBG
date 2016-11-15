package controllers;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JOptionPane;

import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import games.TicTacToe.TDPlayerTTT;
import tools.MessageBox;
import tools.Types;

/**
 * Class AgentBase implements functionality of the interface {@link PlayAgent} 
 * common to all agents (things related to gameNum, maxGameNum, AgentState).
 * 
 * @see PlayAgent
 * @see games.TicTacToe.TicTDBase
 * @see controllers.MCTS.MCTSAgentT
 */
abstract public class AgentBase implements Serializable {
	private int m_GameNum;
	private int	m_MaxGameNum;
	private String m_name;
	private AgentState m_agentState = AgentState.RAW;
	private int epochMax=0;

	/**
	 * Default constructor for AgentBase, needed for loading a serialized version
	 */
	protected AgentBase() {
		this("none");
	}
	public AgentBase(String name) {
		m_name=name;
	}
	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * getScore(), as required by the interface {@link PlayAgent} as well.
	 * The definition of getScore() is needed in 
	 * {@link #rewardEstimate(StateObservation)}.
	 */
	abstract public double getScore(StateObservation sob);
	/**
	 * Default behaviour: return {@link #getScore(StateObservation)}
	 */
	public double rewardEstimate(StateObservation sob) {
		return getScore(sob);
	};
	public void setAgentState(AgentState aState) {
		m_agentState = aState;	
	}

	public AgentState getAgentState() {
		return m_agentState;
	}	

	public String getName() { return m_name; }
	public void setName(String name) { m_name=name; }

	public boolean trainAgent(StateObservation so) 
	{
		return trainAgent(so,Integer.MAX_VALUE);
	}
	public boolean trainAgent(StateObservation so, int epiLength)								 
	{	
		m_GameNum++;
		return false;
	}

	/**
	 * Normalize game score or reward from range [oldMin,oldMax]
	 * to range [newMin,newMax]
	 */
	protected double normalize(double reward, double oldMin, double oldMax, 
			 double newMin, double newMax) {
		reward = (reward-oldMin)*(newMax-newMin)/(oldMax-oldMin) + newMin;
		return reward;
	}

	public String printTrainStatus() {
		return "";
	}

	public byte getSize() {return 1;}	// dummy stub
	
	public int getGameNum()
	{			
		return m_GameNum;		
	}

	public int getMaxGameNum()
	{	
		return m_MaxGameNum;
	}	

	public void setMaxGameNum(int num)
	{
		m_MaxGameNum=num;
	}

	public void setGameNum(int num)
	{
		m_GameNum=num;
	}

	public void incrementGameNum()
	{
		m_GameNum++;
	}

	/**
	 * Check whether pa is a valid (non-null) and trained agent, of the same type as 
	 * requested by agentName
	 * @param paVector	vector of all agents in {@link games.Arena}
	 * @param numPlayers	number of players
	 * @return true, if each agent is valid. Otherwise a RuntimeException is thrown.
	 */
	public static boolean validTrainedAgents(PlayAgent[] paVector, int numPlayers) 
			throws RuntimeException
	{
		PlayAgent pa; 
		String nStr;
		for (int n=0; n<paVector.length; n++) {
			pa=paVector[n];
			nStr  = Types.GUI_PLAYER_NAME[n];
			if (numPlayers==2) nStr = Types.GUI_2PLAYER_NAME[n];
			if (pa==null) {
				throw new RuntimeException( 
						"Cannot execute command. Agent for player "+nStr+" is null!");
			}
			if (pa.getAgentState()!=AgentState.TRAINED) {
				throw new RuntimeException( 
						"Cannot execute command. Agent "+pa.getName()+" for player "+nStr+" is not trained!");
			}		
		}
		return true;
	}

	/**
	 * Check whether pa is a valid (non-null) and trained agent, of the same type as 
	 * requested by agentName
	 * @param pa
	 * @param agentName
	 * @param Player	needed for message forming
	 * @param parent	component to be disabled if a message dialog is shown
	 * @return
	 */
	public static boolean validTrainedAgent(PlayAgent pa, String agentName, int Player, java.awt.Component parent) {
		if (pa==null) {
			MessageBox.show(parent, 
					"Cannot execute command. "+agentName+" is null!", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (pa.getAgentState()!=AgentState.TRAINED) {
			MessageBox.show(parent, 
					"Cannot execute command. "+agentName+" is not trained!", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	
		String s_p = (Player==+1 ? "X" : "O");
		String pa_string = pa.getClass().getName();
		if (agentName.equals("TDS") & !(pa instanceof TDPlayerTTT)) {
			MessageBox.show(parent, 
					"Cannot execute command. "+"Current "+s_p+" agent is not a TDSPlayer: "+ pa_string+".", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
//		if (agentName.equals("CMA-ES") & !pa_string.equals("TicTacToe.CMAPlayer")) {
//			MessageBox.show(parent, 
//					"Cannot execute command. "+"Current "+s_p+" agent is not a CMAPlayer: "+ pa_string+".", 
//					"Warning", JOptionPane.WARNING_MESSAGE);
//			return false;
//		}
		return true;
	}

	public int getEpochMax() {
		return epochMax;
	}

	public void setEpochMax(int epochMax) {
		this.epochMax = epochMax;
	}
	
	

}

