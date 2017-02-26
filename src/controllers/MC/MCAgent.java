package controllers.MC;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;

import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Johannes on 08.12.2016.
 */
public class MCAgent extends AgentBase implements PlayAgent {
    private Random random = new Random();

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
        return getNextActionMultipleAgents(sob, vtable);
    }

    public Types.ACTIONS getNextAction (StateObservation sob, double[] vtable) {
        Types.ACTIONS nextAction = Types.ACTIONS.fromInt(5);
        double nextActionScore = Double.NEGATIVE_INFINITY;
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        nRolloutFinished = 0; 
        nIterations = sob.getNumAvailableActions()*Config.ITERATIONS;
        totalRolloutDepth = 0;

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

            if (nextActionScore <= averageScore) {
                nextAction = actions.get(i);
                nextActionScore = averageScore;
            }
        }
        return nextAction;
    }

    public Types.ACTIONS getNextActionMultipleAgents (StateObservation sob, double[] vtable) {
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()*Config.ITERATIONS*Config.NUMBERAGENTS;
        totalRolloutDepth = 0;

        if(sob.getNumAvailableActions() == 1) {
            return actions.get(0);
        }

        for (int i = 0; i < Config.NUMBERAGENTS; i++) {
            int nextAction = 0;
            double nextActionScore = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < sob.getNumAvailableActions(); j++) {
                double averageScore = 0;

                for (int k = 0; k < Config.ITERATIONS; k++) {
                    StateObservation newSob = sob.copy();

                    newSob.advance(actions.get(j));
                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob);

                    averageScore += newSob.getGameScore();
                    if (newSob.isGameOver()) nRolloutFinished++;
                    totalRolloutDepth += agent.getRolloutDepth();

                }

                averageScore /= Config.ITERATIONS;
                if (nextActionScore <= averageScore) {
                    nextAction = j;
                    nextActionScore = averageScore;
                }
            }
            vtable[nextAction]++;
        }

        List<Types.ACTIONS> nextActions = new ArrayList<>();
        double nextActionScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            if (nextActionScore < vtable[i]) {
                nextActions.clear();
                nextActions.add(actions.get(i));
                nextActionScore = vtable[i];
            } else if(nextActionScore == vtable[i]) {
                nextActions.add(actions.get(i));
            }
        }

        return nextActions.get(random.nextInt(nextActions.size()));
    }

    @Override
    public double getScore(StateObservation sob) {
        double[] vtable = new double[sob.getNumAvailableActions()+1];
        double nextActionScore = Double.NEGATIVE_INFINITY;

        getNextAction(sob, true, vtable, true);

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            if (nextActionScore <= vtable[i]) {
                nextActionScore = vtable[i];
            }
        }

        return nextActionScore;
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
