package controllers.MC;

import controllers.AgentBase;
import controllers.ExpectimaxNWrapper;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.Arena;
import games.StateObsNondeterministic;
import games.StateObservation;
import params.ParMC;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monte Carlo (MC) agent for N-player games.
 * <p>
 * Similar to former {@code MCAgent}, but it operates on {@link ScoreTuple} and is thus general for
 * N-player games with arbitrary N. It might be a bit slower than {@code MCAgent}.
 * <p>
 * @see MCAgentConfig
 * @see RandomSearch
 * @see ParMC
 */
public class MCAgentN extends AgentBase implements PlayAgent {
    private final Random random = new Random();
    private transient ExecutorService executorService = Executors.newWorkStealingPool();

    private int nIterations = 0; 		// counts the total number of iterations
    private int totalRolloutDepth = 0;  // sum of all rollout depths during a getNextAction call.
                                        // (totalRolloutDepth/nIterations = average rollout depth)
    private int nRolloutFinished = 0; 	// counts how many rollouts end with isGameOver==true, given the rollout depth
                                        // prescribed in m_mcPar.getRolloutDepth()

    public ParMC m_mcPar;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	@Serial
    private static final long  serialVersionUID = 12L;

    public MCAgentN(ParMC mcParams){
        this("MC", mcParams, new ParOther());    	
    }
    
