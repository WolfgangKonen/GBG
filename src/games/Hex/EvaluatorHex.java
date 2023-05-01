package games.Hex;

import controllers.MCTS.MCTSAgentT;
import controllers.TD.ntuple2.NTupleBase;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.EvalResult;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import params.ParMCTS;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types.ACTIONS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
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
    private final RandomAgent randomAgent = new RandomAgent("Random");
    private final double trainingThreshold = 0.8;
    private PlayAgent playAgent;
    private int numStartStates = 1;
    private final DecimalFormat frm = new DecimalFormat("+0.0000;-0.0000");
    
	/**
	 * A list of all Hex states which are 0 or 1 ply away from the default start state.
	 * For B={@link HexConfig#BOARD_SIZE}=(4,5,6) these are (10,15,21)+1 states (B*(B+1)/2+1 in general). <br>
	 * <p>
	 * This list is a static member, so that it needs to be constructed only once. <br>
	 * This list is used in evaluation mode 10 and 20 
	 */
	protected static ArrayList<StateObserverHex> diffStartList = null;
	
    /**
     * logResults toggles logging of training progress to a csv file located in {@link #logDir}
     */
    private boolean logResults = false;
    private boolean fileCreated = false;
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
        super(e_PlayAgent, gb, mode, verbose);
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

    /**
     * Add all 1-ply start states plus the default start state to diffStartList
     * @param diffStartList     the incoming list
     * @return diffStartList, filled with states
     */
	private ArrayList<StateObserverHex> 
	addAll1PlyStates(ArrayList<StateObserverHex> diffStartList) {
    	StateObserverHex s = (StateObserverHex) m_gb.getDefaultStartState();
		for (int i=0; i<HexConfig.BOARD_SIZE; i++) {
			for (int j=0; j<=i; j++) {
				ACTIONS a = new ACTIONS(i*HexConfig.BOARD_SIZE+j);
	        	StateObserverHex s_copy = s.copy();
	        	s_copy.advance(a);
	        	diffStartList.add(s_copy);
			}
		}	
		diffStartList.add(s);		// add the default start state as well
		return diffStartList;
	}
	
    @Override
    protected EvalResult evalAgent(PlayAgent pa) {
    	this.playAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
            return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
        }

        // when evalAgent is called for the first time, construct diffStartList once for all 
        // EvaluatorHex objects (will not change during runtime)
        if (diffStartList==null) {
        	diffStartList = new  ArrayList<>();
        	diffStartList = addAll1PlyStates(diffStartList);        	
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
                result = competeAgainstMCTS(playAgent, numEpisodes);
                break;
            case 1:
                result = competeAgainstRandom(playAgent);
                break;
            case 2:
                result = competeAgainstMaxN(playAgent, numEpisodes);
                break;
            case 20:
            case 10:
            	if (/*playAgent instanceof TDNTuple2Agt ||*/ playAgent instanceof NTupleBase) {
            		// we can only call the parallel version, if playAgent's getNextAction2 is 
            		// thread-safe, which is the case for TDNTuple2Agt, TDNTuple3Agt or SarsaAgt
            		// (the latter two are children of NTupleBase).
            		// Also, we have to construct MCTS opponent inside the callables, otherwise
            		// we are not thread-safe as well:
                    if (m_mode==20)    result = competeAgainstMCTS_diffStates_PAR(playAgent, numEpisodes);
                    else /*m_mode==10*/ result = competeAgainstMCTS_diffWinStates_PAR(playAgent, numEpisodes);
            	} else {
                    ParMCTS params = new ParMCTS();
                    int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
                    params.setNumIter((int) Math.pow(10, numIterExp));
                    mctsAgent = new MCTSAgentT("MCTS", new StateObserverHex(), params);

                    if (m_mode==20)    result = competeAgainstOpponent_diffStates(playAgent, mctsAgent, m_gb, numEpisodes);
                    else /*m_mode==10*/ result = competeAgainstOpponent_diffWinStates(playAgent, mctsAgent, m_gb, numEpisodes);
            	}
                break;
            case 11:
    			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
        		result = competeAgainstOpponent_diffStates(playAgent, this.getTDReferee(), m_gb, numEpisodes);
                break;
            default:
                throw new RuntimeException("Invalid m_mode = "+m_mode);
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

        return new EvalResult(result, lastResult>trainingThreshold, m_msg, m_mode, trainingThreshold);
    }

    /**
     * Very weak but fast evaluator to see if there is a training progress at all.
     * Getting a high win rate against this evaluator does not guarantee good performance of the evaluated agent.
     *
     * @param playAgent Agent to be evaluated
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstRandom(PlayAgent playAgent) {
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
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstMaxN(PlayAgent playAgent, int numEpisodes) {
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, maxNAgent), new StateObserverHex(), numEpisodes, verbose, null);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult + "  (#="+numEpisodes+")";
        if (this.verbose > 0) System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates an agent's performance using enough iterations to play (near-) perfectly on boards
     * up to and including 5x5. No guarantees for 6x6 board or higher. Tends to require a lot of
     * memory for 7x7 and up. 
     *
     * @param playAgent agent to be evaluated
     * @param numEpisodes number of episodes played during evaluation
     * @return a value in range [-1,1], depending on the rate of evaluation games won by the agent
     */
    private double competeAgainstMCTS(PlayAgent playAgent, int numEpisodes) {
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverHex(), params);

		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent), new StateObserverHex(), numEpisodes, 0, null);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
        //if (this.verbose > 0) 
        	System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Similar to {@link EvaluatorHex#competeAgainstMCTS(PlayAgent, int)}, but:
     * <ul> 
     * <li>It uses <b>every</b> 0-ply and 1-ply start state, plays an episode in both roles and 
     * averages the results. 
     * <li>It allows a different opponent than MCTS to be passed in as 2nd argument
     * </ul>
     *
     * @param playAgent agent to be evaluated (it plays both 1st and 2nd)
     * @param opponent 	agent against which {@code playAgent} plays
     * @param gb 		Game board the evaluation game is played on
     * @param numEpisodes number of episodes played during evaluation
     * @return a value in range [-1,1], depending on the rate of evaluation games won by the agent
     * 
     * @see EvaluatorHex#competeAgainstMCTS(PlayAgent, int)
     * @see HexConfig#EVAL_START_ACTIONS
     */
    private double competeAgainstOpponent_diffStates(PlayAgent playAgent, PlayAgent opponent, GameBoard gb, int numEpisodes) {
        //double[] res;
        double success;
        double averageSuccess = 0; 
        
		if (opponent == null) {
			gb.getArena().showMessage("ERROR: no opponent","Load Error", JOptionPane.ERROR_MESSAGE);
			return Double.NaN;
		} 

        numStartStates = diffStartList.size();
        
        // evaluate each start state in turn and return average success rate: 
        int i=0;
        for (StateObserverHex so : diffStartList) {
            long gameStartTime = System.currentTimeMillis();
    		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, numEpisodes, 0);
    		success = sc.scTup[0];
    		averageSuccess += success;
        	long duration = System.currentTimeMillis() - gameStartTime;
            System.out.println("Finished evaluation " + i + " after " + duration + "ms, success="+success);
            i++;
        }
        averageSuccess /= numStartStates;
        double winrate = (averageSuccess+1)/2;

        m_msg = playAgent.getName() + ": " + this.getPrintString() + frm.format(averageSuccess)
        			+ " (winrate="+ frm.format(winrate) +")";
//      if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return averageSuccess;
    }

    /**
     * Similar to {@link EvaluatorHex#competeAgainstMCTS(PlayAgent, int)}, but:
     * <ul> 
     * <li>It uses <b>every</b> 0-ply and 1-ply start state, plays an episode in both roles and 
     * averages the results. 
     * <li>It allows a different opponent than MCTS to be passed in as 2nd argument
     * </ul>
     *
     * @param playAgent agent to be evaluated (it plays both 1st and 2nd)
     * @param opponent 	agent against which {@code playAgent} plays
     * @param gb 		Game board the evaluation game is played on
     * @param numEpisodes number of episodes played during evaluation
     * @return a value in range [-1,1], depending on the rate of evaluation games won by the agent
     * 
     * @see EvaluatorHex#competeAgainstMCTS(PlayAgent, int)
     * @see HexConfig#EVAL_START_ACTIONS
     */
    private double competeAgainstOpponent_diffWinStates(PlayAgent playAgent, PlayAgent opponent, GameBoard gb, int numEpisodes) {
        //double[] res;
        double success;
        double averageSuccess = 0; 
        
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
            long gameStartTime = System.currentTimeMillis();
        	if (startAction[i] == -1) {
        		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, opponent), so, numEpisodes, 0, null);
        		success = sc.scTup[0];
        	} else {
        		so.advance(new ACTIONS(startAction[i]));
        		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(opponent, playAgent), so, numEpisodes, 0, null);
        		success = sc.scTup[1];
        	}
    		averageSuccess += success;
        	long duration = System.currentTimeMillis() - gameStartTime;
            System.out.println("Finished evaluation " + i + " after " + duration + "ms, success="+success);
        }
        averageSuccess /= numStartStates;
        double winrate = (averageSuccess+1)/2;

        m_msg = playAgent.getName() + ": " + this.getPrintString() + frm.format(averageSuccess)
        			+ " (winrate="+ frm.format(winrate) +")";
