package games.ZweiTausendAchtundVierzig;

import java.io.Serializable;

/**
 * Created by Johannes on 02.01.2017.
 */
public class Tile implements Serializable{
    private int value;
    private Position position;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

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

class Position implements Serializable{
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
