package games.EWN.StateObserverHelper;

import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;

public class Token implements Serializable {
    private ArrayList<Types.ACTIONS> availableActions;
    private int index;
    private int value;
    private int size;
    private int player;


    public Token(int x, int y,  int size,int value, int player){
        this.size = size;
        this.player = player;
        this.index = x * size + y;
        this.value = value;
        availableActions = new ArrayList<>();
    }

    public Token(Token other){
        this.size = other.getSize();
        this.player = other.getPlayer();
        this.value = other.getValue();
        this.index = other.getIndex();
        this.availableActions = other.getAvailAbleActions();
    }

    public void setAvailableActions(){
        availableActions.clear();
        int[] directions = Helper.getMoveDirection(size,player);
        for(int dir: directions){
            int newPos = index + dir;
            if(newPos < 0  || newPos > size*size-1) continue; // Bounds check for [0,...,size²-1]
            if(newPos % size == 0 && index % size == size-1) continue;
            if(newPos % size == size-1 && index % size == 0) continue;
            Types.ACTIONS a = Helper.parseAction(index,newPos,size);
            if(a != null) availableActions.add(a);
            else System.out.println("index: " + index + " value " + value + " Player: " + player);
        }
    }

    public Token copy(){
        return new Token(this);
    }

    public int getSize(){
        return size;
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
        this.index = i * size + k;
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
