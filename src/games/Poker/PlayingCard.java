package src.games.Poker;

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

    PlayingCard(int suit, int rank)
    {
        this.rank = rank;
        this.suit = suit;
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
}
