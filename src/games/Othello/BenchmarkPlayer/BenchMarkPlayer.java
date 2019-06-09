package games.Othello.BenchmarkPlayer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import games.Othello.BaseOthello;
import games.Othello.ConfigOthello;
import games.Othello.StateObserverOthello;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;



/**
 * Implementation of {@link PlayAgent} for game Othello for any mapping agent.<p>
 * The constructor {@link #BenchMarkPlayer(String, int)} accepts argument {@code mode} to construct different types 
 * of Agents. The acceptable values for {@code mode} can
 * be found in {@link ConfigOthello#BENCHMARKPLAYERMAPPING}. 
 */
public class BenchMarkPlayer extends AgentBase implements PlayAgent, Serializable {

	
		
	private static final long serialVersionUID = 13L;
	private Random random;
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
		random = new Random();
	}
	
	
	public String getName() {
		return super.getName();
	}
	
	/**
	 * Evaluate the game state based on {@link ConfigOthello#BENCHMARKPLAYERMAPPING} <br>
	 * <b>Reminder</b>: the game state has been advanced already, therefore the opponent is the player, who wants to maximize the score.
	 * @return double players score
	 */
	private double evaluate(StateObservation sob) {
		assert ( sob instanceof StateObserverOthello) : "sob is not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello)sob;
		int player = ConfigOthello.OPPONENT[sob.getPlayer()], opponent = ConfigOthello.PLAYER[so.getPlayer()]; // reverse cause the state has been advanced already
		double scorePlayer=0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(so.getCurrentGameState()[i][j] == player) scorePlayer += 
						ConfigOthello.BENCHMARKPLAYERMAPPING[mode][i* ConfigOthello.BOARD_SIZE + j];
				else if(so.getCurrentGameState()[i][j] == opponent) scorePlayer -= 
						ConfigOthello.BENCHMARKPLAYERMAPPING[mode][i* ConfigOthello.BOARD_SIZE + j];
			}
		}
		return scorePlayer;
	}

	/**
	 * Recursive part can be found in {@link #evaluateState(StateObservation, ACTIONS, boolean)}
	 */
	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		assert ( sob instanceof StateObserverOthello) : "sob is not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello)sob;
		int count = 0;
		double bestScore = Double.NEGATIVE_INFINITY;
		double[] vTable = new double[so.getAvailableActions().size()];  // Adding the points to the vTable
		ACTIONS bestAction = null;
		StateObserverOthello newSO;
		for(int i = 0; i < so.getAvailableActions().size(); i++)
		{
			newSO = (StateObserverOthello) so.copy(); // advancing the state for each action;
			ACTIONS action = so.getAvailableActions().get(i);  // get available action
			vTable[i] = evaluateState( newSO,  action, silent); // Advancing here
			if(vTable[i] > bestScore) {
				bestScore = vTable[i];
				bestAction = action;
				count = 1;
			}else if(vTable[i] == bestScore) {
				count++;
				if(this.random.nextDouble() < 1.0/count) {
					bestScore = vTable[i];
					bestAction = action;
				}
			}
		}
		ACTIONS_VT x = new ACTIONS_VT(bestAction.toInt(), false, vTable);
		return x;
		
	}

	/**
	 * Used to advance the game state with given action. Including game winning move. <br>
	 * Using {@link BenchMarkPlayer#getNextAction2(StateObservation, boolean, boolean)} for recursive part. <br>
	 * Using helper method: {@linkplain BenchMarkPlayer#finalMove(StateObserverOthello)} for game winning move.
	 * @param sob
	 * @param action which is used to advance the game state
	 * @param silent
	 * @return score for the an advanced game state.
	 */
	public double evaluateState(StateObservation sob, ACTIONS action,boolean silent) {
		assert (sob instanceof StateObserverOthello):"so not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello) sob.copy() ;
		double CurrentScore;
		StateObserverOthello newSO = (StateObserverOthello) so.copy();
		int player = ConfigOthello.PLAYER[so.getPlayer()];
		newSO.advance(action);
		CurrentScore = evaluate(newSO);
		// Game winning move
		if(newSO.isGameOver() && finalMove(newSO) == ConfigOthello.OPPONENT[newSO.getPlayer()]) {
			return 100000.0;
		}
		//Recursive part: Only if the StateObserver signals that the player does not change. We want the maximal score ending the agent turn end.
		if(!newSO.isGameOver()) {
			if(ConfigOthello.PLAYER[newSO.getPlayer()] == player) {
				ACTIONS_VT actionVT = getNextAction2(newSO, false, silent);
				return actionVT.getVBest();
			}
		}
		return CurrentScore;
	}
	
	/**
	 * Helper function for {@link BenchMarkPlayer#evaluateState(StateObservation, ACTIONS, boolean)} to map the {@link Types.WINNER} to {@link ConfigOthello#OPPONENT} and {@link ConfigOthello#PLAYER}.
	 * @param so
	 * @return	Integer representing the player
	 */
	private int finalMove(StateObserverOthello so) {
		Types.WINNER x = so.getGameWinner();
		if(x == Types.WINNER.PLAYER_WINS) return ConfigOthello.OPPONENT[so.getPlayer()];
		return ConfigOthello.PLAYER[so.getPlayer()];
	}
	
	
	
	@Override
	public double getScore(StateObservation sob) {
		return evaluate(sob); //normieren
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so) {
        int player = so.getPlayer();
        int opponent = BaseOthello.getOpponent(player) ;
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