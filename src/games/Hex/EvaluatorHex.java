package games.Hex;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.TD.ntuple2.NTupleBase;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import gui.MessageBox;
import params.ParMCTS;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

/**
 * Evaluate Hex agents. See {@link #getAvailableModes()} and {@link #evalAgent(PlayAgent)} 
 * for available evaluators. 
 */
public class EvaluatorHex extends Evaluator {
	private MaxNAgent maxNAgent=null; 
    private final String logDir = "logs/Hex/train";
    protected int verbose = 0;
    private MCTSAgentT mctsAgent = null;
    private RandomAgent randomAgent = new RandomAgent("Random");
    private double trainingThreshold = 0.8;
    private PlayAgent playAgent;
    private int numStartStates = 1;
    
//  private int m_mode = 0;					// now in Evaluator
//  private GameBoard m_gb;					// now in Evaluator
//	private AgentLoader agtLoader = null;	// now in Evaluator
    /**
     * logResults toggles logging of training progress to a csv file located in {@link #logDir}
     */
    private boolean logResults = false;
    private boolean fileCreated = false;
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
        if (verbose == 1) {
            System.out.println("Using evaluation mode " + mode);
        }
        initEvaluator(e_PlayAgent, gb);
        if (mode == 2 && maxNAgent.getDepth() < HexConfig.TILE_COUNT) {
            System.out.println("Using Max-N with limited tree depth: " +
                    maxNAgent.getDepth() + " used, " + HexConfig.TILE_COUNT + " needed");
        }
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard) {
        this.m_gb = gameBoard;
        this.playAgent = playAgent;
        
    	ParMaxN parM = new ParMaxN();
    	parM.setMaxNDepth(10);
    	parM.setMaxNUseHashmap(true);
    	maxNAgent = new MaxNAgent("Max-N", parM, new ParOther());
    }

    @Override
    protected boolean evalAgent(PlayAgent pa) {
    	this.playAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
            return false;
        }

        //Disable logging for the final evaluation after training
        if (!fileCreated && playAgent.getGameNum() == playAgent.getMaxGameNum()) {
            logResults = false;
        }

        if (logResults && !fileCreated) {
    		tools.Utils.checkAndCreateFolder(logDir);
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
        int numEpisodes=HexConfig.EVAL_NUMEPISODES;
        switch (m_mode) {
            case 0:
                result = competeAgainstMCTS(playAgent, m_gb, numEpisodes);
                break;
            case 1:
                result = competeAgainstRandom(playAgent, m_gb);
                break;
            case 2:
                result = competeAgainstMaxN(playAgent, m_gb, numEpisodes);
                break;
            case 10:
            	if (playAgent instanceof TDNTuple2Agt || playAgent instanceof NTupleBase) {
            		// we can only call the parallel version, if playAgent's getNextAction2 is 
            		// thread-safe, which is the case for TDNTuple2Agt, TDNTuple3Agt or SarsaAgt
            		// (the latter two are children of NTupleBase).
            		// Also we have to construct MCTS opponent inside the callables, otherwise
            		// we are not thread-safe as well:
                    result = competeAgainstMCTS_diffStates_PAR(playAgent, m_gb, numEpisodes);
            	} else {
                    ParMCTS params = new ParMCTS();
                    int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
                    params.setNumIter((int) Math.pow(10, numIterExp));
                    mctsAgent = new MCTSAgentT("MCTS", new StateObserverHex(), params);

            		result = competeAgainstOpponent_diffStates(playAgent, mctsAgent, m_gb, numEpisodes);
            	}
                break;
            case 11:
    			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
        		result = competeAgainstOpponent_diffStates(playAgent, this.getTDReferee(), m_gb, numEpisodes);
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
     * Very weak but fast evaluator to see if there is a training progress at all.
     * Getting a high win rate against this evaluator does not guarantee good performance of the evaluated agent.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard Game board the evaluation game is played on
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstRandom(PlayAgent playAgent, GameBoard gameBoard) {
        //double success = XArenaFuncs.competeBoth(playAgent, randomAgent, 10, gameBoard);
//      double[] res = XArenaFuncs.compete(playAgent, randomAgent, new StateObserverHex(), 100, verbose, null);
//      lastResult = res[0]-res[2];
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, randomAgent), new StateObserverHex(), 100, verbose, null);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
        if (this.verbose > 0) System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates an agent's performance with perfect play, as long as tree and rollout depth are not limited.
     * Scales poorly with board size, requires more than 8 GB RAM for board sizes higher than 4x4.
     * And the execution time is unbearable for board sizes of 5x5 and higher.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard Game board the evaluation game is played on
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstMaxN(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
//      double[] res = XArenaFuncs.compete(playAgent, maxNAgent, new StateObserverHex(), numEpisodes, verbose, null);
//      lastResult = res[0]-res[2];
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, maxNAgent), new StateObserverHex(), numEpisodes, verbose, null);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult + "  (#="+numEpisodes+")";
        if (this.verbose > 0) System.out.println(m_msg);
        return lastResult;
    }