//      if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return averageSuccess;
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
     * @param playAgent     the play agent
     * @param numEpisodes   number of episodes to compete
     * @return a value in range [-1,1], depending on the rate of evaluation games won by the agent
     */
    private double competeAgainstMCTS_diffStates_PAR(PlayAgent playAgent, int numEpisodes) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));

        numStartStates = diffStartList.size();
        
        List<Double> successObservers = new ArrayList<>();
        List<Callable<Double>> callables = new ArrayList<>();

        // evaluate each start state in turn and return average success rate: 
        int i=0;
        for (StateObserverHex so : diffStartList) {
            int gameNumber = i;
            callables.add(() -> {
                //double[] res;
                double success;
                long gameStartTime = System.currentTimeMillis();
                StateObserverHex so2 = so.copy();
                
                // important: mctsAgent2 has to be constructed inside the 'callables' function, 
                // otherwise all parallel calls would operate on the same agent and would produce
                // garbage.
                MCTSAgentT mctsAgent2 = new MCTSAgentT("MCTS", new StateObserverHex(), params);

        		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, mctsAgent2), so2, numEpisodes, 0);
        		success = sc.scTup[0];
        		
                if(verbose == 0) {
                	long duration = System.currentTimeMillis() - gameStartTime;
                    System.out.println("Finished evaluation " + gameNumber + " after " + duration + "ms, success="+success);
                }
            	return success;
            });
            i++;
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
            averageSuccess += suc;
        }
        averageSuccess/= numStartStates;
        double winrate = (averageSuccess+1)/2;

        m_msg = playAgent.getName() + ": " + this.getPrintString() + frm.format(averageSuccess)
        			+ " (winrate="+ frm.format(winrate) +")";
