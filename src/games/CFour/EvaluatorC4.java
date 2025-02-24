package games.CFour;

import controllers.MCTS.MCTSAgentT;
import controllers.TD.ntuple2.NTupleBase;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.*;
import games.CFour.openingBook.BookSum;
import params.ParMCTS;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types.ACTIONS;

import java.io.File;
import java.io.FileNotFoundException;
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
 * Evaluator for the game C4 (ConnectFour). Depending on the value of parameter {@code mode} in constructor:
 * <ul>
 * <li> -1: no evaluation
 * <li>  0: compete against MCTS
 * <li>  1: compete against Random
 * <li>  2: compete against Max-N (limited tree depth: 6)
 * <li>  3: compete against AlphaBetaAgent (perfect play)
 * <li>  4: compete against AlphaBetaAgent, different start states (perfect play)
 * <li>  5: compete against AlphaBetaAgentDistantLoss (perfect play)
 * <li> 10: compete against MCTS, different start states
 * <li> 11: compete against TDReferee.agt.zip, different start states
 * </ul>  
 * The value of mode is set in the constructor. 
 */
public class EvaluatorC4 extends Evaluator {
    private MaxNAgent maxnAgent = null; 
    private final String logDir = "logs/ConnectFour/train";
    protected int verbose = 0;
    private MCTSAgentT mctsAgent = null;
    private final RandomAgent randomAgent = new RandomAgent("Random");
    private PlayAgent playAgent;
    private int numStartStates = 1;
    
//	private AgentLoader agtLoader = null;		// now in Evaluator
//	private GameBoard m_gb;						// now in Evaluator
	
	private AlphaBetaAgent alphaBetaStd = null;		// random move in loss positions
	private AlphaBetaAgent alphaBeta_DL = null;		// distant losses

    /**
     * logResults toggles logging of training progress to a csv file located in {@link #logDir}
     */
    private boolean logResults = false;
    private boolean fileCreated = false;
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorC4(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
        super(e_PlayAgent, gb, mode, verbose);
        if (verbose == 1) {
            System.out.println("Using evaluation mode " + mode);
        }
        initEvaluator(e_PlayAgent, gb);
        if (mode == 2 && maxnAgent.getDepth() < C4Base.CELLCOUNT) {
            System.out.println("Using Max-N with limited tree depth: " +
                    maxnAgent.getDepth() + " used, " + C4Base.CELLCOUNT + " needed (for perfect play)");
        }
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard) {
        this.m_gb = gameBoard;
        this.playAgent = playAgent;
        //maxnAgent is set once in evaluator constructor, so the search tree does not have to be rebuilt 
        //every time a new evaluation is started.
        //However, this prevents MaxN parameters from being adjusted while the program is running. 
    	//Set needed tree depth during compile time.
        ParMaxN params = new ParMaxN();
        int maxNDepth =  6;
        params.setMaxNDepth(maxNDepth);
        maxnAgent = new MaxNAgent("Max-N", params, new ParOther());
        
		// Initialize the Alpha-Beta-Agents
		// (same as winOptionsGTB in MT's C4)
		alphaBetaStd = new AlphaBetaAgent(new BookSum());		// no search for distant losses
		alphaBetaStd.instantiateAfterLoading();				
		alphaBeta_DL = new AlphaBetaAgent(new BookSum(),1000);	// search for distant losses
		alphaBeta_DL.instantiateAfterLoading();				
    }

