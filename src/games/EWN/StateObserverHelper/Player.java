package games.EWN.StateObserverHelper;

import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    private  ArrayList<Types.ACTIONS> availableActions;
    private ArrayList<Token> tokens;
    private int playerNumber;
    private int size;

    public Player(int playerNumber, int size){
      availableActions = new ArrayList<>();
      tokens = new ArrayList<Token>();
      this.playerNumber  = playerNumber;
      this.size = size;
    }

    public void removeToken(Token token){
        int index = token.getIndex();
        for(Token t: tokens){
            if(t.getIndex() == index){
                tokens.remove(t);
                break;
            }
        }
    }

    public void addToken(Token token){
        tokens.add(token);
    }

    public int getSize(){
        return size;
    }

    public void setAvailableActions(int diceValue){
        availableActions.clear();
        Token[] matchingToken = selectToken(diceValue);
        if(matchingToken.length == 0) return;
        for(Token token : matchingToken){
            token.setAvailableActions();
            availableActions.addAll(token.getAvailAbleActions());
        }
    }

    public void updateToken(Token from, Token to){
        for(int i = 0; i < tokens.size();i++){
            if(tokens.get(i).getIndex() == from.getIndex()){
                tokens.get(i).setIndex(to.getIndex());
            }
        }

    }

    /**
     * Return the list of tokens that are allowed to move, given {@code diceValue}
     * @param diceValue     the value of the dice
     * @return              the list of tokens (pieces of the current player)
     */
    private Token[] selectToken(int diceValue){
        Token t = getTokenByValue(diceValue);
        if(t != null) return new Token[]{t};
        int enhancement = 1;
        Token nextLower = null;
        Token nextHigher = null;
        while(nextLower == null && nextHigher == null && enhancement < 6){
            nextLower = getTokenByValue(diceValue-enhancement);
            nextHigher = getTokenByValue(diceValue+enhancement);
            enhancement++;
        }
        if(enhancement > 6) return new Token[]{};
        if(nextHigher !=null && nextLower != null){
            return new  Token[]{nextHigher,nextLower};
        }
        if(nextHigher  != null) return  new  Token[]{nextHigher};
        if(nextLower  != null) return  new  Token[]{nextLower};
        return new Token[]{};
    }



    private Token getTokenByValue(int value){
        for(Token token: tokens) {
            if (token.getValue() == value) return token;
        }
        return null;
    }


    public ArrayList<Types.ACTIONS> getAvailableActions(){
        return availableActions;
    }


    public int getPlayer(){
        return playerNumber;
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

    public Player copy(){
        return this;
    }
}
