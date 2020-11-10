package src.games.Poker;

import java.util.Arrays;

public class PlayingCard {

    private final int rank;
    private final int suit;
    static String[] suits = { "♥", "♠", "♦", "♣" };
    static String[] ranks = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };

    public static String rankAsString( int rank ) {
        return ranks[rank];
    }

    @SuppressWarnings("unused")
    public static String suitAsString(int suit ) {
        return suits[suit];
    }

    PlayingCard(int suit, int rank){
        this.rank = rank;
        this.suit = suit;
    }

    public PlayingCard(String txt){
        this.suit = Arrays.asList(suits).indexOf(""+txt.charAt(0));
        if(txt.length()<3)
            this.rank = Arrays.asList(ranks).indexOf(""+txt.charAt(1));
        else
            this.rank = Arrays.asList(ranks).indexOf(""+txt.charAt(1)+txt.charAt(2));
    }


    public @Override String toString()
    {
        return "["+ suits[suit] + ranks[rank] + "]";
    }

    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    public PlayingCard copy(){
        return new PlayingCard(this.suit,this.rank);
    }

    public int getId(){
        return (rank+1)+suit*13;
    }
}
