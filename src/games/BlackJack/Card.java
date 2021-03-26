package games.BlackJack;
import static games.BlackJack.BlackJackConfig.CARDS_AS_UNICODE;

public class Card implements Comparable<Card>{


    @Override
    public int compareTo(Card o) {
        return (int) Math.signum(this.rank.sortValue - o.rank.sortValue);
    }

    enum Suit {
        HEART(0), DIAMOND(1), CLUB(2), SPADE(3), X(4);
        private int value;

        Suit(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

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

    public String getImagePath(){
        if(rank.value < 1){
            return "2B.png";
        }
        return rankSubString() + suitSubString() +".png";
    }

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