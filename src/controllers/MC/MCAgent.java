package controllers.MC;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import games.XArenaMenu;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import params.MCParams;
import params.OtherParams;
import params.ParMC;
import params.ParOther;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 08.12.2016.
 */
public class MCAgent extends AgentBase implements PlayAgent {
    private Random random = new Random();
    private ExecutorService executorService = Executors.newWorkStealingPool();

    private int totalRolloutDepth = 0;  // saves the average rollout depth for the mc Agent
    private int nRolloutFinished = 0; 	// counts the number of rollouts ending with isGameOver==true
    private int nIterations = 0; 		// counts the total number of iterations

    public ParMC m_mcPar = new ParMC();
	private ParOther m_oPar = new ParOther();

	// if NEW_GNA==true: use the new functions getNextAction2... in getNextAction;
	// if NEW_GNA==false: use the old functions getNextAction1... in getNextAction;
	private static boolean NEW_GNA=true;	

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

    
    public MCAgent(MCParams mcParams) {
        this("MC", mcParams, new OtherParams());
    }

    public MCAgent(String name, MCParams mcParams, OtherParams oPar)
    {
        super(name);
        this.m_mcPar.setFrom(mcParams);
		this.m_oPar = new ParOther(oPar);
        setAgentState(AgentState.TRAINED);
    }

//    /**
//     * Get the best next action and return it.
//     * 
//     * @param sob			current game state (not changed on return)
//     * @param vtable		must be an array of size n+1 on input, where
//     * 						n=sob.getNumAvailableActions(). On output,
//     * 						elements 0,...,n-1 hold the score for each available
//     * 						action (corresponding to sob.getAvailableActions())
//     * 						In addition, vtable[n] has the score for the
//     * 						best action.
//     * @param iterations    rollout repeats (for each available action)
//     * @param depth			rollout depth
//     * @return nextAction	the next action
//     */
//	@Deprecated
//    @Override
//    public Types.ACTIONS getNextAction(StateObservation sob, boolean random, double[] vtable, boolean silent) {
//        int iterations = m_mcPar.getNumIter();
//        int numberAgents = m_mcPar.getNumAgents();
//        int depth = m_mcPar.getRolloutDepth();
//
//		// this function selector is just intermediate, as long as we want to test getNextAction2 
//		// against getNextAction1 (the former getNextAction). Once everything works fine with
//		// getNextAction2, we should use only this function and make getNextAction deprecated 
//		// (requires appropriate changes in all other agents implementing interface PlayAgent).
//		if (!NEW_GNA) {
//	        if(numberAgents > 1) {
//	            //more than one agent (majority vote)
//	            return getNextAction1MultipleAgents(sob, vtable, iterations, numberAgents, depth);
//	        } else {
//	            //only one agent
//	            return getNextAction1(sob, vtable, iterations, depth);
//	        }
//		} else {
//			Types.ACTIONS_VT actBestVT = getNextAction2(sob, random, silent);
//			double[] VTable = actBestVT.getVTable();
//			for (int i=0; i<VTable.length; i++) vtable[i] = VTable[i];
//			vtable[VTable.length] = actBestVT.getVBest();
//			return actBestVT;
//			
//		}
//    }
//
//    /**
//     * Get the best next action and return it (multi-core version).
//     * Called by calcCertainty and getNextAction.
//     * 
//     * @param sob			current game state (not changed on return)
//     * @param vtable		must be an array of size n+1 on input, where
//     * 						n=sob.getNumAvailableActions(). On output,
//     * 						elements 0,...,n-1 hold the score for each available
//     * 						action (corresponding to sob.getAvailableActions())
//     * 						In addition, vtable[n] has the score for the
//     * 						best action.
//     * @param iterations    rollout repeats (for each available action)
//     * @param depth			rollout depth
//     * @return nextAction	the next action
//     */
//    private Types.ACTIONS getNextAction1(StateObservation sob, double[] vtable, int iterations, int depth) {
//    	//the functions which are to be distributed on the cores: 
//        List<Callable<ResultContainer>> callables = new ArrayList<>();
//        //the results of these functions:
//        List<ResultContainer> resultContainers = new ArrayList<>();
//        // all available actions for state sob:
//        List<Types.ACTIONS> actions = sob.getAvailableActions();
//
//        nRolloutFinished = 0;
//        nIterations = sob.getNumAvailableActions()* iterations;
//        totalRolloutDepth = 0;
//
//        //if only one action is available, return it immediately:
//        if(sob.getNumAvailableActions() == 1) {
//            return actions.get(0);
//        }
//
//        //build the functions to be distributed on the scores.
//        //For each iteration a function is built for each available action:
//        for (int j = 0; j < iterations; j++) {
//            for (int i = 0; i < sob.getNumAvailableActions(); i++) {
//
//                //make a copy of the game state:
//                StateObservation newSob = sob.copy();
//
//                //the actual action i has to be saved on a new variable, since
//                //the  for-loop value i is not accessible at the time of
//                //execution of an callables-element:
//                int firstActionIdentifier = i;
//
//                //The callables, that is, the functions which are later executed on
//                //multiple cores in parallel, are built. The callables are only
//                //built here, they will be executed only later with 
//                //invokeAll(callables) on executorService :
//                callables.add(() -> {
//
//                	//fetch the first action and execute it on the game state:
//                    Types.ACTIONS firstAction = actions.get(firstActionIdentifier);
//                    newSob.advance(firstAction);
//
//                    //construct Random Agent and let it simulate a (random) rollout:
//                    RandomSearch agent = new RandomSearch();
//                    agent.startAgent(newSob, depth);			// contains BUG1 fix
//
//                    //return result of simulation in an object of class ResultContainer:
//                    return new ResultContainer(firstActionIdentifier, newSob, agent.getRolloutDepth());
//                });
//            }
//        }
//
//        try {
//        	//executerService is called and it distributes all callables on all cores
//        	//of the CPU. The callables perform the simulations and the results of these
//        	//simulations are written to a stream:
//            executorService.invokeAll(callables).stream().map(future -> {
//                try {
//                    return future.get();
//                }
//                catch (Exception e) {
//                    throw new IllegalStateException(e);
//                }
//
//            //each result written to the stream is added to list resultContainers:    
//            }).forEach(resultContainers::add);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        //for each resultContainer in list resultContainers: add its game score
//        //to the appropriate action in vtable:
//        for(ResultContainer resultContainer : resultContainers) {
//            //vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore();	
//        	// /WK/ **BUG2** in line above: referingState was missing. Use instead next line:
//        	vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore(sob);
//            totalRolloutDepth += resultContainer.rolloutDepth;
//            if(resultContainer.sob.isGameOver()) {
//                nRolloutFinished++;
//            }
//        }
//
//        //find the best next action:
//        Types.ACTIONS bestAction = null;
//        double nextActionScore = Double.NEGATIVE_INFINITY;
//
//        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
//
//            //calculate average score:
//            vtable[i] /= iterations;
//
//            if (nextActionScore < vtable[i]) {
//                bestAction = actions.get(i);
//                nextActionScore = vtable[i];
//            }
//        }
//        vtable[sob.getNumAvailableActions()] = nextActionScore;
//        bestAction.setRandomSelect(false);
//
//        return bestAction;
//    }
//
//    /**
//     * Get the best next action and return it (single-core version, multiple agents).
//     * Called by calcCertainty and getNextAction.
//     * 
//     * @param sob			current game state (not changed on return)
//     * @param vtable		must be an array of size n+1 on input, where
//     * 						n=sob.getNumAvailableActions(). On output,
//     * 						elements 0,...,n-1 hold the score for each available
//     * 						action (corresponding to sob.getAvailableActions())
//     * 						In addition, vtable[n] has the score for the
//     * 						best action.
//     * @param iterations    rollout repeats (for each available action)
//     * @param numberAgents
//     * @param depth			rollout depth
//     * @return nextAction	the next action
//     */
//    private Types.ACTIONS getNextAction1MultipleAgents (StateObservation sob, double[] vtable,
//    		int iterations, int numberAgents, int depth) {
//        List<Types.ACTIONS> actions = sob.getAvailableActions();
//
//        nRolloutFinished = 0;
//        nIterations = sob.getNumAvailableActions()* iterations * numberAgents;
//        totalRolloutDepth = 0;
//
//        if(sob.getNumAvailableActions() == 1) {
//            return actions.get(0);
//        }
//        for (int i=0; i < vtable.length; i++) vtable[i]=0; // /WK/ bug fix, was missing before
//
//        for (int i = 0; i < numberAgents; i++) {
//            int nextAction = 0;
//            double nextActionScore = Double.NEGATIVE_INFINITY;
//
//            for (int j = 0; j < sob.getNumAvailableActions(); j++) {
//                double averageScore = 0;
//
//                for (int k = 0; k < iterations; k++) {
//                    StateObservation newSob = sob.copy();
//
//                    newSob.advance(actions.get(j));
//
//                    RandomSearch agent = new RandomSearch();
//                    agent.startAgent(newSob, depth);			// contains BUG1 fix
//
//                    //averageScore += newSob.getGameScore();	
//                	// /WK/ **BUG2** in line above: referringState sob was missing. Corrected:
//                    averageScore += newSob.getGameScore(sob);
//                    if (newSob.isGameOver()) nRolloutFinished++;
//                    totalRolloutDepth += agent.getRolloutDepth();
//                }
//
//                averageScore /= iterations;
//                if (nextActionScore <= averageScore) {
//                    nextAction = j;
//                    nextActionScore = averageScore;
//                }
//            }
//            //store in vtable[k] how many of the multiple agents did select action k 
//            //as the best next action
//            vtable[nextAction]++;
//        }
//
//        List<Types.ACTIONS> nextActions = new ArrayList<>();
//        double nextActionScore = Double.NEGATIVE_INFINITY;
//
//        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
//            if (nextActionScore < vtable[i]) {
//                nextActions.clear();
//                nextActions.add(actions.get(i));
//                nextActionScore = vtable[i];
//            } else if(nextActionScore == vtable[i]) {
//                nextActions.add(actions.get(i));
//            }
//        }
//        vtable[sob.getNumAvailableActions()] = nextActionScore;
//        
//        //for (int i = 0; i < vtable.length-1; i++) System.out.print((int)vtable[i]+",");
//        //System.out.println(""+sob.stringDescr());
//
//        Types.ACTIONS bestAction = nextActions.get(random.nextInt(nextActions.size()));
//        bestAction.setRandomSelect(false);
//
//        return bestAction;
//    }

