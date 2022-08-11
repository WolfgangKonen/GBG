package params;

import java.io.Serial;
import java.io.Serializable;

import javax.swing.JPanel;

import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MaxN2Wrapper;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;

/**
 *  Wrapper parameters for all agents
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 *
 *  @see MCTSWrapperAgent
 *  @see MaxN2Wrapper
 */
public class ParWrapper implements Serializable {
    public static int DEFAULT_WRAPPER_MODE = 0;
    public static int DEFAULT_WRAPPER_NPLY = 0;
    public static int DEFAULT_WRAPPER_MCTS_ITERATIONS = 0;
    public static double DEFAULT_WRAPPER_MCTS_PUCT = 1;
    public static int DEFAULT_WRAPPER_MCTS_DEPTH = 100;
    public static int DEFAULT_WRAPPER_MCTS_EXPLOMODE = 0;
    public static double DEFAULT_WRAPPER_MCTS_EPSINIT = 0.1;
    public static double DEFAULT_WRAPPER_MCTS_EPSFINAL = 0.1;

    private int wrapperMode = DEFAULT_WRAPPER_MODE;
    private int wrapperNply = DEFAULT_WRAPPER_NPLY;
    private int wrapperMCTSIterations = DEFAULT_WRAPPER_MCTS_ITERATIONS;
    private double wrapperMCTS_PUCT = DEFAULT_WRAPPER_MCTS_PUCT;
    private int wrapperMCTS_depth = DEFAULT_WRAPPER_MCTS_DEPTH;
    private int wrapperMCTS_exploMode = DEFAULT_WRAPPER_MCTS_EXPLOMODE;
    private double wrapperMCTS_epsInit = DEFAULT_WRAPPER_MCTS_EPSINIT;
    private double wrapperMCTS_epsFinal = DEFAULT_WRAPPER_MCTS_EPSFINAL;
    private boolean useSoftMax = true;
    private boolean useLastMCTS = true;

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
        if (withUI)
            wrparams = new WrapperParams();
    }

    public ParWrapper(ParWrapper wp) {
        this.setFrom(wp);
    }

    public ParWrapper(WrapperParams wp) { this.setFrom(wp); }

    public void setFrom(ParWrapper wp) {
        this.wrapperMode = wp.getWrapperMode();
        this.wrapperNply = wp.getWrapperNPly();
        this.wrapperMCTSIterations = wp.getWrapperMCTSIterations();
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
        this.wrapperMCTSIterations = wp.getWrapperMCTSIterations();
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
     * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
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

    public int getWrapperMCTSIterations() {
        return wrapperMCTSIterations;
    }

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

    public void setWrapperMCTSIterations(final int iterations) {
        this.wrapperMCTSIterations =iterations;
        if (wrparams !=null)
            wrparams.setWrapperMCTSIterations(iterations);
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
