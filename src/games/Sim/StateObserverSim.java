package games.Sim;

import java.util.ArrayList;

import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Arena;
import games.ObserverBase;
import games.StateObservation;
import games.Othello.ArenaOthello;
import games.TicTacToe.TicTDBase;
import tools.Types.ACTIONS;
import tools.Types.WINNER;
import tools.Types;
import tools.ValidateStateObserver;

public class StateObserverSim extends ObserverBase implements StateObservation {
	//rewards
	private static final double REWARD_NEGATIVE = -1;
	private static final double REWARD_POSITIVE = 1;
	//board
	private int numNodes;
	private int numPlayers;
	private int player;
	private Node [] nodes;
	private ArrayList<Types.ACTIONS> acts = new ArrayList();
	//Serial number
	private static final long serialVersionUID = 12L;
	private int winner;				// starts with -2; gets -1 on draw, otherwise i=0,1,2 on game over where i is the winning player
	private int lastNode1, lastNode2;
	private int looser;				// starts with -1; gets i=0,1,2, if player i has just lost (game may not 
									// be over in the 3-player case!)
	private int[] allRewards;		// currently just as debug info:
									// allRewards[i] is the reward for player i in case isGameOver()
	
	StateObserverSim() 
	{
		config(ConfigSim.NUM_PLAYERS, ConfigSim.GRAPH_SIZE);
	}
	
	StateObserverSim(Node [] nodes, int player, int winner, int looser,int numPlayers, int numNodes)
	{
		setupNodes(numNodes);
		copyNodes(nodes);
		
		this.numNodes = numNodes;
		this.numPlayers = numPlayers;
		this.player = player;
		this.winner = winner;
		this.looser = looser;
		this.allRewards = new int[numPlayers]; 		// initialized with 0 - the tie case reward
		setAvailableActions();
	}
	
	private void config(int numberOfPlayer, int numberOfNodes)
	{
	
		this.numNodes = numberOfNodes;
		this.numPlayers = numberOfPlayer;
		setupNodes(numberOfNodes);
		this.player = 0;
		this.winner = -2;
		this.looser = -1;
		this.allRewards = new int[numPlayers]; 		// initialized with 0 - the tie case reward
		setAvailableActions();
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
	
	@Override
	public StateObserverSim copy() 
	{
		StateObserverSim sos = new StateObserverSim(this.nodes,this.player, this.winner, this.looser, this.numPlayers, this.numNodes);
		sos.m_counter = this.m_counter;
		return sos;
	}

	private boolean hasLost(int player)
	{
		for(int i = 1; i <= nodes.length; i++)
			if(i == lastNode1 || i == lastNode2)
				continue;
			else
			{
				if(nodes[i-1].getLinkPlayer(lastNode1) == player + 1 && nodes[i-1].getLinkPlayer(lastNode2) == player + 1)
					return true;
			}
		
		return false;
	}
	
	
	private boolean isDraw()
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
		
		if (looser==-1) 
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
		switch(looser)
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
		switch(looser)
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
		return acts.size();
	}

