package games.Hex;

import controllers.MCTS.MCTSAgentT;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import params.MCTSParams;
import tools.Types;

import java.util.Arrays;


public class EvaluatorHex extends Evaluator {
    private MinimaxAgent minimaxAgent = new MinimaxAgent(Types.GUI_AGENT_LIST[1]);
    private MCTSAgentT mctsAgent = null;
    private MCTSParams mctsParams;
    private RandomAgent randomAgent = new RandomAgent(Types.GUI_AGENT_LIST[2]);
    private double trainingThreshold = 0.8;
    private GameBoard gameBoard;
    private PlayAgent playAgent;
    private double lastResult = 0;
    private int m_mode = 0;
    private String m_msg = null;

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
        super(e_PlayAgent, stopEval);
        initEvaluator(e_PlayAgent, gb,1);
    }

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
        super(e_PlayAgent, stopEval);
        initEvaluator(e_PlayAgent, gb,mode);
        m_mode=mode;
    }

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        initEvaluator(e_PlayAgent, gb,mode);
        m_mode=mode;
    }

    // WK : questionable: last parameter is stopEval, but you call it with mode !?
    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard, int stopEval) {
        if (verbose == 1) {
            System.out.println("InitEval stopEval: " + stopEval);
        }
        //mctsParams = new MCTSParams();
        //mctsParams.setNumIter(2500);
        //mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), mctsParams);
        //mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], gameBoard.getStateObs(), params);
        //-- WK: not needed anymore and would not work, if initEvaluator is called with arguments
        //-- (null,null,0) which might happen for a dummy Evaluator
        this.gameBoard = gameBoard;
        this.playAgent = playAgent;
    }

    // WK: here you might select between different opponents based on m_mode
    // (should then also modify getAvailableModes, getPrintTitle and getPlotTitle appropriately)
    @Override
    protected boolean eval_Agent() {
        switch (m_mode) {
            case 0:  return competeAgainstMCTS(playAgent, gameBoard) >= trainingThreshold;
            case 1:  return competeAgainstRandom(playAgent, gameBoard) >= trainingThreshold;
            case 2:  return competeAgainstMinimax(playAgent, gameBoard) >= trainingThreshold;
            default: return false;
        }
    }

    private double competeAgainstMinimax(PlayAgent playAgent, GameBoard gameBoard){
        double[] res = XArenaFuncs.compete(playAgent, minimaxAgent, new StateObserverHex(), 2, verbose);
        double success = res[0];
        m_msg = this.getPrintString() + success;
        if (this.verbose>0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    private double competeAgainstMCTS(PlayAgent playAgent, GameBoard gameBoard){
        //mctsAgent.params.setNumIter(5000);
    	// /WK/ Bug fix: it is not safe to change this parameter of mctsAgent. I made it private now.
    	// Instead, the safe way is, to go always through the MCTSAgentT constructor:

        //MCTSParams params = new MCTSParams();
        //params.setNumIter(1000);
        //mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), mctsParams);

        // /KG/ Creating a new MCTSParams and MCTSAgentT object for every evaluation causes a GDI object leak which
        // eventually crashes the program after the GDI object limit is reached.

        //double[] res = XArenaFuncs.compete(playAgent, mctsAgent, new StateObserverHex(), 5, 0);//verbose);
        double[] res = {0};
        double success = res[0];
        m_msg = playAgent.getName()+": "+this.getPrintString() + success;
        if (this.verbose>0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    private double competeAgainstRandom(PlayAgent playAgent, GameBoard gameBoard){
        //double success = XArenaFuncs.competeBoth(playAgent, randomAgent, 10, gameBoard);
        double[] res = XArenaFuncs.compete(playAgent, randomAgent, new StateObserverHex(), 100, verbose);
        double success = res[0];
        if (this.verbose>0) System.out.println("Success against Random = " + success);
        lastResult = success;
        return success;
    }

    @Override
    public double getLastResult() {
        return lastResult;
    }

    @Override
    public boolean isAvailableMode(int mode) {
        int[] availableModes = getAvailableModes();
        for (int availableMode: availableModes) {
            if (mode == availableMode){
                return true;
            }
        }

        return false;
    }

    @Override
    public int[] getAvailableModes() {
        //MCTS not available
        return new int[]{1, 2};
    }

	@Override
	public int getQuickEvalMode() {
		return getAvailableModes()[0];
	}

	@Override
	public int getTrainEvalMode() {
		return getAvailableModes()[0];
	}

	@Override
	public int getMultiTrainEvalMode() {
		return getAvailableModes()[0];
	}

	@Override
	public String getPrintString() {
		switch (m_mode) {
		case 0:  return "success against MCTS (best is 1.0): ";
		case 1:  return "success against Random (best is 1.0): ";
		case 2:  return "success against Minimax (best is 1.0): ";
		default: return null;
		}
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "success against MCTS";
		case 1:  return "success against Random";
		case 2:  return "success against Minimax";
		default: return null;
		}
	}

	@Override
	public String getMsg() {
		return m_msg;
	}
}
