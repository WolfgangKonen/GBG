package controllers.SB3;

import controllers.PlayAgent;
import games.*;
import tools.Types;

import java.util.*;

public class RLEnvironmentConnector {
    private StateObservation stateObservation;
    private StateObservationVectorFuncs stateObservationVectorFuncs;
    private List<PlayAgent> enemyAgents;
    private final SB3Agent sb3Agent;

    private int playerNumber;

    public RLEnvironmentConnector( StateObservationVectorFuncs stateObservationVectorFuncs, List<PlayAgent> enemyAgents, SB3Agent sb3Agent, int playerNumber) {
        this.stateObservationVectorFuncs = stateObservationVectorFuncs;
        this.sb3Agent = sb3Agent;
        this.playerNumber = playerNumber;
        this.enemyAgents = enemyAgents;
    }


    public int[] getObservation() {
        return stateObservationVectorFuncs.getStateObservationVector(stateObservation);
    }

    public double getReward() {
        // return stateObservation.getReward(this.trainedPlayer, false);
        return stateObservation.getGameScore(playerNumber); // TODO: Step reward
    }

    public boolean terminated() {
        return stateObservation.isGameOver();
    }

    public boolean truncated() {
        return false;
    }

    public Map<String, String> getInfo() {
        Map<String, String> description = new HashMap<>();
        description.put("description", stateObservation.stringDescr());
        return description;
    }

    public FirstObservation reset() {
        this.stateObservation = sb3Agent.afterGame();
        System.out.println("1");
        advanceEnemies();
        System.out.println("2");
        return new FirstObservation(getObservation(), getInfo());
    }

    public int[] getAvailableActions() {
        return stateObservationVectorFuncs.getAvailableActions(stateObservation);
    }

    public Transition step(int action) {
        // advanceEnemies(); // Just to be sure
        List<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
        Types.ACTIONS chosenAction = new Types.ACTIONS(action);

        System.out.println(stateObservation.getPlayer() + " Player: " + Arrays.toString(getObservation()) + " " + getObservation().length + " before");
        sb3Agent.incrementMoves();
        // When caught cheating return same observation and min game score. StateObservation gets reset afterward when sb3 calls reset.
        if(!availableActions.contains(chosenAction)) {
            Transition transition = new Transition(getObservation(), stateObservation.getMinGameScore(), true, truncated(), getInfo());
            switchPlayerPostions(); // imported to call after new Transition because playerNumber gets changed and getReward depends on it.
            System.out.println("Game Over, because cheating! Starting new one...");
            return transition;
        }

        try {
            stateObservation.advance(chosenAction, null);
        } catch (AssertionError e) {
            e.printStackTrace();
            sb3Agent.notify();
        }

        advanceEnemies();
        System.out.println(stateObservation.getPlayer() + " Player: " + Arrays.toString(getObservation()) + " " + getObservation().length + " after");

        Transition transition = new Transition(getObservation(), getReward(), terminated(), truncated(), getInfo());
        // if Game over
        if (terminated() || truncated()) {
            System.out.println("Game Over! Starting new one...");
            System.out.println("Reward: " + transition.getReward());
            switchPlayerPostions(); // imported to call after new Transition because playerNumber gets changed and getReward depends on it.
        }
        return transition;
    }

    public void advanceEnemies() {
        int enemyPlayer = 0;
        while (stateObservation.getPlayer() != this.playerNumber && !terminated() && !truncated()) {

            System.out.println(stateObservation.getPlayer() + " Player: " + Arrays.toString(getObservation()) + "enemy: " + enemyAgents.get(enemyPlayer).getName());

            // availableActions = stateObservation.getAvailableActions();
            // Random random = new Random();
            // Types.ACTIONS enemyAction = availableActions.get(random.nextInt(availableActions.size()))
            Types.ACTIONS enemyAction = null;
            PlayAgent opponent = enemyAgents.get(enemyPlayer);
            if (opponent instanceof SB3Agent sb3Agent) {
                enemyAction = sb3Agent.selfPlay(stateObservation);
            } else {
                enemyAction = opponent.getNextAction2(stateObservation, false, true, true);
            }

            stateObservation.advance(enemyAction, null);

            enemyPlayer++;
        }
    }

    /**
        imported to call after new Transition because playerNumber gets changed and getReward depends on it.
     */
    public void switchPlayerPostions() {
        playerNumber = (playerNumber + 1) % stateObservationVectorFuncs.getNumPlayers();
        if (enemyAgents.size() <= 1) return;
        List<PlayAgent> enemies = new ArrayList<>();
        enemies.add(enemyAgents.get(enemyAgents.size() - 1));
        for (int i = 0; i < enemyAgents.size() - 1; i++) {
            enemies.add(enemyAgents.get(i));
        }
        enemyAgents = enemies;
    }

    public double eval(String opponentName, int numberOfGames) {
        return sb3Agent.eval(opponentName, numberOfGames);
    }

    public void trainingFinished() {
        System.out.println("Training finished");
        synchronized (sb3Agent) {
            sb3Agent.notify();
        }
    }
}
