package games.RubiksCube;

import java.text.DecimalFormat;

/**
 * This class is only needed for RUBIKS (3x3x3 cube): A representation for an edge cubie
 */
public class CubieDouble {
    int[] loc;
    int[] col;

    /**
     * If {@code i} is the sticker location for the w-face of the wb-edge, then {@code other[i]} is the sticker location
     * for the b-face of the wb-edge.
     */
    protected static int[] other=  {0,23,0,35,0,45,0,9,    0,7,0,43,0,29,0,17,   0,15,0,27,0,37,0,1,
                                    0,39,0,19,0,13,0,41,   0,47,0,3,0,21,0,25,   0,31,0,11,0,5,0,33};

    public CubieDouble(int i) {
        initialize(i);
    }

    public CubieDouble(CubieDouble other) {
        this.loc = other.loc.clone();
        this.col = other.col.clone();
    }

    protected void initialize(int i) {
        int[] ccolo = CubieTriple3x3.ccolo;
        this.loc = new int[] {i, other[i]};
        this.col = new int[] {ccolo[i], ccolo[other[i]]};
    }

    public CubieDouble print() {
        System.out.println(this); // calls toString()
        return this;
    }

    public String toString() {
        DecimalFormat form = new DecimalFormat("00");
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i=0;i<2;i++) {
            sb.append(form.format(this.loc[i]));
            if (i!=1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        assert (other instanceof CubieDouble) : "Object other is not of class CubieDouble";
        //System.out.println("Passing CubieDouble::equals()");
        CubieDouble cOther = (CubieDouble) other;
        for (int i=0; i<col.length; i++) {
            if (this.col[i]!=cOther.col[i]) return false;
            if (this.loc[i]!=cOther.loc[i]) return false;
        }
        return true;

    }
}
