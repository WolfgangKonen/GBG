package games.ZweiTausendAchtundVierzig;

/**
 * Created by Johannes on 02.01.2017.
 */
public class Tile {
    private int value;
    private Position position;

    public int getValue() {
        return value;
    }

    public void addValue(int value) {
        this.value += value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Tile(int value, Position position) {
        this.value = value;
        this.position = position;
    }
}

class Position {
    private int row;
    private int column;

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
