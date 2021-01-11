package games.BlackJack;

import java.util.ArrayList;

public class Dealer {
    ArrayList<Card> hand = new ArrayList<Card>();

    public Dealer(String name) {
        this.name = name;
    }

    ArrayList<Hand> hands = new ArrayList<Hand>();
    Hand activeHand = null;
    String name;

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