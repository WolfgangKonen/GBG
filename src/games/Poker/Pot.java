package src.games.Poker;

public class Pot {
    private double size;
    private final double[] open;
    private boolean[] claim;

    /**
     * Object to handle a pot within Pots.
     */
    public Pot(){
        open = new double[StateObserverPoker.NUM_PLAYER];
        claim = new boolean[StateObserverPoker.NUM_PLAYER];
    }

    /**
     * Create a copy of another Pot.
     * @param otherPot to copy
     */
    public Pot(Pot otherPot){
        this.size =  otherPot.size;
        this.open = new double[otherPot.open.length];
        this.claim = new boolean[otherPot.open.length];
        for(int i=0;i<open.length;i++) {
            this.open[i] = otherPot.open[i];
            this.claim[i] = otherPot.claim[i];
        }
    }

    /**
     * Returns the amount of chips a player has to bet to compete for the pot.
     * @param player to get the open chips for.
     * @return amount of chips to bet.
     */
    public double getOpenPlayer(int player){
        return open[player];
    }

    /**
     * Adds a number of chips to the pot for a certain player.
     * @param player betting player
     */
    public void add(double chips, int player){
        size += chips;

        double dif =  chips - open[player];

        if(dif > 0) {
            for (int i = 0; i < open.length; i++)
                open[i] += dif;
            claim = new boolean[StateObserverPoker.NUM_PLAYER];
        }
        claim[player] = true;
        open[player] -= chips;
    }

    /**
     * Split a pot into two.
     * @param chips chips a player has bet
     * @param player betting player
     * @return the new pot
     */
    public Pot split(double chips, int player){
        Pot splitPot = new Pot();

        double dif = open[player]-chips;
        splitPot.size = dif*getPaid();

        this.size -= splitPot.size;

        for (int i = 0; i < open.length; i++){
            if(open[i]>0){
               open[i] -= dif;
               splitPot.open[i] = dif;
            }
            splitPot.claim[i] = claim[i];
        }
        if(chips>0) {
            this.size += chips;
            this.claim[player] = true;
            this.open[player] = 0;
        }
        return splitPot;
    }

    /**
     * check how many player have already paid into the pot
     * @return number of player
     */
    private int getPaid(){
        // does only work if for not playing players open chips are still tracked
        int p = 0;
        for(double i:open)
            p+=i>0?0:1;
        return p;
    }

    /**
     * returns the size of the pot
     * @return amount of chips in the pot
     */
    public double getSize(){
        return size;
    }

    /**
     * copies the pot object
     * @return a copy of the pot
     */
    public Pot copy(){
        return new Pot(this);
    }

    /**
     * Creates a string representation of the pot in the form.
     * E.g.: "Pot: 70 (0: 20, 1: 20, 2: 0, 3, 0)"
     * @return a string representation of the pot
     */
    public String toString(){
        String toString = "";
        toString = toString.concat("Pot: " + size + " (");
        for(int i = 0 ; i < open.length ; i++)
            toString =  toString.concat(i + ": "+ open[i] +", ");
        return  toString.concat(")");
    }
    public boolean[] getClaims(){
        return claim;
    }

}
