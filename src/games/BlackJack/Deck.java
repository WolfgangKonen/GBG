package games.BlackJack;

import java.util.ArrayList;
import java.util.Random;

import games.BlackJack.Card.Rank;

public class Deck {

    ArrayList<Card> deck;

    public Deck() { // infinite
        deck = new ArrayList<Card>();
        int count = 0;
        // depricated for (int i = 0; i < 6; i++) { // 6 card decks (common in Blackjack)
            for (int s = 0; s < 4; s++) { // create every permutation
                for (int r = 0; r < 13; r++) {
                    deck.add(new Card(Card.Rank.values()[r], Card.Suit.values()[s]));
                }
            }
       // }
    }

    //should always return 52
    public int size() {
        return deck.size();
    }

    /**
     * draws a card from the infinite deck
     * @return a (randomly) drawn card
     */
    public Card draw() {
        Random r = new Random();
        // returns random card between zero and decksize
        return deck.get(r.ints(0, (size() + 1 - 1)).findFirst().getAsInt());
    }

}