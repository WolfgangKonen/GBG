package controllers.MCTSWrapper;

import TournamentSystem.TSTimeStorage;
import controllers.AgentBase;
import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.PassAction;
import controllers.MCTSWrapper.stateApproximation.Approximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import games.ObserverBase;
import games.StateObservation;
import params.ParOther;
import params.ParWrapper;
import tools.ScoreTuple;
import tools.Types;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

/**
 * PlayAgent that performs a Monte Carlo Tree Search (MCTS) to calculate the next action to be selected.
 * This agent wraps an approximator, which is used to evaluate game states in MCTS simulations.
 */
public final class MCTSWrapperAgent extends AgentBase implements PlayAgent, Serializable {
    private final int iterations;
    private final MCTS mcts;
    private final Approximator approximator;

    /**
     * Controls the amount of exploration moves in case ConfigWrapper.EXPLORATION_MODE==2
     * during training. <br>
     * m_epsilon = 0.0: no exploration (random) moves, <br>
     * m_epsilon = 0.1 (def.): 10% of the moves are random, and so forth
     * m_epsilon undergoes a linear change from {@code ParWrapper.epsInit}
     * to {@code ParWrapper.epsFinal}.
     * This is realized in {@link #adjustEpsilon()}.
     */
    protected double m_epsilon = 0.1;

    /**
     * m_EpsilonChangeDelta is the epsilon change per episode.
     */
    protected double m_epsilonChangeDelta = 0.001;

    private final Random rand; // generate random Numbers

    /**
     * @param iterations   Number of monte carlo iterations to be performed before the next action is selected.
     * @param c_puct       A PUCT parameter that controls the importance of exploring new nodes instead of exploiting known ones.
     * @param approximator A component that approximates the value of a given game state.
     * @param name         The name of the agent to be displayed.
     * @param maxDepth     Return from search, if depth==maxDepth. Set to -1, if search should not return because
     *                     of depth. (-1 will be transformed to Integer.MAX_VALUE.)
     * @param oPar         needed for parent class constructor
     */
    public MCTSWrapperAgent(
        final int iterations,
        final double c_puct,
        final Approximator approximator,
        final String name,
        final int maxDepth,
        final ParOther oPar
    ) {
        super(name,oPar);
        this.iterations = iterations;
        this.approximator = approximator;
        this.rand = new Random(System.currentTimeMillis()); //(System.currentTimeMillis()); (42);
        mcts = new MCTS(approximator, c_puct, maxDepth);
        setAgentState(AgentState.TRAINED);
        setWrParams(this.m_wrPar,this.getMaxGameNum());
    }

    /**
     * the action MCTSWrapper took the last time it was called
     */
    private int lastSelectedAction = Integer.MIN_VALUE;
    /**
     * the tree node (state) that MCTSWrapper reached the last time it was called
     */
    private MCTSNode lastSelectedNode;

    /**
     * reset agent: when starting a new episode, a new tree should be built. Therefore, set
     * {@link #lastSelectedNode}{@code =null}
     * (needed when re-using an existing agent, e.g. in competeNum episodes during a competition, see
     * {@link games.XArenaFuncs#competeNPlayer(PlayAgtVector, int, StateObservation, int, int, TSTimeStorage[], java.util.ArrayList)
     * XArenaFuncs.competeNPlayer})
     *
     */
    @Override
    public void resetAgent() {
        this.lastSelectedNode = null;
        this.lastSelectedAction = Integer.MIN_VALUE;
    }

