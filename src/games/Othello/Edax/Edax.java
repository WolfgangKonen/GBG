package games.Othello.Edax;

import java.io.Serializable;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import games.Othello.StateObserverOthello;
import params.ParOther;
import tools.Types;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

public class Edax extends AgentBase implements PlayAgent, Serializable
{
	private static final long serialVersionID = 13l;
	private boolean firstTurn = true;
	private String lastEdaxMove;
	
	private CommandLineInteractor commandLineInteractor;
	
	public Edax()
	{
		super("Edax");
		System.out.println("creating edax");
		super.setAgentState(AgentState.TRAINED);
		commandLineInteractor = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
		//commandLineInteractor.sendCommand("level 1"); // WK just a try to set search depth
		commandLineInteractor.sendCommand("move-time 10"); // WK limit time-per-move to 10 sec
	}
	
	public void initForNewGame() {
		firstTurn=true;
		commandLineInteractor.sendCommand("init"); // Edax starts a new game		
	}

	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		assert sob instanceof StateObserverOthello: "sob not instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello) sob.copy();
		
		if(firstTurn)
		{
			firstTurn = false;
			if(so.getLastMove() == -1) // if nothing was played so far
			{
				lastEdaxMove = commandLineInteractor.sendAndAwait("mode 1"); // Edax goes first
			}
			else
			{
				commandLineInteractor.sendCommand("mode 0"); // Edax goes second
				lastEdaxMove = commandLineInteractor.sendAndAwait(EdaxMoveConverter.ConverteIntToEdaxMove(so.getLastMove()));
			}
		}
		else { // some turns were played
			if(!lastEdaxMove.equals(EdaxMoveConverter.ConverteIntToEdaxMove(so.getLastMove()))) // If Edax plays after the opponent played
				lastEdaxMove = commandLineInteractor.sendAndAwait(EdaxMoveConverter.ConverteIntToEdaxMove(so.getLastMove())); 
			else // If Edax plays two or more consecutive turns
				lastEdaxMove = commandLineInteractor.sendAndAwait("play");
		}
		ACTIONS_VT action = new ACTIONS_VT(EdaxMoveConverter.converteEdaxToInt(lastEdaxMove), 
										   false, new double[so.getAvailableActions().size()+1]);
																							// "+1" added /WK/
		so.getPlayer();
		if(!so.getAvailableActions().contains(action))
		{
			System.err.println("EDAX IST AM SCHUMMELN!");
		}
		
		return action;
	}
	

	@Override
	public double getScore(StateObservation sob) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation sob) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double estimateGameValue(StateObservation sob) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean trainAgent(StateObservation so) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String printTrainStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTrainable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String stringDescr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringDescr2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxGameNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getGameNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNumLrnActions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNumTrnMoves() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMoveCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxGameNum(int num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGameNum(int num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ParOther getParOther() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumEval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumEval(int num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AgentState getAgentState() {
		// TODO Auto-generated method stub
		return super.getAgentState();
	}

	@Override
	public void setAgentState(AgentState aState) {
		// TODO Auto-generated method stub
		super.setAgentState(aState);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}	
}
