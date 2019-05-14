package games.Othello.BenchmarkPlayer;

import java.io.Serializable;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import games.Nim.StateObserverNim;
import games.Othello.ConfigOthello;
import games.Othello.StateObserverOthello;
import tools.ElapsedCpuTimer;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;



/**
 * Implementation of {@link PlayAgent} for game Othello for any mapping agent.<p>
 * The constructor {@link #BenchMarkPlayer(String, int)} accepts argument {@code mode} to construct different types 
 * of Agents. The acceptable values for {@code mode} can
 * be found in {@link #ConfigOthello}. 
 */
public class BenchMarkPlayer extends AgentBase implements PlayAgent, Serializable {

	
		
	private static final long serialVersionUID = 13L;
	
	/**
	 * Depending on the mode which player has been chosen
	 */
	private int mode = 0;
	
	public BenchMarkPlayer(String name, int mode)
	{
		super(name) ;
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
		super.setAgentState(AgentState.TRAINED);
		this.mode = mode;
	}
	
	
	public String getName() {
		return super.getName();
	}
	
	/**
	 * Used to evaluate the current game state
	 * 
	 * @return double
	 */
	private double evaluate(StateObservation sob) {
		assert ( sob instanceof StateObserverOthello) : "sob is not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello)sob;
		int player = so.getPlayer();
		double scorePlayer=0;
		for(int i = 0, z = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++, z++) {
				if(so.getCurrentGameState()[i][j] == player) scorePlayer += ConfigOthello.BENCHMARKPLAYERMAPPING[mode][z];
				else if(so.getCurrentGameState()[i][j] == ((so.getPlayer() == 1) ? 2 : 1)) scorePlayer += -1 * ConfigOthello.BENCHMARKPLAYERMAPPING[mode][z];
			}
		}
		return scorePlayer;
	}
	
	
	/**
	 ** Method {@link #getNextAction2(StateObservation sob, boolean random, boolean silent)} returns    
	 * the best next action (maximizing the score) , which should be taken by the {@link #BenchMarkPlayer}. 
	 */
	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		assert ( sob instanceof StateObserverOthello) : "sob is not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello)sob;
		
		double score = evaluate(so); // just adding the next play to the score below
		ACTIONS bestAction = null;
		double maxScore = Double.NEGATIVE_INFINITY;
		double[] vTable = new double[so.getAvailableActions().size()];
		for(int i = 0; i < so.getAvailableActions().size(); i++)
		{
			ACTIONS x = so.getAvailableActions().get(i);
			vTable[i] = score + ConfigOthello.BENCHMARKPLAYERMAPPING[mode][x.toInt()];
			if(vTable[i] > maxScore) {
				maxScore = vTable[i];
				bestAction = x;
			}
		}
		return new ACTIONS_VT(bestAction.toInt(), false, vTable);
	}

	@Override
	public double getScore(StateObservation sob) {
		return evaluate(sob);
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so) {
        int player = so.getPlayer();
        int opponent = ((so.getPlayer() == 1) ? 2 : 1);
		ScoreTuple sTuple = new ScoreTuple(2);
		sTuple.scTup[player] = this.getScore(so);
		sTuple.scTup[opponent] = -sTuple.scTup[player];
		return sTuple;
	}

	@Override
	public double estimateGameValue(StateObservation so) {
		return getScore(so);
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so) {
		return getScoreTuple(so);
	}

	@Override
	public boolean isTrainable() {
		return false;
	}


}
