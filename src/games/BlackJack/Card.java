package games.BlackJack;

public class Card {
    private final boolean useUnicodeForReprentation = false;
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

    public String toString() {
        String rankStr = "";
        if (rank.value < 1) {
            return "X";
        }
        if (rank.value < 10) {
            rankStr = String.valueOf(rank.value);
        } else {
            rankStr = rank.name().substring(0, 1);
        }
        String suitStr = "";
        if(useUnicodeForReprentation){
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
        }
        else {
            switch (suit) {
                case HEART:
                    suitStr = "h";
                    break;
                case DIAMOND:
                    suitStr = "d";
                    break;
                case CLUB:
                    suitStr = "c";
                    break;
                case SPADE:
                    suitStr = "s";
                    break;
                default:
                    break;
            }
        }
        return rankStr + suitStr;
        }
}