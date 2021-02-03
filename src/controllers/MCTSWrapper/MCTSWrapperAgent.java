package controllers.MCTSWrapper;

import TournamentSystem.TSTimeStorage;
import controllers.AgentBase;
import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.PassAction;
import controllers.MCTSWrapper.stateApproximation.Approximator;
import controllers.PlayAgtVector;
import games.ObserverBase;
import games.StateObservation;
import tools.Types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * PlayAgent that performs a Monte Carlo Tree Search (MCTS) to calculate the next action to be selected.
 * This agent wraps an approximator, which is used to evaluate game states in MCTS simulations.
 */
public final class MCTSWrapperAgent extends AgentBase {
    private final int iterations;
    private final MCTS mcts;
    private final Approximator approximator;

    /**
     * @param iterations   Number of monte carlo iterations to be performed before the next action is selected.
     * @param c_puct       A PUCT parameter that controls the importance of exploring new nodes instead of exploiting known ones.
     * @param approximator A component that approximates the value of a given game state.
     * @param name         The name of the agent to be displayed.
     * @param maxDepth     Return from search, if depth==maxDepth. Set to -1, if search should not return because
     *                     of depth. (-1 will be transformed to Integer.MAX_VALUE.)
     */
    public MCTSWrapperAgent(
        final int iterations,
        final double c_puct,
        final Approximator approximator,
        final String name,
        final int maxDepth
    ) {
        this.iterations = iterations;
        this.approximator = approximator;
        mcts = new MCTS(approximator, c_puct, maxDepth);
        setName(name);
        setAgentState(AgentState.TRAINED);
    }

    /**
     * the action MCTSWrapper took the last time it was called
     */
    private int lastSelectedAction = Integer.MIN_VALUE;
    /**
     * the tree node (state) that MCTSWrapper reached with {@link #lastSelectedNode} the last time it was called
     */
    private MCTSNode lastSelectedNode;

    /**
     * reset agent: when starting a new episode, a new tree should be built. Therefore, set
     * {@link #lastSelectedNode}{@code =null}
     * (needed when re-using an existing agent, e.g. in competeNum episodes during a competition, see
     * {@link games.XArenaFuncs#competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[])
     * XArenaFuncs.competeNPlayer})
     *
     */
    @Override
    public void resetAgent() {
        this.lastSelectedNode = null;
        this.lastSelectedAction = Integer.MIN_VALUE;
    }

