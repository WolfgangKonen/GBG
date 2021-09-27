package games.KuhnPoker;

import java.io.Serializable;
import java.util.ArrayList;

public class Pots implements Serializable {
        private final ArrayList<Pot> pots;

    /**
     * Manages the bets in different pots during a poker game.
     */
    Pots(){
            pots = new ArrayList<>();
            pots.add(new Pot());
        }

    /**
     * Copies an existing Pots object.
     * @param copy to be copied
     */
    Pots(Pots copy){
        pots = new ArrayList<>();
        for(Pot p:copy.pots)
            this.pots.add(new Pot(p));
    }

    /**
     * Copies the pots object.
     * @return a copy of the object
     */
    public Pots copy(){
        return new Pots(this);
    }

    /**
     * Adds a number of chips to the pot for a certain player.
     * @param o_chips number of chips bet
     * @param player betting player
     * @param allin was it an all in?
     * @return 0;
     */
    public double add(double o_chips, int player,boolean allin) {
        double chips = o_chips;
        int numPot = pots.size();
        for(int i = 0;i<numPot;i++){
            Pot pot = pots.get(i);
            double toCall = pot.getOpenPlayer(player);

            if(chips >= toCall) {
                pot.add(toCall, player);
                chips -= toCall;
            }else{
                pots.add(i+1,pot.split(chips, player));
                chips = 0;
                break;
            }
        }
        if(chips>0) {
            pots.get(pots.size() - 1).add(chips, player);
            if (allin)
                pots.add(pots.size(), pots.get(pots.size() - 1).split(0, player));
        }
        return 0;
    }

    /**
     * Adds a number of chips to the pot for a certain player.
     * @param o_chips number of chips bet
     * @param player betting player
     * @return 0;
     */
    public double add(double o_chips, int player){
       return add(o_chips,player,false);
    }

    /**
     * Returns the amount of chips a player has to bet to compete for the pot.
     * @param player to get the open chips for.
     * @return amount of chips to bet.
     */
    public double getOpenPlayer(int player){
        int open = 0;
        for (Pot pot: pots) {
            open += pot.getOpenPlayer(player);
        }
        return open;
    }

    /**
     * Returns the total size of the pot.
     * @return chips in the pots
     */
    public int getSize(){
        int size = 0;
        for (Pot pot: pots) {
            size += pot.getSize();
        }
        return size;
    }

    /**
     * Returns the size of a particular (split)-pot
     * @param p pot to get the size of
     * @return chips in pot[p]
     */
    public double getPotSize(int p){
        return this.pots.get(p).getSize();
    }

    /**
     * Returns an overview which player has a claim for the pot(s).
     * e.g. [0] == {true,true,true,false} => player 0,1,2 are competing for the pot [0] player 3 not.
     * @return two dimensional array with [pot][player] and claim
     */
    public boolean[][] getClaims(){
        boolean[][] claims = new boolean[pots.size()][];
        for(int i = 0;i< pots.size();i++){
            claims[i] = pots.get(i).getClaims();
        }
        return claims;
    }

    /**
     * Returns a string representation of the pot.
     * @return string representation.
     */
    public String toString(){
        String toString = "";
        for (Pot pot: pots){
            toString = toString.concat(pot.toString());
        }
        return toString;
    }
}
