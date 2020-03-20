package games.Sim;

import java.util.ArrayList;

import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Arena;
import games.ObserverBase;
import games.StateObservation;
import games.Othello.ArenaOthello;
import games.Othello.XNTupleFuncsOthello;
import games.Sim.Gui.BoardPanel;
import games.TicTacToe.TicTDBase;
import tools.Types.ACTIONS;
import tools.Types.WINNER;
import tools.Types;
import tools.ValidateStateObserver;

/**
 * This class holds any valid Sim game state. It is coded
 * as array {@link Node}{@code [] nodes}, where each node carries links to all nodes with higher
 * index in the array. Each link can be 
 * <ul>
 * <li>= 0 for an empty link,
 * <li>= 1 for a P0 link,
 * <li>= 2 for a P1 link,
 * <li>= 3 for a P2 link (in the 3-player variant),
 * </ul>
 * where Pi refers to player i=0,1[,2]. 
 * <p>
 * P0 starts the game.<p>
 * The available actions are the (empty) links, which are numbered in consecutively: If we have K nodes
 * in total, then node i=0,...,K-1 has K-1-i links (to node i+1,...,K-1). For example, in the case
 * K=6 we have K*(K-1)/2=15 actions which are numbered as: 
 * <pre>
 *      j  0   1   2   3   4   
 *                                 i
 *        00  01  02  03  04       0
 *        05  06  07  08           1
 *        09  10  11               2
 *        12  13                   3
 *        14                       4
 *  </pre>
 *  Note that index j does not refer to node numbers, but j=0 means "node plus 1", j=1 means 
 *  "node plus 2" and so on.
 *  <p>
 *  The {@code lines} in {@link BoardPanel} are numbered exactly the same way as the actions.
 */
public class StateObserverSim extends ObserverBase implements StateObservation {
	//rewards
	private static final double REWARD_NEGATIVE = -1;
	private static final double REWARD_POSITIVE = 1;
	//board
	private int numNodes;
	private int numPlayers;
	private int player;
	private Node [] nodes;
	/**
	 * The list of avaialble actions
	 */
	private ArrayList<Types.ACTIONS> availableActions = new ArrayList();
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
	//Serial number
	private static final long serialVersionUID = 12L;
	private int winner;				// starts with -2; gets -1 on draw, otherwise i=0,1,2 on game over where i is the winning player
	private int loser;				// starts with -1; gets i=0,1,2, if player i has just lost (note that game 
									// needs not to be over in the 3-player case!)
	int[] allRewards;		// allRewards[i] is the reward for player i in case isGameOver()
	
	StateObserverSim() 
	{
		config(ConfigSim.NUM_PLAYERS, ConfigSim.NUM_NODES);
	}
	
	// obsolete now, we have StateObserverSim(StateObserverSim other)
//	StateObserverSim(Node [] nodes, int player, int winner, int loser,int numPlayers, int numNodes)
//	{
//		setupNodes(numNodes);
//		copyNodes(nodes);
//		
//		this.numNodes = numNodes;
//		this.numPlayers = numPlayers;
//		this.player = player;
//		this.winner = winner;
//		this.loser = loser;
//		this.allRewards = new int[numPlayers]; 		// initialized with 0 - the tie case reward
//		if (winner>=0) {
//			for (int i=0; i<allRewards.length; i++) allRewards[i]=-1;	// all others have lost
//			allRewards[winner]=+1;
//		}
//		if (loser>=0) allRewards[loser]=-1;
//		setAvailableActions();
//		lastMoves = new ArrayList<Integer>();
//	}
	
	StateObserverSim(StateObserverSim other)
	{
		super(other);		// copy members m_counter and stored*
		setupNodes(other.numNodes);
		copyNodes(other.nodes);
		
		this.numNodes = other.numNodes;
		this.numPlayers = other.numPlayers;
		this.player = other.player;
		this.winner = other.winner;
		this.loser = other.loser;
		this.allRewards = other.allRewards.clone();
		this.lastMoves = (ArrayList<Integer>) other.lastMoves.clone();
		this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this as replacement for availableActions.clone() would be a bit slower
	}
	
