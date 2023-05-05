package games.EWN.StateObserverHelper;

import games.EWN.config.ConfigEWN;
import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;

public class Token implements Serializable {
    private ArrayList<Types.ACTIONS> availableActions;
    private int index;      // index of the token's position on the board
    private int value;      // the value on the token minus 1
    private int player;


    public Token(int x, int y,int value, int player){
        this.player = player;
        this.index = x * ConfigEWN.BOARD_SIZE + y;
        this.value = value;
        availableActions = new ArrayList<>();
    }

    public Token(Token other){
        this.player = other.getPlayer();
        this.value = other.getValue();
        this.index = other.getIndex();
        this.availableActions = other.getAvailAbleActions();
    }

    /**
     * Given the specific {@code (index,player)} of {@code this} token, calculate the available actions for it
     */
    public void setAvailableActions(){
        availableActions.clear();
        int[] directions = Helper.getMoveDirection(player);
        int sz = ConfigEWN.BOARD_SIZE;
        for(int dir: directions){
            int newPos = index + dir;
            if(newPos < 0  || newPos > sz*sz-1) continue; // Bounds check for [0,...,size²-1]
            if(newPos % sz == 0 && index % sz == sz-1) continue;
            if(newPos % sz == sz-1 && index % sz == 0) continue;
            Types.ACTIONS a = Helper.parseAction(index,newPos);
            availableActions.add(a);
        }
    }

    public Token copy(){
        return new Token(this);
    }

    public int getSize(){
        return ConfigEWN.BOARD_SIZE;
    }

    public int getIndex(){
        return index;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public void setIndex(int i, int k) {
        this.index = i * ConfigEWN.BOARD_SIZE + k;
    }

    public void setIndex(int i){
        this.index = i;
    }

    public void setPlayer(int p){
        player = p;
    }

    public int getPlayer(){
        return player;
    }


    public ArrayList<Types.ACTIONS> getAvailAbleActions(){
        return availableActions;
    }

}