    public MCAgentN(String name, ParMC mcParams, ParOther oPar)
    {
        super(name);
        this.m_mcPar = new ParMC(mcParams);
		this.m_oPar = new ParOther(oPar);
        setAgentState(AgentState.TRAINED);
    }

	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setMcParFrom(n, this.getParMC() );
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
	}
	
	/**
	 * Get the best next action and return it 
	 * (NEW version: returns ACTIONS_VT)
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent        no output
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 * <p>
	 * {@code actBest.getScoreTuple()} returns the {@link ScoreTuple} connected with this
     * selected action.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
        int iterations = m_mcPar.getNumIter();
        int numberAgents = m_mcPar.getNumAgents();
        int depth = m_mcPar.getRolloutDepth();
        boolean stopOnRoundOver = m_mcPar.getStopOnRoundOver();

        Types.ACTIONS_VT actBest;
        if(numberAgents > 1) {
            //more than one agent (majority vote)
        	actBest = getNextAction2MultipleAgents(so, iterations, numberAgents, depth, stopOnRoundOver);
        } else {
            //only one agent: select one of the following lines:
        	actBest =  getNextAction_PAR(so, iterations, depth, stopOnRoundOver);
        	//actBest = getNextAction_MassivePAR(so, iterations, depth, stopOnRoundOver);
        }

        // TODO (when ObserverBase's storedActBest is ACTIONS_VT)
        //      But perhaps the update storeBestActionInfo is done outside in Arena
        // Check if the update of the stored ScoreTuple in so is the same as in actBest:
        //assert actBest.getScoreTuple().equals(so.getStoredActBest().getScoreTuple())

        return actBest;
	}

    /**
     * Get the best next action and return it (multi-core [parallel] version).
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
	 * <p>
	 * {@code actBest.getScoreTuple()} returns the {@link ScoreTuple} connected with this
     * selected action.
     * <p>
     * The parallelization is done over the available actions. It is NOT done over the rollout
     * iterations (as well), this would be too fine-grained (at least for games like Hex). 
     */
    private Types.ACTIONS_VT getNextAction_PAR(StateObservation sob,
                                               int iterations,
                                               int depth,
                                               boolean stopOnRoundOver) {
    	//the functions which are to be distributed on the cores: 
        List<Callable<ResultContainerN>> callables = new ArrayList<>();
        //the results of these functions:
        List<ResultContainerN> resultContainers = new ArrayList<>();
        // all available actions for state sob:
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        Types.ACTIONS actBest = actions.get(0);
        Types.ACTIONS_VT actBestVT;
		double[] vtable;
        vtable = new double[actions.size()];  
		double currProbab = 1.0/iterations;
		int sobPlayer = sob.getPlayer();
		ScoreTuple bestActionScoreTuple = null;
		ScoreTuple[] nextActionScoreTuple = new ScoreTuple[actions.size()];

		for (int i=0; i<actions.size(); i++) {
			nextActionScoreTuple[i] = new ScoreTuple(sob);
		}
		
        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations;
        totalRolloutDepth = 0;

//        //if only one action is available, return it immediately:
//        if(sob.getNumAvailableActions() == 1) {
//        	actBest = actions.get(0);
//            return new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 0.0);       		
//        }
        // we do  not return early but run the code below even in case sob.getNumAvailableActions()==1  
        // in order to get a meaningful nextAction.getScoreTuple() (score tuple for the action taken)

        //build the functions to be distributed on the cores.
        //For each available action a function is built:
        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            RandomSearch agent= new RandomSearch();

            //the actual action i has to be saved on a new variable, since
            //the  for-loop value i is not accessible at the time of
            //execution of an callables-element:
            int firstActionIdentifier = i;

            //The callables, that is, the functions which are later executed on
            //multiple cores in parallel, are built. The callables are only
            //built here, they will be executed only later with 
            //invokeAll(callables) on executorService :
            callables.add(() -> {
            	long rolloutDepth=0;
            	int nRolloutFinished=0;
            	ScoreTuple avgScoreTuple = new ScoreTuple(sob);
            	
                for (int j = 0; j < iterations; j++) {

                    //make a copy of the game state:
                    StateObservation newSob = sob.copy();
                    
                	//fetch the first action (of rollout) and execute it on the game state:
                    newSob.advance(actions.get(firstActionIdentifier));

                    //construct Random Agent and let it simulate a (random) rollout until game over:
                    agent.startAgent(newSob, depth, stopOnRoundOver);			// contains BUG1 fix

                    // since newSob now has the *cumulative* reward in its GameScoreTuple, we subtract the cumulative
                    // reward at rollout start, since this delta is the utility of the rollout. /WK/03/2021/
                    newSob.getGameScoreTuple().combine(
                            sob.getGameScoreTuple(),
                            ScoreTuple.CombineOP.DIFF,0,0
                    );

                    avgScoreTuple.combine(
                            newSob.getGameScoreTuple(),
                            ScoreTuple.CombineOP.AVG,
                            sobPlayer,
                            currProbab
                    );
                    rolloutDepth += agent.getRolloutDepth();
                    if(newSob.isGameOver()) nRolloutFinished++;
                    
                } // for (j)

                //average score is:  
                //     avgScoreTuple.scTup[sobPlayer];              

                //return result of simulation in an object of class ResultContainer:
                return new ResultContainerN(firstActionIdentifier, avgScoreTuple, 
                		rolloutDepth, nRolloutFinished);
            });
        } // for (i)

        try {
        	//executerService is called and it distributes all callables on all cores
        	//of the CPU. The callables perform the simulations and the results of these
        	//simulations are written to a stream:
            if (executorService==null)
                executorService = Executors.newWorkStealingPool();
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
        int r_i;
        for(ResultContainerN resultContainer : resultContainers) {
        	r_i = resultContainer.firstAction;
        	vtable[r_i] = resultContainer.avgScoreTuple.scTup[sobPlayer];
        	nextActionScoreTuple[r_i] = resultContainer.avgScoreTuple;
            totalRolloutDepth += resultContainer.rolloutDepth;
            nRolloutFinished += resultContainer.nRolloutFinished;
        }

        //find the best next action:
        double bestActionScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {

            if (bestActionScore < vtable[i]) {
            	actBest = actions.get(i);
                bestActionScore = vtable[i];
                bestActionScoreTuple = nextActionScoreTuple[i];
            }
        }

		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 
				 						 bestActionScore, bestActionScoreTuple);
        return actBestVT;
    }

    /**
     * Get the best next action and return it (multi-core [massive parallel] version).
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
	 * <p>
	 * {@code actBest.getScoreTuple()} returns the {@link ScoreTuple} connected with this
     * selected action.
     * <p>
     * The parallelization is done over the available actions AND over the rollout
     * iterations as well. This might be an alternative for games like 2048, where there are only 
     * up to 4 actions. If you want to activate this function, you have to change the source
     * code in {@link #getNextAction2(StateObservation, boolean, boolean).} 
     */
    private Types.ACTIONS_VT getNextAction_MassivePAR(StateObservation sob,
                                                      int iterations,
                                                      int depth,
                                                      boolean stopOnRoundOver) {
    	//the functions which are to be distributed on the cores: 
        List<Callable<ResultContainer>> callables = new ArrayList<>();
        //the results of these functions:
        List<ResultContainer> resultContainers = new ArrayList<>();
        // all available actions for state sob:
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        Types.ACTIONS actBest = actions.get(0);
        Types.ACTIONS_VT actBestVT;
		double[] vtable;
        vtable = new double[actions.size()];  
		double currProbab = 1.0/iterations;
		int sobPlayer = sob.getPlayer();
		ScoreTuple.CombineOP cOP1 = ScoreTuple.CombineOP.AVG;
		ScoreTuple bestActionScoreTuple = null;
		ScoreTuple[] nextActionScoreTuple = new ScoreTuple[actions.size()];

		for (int i=0; i<actions.size(); i++) {
			nextActionScoreTuple[i] = new ScoreTuple(sob);
		}
		
        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations;
        totalRolloutDepth = 0;

//        //if only one action is available, return it immediately:
//        if(sob.getNumAvailableActions() == 1) {
//        	actBest = actions.get(0);
//            return new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 0.0);       		
//        }
        // we do  not return early but run the code below even in case sob.getNumAvailableActions()==1  
        // in order to get a meaningful nextAction.getScoreTuple() (score tuple for the action taken)

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
                    agent.startAgent(newSob, depth, stopOnRoundOver);			// contains BUG1 fix

                    // since newSob now has the *cumulative* reward in its GameScoreTuple, we subtract the cumulative
                    // reward at rollout start, since this delta is the utility of the rollout. /WK/03/2021/
                    newSob.getGameScoreTuple().combine(
                            sob.getGameScoreTuple(),
                            ScoreTuple.CombineOP.DIFF,0,0
                    );

                    //return result of simulation in an object of class ResultContainer:
                    return new ResultContainer(firstActionIdentifier, newSob, agent.getRolloutDepth());
                });
            }
        }

        try {
        	//executerService is called and it distributes all callables on all cores
        	//of the CPU. The callables perform the simulations and the results of these
        	//simulations are written to a stream:
            if (executorService==null)
                executorService = Executors.newWorkStealingPool();
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
        int r_i;
        for(ResultContainer resultContainer : resultContainers) {
        	r_i = resultContainer.firstAction;
        	vtable[r_i] += resultContainer.sob.getGameScore(sob.getPlayer());
        	nextActionScoreTuple[r_i].combine(resultContainer.sob.getGameScoreTuple(), cOP1, sobPlayer, currProbab);
            totalRolloutDepth += resultContainer.rolloutDepth;
            if(resultContainer.sob.isGameOver()) {
                nRolloutFinished++;
            }
        }

        //find the best next action:
        double bestActionScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {

            //calculate average score:
            vtable[i] /= iterations;

            if (bestActionScore < vtable[i]) {
            	actBest = actions.get(i);
                bestActionScore = vtable[i];
                bestActionScoreTuple = nextActionScoreTuple[i];
            }
        }

		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 
				 						 bestActionScore, bestActionScoreTuple);
        return actBestVT;
    }

    /**
     * Get the best next action and return it (single-core version, multiple agents).<br>
     * Called by {@code calcCertainty(..)} and {@link #getNextAction2(StateObservation, boolean, boolean)}.
     * 
     * @param sob			current game state (not changed on return)
     * @param iterations    rollout repeats (for each available action)
     * @param numberAgents	how many MC agents to calculate
     * @param depth			rollout depth
     * @return actBest		the best next action (including VTable and best score)
     * <p>
     * Each element of {@code actBest.getVTable()} holds the score for each available
     * action (corresponding to {@code sob.getAvailableActions()}). The score is in the case 
     * of multiple agents the <b>number of agents</b> which did select this action. 
     * <p>
     * The next action is one of the actions where <b>number of agents</b> is maximal. (If 
     * there are multiple such actions, one of them is selected at random.)  
     * <p>
     * {@code actBest.getScoreTuple()} returns the {@link ScoreTuple} connected with this
     * selected action.
     */
    private Types.ACTIONS_VT getNextAction2MultipleAgents(StateObservation sob,
                                                          int iterations,
                                                          int numberAgents,
                                                          int depth,
                                                          boolean stopOnRoundOver) {
        Types.ACTIONS actBest;
        Types.ACTIONS_VT actBestVT;
        List<Types.ACTIONS> actions = sob.getAvailableActions();
		double[] vtable;
        vtable = new double[actions.size()];  
		double currProbab = 1.0/iterations;
		int sobPlayer = sob.getPlayer();
		ScoreTuple[] nextActionScoreTuple = new ScoreTuple[actions.size()];
		ScoreTuple bestActionScoreTuple = null;

		for (int i=0; i<actions.size(); i++) {
			nextActionScoreTuple[i] = new ScoreTuple(sob);
		}
		
        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations * numberAgents;
        totalRolloutDepth = 0;

//        if(sob.getNumAvailableActions() == 1) {
//        	actBest = actions.get(0);
//            return new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 0.0);       		
//        }
        // we do  not return early but run the code below even in case sob.getNumAvailableActions()==1  
        // in order to get a meaningful nextAction.getScoreTuple() (score tuple for the action taken)

        for (int i = 0; i < numberAgents; i++) {
            int nextAction = 0;
            double nextActionScore = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < sob.getNumAvailableActions(); j++) {
                ScoreTuple avgScoreTuple = new ScoreTuple(sob);

                for (int k = 0; k < iterations; k++) {
                    StateObservation newSob = sob.copy();

                    newSob.advance(actions.get(j));

                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob, depth, stopOnRoundOver);			// contains BUG1 fix

                    // since newSob now has the *cumulative* reward in its GameScoreTuple, we subtract the cumulative
                    // reward at rollout start, since this delta is the utility of the rollout. /WK/03/2021/
                    newSob.getGameScoreTuple().combine(
                            sob.getGameScoreTuple(),
                            ScoreTuple.CombineOP.DIFF,0,0
                    );

                    avgScoreTuple.combine(
                            newSob.getGameScoreTuple(),
                            ScoreTuple.CombineOP.AVG,
                            sobPlayer,
                            currProbab
                    );
                    if (newSob.isGameOver()) nRolloutFinished++;
                    totalRolloutDepth += agent.getRolloutDepth();
                }

                double averageScore = avgScoreTuple.scTup[sobPlayer];
                if (nextActionScore <= averageScore) {
                    nextAction = j;
                    nextActionScore = averageScore;
                    nextActionScoreTuple[j].combine(
                            avgScoreTuple,
                            ScoreTuple.CombineOP.MAX,
                            sobPlayer,
                            0.0
                    );
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
                bestActionScoreTuple = nextActionScoreTuple[i];
            } else if(bestActionScore == vtable[i]) {
                nextActions.add(actions.get(i));
            }
        }
        
        actBest = nextActions.get(random.nextInt(nextActions.size()));
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, 
										 bestActionScore, bestActionScoreTuple);
        return actBestVT;
    }

	@Override
    public double getScore(StateObservation sob) {
        
		// This if branch is vital: It was missing before, and if 'sob' was a game-over state
		// this could result in wrong scores.
		// Now we fix this by returning sob.getGameScore(sob) on a game-over situation:
        if (sob.isGameOver()) {
        	return sob.getGameScore(sob.getPlayer());
        } else {       	
            Types.ACTIONS_VT actBestVT = getNextAction2(sob.partialState(), false, true);
            return actBestVT.getVBest();
        }

    }
    
	/**
	 * Return a tuple with the agent's estimate of {@code sob}'s final game value (final reward) 
	 * <b>for all players</b>. <br>
	 * Is called by the n-ply wrappers ({@link MaxN2Wrapper}, {@link ExpectimaxNWrapper}).
	 * @param so	the state s_t for which the value is desired
	 * 
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of 
	 * 				the future score for s_t from the perspective of player i.
	 * <p>
	 * The {@link ScoreTuple} is obtained by running  
	 * {@link #getNextAction2(StateObservation, boolean, boolean)} and retrieving from 
	 * the returned {@link Types.ACTIONS_VT} object the score tuple.
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
        if (so.isGameOver()) {
            return so.getGameScoreTuple();
        } else {
            if (so.isNextActionDeterministic()) {
                Types.ACTIONS_VT actBestVT = getNextAction2(so.partialState(), false, true);
                return actBestVT.getScoreTuple();
            } else {
                return getScoreTuple_NextActionNonDet((StateObsNondeterministic) so);
            }
        }
	}

    /**
     * This is for the case that getScoreTuple is called with a next-action-nondeterministic state.
     * In that case we average over all nondeterministic actions and call for each of the resulting
     * states with next-action-deterministic {@link #getNextAction2(StateObservation, boolean, boolean)}.
     *
     * @param soND  the state whose next action is nondeterministic
     * @return the weighted average over all non-deterministic choices (weight = probability of each choice)
     */
	private ScoreTuple getScoreTuple_NextActionNonDet(StateObsNondeterministic soND) {
        ArrayList<Types.ACTIONS> rans = soND.getAvailableRandoms();
        assert (rans.size()>0) : "Error: getAvailableRandoms returns no actions";
        ScoreTuple expecScoreTuple=new ScoreTuple(soND);	// a 0-ScoreTuple
        ScoreTuple currScoreTuple;
        double currProbab;
        double sumProbab=0.0;
        StateObsNondeterministic NewSO;
        for(int i = 0; i < rans.size(); ++i)
        {
            NewSO = soND.copy();
            NewSO.advanceNondeterministic(rans.get(i));
            while(!NewSO.isNextActionDeterministic() && !NewSO.isRoundOver()){		// /WK/03/2021 NEW
                NewSO.advanceNondeterministic();
            }

            currScoreTuple = getNextAction2(NewSO.partialState(), false, true).getScoreTuple();

            currProbab = soND.getProbability(rans.get(i));
            sumProbab += currProbab;
            expecScoreTuple.combine(currScoreTuple, ScoreTuple.CombineOP.AVG, 0, currProbab);
        }
        assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilities is not 1.0";

        return expecScoreTuple;
    }

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>.
	 * <p>
	 * The difference to {@link #getScoreTuple(StateObservation, ScoreTuple)} is that this function should 
	 * <b>not</b> call getScore or getScoreTuple in any way, to avoid endless recursion.
	 * 
	 * @param sob			the current game state
	 * @return				the agent's estimate of the final game value <b>for all players</b>. 
	 * 						The return value is a tuple containing  
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s. 
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		ScoreTuple sc = new ScoreTuple(sob);
		boolean rgs = m_oPar.getRewardIsGameScore();
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] += sob.getReward(i, rgs);
		return sc;
	}

    /**
     * Calculate the certainty by repeating the next-action calculation 
     * NC times and calculating the relative frequency of the most frequent
     * next action
     * 
     * @param sob			the game state
     * @param numberAgents	= 1: use getNextAction2 (parallel version) <br>
     *                      &gt; 1: use getNextActionMultipleAgents with NUMBERAGENTS=numberAgents
     * @param silent        no output
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
        		nextAction = getNextAction_PAR(sob,iterations, depth,false).toInt();
        	} else {
        		nextAction = getNextAction2MultipleAgents(sob,iterations,numberAgents, depth, false).toInt();
        		if (!silent) System.out.print(".");
        	}
            wtable[nextAction]++;
        }
        if (numberAgents!=1 & !silent) System.out.println();

        highestBin = Double.NEGATIVE_INFINITY;

        for (double v : wtable) {
            if (highestBin < v) {
                highestBin = v;
            }
        }

    	return highestBin/NC;
    }

    public double getAverageRolloutDepth() {
        return totalRolloutDepth/(double)nIterations;
    }

    public int getNRolloutFinished() {
        return nRolloutFinished;
    }

    public int getNIterations() {
        return nIterations;
    }
	public ParMC getParMC() {
		return m_mcPar;
	}
	
    @Override
    public String stringDescr() {
        String cs = getClass().getName();
		    cs += ": iterations:" + m_mcPar.getNumIter()
				+ ", rollout depth:" + m_mcPar.getRolloutDepth()
				+ ", # agents:"+ m_mcPar.getNumAgents();
		return cs;
    }
    
} // class MCAgentN

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

class ResultContainerN {
    public int firstAction;
    public long rolloutDepth;
    public ScoreTuple avgScoreTuple;
    public int nRolloutFinished;

    public ResultContainerN(int firstAction, ScoreTuple avgScoreTuple, 
    		long rolloutDepth, int nRolloutFinished) {
        this.firstAction = firstAction;
        this.avgScoreTuple = avgScoreTuple;
        this.rolloutDepth = rolloutDepth;
        this.nRolloutFinished = nRolloutFinished;	
    }
}