	private void config(int numberOfPlayer, int numberOfNodes)
	{
		setupNodes(numberOfNodes);
		
		this.numNodes = numberOfNodes;
		this.numPlayers = numberOfPlayer;
		this.player = 0;
		this.winner = -2;
		this.loser = -1;
		this.allRewards = new int[numPlayers]; 		// initialized with 0 - the tie case reward
		setAvailableActions();
		this.lastMoves = new ArrayList<Integer>();
	}
	
	@Override
	public StateObserverSim copy() 
	{
		StateObserverSim sos = new StateObserverSim(this);		// includes via 'super(other)' the copying of stored*-members in ObserverBase

		// now obsolete:
//		StateObserverSim sos = new StateObserverSim(this.nodes,this.player, this.winner, this.loser, this.numPlayers, this.numNodes);
//		sos.m_counter = this.m_counter;
//		sos.lastMoves = (ArrayList<Integer>) lastMoves.clone();
//		sos.storedMaxScore = this.storedMaxScore;
//		sos.storedActBest = this.storedActBest;
//		if (this.storedActions!=null) sos.storedActions = this.storedActions.clone();
//		if (this.storedValues!=null) sos.storedValues = this.storedValues.clone();
		return sos;
	}

	private void setupNodes(int size)
	{
		nodes = new Node[size];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new Node(size, i+1);
	}
	
	private void copyNodes(Node[] nodes)
	{
		for(int i = 0; i < nodes.length; i++)
			this.nodes[i].setLinksCopy(nodes[i].getLinks());
	}
	
	public boolean hasLost(int player)
	{
		if (lastNodes[0] != lastNodes[1]) {		// if an action was taken
			for(int i = 1; i <= nodes.length; i++)
				if(i == lastNodes[0] || i == lastNodes[1])
					continue;
				else
				{
					if(nodes[i-1].getLinkPlayer(lastNodes[0]) == player + 1 && 
					   nodes[i-1].getLinkPlayer(lastNodes[1]) == player + 1) {
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
		for(Node node : nodes)
			if(node.hasSpaceLeft())
				return false;
		return true;
	}
	
	@Override
	public boolean isGameOver() {
		
		if(winner != -2)
			return true;
		else
			return false;
	}

	@Override
	public boolean isDeterministicGame() {
		return true;
	}

	@Override
	public boolean isFinalRewardGame() {
		return true;
	}

	private void assertNodeSymmetry() {
		// this code just asserts that the link from node a to node b has always 
		// the same player as the link from node b to node a
		int p1,p2,n1,n2;
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				n1 = nodes[i].getNumber();
				p1 = nodes[i].getLinkPlayerPos(j);
				n2 = nodes[i].getLinkNodePos(j);
				p2 = nodes[n2-1].getLinkPlayer(i+1);
				assert (p1==p2) : "Node symmetry in Sim violated between nodes "+n1+" and "+n2;
			}
		}
		
	}
	@Override
	public boolean isLegalState() {
		assertNodeSymmetry();
		return (numPlayers > 2) ? isLegalState3Player() : isLegalState2Player();
	}

	private boolean isLegalState2Player()
	{
		int count0 = 0, count1 = 0;			// counti: number of links that player i=0,1 occupies
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 1)
					count0++;
				else if(nodes[i].getLinkPlayerPos(j) == 2)
					count1++;
			}
		}
		
		return checkIfLegal2(count0,count1);
	}
	
	private boolean isLegalState3Player()
	{
		int count0 = 0, count1 = 0, count2 = 0;		// counti: number of links that player i=0,1,2 occupies
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 1)
					count0++;
				else if(nodes[i].getLinkPlayerPos(j) == 2)
					count1++;
				else if(nodes[i].getLinkPlayerPos(j) == 3)
					count2++;
			}
		}
		// now count0,1,2 have the # of links occupied by player P0,1,2
		
		if (loser==-1) 
		{						// /WK/ bug fix, this if-condition was missing before. In the case 
								// that one player has already lost, the counts for that player do 
								// not longer necessarily fulfill the conditions below
			return checkIfLegal3(count0, count1, count2);
		} else {
			
			int [] remainingPlayers = getRemainingPlayers();
			int [] remainingCounts = getRemainingCount(count0, count1, count2);
			return checkIfLegal2Variabel(remainingPlayers, remainingCounts);
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
	
	private boolean checkIfLegal2Variabel(int [] players, int [] counts)
	{
		if (Math.abs(counts[0]-counts[1])<=1) return true;
		return false;
		
		// /wK/ this check seems to strong, it leads sometimes (seldom) to a wrongly firing 
		// assertion (because players=[1,2] and counts[1] = counts[2]+1)
//		if(player == players[0] && counts[0] == counts[1])
//			return true;
//		else if(player == players[1] && counts[0] > counts[1])
//			return true;
//		else
//			return false;
	}
	private int[] getRemainingPlayers()
	{
		int [] players = new int[2];
		switch(loser)
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
		switch(loser)
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
	
	
	// obsolete?
//	@Override
//	public WINNER getGameWinner() {
//		assert isGameOver() : "Game is not over yet!";
//		if(winner == 0)
//			return Types.WINNER.PLAYER_WINS;
//		else if(winner == 1)
//			return Types.WINNER.PLAYER_LOSES;
//		else
//			return Types.WINNER.TIE;
//	}
//	
//	@Override
//	public int getGameWinner3player() {
//		assert isGameOver() : "Game is not over yet!";
//		return winner;
//	}  
	
	
	@Override
	public double getMinGameScore() {
		return REWARD_NEGATIVE;
	}

	@Override
	public double getMaxGameScore() {
		return REWARD_POSITIVE;
	}

	@Override
	public String getName() {
		return "Sim";
	}

	@Override
	public StateObservation getPrecedingAfterstate() {
		return this;
	}

	@Override
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	@Override
	public void setAvailableActions() {
		availableActions.clear();
		int action = 0;
		
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++, action++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 0)			// all empty links are available actions
					availableActions.add(Types.ACTIONS.fromInt(action));
//				action++;	// /WK/ moved to for (int j...)
				
				// just debug:
//				int action_fromij = getActionIntFromIJ( i,j);
//				Point pnt = this.getIJfromActionInt(action_fromij);
//				System.out.println("("+i+","+j+"): "+action+" | "+action_fromij + " ("+pnt.getX()+","+pnt.getY()+")");
			}
		}
