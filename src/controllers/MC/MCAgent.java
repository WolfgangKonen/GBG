package controllers.MC;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 08.12.2016.
 */
public class MCAgent extends AgentBase implements PlayAgent {
    private Random random = new Random();
    private ExecutorService executorService = Executors.newWorkStealingPool();

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
        return getNextAction(sob, vtable);
    }

/**
 * Get the best next action and return it
 * @param sob			current game state (not changed on return)
 * @param vtable		must be an array of size n+1 on input, where
 * 						n=sob.getNumAvailableActions(). On output,
 * 						elements 0,...,n-1 hold the score for each available
 * 						action (corresponding to sob.getAvailableActions())
 * 						In addition, vtable[n] has the score for the
 * 						best action.
 * @return nextAction	the next action
 */

public Types.ACTIONS getNextAction (StateObservation sob, double[] vtable) {
    List<Callable<ResultContainer>> callables = new ArrayList<>();
    List<ResultContainer> resultContainers = new ArrayList<>();
    List<Types.ACTIONS> actions = sob.getAvailableActions();

    nRolloutFinished = 0;
    nIterations = sob.getNumAvailableActions()*Config.ITERATIONS;
    totalRolloutDepth = 0;

    if(sob.getNumAvailableActions() == 1) {
        return actions.get(0);
    }

    for (int j = 0; j < Config.ITERATIONS; j++) {
        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            StateObservation newSob = sob.copy();
            int staticI = i;
            Types.ACTIONS firstAction = actions.get(i);
            callables.add(() -> {
                newSob.advance(firstAction);
                RandomSearch agent = new RandomSearch();
                agent.startAgent(newSob);

                return new ResultContainer(staticI, newSob, agent.getRolloutDepth());
            });

        }
    }

    try {
        executorService.invokeAll(callables).stream().map(future -> {
            try {
                return future.get();
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).forEach(resultContainers::add);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    for(ResultContainer resultContainer : resultContainers) {
        vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore();
        totalRolloutDepth += resultContainer.rolloutDepth;
        if(resultContainer.sob.isGameOver()) {
            nRolloutFinished++;
        }
    }

    Types.ACTIONS nextAction = null;
    double nextActionScore = Double.NEGATIVE_INFINITY;

    for (int i = 0; i < sob.getNumAvailableActions(); i++) {
        vtable[i] /= Config.ITERATIONS;
        if (nextActionScore < vtable[i]) {
            nextAction = actions.get(i);
            nextActionScore = vtable[i];
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

class ResultContainer {
    public int firstAction;
    public StateObservation sob;
    public int rolloutDepth;

    public ResultContainer(int firstAction, StateObservation sob, int rolloutDepth) {
        this.firstAction = firstAction;
        this.sob = sob;
        this.rolloutDepth = rolloutDepth;
    }
}