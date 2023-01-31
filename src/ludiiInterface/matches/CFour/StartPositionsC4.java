package ludiiInterface.matches.CFour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartPositionsC4 {

    public List<Integer> getOpening(int ply) {

        int even = ply%2;

        if(even!=0 || ply > 7 ) {
            throw new IllegalArgumentException("Amount of opening moves (plies) needs to be even! Maximum amount of opening moves (plies) is 6!");
        }

        List<Integer> positionsList = new ArrayList<>();

        for(int i = 0; i < ply; i++)
            positionsList.add(new Random().nextInt(7));

        return positionsList;
    }

}
