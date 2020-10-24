package games.Sim;

import java.io.Serializable;
import java.util.ArrayList;

import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Arena;
import games.ObserverBase;
import games.StateObservation;
import games.Sim.Gui.BoardPanel;
import tools.Types.ACTIONS;
import tools.Types;

/**
 * This class holds any valid Sim game state. It is coded
 * as array {@link Link2}{@code [] lFrom}, where {@code lFrom[i]} carries all links from node {@code i} 
 * to all nodes with higher index than {@code i}. Each link can be 
 * <ul>
 * <li>= 0 for an empty link,
 * <li>= 1 for a P0 link,
 * <li>= 2 for a P1 link,
 * <li>= 3 for a P2 link (in the 3-player variant),
 * </ul>
 * where Pi refers to player i=0,1[,2]. 
 * <p>
 * P0 starts the game.
 * <p>
 * The available actions are the (empty) links, which are numbered consecutively: If we have K nodes
 * in total, then node i=0,...,K-1 has K-1-i links (to node i+1,...,K-1). For example, in the case
 * K=6 we have K*(K-1)/2=15 actions (the links from node i to node j) which are numbered as: 
 * <pre>
 *      j  0   1   2   3   4   5  
 *                                 i
 *             00  01  02  03  04  0
 *                 05  06  07  08  1
 *                     09  10  11  2
 *                         12  13  3
 *                             14  4
 *                                 5    </pre>
 *  The {@code lines} in {@link BoardPanel} are numbered exactly the same way as the actions.
 *  <p>
 *  This class is a completely rewritten version of the former StateObsererSim (by P. Wuensch, now in 
 *  deprecated/.../StateObserverSim_OLD.java). The new version has code easier to maintain and can be better 
 *  extended to the case of using only a few symmetries.
 *  
 *  @author Wolfgang Konen, TH Koeln, 2020
 *
 */
public class StateObserverSim extends ObserverBase implements StateObservation {
	private int numNodes;
	private int numPlayers;
	private int player;			// 0,1 in 2-player variant;   0,1,2 in 3-player variant
	private Link2[] lFrom;
	/**
	 * The list of available actions
	 */
	private ArrayList<Types.ACTIONS> availableActions = new ArrayList<>();
	/**
	 * The list of last moves in an episode. Each move is stored as {@link Integer} {@code iAction}.
	 */
	private ArrayList<Integer> lastMoves;
	/**
	 * This array holds the nodes involved in the last action taken. More precisely, 
	 * {@code lastNodes[0]} and {@code lastNodes[1]} hold the node numbers connected by 
	 * the last link taken. {@code lastNodes[2]} is only set when {@link #hasLost(int)} is called and
	 * returns {@code true}: It is the node number completing the losing triangle 
	 * (needed in {@link BoardPanel} to color this triangle).
	 */
	private int[] lastNodes = {-1,-1,-1};
	
	private static final long serialVersionUID = 12L;	//Serial number
	private FinalSim finalSim;

	StateObserverSim() 
	{
		config(ConfigSim.NUM_PLAYERS, ConfigSim.NUM_NODES);
	}
		
	StateObserverSim(StateObserverSim other)
	{
		super(other);		// copy members m_counter and stored*
		this.numNodes = other.numNodes;
		this.numPlayers = other.numPlayers;
		this.player = other.player;
		this.finalSim = new FinalSim(other.finalSim);

		setupLinks(other.numNodes);
		copyLinks(other.lFrom);
		
		this.lastMoves = (ArrayList<Integer>) other.lastMoves.clone();
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this as replacement for availableActions.clone() would be a bit slower
	}
	
	private void config(int numberOfPlayer, int numberOfNodes)
	{
		this.numNodes = numberOfNodes;
		this.numPlayers = numberOfPlayer;
		this.player = 0;
		this.finalSim = new FinalSim(numberOfPlayer);
		
		setupLinks(numberOfNodes);
		setAvailableActions();
		this.lastMoves = new ArrayList<>();
	}
	
	@Override
	public StateObserverSim copy() 
	{
		StateObserverSim sos = new StateObserverSim(this);		// includes via 'super(other)' the copying of stored*-members in ObserverBase

		return sos;
	}

	private void setupLinks(int numberOfNodes)
	{
		lFrom = new Link2[numberOfNodes];
		for(int i = 0; i < numberOfNodes; i++)
			lFrom[i] = new Link2(i,numberOfNodes);
	}
	
	private void copyLinks(Link2[] links)
	{
		for(int i = 0; i < numNodes; i++)
			this.lFrom[i] = new Link2(links[i]);
	}
	
