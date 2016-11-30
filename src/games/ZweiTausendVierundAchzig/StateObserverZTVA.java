package games.ZweiTausendVierundAchzig;

import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Johannes on 18.11.2016.
 */
public class StateObserverZTVA implements StateObservation{
    private Random random = new Random();
    private List<Position> emptyTiles = new ArrayList();
    protected List<Integer> viableMoves;
    private Position[][] gameBoard;
    protected Types.ACTIONS[] actions;

    // 0 = running, 1 = won, -1 = lost
    private int winState;
    private int score;

    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    public double storedMaxScore;

    private final static double MAXSCORE = 3932156;
    private final static double MINSCORE = 0;

    public StateObserverZTVA() {
        newBoard();
    }

    public StateObserverZTVA(int[][] values, int score, int winState) {
        gameBoard = new Position[Config.ROWS][Config.COLUMNS];
        for(int row = 0; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++) {
                Position newPosition = new Position(row,column,null);
                gameBoard[row][column] = newPosition;
                if(values[row][column] != 0) {
                    Tile newTile = new Tile(values[row][column], newPosition);
                    newPosition.setTile(newTile);
                }
                else {
                    emptyTiles.add(newPosition);
                }
            }
        }
        this.score = score;
        this.winState = winState;

        if(!movesAvailable()) {
            setWinState(-1);
        }
    }

    @Override
    public StateObserverZTVA copy() {
        return new StateObserverZTVA(toArray(), score, winState);
    }

    @Override
    public boolean isGameOver() {
        if(winState == -1) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean isLegalState() {
        //ToDo: überprüfen ob wirklich legal state ist!
        return true;
    }

    @Override
    public Types.WINNER getGameWinner() {
        assert isGameOver() : "Game is not yet over!";
        switch (winState) {
            case -1:
                return Types.WINNER.PLAYER_LOSES;
            default:
                return Types.WINNER.PLAYER_WINS;
        }
    }

    @Override
    public double getGameScore() {
        return score;
    }

    @Override
    public double getGameScore(StateObservation referingState) {
        //ToDo: nicht sicher ob richtig
        assert (referingState instanceof StateObserverZTVA) : "referingState is not of class StateObserverZTVA";
        return referingState.getGameScore();
    }

    @Override
    public double getMinGameScore() {
        return MINSCORE;
    }

    @Override
    public double getMaxGameScore() {
        return MAXSCORE;
    }

    @Override
    public void advance(Types.ACTIONS action) {
        int iAction = action.toInt();
        assert (viableMoves.contains(iAction)) : "iAction is not viable.";

        move(iAction);

        setAvailableActions();
    }

    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        //ToDo: nicht sicher ob syntax so stimmt! :)
        ArrayList<Types.ACTIONS> availAct = new ArrayList<>();
        for(int viableMove : viableMoves) {
            availAct.add(Types.ACTIONS.fromInt(viableMove));
        }
        return availAct;
    }

    @Override
    public int getNumAvailableActions() {
        return viableMoves.size();
    }

    @Override
    public void setAvailableActions() {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
    }

    @Override
    public Types.ACTIONS getAction(int i) {
        return actions[i];
    }

    @Override
    public void storeBestActionInfo(Types.ACTIONS actBest, double[] vtable) {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        storedActions = new Types.ACTIONS[acts.size()];
        storedValues = new double[acts.size()];
        for(int i = 0; i < storedActions.length; ++i)
        {
            storedActions[i] = acts.get(i);
            storedValues[i] = vtable[i];
        }
        storedActBest = actBest;
        storedMaxScore = vtable[acts.size()];
    }

    @Override
    public int getPlayer() {
        return 0;
    }

    @Override
    public int getPlayerPM() {
        return 1;
    }

    @Override
    public int getNumPlayers() {
        return 1;
    }





    public Tile getTile(int row, int column) {
        return gameBoard[row][column].getTile();
    }

    public void addTile(Position position, int value) {
        emptyTiles.remove(position);
        gameBoard[position.row][position.column].setTile(new Tile(value, position));
    }

    public void mergeTiles(Tile tileOne, Tile tileTwo) {
        tileOne.addValue(tileTwo.getValue());
        score += tileOne.getValue();
        if(tileOne.getValue() >= Config.WINNINGVALUE) {
            setWinState(1);
        }
        tileTwo.getPosition().setTile(null);
        emptyTiles.add(tileTwo.getPosition());
    }

    public void moveTile(Tile tile, Position position) {
        tile.getPosition().setTile(null);
        emptyTiles.add(tile.getPosition());
        emptyTiles.remove(position);
        tile.setPosition(position);
        position.setTile(tile);
    }

    public Position getPosition(int row, int column) {
        return gameBoard[row][column];
    }

    /**
     *
     * @param winState -1 > lost, 0 > running, 1 > won
     */
    public void setWinState(int winState) {
        if(this.winState == 0) {
            this.winState = winState;
        }
    }

    public int[][] toArray() {
        int[][] newBoard = new int[Config.ROWS][Config.COLUMNS];
        for(int row = 0; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++) {
                if(gameBoard[row][column].getTile() == null) {
                    newBoard[row][column] = 0;
                }
                else {
                    newBoard[row][column] = gameBoard[row][column].getTile().getValue();
                }
            }
        }
        return newBoard;
    }

    public boolean movesAvailable() {
        viableMoves = new ArrayList<>();

        loop:
        for(int row = 0; row < Config.ROWS; row++) {
            for (int column = 1; column < Config.COLUMNS; column++) {
                if(getTile(row, column) == null){

                }
                else if (getTile(row, column - 1) == null || getTile(row, column).getValue() == getTile(row, column - 1).getValue()) {
                    viableMoves.add(0);
                    break loop;
                }
            }
        }

        loop:
        for(int row = 1; row < Config.ROWS; row++) {
            for (int column = 0; column < Config.COLUMNS; column++) {
                if(getTile(row, column) == null){

                }
                else if (getTile(row - 1, column) == null || getTile(row, column).getValue() == getTile(row - 1, column).getValue()) {
                    viableMoves.add(1);
                    break loop;
                }
            }
        }

        loop:
        for(int row = 0; row < Config.ROWS; row++) {
            for (int column = 0; column < Config.COLUMNS-1; column++) {
                if(getTile(row, column) == null){

                }
                else if (getTile(row, column + 1) == null || getTile(row, column).getValue() == getTile(row, column + 1).getValue()) {
                    viableMoves.add(2);
                    break loop;
                }
            }
        }

        loop:
        for(int row = 0; row < Config.ROWS-1; row++) {
            for (int column = 0; column < Config.COLUMNS; column++) {
                if(getTile(row, column) == null){

                }
                else if (getTile(row + 1, column) == null || getTile(row, column).getValue() == getTile(row + 1, column).getValue()) {
                    viableMoves.add(3);
                    break loop;
                }
            }
        }

        if(viableMoves.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void printBoard() {
        System.out.println("---------------------------------");
        for(Position[] row: gameBoard)
        {
            System.out.print("|");
            for(Position position: row) {
                Tile tile = position.getTile();
                if(tile != null) {
                    if(tile.getValue() < 10) {
                        System.out.print("   " + tile.getValue() + "   |");
                    }
                    else if(tile.getValue() < 100) {
                        System.out.print("  " + tile.getValue() + "   |");
                    }
                    else if(tile.getValue() < 1000) {
                        System.out.print("  " + tile.getValue() + "  |");
                    }
                    else if(tile.getValue() < 10000) {
                        System.out.print(" " + tile.getValue() + "  |");
                    }
                    else if(tile.getValue() < 100000) {
                        System.out.print(" " + tile.getValue() + " |");
                    }
                }
                else {
                    System.out.print("   0   |");
                }
            }
            System.out.println();
            System.out.println("---------------------------------");
        }
        System.out.println("score = " + score);
        System.out.println();
    }

    public void addRandomTile () {
        if(emptyTiles.size() > 0) {
            Position position = emptyTiles.get(random.nextInt(emptyTiles.size()));
            addTile(position, Config.STARTINGVALUES[random.nextInt(Config.STARTINGVALUES.length)]);
        }
    }

    /**
     *
     * @param move 0 > left, 1 > up, 2 > right, 3 > down
     */
    public void move(int move) {
        switch (move) {
            case 0:
                left();
                break;

            case 1:
                up();
                break;

            case 2:
                right();
                break;

            case 3:
                down();
                break;
        }
    }

    private void left() {
        if(viableMoves.contains(0)) {
            for (int row = 0; row < Config.ROWS; row++) {
                Tile lastTile = getTile(row, 0);
                int position = 0;

                for (int column = 1; column < Config.COLUMNS; column++) {
                    Tile currentTile = getTile(row, column);

                    if (currentTile != null && lastTile != null) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            mergeTiles(lastTile, currentTile);
                            position++;
                            lastTile = null;
                        } else {
                            position++;
                            lastTile = currentTile;
                            moveTile(currentTile, getPosition(row, position));
                        }
                    } else if (currentTile != null) {
                        moveTile(currentTile, getPosition(row, position));
                        lastTile = currentTile;
                    }
                }
            }
            addRandomTile();
        }
        else {
            System.out.println("Invalid Move 0");
            System.out.println("viableMoves = " + viableMoves);
        }

        if(!movesAvailable()) {
            setWinState(-1);
        }
    }

    private void up() {
        if(viableMoves.contains(1)) {
            for (int column = 0; column < Config.COLUMNS; column++) {
                Tile lastTile = getTile(0, column);
                int position = 0;

                for (int row = 1; row < Config.ROWS; row++) {
                    Tile currentTile = getTile(row, column);

                    if (currentTile != null && lastTile != null) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            mergeTiles(lastTile, currentTile);
                            position++;
                            lastTile = null;
                        } else {
                            position++;
                            lastTile = currentTile;
                            moveTile(currentTile, getPosition(position, column));
                        }
                    } else if (currentTile != null) {
                        moveTile(currentTile, getPosition(position, column));
                        lastTile = currentTile;
                    }
                }
            }
            addRandomTile();
        }
        else {
            System.out.println("Invalid Move 1");
            System.out.println("viableMoves = " + viableMoves);
        }

        if(!movesAvailable()) {
            setWinState(-1);
        }
    }

    private void right() {
        if(viableMoves.contains(2)) {
            for (int row = 0; row < Config.ROWS; row++) {
                Tile lastTile = getTile(row, Config.COLUMNS - 1);
                int position = Config.COLUMNS - 1;

                for (int column = Config.COLUMNS - 2; column >= 0; column--) {
                    Tile currentTile = getTile(row, column);

                    if (currentTile != null && lastTile != null) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            mergeTiles(lastTile, currentTile);
                            position--;
                            lastTile = null;
                        } else {
                            position--;
                            lastTile = currentTile;
                            moveTile(currentTile, getPosition(row, position));
                        }
                    } else if (currentTile != null) {
                        moveTile(currentTile, getPosition(row, position));
                        lastTile = currentTile;
                    }
                }
            }
            addRandomTile();
        }
        else {
            System.out.println("Invalid Move 2");
            System.out.println("viableMoves = " + viableMoves);
        }

        if(!movesAvailable()) {
            setWinState(-1);
        }
    }

    private void down() {
        if(viableMoves.contains(3)) {
            for (int column = 0; column < Config.COLUMNS; column++) {
                Tile lastTile = getTile(Config.ROWS - 1, column);
                int position = Config.ROWS - 1;

                for (int row = Config.ROWS - 2; row >= 0; row--) {
                    Tile currentTile = getTile(row, column);

                    if (currentTile != null && lastTile != null) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            mergeTiles(lastTile, currentTile);
                            position--;
                            lastTile = null;
                        } else {
                            position--;
                            lastTile = currentTile;
                            moveTile(currentTile, getPosition(position, column));
                        }
                    } else if (currentTile != null) {
                        moveTile(currentTile, getPosition(position, column));
                        lastTile = currentTile;
                    }
                }
            }
            addRandomTile();
        }
        else {
            System.out.println("Invalid Move 3");
            System.out.println("viableMoves = " + viableMoves);
        }

        if(!movesAvailable()) {
            setWinState(-1);
        }
    }

    private void newBoard() {
        gameBoard = new Position[Config.ROWS][Config.COLUMNS];
        for(int row = 0; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++) {
                Position newPosition = new Position(row,column,null);
                gameBoard[row][column] = newPosition;
                emptyTiles.add(newPosition);
            }
        }
        score = 0;
        winState = 0;

        for(int i = Config.STARTINGFIELDS; i > 0; i--) {
            addRandomTile();
        }

        if(!movesAvailable()) {
            setWinState(-1);
        }
    }
}