//		int dummy=1;
	}

	/**
	 * Given i,j, calculate the action index. 
	 * See triangular table in {@link StateObserverSim} for an example.
	 * @param i	the node number \in 0,...,K-1
	 * @param j the link number for this node
	 * @return the action index
	 */
	public int getActionIntFromIJ(int i, int j) {
		int s=0;
		for (int k=1; k<=i; k++) s+=(nodes.length-k);
		return s+j;
	}

	/**
	 * Given the action index, calculate (i,j). 
	 * See triangular table in {@link StateObserverSim} for an example.
	 * @param iAction the action index
	 * @return {@link Point}(i,j)
	 * 
	 * @return
	 */
	public Point getIJfromActionInt(int iAction) {
		int i=0, s=0;
		for (; i<nodes.length-1; i++) {
			if (s+(nodes.length-1-i)>iAction) break;	// out of for
			s += (nodes.length-1-i);
		}
		int j = iAction-s;
		return new Point(i,j);
	}
	
	@Override
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

    @Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
		int action = 0;
        ArrayList allActions = new ArrayList<>();
		for(int i = 0; i < nodes.length -1 ; i++) {
			for(int j = 0; j < nodes.length - 1 - i; j++) {
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

	// never used
//	private boolean twoPlayerLost()
//	{
//		int s = 0;
//		for (int i=0; i<allRewards.length; i++) s+=allRewards[i];
//		return (s==-2) ? true : false;
//	}
	
	private void checkIfPlayerLost2Player()
	{
		if(hasLost(player))
		{
			winner = getNextPlayer();
			allRewards[player] = -1;
			allRewards[winner] = +1;
		}
		else if(isFull())
			winner = -1;	// it's a draw
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
			winner = -1;			// it's a draw
			
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
				break;
			case 1:
			case 2:
				allRewards = winAlone;
				break;
			default:
				throw new RuntimeException("Unknown case in checkIfPlayerLost3PlayerCoalition12");
			}
			winner = -1; 	// signal that game is over
		}
		else if(isFull())
			winner = -1;	// it's a draw
	}
	
	@Override
	public void advance(ACTIONS action) {
		super.advanceBase();
		int iAction = action.toInt();
		
		setAction(iAction);
		setAvailableActions();		// IMPORTANT: adjust the available actions (have reduced by one)
		
		if(numPlayers > 2)
	    	checkIfPlayerLost3Player();
		else
			checkIfPlayerLost2Player();
		
		player = getNextPlayer();	// 2-player games: 0,1,0,1,...;   3-player games: 0,1,2,0,1,...
		super.incrementMoveCounter();		
		lastMoves.add(action.toInt());
//		System.out.println("lastMove: "+action.toInt());
//		System.out.println(this.stringDescr());		// only debug
	}
	
	// obsolete now: 