	public boolean hasLost(int player)
	{
		if (lastNodes[0] != lastNodes[1]) {		// if action 'grab the link between these two nodes' is taken
			for(int i = 0; i < lFrom.length; i++)
				if(!(i == lastNodes[0] || i == lastNodes[1]))
				{
					if(getLinkFromTo(i,lastNodes[0]) == player + 1 && 
					   getLinkFromTo(i,lastNodes[1]) == player + 1) {
						lastNodes[2] = i;
						//System.out.println(this.stringDescr());
						return true;
					}
				}
		}		
		return false;
	}
	
	public int[] getLastNodes() {
		return lastNodes;
	}
	
	/**
	 * Check if the graph is full (all links occupied).
	 * This is a draw only, if no player has lost (no monochromatic triangle).
	 */
	private boolean isFull()
	{
		for(Link2 link : lFrom)
			if(link.hasSpaceLeft())
				return false;
		return true;
	}
	
	@Override
	public boolean isGameOver() {		
		return finalSim.isGameOver();
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
		return (numPlayers > 2) ? isLegalState3Player() : isLegalState2Player();
	}

	private boolean isLegalState2Player()
	{
		int count0 = 0, count1 = 0;			// counti: number of links that player i=0,1 occupies
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++) {
				if (lFrom[i].getPlayer(j) == 1)
					count0++;
				else if (lFrom[i].getPlayer(j) == 2)
					count1++;
			}
		}	
		return checkIfLegal2(count0,count1);
	}
	
	private boolean isLegalState3Player()
	{
		int count0 = 0, count1 = 0, count2 = 0;		// counti: number of links that player i=0,1,2 occupies
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++) {
				if (lFrom[i].getPlayer(j) == 1)
					count0++;
				else if (lFrom[i].getPlayer(j) == 2)
					count1++;
				else if (lFrom[i].getPlayer(j) == 3)
					count2++;
			}
		}
		// now count0,1,2 have the # of links occupied by player P0,1,2
		
		if (finalSim.getLoser()==-1) 
		{						// /WK/ bug fix, this if-condition was missing before. In the case 
								// that one player has already lost, the counts for that player do 
								// not longer necessarily fulfill the conditions below
			return checkIfLegal3(count0, count1, count2);
		} else {
			
			int [] remainingPlayers = getRemainingPlayers();
			int [] remainingCounts = getRemainingCount(count0, count1, count2);
			return checkIfLegal2Variable(remainingPlayers, remainingCounts);
		}
	}
	
	private boolean checkIfLegal2(int count0, int count1)
	{
		if(player == 0 && count0 == count1)
			return true;
		else if(player == 1 && count0 > count1)
			return true;
		else
			return false;
	}
	
	private boolean checkIfLegal3(int count0, int count1, int count2)
	{
		if(player == 0 && count0 == count1 && count0 == count2)
			return true;
		else if(player == 1 && count0 > count1 && count1 == count2)
			return true;
		else if(player == 2 && count0 > count2 && count1 > count2)
			return true;
		else
			return false;
	}
	
	private boolean checkIfLegal2Variable(int [] players, int [] counts)
	{
		if (Math.abs(counts[0]-counts[1])<=1) return true;
		return false;
	}
	
	private int[] getRemainingPlayers()
	{
		int [] players = new int[2];
		switch(finalSim.getLoser())
		{
		case 0:
			players[0] = 1;
			players[1] = 2;
			break;
		case 1:
			players[0] = 0;
			players[1] = 2;
			break;
		case 2:
			players[0] = 0;
			players[1] = 1;
			break;
		}
		return players;
	}
	
	private int[] getRemainingCount(int count0, int count1, int count2)
	{
		int [] counts = new int[2];
		switch(finalSim.getLoser())
		{
		case 0:
			counts[0] = count1;			// /WK/ bug fix (was count0 before)
			counts[1] = count2;
			break;
		case 1:
			counts[0] = count0;
			counts[1] = count2;
			break;
		case 2:
			counts[0] = count0;
			counts[1] = count1;
			break;		
		}
		return counts;
	}
	
	
	@Override
	public double getMinGameScore() {
		return -1;
	}

	@Override
	public double getMaxGameScore() {
		return +1;
	}

	@Override
	public String getName() {
		return "Sim";
	}

	@Override
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	@Override
	public void setAvailableActions() {
		availableActions.clear();
		int action = 0;
		
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++) {
				if (lFrom[i].getPlayer(j) == 0)					// all empty links are available actions
					availableActions.add(Types.ACTIONS.fromInt(action));
				action++;
			}
		}
	}

	@Override
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

    @Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
		int action = 0;
        ArrayList<ACTIONS> allActions = new ArrayList<>();
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++) {
				allActions.add(Types.ACTIONS.fromInt(action));
				action++;
			}
		}       
        return allActions;
	}
	
	@Override
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}

	@Override
	public void advance(ACTIONS action) {
		super.advanceBase();
		int iAction = action.toInt();
		
		setAction(iAction);
		setAvailableActions();		// IMPORTANT: adjust the available actions (have reduced by one)
		
		finalSim.checkIfPlayerLost();
		
		player = getNextPlayer();	// 2-player games: 0,1,0,1,...;   3-player games: 0,1,2,0,1,...
		super.incrementMoveCounter();		
		lastMoves.add(action.toInt());
//		System.out.println("lastMove: "+action.toInt());
//		System.out.println(this.stringDescr());		// only debug
	}
	
	@Override
	public int getPlayer() {
		return player;
	}

	@Override
	public int getNumPlayers() {
		return numPlayers;
	}
	
	public int[] getAllRewards() {
		return finalSim.getAllRewards();
	}

	@Override
	public double getGameScore(StateObservation referringState) {
		return getGameScore(referringState.getPlayer());									
	}
	
	@Override
	public double getGameScore(int player) {
        if(isGameOver()) {
        	return finalSim.getAllRewards()[player];
	    }
        return 0;
	}
	
	public double getReward(int player, boolean rewardIsGameScore) {
		// currently, getReward and getGameScore are the same in Sim.  
		return getGameScore(player);
	}
	
	public int getLastMove() {
		if (lastMoves.size() == 0) return -1;
		return lastMoves.get(lastMoves.size()-1);
	}
	
	public void resetLastMoves() {
		this.lastMoves = new ArrayList<>();
	}
	
	@Override
	public String stringDescr() {
		StringBuilder sout = new StringBuilder();
		String[] str = new String[4];
		str[0] = "_"; str[1]="0"; str[2]="1";str[3]="2" ;
		
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++) {
				sout.append(str[lFrom[i].getPlayer(j)]);
			}
		}
		
 		return sout.toString();
	}

	public String stringDescr2() {
		StringBuilder sout = new StringBuilder();
		String[] str = new String[4];
		str[0] = "-"; str[1]="0"; str[2]="1";str[3]="2" ;
		
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = 0; j < lFrom.length; j++) {
				if (j <= lFrom[i].getNode()) {
					sout.append(" ");
				} else {
					sout.append(str[lFrom[i].getPlayer(j)]);
				}
			}
			sout.append("\n");
		}
 		return sout.toString();
	}

	void setAction(int action)
	{
		int k = 0;
		
		for(int i = 0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++,k++) {
				if(k == action)	{
					lFrom[i].setPlayer(j, player + 1);
					setLastNodes(i,j);
					return;
				}
			}
		}
	}

	public boolean isLegalAction(ACTIONS act)
	{
		int iAction = act.toInt();
		
		for(int i=0, k=0; i < lFrom.length -1 ; i++) {
			for(int j = lFrom[i].getNode()+1; j < lFrom.length; j++,k++) {
				if(k == iAction && lFrom[i].getPlayer(j) == 0)
					return true;
			}
		}
		
		return false;
	}
	
	public int inputToActionInt(int n1, int n2)
	{
		if (n1>n2) return inputToActionInt(n2,n1);
		
		for(int i=0, k=0; i < lFrom.length -1 ; i++) {
			for(int j = i+1; j < lFrom.length; j++,k++) {
				if(n1 == i && n2 == j)
					return k;
			}
		}
		
		throw new RuntimeException("No action fits to n1="+n1+", n2="+n2+" !");
//		return -1;
	}

	public int getLinkFromTo(int i, int j) {
		if (i>j) return lFrom[j].getPlayer(i);
		return lFrom[i].getPlayer(j);
	}
	
	public int getNumNodes()
	{
		return numNodes;
	}
	
	private int getNextPlayer()
	{
		int nextPlayer = (player+1)%numPlayers;
		
		if(nextPlayer == finalSim.getLoser())		// if nextPlayer has already lost, pass to the next one once more
			nextPlayer = (nextPlayer+1)%numPlayers;
		
		return nextPlayer;
	}
	
	private void setLastNodes(int x, int y)
	{
			lastNodes[0] = x;
			lastNodes[1] = y;
	}

	/**
	 *  This class holds information about the final outcome of a Sim episode: is the game over or not, who is winner, 
	 *  who is loser, what are the rewards for all players at end-of-game. 
	 *  
	 *  It uses from the surrounding class: numPlayers, player, hasLost(player)
	 *
	 */
	class FinalSim implements Serializable {
		private int winner;				// starts with -2; gets -1 on draw, otherwise i=0,1,2 on game over where i is the winning player
		private int loser;				// starts with -1; gets i=0,1,2, if player i has just lost (note that game 
										// needs not to be over in the 3-player case!)
		int[] allRewards;		// allRewards[i] is the reward for player i in case isGameOver()
		
		public FinalSim(int numberOfPlayer) {
			this.winner = -2;
			this.loser = -1;
			this.allRewards = new int[numberOfPlayer]; 		// initialized with 0 - the tie case reward
		}
		public FinalSim(FinalSim other) {
			this.winner = other.winner;
			this.loser = other.loser;
			this.allRewards = other.allRewards.clone();			
		}
		public boolean isGameOver() {
			return (winner != -2);
		}
		public void checkIfPlayerLost() {
			if(numPlayers > 2)
		    	checkIfPlayerLost3Player();
			else
				checkIfPlayerLost2Player();			
		}
		private void checkIfPlayerLost2Player()
		{
			if(hasLost(player))
			{
				winner = getNextPlayer();
				allRewards[player] = -1;
				allRewards[winner] = +1;
			}
			else if(isFull())
				winner = -1;	// it's a draw	--> allRewards = {0,0}
		}
		
		private void checkIfPlayerLost3Player()
		{
			switch (ConfigSim.COALITION) {
			case "None":
				checkIfPlayerLost3PlayerNoCoalition();
				break;
			case "1-2":
				checkIfPlayerLost3PlayerCoalition12();
				break;
			default:
				throw new RuntimeException("Unknown case in ConfigSim.COAlITION");
			}
		}
		private void checkIfPlayerLost3PlayerNoCoalition() 
		{
			if(hasLost(player))
			{ 
				allRewards[player] = -1;
				if(loser == -1)			// no one has lost so far
				{
					if(isFull())
					{
						loser = player;
						winner = -1;	// it's a tie between the two remaining players
					}
					else
					{
						loser = player;
					}
				}
				else	// if there is already a loser AND the current player has lost, then the next player is the winner
				{
					winner = getNextPlayer();
					allRewards[winner] = +1;
					
				}
			}
			else if(isFull())
				winner = -1;			// it's a draw  --> allRewards = {0,0,0}
				
		}
		private void checkIfPlayerLost3PlayerCoalition12() 
		{
			if(hasLost(player))
			{ 
				int[] winAlone     = {+1,-1,-1};
				int[] winCoalition = {-1,+1,+1};
				switch (player) {
				case 0:
					allRewards = winCoalition;
					winner = 0;			// signal that game is over
					break;
				case 1:
				case 2:
					allRewards = winAlone;
					winner = 1; 		// signal that game is over
					break;
				default:
					throw new RuntimeException("Unknown case in checkIfPlayerLost3PlayerCoalition12");
				}
			}
			else if(isFull())
				winner = -1;	// it's a draw --> allRewards = {0,0,0}
		}
		
		public int getWinner() {
			return winner;
		}
		public int getLoser() {
			return loser;
		}
		public int[] getAllRewards() {
			return allRewards;
		}
	}
	
	public static void main(String[] args) {
		// /WK/ just some validation code: 
		// We play R random Sim games. 
		// * assert that, once a game is over, getGameScore[k] (the logic based on winner & loser)
		//	 and allRewards[k] (the other logic) match in every case
		// * print the final reward vector for each game and check manually that, if R is large enough, 
		// 	 eventually all possible reward vectors occur:
		//		*  0/ 0/ 0
		//		* -1/-1/ 1 (and permutations)
		//		* -1/ 0/ 0 (and permutations)
		//
		int R = 20;
		Arena ar = new ArenaSim("",true);
		PlayAgent p = new RandomAgent("");
		
		for (int i=0; i<R; i++) {
			StateObserverSim sob = (StateObserverSim) ar.getGameBoard().getDefaultStartState();
			while (!sob.isGameOver()) 
				sob.advance(p.getNextAction2(sob.partialState(), true, true));
			
			for (int k=0; k<sob.getNumPlayers(); k++) {
//				System.out.println("i="+i+",k="+k+":"+(int)sob.getGameScore(k)+"/"+sob.getAllRewards()[k]);
				assert ((int)sob.getGameScore(k) == sob.getAllRewards()[k]) : "Oops";
			}
			System.out.print("i="+i+", allRewards=");	// print reward vector
			for (int k=0; k<sob.getNumPlayers(); k++) System.out.print(sob.getAllRewards()[k]+"/");
			System.out.println();
		}
		
		ar.destroy();
	}
}
