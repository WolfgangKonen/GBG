package games.Nim;

import java.util.ArrayList;

import controllers.PlayAgent;
import games.ObserverBase;
import games.StateObservation;
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
public class StateObserverNim extends ObserverBase implements StateObservation {
	private int[] m_heap;		// has for each heap the count of items in it
	private int m_player;			// Player who makes the next move 
	private ArrayList<Types.ACTIONS> acts = new ArrayList();	// holds all available actions
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	/**
	 * Construct an object with {@link NimConfig#NUMBER_HEAPS} heaps. Each heap starts with
	 * {@link NimConfig#HEAP_SIZE} items. Player 0 is the starting player.
	 */
	public StateObserverNim() {
		m_heap = new int[NimConfig.NUMBER_HEAPS]; 
		for (int i=0;i<NimConfig.NUMBER_HEAPS;i++) 
			m_heap[i] = NimConfig.HEAP_SIZE;
		m_player = 0;
		setAvailableActions();
	}

	public StateObserverNim(int[] heaps, int player) {
		assert heaps.length==NimConfig.NUMBER_HEAPS;
		m_heap = heaps.clone();
		m_player = player;
		setAvailableActions();
	}
	
	public StateObserverNim copy() {
		StateObserverNim so = new StateObserverNim(m_heap,m_player);
		so.m_counter = this.m_counter;
		return so;
	}

    @Override
	public boolean isGameOver() {
		int sum=0;
		for (int i=0;i<NimConfig.NUMBER_HEAPS;i++) 
			sum = sum + this.m_heap[i];
		
		return (sum==0);
	}

    @Override
	public boolean isDeterministicGame() {
		return true;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

//    @Override
//	public boolean has2OppositeRewards() {
//		return true;
//	}

    @Override
	public boolean isLegalState() {
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		int iAction = act.toInt();						
		int j=iAction%NimConfig.MAX_SUB;
		int heap=(iAction-j)/NimConfig.MAX_SUB;		// from which heap to subtract
		int subtractor = j+1;
										// reverse: iAction = MAX_SUB*heap + j
		
		assert heap < NimConfig.NUMBER_HEAPS : "Oops, heap no "+heap+" is not available!";
				
		return (m_heap[heap]>=subtractor); 		
	}

	@Override
    public String stringDescr() {
		String sout = "";
	
		for (int i=0;i<NimConfig.NUMBER_HEAPS;i++) 
				sout = sout + this.m_heap[i];
		
 		return sout;
	}
	
	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win()
	{
		return isGameOver();
	}
	

	public Types.WINNER getGameWinner() {
		assert isGameOver() : "Game is not yet over!";
		if (win())							  // advance() has changed m_player (although game is over): 
			return Types.WINNER.PLAYER_LOSES; // the player, who would have to move, looses.
		return null;
	}

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For Nim only game-over states have a non-zero game score. 
	 * 			It is the reward for the player who *would* move next (if 
	 * 			the game were not over). 
	 */
	public double getGameScore() {
        boolean gameOver = this.isGameOver();
        if(gameOver) {
            Types.WINNER win = this.getGameWinner();
        	switch(win) {
        	case PLAYER_LOSES:
                return NimConfig.REWARD_NEGATIVE;
        	case TIE:
                return 0;
        	case PLAYER_WINS:
                return NimConfig.REWARD_POSITIVE;
            default:
            	throw new RuntimeException("Wrong enum for Types.WINNER win !");
        	}
        }
        
        return 0; 
	}

	public double getMinGameScore() { return NimConfig.REWARD_NEGATIVE; }
	public double getMaxGameScore() { return NimConfig.REWARD_POSITIVE; }

	public String getName() { return "Nim";	}

	/**
	 * Advance the current state with action {@code action} to a new state
	 * @param action the action having key  
	 * <pre>
	 * 		iAction = i*NimConfig.MAX_SUB+j
	 * </pre> 
	 * with j = 0,...,{@link NimConfig#MAX_SUB} means: 
	 * Subtract {@code j+1} items from heap no {@code i}. 
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		int j=iAction%NimConfig.MAX_SUB;
		int heap=(iAction-j)/NimConfig.MAX_SUB;		// from which heap to subtract
		int subtractor = j+1;
										// reverse: iAction = MAX_SUB*heap + j
		
		assert heap < NimConfig.NUMBER_HEAPS : "Oops, heap no "+heap+" is not available!";
		assert subtractor <= NimConfig.MAX_SUB : "Oops, cannot take more than "+NimConfig.MAX_SUB+" items from heap!";
		assert m_heap[heap]>=subtractor : "Oops, heap no "+heap+" has not "+subtractor+" items left!"; 		
		
		m_heap[heap] -= subtractor;
    	
    	setAvailableActions(); 			// IMPORTANT: adjust the available actions (have reduced by one)
    	
    	// set up player for next advance()
    	int n=this.getNumPlayers();
		m_player = (m_player+1) % n;  // many-player games: 0,1,...,n-1,0,1,...
		
		super.incrementMoveCounter();
	}

    /**
     * Return the afterstate preceding {@code this}. 
     */
    @Override
    public StateObservation getPrecedingAfterstate() {
    	// for deterministic games, this state and its preceding afterstate are the same
    	return this;
    }

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
        for (int i = 0; i < NimConfig.NUMBER_HEAPS*NimConfig.MAX_SUB; i++) 
            	allActions.add(Types.ACTIONS.fromInt(i));
        
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return acts;
	}
	
	public int getNumAvailableActions() {
		return acts.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] acts.
	 * 
	 * Action 
	 * <pre>
	 * 		iAction = i*NimConfig.MAX_SUB+j
	 * </pre> 
	 * with j = 0,...,{@link NimConfig#MAX_SUB} means: 
	 * Subtract {@code j+1} items from heap no {@code i}. 
	 * <p>
	 */
	public void setAvailableActions() {
		acts.clear();
		for (int i=0;i<NimConfig.NUMBER_HEAPS;i++) {
			for (int j=0; j<NimConfig.MAX_SUB; j++) 
				if (j<m_heap[i]) acts.add(Types.ACTIONS.fromInt(i*NimConfig.MAX_SUB+j));
			
		}
	}
	
	public Types.ACTIONS getAction(int i) {
		return acts.get(i);
	}

	public int[] getHeaps() {
		return m_heap;
	}

	/**
	 * @return 	{0,1} for the player to move next. 
	 * 			Player 0 is X, the player who starts the game. Player 1 is O.
	 */
	public int getPlayer() {
		return m_player;
	}
	
	public int getNumPlayers() {
		return 2;				// Nim is a 2-player game
	}


}