	/**
	 * Get the best next action and return it 
	 * (NEW version: returns ACTIONS_VT)
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
        int iterations = m_mcPar.getNumIter();
        int numberAgents = m_mcPar.getNumAgents();
        int depth = m_mcPar.getRolloutDepth();
        
        if(numberAgents > 1) {
            //more than one agent (majority vote)
        	return getNextAction2MultipleAgents(so, iterations, numberAgents, depth);
        } else {
            //only one agent
        	return getNextAction2(so, iterations, depth);
        }
	}

    /**
     * Get the best next action and return it (multi-core version).
     * Called by calcCertainty and getNextAction2.
     * 
     * @param sob			current game state (not changed on return)
     * @param iterations    rollout repeats (for each available action)
     * @param depth			rollout depth
     * @return actBest		the best next action
 	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
    */
    private Types.ACTIONS_VT getNextAction2(StateObservation sob, int iterations, int depth) {
    	//the functions which are to be distributed on the cores: 
        List<Callable<ResultContainer>> callables = new ArrayList<>();
        //the results of these functions:
        List<ResultContainer> resultContainers = new ArrayList<>();
        // all available actions for state sob:
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
		double[] vtable;
        vtable = new double[actions.size()];  

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations;
        totalRolloutDepth = 0;

        //if only one action is available, return it immediately:
        if(sob.getNumAvailableActions() == 1) {
        	actBest = actions.get(0);
            return new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 0.0);       		
        }

