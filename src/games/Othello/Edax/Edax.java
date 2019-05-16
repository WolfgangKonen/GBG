package games.Othello.Edax;

import controllers.PlayAgent;
import games.StateObservation;
import params.ParOther;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

public class Edax implements PlayAgent
{
	private boolean firstTurn;
	
	public Edax(int mode)
	{
		firstTurn = true;
		
		Process edax = Runtime.getRuntime().exec("~\\")
		// exec edax.exe
		// set Mode -> Console
		// 
	}

	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		// TODO 
		if(firstTurn)
		{
			firstTurn = false;			
		}
		
		return null;
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
		return null;
	}

	@Override
	public void setAgentState(AgentState aState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}
	
	
}