	@Override
	public void setAvailableActions() {
		acts.clear();
		int action = 0;
		
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 0)
					acts.add(Types.ACTIONS.fromInt(action));
				action++;
			}
		}
	}

	@Override
	public ACTIONS getAction(int i) {
		return acts.get(i);
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
		return acts;
	}

	private boolean twoPlayerLost()
	{
		int s = 0;
		for (int i=0; i<allRewards.length; i++) s+=allRewards[i];
		return (s==-2) ? true : false;
	}
	
	private void checkIfPlayerLost3Player()
	{
		if(hasLost(player))
		{ 
			String sout = this.stringDescr();
			allRewards[player] = -1;
			//if (twoPlayerLost()) {
				//allRewards[getNextPlayer3Player()]=1;	// getNextPlayer() is winner
			//}
			if(looser == -1)
			{
				if(isDraw())
				{
					allRewards[player] = -1;
					looser = player;
					winner = -1;
				}
				else
				{
					allRewards[player] = -1;
					looser = player;
				}
			}
			else
			{
				winner = getNextPlayer3Player();
				allRewards[winner] = +1;
				
			}
		}
		else if(isDraw())
			winner = -1;
			
	}
	
	private void checkIfPlayerLost2Player()
	{
		if(hasLost(player))
		{
			winner = getNextPlayer2Player();
			allRewards[player] = -1;
			allRewards[winner] = +1;
		}
		else if(isDraw())
			winner = -1;
	}
	
	@Override
	public void advance(ACTIONS action) {
		super.advanceBase();
		int iAction = action.toInt();
		
		if(numPlayers > 2)
			advance3Player(iAction);
		else
    		advance2Player(iAction);
		super.incrementMoveCounter();
		
//		System.out.println(this.stringDescr());		// only debug
	}
	
	private void advance2Player(int action)
	{
		setAction(action);
		setAvailableActions();
		
		checkIfPlayerLost2Player();
		
		player = getNextPlayer2Player();
	}
	
	private void advance3Player(int action)
	{
		setAction(action);
    	setAvailableActions(); 		// IMPORTANT: adjust the available actions (have reduced by one)
    	
    	checkIfPlayerLost3Player();
    	
    	//setLastPlayer(player);
		player = getNextPlayer3Player();    // 2-player games: 1,-1,1,-1,...
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
			if(looser == player)
				return REWARD_NEGATIVE;
			else if(winner == -1)
	        	return 0;
	        else if(player == winner)
	        	return REWARD_POSITIVE;
	        else
	        	return REWARD_NEGATIVE;
	    }
        return 0;
	}

	public double getReward(int player, boolean rewardIsGameScore) {
		// currently, getReward and getGameScore are the same in Sim.  
		return getGameScore(player);
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
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(k == action)
				{
					nodes[i].setPlayerPos(j, player + 1);
					nodes[nodes[i].getLinkNodePos(j)-1].setPlayerNode(i+1, player + 1);
					setLastNodes(nodes[i].getNumber(), nodes[nodes[i].getLinkNodePos(j)-1].getNumber());
					return;
				}
				k++;
			}
		}
	}

	private boolean isLegal(int action)
	{
		int k = 0;
		
		for(int i = 0; i < nodes.length -1 ; i++)
			for(int j = 0; j < nodes.length - 1 - i; j++)
				if(k == action && nodes[i].getLinkPlayerPos(j) == 0)
					return true;
				else
					k++;
		
		return false;
	}
	
	public boolean isLegalAction(ACTIONS act)
	{
		int iAction = act.toInt();
		return isLegal(iAction); 
	}
	
	public int inputToAction(String text1, String text2)
	{
		int n1 = Integer.parseInt(text1);
		int n2 = Integer.parseInt(text2);
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

	public void setNodes(Node [] nodes) {
		this.nodes = nodes;
	}
	
	public void setNodesCopy(Node [] nodes) 
	{
		for(int i = 0; i < nodes.length; i++)
			this.nodes[i].setLinksCopy(nodes[i].getLinks());
	}
	
	
	public int getNodesLength()
	{
		return nodes.length;
	}
	
	public void setState(StateObserverSim som)
	{
		copyNodes(som.getNodes());
		this.player = som.getPlayer();
		setAvailableActions();
		this.m_counter = som.getMoveCounter();
	}
	
	public int getPreviousPlayer()
	{
		if(player == 0)
			return 2;
		else
			return player - 1;
	}
	
	private int getNextPlayer3Player()
	{
//		int nextPlayer = nextPlayer();
		int nextPlayer = (player+1)%3;
		
		if(nextPlayer == looser)		// if nextPlayer has already lost, pass to the next one once more
		{
//			nextPlayer++;
//			if(nextPlayer > 2)
//				nextPlayer = 0;
			nextPlayer = (nextPlayer+1)%3;
		}
		
		return nextPlayer;
	}
	
	private int getNextPlayer2Player()
	{
		return (player == 0) ? 1 : 0;
	}
	
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
			lastNode1 = x;
			lastNode2 = y;
	}

	//public int getLastPlayer() {
		//return lastPlayer;
	//}

	//public void setLastPlayer(int lastPlayer) {
		//this.lastPlayer = lastPlayer;
	//}
	
	public int getLooser() {
		return looser;
	}

	public void setLooser(int looser) {
		this.looser = looser;
	}
	
	public static void main(String[] args) {
		// /WK/ just some validation code: 
		// We play R random Sim games. 
		// * assert that, once a game is over, getGameScore[k] (the logic based on winner&looser)
		//	 and allRewards[k] (the other logic) match in every case
		// * print the final reward vector for each game and check manually that, if R is large enough, 
		// 	 eventually all possible reward vectors occur:
		//		*  0/ 0/ 0
		//		* -1/-1/ 1 (and permutations)
		//		* -1/ 0/ 0 (and permutations)
		//
		int R = 20;
		Arena ar = new ArenaSim();
		PlayAgent p = new RandomAgent("");
		
		for (int i=0; i<R; i++) {
			StateObserverSim sob = (StateObserverSim) ar.getGameBoard().getDefaultStartState();
			while (!sob.isGameOver()) 
				sob.advance(p.getNextAction2(sob, true, true));
			
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
