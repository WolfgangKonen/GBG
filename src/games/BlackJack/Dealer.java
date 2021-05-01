package games.BlackJack;

import java.util.ArrayList;


/**
 * Wraps an action and a button. This simplifies the assignement of an evaluation value made from an agent for this action.
 */
public class Dealer {


    public Dealer(String name) {
        this.name = name;
    }
    /**
     * Hand of the dealer
     */
    Hand activeHand = null;
    String name;

    /**
     * Copyconstructor
     * @param other dealer
     */
    public Dealer(Dealer other) {
        if (other.activeHand != null) {
            this.activeHand = new Hand(other.activeHand);
        }

        this.name = other.name;
    }

    public Hand getActiveHand() {
        return activeHand;
    }

    public void addCardToActiveHand(Card c) {
        if (activeHand == null) {
            activeHand = new Hand(c);
        } else {
            activeHand.addCard(c);
        }
    }

    public void clearHand() {
        activeHand = null;
    }

    public String toString() {
        return name;
    }

    public boolean hasHand() {
        return activeHand != null;
    }

}