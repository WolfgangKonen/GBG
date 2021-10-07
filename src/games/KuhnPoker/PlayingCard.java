package games.KuhnPoker;

import java.io.Serializable;
import java.util.Arrays;

public class PlayingCard implements Serializable {

    private final int rank;
    private final int suit;
    //static String[] suits = { "♥", "♠", "♦", "♣" };
    static String[] pathSuits = { "H", "S", "D", "C" };
    static String[] suits = pathSuits;
    static String[] ranks = { "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A" };
    static String[] longRanks = {"Two","Three", "Four","Five","Six","Seven","Eight","Nine","Ten","Jack","Queen","King","Ace"};
    /**
     * String representation of a rank
     * @return String representation of a rank
     */
    public static String rankAsString( int rank ) {
        return ranks[rank];
    }

    /**
     * String representation of a suit
     * @return String representation of a suit
     */
    @SuppressWarnings("unused")
    public static String suitAsString(int suit ) {
        return suits[suit];
    }

    /**
     * Creates a playing card by ID:
     * ♥2 = 1
     * ♥3 = 2
     * ...
     * ♣A = 52
     * @param id of the card
     */
    PlayingCard(int id){
        int x = id - 1;
        this.suit = (x)/13;
        this.rank = x-suit*13;
    }

    /**
     * Creates a playing card with a rank and a suit
     * suits = { "♥", "♠", "♦", "♣" };
     * ranks = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
     * @param suit of the card
     * @param rank of the card
     */
    PlayingCard(int suit, int rank){
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Creates a playing card from a string in the form "[♥2]" or "♥2"
     * @param txt string to create card from e.g. "[♥2]" or "♥2"
     */
    public PlayingCard(String txt){
        if(txt.length()>3) {
            txt = txt.replace("[", "");
            txt = txt.replace("]", "");
        }
        this.suit = Arrays.asList(suits).indexOf(""+txt.charAt(0));
        if(txt.length()<3)
            this.rank = Arrays.asList(ranks).indexOf(""+txt.charAt(1));
        else
            this.rank = Arrays.asList(ranks).indexOf(""+txt.charAt(1)+txt.charAt(2));
    }

    /**
     * String representation of the card. e.g. "[♥2]"
     * @return String representation of the card. e.g. "[♥2]"
     */
    public @Override String toString()
    {
        return "["+  ranks[rank] + suits[suit] +"]";
    }

    /**
     * Get the rank { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" } of the playing card
     * @return value of the rank of the playing card. 0 = 2, 1 = 3 ... 12 = A
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the suit { "♥", "♠", "♦", "♣" } of the playing card
     * @return value of the suit of the playing card. 0 = ♥, 1 = ♠, 2 = ♦, 3 = ♣
     */
    public int getSuit() {
        return suit;
    }

    /**
     * copies the playing card
     * @return a copy of the playing card
     */
    public PlayingCard copy(){
        return new PlayingCard(this.suit,this.rank);
    }

    /**
     * ♥2 = 1
     * ♥3 = 2
     * ...
     * ♣A = 52
     * @return identifier of the card
     */
    public int getId(){
        return (rank+1)+suit*13;
    }

    /**
     * checks if two Playing Cards are equal.
     * @param o object to compare two
     * @return true if equal else false
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof PlayingCard){
            return ((PlayingCard) o).rank == rank && ((PlayingCard) o).suit == suit;
        }
        return false;
    }

    public String getImagePath(){
        return ranks[rank] + pathSuits[suit] +".png";
    }


    public String getLongRank(){
        return longRanks[rank];
    }
}