    @Override
    protected EvalResult evalAgent(PlayAgent pa) {
        double trainingThreshold = 0.8;
        this.playAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
            m_msg = "No evaluation done ";
            lastResult = 0.0;
            return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
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
        int numEpisodes=8;
        int mctsF=2; 	// in case 0 (opponent=MCTS) we increase numEpisodes by factor mctsF, 
        				// since MCTS results otherwise have such a large fluctuation
        switch (m_mode) {
            case 0:
                result = competeAgainstMCTS(playAgent, mctsF*numEpisodes);
                break;
            case 1:
                result = competeAgainstRandom(playAgent);
                break;
            case 2:
                result = competeAgainstMaxN(playAgent, numEpisodes);
                break;
            case 3:
            	numEpisodes=20;
                result = competeAgainstAlphaBeta(playAgent, numEpisodes);
                break;
            case 4:
                result = competeAgainstOpponent_diffStates(playAgent, alphaBetaStd, m_gb, numEpisodes);
                break;
            case 5:
            	numEpisodes=20;
                result = competeAgainstAlphaBetaDistantLoss(playAgent, numEpisodes);
                break;
            case 10:
            	if (/*playAgent instanceof TDNTuple2Agt ||*/ playAgent instanceof NTupleBase) {
            		// we can only call the parallel version, if playAgent's getNextAction2 is 
            		// thread-safe, which is the case for TDNTuple2Agt, TDNTuple3Agt and SarsaAgt. 
            		// Also, we have to construct MCTS opponent inside the callables, otherwise
            		// we are not thread-safe as well:
                    result = competeAgainstMCTS_diffStates_PAR(playAgent, numEpisodes);
            	} else {
                    ParMCTS params = new ParMCTS();
                    int numIterExp =  (Math.min(C4Base.CELLCOUNT,5) - 1);
                    params.setNumIter((int) Math.pow(10, numIterExp));
                    mctsAgent = new MCTSAgentT("MCTS", new StateObserverC4(), params);

            		result = competeAgainstOpponent_diffStates(playAgent, mctsAgent, m_gb, numEpisodes);
            	}
                break;
            case 11:
            	// just debug code to find out which is a winning startAction in competeAgainstOpponent_diffStates:
//            	AlphaBetaAgent alphaBetaAgentP = new AlphaBetaAgent(new BookSum());
//        		result = competeAgainstOpponent_diffStates(alphaBetaAgentP, alphaBetaStd, m_gb, numEpisodes);

    			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
        		result = competeAgainstOpponent_diffStates(playAgent, getTDReferee(), m_gb, numEpisodes);
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
     * Evaluates an agent's performance with perfect play, as long as tree and rollout depth are not limited.
     * Scales poorly with board size, requires lots of GB and time for increased tree depth.
     *
     * @param playAgent Agent to be evaluated
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstMaxN(PlayAgent playAgent, int numEpisodes) {
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, maxnAgent), 0, new StateObserverC4(), numEpisodes, verbose, null, null, null, false);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
        if (this.verbose > 0) System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates an agent's performance against perfect play, since {@link AlphaBetaAgent} with 
     * opening books is an agent playing perfectly. We test only the games where  
     * {@code playAgent} starts, since games where {@link AlphaBetaAgent} starts are a safe 
     * win for {@link AlphaBetaAgent}.
     *
     * @param playAgent Agent to be evaluated
     * @param numEpisodes	actually 2*numEpisodes single-compete games are played (to be
     * 						comparable with the number of both-compete games in other funcs) 
     * @return a value between +1 and -1, depending on the rate of episodes won by the agent 
     * 		or opponent.
     */
    private double competeAgainstAlphaBeta(PlayAgent playAgent, int numEpisodes) {
//    	verbose=1;
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, alphaBetaStd), 0, new StateObserverC4(), 2*numEpisodes, verbose, null, null, null, false);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
       	System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates an agent's performance against perfect play, since {@link AlphaBetaAgent} with 
     * opening books is an agent playing perfectly. We test only the games where  
     * {@code playAgent} starts, since games where {@link AlphaBetaAgent} starts are a safe 
     * win for {@link AlphaBetaAgent}.
     * <p>
     * Here, it is an AlphaBeta agent <b>with distant losses</b>, which is harder to beat. It selects among a 
     * set of losing moves always that move that postpones the loss as far in the future as possible.
     *
     * @param playAgent Agent to be evaluated
     * @param numEpisodes	actually 2*numEpisodes single-compete games are played (to be
     * 						comparable with the number of both-compete games in other funcs) 
     * @return a value between +1 and -1, depending on the rate of episodes won by the agent 
     * 		or opponent. Results may vary somewhat since AlphaBeta-DistantLosses selects randomly one of the possible
     * 		best moves if there is more than one.
     */
    private double competeAgainstAlphaBetaDistantLoss(PlayAgent playAgent, int numEpisodes) {
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, alphaBeta_DL), 0, new StateObserverC4(), 2*numEpisodes, verbose, null, null, null, false);
//        ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, alphaBeta_DL), new StateObserverC4(), 2*numEpisodes, verbose);
		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
       	System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates {@code playAgent}'s performance when playing against MCTS using 1000 iterations.
     * Plays {@code numEpisodes} episodes as 1st player (those episodes that {@code playAgent} can win).
     *
     * @param playAgent agent to be evaluated
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between +1 and -1, depending on the rate of episodes won by the agent 
     * 		or opponent. Best for {@code playAgent} is +1, worst is -1. 
     */
    private double competeAgainstMCTS(PlayAgent playAgent, int numEpisodes) {
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(C4Base.CELLCOUNT,5) - 2);
        params.setNumIter((int) Math.pow(10, numIterExp));
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverC4(), params);

        // this version plays only games that playAgent can win (same as against AlphaBetaAgent):
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent), 0, new StateObserverC4(), 2*numEpisodes, verbose, null, null, null, false);
        // this version, if you want to test both directions:
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, mctsAgent), new StateObserverC4(), 2*numEpisodes, verbose);

		lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
       	System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Similar to {@link EvaluatorC4#competeAgainstMCTS(PlayAgent, int)}, but:
     * <ul> 
     * <li>It does not only play evaluation games from the default start state (empty board) but 
     * also games where the first player (Yellow) has made a losing move and the agent as second
     * player (Red) will win, if it plays perfect. 
     * <li>It allows a different opponent than MCTS to be passed in as 2nd argument
     * <li>It plays all episodes in both roles, 
     * </ul>
     *
     * @param playAgent agent to be evaluated
     * @param opponent 	agent against which {@code playAgent} plays.
     * @param gameBoard game board for the evaluation episodes
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between -1 and 1, with 1.0 as best possible result. If opponent is AlphaBeta, results may vary
     *      somewhat since AlphaBeta selects randomly one of the possible best moves if there is more than one.
     * 
     * @see EvaluatorC4#competeAgainstMCTS(PlayAgent, int)
     */
    private double competeAgainstOpponent_diffStates(PlayAgent playAgent, PlayAgent opponent, GameBoard gameBoard, int numEpisodes) {
        double sumResult = 0.0;
        double singleResult;
        lastResult = 0;
        
		if (opponent == null) {
			gameBoard.getArena().showMessage("ERROR: no opponent","Load Error", JOptionPane.ERROR_MESSAGE);
			lastResult = Double.NaN;
			return lastResult;
		} 

        int [] startAction = {-1,0,1,5,6};
        // The start states to evaluate:
        // the int's in startAction code the start board. -1: empty board (a winning 
        // board for 1st player Yellow), 
        // 0/1/...: Black's 1st move was column 0/1/... (it is a losing move, so it is a winning
        // board for 2nd player Red)
        //
        // [We found out by the now commented out lines System.out... here and the commented lines
        //  in case 11 in eval_Agent that the winning start actions are -1,0,1,5,6. (3 is a loosing
        //  action and 2 and 4 are a Tie.)]
       numStartStates = startAction.length;

        // evaluate each start state in turn and return average success rate: 
        for (int i=0; i<startAction.length; i++) {
        	StateObserverC4 so = new StateObserverC4();
        	if (startAction[i] == -1) {
        		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, opponent), 0, so, numEpisodes, 0, null, null, null, false);
//                ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, numEpisodes, 0);
        		singleResult = sc.scTup[0];
       	    } else {
        		so.advance(new ACTIONS(startAction[i]), null);
        		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(opponent, playAgent), 0, so, numEpisodes, 0, null, null, null, false);
//                ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(opponent, playAgent), so, numEpisodes, 0);
        		singleResult = sc.scTup[1];
        	}
        	//System.out.println(startAction[i]+": "+singleResult);
        	sumResult += singleResult;
        }
        lastResult = sumResult/startAction.length;
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
//        if (this.verbose > 0) 
        	System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Does the same as {@code competeAgainstOpponent_diffStates} with opponent=MCTS,  
     * but with 6 parallel cores, so it is 6 times faster. 
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
     * @param playAgent agent to be evaluated
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between -1 and 1, with 1.0 as best possible result.
     */
    private double competeAgainstMCTS_diffStates_PAR(PlayAgent playAgent, int numEpisodes) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(C4Base.CELLCOUNT,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));
        //mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[5], new StateObserverHex(), params);

        int [] startAction = {-1,0,1,5,6};
        // The start states to evaluate:
        // the int's in startAction code the start board. -1: empty board (a winning 
        // board for 1st player Yellow), 
        // 0/1/...: Black's 1st move was tile 00/01/... (it is a losing move, so it is a winning
        // board for 2nd player Red)
       numStartStates = startAction.length;
        
        List<Double> successObservers = new ArrayList<>();
        List<Callable<Double>> callables = new ArrayList<>();
        final int[] startAction2 = startAction;

        // evaluate each start state in turn and return average success rate: 
        for (int i=0; i<startAction.length; i++) {
            int gameNumber = i+1;
            final int i2 = i;
            callables.add(() -> {
                double success;
                long gameStartTime = System.currentTimeMillis();
                StateObserverC4 so = new StateObserverC4();
                
                // important: mctsAgent2 has to be constructed inside the 'callables' function, 
                // otherwise all parallel calls would operate on the same agent and would produce
                // garbage.
                MCTSAgentT mctsAgent2 = new MCTSAgentT("MCTS", new StateObserverC4(), params);

            	if (startAction2[i2] == -1) {
            		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent2), 0, so, numEpisodes, 0, null, null, null, false);
            		success = sc.scTup[0];
            	} else {
            		so.advance(new ACTIONS(startAction2[i2]), null);
            		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(mctsAgent2, playAgent), 0, so, numEpisodes, 0, null, null, null, false);
            		success = sc.scTup[1];
            	}
                if(verbose == 0) {
                    System.out.println("Finished evaluation " + gameNumber + " after " + (System.currentTimeMillis() - gameStartTime) + "ms. ");
                }
                lastResult = success;
                return lastResult;
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

        m_msg = playAgent.getName() + ": " + this.getPrintString() + averageSuccess;
