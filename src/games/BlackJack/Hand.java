package games.BlackJack;

import java.util.ArrayList;

public class Hand {
    ArrayList<Card> hand = new ArrayList<Card>();
    private boolean isHandFinished = false;

    public Hand(Card c) {
        hand.add(c);
    }

    public Hand(Hand other) {
        this.hand = new ArrayList<>(other.hand);
        this.isHandFinished = other.isHandFinished;
    }

    public void addCard(Card c) {
        hand.add(c);
    }

    public int size() {
        return hand.size();
    }

    public void setHandFinished() {
        isHandFinished = true;
    }

    public ArrayList<Card> getCards() {
        return hand;
    }

    public boolean isHandFinished() {
        return isHandFinished || checkForBlackJack() || getHandValue() > 20;
    }

    public boolean checkForBlackJack() {
        return hand.get(0).rank.getValue() + hand.get(1).rank.getValue() == 21 && hand.size() == 2;
    }

    // returns new Hand
    public Hand split() {
        return new Hand(hand.remove(1));
    }

    public int getHandValue() {
        int result = 0;
        int aces = 0;
        for (Card c : hand) {
            if (c.rank.equals(Card.Rank.ACE)) {
                aces++;
            }
            result += c.rank.getValue();
        }
        for (int i = 0; i < aces; i++) {
            if (result > 21) {
                result -= 10;
            }
        }
        return result;
    }

    public boolean isSoft(){
        int result = 0;
        int aces = 0;
        for (Card c : hand) {
            if (c.rank.equals(Card.Rank.ACE)) {
                aces++;
            }
            result += c.rank.getValue();
        }
        if(aces == 0) {
            return false;
        }
        result -= aces*10;
        return result != getHandValue();
    }

    public boolean isPair(){
        return hand.size() == 2 && hand.get(0).rank.equals(hand.get(1).rank);
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