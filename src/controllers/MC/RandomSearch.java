package controllers.MC;


import java.util.Random;

import games.StateObservation;

/**
 * Perform a random rollout for {@link MCAgentN}
 */
public class RandomSearch{
    private Random random = new Random();
    private int rolloutDepth = MCAgentConfig.DEFAULT_ROLLOUTDEPTH;

    /**
     * @param sob       start state
     * @param depth     rollout depth
     */
    public void startAgent(StateObservation sob, int depth) {

        for(int i = 0; i < depth; i++) {
        	if (!sob.isGameOver()) {
                if (sob.getNumAvailableActions() > 0) {
                    sob.advance(sob.getAvailableActions().get(random.nextInt(sob.getNumAvailableActions())));
                }
                else {
                    rolloutDepth = i+1;
                    //break; // out of for      // /WK/12/20/ break commented out, because if game is not yet over but
                                                // the current player has no available action: we have a pass situation
                                                // (like in Othello) and should just continue (!)
                }
        	} else {					
                // /WK/ **BUG1** fix: check every sob (including the first!) whether game is 
        		// already over. If this is the case, return sob without advance and set 
        		// rolloutDepth to i (being 0 in the first iteration).
        		//
        		// (The check isGameOver() is necessary in addition to 
        		//		G = sob.getNumAvailableActions() > 0
        		// because there might be StateObservation classes which return G>0 even when
        		// the game is already over. For example empty fields in Hex or TicTacToe
        		// might still count for G, even when one player has already a winning chain.)
        		rolloutDepth = i;
        		if (sob.isPartialState())
                    sob.randomCompletion();     // complete a partial state with random infills
                                                // so that it is possible to calculate a score for game-over state sob.

                break; // out of for
        	}
        }
    }

    public int getRolloutDepth() {
        return  rolloutDepth;
    }
}
