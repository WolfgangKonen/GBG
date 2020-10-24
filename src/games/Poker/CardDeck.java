package src.games.Poker;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Handling of a card deck for games with 52 cards.
 * @version 1.0
 * @author Tim Zeh
 */
public class CardDeck {
    private final ArrayList<PlayingCard> cards;

    /**
     * Create a new randomized deck with 52 cards
     */
    public CardDeck(){
        cards = new ArrayList<>();

        for (short suit=0; suit<=3; suit++) {
            for (short rank=0; rank < 13; rank++) {
                cards.add(new PlayingCard(suit,rank));
            }
        }

        Collections.shuffle(cards);
    }

    /**
     * Create a deck of cards based on an ArrayList of other cards
     * @param other_cards cards that should make up the deck
     */
    public CardDeck(ArrayList<PlayingCard> other_cards){
        cards = new ArrayList<>();
        for(PlayingCard card:other_cards)
            this.cards.add(card.copy());
    }

    /**
     * Create a deck of cards based on another card deck (basically copies the deck)
     * @param other_card_deck card deck to copy
     */
    public CardDeck(CardDeck other_card_deck){
        cards = new ArrayList<>();
        for(PlayingCard card:other_card_deck.cards)
            this.cards.add(card.copy());
    }

    /**
     * create a copy of the card deck
     * @return copy of the deck
     */
    public CardDeck copy(){
        return new CardDeck(this);
    }

    /**
     * randomizes the order of cards in the deck
     */
    public void shuffle(){
        Collections.shuffle(cards);
    }

    /**
     * get the first card of the deck of cards
     * @return drawn card
     */
    public PlayingCard draw() {
        return cards.remove( 0 );
    }

    /**
     * get the number of cards left in the deck
     * @return number of cards in the deck
     */
    public int getTotalCards(){return cards.size();}
}
