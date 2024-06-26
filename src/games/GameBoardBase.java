package games;

import controllers.PlayAgent;

import java.util.LinkedList;
import java.util.Random;

abstract public class GameBoardBase implements GameBoard {
    protected Arena m_Arena;
    private final LinkedList<StateObservation> queue;
    private boolean arenaActReq = false;

    public GameBoardBase(Arena arena){
        m_Arena = arena;
        queue = new LinkedList<>();
    }

    /**
     * things to be initialized prior to starting a training
     */
    @Override
    public void initialize() {

    }

    /**
     * update game-specific parameters from {@link Arena}'s param tabs
     */
    @Override
    public void updateParams() {

    }

    /**
     * things to be done when disposing a GameBoard object
     */
    @Override
    public void destroy() {

    }

    @Override
    public void setActionReq(boolean actionReq) {
        arenaActReq = actionReq;
    }

    /**
     * Is an action requested from Arena (i.e. was human interaction done)?
     *
     * @return  true: if an action is requested from Arena or ArenaTrain
     * 			false: no action requested from Arena, next action has to come
     * 			from GameBoard (e.g. user input / human move)
     */
    @Override
    public boolean isActionReq() {
        return arenaActReq;
    }

    @Override
    public Arena getArena() {
        return m_Arena;
    }

    @Override
    public LinkedList<StateObservation> getStateQueue() {
        return queue;
    }

    public StateObservation rollbackFromQueue() {
        LinkedList<StateObservation> q = this.getStateQueue();
        StateObservation backstate = null;
        if (q != null) {
            if (q.size() > 1) {
                q.removeFirst();
                backstate = q.removeFirst();
                // we remove backstate from the queue, because it will later, in the
                // gb.isActionReq()-branch of InspectGame, again be added to the queue
                System.out.println("Returned to state with player " + backstate.getPlayer()
                        + " and string " + backstate.stringDescr());
                this.setStateObs(backstate);	// set m_gb.m_so to backstate
                this.setActionReq(true);	// loop with the newly set state of m_gb through the
                                            // gb.isActionReq()-branch of InspectGame --> sets the
                                            // stored actions and their values correctly (with the
                                            // agent only available in InspectGame)
            }
        }
        return backstate;
    }


    //
    // all other methods from GameBoard are currently abstract in GameBoardBase:
    //

    /**
     * Set {@link GameBoard}'s state to the default start state and clear the game board
     *
     * @param boardClear whether to clear the board
     * @param vClear     whether to clear the value table
     * @param cmpRand    if non-null, use this (reproducible) RNG instead of StateObservation's RNG
     */
    @Override
    abstract public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand);

    /**
     * Update {@link GameBoard}'s state to the given state {@code so} and show it and the associated values (labels)
     * on the game board.
     *
     * @param so                   the game state
     * @param withReset            if true, reset the board prior to updating it to state so
     * @param showValueOnGameboard if true, show the game values for the available actions
     *                             (only if they are stored in state {@code so}).
     */
    @Override
    abstract public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard);

    @Override
    abstract public void showGameBoard(Arena arena, boolean alignToMain);

    @Override
    abstract public void toFront();

    @Override
    abstract public void enableInteraction(boolean enable);

    /**
     * If logs and agents should be placed in a subdirectory (e.g. Hex: BoardSize), then
     * this method returns a suitable string. If it returns {@code null}, then logs and
     * agents are placed in the {@code gameName} directory directly.
     *
     * @return subdir string
     */
    @Override
    abstract public String getSubDir();

    /**
     * @param cmpRand if non-null, use this (reproducible) RNG instead of StateObservation's RNG
     * @return the 'empty-board' start state
     */
    @Override
    abstract public StateObservation getDefaultStartState(Random cmpRand);

    @Override
    abstract public void setStateObs(StateObservation so);

    @Override
    abstract public StateObservation getStateObs();

    @Override
    abstract public StateObservation chooseStartState(PlayAgent pa);

    @Override
    abstract public StateObservation chooseStartState();
}
