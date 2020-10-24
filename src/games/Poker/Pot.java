package src.games.Poker;

public class Pot {
    private double size;
    private final double[] open;
    private boolean[] claim;

    public Pot(){
        open = new double[StateObserverPoker.NUM_PLAYER];
        claim = new boolean[StateObserverPoker.NUM_PLAYER];
    }

    public Pot(Pot otherPot){
        this.size =  otherPot.size;
        this.open = new double[otherPot.open.length];
        this.claim = new boolean[otherPot.open.length];
        for(int i=0;i<open.length;i++) {
            this.open[i] = otherPot.open[i];
            this.claim[i] = otherPot.claim[i];
        }
    }

    public double getOpenPlayer(int player){
        return open[player];
    }


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

    private int getPaid(){
        int p = 0;
        for(double i:open)
            p+=i>0?0:1;
        return p;
    }

    public double getSize(){
        return size;
    }

    public Pot copy(){
        return new Pot(this);
    }

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
