package games.KuhnPoker;

import controllers.MCTSWrapper.utils.Tuple;
import games.ObsNondetBase;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverKuhnPoker extends ObsNondetBase implements StateObsNondeterministic {

	public static final int NUM_PLAYER = 2;
	private static final int START_CHIPS = 10;
	private static final int BIGBLIND = 1;

	/**
	 * If {@code PLAY_ONE_ROUND==true}, the game is over after just one round. The <b>delta in chips</b> is reported
	 * as game score and game reward.
	 * <p>
	 * If {@code PLAY_ONE_ROUND==false}, the game proceeds over many rounds until one player has won all chips.
	 * The <b>current chips</b> are reported as game score and game reward.
	 */
	public static boolean PLAY_ONE_ROUND_ONLY=true;

    private int m_Player;			// player who makes the next move
	protected ArrayList<ACTIONS> availableActions = new ArrayList<>();	// holds all available actions
	/**
	 * the actions (cards) that are possible for the {@link #holeCards} being null
	 */
	private ArrayList<ACTIONS> availableRandomActions;
		// availableRandomActions is modified in initCardDeck, in copy constructor, in dealCard,
		// and in partialState. It contains the cards available for dealCard.
	public ACTIONS lastAction;

	/**
	 * holeCard[i][j] is the j-th card that player i holds. In KuhnPoker, each player has exactly one card, so
	 * j=0 is the only possible choice
	 */
	private PlayingCard[][] holeCards; //[player][card]

	private ArrayList<Integer> cards;

	// folded players are those folded in a game round
	private final boolean[] foldedPlayers;

	int start_player;

	private final Boolean[] checkedPlayer;

	private final double[] gamescores;
	private double[] chips;
	private final double[] rewards;		// currently never used /WK/09/21

	private boolean isNextActionDeterministic;
	private ACTIONS nextNondeterministicAction;

	protected ArrayList<String> lastActions;

	private boolean GAMEOVER;
	boolean isPartialState;

	private Pots pots;
	private Random rand;

	private final boolean newRound;

	private ArrayList<Integer> lastRoundMoves;

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .gamelog containing this object will become
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	private int gameround;

	//</editor-fold>

	//<editor-fold desc="constructor">

	public StateObserverKuhnPoker() {
		start_player = 1;
		m_Player = 1;
		isPartialState = false;

		gameround = 0;
		newRound = true;
		lastActions = new ArrayList<>();
		//rand = new Random(System.currentTimeMillis());
		rand = new Random(ThreadLocalRandom.current().nextInt());
		GAMEOVER = false;
		setRoundOver(false);
		availableRandomActions = new ArrayList<>();
		// information about the player:
		chips =  new double[NUM_PLAYER];
		gamescores = new double[NUM_PLAYER];
		rewards = new double[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			chips[i] = START_CHIPS;
			gamescores[i] = START_CHIPS;
		}

		foldedPlayers = new boolean[2];

		checkedPlayer = new Boolean[2];

		initRound();
		isNextActionDeterministic = true;
	}

	public StateObserverKuhnPoker(StateObserverKuhnPoker other)	{
		super(other);

		start_player = other.start_player;

		newRound = other.newRound;
		gameround = other.gameround;
		isPartialState = other.isPartialState;
		//rand = new Random(System.currentTimeMillis());
		rand = new Random(ThreadLocalRandom.current().nextInt());

		isNextActionDeterministic = other.isNextActionDeterministic;
		nextNondeterministicAction = other.nextNondeterministicAction;
		lastActions = new ArrayList<>();
		for(String entry:other.lastActions){
			addToLog(entry);
		}
		if(other.lastAction!=null)
			lastAction = ACTIONS.fromInt(other.lastAction.toInt());
		GAMEOVER = other.GAMEOVER;

		m_Player = other.m_Player;

		chips =  new double[NUM_PLAYER];
		holeCards = new PlayingCard[NUM_PLAYER][2];

		this.pots = new Pots(other.pots);

		availableRandomActions = new ArrayList<>();
		cards = new ArrayList<>();
		for(int card:other.cards){
			cards.add(card);
			availableRandomActions.add(ACTIONS.fromInt(card));
		}

		chips =  new double[NUM_PLAYER];
		gamescores = new double[NUM_PLAYER];
		rewards = new double[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			if(other.holeCards[i][0]!=null) {
				this.holeCards[i][0] = other.holeCards[i][0].copy();
			}
			this.chips[i] = other.chips[i];
			this.gamescores[i] = other.gamescores[i];
			this.rewards[i] = other.rewards[i];
		}

		checkedPlayer = new Boolean[2];
		foldedPlayers = new boolean[2];
		checkedPlayer[0] = other.checkedPlayer[0];
		checkedPlayer[1] = other.checkedPlayer[1];
		foldedPlayers[0] = other.foldedPlayers[0];
		foldedPlayers[1] = other.foldedPlayers[1];

		this.lastRoundMoves = (ArrayList<Integer>) other.lastRoundMoves.clone();

		setAvailableActions();
	}
	//</editor-fold>

	//<editor-fold desc="helper">

	private void initCardDeck(){
		cards = new ArrayList<>();
		availableRandomActions = new ArrayList<>();

		for(int i=10;i<13;i++){
			cards.add(i);
			availableRandomActions.add(ACTIONS.fromInt(i));
		}
	}

	private int dealCard(){
		int randomCard = cards.remove(ThreadLocalRandom.current().nextInt(cards.size()));
		availableRandomActions.remove(ACTIONS.fromInt(randomCard));
		return randomCard;
	}

	public int dealCard(int id) {

		// remove from cards and available random actions
		cards.remove(Integer.valueOf(id));
		availableRandomActions.remove(ACTIONS.fromInt(id));
		return id;
	}

	private void resetLastRoundMoves(){
		lastRoundMoves = new ArrayList<>();
	}

	public void initRound(){
		gameround++;
		addToLog("------------------New Round ("+gameround+")------------------");
		resetLastRoundMoves();
		if(!GAMEOVER) {
			initCardDeck();
			setRoundOver(false);

			// cards
			holeCards = new PlayingCard[NUM_PLAYER][1];

			pots = new Pots();

			//next player becomes the active one
			start_player = (start_player + 1) % 2;
			m_Player = start_player;

			//Add Ante
			pots.add(1, 0);
			pots.add(1, 1);

			chips[0] -= 1;
			chips[1] -= 1;

			//Deal Card
			holeCards[0][0] = new PlayingCard(dealCard());
			holeCards[1][0] = new PlayingCard(dealCard());

			checkedPlayer[0] = null;
			checkedPlayer[1] = null;

			foldedPlayers[0] = false;
			foldedPlayers[1] = false;

			lastAction = null;

			setAvailableActions();
		}
	}

	/**
	 * function to reset the "log" (text version of what happened in GUI) of the game.
	 */
	public void resetLog(){
		lastActions = new ArrayList<>();
	}

	/**
	 * function to add entry to log (text version of what happened in GUI)
	 * */
	public void addToLog(String log){
		lastActions.add(log);
	}

	//</editor-fold>

	//<editor-fold desc="advance">

	/**
	 * Advance the current state with 'action' to a new state combining the deterministic and the non-deterministic
	 * actions in one method.
	 * Actions:<ul>
	 * <li> 0 -> FOLD	: give up the current round
	 * <li> 1 -> CHECK	: pass for the current action (wait for someone else to do something to react on)
	 * <li> 2 -> BET	: bet the "Big Blind"
	 * <li> 3 -> CALL	: bet the same amount as the previous player bet
	 * </ul>
	 * NOTE: If the deterministic advance results in a next-adction-deterministic state (as it can happen in Poker),
	 * then the non-deterministic part is skipped.
	 * @param action to advance the state of the game
	 */
	public void advance(ACTIONS action){
		if(isNextActionDeterministic()){
			advanceDeterministic(action);
		}
		if(!isNextActionDeterministic()){
			advanceNondeterministic();
		}
	}

	/**
	 * Advance the current state with 'action' to a new state
	 * Actions:<ul>
	 * <li> 0 -> FOLD	: give up the current round
	 * <li> 1 -> CHECK	: pass for the current action (wait for someone else to do something to react on)
	 * <li> 2 -> BET	: bet the "Big Blind"
	 * <li> 3 -> CALL	: bet the same amount as the previous player bet
	 * </ul>
	 * @param action to advance the state of the game
	 */
	public void advanceDeterministic(ACTIONS action) {
		super.advanceBase(action);		//		includes addToLastMoves(action)
		lastRoundMoves.add(action.toInt());
		if(isRoundOver())
			return;

		if(!isNextActionDeterministic) {
			throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
		}

		incrementMoveCounter();
		lastAction = action;
		int iAction = action.toInt();

		switch (iAction) {
			case 0 -> fold();
			case 1 -> check();
			case 2 -> bet();
			case 3 -> call();
		}

		if(foldedPlayers[(m_Player)] ||
				(checkedPlayer[0]!=null&&checkedPlayer[1]!=null&&checkedPlayer[0].equals(checkedPlayer[1]))
				//|| pots.getOpenPlayer(0) == 0 && pots.getOpenPlayer(1) == 0 && checkedPlayer[0]!=null&&checkedPlayer[1]!=null && checkedPlayer[0] == false && checkedPlayer[1] == false
		) {
			isNextActionDeterministic = false;
		}else{
			isNextActionDeterministic=true;
		}

		m_Player = (m_Player+1)%2;
		setAvailableActions();
	}

	public ACTIONS advanceNondeterministic(ACTIONS randAction) {
		if(isRoundOver())
			return randAction;
		if (isNextActionDeterministic) {
			throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
		}
		setRoundOver(true);
		addToLog("Showdown - finding the winner...");
		boolean[][] claims = pots.getClaims();
		for(int p = 0;p<claims.length;p++) {
			int winner;
			if(foldedPlayers[0]){
				winner = 1;
				addToLog("Player 0 has folded therefore player 1 wins the round.");
			}else{
				if(foldedPlayers[1]){
					winner = 0;
					addToLog("Player 1 has folded therefore player 0 wins the round.");
				}else{
					if(isPartialState){
						rand = new Random(ThreadLocalRandom.current().nextInt());
						if(holeCards[0][0]==null){
							//holeCards[0][0] = new PlayingCard(dealCard());
							holeCards[0][0] = new PlayingCard(dealCard(randAction.toInt()));
						}else{
							//holeCards[1][0] = new PlayingCard(dealCard());
							holeCards[1][0] = new PlayingCard(dealCard(randAction.toInt()));
						}
					}
					winner = holeCards[0][0].getRank()>holeCards[1][0].getRank() ? 0:1;
					if(winner==0){
						addToLog("Player 0 wins the round with "+holeCards[0][0].getLongRank()+" over "+holeCards[1][0].getLongRank()+".");
					}else {
						addToLog("Player 1 wins the round with "+holeCards[1][0].getLongRank()+" over "+holeCards[0][0].getLongRank()+".");
					}
				}
			}
			String str = winner +" has won Pot ("+ p +") with "+ (int)pots.getPotSize(p)  +" chips";
			// if it is not a partial state (i.e. not during rollout) then add the known holecards to the log description:
			if (holeCards[0][0]!=null && holeCards[1][0]!=null)
				str = str+ "   ["+holeCards[0][0].getLongRank()+"-"+holeCards[1][0].getLongRank()+"]";
			addToLog(str);
			chips[winner] += pots.getPotSize(p);
		}

		//update scores
		for(int i=0;i<rewards.length;i++)
			rewards[i] = chips[i] - gamescores[i];
		//System.arraycopy(chips,0,gamescores,0,gamescores.length);
		for(int i=0;i<gamescores.length;i++)
			gamescores[i] = PLAY_ONE_ROUND_ONLY ? chips[i] : chips[i] - START_CHIPS;


		addToLog("-----------------End Round("+gameround+")------------------");
		for(int i = 0 ; i <getNumPlayers() ; i++)
			addToLog("Chips "+Types.GUI_PLAYER_NAME[i]+": "+(int)chips[i] +" ");

		isNextActionDeterministic = true;

		if(chips[0]<1||chips[1]<1)
			GAMEOVER = true;

		if (PLAY_ONE_ROUND_ONLY && isRoundOver())
			GAMEOVER = true;

		return randAction;
	}

	public ACTIONS advanceNondeterministic() {
		ArrayList<ACTIONS> possibleRandoms = getAvailableRandoms();
		ACTIONS act = possibleRandoms.get(ThreadLocalRandom.current().nextInt(possibleRandoms.size()));
		advanceNondeterministic(act);

		return act;
	}

	//</editor-fold>

	//<editor-fold desc="GBG">

	public StateObserverKuhnPoker copy() {
		return new StateObserverKuhnPoker(this);
	}

	@Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
		ArrayList<ACTIONS> allActions = new ArrayList<>();
		allActions.add(ACTIONS.fromInt(0)); // FOLD
		allActions.add(ACTIONS.fromInt(1)); // CHECK
		allActions.add(ACTIONS.fromInt(2)); // BET
		allActions.add(ACTIONS.fromInt(3)); // CALL
		return allActions;
	}

	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}

	/**
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		availableActions.clear();

		// You can only fold if there is a bet in place
		if(pots.getOpenPlayer(m_Player)>0)
			availableActions.add(ACTIONS.fromInt(0)); // FOLD

		// You can only call if somebody has bet before
		if(pots.getOpenPlayer(m_Player)>0&&chips[m_Player]>=pots.getOpenPlayer(m_Player))
			availableActions.add(ACTIONS.fromInt(3)); // CALL

		// You can only check if nobody has bet before
		if(pots.getOpenPlayer(m_Player)==0)
			availableActions.add(ACTIONS.fromInt(1)); // CHECK

		// You can only bet if nobody has bet before
		if(pots.getOpenPlayer(m_Player)==0&&chips[m_Player]>=BIGBLIND)
			availableActions.add(ACTIONS.fromInt(2)); // BET

	}

	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Return the afterstate preceding {@code this}.
	 */
	@SuppressWarnings("unused")
	public StateObservation getPrecedingAfterstate() {
		return this;
	}

	public ArrayList<ACTIONS> getAvailableRandoms() {
		return availableRandomActions;
			// availableRandomActions is modified in initCardDeck, in copy constructor, in dealCard,
			// and in partialState. It contains the cards available for dealCard.
	}

	/**
	 * For KuhnPoker the possible completions are the available random actions (the cards that can be dealt)
	 */
	public ArrayList<ACTIONS> getAvailableCompletions() {
		return getAvailableRandoms();
	}


	public int getNumAvailableRandoms() {
		return cards.size();
	}

	public double getProbability(ACTIONS action) {
		// each random action (card dealt) has the same probability
		return 1.0/getNumAvailableRandoms();
	}

	public double getProbCompletion(ACTIONS action) {
		// each random action (card dealt) has the same probability
		return 1.0/getNumAvailableRandoms();
	}

	/**
	 *
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win() {
		return GAMEOVER;
	}

	/**
	 * @return 	returns the sum of rewards in state so, seen from the perspective of the player to move in state refer. {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		return gamescores[refer.getPlayer()];
	}

	@Override
	public double getGameScore(int player){
		return gamescores[player];
	}

	@Override
	public double getReward(StateObservation referringState, boolean rewardIsGameScore){
		if(rewardIsGameScore)
			return gamescores[referringState.getPlayer()];
		return gamescores[referringState.getPlayer()];
		//return rewards[referringState.getPlayer()];
	}

	@Override
	public double getReward(int player, boolean rewardIsGameScore){
		if(rewardIsGameScore)
			return gamescores[player];
		return gamescores[player];
		//return rewards[player];
	}

	public double getMinGameScore() {
		return PLAY_ONE_ROUND_ONLY ? -2 : 0;
	}

	public double getMaxGameScore() {
		return PLAY_ONE_ROUND_ONLY ? +2 : START_CHIPS*NUM_PLAYER;
	}

	@Override
	public boolean isGameOver() {
		return GAMEOVER;
	}

	@Override
	public boolean isDeterministicGame() {
		return false;
	}

	@Override
	public boolean isFinalRewardGame() {
		return false;
	}

	@Override
	public boolean isLegalState() {
		return true;
	}

	public ArrayList<Integer> getLastMoves() {
		return lastMoves;
	}

	@SuppressWarnings("unused")
	public boolean isLegalAction(ACTIONS act) {
		return true;
	}


	@Deprecated
	public String toString() {
		return stringDescr();
	}

	@Override
	public String stringDescr() {
		// IMPORTANT FOR MCTSE!
		String desc = "";
		desc += "Player 0:\r\nScore: "+getGameScore(0)+" Holecard: ";
		if(getHoleCards(0)[0]==null)
			desc +="???";
		else
			desc += getHoleCards(0)[0].toString();

		desc+="\r\n";
		desc += "Player 1: \r\nScore: "+getGameScore(1)+" Holecard: ";
		if(getHoleCards(1)[0]==null)
			desc +="???";
		else
			desc += getHoleCards(1)[0].toString();
		desc+="\r\nmoves:";
		for (int move:getLastRoundMoves()
			 ) {
			desc+=move+"-";
		}
		return desc;
	}

	public String getName() { return "Poker";	}

	public boolean isNextActionDeterministic() {
		return isNextActionDeterministic;
	}

//	public void setNextActionDeterministic(boolean b) {
//		isNextActionDeterministic = b;
//	}

	public ACTIONS getNextNondeterministicAction() {
		setNextNondeterministicAction();
		return nextNondeterministicAction;
	}

	private void setNextNondeterministicAction() {
		if(isNextActionDeterministic) {
			throw new RuntimeException("next Action is Deterministic");
		} else if(nextNondeterministicAction != null) {
			return;
		}
		System.out.print("setNextNondeterministicAction");
	}

	//</editor-fold>

	//<editor-fold desc="actions">
	public void fold(){
		foldedPlayers[m_Player] = true;
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'fold'");
		checkedPlayer[m_Player]=false;
	}

	public void check(){
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'check'");
		checkedPlayer[m_Player]=true;
	}

	public void bet(){
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'bet' ("+BIGBLIND+")");
		betSub(BIGBLIND);
		checkedPlayer[m_Player]=false;
	}

	public void call(){
		double toCall = pots.getOpenPlayer(m_Player);

		// shouldn't happen - action is only available if player has more chips.
		if(toCall > chips[m_Player]){
			//PokerLog.gameLog.severe("Player calls with not enough chips.");
			toCall = chips[m_Player];
		}
		pots.add(toCall,m_Player);
		chips[m_Player] -= toCall;

		if(chips[m_Player]==0) {
			addToLog(Types.GUI_PLAYER_NAME[m_Player]+": call ("+(int)toCall+") - ALL IN");
		}else{
			addToLog(Types.GUI_PLAYER_NAME[m_Player]+": call ("+(int)toCall+")");
		}
		checkedPlayer[m_Player]=false;
	}

	public void betSub(double sizeOfBet){

		pots.add(sizeOfBet, m_Player);
		chips[m_Player] -= sizeOfBet;

	}
	//</editor-fold>

	public double getOpenPlayer(int player){
		return pots.getOpenPlayer(player);
	}

	public PlayingCard[] getHoleCards(int player){
		if(holeCards!=null)
			return holeCards[player];
		return null;
	}

	public PlayingCard[] getHoleCards(){
		return getHoleCards(m_Player);
	}

	public String getLastAction(){
		if(lastActions.size()>0)
			return lastActions.get(lastActions.size()-1)+"\r\n";
		else
			return "";
	}

	public ArrayList<String> getLastActions(){
		return lastActions;
	}

	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	/**
	 * @return the id of the active player.
	 */
	public int getPlayer() {
		return m_Player;
	}

	public int getNumPlayers() {
		return NUM_PLAYER;
	}

	public int getBigblind(){return BIGBLIND;}

	public double[] getChips(){
		return chips;
	}

	public int getGameround(){
		return gameround;
	}

	public int getPotSize(){
		return pots.getSize();
	}
	//</editor-fold>

	public void setHoleCards(int player, PlayingCard first){
		holeCards[player][0] = first;
	}


	@Override
	public StateObserverKuhnPoker partialState(){
		StateObserverKuhnPoker partialState = this.copy();
		int player = partialState.getPlayer();
		partialState.setPartialState(true);
		//setRoundOver(false);
		for (int i = 0; i < partialState.holeCards.length; i++) {
			if(i==player)
				continue;
			if(partialState.holeCards[i][0]!=null) {

				partialState.cards.add(partialState.holeCards[i][0].getId());
				partialState.availableRandomActions.add(ACTIONS.fromInt(partialState.holeCards[i][0].getId()));
					// availableRandomActions contains already the stock card, now it gets additionally the holeCard
					// which is set to null in next line --> there are two available actions

				partialState.holeCards[i][0] = null;
			}
		}
		return partialState;
	}

	/**
	 * Complete a partial state by randomly 'filling the holes' (for all players)
	 *
	 * @return 	a {@link Tuple} where {@code element1} carries the randomly completed state and {@code element2} has
	 * 			the probability that this random completion occurs.
	 */
	@Override
	public Tuple<StateObservation,Double> completePartialState() {
		ArrayList<ACTIONS> rans = getAvailableRandoms();
		int ransSize = rans.size();  // (!) backup the size here, because dealCard below will *decrease* rans.size()!
		if (isPartialState) {
			ACTIONS randAction = rans.get(ThreadLocalRandom.current().nextInt(rans.size()));
			if(holeCards[0][0]==null){
				holeCards[0][0] = new PlayingCard(dealCard(randAction.toInt()));
			}
			if(holeCards[1][0]==null){
				holeCards[1][0] = new PlayingCard(dealCard(randAction.toInt()));
			}
			setPartialState(false);
		}
		return new Tuple<>(this,1.0/ransSize);
	}

	/**
	 * Complete a partial state by randomly 'filling the holes' (for player p)
	 *
	 * @param p		the player for which the state is completed
	 * @return 	a {@link Tuple} where {@code element1} carries the randomly completed state and {@code element2} has
	 * 			the probability that this random completion occurs.
	 */
	@Override
	public Tuple<StateObservation,Double> completePartialState(int p) {
		ArrayList<ACTIONS> rans = getAvailableRandoms();
		int ransSize = rans.size();  // (!) backup the size here, because dealCard below will *decrease* rans.size()!
		if (isPartialState) {
			ACTIONS randAction = rans.get(ThreadLocalRandom.current().nextInt(rans.size()));
			if(holeCards[p][0]==null){
				holeCards[p][0] = new PlayingCard(dealCard(randAction.toInt()));
			}
			if(holeCards[0][0]!=null && holeCards[1][0]!=null)
				setPartialState(false);
		}
		return new Tuple<>(this,1.0/ransSize);
	}

	/**
	 * Complete a partial state by randomly 'filling the holes' (for player p)
	 *
	 * @param p		the player for which the state is completed
	 * @return 	a {@link Tuple} where {@code element1} carries the randomly completed state and {@code element2} has
	 * 			the probability that this random completion occurs.
	 */
	@Override
	public Tuple<StateObservation,Double> completePartialState(int p, ACTIONS ranAct) {
		int ransSize = getAvailableRandoms().size();  // (!) backup the size here, because dealCard below will *decrease* rans.size()!
		if (isPartialState) {
			if(holeCards[p][0]==null){
				holeCards[p][0] = new PlayingCard(dealCard(ranAct.toInt()));
			}
			if(holeCards[0][0]!=null && holeCards[1][0]!=null)
				setPartialState(false);
		}
		return new Tuple<>(this,1.0/ransSize);
	}

	/**
	 * @param p		player
	 * @return Is {@code this} partial with respect to player p?
	 */
	public boolean isPartialState(int p) {
		return (holeCards[p][0]==null);
	}

	/**
	 * @return Is {@code this} partial with respect to any player?
	 */
	@Override
	public boolean isPartialState(){ return isPartialState; }

	@Override
	public void setPartialState(boolean pstate) {
		isPartialState = pstate;
	}

	public int getMoveCounter() {
		return m_counter;
	}

	public Types.ACTIONS[] getStoredActions(){
		return storedActions;
	}


	@Override
	public boolean isRoundBasedGame(){
		return true;
	}


	public ArrayList<Integer> getLastRoundMoves() {
		return lastRoundMoves;
	}
}