    /**
     * Get the best next action and return it
     *
     * @param sob			current game state (is returned unchanged)
     * @param random		allow random action selection (exploration)
     * @param silent		whether to be silent
     * @return the best action (or random action). If several actions have the same
     * 		   score, break ties by selecting one of them at random.
     */
    @Override
    public Types.ACTIONS_VT getNextAction2(
        final StateObservation sob,
        final boolean random,
        final boolean silent
    ) {

        MCTSNode mctsNode;

        if (lastSelectedNode == null || !getParWrapper().getUseLastMCTS() || !sob.isDeterministicGame()) {
//      if (lastSelectedNode == null || !ConfigWrapper.USELASTMCTS || !sob.isDeterministicGame()) {
            // There is no search tree yet, or it is not valid for the current situation.
            // So a new MCTSNode is created from the given game state sob.
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
        }  // else (lastSelectedNode...)

        // At this point, mctsNode represents the current game state's node in the Monte Carlo search tree.

        // /WK/ a possible assertion, which turns out to be violated from time to time in RubiksCube.
        //      It seems always true in the Othello case --> TODO: Clarify the RubiksCube case!
        //assert (mctsNode.gameState.stringDescr().equals(sob.stringDescr())) : "Oops, state mismatch!";
        if (!mctsNode.gameState.stringDescr().equals(sob.stringDescr())) {
            System.err.println("Oops, state mismatch!");
            int dummy = 1;
        }

        int exploMode = this.getParWrapper().getWrapperMCTS_ExplorationMode(); //ConfigWrapper.EXPLORATION_MODE;
        if (!random) exploMode=0;               // always exploit, if random==false
        if (exploMode==2 && (rand.nextDouble() >= this.m_epsilon /*ConfigWrapper.epsilon*/) ) {
            exploMode=0;                        // exploit (the greedy case of EXPLORATION_MODE==2)
        }

        if (exploMode!=2) {     // in case exploMode==2, we do not need the MCTS search, because we take a random action anyway
            mcts.largestDepth=0;
            // Performs the given number of mcts iterations.
            for (int i = 0; i < iterations; i++) {
                mcts.search(mctsNode,0);
            }

            if (mctsNode.visitCounts.size()==0) {
                // As far as we see, this can only happen if iterations==1 (which is not a sensible choice),
                // but we leave it in as debug check for the moment.
                // We return always action 0 (which may or may not be a sensible choice)
                System.err.println("MCTSWrapperAgent.getNextAction2: *** Warning *** visitCounts.size = 0");
                System.err.println(mctsNode.gameState.stringDescr());
                return new Types.ACTIONS_VT(0,false,new double[sob.getNumAvailableActions()],0.0);
            }
        } // exploMode!=2

        // Selects the int value of one of the available actions:
        // If getNextAction2 is called with random==false (eval or play), exploMode is always 0.
        // If it is called with random==true (during training), exploMode depends on EXPLORATION_MODE (see above).
        switch (exploMode) {
            case 0 -> {
                // case EXPLORATION_MODE==0 or the greedy case of EXPLORATION_MODE==2: exploit, take action with max visit counts
                lastSelectedAction = mctsNode.visitCounts.entrySet().stream().max(
                        Comparator.comparingDouble(Map.Entry::getValue)
                ).orElseThrow().getKey();
                // Caches the child node belonging to the previously selected action.
                lastSelectedNode = mctsNode.childNodes.get(lastSelectedAction);
            }
            case 1 -> {
                // case EXPLORATION_MODE==1: sample an action proportional to visit counts
                lastSelectedAction = selectActionProportional(mctsNode);
                lastSelectedNode = mctsNode.childNodes.get(lastSelectedAction);
                //lastSelectedNode = null;    // do not reuse the tree if random action.
            }
            case 2 -> {
                // the random case of EXPLORATION_MODE==2:
                final var vTable2 = getVTableRandom(mctsNode);
                final var vBest2 = Arrays.stream(vTable2).max().orElse(Double.NaN);
                ScoreTuple scBest2 = new ScoreTuple(sob, vBest2);
                mctsNode.setMoveProbabilities(vTable2);
                int selectedAction = mctsNode.moveProbabilities.entrySet().stream().max(
                        Comparator.comparingDouble(Map.Entry::getValue)
                ).orElseThrow().getKey();
                lastSelectedNode = null;    // IMPORTANT: we have to reset lastSelectedNode after each random
                                            // action because the tree is then no longer valid (this reset may
                                            // affect adversely the quality of training)
                return new Types.ACTIONS_VT(
                        selectedAction,
                        true,
                        vTable2,
                        vBest2,
                        scBest2
                );
            }
            default ->  throw new RuntimeException("[MCTSWrapperAgent] Invalid choice for exploMode");
        } // switch(exploMode)

        // Pass states should not be cached.
        while (lastSelectedNode != null && lastSelectedNode.gameState.lazyMustPass.value()) {
            lastSelectedNode = lastSelectedNode.childNodes.get(new PassAction().getId());
        }

        // --- debug info RubiksCube ---
        //System.out.println("largestDepth = "+mcts.largestDepth);

        final var vTable = getVTableFor(mctsNode);
        final var vBest = Arrays.stream(vTable).max().orElse(Double.NaN);
        ScoreTuple scBest = new ScoreTuple(sob,vBest);
        return new Types.ACTIONS_VT(
            lastSelectedAction,
            exploMode==1,
            vTable,
            vBest,
            scBest
        );
    }   // getNextAction2

