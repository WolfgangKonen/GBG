package controllers.MCTSExpWrapper;

import TournamentSystem.TSTimeStorage;
import controllers.AgentBase;
import controllers.MCTSExpWrapper.stateApproximation2.Approximator2;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.PlayAgtVector;
import games.StateObservation;
import tools.Types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * PlayAgent that performs a Monte Carlo Tree Search Expectimax (MCTSE) to calculate the next action to be selected.
 * This agent wraps an approximator, which is used to evaluate game states in MCTSE simulations.
 */
public final class MctseWrapperAgent extends AgentBase {
    private final int iterations;
    private final Mctse mcts;
    private final Approximator2 approximator;

    /**
     * @param iterations   Number of monte carlo iterations to be performed before the next action is selected.
     * @param c_puct       A PUCT parameter that controls the importance of exploring new nodes instead of exploiting known ones.
     * @param approximator A component that approximates the value of a given game state.
     * @param name         The name of the agent to be displayed.
     * @param maxDepth     Return from search, if depth==maxDepth. Set to -1, if search should not return because
     *                     of depth. (-1 will be transformed to Integer.MAX_VALUE.)
     */
    public MctseWrapperAgent(
        final int iterations,
        final double c_puct,
        final Approximator2 approximator,
        final String name,
        final int maxDepth
    ) {
        this.iterations = iterations;
        this.approximator = approximator;
        mcts = new Mctse(approximator, c_puct, maxDepth);
        setName(name);
        setAgentState(AgentState.TRAINED);
    }

    /**
     * the action MCTSWrapper took the last time it was called
     */
    private int lastSelectedAction = Integer.MIN_VALUE;
//    /**
//     * the tree node (state) that MCTSWrapper reached with {@link #lastSelectedNode} the last time it was called
//     */
//    private MctseExpecNode lastSelectedNode;

    /**
     * reset agent: when starting a new episode, a new tree should be built. Therefore, set
     * {@code lastSelectedNode=null}
     * (needed when re-using an existing agent, e.g. in competeNum episodes during a competition, see
     * {@link games.XArenaFuncs#competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[])
     * XArenaFuncs.competeNPlayer})
     *
     */
    @Override
    public void resetAgent() {
        //this.lastSelectedNode = null;
        this.lastSelectedAction = Integer.MIN_VALUE;
    }

    @Override
    public Types.ACTIONS_VT getNextAction2(
        final StateObservation sob,
        final boolean random,
        final boolean silent
    ) {
        MctseChanceNode mctsNode;

        mctsNode = new MctseChanceNode(new GameStateIncludingPass(sob));


        mcts.largestDepth=0;
        // Performs the given number of mcts iterations.
        for (int i = 0; i < iterations; i++) {
            mcts.search(mctsNode,0);
        }

        // Selects the int value of the action that leads to the child node that maximizes the visit count.
        // This value is also cached for further calls.
        if (mctsNode.visitCounts.size()==0) {
            // As far as we see, this can only happen if iterations==1 (which is not a sensible choice),
            // but we leave it in as debug check for the moment
            //System.err.println("MCTSWrapperAgent.getNextAction2: *** Warning *** visitCounts.size = 0");
            //System.err.println(mctsNode.gameState.stringDescr());
            return mctsNode.gameState.getNextAction(this.approximator);
        }
        lastSelectedAction = mctsNode.visitCounts.entrySet().stream().max(
            Comparator.comparingDouble(Map.Entry::getValue)
        ).orElseThrow().getKey();

//        // Caches the child node belonging to the previously selected action.
//        lastSelectedNode = mctsNode.childNodes.get(lastSelectedAction);
//
//        // Pass states should not be cached.
//        while (lastSelectedNode != null && lastSelectedNode.gameState.lazyMustPass.value()) {
//            lastSelectedNode = lastSelectedNode.childNodes.get(new PassAction().getId());
//        }

        // --- debug info RubiksCube ---
        //System.out.println("largestDepth = "+mcts.largestDepth);

        final var vTable = getVTableFor(mctsNode);
        final var vBest = Arrays.stream(vTable).max().orElse(Double.NaN);
        return new Types.ACTIONS_VT(
            lastSelectedAction,
            false,
            vTable,
            vBest
        );
    }   // getNextAction2

    public double[] getVTableFor(final MctseChanceNode mctsNode) {
        return getDistributionOver(
            Arrays
                .stream(mctsNode.gameState.getAvailableActionsIncludingPassActions())
                .mapToDouble(action -> mctsNode.visitCounts.getOrDefault(action.getId(), 0))
                .toArray()
        );
    }

    private double[] getDistributionOver(final double[] values) {
        final var sum = Arrays.stream(values).sum();

        return Arrays.stream(values)
            .map(v -> v / sum)
            .toArray();
    }

    @Override
    public double getScore(final StateObservation sob) {
        return approximator.getScore(sob);
    }
    // /WK/ getScore is needed to make the interface happy, it is probably never really used

    @Override
    public String stringDescr() {
        String cs = approximator.getName();
        cs = cs + "[iter="+this.iterations+"]";
        return cs;
    }

    @Override
    public String stringDescr2() {
        return getClass().getSimpleName()+"["+approximator.getName()+  ", iter="+this.iterations+"]" ;
    }

    // override AgentBase::getName()
    @Override
    public String getName() {
        String cs = super.getName();
        cs = cs + "["+approximator.getName()+","+this.iterations+"]";
        return cs;
    }

}
