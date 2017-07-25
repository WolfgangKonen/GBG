package games.Hex;

import controllers.MCTS.MCTSAgentT;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import params.MCTSParams;
import params.ParMCTS;
import tools.Types;

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
    private MCTSAgentT mctsAgent = null;
    private RandomAgent randomAgent = new RandomAgent(Types.GUI_AGENT_LIST[2]);
    private double trainingThreshold = 0.8;
    private GameBoard gameBoard;
    private PlayAgent playAgent;
    private double lastResult = 0;
    private int m_mode = 0;
    private String m_msg = null;
    protected int verbose=0;

    private boolean logResults = false;
    private boolean fileCreated = false;
    private final String logDir = "logs/Hex/train";
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        m_mode=mode;
        if (verbose == 1) {
            System.out.println("Using evaluation mode " + mode);
        }
        initEvaluator(e_PlayAgent, gb);
        if (m_mode == 2 && minimaxAgent.getDepth() < HexConfig.TILE_COUNT){
            System.out.println("Using minimax with limited tree depth: "+
                    minimaxAgent.getDepth()+" used, "+HexConfig.TILE_COUNT+" needed");
        }
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        this.playAgent = playAgent;
    }

    // WK: here you might select between different opponents based on m_mode
    // (should then also modify getAvailableModes, getPrintTitle and getPlotTitle appropriately)
    @Override
    protected boolean eval_Agent() {
        if (m_mode == -1){
            return true;
        }

        if (!fileCreated && playAgent.getGameNum() == playAgent.getMaxGameNum()){
            logResults = false;
        }

        if (logResults && !fileCreated){
            checkAndCreateFolder(logDir);
            logSB = new StringBuilder();
            logSB.append("training_matches");
            logSB.append(",");
            logSB.append("result");
            logSB.append("\n");
            try {
                logFile = new PrintWriter(new File(logDir+"/"+getCurrentTimeStamp()+".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fileCreated = true;
        }

        double result;
        switch (m_mode) {
            case 0:
                result = competeAgainstMCTS(playAgent, gameBoard);
                break;
            case 1:
                result = competeAgainstRandom(playAgent, gameBoard);
                break;
            case 2:
                result = competeAgainstMinimax(playAgent, gameBoard);
                break;
            default: return false;
        }


        if (logResults){
            logSB.append(playAgent.getGameNum());
            logSB.append(",");
            logSB.append(result);
            logSB.append("\n");
            logFile.write(logSB.toString());
            logSB.delete(0, logSB.length());
            logFile.flush();
            if (playAgent.getMaxGameNum() == playAgent.getGameNum()){
                logFile.close();
            }
        }

        return result >= trainingThreshold;
    }

    private double competeAgainstMinimax(PlayAgent playAgent, GameBoard gameBoard){
        double[] res = XArenaFuncs.compete(playAgent, minimaxAgent, new StateObserverHex(), 100, verbose);
        double success = res[0];
        m_msg = this.getPrintString() + success;
        if (this.verbose>0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    private double competeAgainstMCTS(PlayAgent playAgent, GameBoard gameBoard){
        ParMCTS params = new ParMCTS();
        params.setNumIter((int) Math.pow(10, HexConfig.BOARD_SIZE-1));
        mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), params);

        double[] res = XArenaFuncs.compete(playAgent, mctsAgent, new StateObserverHex(), 1, 0);
        //double[] res = {0};
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
        m_msg = playAgent.getName()+": "+this.getPrintString() + success;
        if (this.verbose>0) System.out.println(m_msg);
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
        return new int[]{-1, 0, 1, 2};
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

    /**
     * checks if a folder exists and creates a new one if it doesn't
     *
     * @param filePath the folder Path
     * @return true if a folder already existed
     */
    private boolean checkAndCreateFolder(String filePath) {
        File file = new File(filePath);
        boolean exists = file.exists();
        if(!file.exists()) {
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
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//-SSS
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
