package games.Yavalath;

import java.awt.*;
import java.io.Serializable;

import static games.Yavalath.ConfigYavalath.*;

public class TileYavalath implements Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip will become unreadable, or you have
     * to provide a special version transformation)
     */
    private static final long  serialVersionUID = 12L;
    private int x;
    private int y;
    private int player;
    private double value;
    private Polygon poly;
    private Point tileCenter;
    private boolean threateningMove; //Only used for marking it on the board so its easy to recognize

    public TileYavalath(int x, int y, int player){
        this.player = player;
        this.x = x;
        this.y = y;
        value = Double.NaN;
        this.createPoly();
    }

    public TileYavalath(int x, int y, int player, double value, Polygon poly, Point tileCenter) {
        this.x = x;
        this.y = y;
        this.player = player;
        this.value = value;
        this.poly = poly;
        this.tileCenter = tileCenter;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public TileYavalath copyTile(){
        return new TileYavalath(x,y,player, value, poly, tileCenter);
    }

    public void setPoly(Polygon poly){
        this.poly = poly;
    }

    public Polygon getPoly(){
        return poly;
    }

    public Point getTileCenter(){
        return tileCenter;
    }

    public void setThreateningMove(boolean threat){
        threateningMove = threat;
    }

    public boolean getThreateningMove(){
        return threateningMove;
    }

    /**
     *
     * Creates the polygon that will represent the hexagonal tile in the GUI
    */
    public void createPoly(){
        if(player == INVALID_FIELD) return;
        poly = new Polygon();
        tileCenter = new Point();
        tileCenter.x = (2*TILE_SIZE+OFFSET+TILE_SIZE/2)-x*(TILE_SIZE/2)+(y*TILE_SIZE);
        tileCenter.y = (OFFSET+(TILE_SIZE/2))+((3*TILE_SIZE/4)*x);

        poly.addPoint(tileCenter.x,tileCenter.y-TILE_SIZE/2);
        poly.addPoint(tileCenter.x+TILE_SIZE/2,tileCenter.y-TILE_SIZE/4);
        poly.addPoint(tileCenter.x+TILE_SIZE/2,tileCenter.y+TILE_SIZE/4);
        poly.addPoint(tileCenter.x,tileCenter.y+TILE_SIZE/2);
        poly.addPoint(tileCenter.x-TILE_SIZE/2,tileCenter.y+TILE_SIZE/4);
        poly.addPoint(tileCenter.x-TILE_SIZE/2,tileCenter.y-TILE_SIZE/4);

    }

}