//	private void advance2Player(int action)
//	{
//		checkIfPlayerLost2Player();
//		
//		player = getNextPlayer();	// 2-player games: 0,1,0,1,...
//	}
//	
//	private void advance3Player(int action)
//	{
//    	checkIfPlayerLost3Player();
//    	
//		player = getNextPlayer();    // 3-player games: 0,1,2,0,1,...
//	}

	@Override
	public int getPlayer() {
		return player;
	}

	@Override
	public int getNumPlayers() {
		return numPlayers;
	}
	
	public int[] getAllRewards() {
		return allRewards;
	}

	@Override
	public double getGameScore(int player) {
		if(numPlayers > 2)
			return getGameScore3Player(player);
		else
			return getGameScore2Player(player);									
	}
	
	@Override
	public double getGameScore(StateObservation referringState) {
        //boolean gameOver = this.isGameOver();
      
//		if(numPlayers > 2)
//			return getGameScore3Player(referringState.getPlayer());
//		else
//			return getGameScore2Player(referringState.getPlayer());
		return getGameScore(referringState.getPlayer());									
	}
	
	private double getGameScore2Player(int player)
	{
        if(isGameOver()) {
    		if(winner == -1)
            	return 0;
            else if(player == winner)
            	return REWARD_POSITIVE;
            else
            	return REWARD_NEGATIVE;
	    }
        return 0;
	}
	
	private double getGameScore3Player(int player)
	{
        if(isGameOver()) {
        	// if these assertion all DO NOT fire, we can replace the code below just by
        	//      return allRewards[player];
//			if(loser == player) {
//				assert (allRewards[player]==REWARD_NEGATIVE);
//				return REWARD_NEGATIVE;
//			} else if(winner == -1) {
//				assert (allRewards[player]==0);
//	        	return 0;
//			} else if(player == winner) {
//				assert (allRewards[player]==REWARD_POSITIVE);
//	        	return REWARD_POSITIVE;
//			} else {
//				assert (allRewards[player]==REWARD_NEGATIVE);
//	        	return REWARD_NEGATIVE;
//			}
        	return allRewards[player];
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
		this.lastMoves = new ArrayList<Integer>();		
	}
	
	@Override
	public String stringDescr() {
		String sout = "";
		String str[] = new String[4]; 
		str[0] = "_"; str[1]="0"; str[2]="1";str[3]="2" ;
		
		for(int i = 0; i < nodes.length -1 ; i++)
			for(int j = 0; j < nodes.length - 1 - i; j++)
				sout = sout + (str[nodes[i].getLinkPlayerPos(j)]);
		
 		return sout;
	}

	void setAction(int action)
	{
		int k = 0;
		
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++, k++)
			{
				if(k == action)
				{
					nodes[i].setPlayerPos(j, player + 1);
					nodes[nodes[i].getLinkNodePos(j)-1].setPlayerNode(i+1, player + 1);
					setLastNodes(nodes[i].getNumber(), nodes[nodes[i].getLinkNodePos(j)-1].getNumber());
					return;
				}
			}
		}
	}

	private boolean isLegal(int iAction)
	{
		int k = 0;
		
		for(int i = 0; i < nodes.length -1 ; i++)
			for(int j = 0; j < nodes.length - 1 - i; j++, k++)
				if(k == iAction && nodes[i].getLinkPlayerPos(j) == 0)
					return true;
		
		return false;
	}
	
	public boolean isLegalAction(ACTIONS act)
	{
		int iAction = act.toInt();
		return isLegal(iAction); 
	}
	
	// obsolete?
