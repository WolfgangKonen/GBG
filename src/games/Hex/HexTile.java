package games.Hex;

import java.awt.*;
import java.io.Serializable;

public class HexTile implements Serializable {
    private Point coords;
    private int player;
    private Polygon poly;
    private double value;

    public HexTile(int i, int j){
        coords = new Point(i, j);
        player = HexConfig.PLAYER_NONE;
        value = Double.NaN;
    }

    public HexTile(int i, int j, int player, Polygon poly, double value){
        coords = new Point(i, j);
        this.player = player;
        this.poly = poly;
        this.value = value;
    }

    public Point getCoords() {
        return coords;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public Polygon getPoly() {
        return poly;
    }

    public void setPoly(Polygon poly) {
        this.poly = poly;
    }

    @Override
    public boolean equals(Object object){
        return object != null && object instanceof HexTile &&
                ((HexTile) object).getCoords().x == this.coords.x &&
                ((HexTile) object).getCoords().y == this.coords.y;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public HexTile copy(){
        return new HexTile(coords.x, coords.y, player, poly, value);
    }
}
