package games.BlackJack;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a hand in Black Jack
 */
public class Hand {
    ArrayList<Card> hand = new ArrayList<Card>();
    private boolean isHandFinished = false;

    /**
     * a hand needs to have at least one card
     * @param c Card
     */
    public Hand(Card c) {
        hand.add(c);
    }

    /**
     * copy-constructor
     * @param other Hand
     */
    public Hand(Hand other) {
        this.hand = new ArrayList<>(other.hand);
        this.isHandFinished = other.isHandFinished;
    }

    /**
     * adds a card to this hand
     * @param c card to add
     */
    public void addCard(Card c) {
        hand.add(c);
    }

    /**
     * sorts hand (used to avoid symmetries for XNTupleFuncs)
     */
    public void sortHand(){
        Collections.sort(hand);
    }

    public int size() {
        return hand.size();
    }

    /**
     * sets the hand finished (used if a player decides to STAND)
     */
    public void setHandFinished() {
        isHandFinished = true;
    }

    /**
     * @return ArrayList of cards this hand contains
     */
    public ArrayList<Card> getCards() {
        return hand;
    }

    /**
     * checks if hand is finished
     * @return true if finished, false if not finished
     */
    public boolean isHandFinished() {
        return isHandFinished || getHandValue() > 20;
    }

    /**
     * checks is hand is a Natural-Blackjack
     * @return true if natural, false if not a natural
     */
    public boolean checkForBlackJack() {
        return getHandValue() == 21 && hand.size() == 2;
    }

    /**
     * splits this hand by removing the second card
     * @return the new hand resulting from the split
     */
    public Hand split() {
        return new Hand(hand.remove(1));
    }

    /**
     * @return handvalue as int
     */
    public int getHandValue() {
        int result = 0;
        boolean ace = false;
        for (Card c : hand) {
            if (c.rank == Card.Rank.ACE) {
                ace = true;
            }
            result += c.rank.getValue();
        }
        if(ace && ((result+10) <= 21))
            result += 10;
        return result;
    }

    /**
     * checks if hand is soft or hard
     * @return true if hand is soft, false if hand is hard
     */
    public boolean isSoft(){
        int result = 0;
        boolean ace = false;
        for (Card c : hand) {
            if (c.rank == Card.Rank.ACE) {
                ace = true;
            }
            result += c.rank.getValue();
        }
        if(!ace)
            return false;
        return ace && (result + 10) < 21;
    }

    /**
     * checks if hand is a pair
     * @return true if pair, false if not a pair
     */
    public boolean isPair(){
        return hand.size() == 2 && hand.get(0).rank == hand.get(1).rank;
    }


    public boolean isBust() {
        return getHandValue() > 21;
    }

    public String toString() {
        String result = "";
        for (Card c : hand) {
            result += c + " ";
        }
        return result;
    }
}