// --- This text is no longer valid: ---
//  * Since the Max-N agent is a static object, the game tree is cached between evaluations. This also means that
//  * a restart of the program is required to change Max-N params and to free memory used by the game tree.
//  * Plays a high number of games for accurate measurements, since performance is high once tree is built.


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
        int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverHex(), params);

//      double[] res = XArenaFuncs.compete(playAgent, mctsAgent, new StateObserverHex(), numEpisodes, 0, null);
//      lastResult = res[0]-res[2];        	
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent), new StateObserverHex(), numEpisodes, 0, null);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
        //if (this.verbose > 0) 
        	System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Similar to {@link EvaluatorHex#competeAgainstMCTS(PlayAgent, GameBoard, int)}, but:
     * <ul> 
     * <li>It does not only play evaluation games from the default start state (empty board) but 
     * also games where the first player (Black) has made a losing move and the agent as second
     * player (White) will win, if it plays perfect (see {@link HexConfig#EVAL_START_ACTIONS}). 
     * <li>It allows a different opponent than MCTS to be passed in as 2nd argument
     * </ul>
     *
     * @param playAgent agent to be evaluated (it plays both 1st and 2nd)
     * @param opponent 	agent against which {@code playAgent} plays
     * @param gb 		Game board the evaluation game is played on
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between 0 or 1, depending on the rate of evaluation games won by the agent
     * 
     * @see EvaluatorHex#competeAgainstMCTS(PlayAgent, GameBoard, int)
     * @see HexConfig#EVAL_START_ACTIONS
     */
    private double competeAgainstOpponent_diffStates(PlayAgent playAgent, PlayAgent opponent, GameBoard gb, int numEpisodes) {
        double[] res;
        double success = 0;
        
		if (opponent == null) {
			gb.getArena().showMessage("ERROR: no opponent","Load Error", JOptionPane.ERROR_MESSAGE);
			return Double.NaN;
		} 

        // find the start states to evaluate:
        int [] startAction = {-1};
        int N = HexConfig.BOARD_SIZE;
        if (N>HexConfig.EVAL_START_ACTIONS.length-1) {
            System.out.println("*** WARNING ***: 1-ply winning boards for board size N="+N+
            		"are not coded in " +"HexConfig.EVAL_START_ACTIONS." );
            System.out.println("*** WARNING ***: Evaluator(mode 10) will use only " +
            		"empty board for evaluation.");
        } else {
            // the int's in startAction code the start board. -1: empty board (a winning 
            // board for 1st player Black), 
            // 0/1/...: Black's 1st move was tile 00/01/... (it is a losing move, a winning
            // board for 2nd player White)
            startAction = HexConfig.EVAL_START_ACTIONS[N];
        }
        numStartStates = startAction.length;
        
        // evaluate each start state in turn and return average success rate: 
        for (int i=0; i<startAction.length; i++) {
        	StateObserverHex so = new StateObserverHex();
        	if (startAction[i] == -1) {
//        		res = XArenaFuncs.compete(playAgent, opponent, so, numEpisodes, 0, null);
//              success += res[0]-res[2];        	
        		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, opponent), so, numEpisodes, 0, null);
        		success = sc.scTup[0];
        	} else {
        		so.advance(new ACTIONS(startAction[i]));
//        		res = XArenaFuncs.compete(opponent, playAgent, so, numEpisodes, 0, null);
//              success += res[2]-res[0];        	
        		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(opponent, playAgent), so, numEpisodes, 0, null);
        		success = sc.scTup[1];
        	}
        }
        success /= startAction.length;
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
//        if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Does the same as {@code competeAgainstMCTS_diffStates}, but with 6 parallel cores, 
     * so it is 6 times faster. 
     * <p>
     * NOTES: <ul>
     * <li> The memory consumption grows when this function is repeatedly called (by about 60 kB
     * for each call, but there seems to be an effective limit for the Java process at 4.3 GB -- 
     * beyond the garbage collector seems to do its work effectively)
     * <li> The call to compete may not alter anything in {@code playAgent}. So the function 
     * getNextAction2 invoked by compete should be thread-safe. This is valid, if getNextAction2 
     * does not modify members in playAgent. Possible for TD-n-Tuple-agents, if we do not write 
     * on class-global members (e.g. do not use BestScore, but use local variable BestScore2). 
     * Parallel threads are not possible when playAgent is MCTSAgentT or MaxNAgent.
     * </ul>
     * 
     * @param playAgent
     * @param gameBoard
     * @param numEpisodes
     * @return
     */
    private double competeAgainstMCTS_diffStates_PAR(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));
        //mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[3], new StateObserverHex(), params);

        // find the start states to evaluate:
        int [] startAction = {-1};
        int N = HexConfig.BOARD_SIZE;
        if (N>HexConfig.EVAL_START_ACTIONS.length-1) {
            System.out.println("*** WARNING ***: 1-ply winning boards for board size N="+N+
            		"are not coded in " +"HexConfig.EVAL_START_ACTIONS." );
            System.out.println("*** WARNING ***: Evaluator(mode 10) will use only " +
            		"the empty board for evaluation.");
        } else {
            // the int's in startAction code the start board. -1: empty board (a winning 
            // board for 1st player Black), 
            // 0/1/...: Black's 1st move was tile 00/01/... (it is a losing move, thus a winning
            // board for 2nd player White)
            startAction = HexConfig.EVAL_START_ACTIONS[N];
        }
        numStartStates = startAction.length;
        
        List<Double> successObservers = new ArrayList<>();
        List<Callable<Double>> callables = new ArrayList<>();
        final int[] startAction2 = startAction;

        // evaluate each start state in turn and return average success rate: 
        for (int i=0; i<startAction.length; i++) {
            int gameNumber = i+1;
            final int i2 = i;
            callables.add(() -> {
                double[] res;
                double success = 0;
                long gameStartTime = System.currentTimeMillis();
                StateObserverHex so = new StateObserverHex();
                
                // important: mctsAgent2 has to be constructed inside the 'callables' function, 
                // otherwise all parallel calls would operate on the same agent and would produce
                // garbage.
                MCTSAgentT mctsAgent2 = new MCTSAgentT("MCTS", new StateObserverHex(), params);

            	if (startAction2[i2] == -1) {
//            		res = XArenaFuncs.compete(playAgent, mctsAgent2, so, numEpisodes, 0, null);
//                  success = res[0]-res[2];        	
            		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent2), so, numEpisodes, 0, null);
            		success = sc.scTup[0];
            	} else {
            		so.advance(new ACTIONS(startAction2[i2]));
//            		res = XArenaFuncs.compete(mctsAgent2, playAgent, so, numEpisodes, 0, null);
//                  success = res[2]-res[0];        	
            		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(mctsAgent2, playAgent), so, numEpisodes, 0, null);
            		success = sc.scTup[1];
            	}
                if(verbose == 0) {
                    System.out.println("Finished evaluation " + gameNumber + " after " + (System.currentTimeMillis() - gameStartTime) + "ms. ");
                }
            	return Double.valueOf(success);
            });
        }
   
        // invoke all callables and store results on List successObservers
        try {
            executorService.invokeAll(callables).stream().map(future -> {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).forEach(successObservers::add);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // reduce results (here: calculate average success)
        double averageSuccess = 0; 
        for(Double suc : successObservers) {
            averageSuccess += suc.doubleValue();
        }
        averageSuccess/= startAction.length;

        m_msg = playAgent.getName() + ": " + this.getPrintString() + averageSuccess;
//        if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return averageSuccess;
    }

 	// --- implemented by Evaluator ---
//    @Override
//    public double getLastResult() {
//        return lastResult;
//    }

 	// --- implemented by Evaluator ---
//    @Override
//    public boolean isAvailableMode(int mode) {
//        int[] availableModes = getAvailableModes();
//        for (int availableMode : availableModes) {
//            if (mode == availableMode) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2, 10, 11};
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return -1;
    }