    @Override
    public Types.ACTIONS_VT getNextAction2(
        final StateObservation sob,
        final boolean random,
        final boolean silent
    ) {
        MCTSNode mctsNode;

        if (lastSelectedNode == null || !ConfigWrapper.USELASTMCTS) {
            // There is no search tree yet.
            // So a new mcts node is created from the given game state sob.
            mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
        } else {
            // There already exists a search-tree, which was built in a previous call to MCTSWrapper in this episode.

            // In the following, the moves made since the last cache of the search tree are reconstructed
            // on the last saved mcts node. This should result in the mcts node belonging to the current state of the game.
            final var pastActions = ((ObserverBase) sob).getLastMoves();    // /WK/ new

            var node = lastSelectedNode;
//            if (node!=null) {                       // /WK/ debug
//                if (node.gameState.isFinalGameState()) {
//                    System.err.println("Unexpected final state via lastSelectedNode!");
//                    System.err.println("pastActions index: "+pastActions.indexOf(lastSelectedAction)
//                            +",  pastActions.size:"+pastActions.size());
//                }
//            }

            // The former for-loop statement
            //     for (int i = 1 + pastActions.indexOf(lastSelectedAction); i < pastActions.size(); i++) { ...
            // was correct for Othello but WRONG for games with recurring actions (like ConnectFour, RubiksCube, ...).
            // Because if pastActions contains an action (e.g. 'take column 3' in ConnectFour) several times, indexOf()
            // might select the wrong one. Even lastIndexOf() will not help, we mean to grab the last action of
            // MCTSWrapperAgent, but the opponent might have taken the same action afterwords.
            //
            // We follow thus another solution: We store in sz the size of lastMoves at lastSelectedAction-time and
            // start with i=sz in the pastActions-loop (pastActions = lastMoves of sob):
            int sz=lastSelectedNode.getLastMoves().size();
            // assert (sz==1 + pastActions.indexOf(lastSelectedAction)) : "Oops, sz mismatch!";
            // The above assertion is only correct for games like Othello, where each action appears only once (!).
            // It was used in the Othello-case to check that the sz-logic is correct.
            assert (sz>0) : "Wrongly sz=lastMoves.size()==0. [Probably advance does not call super.addToLastMoves.]";
            assert (pastActions.get(sz-1)==lastSelectedAction) : "Oops, action mismatch!";  // /WK/ general check
            for (int i = sz; i < pastActions.size(); i++) {
                node = node.childNodes.get(pastActions.get(i));
                //node = node.childNodes.getOrDefault(pastActions.get(i), null);   // alternative suggestion from JS

//              if (node!=null) {
//                  if (node.gameState.isFinalGameState()) {        // /WK/ debug
//                      System.err.println("*** Unexpected final state in child node!");
//                  }
//              }

                if (node == null) {
                    // In this case the current game state is not present in the previously expanded search tree,
                    // because it was not relevant enough in the MCTS to be expanded.
                    // In this case a new Monte Carlo search tree is created based on sob.
                    node = new MCTSNode(new GameStateIncludingPass(sob));
//                  if (node.gameState.isFinalGameState()) {        // /WK/ debug
//                        System.err.println("*** Unexpected final state in (node==null)-branch!");
//                  }
                    break;
                }
//              if (node.gameState.isFinalGameState()) {            // /WK/ debug, new if-branch for RubiksCube
//                    node = new MCTSNode(new GameStateIncludingPass(sob));
//                    if (node.gameState.isFinalGameState()) {
//                        System.err.println("Unexpected final state in final-state-branch!");
//                    }
//                    break;
//              }
            }
            mctsNode = node;
            if (mctsNode.gameState.isFinalGameState()) {        // WK, fix for RubiksCube
//                System.err.println("Unexpected final state for mctsNode! --> Replacing");
//                mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
                System.err.println("Unexpected final state for mctsNode! --> NOT Replacing");
            }
        }

        // At this point mctsNode represents the current game state's node in the monte carlo search tree.

        // /WK/ a possible assertion, which turns out to be violated from time to time in RubiksCube.
        //      It seems always true in the Othello case --> TODO: Clarify the RubiksCube case!
        //assert (mctsNode.gameState.stringDescr().equals(sob.stringDescr())) : "Oops, state mismatch!";
        if (!mctsNode.gameState.stringDescr().equals(sob.stringDescr())) {
            System.err.println("Oops, state mismatch!");
            int dummy = 1;
        }

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
            System.err.println("MCTSWrapperAgent.getNextAction2: *** Warning *** visitCounts.size = 0");
            System.err.println(mctsNode.gameState.stringDescr());
            return new Types.ACTIONS_VT(0,false,new double[sob.getNumAvailableActions()],0.0);
        }
        lastSelectedAction = mctsNode.visitCounts.entrySet().stream().max(
            Comparator.comparingDouble(Map.Entry::getValue)
        ).orElseThrow().getKey();
        // Caches the child node belonging to the previously selected action.
        lastSelectedNode = mctsNode.childNodes.get(lastSelectedAction);

        // Pass states should not be cached.
        while (lastSelectedNode != null && lastSelectedNode.gameState.lazyMustPass.value()) {
            lastSelectedNode = lastSelectedNode.childNodes.get(new PassAction().getId());
        }

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

    // just a check whether this is faster than getVTableFor --> see MCTSWrapperAgentTest::getVTableForTest.
    // getVTable2For is 5x faster than getVTableFor, but it it has only negligible effect on overall performance since
    // it is called seldom.
    public double[] getVTable2For(final MCTSNode mctsNode) {
        ApplicableAction[] arrAction = mctsNode.gameState.getAvailableActionsIncludingPassActions();
        double[] vTab = new double[arrAction.length];
        double v, sum = 0;
        int i=0;
        for (var action : arrAction) {
            v = mctsNode.visitCounts.getOrDefault(action.getId(), 0);
            sum += v;
            vTab[i++] = v;
        }
        for (int j=0; j<vTab.length; j++) vTab[j] /= sum;
        return vTab;
    }

    public double[] getVTableFor(final MCTSNode mctsNode) {
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
