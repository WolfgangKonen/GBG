package games.Nim;

import java.util.ArrayList;
import java.util.Arrays;

import controllers.PlayAgent;
import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObserverNim observes the current state of the game Nim <b>for 2 players</b> and it is the 
 * superclass for {@link StateObserverNim3P}. It has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 * See {@link GameBoardNim2P} for game rules.
 */
public class StateObserverNim extends ObserverBase implements StateObservation {
	protected int[] m_heap;		// has for each heap the count of items in it
	protected int m_player;		// player who makes the next move (0 or 1)
	protected ArrayList<Types.ACTIONS> availableActions = new ArrayList();	// holds all available actions
	protected boolean SORT_IT = false;		// experimental
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	protected static final long serialVersionUID = 12L;

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

	public StateObserverNim(StateObserverNim other) {
		super(other);		// copy members m_counter and stored*
		this.m_heap = other.m_heap.clone();
		this.m_player = other.m_player;
		this.SORT_IT = other.SORT_IT;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this would be a bit slower
	}
	
	public StateObserverNim copy() {
		StateObserverNim so = new StateObserverNim(this);
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

    @Override
	public boolean isLegalState() {
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		int iAction = act.toInt();						
		int j=iAction%NimConfig.MAX_MINUS;
		int heap=(iAction-j)/NimConfig.MAX_MINUS;		// from which heap to subtract
		int subtractor = j+1;
										// reverse: iAction = MAX_MINUS*heap + j
		
		assert heap < NimConfig.NUMBER_HEAPS : "Oops, heap "+heap+" is not available!";
				
		return (m_heap[heap]>=subtractor); 		
	}

	@Override
    public String stringDescr() {
		String sout = "";
		String[] play = {"X","O"};
	
		sout = sout + play[m_player]+"("+this.m_heap[0];
		for (int i=1;i<NimConfig.NUMBER_HEAPS;i++) 
			sout = sout + "," + this.m_heap[i];
		sout = sout + ")";
		
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
	
	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For Nim only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		int sign = (refer.getPlayer()==this.getPlayer()) ? 1 : (-1);
        if(isGameOver()) {
        	// if the game is over, it is a win for the player who made the action towards this
        	// --> it is a loss for the player to move in this.
        	// [There is no tie in game Nim.]
        	return -sign;        	
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
	 * 		iAction = i*NimConfig.MAX_MINUS+j
	 * </pre> 
	 * with j = 0,...,{@link NimConfig#MAX_MINUS} means: 
	 * Subtract {@code j+1} items from heap no {@code i}. 
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		int j=iAction%NimConfig.MAX_MINUS;
		int heap=(iAction-j)/NimConfig.MAX_MINUS;		// from which heap to subtract
		int subtractor = j+1;
											  // reverse: iAction = MAX_MINUS*heap + j
		
		assert heap < NimConfig.NUMBER_HEAPS : "Oops, heap "+heap+" is not available!";
		assert subtractor <= NimConfig.MAX_MINUS : "Oops, cannot take more than "+NimConfig.MAX_MINUS+" items from heap!";
		assert m_heap[heap]>=subtractor : "Oops, heap "+heap+" has not "+subtractor+" items left!"; 		
		
		m_heap[heap] -= subtractor;
    	
		if (SORT_IT) Arrays.sort(m_heap);	// still experimental
		
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
        for (int i = 0; i < NimConfig.NUMBER_HEAPS*NimConfig.MAX_MINUS; i++) 
            	allActions.add(Types.ACTIONS.fromInt(i));
        
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] acts.
	 * 
	 * Action 
	 * <pre>
	 * 		iAction = i*NimConfig.MAX_MINUS+j
	 * </pre> 
	 * with j = 0,...,{@link NimConfig#MAX_MINUS}-1 means: 
	 * Subtract {@code j+1} items from heap no {@code i}. 
	 * <p>
	 */
	public void setAvailableActions() {
		availableActions.clear();
		for (int i=0;i<NimConfig.NUMBER_HEAPS;i++) {
			for (int j=0; j<NimConfig.MAX_MINUS; j++) 
				if (j<m_heap[i]) availableActions.add(Types.ACTIONS.fromInt(i*NimConfig.MAX_MINUS+j));
			
		}
	}
	
	public Types.ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[] getHeaps() {
		return m_heap;
	}

	public int getHeapSum() {
		int s=0;
		for (int h : m_heap) s+=h;
		return s;
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

	/**
	 * Calculate the winning move for the Nim configuration in {@code heaps} according to 
	 * Bouton's theory. See <a href="https://en.wikipedia.org/wiki/Nim">
	 * https://en.wikipedia.org/wiki/Nim</a>. If there is no winning move, make a dummy 
	 * move (take 1 item from the 1st heap).
	 * 
	 * @param heaps the array of heaps, element {@code i} has the number of items in the 
	 * 		  {@code i}th heap
	 * @return a tuple with two values: <br>
	 * 		(index of the heap to change, amount of items to remove) 
	 */
	public int[] bouton(int[] heaps) {
		/*
		 * 	the Python implementation (from https://en.wikipedia.org/wiki/Nim):
		 * 
		    nim_sum = functools.reduce(lambda x, y: x ^ y, heaps)
		    if nim_sum == 0:
		        return "You will lose :("
	    */
		//
		// The equivalent Java implementation is
		// 		Arrays.stream(heaps).reduce(startValue, (x,y) -> (x%K) ^ (y%K));
		// The variable heaps, which is a sequence of int's, is fed via reduce into the lambda-construct 
		// 		(x,y) -> (x%K) ^ (y%K) 
		// where ^ is the XOR operator: the first two elements are bitwise XOR'ed, then the
		// result of this together with the 3rd element is bitwise XOR'ed, then the
		// result of this together with the 4th element is bitwise XOR'ed, and so on. 
		// The final result is the binary digital sum or nim-sum of all heaps.
		//
		// Why do we apply %K, i.e. modulo K, before XORing x and y? - If we have the constraint
		// that at most K-1 items may be taken from any heap, then a safe winning position is one 
		// where every heap has K items or multiples of K items (clear?). So we try to reach such a 
		// winning position. This means that only the *remainder* of x and y in excess of multiples
		// of K (i.e. x%K) is relevant to compute the nim-sum and find the winning move.
		//
		// It is an important bug fix (05/2020) to do %K on array 'remain' BEFORE reduce and NOT within the XOR:
		// If we did (wrongly) (x%K)^(y%K), then it might happen that the XOR produces a K and the modulo 
		// would replace this with 0. Example: K=6, x=3, y=5
		//
		// Why reduce(startValue,...)? - Because reduce(int,...) directly returns an int as well.
		//
		int startValue=0;
		int K = NimConfig.MAX_MINUS+1;
		int[] remain = new int[heaps.length];
		for (int i=0; i<heaps.length; i++) remain[i] = heaps[i]%K;
		int nim_sum = Arrays.stream(remain).reduce(startValue, (x,y) -> x ^ y);
		if ( nim_sum==0 ) 
			return new int[] {0,1};		// we will loose with any move --> return dummy move

	    // We are in a winning position: Calculate which move to make
		// (again adapted from https://en.wikipedia.org/wiki/Nim)
	    int index=0;
	    for (int heap : heaps) {
	        int target_size = heap ^ nim_sum;		// ^ : bitwise XOR
	        if (target_size < heap) {
	            int amount_to_remove = heap - target_size;
	            return new int [] {index, amount_to_remove};	        	
	        }
	        index++;
	    }
	    
	    // we should never get here, just for safety:
	    throw new RuntimeException("Oops, bouton() failed!");
	}
	
	/**
	 * Apply {@link #bouton(int[])} to the heaps of {@code this}.
	 */
	public int[] bouton() {
		return bouton(this.m_heap);
	}
	
	/**
	 * Return the game value of configuration {@code heaps}  according to Bouton's theory.
	 * See <a href="https://en.wikipedia.org/wiki/Nim">
	 * https://en.wikipedia.org/wiki/Nim</a>. 
	 * 
	 * @param heaps	{@code heaps[i]} has the number of items in the {@code i}th heap
	 * @return 	Return +1.0 or -1.0, depending on whether the configuration in {@code heaps} is a 
	 * 			win (for the player which performed the action leading to {@code heaps}) or not.
	 */
	public double boutonValue(int[] heaps) {
		int startValue=0;
		int K = NimConfig.MAX_MINUS+1;
		
		// see same lines in method bouton(int[] heaps) for an explanation of the following statements:
		int[] remain = new int[heaps.length];
		for (int i=0; i<heaps.length; i++) remain[i] = heaps[i]%K;
		int nim_sum = Arrays.stream(remain).reduce(startValue, (x,y) -> x ^ y);
		// if nim_sum==0, heaps is a winning configuration for the player who created it, else not:
		return (nim_sum==0) ? +1.0 : -1.0;
	}
	
}