//	public int inputToAction(String text1, String text2)
//	{
//		int n1 = Integer.parseInt(text1);
//		int n2 = Integer.parseInt(text2);
//		int i = 0;
//		
//		for(int j = 1; j < nodes.length; j++)
//			for(int k = j + 1; k < nodes.length + 1; k++)
//			{
//				if((n1 == j || n1 == k) && (n2 == j || n2 == k))
//					return i;
//				i++;
//			}
//		
//		return -1;
//	}
	
	public int inputToActionInt(int n1, int n2)
	{
		int i = 0;
		
		for(int j = 1; j < nodes.length; j++)
			for(int k = j + 1; k < nodes.length + 1; k++)
			{
				if((n1 == j || n1 == k) && (n2 == j || n2 == k))
					return i;
				i++;
			}
		
		return -1;
	}

	public Node [] getNodes() {
		return nodes;
	}

	// obsolete
//	public void setNodes(Node [] nodes) {
//		this.nodes = nodes;
//	}
//	
//	public void setNodesCopy(Node [] nodes) 
//	{
//		for(int i = 0; i < nodes.length; i++)
//			this.nodes[i].setLinksCopy(nodes[i].getLinks());
//	}
	
	
	public int getNodesLength()
	{
		return nodes.length;
	}
	
	// obsolete
//	public void setState(StateObserverSim som)
//	{
//		copyNodes(som.getNodes());
//		this.player = som.getPlayer();
//		setAvailableActions();
//		this.m_counter = som.getMoveCounter();
//	}
	
	// obsolete
//	public int getPreviousPlayer()
//	{
//		if(player == 0)
//			return 2;
//		else
//			return player - 1;
//	}
	
	private int getNextPlayer()
	{
		int nextPlayer = (player+1)%numPlayers;
		
		if(nextPlayer == loser)		// if nextPlayer has already lost, pass to the next one once more
			nextPlayer = (nextPlayer+1)%numPlayers;
		
		return nextPlayer;
	}
	
	// --- /WK/ obsolete now, we have unified this in getNextPlayer()
//	private int getNextPlayer3Player()
//	{
////		int nextPlayer = nextPlayer();
//		int nextPlayer = (player+1)%3;
//		
//		if(nextPlayer == loser)		// if nextPlayer has already lost, pass to the next one once more
//		{
////			nextPlayer++;
////			if(nextPlayer > 2)
////				nextPlayer = 0;
//			nextPlayer = (nextPlayer+1)%3;
//		}
//		
//		return nextPlayer;
//	}
//	
//	private int getNextPlayer2Player()
//	{
//		return (player == 0) ? 1 : 0;
//	}
	
	// --- /WK/ obsolete now
//	private int nextPlayer()
//	{
//		if(player == 2)
//			return 0;
//		else
//			return player + 1;
//	}
	
	private void setLastNodes(int x, int y)
	{
			lastNodes[0] = x;
			lastNodes[1] = y;
	}

	//public int getLastPlayer() {
		//return lastPlayer;
	//}

	//public void setLastPlayer(int lastPlayer) {
		//this.lastPlayer = lastPlayer;
	//}
	
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
				sob.advance(p.getNextAction2(sob, true, true));
			
			for (int k=0; k<sob.getNumPlayers(); k++) {
//				System.out.println("i="+i+",k="+k+":"+(int)sob.getGameScore(k)+"/"+sob.getAllRewards()[k]);
				assert ((int)sob.getGameScore(k) == sob.allRewards[k]) : "Oops";
			}
			System.out.print("i="+i+", allRewards=");	// print reward vector
			for (int k=0; k<sob.getNumPlayers(); k++) System.out.print(sob.allRewards[k]+"/");
			System.out.println();
		}
		
		ar.destroy();
	}
}
