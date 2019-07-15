package controllers.MCTSExpectimax;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.MCTS.MCTSAgentT;
import games.Arena;
import games.StateObservation;
import games.XArenaMenu;
//import params.MCTSExpectimaxParams;
//import params.MCTSParams;
//import params.OtherParams;
import params.ParMCTS;
import params.ParMCTSE;
import params.ParOther;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * MCTSExpectimaxAgt is the extension of {@link MCTSAgentT} for <b>non-deterministic</b> games.
 *
 * @author Johannes Kutsch
 * 
 * @see MCTSAgentT MCTSAgentT for deterministic games
 */
public class MCTSExpectimaxAgt extends AgentBase implements PlayAgent
{
	public ParMCTSE params;
    private MCTSEPlayer player;

	// if NEW_GNA==true: use the new functions getNextAction2... in getNextAction;
	// if NEW_GNA==false: use the old functions getNextAction1... in getNextAction;
//	private static boolean NEW_GNA=true;	

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	/**
	 * @param name	agent name, should be "MCTS Expectimax"
	 * @param mcPar Settings for the Agent
	 */
    public MCTSExpectimaxAgt(String name, ParMCTSE mcPar) {
		super(name);
		params = new ParMCTSE();
		params.setFrom(mcPar);

		initMCTSEAgent(mcPar, new ParOther());
	}

	/**
	 * @param name	agent name, should be "MCTS Expectimax"
	 * @param mcPar Settings for the Agent
	 */
    public MCTSExpectimaxAgt(String name, ParMCTSE mcPar, ParOther oPar) {
		super(name);
		params = new ParMCTSE();
		params.setFrom(mcPar);
		initMCTSEAgent(mcPar, oPar);
	}

    private void initMCTSEAgent(ParMCTSE mcPar, ParOther oPar) {   
		super.m_oPar = oPar;		// m_oPar is in AgentBase
		player = new MCTSEPlayer(this,new Random(), mcPar);

		setAgentState(AgentState.TRAINED);
    }

	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see XArenaMenu#loadAgent
	 * @see XArenaTabs
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setMctseParFrom(n, this.getParMCTSE() );
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
	}
	
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
	 *
     * @param stateObs Observation of the current state.
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, double[] vtable) {
		if (params.getNumAgents() > 1) {
			return actMultipleAgents(stateObs, vtable, params.getNumAgents());
		} else {
			return actOneAgent(stateObs, vtable);
		}
	}

	/**
	 * only one Agent, no majority Vote
	 */
    private Types.ACTIONS actOneAgent(StateObservation stateObs, double[] vtable) {
		//Set the state observation object as the new root of the tree.
		player.init(stateObs);

		//Determine the action using MCTS Expectimax and return it.
		Types.ACTIONS actBest;
		actBest = player.run(vtable); 
		actBest.setRandomSelect(false);		// the action was not a random move
		
		return actBest;
	}

	/**
	 * majority vote
	 */
    private Types.ACTIONS actMultipleAgents(StateObservation stateObs, double[] vtable, int numAgents) {
		double[] vtableIgnore = new double[vtable.length];
		double[] actions = new double[4];

		for (int i = 0; i < actions.length; i++) {
			actions[i] = 0;
		}

		//determine numAgents Actions and save them in the vtable
		for(int i = 0; i < numAgents; i++) {
			//Set the state observation object as the new root of the tree.
			player.init(stateObs);

			int act = player.run(vtableIgnore).toInt();

			actions[act]++;
		}

		List<Types.ACTIONS> nextActions = new ArrayList<>();
		double nextActionScore = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < actions.length; i++) {
			if (nextActionScore < actions[i]) {
				nextActions.clear();
				nextActions.add(Types.ACTIONS.fromInt(i));
				nextActionScore = actions[i];
			} else if(nextActionScore == actions[i]) {
				nextActions.add(Types.ACTIONS.fromInt(i));
			}
		}

		Random random = new Random();
		Types.ACTIONS actBest;
		actBest = nextActions.get(random.nextInt(nextActions.size()));
		
		actBest.setRandomSelect(false);		// the action was not a random move
		
    	return actBest;         
	}

    /**
     * Get the best next action and return it (new version, NEW_GNA==true).
     * Called by calcCertainty and getNextAction.
     * 
     * @param so 			current game state (not changed on return)
     * @return actBest		the next action
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
     */
    public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        List<Types.ACTIONS> actions = so.getAvailableActions();
		double[] VTable, vtable;
        vtable = new double[actions.size()];  
        VTable = new double[actions.size()+1];  
		
		assert so.isLegalState() 
			: "Not a legal state"; // e.g. player to move does not fit to Table
		
		// Ask MCTS for the best action ...
		actBest = act(so,VTable);
		
		double bestScore = VTable[actions.size()];
		for (int i=0; i<vtable.length; i++) vtable[i]=VTable[i];
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, bestScore);
        return actBestVT;
	}



	/**
	 * Get the best next action and return its score
	 *
	 * @param so 			current game state (not changed on return)
	 *
	 * @return				the score of the best action
	 */
	public double getScore(StateObservation so) {
		double[] vtable = new double[so.getNumAvailableActions()+1];
        double nextActionScore = Double.NEGATIVE_INFINITY;

        if (so.isGameOver()) {
        	return so.getGameScore(so);
        } else {
    		act(so,vtable);

            for (int i = 0; i < so.getNumAvailableActions(); i++) {
                if (nextActionScore <= vtable[i]) {
                    nextActionScore = vtable[i];
                }
            }

            return nextActionScore;
        }
	}

	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ": iterations:" + getParMCTSE().getNumIter() 
				+ ", rollout depth:" + getParMCTSE().getRolloutDepth()
				+ ", K_UCT:"+ getParMCTSE().getK_UCT()
				+ ", tree depth:" + getParMCTSE().getTreeDepth();
		return str;
	}

    public int getNRolloutFinished() {
        return player.getNRolloutFinished();
    }

    public int getNIterations() {
        return player.getNUM_ITERS();
    }
    
	public ParMCTSE getParMCTSE() {
		return player.getParMCTSE();
	}
	
}
