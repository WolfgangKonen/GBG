package games.Othello.Edax;

import java.io.Serializable;
import java.util.ArrayList;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import games.Othello.StateObserverOthello;
import params.ParOther;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

public class Edax2 extends AgentBase implements PlayAgent, Serializable
{
	private static final long serialVersionID = 13L;
	private String lastEdaxMove;
	
	private int level;			// ... for later
	private double moveTime;	//
	
	transient private CommandLineInteractor commandLineInteractor;
	
	public Edax2()
	{
		super("Edax2");
		System.out.println("creating edax2");
		super.setAgentState(AgentState.TRAINED);
		commandLineInteractor = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
		commandLineInteractor.sendCommand("mode 3"); // no automatic moves
//		commandLineInteractor.sendCommand("level 1"); 		//  set search depth (default: 21)
		commandLineInteractor.sendCommand("move-time 10"); // WK limit time-per-move to 10 sec
		
	}
	
	/**
	 * Set {@code vTable} of returned ACTIONS_VT in such a way that it is 1.0 for the action chosen
	 * by Edax, 0.0 for all other available actions.	 
	 */
	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		assert sob instanceof StateObserverOthello: "sob not instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello) sob;
		ArrayList<ACTIONS> availActions = so.getAvailableActions();
		
//		commandLineInteractor.sendCommand("init"); 
		commandLineInteractor.sendCommand("setboard "+so.toEdaxString()); 
		lastEdaxMove = commandLineInteractor.sendAndAwait("go");  // force Edax to play now			
		
		int actInteger = EdaxMoveConverter.converteEdaxToInt(lastEdaxMove);
		double[] vTable = new double[availActions.size()+1];
		for (int i=0; i<availActions.size(); i++) {
			if (availActions.get(i).toInt()==actInteger) vTable[i]=1.0;
		}
		vTable[availActions.size()]=1.0;
		
		ACTIONS_VT action = new ACTIONS_VT(actInteger, false, vTable);

		if(!so.getAvailableActions().contains(action))
		{
			System.err.println(this.getName() + " IST AM SCHUMMELN!");
		}
				
		return action;
	}
	
	@Override
	public double getScore(StateObservation sob) {
		// Edax has no estimates for score values, so we return 0 ...
		return 0;
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation sob) {
		// ... and so also no real score tuple
		return new ScoreTuple(sob.getNumPlayers());
	}

}
