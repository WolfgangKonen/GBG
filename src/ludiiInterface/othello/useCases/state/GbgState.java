package ludiiInterface.othello.useCases.state;

import ludiiInterface.othello.transform.players.GbgPlayer;
import tools.Types;

import java.util.ArrayList;

interface GbgState {
    void advance(final Types.ACTIONS action);

    int[][] toArray2D();

    ArrayList<Types.ACTIONS> allAvailableActions();

    ArrayList<Types.ACTIONS> availableActions();

    GbgState copy();

    GbgPlayer player();

    String stringRepresentation();
}