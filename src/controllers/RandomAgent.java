package controllers;
import java.util.ArrayList;
import java.util.Random;

import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;
import params.TDParams;
import tools.Types;



/**
 * the Random {@link PlayAgent} for TicTacToe 
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 * 
 */
public class RandomAgent extends AgentBase implements PlayAgent
{
	private Random rand;
	private int[][] m_trainTable=null;
	private double[][] m_deltaTable=null;
		
	public RandomAgent(String name)
	{
		super(name);
		super.setMaxGameNum(1000); 		
		super.setGameNum(0); 			
        rand = new Random(System.currentTimeMillis());
		setAgentState(AgentState.TRAINED);
	}
		
	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param VTable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
	 * @param silent
	 * @return actBest		the best action 
	 */	
	@Override
	public Types.ACTIONS getNextAction(StateObservation so, boolean random, double[] VTable, boolean silent) {
		int i,j;
		double MaxScore = -Double.MAX_VALUE;
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		StateObservation NewSO;
		int count = 1; // counts the moves with same MaxScore
        Types.ACTIONS actBest = null;
        int iBest;
		//VTable = new double[so.getNumAvailableActions()];
        // DON'T! The caller has to define VTable with the right length

		assert so.isLegalState() 
			: "Not a legal state"; // e.g. player to move does not fit to Table

        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        //VTable = new double[acts.size()];
        // DON'T! The caller has to define VTable with the right length

        for(i = 0; i < actions.length; ++i)
        {
        	actions[i] = acts.get(i);
        	NewSO = so.copy();
        	NewSO.advance(actions[i]);
        	CurrentScore = rand.nextDouble();
        	VTable[i] = CurrentScore;
        	if (MaxScore < CurrentScore) {
        		MaxScore = CurrentScore;
        		actBest = actions[i];
        		iBest  = i; 
        		count = 1;
        	} else if (MaxScore == CurrentScore) {
        		count++;	        
        	}
        } // for
        if (count>1) {  // more than one action with MaxScore: 
        	// break ties by selecting one of them randomly
        	int selectJ = (int)(rand.nextDouble()*count);
        	for (i=0, j=0; i < actions.length; ++i) 
        	{
        		if (VTable[i]==MaxScore) {
        			if (j==selectJ) actBest = actions[i];
        			j++;
        		}
        	}
        }

        // optional: show the best action
        assert actBest != null : "Oops, no best action actBest";
        if (!silent) {
        	NewSO = so.copy();
        	NewSO.advance(actBest);
        	System.out.print("---Best Move: "+NewSO.toString()+"   "+MaxScore);
        }			

        return actBest;         // the action was not a random move
	}


	/**
	 * 
	 * @return	returns true/false, whether the action suggested by last call 
	 * 			to getNextAction() was a random action. 
	 * 			Always true in the case of RandomAgent.
	 */
	public boolean wasRandomAction() {
		return true;
	}


	@Override
	public double getScore(StateObservation sob) {
		return rand.nextDouble();
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		return cs;
	}
	
}