package controllers.MC;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;

import tools.Types;
import java.util.List;
import java.util.Random;

/**
 * Created by Johannes on 08.12.2016.
 */
public class MCAgent extends AgentBase implements PlayAgent {
    private List<Types.ACTIONS> actions;
    private Types.ACTIONS nextAction = Types.ACTIONS.fromInt(5);
    private double nextMoveScore = 0;
    private int totalRolloutDepth = 0; // saves the average rollout depth for the mc Agent
    private int nRolloutFinished = 0; 	// counts the number of rollouts ending with isGameOver==true
    private int nIterations = 0; 		// counts the total number of iterations

    public MCAgent() {
        this("MC");
    }

    public MCAgent(String name)
    {
        super(name);
        setAgentState(AgentState.TRAINED);
    }

    @Override
    public Types.ACTIONS getNextAction(StateObservation sob, boolean random, double[] vtable, boolean silent) {
        return getNextActionAverage(sob, vtable);
    }

    public Types.ACTIONS getNextActionAverage (StateObservation sob, double[] vtable) {
        nextAction = null;
        nextMoveScore = Double.NEGATIVE_INFINITY;
        nRolloutFinished = 0; 
        nIterations = 0;
        totalRolloutDepth = 0;

        actions = sob.getAvailableActions();

        if(sob.getNumAvailableActions() == 1) {
            return actions.get(0);
        }

        for(int i = 0; i < sob.getNumAvailableActions(); i++) {
            double averageScore = 0;
            for (int j = 0; j < Config.ITERATIONS; j++) {
                StateObservation newSob = sob.copy();

                newSob.advance(actions.get(i));
                RandomSearch agent = new RandomSearch();
                agent.startAgent(newSob);

                averageScore += newSob.getGameScore();
                if (newSob.isGameOver()) nRolloutFinished++;
                totalRolloutDepth += agent.getRolloutDepth();

            }
            averageScore /= Config.ITERATIONS;
            vtable[i] = averageScore;
            nIterations += Config.ITERATIONS;

            if (nextMoveScore <= averageScore) {
                nextAction = actions.get(i);
                nextMoveScore = averageScore;
            }
        }
        return nextAction;
    }

    public Types.ACTIONS getNextActionMax (StateObservation sob, double[] vtable) {
        nextAction = null;
        nextMoveScore = Double.NEGATIVE_INFINITY;
        actions = sob.getAvailableActions();

        if(sob.getNumAvailableActions() == 1) {
            return actions.get(0);
        }

        for(int i = 0; i < sob.getNumAvailableActions(); i++) {
            for (int j = 0; j < Config.ITERATIONS; j++) {
                StateObservation newSob = sob.copy();

                newSob.advance(actions.get(i));
                RandomSearch agent = new RandomSearch();
                agent.startAgent(newSob);

                if(newSob.getGameScore() > vtable[i]) {
                    nextAction = actions.get(i);
                    vtable[i] = newSob.getGameScore();
                }
            }
        }

        return nextAction;
    }

    @Override
    public double getScore(StateObservation sob) {
        return nextMoveScore;
    }

    public double getAverageRolloutDepth() {
        return totalRolloutDepth/nIterations;
    }

    public int getNRolloutFinished() {
        return nRolloutFinished;
    }

    public int getNIterations() {
        return nIterations;
    }

    @Override
    public boolean wasRandomAction() {
        return false;
    }

    @Override
    public String stringDescr() {
        String cs = getClass().getName();
        return cs;
    }
}