        //build the functions to be distributed on the scores.
        //For each iteration a function is built for each available action:
        for (int j = 0; j < iterations; j++) {
            for (int i = 0; i < sob.getNumAvailableActions(); i++) {

                //make a copy of the game state:
                StateObservation newSob = sob.copy();

                //the actual action i has to be saved on a new variable, since
                //the  for-loop value i is not accessible at the time of
                //execution of an callables-element:
                int firstActionIdentifier = i;

                //The callables, that is, the functions which are later executed on
                //multiple cores in parallel, are built. The callables are only
                //built here, they will be executed only later with 
                //invokeAll(callables) on executorService :
                callables.add(() -> {

                	//fetch the first action and execute it on the game state:
                    Types.ACTIONS firstAction = actions.get(firstActionIdentifier);
                    newSob.advance(firstAction);

                    //construct Random Agent and let it simulate a (random) rollout:
                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob, depth);			// contains BUG1 fix

                    //return result of simulation in an object of class ResultContainer:
                    return new ResultContainer(firstActionIdentifier, newSob, agent.getRolloutDepth());
                });
            }
        }

        try {
        	//executerService is called and it distributes all callables on all cores
        	//of the CPU. The callables perform the simulations and the results of these
        	//simulations are written to a stream:
            executorService.invokeAll(callables).stream().map(future -> {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }

            //each result written to the stream is added to list resultContainers:    
            }).forEach(resultContainers::add);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //for each resultContainer in list resultContainers: add its game score
        //to the appropriate action in vtable:
        for(ResultContainer resultContainer : resultContainers) {
            //vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore();	
        	// /WK/ **BUG2** in line above: referingState was missing. Use instead next line:
        	vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore(sob);
            totalRolloutDepth += resultContainer.rolloutDepth;
            if(resultContainer.sob.isGameOver()) {
                nRolloutFinished++;
            }
        }

        //find the best next action:
        Types.ACTIONS bestAction = null;
        double bestActionScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {

            //calculate average score:
            vtable[i] /= iterations;

            if (bestActionScore < vtable[i]) {
            	actBest = actions.get(i);
                bestActionScore = vtable[i];
            }
        }

		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, bestActionScore);
        return actBestVT;
    }

    /**
     * Get the best next action and return it (single-core version, multiple agents).
     * Called by calcCertainty and getNextAction2.
     * 
     * @param sob			current game state (not changed on return)
     * @param iterations    rollout repeats (for each available action)
     * @param numberAgents
     * @param depth			rollout depth
     * @return nextAction	the next action (including VTable and best score)
     * <p>
     * Each element of {@code nextAction.getVTable()} holds the score for each available
     * action (corresponding to {@code sob.getAvailableActions()}). The score is in the case 
     * of multiple agents the <b>number of agents</b> which did select this action. 
     */
    private Types.ACTIONS_VT getNextAction2MultipleAgents (StateObservation sob, 
    		int iterations, int numberAgents, int depth) {
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        List<Types.ACTIONS> actions = sob.getAvailableActions();
		double[] vtable;
        vtable = new double[actions.size()];  

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations * numberAgents;
        totalRolloutDepth = 0;

        if(sob.getNumAvailableActions() == 1) {
        	actBest = actions.get(0);
            return new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 0.0);       		
        }

        for (int i = 0; i < numberAgents; i++) {
            int nextAction = 0;
            double nextActionScore = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < sob.getNumAvailableActions(); j++) {
                double averageScore = 0;

                for (int k = 0; k < iterations; k++) {
                    StateObservation newSob = sob.copy();

                    newSob.advance(actions.get(j));

                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob, depth);			// contains BUG1 fix

                    //averageScore += newSob.getGameScore();	
                	// /WK/ **BUG2** in line above: referringState sob was missing. Corrected:
                    averageScore += newSob.getGameScore(sob);
                    if (newSob.isGameOver()) nRolloutFinished++;
                    totalRolloutDepth += agent.getRolloutDepth();
                }

                averageScore /= iterations;
                if (nextActionScore <= averageScore) {
                    nextAction = j;
                    nextActionScore = averageScore;
                }
            }
            //store in vtable[k] how many of the multiple agents did select action k 
            //as the best next action
            vtable[nextAction]++;
        }

        List<Types.ACTIONS> nextActions = new ArrayList<>();
        double bestActionScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            if (bestActionScore < vtable[i]) {
                nextActions.clear();
                nextActions.add(actions.get(i));
                bestActionScore = vtable[i];
            } else if(bestActionScore == vtable[i]) {
                nextActions.add(actions.get(i));
            }
        }
        
        actBest = nextActions.get(random.nextInt(nextActions.size()));
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, bestActionScore);
        return actBestVT;
    }

	@Override
    public double getScore(StateObservation sob) {
//        double[] vtable = new double[sob.getNumAvailableActions()+1];
//        double nextActionScore = Double.NEGATIVE_INFINITY;
        
		// This if branch is vital: It was missing before, and if 'so' was a game-over state
		// this could result in wrong scores.
		// Now we fix this by returning so.getGameScore(so) on a game-over situation:
        if (sob.isGameOver()) {
        	return sob.getGameScore(sob);
        } else {
        	
            Types.ACTIONS_VT actBestVT = getNextAction2(sob, false, true);

//            for (int i = 0; i < sob.getNumAvailableActions(); i++) {
//                if (nextActionScore <= vtable[i]) {
//                    nextActionScore = vtable[i];
//                }
//            }

            return actBestVT.getVBest();
        }

    }
    
    /**
     * Calculate the certainty by repeating the next-action calculation 
     * NC times and calculating the relative frequency of the most frequent
     * next action
     * 
     * @param sob			the game state
     * @param numberAgents	= 1: use getNextAction2 (parallel version) <br>
     *                      &gt; 1: use getNextActionMultipleAgents with NUMBERAGENTS=numberAgents
     * @param silent
     * @param NC			number of repeats for next-action calculation
     * @param iterations    rollout repeats (for each available action)
     * @param depth			rollout depth
     * @return the certainty (highest bin in the relative-frequency histogram of possible actions)
     */
    public double calcCertainty(StateObservation sob, /*double[] vtable,*/
    		int numberAgents, boolean silent, int NC, int iterations, int depth) {
        double[] wtable = new double[4];
        double highestBin;
        int nextAction;
        
        if(sob.getNumAvailableActions() == 1) {
            return 1.0;
        }

        for (int i = 0; i < NC; i++) {
        	if (numberAgents==1) {
//        		nextAction = getNextAction1(sob,vtable,iterations, depth).toInt();
        		nextAction = getNextAction2(sob,iterations, depth).toInt();
        	} else {
//        		nextAction = getNextAction1MultipleAgents(sob,vtable,iterations,numberAgents, depth).toInt();
        		nextAction = getNextAction2MultipleAgents(sob,iterations,numberAgents, depth).toInt();
        		if (!silent) System.out.print(".");
        	}
            wtable[nextAction]++;
        }
        if (numberAgents!=1 & !silent) System.out.println("");

        highestBin = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < wtable.length; i++) {
            if (highestBin < wtable[i]) {
                highestBin = wtable[i];
            } 
        }
        double cert = highestBin/NC;
        
    	return cert;
    }

    public double getAverageRolloutDepth() {
        return totalRolloutDepth/nIterations;
    }

    public int getNRolloutFinished() {
        return nRolloutFinished;
    }

    public int getNIterations() {
        return nIterations;
    }
	public ParMC getMCPar() {
		return m_mcPar;
	}
	public ParOther getOtherPar() {
		return m_oPar;
	}
	/**
	 * Set defaults for m_oPar 
	 * (needed in {@link XArenaMenu#loadAgent} when loading older agents, where 
	 * m_oPar=null in the saved version).
	 */
	public void setDefaultOtherPar() {
		m_oPar = new ParOther();
	}

