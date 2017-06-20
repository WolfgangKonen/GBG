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


public class EvaluatorHex extends Evaluator {
    private MinimaxAgent minimaxAgent = new MinimaxAgent(Types.GUI_AGENT_LIST[1]);
    private MCTSAgentT mctsAgent;
    private RandomAgent randomAgent = new RandomAgent(Types.GUI_AGENT_LIST[2]);
    private double trainingThreshold = 0.8;
    private GameBoard gameBoard;
    private PlayAgent playAgent;
    private double lastResult = 0;

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
        super(e_PlayAgent, stopEval);
        initEvaluator(e_PlayAgent, gb,1);
    }

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
        super(e_PlayAgent, stopEval);
        initEvaluator(e_PlayAgent, gb,mode);
    }

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        initEvaluator(e_PlayAgent, gb,mode);
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard, int stopEval) {
        if (verbose == 1) {
            System.out.println("InitEval stopEval: " + stopEval);
        }
        MCTSParams params = new MCTSParams();
        mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], gameBoard.getStateObs(), params);
        this.gameBoard = gameBoard;
        this.playAgent = playAgent;
    }

    @Override
    protected boolean eval_Agent() {
        //return competeAgainstMinimax(playAgent, gameBoard) >= trainingThreshold;
        return competeAgainstMCTS(playAgent, gameBoard) >= trainingThreshold;
        //return competeAgainstRandom(playAgent, gameBoard) >= trainingThreshold;
    }

    private double competeAgainstMinimax(PlayAgent playAgent, GameBoard gameBoard){
        double[] res = XArenaFuncs.compete(playAgent, minimaxAgent, new StateObserverHex(), 2, verbose);
        double success = res[0];
        if (this.verbose>0) System.out.println("Success against minimax = " + success);
        lastResult = success;
        return success;
    }

    private double competeAgainstMCTS(PlayAgent playAgent, GameBoard gameBoard){
        //mctsAgent.params.setNumIter(5000);
    	// /WK/ Bug fix: it is not safe to change this parameter of mctsAgent. I made it private now.
    	// Instead, the safe way is, to go always through the MCTSAgentT constructor:
        MCTSParams params = new MCTSParams();
        params.setNumIter(1000);
        mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), params);

        double[] res = XArenaFuncs.compete(playAgent, mctsAgent, new StateObserverHex(), 5, 0);//verbose);
        double success = res[0];
        if (this.verbose>0) System.out.println("Success against MCTS = " + success);
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
        //Only one mode currently
        return true;
    }

    @Override
    public int[] getAvailableModes() {
        //Only one mode currently
        return new int[0];
    }
}
