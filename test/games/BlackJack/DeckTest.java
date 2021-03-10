package games.BlackJack;

import org.junit.Test;

public class DeckTest {
    @Test
    public void testDeck(){
        int num_iterations = 100000000;
        int [] counts = new int[52];
        for(int i = 0; i < num_iterations; i++) {
            counts[ArenaBlackJack.deck.draw().Id]++;
        }

        for(int i = 0; i < counts.length; i++) {
            assert (counts[i]/100000 == 19): "distribution of cards should be around 190000 for each card";
            //System.out.println("card " + i + " : " + counts[i]);
        }
    }
}
