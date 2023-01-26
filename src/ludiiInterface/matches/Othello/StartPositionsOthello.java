package ludiiInterface.matches.Othello;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.Map.entry;

/**
 * Class for using XOT Openings by Borja Moreno & Matthias Berg,
 * found at <a href="https://berg.earthlingz.de/xot/index.php?lang=en">xot.xmav.eu</a>.
 * */

public class StartPositionsOthello {

    static Map<String, Integer> mapPositionsToMoves;

    public StartPositionsOthello(){
        //noinspection RedundantTypeArguments (explicit type arguments speedup compilation and analysis time)
        mapPositionsToMoves = Map.<String, Integer>ofEntries(
                entry("a8", 0), entry("b8", 1), entry("c8", 2), entry("d8", 3), entry("e8", 4), entry("f8", 5), entry("g8", 6), entry("h8", 7),
                entry("a7", 8), entry("b7", 9), entry("c7", 10), entry("d7", 11), entry("e7", 12), entry("f7", 13), entry("g7", 14), entry("h7", 15),
                entry("a6", 16), entry("b6", 17), entry("c6", 18), entry("d6", 19), entry("e6", 20), entry("f6", 21), entry("g6", 22), entry("h6", 23),
                entry("a5", 24), entry("b5", 25), entry("c5", 26), entry("d5", 27), entry("e5", 28), entry("f5", 29), entry("g5", 30), entry("h5", 31),
                entry("a4", 32), entry("b4", 33), entry("c4", 34), entry("d4", 35), entry("e4", 36), entry("f4", 37), entry("g4", 38), entry("h4", 49),
                entry("a3", 40), entry("b3", 41), entry("c3", 42), entry("d3", 43), entry("e3", 44), entry("f3", 45), entry("g3", 46), entry("h3", 47),
                entry("a2", 48), entry("b2", 49), entry("c2", 50), entry("d2", 51), entry("e2", 52), entry("f2", 53), entry("g2", 54), entry("h2", 55),
                entry("a1", 56), entry("b1", 57), entry("c1", 58), entry("d1", 59), entry("e1", 60), entry("f1", 61), entry("g1", 62), entry("h1", 63)
        );
    }

    /**
     * @return random 8-move opening out of 3623 openings from the small list
     * */
    public List<Integer> getFromSmallList() {

        Path txtfile = Paths.get("src", "ludiiInterface", "matches", "Othello", "XOTOpeningsSmallList.txt");
        //System.out.println(txtfile);

        int line = new Random().nextInt(1, 3623);
        System.out.println("Opening taken from line "+(line+1));

        String startPosition = null;

        try (BufferedReader br = Files.newBufferedReader(txtfile)) {

            for (int x = 0; x < line; x++) {
                br.readLine();
            }
            //System.out.println(br.readLine());
            startPosition = br.readLine();

        } catch (IOException exception) {
            System.out.print("Fehler!");
        }

        List<Integer> openings = new ArrayList<Integer>();

        for(int i = 0; i < 16; i+=2){
            assert startPosition != null;
            openings.add(mapPositionsToMoves.get(startPosition.substring(i,i+2))); // könnte auch ohne math.min funktionieren (?)
        }

        return openings;
    }

    /*public List<Integer> getFromLargeList(){

    }*/
}
