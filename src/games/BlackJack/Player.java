package games.BlackJack;

import java.util.ArrayList;
import static games.BlackJack.BlackJackConfig.START_CHIPS;

/**
 * stores informations about players participating in a game of Blackjack
 */
public class Player {
    ArrayList<Hand> hands = new ArrayList<Hand>();
    Hand activeHand = null;
    private double chips = START_CHIPS;
    private double betThisRound[] = new double[3];
    private boolean splitHand = false;
    private int activeHandIndex = 0;
    private double insurance = 0;
    private boolean surrender = false;
    private double roundPayoff = 0;

    String name;


    public void setChips(double chips) {
        this.chips = chips;
    }

    public Player(String name) {
        this.name = name;
    }

    /**
     * copy-constructer
     * @param other Player
     */
    public Player(Player other) {
        if (other.activeHand != null) {
            this.activeHand = new Hand(other.activeHand);
            int count = 0;
            for (Hand a : other.hands) {
                if (count++ == other.activeHandIndex) {
                    this.hands.add(this.activeHand);
                } else {
                    this.hands.add(new Hand(a));
                }
            }
        }
        this.surrender = other.surrender;
        this.insurance = other.insurance;
        this.name = other.name;
        this.chips = other.chips;
        this.splitHand = other.splitHand;
        this.activeHandIndex = other.activeHandIndex;
        this.betThisRound = other.betThisRound.clone();
        this.roundPayoff = other.roundPayoff;
    }

    /**
     * @return the active hand of the player
     */
    public Hand getActiveHand() {
        return activeHand;
    }

    /**
     * @return all hands of the player
     */
    public ArrayList<Hand> getHands() {
        return hands;
    }

    /**
     * adds card to the players active hand
     * @param c card to add
     */
    public void addCardToActiveHand(Card c) {
        if (activeHand == null) {
            activeHand = new Hand(c);
            hands.add(activeHand);
        } else {
            activeHand.addCard(c);
        }
    }

    /**
     * sets players next hand active
     * sideeffect: activeHandindex will get increased, @code betOnActiveHand() and bet(double amount) uses the activeHandindex
     * @return the new active hand (null if there are no more hands)
     */
    public Hand setNextHandActive() {
        if (hands.size() <= activeHandIndex + 1) {
            return null;
        }
        return activeHand = hands.get(++activeHandIndex);
    }

    /**
     * makes a bet for the active hand
     * @param amount that gets bet
     */
    public void bet(double amount) {
        chips -= amount;
        betThisRound[activeHandIndex] += amount;
    }

    /**
     * clears all hands and bets
     * (used when a new round starts)
     */
    public void clearHand() {
        activeHand = null;
        hands.clear();
        betThisRound = new double[3];
        activeHandIndex = 0;
        insurance = 0;
        splitHand = false;
        surrender = false;
        roundPayoff = 0;
    }

    /**
     * @return the amount that was bet on the active hand
     */
    public double betOnActiveHand() {
        return betThisRound[activeHandIndex];
    }

    /**
     * returns bet amount for a specific hand
     * @param h specific hand
     * @return bet amount
     */
    public double getBetAmountForHand(Hand h) {
        int index = hands.indexOf(h);
        return betThisRound[index];
    }

    public boolean hasHand() {
        return !hands.isEmpty();
    }

    public double getChips() {
        return chips;
    }

    public boolean hasSplitHand() {
        return splitHand;
    }

    /**
     * adds chips to the players chipsstack
     * @param chips
     * @return new amount of chips
     */
    public double collect(double chips) {
        return this.chips += chips;
    }

    /**
     * splits the active hand
     * sideeffect: makes a bet for the new hand
     */
    public void splitHand() {
        splitHand = true;
        hands.add(activeHand.split());
        betThisRound[hands.size() - 1] = betThisRound[activeHandIndex];
        chips -= betThisRound[activeHandIndex];
    }

    /**
     * makes the insurance bet
     * sideeffect: lets the player make a bet in size of the initial bet (10 chips)
     */
    public void insurance() {
        chips -= betThisRound[0];
        insurance = betThisRound[0];
    }

    /**
     * keeps track of the payOff the player got this round
     * @param payOff to add
     * @return the summed payOff so far
     */
    public double addPayOff(double payOff){
        return roundPayoff += payOff;
    }

    /**
     * @return the summed payOff this round
     */
    public double getRoundPayoff(){
        return roundPayoff;
    }

    /**
     * @return amount of chips the player placed for the insurance-bet
     */
    public double insuranceAmount() {
        return insurance;
    }

    /**
     * marks that the player surrendered this round
     */
    public void surrender() {
        surrender = true;
    }


    public boolean hasSurrender() {
        return surrender;
    }

    /**
     * checks if the player is eligible to keep playing
     * @return true if the player is eligible to play, false if the player is not eligible to play
     */
    public boolean hasLost(){
        return chips < 10 && betOnActiveHand() == 0;
    }

    /**
     * @return sum of all bets placed this round
     */
    public double getSumAllBets(){
        return betThisRound[0] + betThisRound[1] + betThisRound[2] + insurance;

    }

    public String toString() {
        String result = name + " | chips: " + chips;
        for (Hand h : hands) {
            result += " | hand : " + h + " = "+ h.getHandValue();
        }
        return result;
    }
}