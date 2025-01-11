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

    private int playerNumber;

    public RLEnvironmentConnector(XNTupleFuncs xnTupleFuncs, List<PlayAgent> enemyAgents, SB3Agent sb3Agent, boolean oneHot, int playerNumber) {
        this.xnTupleFuncs = xnTupleFuncs;
        this.enemyAgents = enemyAgents;
        this.sb3Agent = sb3Agent;
        this.oneHot = oneHot;
        this.playerNumber = playerNumber;
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
        return stateObservation.getGameScore(playerNumber);
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
        int enemyPlayer = 0;
        while (stateObservation.getPlayer() != this.playerNumber && !terminated() && !truncated()) {

            System.out.println("enemy" + getPlainObservation());

            // availableActions = stateObservation.getAvailableActions();
            // Random random = new Random();
            // Types.ACTIONS enemyAction = availableActions.get(random.nextInt(availableActions.size()))
            Types.ACTIONS enemyAction = enemyAgents.get(enemyPlayer).getNextAction2(stateObservation, false, true, true);

            stateObservation.advance(enemyAction, null);

            enemyPlayer++;
        }

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
