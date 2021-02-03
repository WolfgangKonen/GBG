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
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;



/**
 * Implementation of {@link PlayAgent} for game Othello for any mapping agent.<p>
 * The constructor {@link #BenchMarkPlayer(String, int)} accepts arguments {@code mode=0} (<b>HeurPlayer</b>) and 
 * {@code mode=1} (<b>BenchPlayer</b>)  to construct different types of agents. 
 * The associated value maps can be found in {@link ConfigOthello#BENCHMARKPLAYERMAPPING}. 
 * <p>
 * Note that both BenchPlayer and HeurPlayer may have more than one best action in certain states. In these cases, one of 
 * the actions leading to the best score is selected randomly. Consequently, the evaluation results with BenchPlayer and
 * HeurPlayer can vary, even if the set of start states is exactly the same. Stronger variations for HeurPlayer, because 
 * due to its symmetric values more actions may lead to the same best score.
 */
public class BenchMarkPlayer extends AgentBase implements PlayAgent, Serializable {

	
	public static final long serialVersionUID = 13L;
	private Random random;
	private boolean strong = false;
	private double min, max;
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
		min = ConfigOthello.BENCHMIN[mode];
		max = ConfigOthello.BENCHMAX[mode];
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
		int player = so.getOpponent(so.getPlayer());// reverse cause the state has been advanced already
		int opponent = so.getPlayer(); // reverse cause the state has been advanced already
		double scorePlayer=0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(so.getCurrentGameState()[i][j] == player) {
					scorePlayer += ConfigOthello.BENCHMARKPLAYERMAPPING[mode][i* ConfigOthello.BOARD_SIZE + j];
				}
				else if(so.getCurrentGameState()[i][j] == opponent) {
					scorePlayer -= 
						ConfigOthello.BENCHMARKPLAYERMAPPING[mode][i* ConfigOthello.BOARD_SIZE + j];
				}
			}
		}
		return scorePlayer;
	}

	
	private double normalize(double score) {
		return (((score - min) * (1 +1))/(max-min)) -1;
	}
	
	/**
	 * Recursive part can be found in {@link #evaluateState(StateObservation, ACTIONS, boolean)}
	 */
	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		assert ( sob instanceof StateObserverOthello) : "sob is not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello)sob;
		int count = 0;
		double bestScore = Double.NEGATIVE_INFINITY, score = 0;
		double[] vTable = new double[so.getAvailableActions().size()];  // Adding the points to the vTable
		ACTIONS bestAction = null;
		StateObserverOthello newSO;
		for(int i = 0; i < so.getAvailableActions().size(); i++)
		{
			newSO = (StateObserverOthello) so.copy(); // advancing the state for each action;
			ACTIONS action = so.getAvailableActions().get(i);  // get available action
			score = evaluateState( newSO,  action, silent); // Advancing here
			vTable[i] = score;
//			System.out.println("score= " + score + " min " + vTable[i]);	// WK too much output
//			System.out.println((score -min) + " / " + (max -score));
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
			//if (count>1) System.out.println("more than one best action");
		}
		ACTIONS_VT x = new ACTIONS_VT(bestAction.toInt(), false, vTable, bestScore);
		return x;
		
	}

	/**
	 * Used to advance the game state with given action. Including game winning move. <br>
	 * Using {@link BenchMarkPlayer#getNextAction2(StateObservation, boolean, boolean)} for recursive part. <br>
	 * Using helper method: {@linkplain BenchMarkPlayer#finalMove(StateObserverOthello)} for game winning move.
	 * @param sob	the state
	 * @param action which is used to advance the game state
	 * @param silent whether to print or not
	 * @return score for the an advanced game state.
	 */
	public double evaluateState(StateObservation sob, ACTIONS action,boolean silent) {
		assert (sob instanceof StateObserverOthello):"so not an instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello) sob.copy() ;
		double currentScore, scoreNormalized;
		StateObserverOthello newSO = (StateObserverOthello) so.copy();
		int player = newSO.getPlayer();
		newSO.advance(action);
		currentScore = evaluate(newSO);
		scoreNormalized = normalize(currentScore);
//		System.out.println(scoreNormalized + " " + currentScore);
		// Game winning move
		if(strong) {
			if(newSO.isGameOver() && finalMove(newSO) == BaseOthello.getOpponent(player)) {
				return 100000.0;
			}
		//Recursive part: Only if the StateObserver signals that the player does not change.
		//We want the maximal score after the agents turn end.
			if(!newSO.isGameOver()) {
				if(newSO.getPlayer() == player) {
					ACTIONS_VT actionVT = getNextAction2(newSO.partialState(), false, silent);
					return actionVT.getVBest();
				}
			}
		}
		
		return scoreNormalized;
	}
	
	/**
	 * Helper function for {@link BenchMarkPlayer#evaluateState(StateObservation, ACTIONS, boolean)} to map the
	 * {@link Types.WINNER} to {@link StateObserverOthello#getOpponent(int)} and {@link StateObserverOthello#getPlayer()}.
	 * @param so
	 * @return	Integer representing the player
	 */
	private int finalMove(StateObserverOthello so) {
		Types.WINNER x = so.winStatus();
		StateObserverOthello newSO = (StateObserverOthello) so;
		if(x == Types.WINNER.PLAYER_WINS) return so.getOpponent(so.getPlayer());
		return so.getPlayer();
	}
	
	
	
	@Override
	public double getScore(StateObservation sob) {
		return evaluate(sob); 
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
        int player = so.getPlayer();
        int opponent = BaseOthello.getOpponent(player) ;
		ScoreTuple sTuple = new ScoreTuple(2);
		sTuple.scTup[player] = this.getScore(so);
		sTuple.scTup[opponent] = -sTuple.scTup[player];
		return sTuple;
	}

//	@Override
//	public double estimateGameValue(StateObservation so) {
//		return so.getGameScore(so.getPlayer());     //getScore(so);
//	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so, ScoreTuple prevTuple) {
		return so.getRewardTuple(true);  //getScoreTuple(so, null);
	}

	@Override
	public boolean isTrainable() {
		return false;
	}


}