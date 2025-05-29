package controllers.SB3;

import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.SB3.HttpServer.RLEnvironmentServer;
import games.*;
import tools.ScoreTuple;
import tools.Types;
import tools.WinTieCounter;

import java.util.*;

/**
 * <p>This class is used by the {@link RLEnvironmentServer} to provide all the necessary function to emulate a gymnasium gym.Env for the GBGEnvironmentClient on the python side.</p>
 * <p>When training an SB3 agent the game loop no longer gets controlled by the {@link XArenaFuncs}, rather the SB3 agent progress the game by using the /step http endpoint of the {@link RLEnvironmentServer}
 * that calls this {@link #step(int)} methode to advance the player controlled by SB3 as well as the opponents either through self play or the other chosen opponents by the user.</p>
 */
public class RLEnvironmentService {
    private StateObservation stateObservation;
    private StateObservation firstStateObservation = null;
    private StateObservationVectorFuncs stateObservationVectorFuncs;
    private List<PlayAgent> opponentAgents;
    private final SB3AgentProxy sb3AgentProxy;
    private PlayAgent evalOpponent;

    private int playerNumber;

    public RLEnvironmentService(StateObservationVectorFuncs stateObservationVectorFuncs, List<PlayAgent> opponentAgents, SB3AgentProxy sb3AgentProxy, int playerNumber, PlayAgent evalOpponent) {
        this.stateObservationVectorFuncs = stateObservationVectorFuncs;
        this.sb3AgentProxy = sb3AgentProxy;
        this.playerNumber = playerNumber;
        this.opponentAgents = opponentAgents;
        this.evalOpponent = evalOpponent;
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

    /**
     * Serves the /reset endpoint.
     * Serves the gymnasium's gym.Env interface rest() methode.
     * @return The {@link FirstObservation} containing the first observation and info.
     */
    public FirstObservation reset() {
        this.stateObservation = sb3AgentProxy.afterGame();
        if (firstStateObservation == null) {
            this.firstStateObservation = stateObservation.copy();
        }
        advanceEnemies();
        return new FirstObservation(getObservation(), getInfo());
    }

    /**
     * Serves the /availableActions endpoint. Currently only gets used by MaskablePPO.
     * @return available actions at current time. List of ints representing the available actions.
     */
    public int[] getAvailableActions() {
        return stateObservationVectorFuncs.getAvailableActions(stateObservation);
    }

    /**
     * Serves the /step endpoint.
     * Advance the player with the action chosen by the SB3 agent as well as the opponents either through self play or the other chosen opponents by the user.
     * Serves the gymnasium's gym.Env interface step(action: int) methode.
     * @param action
     * @return A {@link Transition} containing the new state, reward, if the game has ended, as well as an info about the game.
     */
    public Transition step(int action) {
        // advanceEnemies(); // Just to be sure
        List<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
        Types.ACTIONS chosenAction = new Types.ACTIONS(action);

        sb3AgentProxy.incrementMoves();
        // When caught cheating return same observation and min game score. StateObservation gets reset afterward when sb3 calls reset.
        if (!availableActions.contains(chosenAction)) {
            Transition transition = new Transition(getObservation(), stateObservation.getMinGameScore(), true, truncated(), getInfo());
            switchPlayerPostions(); // Call after Games has ended and after new Transitions was instantiated.
            return transition;
        }

        try {
            stateObservation.advance(chosenAction, null);
        } catch (AssertionError e) {
            e.printStackTrace();
            sb3AgentProxy.notify();
        }

        advanceEnemies();

        Transition transition = new Transition(getObservation(), getReward(), terminated(), truncated(), getInfo());
        // if Game over
        if (terminated() || truncated()) {
            switchPlayerPostions(); // Call after Games has ended and after new Transitions was instantiated.
        }
        return transition;
    }

    /**
     * Advances the curren opponent in the rotation.
     */
    public void advanceEnemies() {
        int enemyPlayer = 0;
        while (stateObservation.getPlayer() != this.playerNumber && !terminated() && !truncated()) {
            // availableActions = stateObservation.getAvailableActions();
            // Random random = new Random();
            // Types.ACTIONS enemyAction = availableActions.get(random.nextInt(availableActions.size()))
            Types.ACTIONS enemyAction = null;
            PlayAgent opponent = opponentAgents.get(enemyPlayer);
            if (opponent instanceof SB3AgentProxy sb3AgentProxy) {
                enemyAction = sb3AgentProxy.selfPlay(stateObservation);
            } else {
                enemyAction = opponent.getNextAction2(stateObservation, false, true, true);
            }

            stateObservation.advance(enemyAction, null);

            enemyPlayer++;
        }
    }

    /**
     * Call after Games has ended and after new Transitions was instantiated.
     * Rotates the list of opponent agents, so next game round another agent gets used as an opponent and the main player controlled by the sb3 agent is in another player position.
     * This serves to get a greater variety in opponent strategies for training.
     */
    public void switchPlayerPostions() {
        playerNumber = (playerNumber + 1) % stateObservationVectorFuncs.getNumPlayers();
        if (opponentAgents.size() <= 1) return;
        List<PlayAgent> opponent = new ArrayList<>();
        opponent.add(opponentAgents.get(opponentAgents.size() - 1));
        for (int i = 0; i < opponentAgents.size() - 1; i++) {
            opponent.add(opponentAgents.get(i));
        }
        opponentAgents = opponent;
    }

    /**
     * Starts an evaluation with the default opponent chosen by the user in the parameter tab.
     * @param numberOfGames for each player on each playerPostion (e.g. X or O in TicTacToe). So Total number of games = numberOfGames * players.
     * @return The average reward in the number of games played.
     */
    public EvalResults evalWithDefaultOpponent(int numberOfGames) {
        return eval(evalOpponent, numberOfGames);
    }

    /**
     * Starts an evaluation.
     * @param numberOfGames for each player on each playerPostion (e.g. X or O in TicTacToe). So Total number of games = numberOfGames * players.
     * @return The average reward in the number of games played.
     */
    public EvalResults eval(PlayAgent opponent, int numberOfGames) {
        StateObservation startState = getStartSate();
        WinTieCounter winTieCounter = new WinTieCounter(startState.getNumPlayers());
        ScoreTuple scoreTuple = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(sb3AgentProxy, opponent), startState, numberOfGames, 0, null, null, true, winTieCounter);

        EvalResults evalResults = new EvalResults(winTieCounter.wins[0], winTieCounter.tie, winTieCounter.getLosses(0), scoreTuple.scTup[0]);
        return evalResults;
    }

    /**
     * Gets called after SB3 has finished it's training. Wakes up the Main thread that was paused in {@link SB3AgentProxy#learn(XArenaFuncs.GameProgressor)}.
     */
    public void trainingFinished() {
        System.out.println("Training finished");
        synchronized (sb3AgentProxy) {
            sb3AgentProxy.notify();
        }
    }

    /**
     *
     * @return A fresh {@link StateObservation} for a new game round.
     */
    private StateObservation getStartSate() {
        return firstStateObservation.copy();
    }
}


