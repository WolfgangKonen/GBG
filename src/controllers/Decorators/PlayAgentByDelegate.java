package controllers.Decorators;

import controllers.PlayAgent;
import games.Arena;
import games.StateObservation;
import params.ParOther;
import params.ParRB;
import tools.ScoreTuple;
import tools.Types;

import java.io.Serializable;

/**
 * This class encapsulates a PlayAgent instance and delegates all method calls to it.
 */
abstract class PlayAgentByDelegate implements PlayAgent, Serializable {

    private final PlayAgent delegateAgent;

    /**
     * @param delegateAgent PlayAgent instance to which all method calls are delegated.
     */
    protected PlayAgentByDelegate(final PlayAgent delegateAgent) {
        this.delegateAgent = delegateAgent;
    }

    //region Delegations
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
        return delegateAgent.getNextAction2(sob, random, silent);
    }

    public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
        return delegateAgent.getScoreTuple(sob, prevTuple);
    }

    public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
        return delegateAgent.estimateGameValueTuple(sob, prevTuple);
    }

    public boolean trainAgent(StateObservation so) {
        return delegateAgent.trainAgent(so);
    }

    public boolean trainAgent(StateObservation so, PlayAgent acting_pa) {
        return delegateAgent.trainAgent(so, acting_pa);
    }

    public String printTrainStatus() {
        return delegateAgent.printTrainStatus();
    }

    public boolean isTrainable() {
        return delegateAgent.isTrainable();
    }

    public boolean isRetrained() {
        return delegateAgent.isRetrained();
    }

    public void incrementDurationTrainingMs(long incr) {
        delegateAgent.incrementDurationTrainingMs(incr);
    }

    public void incrementDurationEvaluationMs(long incr) {
        delegateAgent.incrementDurationEvaluationMs(incr);
    }

    public long getDurationTrainingMs() {
        return delegateAgent.getDurationTrainingMs();
    }

    public long getDurationEvaluationMs() {
        return delegateAgent.getDurationEvaluationMs();
    }

    public boolean instantiateAfterLoading() {
        return delegateAgent.instantiateAfterLoading();
    }

    public void fillParamTabsAfterLoading(int n, Arena m_arena) {
        delegateAgent.fillParamTabsAfterLoading(n, m_arena);
    }

    public String stringDescr() {
        return delegateAgent.stringDescr();
    }

    public String stringDescr2() {
        return delegateAgent.stringDescr2();
    }

    public byte getSize() {
        return delegateAgent.getSize();
    }

    public int getMaxGameNum() {
        return delegateAgent.getMaxGameNum();
    }

    public int getGameNum() {
        return delegateAgent.getGameNum();
    }

    public long getNumLrnActions() {
        return delegateAgent.getNumLrnActions();
    }

    public long getNumTrnMoves() {
        return delegateAgent.getNumTrnMoves();
    }

    public int getMoveCounter() {
        return delegateAgent.getMoveCounter();
    }

    public void setMaxGameNum(int num) {
        delegateAgent.setMaxGameNum(num);
    }

    public void setGameNum(int num) {
        delegateAgent.setGameNum(num);
    }

    public ParOther getParOther() {
        return delegateAgent.getParOther();
    }

    public ParRB getParReplay() {
        return delegateAgent.getParReplay();
    }

    public int getNumEval() {
        return delegateAgent.getNumEval();
    }

    public void setNumEval(int num) {
        delegateAgent.setNumEval(num);
    }

    public void setStopEval(int num) {
        delegateAgent.setStopEval(num);
    }

    public void setWrapperParams(ParOther otherPar) {
        delegateAgent.setWrapperParams(otherPar);
    }

    public void setParOther(ParOther op) {
        delegateAgent.setParOther(op);
    }

    public void setParReplay(ParRB prb) {
        delegateAgent.setParReplay(prb);
    }

    public PlayAgent.AgentState getAgentState() {
        return delegateAgent.getAgentState();
    }

    public void setAgentState(PlayAgent.AgentState aState) {
        delegateAgent.setAgentState(aState);
    }

    public String getAgentFile() {
        return delegateAgent.getAgentFile();
    }

    public void setAgentFile(String agtFile) {
        delegateAgent.setAgentFile(agtFile);
    }

    public void resetAgent() {
        delegateAgent.resetAgent();
    }

    public String getName() {
        return delegateAgent.getName();
    }

    public void setName(String name) {
        delegateAgent.setName(name);
    }

    public boolean isWrapper() {
        return delegateAgent.isWrapper();
    }

    public PlayAgent getWrappedPlayAgent() {
        return delegateAgent.getWrappedPlayAgent();
    }
    //endregion
}