//	/**
//	 * 
//	 * Use now {@link Types.ACTIONS#isRandomAction()}
//	 */
//    @Deprecated
//    public boolean wasRandomAction() {
//        return false;
//    }

    @Override
    public String stringDescr() {
        String cs = getClass().getName();
		String str = cs + ": iterations:" + m_mcPar.getNumIter() 
				+ ", rollout depth:" + m_mcPar.getRolloutDepth()
				+ ", # agents:"+ m_mcPar.getNumAgents();
		return str;
    }
}


//
// this code snippet was just for debug purposes [formerly part of getNextAction()]:
//
/*  if (m_mcPar.getCalcCertainty()) {
StateObserver2048 sobZTAV = (StateObserver2048) sob;
if (sobZTAV.getNumEmptyTiles()==10) {
	int NC = ConfigEvaluator.NC;
    double cert0, cert1,cert2=0,cert4=0,cert6=0;
    cert0 = calcCertainty(sob, vtable,1,false, NC, iterations, depth);
    NC = 20;
    cert1 = calcCertainty(sob, vtable,1,false, NC, iterations, depth);
    cert2 = calcCertainty(sob, vtable,2,false, NC, iterations, depth);
    cert6 = calcCertainty(sob, vtable,6,false, NC, iterations, depth);
    System.out.println("n="+sob.getNumAvailableActions()+": certainty ="
            + cert0+","+cert1
            +" / "+ cert2
            +" / "+ cert6);
}
} */



class ResultContainer {
    public int firstAction;
    public StateObservation sob;
    public double rolloutDepth;

    public ResultContainer(int firstAction, StateObservation sob, double rolloutDepth) {
        this.firstAction = firstAction;
        this.sob = sob;
        this.rolloutDepth = rolloutDepth;
    }
}