//    @Override
//    public int getMultiTrainEvalMode() {
//        return 0; //getAvailableModes()[0];
//    }

    @Override
    public String getPrintString() {
        switch (m_mode) {
			case -1: return "no evaluation done ";
            case 0:  return "success against MCTS (best is 1.0): ";
            case 1:  return "success against Random (best is 1.0): ";
            case 2:  return "success against Max-N (best is 1.0): ";
            case 10: return "success against MCTS (" + numStartStates + " diff. start states, best is 1.0): ";
            case 11: return "success against TDReferee (" + numStartStates + " diff. start states, best is 1.0): ";
            default: return null;
        }
    }

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: against MCTS, best is 1.0<br>"
				+ "1: against Random, best is 1.0<br>"
				+ "2: against Max-N, best is 1.0<br>"
				+ "10: against MCTS, different starts, best is 1.0<br>"
				+ "11: against TDReferee.agt.zip, different starts"
				+ "</html>";
	}

    @Override
    public String getPlotTitle() {
        switch (m_mode) {
            case 0:  return "success against MCTS";
            case 1:  return "success against Random";
            case 2:  return "success against Max-N";
            case 10: return "success against MCTS";
            case 11: return "success against TDReferee";
            default: return null;
        }
    }

 	// --- implemented by Evaluator ---
//    @Override
//    public String getMsg() {
//        return m_msg;
//    }

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