//      if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return averageSuccess;
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
     * @param playAgent     the play agent
     * @param numEpisodes   number of episodes to compete
     * @return a value in range [-1,1], depending on the rate of evaluation games won by the agent
     */
    private double competeAgainstMCTS_diffWinStates_PAR(PlayAgent playAgent, int numEpisodes) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(HexConfig.BOARD_SIZE,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));

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
        	final int gameNumber=i;
            final int i2 = i;
            callables.add(() -> {
                //double[] res;
                double success;
                long gameStartTime = System.currentTimeMillis();
                StateObserverHex so = new StateObserverHex();
                
                // important: mctsAgent2 has to be constructed inside the 'callables' function, 
                // otherwise all parallel calls would operate on the same agent and would produce
                // garbage.
                MCTSAgentT mctsAgent2 = new MCTSAgentT("MCTS", new StateObserverHex(), params);

            	if (startAction2[i2] == -1) {
            		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent2), so, numEpisodes, 0, null);
            		success = sc.scTup[0];
            	} else {
            		so.advance(new ACTIONS(startAction2[i2]));
            		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(mctsAgent2, playAgent), so, numEpisodes, 0, null);
            		success = sc.scTup[1];
            	}
                if(verbose == 0) {
                	long duration = System.currentTimeMillis() - gameStartTime;
                    System.out.println("Finished evaluation " + gameNumber + " after " + duration + "ms, success="+success);
                }
            	return success;
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
            averageSuccess += suc;
        }
        averageSuccess/= startAction.length;
        double winrate = (averageSuccess+1)/2;

        m_msg = playAgent.getName() + ": " + this.getPrintString() + frm.format(averageSuccess)
        			+ " (winrate="+ frm.format(winrate) +")";
//      if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return averageSuccess;
    }
    
    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2, 10, 11, 20};
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return -1;
    }

    @Override
    public String getPrintString() {
        return switch (m_mode) {
            case -1 -> "no evaluation done ";
            case 0 -> "success against MCTS (best is 1.0): ";
            case 1 -> "success against Random (best is 1.0): ";
            case 2 -> "success against Max-N (best is 1.0): ";
            case 10 -> "success against MCTS (" + numStartStates + " diff. win start states, range [-1,1]): ";
            case 20 -> "success against MCTS (" + numStartStates + " diff. start states, range [-1,1]): ";
            case 11 -> "success against TDReferee (" + numStartStates + " diff. start states, range [-1,1]): ";
            default -> null;
        };
    }

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: against MCTS, best is 1.0<br>"
				+ "1: against Random, best is 1.0<br>"
				+ "2: against Max-N, best is 1.0<br>"
				+ "10: against MCTS, different win starts, best is 1.0<br>"
				+ "20: against MCTS, different starts, range [-1,1]<br>"
				+ "11: against TDReferee.agt.zip, different starts"
				+ "</html>";
	}

    @Override
    public String getPlotTitle() {
        return switch (m_mode) {
            case 1 -> "success against Random";
            case 2 -> "success against Max-N";
            case 0,10,20 -> "success against MCTS";
            case 11 -> "success against TDReferee";
            default -> null;
        };
    }

    /**
     * generates String containing the current timestamp
     *
     * @return the timestamp
     */
    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        return sdfDate.format(now);
    }
}
