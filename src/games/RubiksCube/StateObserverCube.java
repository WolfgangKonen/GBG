package games.RubiksCube;

import java.util.ArrayList;

import controllers.PlayAgent;
import games.ObserverBase;
import games.StateObservation;
import games.RubiksCube.CubeState.Twist;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverCube extends ObserverBase implements StateObservation {
	private CubeState m_state;
	private static CubeState def = new CubeState(); // a solved cube as reference
    private static final double REWARD_POSITIVE =  1.0;
	protected Types.ACTIONS[] actions;
    
    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    public double storedMaxScore; 
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverCube() {
		m_state = new CubeState(); 
		setAvailableActions();
	}

	public StateObserverCube(int[] fcol) {
		m_state = new CubeState(fcol);
		setAvailableActions();
	}
	
	public StateObserverCube(CubeState other) {
		m_state = new CubeState(other);
		setAvailableActions();
	}
	
	public StateObserverCube(StateObserverCube other) {
		m_state = new CubeState(other.m_state);
		m_state.lastTwist = Twist.ID;		// we assume that we do not know the last twist
											// when we get a new initial state
	}
	
	public StateObserverCube copy() {
		return new StateObserverCube(this);
	}

    @Override
	public boolean isGameOver() {
    	boolean pred = (this.m_state.equals(def));
    	if (pred==true) {
    		int dummy=1;
    	}
		return pred;
	}

    @Override
	public boolean isDeterministicGame() {
		return true;
	}
	
    @Override
	public boolean isLegalState() {
    	if (!m_state.twistSeq.equals("")) {
    		return m_state.assertTwistSequence();
    	}
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		return (act.toInt()<9); 
		
	}

	@Override
    public String stringDescr() {
 		return m_state.toString();
	}
	
	public Types.WINNER getGameWinner() {
		assert isGameOver() : "Game is not yet over!";
		return Types.WINNER.PLAYER_WINS;
	}

	public CubeState getCubeState() {
		return m_state;
	}
	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
	 * 			It is the reward for the player who *would* move next (if 
	 * 			the game were not over). 
	 */
	public double getGameScore() {
        boolean gameOver = this.isGameOver();
        if(gameOver) return REWARD_POSITIVE;
        return 0; 
	}

	public double getMinGameScore() { return 0; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "RubiksCube";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		assert (0<=iAction && iAction<9) : "iAction is not in 0,1,...,8.";
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j
		
		switch(i) {
		case 0: m_state.UTw(j+1); break;
		case 1: m_state.LTw(j+1); break;
		case 2: m_state.FTw(j+1); break;
		}
	}

    /**
     * Return the afterstate preceding {@code this}. 
     */
    @Override
    public StateObservation getPrecedingAfterstate() {
    	// for deterministic games, this state and its preceding afterstate are the same
    	return this;
    }

	public ArrayList<ACTIONS> getAvailableActions() {
		ArrayList<ACTIONS> availAct = new ArrayList<ACTIONS>();
		if (m_state.lastTwist!=Twist.U) {
			availAct.add(Types.ACTIONS.fromInt(0));  // U1
			availAct.add(Types.ACTIONS.fromInt(1));  // U2
			availAct.add(Types.ACTIONS.fromInt(2));  // U3
		}
		if (m_state.lastTwist!=Twist.L) {
			availAct.add(Types.ACTIONS.fromInt(3));  // L1
			availAct.add(Types.ACTIONS.fromInt(4));  // L2
			availAct.add(Types.ACTIONS.fromInt(5));  // L3
		}		
		if (m_state.lastTwist!=Twist.F) {
			availAct.add(Types.ACTIONS.fromInt(6));  // F1
			availAct.add(Types.ACTIONS.fromInt(7));  // F2
			availAct.add(Types.ACTIONS.fromInt(8));  // F3
		}		
		return availAct;
	}
	
	public int getNumAvailableActions() {
		if (actions==null) setAvailableActions();
		return actions.length;
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
        // /WK/ Get the available actions in an array.
		// *TODO* Does this work if acts.size()==0 ?
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
		
	}
	
	public Types.ACTIONS getAction(int i) {
		return actions[i];
	}

	/**
	 * Given the current state, store some info useful for inspecting the  
	 * action actBest and double[] vtable returned by a call to <br>
	 * {@code ACTION_VT} {@link PlayAgent#getNextAction2(StateObservation, boolean, boolean)}. 
	 *  
	 * @param actBest	the best action
	 * @param vtable	one double for each action in this.getAvailableActions():
	 * 					it stores the value of that action (as given by the double[] 
	 * 					from {@link Types.ACTIONS_VT#getVTable()}) 
	 */
	public void storeBestActionInfo(ACTIONS actBest, double[] vtable) {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        storedActions = new Types.ACTIONS[acts.size()];
        storedValues = new double[acts.size()];
        for(int i = 0; i < storedActions.length; ++i)
        {
        	storedActions[i] = acts.get(i);
        	storedValues[i] = vtable[i];
        }
        storedActBest = actBest;
        if (actBest instanceof Types.ACTIONS_VT) {
        	storedMaxScore = ((Types.ACTIONS_VT) actBest).getVBest();
        } else {
            storedMaxScore = vtable[acts.size()];        	
        }
	}

    public int getPlayer() {
        return 0;
    }

	public int getNumPlayers() {
		return 1;				// the Cube is a one-player puzzle
	}


}