    // just a check whether this is faster than getVTableFor --> see MCTSWrapperAgentTest::getVTableForTest.
    // getVTable2For is 5x faster than getVTableFor, but it has only negligible effect on overall performance since
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

    public double[] getVTableRandom(final MCTSNode mctsNode) {
        return getDistributionOver(
            Arrays
                    .stream(mctsNode.gameState.getAvailableActionsIncludingPassActions())
                    .mapToDouble(action -> rand.nextDouble())
                    .toArray()
        );
    }

    private double[] getDistributionOver(final double[] values) {
        final var sum = Arrays.stream(values).sum();

        return Arrays.stream(values)
            .map(v -> v / sum)
            .toArray();
    }

    /**
     * Sample an action proportional to the visit count distribution in {@code mctsNode}.
     * @param mctsNode the node
     * @return the selected action id
     */
    private int selectActionProportional(final MCTSNode mctsNode) {
        ApplicableAction[] arrAction = mctsNode.gameState.getAvailableActionsIncludingPassActions();
        double r = rand.nextDouble();
        double[] cTab = getCumulTable2For(mctsNode,arrAction);
        int i=0;
        for (var action : arrAction) {
            if (cTab[i]<=r && r<cTab[i+1])
                return action.getId();  // the normal return (r has to be between 0.0=cTab[0] and 1.0=cTab[n])
            i++;
        }
        throw new RuntimeException("[selectActionProportional] We should not get here!");
    }

    /**
     * Return the cumulative distribution function for {@code mctsNode}'s visit counts
     * @param mctsNode the node
     * @return array of length (numberAvailableActions + 1)
     */
    public double[] getCumulTable2For(final MCTSNode mctsNode, ApplicableAction[] arrAction) {
        double v, sum = 0;
        int i=1;
        double[] cTab = new double[arrAction.length+1];
        cTab[0]=0;
        for (var action : arrAction) {
            v = mctsNode.visitCounts.getOrDefault(action.getId(), 0);
            cTab[i] = cTab[i-1]+v;
            sum += v;
            i++;
        }
        for (int j=0; j<cTab.length; j++) cTab[j] /= sum;
        return cTab;
    }

//    @Override
//    public double getScore(final StateObservation sob) {
//        return approximator.getScore(sob);
//    }
//    // /WK/ getScore is needed to make the interface happy, it is probably never really used

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
        cs = cs + "["/*+approximator.getName()+","*/+this.iterations+"]";
        return cs;
    }

    @Override
    public PlayAgent getWrappedPlayAgent() {
        return approximator.getWrappedPlayAgent();
    }

    @Override
    public boolean isWrapper() { return true; }

    /**
     * Train this agent for one episode, starting from state {@code so}.
     * Train the inner (wrapped) agent, but use the outer agent (the wrapper) for selecting the next action.
     *
     * @param so    the start state of the episode
     * @return	true, if agent raised a stop condition (deprecated)
     */
    @Override
    public boolean trainAgent(StateObservation so) {
        resetAgent();	// do not re-use last MCTS
        setWrParams(this.m_wrPar,this.getMaxGameNum());     // adjust m_epsilonChangeDelta to the actual maxGameNum
        return getWrappedPlayAgent().trainAgent(so,this);

    }

    /**
     * Set the agent parameters {@code m_epsilon} and {@code m_epsilonChangeDelta} from wrPar and maxGameNum
     * (only relevant for 'MCTS inside training loop' and {@code wrapperMCTS_exploMode==2})
     *
     * @param wrPar       the wrapper parameters, we need here epsInit and epsFinal
     * @param maxGameNum  number of training episodes
     */
    public void setWrParams(ParWrapper wrPar, int maxGameNum) {
        m_epsilon = wrPar.getWrapperMCTS_epsInit();
        m_epsilonChangeDelta = (m_epsilon - wrPar.getWrapperMCTS_epsFinal()) / maxGameNum;
    }

    public boolean instantiateAfterLoading() {
        super.instantiateAfterLoading();
        setWrParams(this.m_wrPar,this.getMaxGameNum());
        return true;
    }

    public void adjustEpsilon() {
        m_epsilon = m_epsilon - m_epsilonChangeDelta;   // linear decrease of m_epsilon
    }

}
