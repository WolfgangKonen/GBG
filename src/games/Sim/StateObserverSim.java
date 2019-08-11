package games.Sim;

import java.util.ArrayList;

import games.ObserverBase;
import games.StateObservation;
import games.TicTacToe.TicTDBase;
import tools.Types.ACTIONS;
import tools.Types.WINNER;
import tools.Types;

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
	private int winner;
	private int lastNode1, lastNode2;
	private int looser;
	
	StateObserverSim() 
	{
		config(2,6);
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

	@Override
	public boolean isLegalState() {
		return (numPlayers > 2) ? isLegalState3Player() : isLegalState2Player();
	}

	private boolean isLegalState2Player()
	{
		int count1 = 0, count2 = 0;
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 1)
					count1++;
				else if(nodes[i].getLinkPlayerPos(j) == 2)
					count2++;
			}
		}
		
		if(player == 0 && count1 == count2)
			return true;
		else if(player == 1 && count1 > count2)
			return true;
		else
			return false;
	}
	
	private boolean isLegalState3Player()
	{
		int count1 = 0, count2 = 0, count3 = 0;
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 1)
					count1++;
				else if(nodes[i].getLinkPlayerPos(j) == 2)
					count2++;
				else if(nodes[i].getLinkPlayerPos(j) == 3)
					count3++;
			}
		}
		
		if(player == 0 && count1 == count2 && count1 == count3)
			return true;
		else if(player == 1 && count1 > count2 && count2 == count3)
			return true;
		else if(player == 2 && count1 > count3 && count2 > count3)
			return true;
		else
			return false;
	}
	
	@Override
	public WINNER getGameWinner() {
		assert isGameOver() : "Game is not over yet!";
		if(winner >= 0)
			return Types.WINNER.PLAYER_WINS;
		else
			return Types.WINNER.TIE;
	}
	
	@Override
	public int getGameWinner3player() {
		assert isGameOver() : "Game is not over yet!";
		return winner;
	}  
	
	
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
				acts.add(Types.ACTIONS.fromInt(action));
				action++;
			}
		}       
        return allActions;
	}
	
	@Override
	public ArrayList<ACTIONS> getAvailableActions() {
		return acts;
	}

	private void checkIfPlayerLost()
	{
		if(hasLost(player))
		{ 
			if(looser == -1)
			{
				if(isDraw())
				{
					looser = player;
					winner = -1;
				}
				else
					looser = player;
			}
			else
				winner = getNextPlayer();
		}
		else if(isDraw())
			winner = -1;
			
	}
	
	private void checkIfPlayerLost2Player()
	{
		if(hasLost(player))
		{
			winner = getNextPlayer2Player();
		}
		else if(isDraw())
			winner = -1;
	}
	
	@Override
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		
		if(numPlayers > 2)
			advance3Player(iAction);
		else
    		advance2Player(iAction);
		super.incrementMoveCounter();
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
    	
    	checkIfPlayerLost();
    	
    	//setLastPlayer(player);
		player = getNextPlayer();    // 2-player games: 1,-1,1,-1,...
	}

	@Override
	public int getPlayer() {
			return player;
	}

	@Override
	public int getNumPlayers() {
		return numPlayers;
	}

	@Override
	public double getGameScore(StateObservation referringState) {
        //boolean gameOver = this.isGameOver();
      
		if(numPlayers > 2)
			return getGameScore3Player(referringState.getPlayer());
		else
			return getGameScore2Player(referringState.getPlayer());
									
	}
	
	private double getGameScore2Player(int player)
	{
		if(winner == -1)
        	return 0;
        else if(player == winner)
        	return REWARD_POSITIVE;
        else
        	return REWARD_NEGATIVE;
	}
	
	private double getGameScore3Player(int player)
	{
		if(looser == player)
			return REWARD_NEGATIVE;
		else if(winner == -1)
        	return 0;
        else if(player == winner)
        	return REWARD_POSITIVE;
        else
        	return REWARD_NEGATIVE;
	}

	@Override
	public String stringDescr() {
		String sout = "";
		String str[] = new String[4]; 
		str[0] = "-"; str[1]="1"; str[2]="2";str[3]="3" ;
		
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
	
	private int getNextPlayer()
	{
		int nextPlayer = nextPlayer();
		
		if(nextPlayer == looser)
		{
			nextPlayer++;
			if(nextPlayer > 2)
				nextPlayer = 0;
		}
		
		return nextPlayer;
	}
	
	private int getNextPlayer2Player()
	{
		return (player == 0) ? 1 : 0;
	}
	
	private int nextPlayer()
	{
		if(player == 2)
			return 0;
		else
			return player + 1;
	}
	
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
}
