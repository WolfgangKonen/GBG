package games.EWS.StateObserverHelper;

import tools.Types;

public class Helper {

    /**
     * Concatenate from + to =>  1, 4 => 104
     * where [1][04] is the string;
     * @param from  index of board
     * @param to index of board
     * @param size  board size
     * @return  ACTiON
     */
    public static Types.ACTIONS parseAction(int from, int to, int size){
        from *= 100; // 0 => 0   [1,...9] => x00  [10,...25] => xx00
        return new Types.ACTIONS(from + to);
    }

    /**
     * Parsing the action to an array of indices [from, to]
     * @param act   Action to parse
     * @return  Array of int [from, to]
     */
    public static int[] getIntsFromAction(Types.ACTIONS act){
        int to = act.toInt() %  100;
        int from = (act.toInt() - to) / 100;
        return new int[]{from,to};
    }

    public static int[] getMoveDirection(int size, int player){
        switch(player){
            case 0: return new int[]{1,size,size+1};
            case 1: return new int[]{-1,-size,-size+(-1)};
            case 2: return new int[]{-size,-size+1,1};
            case 3: return new int[]{-1,size,size-1};
            default: throw new RuntimeException("Player does not exist yet.");
        }
    }


}
