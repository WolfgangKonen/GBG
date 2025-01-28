package controllers.SB3;

import controllers.PlayAgent;
import games.BoardVector;
import games.StateObservation;
import games.StateObservationVectorFuncs;
import games.XNTupleFuncs;
import tools.Types;

import java.io.Serializable;
import java.util.*;

public class RLEnvironmentConnector {
    private StateObservation stateObservation;
    private XNTupleFuncs xnTupleFuncs;
    private StateObservationVectorFuncs stateObservationVectorFuncs;
    private List<PlayAgent> enemyAgents;
    private final SB3Agent sb3Agent;
    private final boolean switchPlayerPostions = true;
    private boolean selfPlay;

    private int playerNumber;

    public RLEnvironmentConnector(XNTupleFuncs xnTupleFuncs, StateObservationVectorFuncs stateObservationVectorFuncs, List<PlayAgent> enemyAgents, SB3Agent sb3Agent, boolean selfPlay, int playerNumber) {
        this.xnTupleFuncs = xnTupleFuncs;
        this.stateObservationVectorFuncs = stateObservationVectorFuncs;
        this.sb3Agent = sb3Agent;
        this.selfPlay = selfPlay;
        this.playerNumber = playerNumber;

        if (selfPlay) {
            setSelfPlayTrue();
        }
        else {
            this.enemyAgents = enemyAgents;
        }
    }

    public void setSelfPlayTrue() {
        selfPlay = true;
        for (int i = 0; i < xnTupleFuncs.getNumPlayers(); i++) {
            this.enemyAgents = new LinkedList<>();
            this.enemyAgents.add(sb3Agent);
        }
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

    public int[] getPlayerVector(StateObservation stateObservation) {
        int[] vector = new int[stateObservation.getNumPlayers()];
        vector[stateObservation.getPlayer()] = 1;
        return vector;
    }

    public int[] getObservation() {
        // return oneHot ? getOneHotStandardObservation() : getStandardPerspectiveObservation();

        /*List<Double> observationVector = new ArrayList<>(); // TODO: Where put this logic?
        BoardVector boardVector = useStandardPerspective ? xnTupleFuncs.getStandardPerspectivesBoardVector(stateObservation) :
                xnTupleFuncs.getBoardVector(stateObservation);
        if (oneHot) boardVector = xnTupleFuncs.getOneHotBoardVector(boardVector);

        for(int i: boardVector.bvec) {
            observationVector.add((double) i);
        }
        if (!useStandardPerspective) {
            for (int i: getPlayerVector(stateObservation)) {
                observationVector.add((double) i);
            }
        }*/


        return stateObservationVectorFuncs.getStateObservationVector(stateObservation);
    }

    public void initiateNewGame(StateObservation stateObservation) {
        this.stateObservation = stateObservation;
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
        this.stateObservation = sb3Agent.getStartSate();
        advanceEnemies();
        return new FirstObservation(getObservation(), getInfo());
    }

    public Transition step(int action) {
        // advanceEnemies(); // Just to be sure
        List<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
        Types.ACTIONS chosenAction = new Types.ACTIONS(action);

        System.out.println(stateObservation.getPlayer() + " Player: " + Arrays.toString(getObservation()) + " " + getObservation().length + " before");

        // When caught cheating return same observation and min game score. StateObservation gets reset afterward when sb3 calls reset.
        if(!availableActions.contains(chosenAction)) {
            Transition transition = new Transition(getObservation(), stateObservation.getMinGameScore(), true, truncated(), getInfo());
            if (switchPlayerPostions) switchPlayerPostions();
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

        Transition transition = new  Transition(getObservation(), getReward(), terminated(), truncated(), getInfo());
        if (switchPlayerPostions && (terminated() || truncated())) {
            System.out.println("Game Over! Starting new one...");
            switchPlayerPostions();
        }
        return transition;
    }

    public void advanceEnemies() {
        int enemyPlayer = 0;
        while (stateObservation.getPlayer() != this.playerNumber && !terminated() && !truncated()) {

            System.out.println(stateObservation.getPlayer() + " Player: " + Arrays.toString(getObservation()) + "enemy");

            // availableActions = stateObservation.getAvailableActions();
            // Random random = new Random();
            // Types.ACTIONS enemyAction = availableActions.get(random.nextInt(availableActions.size()))
            Types.ACTIONS enemyAction = enemyAgents.get(enemyPlayer).getNextAction2(stateObservation, false, true, true);

            stateObservation.advance(enemyAction, null);

            enemyPlayer++;
        }
    }

    public void switchPlayerPostions() {
        playerNumber = (playerNumber + 1) % xnTupleFuncs.getNumPlayers();
        if (enemyAgents.size() <= 1) return;
        List<PlayAgent> enemys = new ArrayList<>();
        enemys.add(enemyAgents.get(enemyAgents.size() - 1));
        for (int i = 1; i < enemyAgents.size() - 1; i++) {
            enemys.add(enemyAgents.get(i));
        }
        enemyAgents = enemys;
    }

    public void trainingFinished() {
        System.out.println("Training finished");
        synchronized (sb3Agent) {
            sb3Agent.notify();
        }
    }
}
