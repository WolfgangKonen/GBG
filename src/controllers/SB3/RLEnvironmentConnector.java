package controllers.SB3;

import controllers.PlayAgent;
import games.StateObservation;
import games.XNTupleFuncs;
import tools.Types;

import java.util.*;

public class RLEnvironmentConnector implements RLEnvironment {
    private StateObservation stateObservation;
    private XNTupleFuncs xnTupleFuncs;
    private List<PlayAgent> enemyAgents;
    private SB3Agent sb3Agent;
    private boolean oneHot;

    private int trainedPlayer = 0;

    public RLEnvironmentConnector(XNTupleFuncs xnTupleFuncs, List<PlayAgent> enemyAgents, SB3Agent sb3Agent, boolean oneHot) {
        this.stateObservation = stateObservation;
        this.xnTupleFuncs = xnTupleFuncs;
        this.enemyAgents = enemyAgents;
        this.sb3Agent = sb3Agent;
        this.oneHot = oneHot;
    }

    public List<Double> getPlainObservation() {
        int[] boardVector = xnTupleFuncs.getBoardVector(this.stateObservation).bvec;
        List<Double> observation= new ArrayList<>();
        for(int i : boardVector) {
            observation.add((double) i);
        }
        return observation;
    }

    public List<Double> getStandardPerspectiveObservation() {
        int[] boardVector = xnTupleFuncs.getStandardPerspectivesBoardVector(this.stateObservation).bvec;
        List<Double> observation= new ArrayList<>();
        for(int i : boardVector) {
            observation.add((double) i);
        }
        return observation;
    }

    public List<Double> getOneHotStandardObservation() {
        int[] boardVector = xnTupleFuncs.getOneHotBoardVector(
                xnTupleFuncs.getStandardPerspectivesBoardVector(this.stateObservation)
        ).bvec;
        List<Double> observation= new ArrayList<>();
        for(int i : boardVector) {
            observation.add((double) i);
        }
        return observation;
    }

    @Override
    public List<Double> getObservation() {
        return oneHot ? getOneHotStandardObservation() : getStandardPerspectiveObservation();
    }

    public void initiateNewGame(StateObservation stateObservation) {
        this.stateObservation = stateObservation;
    }

    @Override
    public double getReward() {
        // return stateObservation.getReward(this.trainedPlayer, false);
        return stateObservation.getGameScore(trainedPlayer);
    }

    @Override
    public boolean terminated() {
        return stateObservation.isGameOver();
    }

    @Override
    public boolean truncated() {
        return false;
    }

    @Override
    public Map<String, String> getInfo() {
        Map<String, String> description = new HashMap<>();
        description.put("description", stateObservation.stringDescr());
        return description;
    }

    @Override
    public FirstObservation reset() {
        this.stateObservation = sb3Agent.getStartSate();

        return new FirstObservation(getObservation(), getInfo());
    }

    @Override
    public Transition step(int action) {
        List<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
        Types.ACTIONS chosenAction = new Types.ACTIONS(action);

        // When caught cheating return same observation and min game score. StateObservation gets reset afterward when sb3 calls reset.
        if(!availableActions.contains(chosenAction)) {
            return new Transition(getObservation(), stateObservation.getMinGameScore(), true, truncated(), getInfo());
        }

        try {
            stateObservation.advance(chosenAction, null);
        } catch (AssertionError e) {
            e.printStackTrace();
            sb3Agent.notify();
        }

        int enemyPlayer = 0;
        while (stateObservation.getPlayer() != this.trainedPlayer && !terminated() && !truncated()) {
            // Types.ACTIONS actions = enemyAgents.get(enemyPlayer).getNextAction2(stateObservation, false, true, true);
            System.out.println("enemy" + getPlainObservation());
            availableActions = stateObservation.getAvailableActions();
            Random random = new Random();
            stateObservation.advance(availableActions.get(random.nextInt(availableActions.size())), null);

            enemyPlayer++;
        }

        System.out.println("me   " + getObservation() + terminated());

        return new Transition(getObservation(), getReward(), terminated(), truncated(), getInfo());
    }

    @Override
    public void trainingFinished() {
        System.out.println("Training finished");
        synchronized (sb3Agent) {
            sb3Agent.notify();
        }
    }
}
