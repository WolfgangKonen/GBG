package games.Hex;

import controllers.MCTS.MCTSAgentT;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import params.ParMCTS;
import tools.Types;
import tools.Types.ACTIONS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EvaluatorHex extends Evaluator {
    //minimaxAgent is static so the search tree does not have to be rebuilt every time a new evaluator is created.
    //However, this prevents minimax parameters from being adjusted while the program is running. Set needed tree depth
    //during compile time.
    private static MinimaxAgent minimaxAgent = new MinimaxAgent(Types.GUI_AGENT_LIST[1]);
    private final String logDir = "logs/Hex/train";
    protected int verbose = 0;
    private MCTSAgentT mctsAgent = null;
    private RandomAgent randomAgent = new RandomAgent(Types.GUI_AGENT_LIST[2]);
    private double trainingThreshold = 0.8;
    private GameBoard gameBoard;
    private PlayAgent playAgent;
    private double lastResult = 0;
    private int m_mode = 0;
    private String m_msg = null;
    /**
     * logResults toggles logging of training progress to a csv file located in {@link #logDir}
     */
    private boolean logResults = false;
    private boolean fileCreated = false;
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        m_mode = mode;
        if (verbose == 1) {
            System.out.println("Using evaluation mode " + mode);
        }
        initEvaluator(e_PlayAgent, gb);
        if (m_mode == 2 && minimaxAgent.getDepth() < HexConfig.TILE_COUNT) {
            System.out.println("Using minimax with limited tree depth: " +
                    minimaxAgent.getDepth() + " used, " + HexConfig.TILE_COUNT + " needed");
        }
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        this.playAgent = playAgent;
    }

    @Override
    protected boolean eval_Agent() {
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
            return true;
        }

        //Disable logging for the final evaluation after training
        if (!fileCreated && playAgent.getGameNum() == playAgent.getMaxGameNum()) {
            logResults = false;
        }

        if (logResults && !fileCreated) {
            checkAndCreateFolder(logDir);
            logSB = new StringBuilder();
            logSB.append("training_matches");
            logSB.append(",");
            logSB.append("result");
            logSB.append("\n");
            try {
                logFile = new PrintWriter(new File(logDir + "/" + getCurrentTimeStamp() + ".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fileCreated = true;
        }

        double result;
        int numEpisodes=3;
        switch (m_mode) {
            case 0:
                result = competeAgainstMCTS(playAgent, gameBoard, numEpisodes);
                break;
            case 1:
                result = competeAgainstRandom(playAgent, gameBoard);
                break;
            case 2:
                result = competeAgainstMinimax(playAgent, gameBoard);
                break;
            case 10:
                result = competeAgainstMCTS_diffStates(playAgent, gameBoard, numEpisodes);
                break;
            default:
                return false;
        }


        if (logResults) {
            logSB.append(playAgent.getGameNum());
            logSB.append(",");
            logSB.append(result);
            logSB.append("\n");
            logFile.write(logSB.toString());
            logSB.delete(0, logSB.length());
            logFile.flush();

            //If the last game has been played, close the file handle.
            //Does not work if the maximum number of training games is not divisible by the number of games per eval.
            if (playAgent.getMaxGameNum() == playAgent.getGameNum()) {
                logFile.close();
            }
        }

        return result >= trainingThreshold;
    }

    /**
     * Evaluates an agent's performance with perfect play, as long as tree and rollout depth are not limited.
     * Scales poorly with board size, requires more than 8 GB RAM for board sizes higher than 4x4.
     * Since the minimax agent is a static object, the game tree is cached between evaluations. This also means that
     * a restart of the program is required to change minimax params and to free memory used by the game tree.
     * Plays a high number of games for accurate measurements, since performance is high once tree is built.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard Game board the evaluation game is played on
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstMinimax(PlayAgent playAgent, GameBoard gameBoard) {
        double[] res = XArenaFuncs.compete(playAgent, minimaxAgent, new StateObserverHex(), 100, verbose);
        double success = res[0];
        m_msg = this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Evaluates an agent's performance using enough iterations to play (near-) perfectly on boards
     * up to and including 5x5. No guarantees for 6x6 board or higher. Tends to require a lot of
     * memory for 7x7 and up. Only one game per evaluation because of the high runtime of the MCTS agent.
     *
     * @param playAgent agent to be evaluated
     * @param gameBoard Game board the evaluation game is played on
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between 0 or 1, depending on the rate of evaluation games won by the agent
     */
    private double competeAgainstMCTS(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
        ParMCTS params = new ParMCTS();
        params.setNumIter((int) Math.pow(10, HexConfig.BOARD_SIZE - 1));
        mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), params);

        double[] res = XArenaFuncs.compete(playAgent, mctsAgent, new StateObserverHex(), numEpisodes, 0);
        double success = res[0];        	
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Similar to {@link EvaluatorHex#competeAgainstMCTS(PlayAgent, GameBoard, int)}: 
     * It does not only play evaluation games from the default start state (empty board) but 
     * also games where the first player (Black) has made a losing moves and the agent as second
     * player (White) will win, if it plays perfect. 
     *
     * @param playAgent agent to be evaluated
     * @param gameBoard Game board the evaluation game is played on
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between 0 or 1, depending on the rate of evaluation games won by the agent
     * 
     * @see EvaluatorHex#competeAgainstMCTS(PlayAgent, GameBoard, int)
     */
    private double competeAgainstMCTS_diffStates(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
        ParMCTS params = new ParMCTS();
        params.setNumIter((int) Math.pow(10, HexConfig.BOARD_SIZE - 1));
        mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), params);

        double success = 0;
        // the int's in startAction code the start board. -1: empty board (Black can win), 
        // 0/1/...: tile 00/01/... was Black's 1st move (losing move, White can win)
        int [] startAction = {-1,0}; 
        // indResWin: 0: res[0] contains the agent's success rate, he plays Black 
        // 			  2: res[2] contains the agent's success rate, he plays White 
        int [] indResWin = {0,2,2,2};
        for (int i=0; i<startAction.length; i++) {
        	StateObserverHex so = new StateObserverHex();
        	if (startAction[i] > -1) so.advance(new ACTIONS(startAction[i]));
            double[] res = XArenaFuncs.compete(playAgent, mctsAgent, so, numEpisodes, 0);
            success += res[indResWin[i]];        	
        }
        success /= startAction.length;
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Very weak but fast evaluator to see if there is a training progress at all.
     * Getting a high win rate against this evaluator does not guarantee good performance of the evaluated agent.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard Game board the evaluation game is played on
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstRandom(PlayAgent playAgent, GameBoard gameBoard) {
        //double success = XArenaFuncs.competeBoth(playAgent, randomAgent, 10, gameBoard);
        double[] res = XArenaFuncs.compete(playAgent, randomAgent, new StateObserverHex(), 100, verbose);
        double success = res[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
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
        for (int availableMode : availableModes) {
            if (mode == availableMode) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2, 10};
    }

    @Override
    public int getQuickEvalMode() {
        return 2;
    }

    @Override
    public int getTrainEvalMode() {
        return -1;
    }

    @Override
    public int getMultiTrainEvalMode() {
        return 0; //getAvailableModes()[0];
//        return getQuickEvalMode();
    }

    @Override
    public String getPrintString() {
        switch (m_mode) {
            case 0:
                return "success against MCTS (best is 1.0): ";
            case 10:
                return "success against MCTS (diff. start states, best is 1.0): ";
            case 1:
                return "success against Random (best is 1.0): ";
            case 2:
                return "success against Minimax (best is 1.0): ";
            default:
                return null;
        }
    }

    @Override
    public String getPlotTitle() {
        switch (m_mode) {
            case 0:
                return "success against MCTS";
            case 1:
                return "success against Random";
            case 2:
                return "success against Minimax";
            default:
                return null;
        }
    }

    @Override
    public String getMsg() {
        return m_msg;
    }

    /**
     * checks if a folder exists and creates a new one if it doesn't
     *
     * @param filePath the folder Path
     * @return true if a folder already existed
     */
    private boolean checkAndCreateFolder(String filePath) {
        File file = new File(filePath);
        boolean exists = file.exists();
        if (!file.exists()) {
            file.mkdirs();
        }
        return exists;
    }

    /**
     * generates String containing the current timestamp
     *
     * @return the timestamp
     */
    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
