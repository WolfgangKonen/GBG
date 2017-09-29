package controllers;

import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

public class HumanPlayer extends AgentBase implements PlayAgent
{	
	
	public HumanPlayer(String name)
	{
		super(name);
		setAgentState(AgentState.TRAINED);		
	}


	@Deprecated
	@Override
	public ACTIONS getNextAction(StateObservation sob, boolean random, double[] VTable, boolean silent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		return null;
	}

	@Override
	public double getScore(StateObservation sob) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * <p>
	 * Use now {@link Types.ACTIONS#isRandomAction()}
	 */
	@Deprecated
	public boolean wasRandomAction() {
		return false;
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		return cs;
	}
}