//        if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return lastResult;
    }

    /**
     * Very weak but fast evaluator to see if there is a training progress at all.
     * Getting a high win rate against this evaluator does not guarantee good performance of the evaluated agent.
     *
     * @param playAgent Agent to be evaluated
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstRandom(PlayAgent playAgent) {
    	StateObservation so = new StateObserverC4();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,randomAgent), so, 50, 0, null, null, false);
		double success = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
        lastResult = success;
        return lastResult;
    }


    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2, 3, 4, 5, 10, 11};
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
            // Note: the best result is always 1.0 here, because the start state is in all cases
            // such that the agent to be evaluated can win if he plays perfect.
            case 0 -> "success against MCTS (best is 1.0): ";
            case 1 -> "success against Random (best is 1.0): ";
            case 2 -> "success against Max-N (best is 1.0): ";
            case 3 -> "success against AlphaBetaAgent (best is 1.0): ";
            case 4 -> "success against AlphaBetaAgent (" + numStartStates + " diff. start states, best is 1.0): ";
            case 5 -> "success against AlphaBetaAgentDistantLoss (best is 1.0): ";
            case 10 -> "success against MCTS (" + numStartStates + " diff. start states, best is 1.0): ";
            case 11 -> "success against TDReferee (" + numStartStates + " diff. start states, expected 0.0): ";
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
				+ "3: against AlphaBetaAgent, best is 1.0<br>"
				+ "4: against AlphaBetaAgent, diff. starts, best is 1.0<br>"
				+ "5: against AlphaBetaAgentDistantLoss, best is 1.0<br>"
				+ "10: against MCTS, diff. starts, best is 1.0<br>"
				+ "11: against TDReferee.agt.zip, diff. starts, expected 0.0"
				+ "</html>";
    	// Note: the best result is always 1.0 here, because the start state is in all cases
    	// such that the agent to be evaluated can win if he plays perfect.
	}

    @Override
    public String getPlotTitle() {
        return switch (m_mode) {
            case 0 -> "success against MCTS";
            case 1 -> "success against Random";
            case 2 -> "success against Max-N";
            case 3 -> "success against AlphaBeta";
            case 4 -> "success against AlphaBeta, dStart";
            case 5 -> "success against AlphaBetaDL";
            case 10 -> "success against MCTS, dStart";
            case 11 -> "success against TDReferee";
            default -> null;
        };
    }

 	// --- implemented by Evaluator ---
//  @Override
//  public String getMsg() {
//      return m_msg;
//  }

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
