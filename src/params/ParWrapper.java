package params;

import java.io.Serial;
import java.io.Serializable;

import javax.swing.JPanel;

import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.MaxN2Wrapper;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.XArenaFuncs;

/**
 * <b>Wrapper parameter</b> settings for board games.
 * <p>
 * These parameters and their [defaults] are:
 * <ul>
 * <li><b>wrapperMode</b>: [0] 0: none, 1: (Expecti)Max-N wrapper, 2: MCTS(E)Wrapper
 * <li><b>wrapperNPly</b>: [1] n for (Expecti)Max-N wrapper: wrap the agent with an (Expecti)Max-N wrapper with
 * n plies of look-ahead. CAUTION: n &gt; 5 can dramatically slow down computation.
 * </ul>
 * The following parameters are only for {@link MCTSWrapperAgent}:
 * <ul>
 * <li><b>wrapperMCTS_iterations</b>: [0] i for MCTSWrapper: wrap the agent with a {@link MCTSWrapperAgent} with i iterations.
 * <li><b>wrapperMCTS_iter_train</b>: [0] i for MCTSWrapper: wrap the agent during training with a
 *                                    {@link MCTSWrapperAgent} with i iterations.
 * <li><b>wrapperMCTS_PUCT</b>: [1] PUCT parameter from [0,1] for {@link MCTSWrapperAgent}
 * <li><b>wrapperMCTS_depth</b>: [100] Depth parameter for {@link MCTSWrapperAgent}
 * <li><b>wrapperMCTS_exploMode</b>: [0] 0: none, 1: proportional to visit counts, 2: epsilon-greedy, only for {@link MCTSWrapperAgent}
 * <li><b>wrapperMCTS_epsInit</b>: [0.1] initial epsilon, only for Exploration Mode = 2
 * <li><b>wrapperMCTS_epsFinal</b>: [0.1] final epsilon, only for Exploration Mode = 2
 * <li><b>useSoftMax</b>: [true] <br>
 *      A switch for {@link PlayAgentApproximator}:
 *      <ul>
 *          <li> If false (recommended setting for RubiksCube 2x2x2), do not use softmax squashing for move probabilities.
 *          <li> If true (recommended setting for Othello, ConnectFour, RubiksCube 3x3x3), use softmax squashing.
 *      </ul>
 * <li><b>useLastMCTS</b>: [true] <br>
 *      A switch for {@link MCTSWrapperAgent}:
 *      <ul>
 *          <li>  If false (recommended setting for ConnectFour), force tree re-build in every call.
 *          <li>  If true (recommended setting for Othello, RubiksCube), re-use the tree (i.e. as in JS's master code) in
 *                  subsequent calls during one episode.
 *      </ul>
 * </ul>
 *
 * @see MCTSWrapperAgent
 * @see MaxN2Wrapper
 * @see WrapperParams
 * @see ConfigWrapper
 */
public class ParWrapper implements Serializable {
    public static int DEFAULT_WRAPPER_MODE = 0;
    public static int DEFAULT_WRAPPER_NPLY = 0;
    public static int DEFAULT_WRAPPER_MCTS_ITERATIONS = 0;
    public static int DEFAULT_WRAPPER_MCTS_ITER_TRAIN = 0;
    public static double DEFAULT_WRAPPER_MCTS_PUCT = 1;
    public static int DEFAULT_WRAPPER_MCTS_DEPTH = 100;
    public static int DEFAULT_WRAPPER_MCTS_EXPLOMODE = ConfigWrapper.EXPLORATION_MODE; //0;
    public static double DEFAULT_WRAPPER_MCTS_EPSINIT = ConfigWrapper.epsilon; //0.15;
    public static double DEFAULT_WRAPPER_MCTS_EPSFINAL = ConfigWrapper.epsilon; //0.15;
    public static boolean DEFAULT_USESOFTMAX = ConfigWrapper.USESOFTMAX; // true
    public static boolean DEFAULT_USELASTMCTS = ConfigWrapper.USELASTMCTS; //true;

