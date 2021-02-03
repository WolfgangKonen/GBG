package games.BlackJack;
import static games.BlackJack.BlackJackConfig.CARDS_AS_UNICODE;

public class Card {

    enum Suit {
        HEART, DIAMOND, CLUB, SPADE, X;
    }

    enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10),
        ACE(11), X(0);

        private int value;

        Rank(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    Rank rank;
    Suit suit;
    int Id;

    public Card(Rank rank, Suit suit, int Id) {
        this.rank = rank;
        this.suit = suit;
        this.Id = Id;
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
        if (rank.value < 10) {
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