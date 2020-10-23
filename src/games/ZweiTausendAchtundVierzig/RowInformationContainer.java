package games.ZweiTausendAchtundVierzig;

import java.io.Serializable;

class RowInformationContainer implements Serializable {
    int rowLength;
    int rowValue;

    public RowInformationContainer(int rowLength, int rowValue) {
        this.rowLength = rowLength;
        this.rowValue = rowValue;
    }
}