    private int wrapperMode = DEFAULT_WRAPPER_MODE;
    private int wrapperNply = DEFAULT_WRAPPER_NPLY;
    private int wrapperMCTS_iterations = DEFAULT_WRAPPER_MCTS_ITERATIONS;
    private int wrapperMCTS_iter_train = DEFAULT_WRAPPER_MCTS_ITER_TRAIN;
    private double wrapperMCTS_PUCT = DEFAULT_WRAPPER_MCTS_PUCT;
    private int wrapperMCTS_depth = DEFAULT_WRAPPER_MCTS_DEPTH;
    private int wrapperMCTS_exploMode = DEFAULT_WRAPPER_MCTS_EXPLOMODE;
    private double wrapperMCTS_epsInit = DEFAULT_WRAPPER_MCTS_EPSINIT;      // the effective m_epsilon is set in
    private double wrapperMCTS_epsFinal = DEFAULT_WRAPPER_MCTS_EPSFINAL;    // MCTSWrapperAgent.adjustEpsilon()
    private boolean useSoftMax = DEFAULT_USESOFTMAX;
    private boolean useLastMCTS = DEFAULT_USELASTMCTS;

    /**
     * This member is only constructed when the constructor
     * {@link #ParWrapper(boolean) ParWrapper(boolean withUI)}
     * called with {@code withUI=true}. It holds the GUI for {@link ParWrapper}.
     */
    private transient WrapperParams wrparams = null;

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip containing this object will become
     * unreadable, or you have to provide a special version transformation)
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public ParWrapper() {    }

    public ParWrapper(boolean withUI) {
        if (withUI) {
            wrparams = new WrapperParams();
            wrparams.setFrom(this);             // set GUI parameter according to 'this'
        }
    }

    public ParWrapper(ParWrapper wp) {
        this.setFrom(wp);
    }

    public ParWrapper(WrapperParams wp) { this.setFrom(wp); }

    public void setFrom(ParWrapper wp) {
        this.wrapperMode = wp.getWrapperMode();
        this.wrapperNply = wp.getWrapperNPly();
        this.wrapperMCTS_iterations = wp.getWrapperMCTS_iterations();
        this.wrapperMCTS_iter_train = wp.getWrapperMCTS_iter_train();
        this.wrapperMCTS_PUCT = wp.getWrapperMCTS_PUCT();
        this.wrapperMCTS_depth = wp.getWrapperMCTS_depth();
        this.wrapperMCTS_exploMode = wp.getWrapperMCTS_ExplorationMode();
        this.wrapperMCTS_epsInit = wp.getWrapperMCTS_epsInit();
        this.wrapperMCTS_epsFinal = wp.getWrapperMCTS_epsFinal();
        this.useSoftMax = wp.getUseSoftMax();
        this.useLastMCTS = wp.getUseLastMCTS();

        if (wrparams !=null)
            wrparams.setFrom(this);
    }

    public void setFrom(WrapperParams wp) {
        this.wrapperMode = wp.getWrapperMode();
        this.wrapperNply = wp.getWrapperNPly();
        this.wrapperMCTS_iterations = wp.getWrapperMCTSIterations();
        this.wrapperMCTS_iter_train = wp.getWrapperMCTSIter_train();
        this.wrapperMCTS_PUCT = wp.getWrapperMCTS_PUCT();
        this.wrapperMCTS_depth = wp.getWrapperMCTS_depth();
        this.wrapperMCTS_exploMode = wp.getWrapperMCTS_ExplorationMode();
        this.wrapperMCTS_epsInit = wp.getWrapperMCTS_epsInit();
        this.wrapperMCTS_epsFinal = wp.getWrapperMCTS_epsFinal();
        this.useSoftMax = wp.getUseSoftMax();
        this.useLastMCTS = wp.getUseLastMCTS();

        if (wrparams !=null)
            wrparams.setFrom(this);
    }

    /**
     * Call this from {@link XArenaFuncs} constructAgent or fetchAgent to get the latest changes from GUI
     */
    public void pushFromWrParams() {
        if (wrparams !=null)
            this.setFrom(wrparams);
    }

    public JPanel getPanel() {
        if (wrparams !=null)
            return wrparams.getPanel();
        return null;
    }

    public int getWrapperMode() {
        return wrapperMode;
    }

    public int getWrapperNPly() {
        return wrapperNply;
    }

    public int getWrapperMCTS_iterations() { return wrapperMCTS_iterations; }

    public int getWrapperMCTS_iter_train() { return wrapperMCTS_iter_train; }

    public double getWrapperMCTS_PUCT() {
        return wrapperMCTS_PUCT;
    }

    public int getWrapperMCTS_depth() { return wrapperMCTS_depth; }

    public int getWrapperMCTS_ExplorationMode() { return wrapperMCTS_exploMode; }

    public double getWrapperMCTS_epsInit() {
        return wrapperMCTS_epsInit;
    }

    public double getWrapperMCTS_epsFinal() {
        return wrapperMCTS_epsFinal;
    }

    public boolean getUseSoftMax() {
        return useSoftMax;
    }

    public boolean getUseLastMCTS() { return useLastMCTS; }

    public void setWrapperMode(int mode) {
        this.wrapperMode=mode;
        if (wrparams !=null)
            wrparams.setWrapperMode(mode);
    }

    public void setWrapperNPly(int nply) {
        this.wrapperNply=nply;
        if (wrparams !=null)
            wrparams.setWrapperNPly(nply);
    }

    public void setWrapperMCTS_iterations(final int iterations) {
        this.wrapperMCTS_iterations =iterations;
        if (wrparams !=null)
            wrparams.setWrapperMCTSIterations(iterations);
    }

    public void setWrapperMCTS_iter_train(final int iterations) {
        this.wrapperMCTS_iter_train =iterations;
        if (wrparams !=null)
            wrparams.setWrapperMCTSIter_train(iterations);
    }

    public void setWrapperMCTS_PUCT(final double puct) {
        this.wrapperMCTS_PUCT =puct;
        if (wrparams !=null)
            wrparams.setWrapperMCTS_PUCT(puct);
    }

    public void setWrapperMCTS_depth(final int depth) {
        this.wrapperMCTS_depth =depth;
        if (wrparams !=null)
            wrparams.setWrapperMCTS_depth(depth);
    }

    public void setWrapperMCTS_ExplorationMode(int exploMode) {
        this.wrapperMCTS_exploMode=exploMode;
        if (wrparams !=null)
            wrparams.setWrapperMode(exploMode);
    }
    public void setWrapperMCTS_epsInit(final double epsInit) {
        this.wrapperMCTS_epsInit =epsInit;
        if (wrparams !=null)
            wrparams.setWrapperMCTS_epsInit(epsInit);
    }

    public void setWrapperMCTS_epsFinal(final double epsFinal) {
        this.wrapperMCTS_epsFinal =epsFinal;
        if (wrparams !=null)
            wrparams.setWrapperMCTS_epsFinal(epsFinal);
    }

    public void setUseSoftMax(boolean bUseSoft) {
        this.useSoftMax=bUseSoft;
        if (wrparams!=null)
            wrparams.setUseSoftMax(bUseSoft);
    }

    public void setUseLastMCTS(boolean bUseLast) {
        this.useLastMCTS=bUseLast;
        if (wrparams!=null)
            wrparams.setUseLastMCTS(bUseLast);
    }

    /**
     * Set sensible parameters for a specific agent and specific game. By "sensible
     * parameters" we mean parameter producing good results. Likewise, some parameter
     * choices may be enabled or disabled.
     *
     * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "Sarsa" (for {@link SarsaAgt})
     * @param gameName the string from {@link games.Arena#getGameName()}
     */
    public void setParamDefaults(String agentName, String gameName) {
        switch (agentName) {
            case "Sarsa":
                break;
            default:
                break;
        }
        switch (gameName) {
            case "RubiksCube":
            case "TicTacToe":
                switch (agentName) {
                    case "Sarsa":
                    case "Sarsa-4":
                    case "Qlearn-4":
                    case "TD-Ntuple-3":
                    case "TD-Ntuple-4":
                        break;
                    default:
                        break;
                }
                break;
            default:								//  all other
                break;
        }
    }

}  // class ParWrapper
