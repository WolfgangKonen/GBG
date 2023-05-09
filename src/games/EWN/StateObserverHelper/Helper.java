package games.EWN.StateObserverHelper;

import games.EWN.config.ConfigEWN;
import tools.Types;

public class Helper {

    /**
     * Given a move 'from &rArr; to', concatenate from with to, e.g.  from=1, to=4 &rArr; 104
     *
     * @param from  index of board
     * @param to index of board
     * @return  ACTION
     */
    public static Types.ACTIONS parseAction(int from, int to){
        from *= 100; // 0 => 0   [1,...9] => x00  [10,...25] => xx00
        return new Types.ACTIONS(from + to);
    }

    /**
     * Parse the action number to get the array of indices [from, to]
     * @param act   Action to parse
     * @return  Array of int [from, to]
     */
    public static int[] getIntsFromAction(Types.ACTIONS act){
        int to = act.toInt() %  100;
        int from = (act.toInt() - to) / 100;
        return new int[]{from,to};
    }

    /**
     * Return potential field increments: array of integers {@code dir} such that {@code newpos = pos + dir} is a
     * possible new position for {@code player} (if still on the board, checked by {@link Token#setAvailableActions()})
     * @param player    the player number (0,1,2,3), has to be less than {@link ConfigEWN#NUM_PLAYERS}
     * @return  array of field increments
     *
     * @see Token#setAvailableActions()
     */
    public static int[] getMoveDirection(int player){
        int size = ConfigEWN.BOARD_SIZE;
        switch(player){
            case 0: return new int[]{1,size,size+1};
            case 1: return new int[]{-1,-size,-size+(-1)};
            case 2: return new int[]{-size,-size+1,1};
            case 3: return new int[]{-1,size,size-1};
            default: throw new RuntimeException("Player does not exist yet.");
        }
    }


}
