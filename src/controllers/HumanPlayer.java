package controllers;

import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import tools.Types.ACTIONS;

public class HumanPlayer extends AgentBase implements PlayAgent
{	
	
	public HumanPlayer(String name)
	{
		super(name);
		setAgentState(AgentState.TRAINED);		
	}


	@Override
	public ACTIONS getNextAction(StateObservation sob, boolean random, double[] VTable, boolean silent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getScore(StateObservation sob) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean wasRandomAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		return cs;
	}
}
