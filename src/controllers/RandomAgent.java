package controllers;

import games.StateObservation;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * the Random {@link PlayAgent} 
 * 
 * @author Wolfgang Konen, TH Koeln, 2016
 * 
 */
public class RandomAgent extends AgentBase implements PlayAgent {
	private Random rand;
	private int[][] m_trainTable = null;
	private double[][] m_deltaTable = null;
	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;
		
	public RandomAgent(String name) {
		super(name);
		super.setMaxGameNum(1000); 		
		super.setGameNum(0); 			
        rand = new Random(System.currentTimeMillis());
		setAgentState(AgentState.TRAINED);
	}

	public RandomAgent(String name, long randomSeed) {
		super(name);
		super.setMaxGameNum(1000);
		super.setGameNum(0);
		rand = new Random(randomSeed);
		setAgentState(AgentState.TRAINED);
	}
		
    public RandomAgent(String name, ParOther oPar) {
        super(name);
		super.setMaxGameNum(1000); 		
		super.setGameNum(0); 			
		this.m_oPar = new ParOther(oPar);
        rand = new Random(System.currentTimeMillis());
        setAgentState(AgentState.TRAINED);
    }

    /**
	 * Get the best next action and return it 
	 * (NEW version: returns ACTIONS_VT and has a recursive part for multi-moves)
	 * 
	 * @param so            current game state (is returned unchanged)
	 * @param random        allow random action selection with probability m_epsilon
	 * @param deterministic
     * @param silent        execute silently without outputs
     * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable to store the value for each available
	 * action (as returned by so.getAvailableActions()) and vBest to store the value for the best action actBest.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean deterministic, boolean silent) {
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        List<Types.ACTIONS> bestActions = new ArrayList<>();
		double[] vtable = new double[acts.size()];  
		
		int i,j;
		double maxScore = -Double.MAX_VALUE;
		double CurrentScore = 0; 	// the quantity to be maximized
		
		assert so.isLegalState() : "Not a legal state";

		assert acts.size()>0 : "No available actions";
		
        for(i = 0; i < acts.size(); ++i)
        {
        	CurrentScore = rand.nextDouble();
        	vtable[i] = CurrentScore;
        	if (maxScore < CurrentScore) {
        		maxScore = CurrentScore;
                bestActions.clear();
                bestActions.add(acts.get(i));
        	} else if (maxScore == CurrentScore) {
                bestActions.add(acts.get(i));
        	}
        } // for
        actBest = bestActions.get(rand.nextInt(bestActions.size()));
        // if several actions have the same best score, select one of them randomly

        assert actBest != null : "Oops, no best action actBest";
		StateObservation NewSO = so.copy();
		NewSO.advance(actBest, null);
        if (!silent) {
			// optional: show the best action
        	System.out.println("---Best Move: "+NewSO.stringDescr()+"   "+maxScore);
        }			
		actBest.setRandomSelect(true);		// the action was a random move

		// determine the ScoreTuple scBest (needed when we wrap this agent with MCTS(Exp)Wrapper):
		ScoreTuple scBest = new ScoreTuple(so,maxScore);
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), true, vtable, maxScore, scBest);
        return actBestVT;
	}


//	@Override
//	public double getScore(StateObservation sob) {
//		return rand.nextDouble();
//	}

	/**
	 * Return a tuple with the agent's estimate of {@code sob}'s final game value (final reward)
	 * <b>for all players</b>. <br>
	 * Is called by the n-ply wrappers ({@link MaxN2Wrapper}, {@link ExpectimaxNWrapper}).
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		ScoreTuple st = new ScoreTuple(so.getNumPlayers());
		for (int i=0; i<st.scTup.length; i++) st.scTup[i]=rand.nextDouble();
    	return st;
	}

}