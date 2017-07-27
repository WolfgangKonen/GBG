package games.Hex;

import java.awt.*;
import java.io.Serializable;

public class HexTile implements Serializable {
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog containing this object will become
     * unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 12L;
    private Point coords;
    private int player;
    private Polygon poly;
    private double value;

    /**
     * Constructor for tiles that are not supposed to be drawn to the screen.
     *
     * @param i first index in board array
     * @param j second index in board array
     */
    public HexTile(int i, int j) {
        coords = new Point(i, j);
        player = HexConfig.PLAYER_NONE;
        value = Double.NaN;
    }

    /**
     * Constructor for tiles that are part of the visible game board.
     *
     * @param i      first index in board array
     * @param j      second index in board array
     * @param player The player that owns the tile
     * @param poly   Polygon containing the vertices needed for drawing the tile to the screen
     * @param value  Tile value
     */
    public HexTile(int i, int j, int player, Polygon poly, double value) {
        coords = new Point(i, j);
        this.player = player;
        this.poly = poly;
        this.value = value;
    }

    /**
     * @return A point, with x and y being the place in the board array
     */
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
    public boolean equals(Object object) {
        return object != null && object instanceof HexTile &&
                ((HexTile) object).getCoords().x == this.coords.x &&
                ((HexTile) object).getCoords().y == this.coords.y;
    }

    public double getValue() {
        return value;
    }

    /**
     * Sets the tile value given by the agent that had the latest turn. Used when drawing the tiles.
     *
     * @param value Tile value
     */
    public void setValue(double value) {
        this.value = value;
    }

    public HexTile copy() {
        return new HexTile(coords.x, coords.y, player, poly, value);
    }

    @Override
    public String toString() {
        return "HexTile [" + coords.x + ", " + coords.y + "]";
    }
}
