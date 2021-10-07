package games.BlackJack;
import static games.BlackJack.BlackJackConfig.CARDS_AS_UNICODE;

public class Card implements Comparable<Card>{

    /**
     * Compares this card with the specified card for order. Returns a negative integer, zero,
     * or a positive integer as this card is less than, equal to, or greater than the specified object.
     * @param o the other card
     * @return int -1 or 0 or 1
     */
    @Override
    public int compareTo(Card o) {
        return (int) Math.signum(this.rank.sortValue - o.rank.sortValue);
    }

    /**
     * Represents the suit of a card as enum
     */
    enum Suit {
        HEART(0), DIAMOND(1), CLUB(2), SPADE(3), X(4);
        private int value;

        Suit(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

    /**
     * Represents the rank of a card as enum
     */
    enum Rank {
        TWO(2, 0), THREE(3, 1), FOUR(4 , 2), FIVE(5, 3),
        SIX(6, 4), SEVEN(7, 5), EIGHT(8, 6), NINE(9, 7),
        TEN(10, 8), JACK(10, 9), QUEEN(10, 10), KING(10, 11),
        ACE(1, 12), X(0, 13);

        private int value;
        private int sortValue;

        Rank(int value, int sortValue) {
            this.value = value;
            this.sortValue = sortValue;
        }
        
        public int getValue() { return value; }
        public int getSortValue(){ return sortValue; }
        public static Rank getRankFromValue(int v){
            for(Rank r : Rank.values()){
                if(v == r.value)
                    return r;
            }
            return null;
        }
    }

    Rank rank;
    Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * @return imagePath to corresponding image representation of the card
     */
    public String getImagePath(){
        if(rank.value < 1){
            return "2B.png";
        }
        return rankSubString() + suitSubString() +".png";
    }

    /**
     * @return suit of the card as String
     */
    public String suitSubString(){
        String suitStr="";
        switch (suit) {
            case HEART:
                suitStr = "H";
                break;
            case DIAMOND:
                suitStr = "D";
                break;
            case CLUB:
                suitStr = "C";
                break;
            case SPADE:
                suitStr = "S";
                break;
            default:
                break;
        }
        return suitStr;
    }

    /**
     * @return suit of the card as unicode String
     */
    public String suitSubStringUnicode(){
        String suitStr = "";
        switch (suit) {
            case HEART:
                suitStr = "♥";
                break;
            case DIAMOND:
                suitStr = "♦";
                break;
            case CLUB:
                suitStr = "♣";
                break;
            case SPADE:
                suitStr = "♠";
                break;
            default:
                break;
        }
        return suitStr;
    }

    /**
     * @return rank of the card as unicode String
     */
    public String rankSubString(){
        String rankStr = "";
        if (rank.value < 1) {
            return "X";
        }
        if (rank.value > 1 && rank.value < 10) {
            rankStr = String.valueOf(rank.value);
        } else {
            rankStr = rank.name().substring(0, 1);
        }
        return rankStr;
    }


    public String toString() {
        return rankSubString() + (CARDS_AS_UNICODE ? suitSubStringUnicode() : suitSubString());
    }
}