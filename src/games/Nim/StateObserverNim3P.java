package games.Nim;

import java.util.ArrayList;
import java.util.Arrays;

import controllers.PlayAgent;
import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObserverNim3P observes the current state of the game Nim <b>for 3 players</b>. It has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 * See {@link GameBoardNim3P} for game rules: The player <b>who comes after</b> the player removing the last item 
 * is the player who wins. He/she gets reward 1, the other two get reward 0. See also {@link #EXTRA_RULE}.
 */
public class StateObserverNim3P extends StateObserverNim implements StateObservation {
    
	
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
	public StateObserverNim3P() {
		super();				// initialize all members in superclass StateObserverNim
	}

	public StateObserverNim3P(int[] heaps, int player) {
		super(heaps,player);	// initialize all members in superclass StateObserverNim
	}

	public StateObserverNim3P(StateObserverNim3P other) {
		super(other);			// copy all members in superclass StateObserverNim
	}
	
	public StateObserverNim3P copy() {
		StateObserverNim3P so = new StateObserverNim3P(this);
		return so;
	}

	@Override
    public String stringDescr() {
		String sout = "";
		String[] play = {"P0","P1","P2"};
	
		sout = sout + play[m_player]+"("+this.m_heap[0];
		for (int i=1;i<NimConfig.NUMBER_HEAPS;i++) 
			sout = sout + "," + this.m_heap[i];
		sout = sout + ")";
		
 		return sout;
	}
	
	/**
	 * The game score, seen from the perspective of player {@code player}.  
	 * The rule for 3-player Nim is acc. to [Luckhardt86]: If the player just before me has taken the last piece, 
	 * then I win. This happens if {@code this} is a game-over state and I am the player 'to move next'.
	 * 
	 * @param player the player whose perspective is taken, a number in 0,1,...,N.
	 * @return  If the game is over and {@code player} and {@code this.player} are the same, then return 1 
	 * 			({@code player} is the <b>winner</b>). 
	 * 			Otherwise return 0. Additional, if {@link NimConfig#EXTRA_RULE} is set: If {@code player} is the <b>successor
	 * 			of the winner</b>, then add a reward of 0.2 for this {@code player}.
	 */
	@Override
	public double getGameScore(int player) {
        if(isGameOver()) {
        	double secondReward=0.0;
        	if (NimConfig.EXTRA_RULE && player==(this.getPlayer()+1)%this.getNumPlayers()) secondReward = 0.2;
        	return (this.getPlayer() == player ? 1 : secondReward);
        }      
        return 0; 
	}
	
	@Override
	public double getReward(int player, boolean rewardIsGameScore) {
		return getGameScore(player);
	}

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For Nim3P only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	@Override
	public double getGameScore(StateObservation refer) {
        if(isGameOver()) {
        	// If the game is over, it is a win for the player to move in this, and he/she gets reward 1.
        	// If EXTRA_RULE==false, both other players lose and get reward 0.0, 
        	// If EXTRA_RULE==true, then the player following the winner gets reward 0.2.
        	// [There is no tie in game Nim3P.]
        	double secondReward=0.0;
        	if (NimConfig.EXTRA_RULE && refer.getPlayer()==(this.getPlayer()+1)%this.getNumPlayers()) secondReward = 0.2;
        	return (refer.getPlayer()==this.getPlayer()) ? 1 : secondReward;
        	
        }       
        return 0; 
	}

	@Override
	public String getName() { return "Nim3P";	}

	/**
	 * @return 	{0,1,2} for the player to move next. 
	 */
	@Override
	public int getPlayer() {
		return m_player;
	}
	
	@Override
	public int getNumPlayers() {
		return 3;				// Nim3P is a 3-player game
	}
	
}
