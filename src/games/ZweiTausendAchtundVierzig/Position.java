package games.ZweiTausendAchtundVierzig;

/**
 * Created by Johannes on 06.11.2016.
 */
public class Position {
    public int row;
    public int column;
    private Tile tile;

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public Position(int row, int column, Tile tile) {
        this.row = row;
        this.column = column;
    }
}

class Tile {
    private int value;
    private Position position;

    public int getValue() {
        return value;
    }

    public void addValue(int value) {
        this.value += value;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Tile(int value, Position position) {
        this.value = value;
        this.position = position;
    }
}