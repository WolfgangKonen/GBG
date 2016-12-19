package games.ZweiTausendAchtundVierzig;

import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Johannes on 18.11.2016.
 */
public class StateObserver2048 implements StateObservation{
    private Random random = new Random();
    private List<Position> emptyTiles = new ArrayList();
    protected List<Integer> availableMoves;
    private Position[][] gameBoard;
    protected Types.ACTIONS[] actions;

    // 0 = running, 1 = won, -1 = lost
    private int winState;
    public int score;
    private List<Tile> highestTiles = new ArrayList();

    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    public double storedMaxScore;

    public final static double MAXSCORE = 3932156;
    public final static double MINSCORE = 0;
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

    public boolean highestTileInCorner = false;

    public StateObserver2048() {
        newBoard();
    }

    public StateObserver2048(int[][] values, int score, int winState) {
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

        updateAvailableMoves();
    }

    @Override
    public StateObserver2048 copy() {
        return new StateObserver2048(toArray(), score, winState);
    }

    @Override
    public boolean isGameOver() {
        if(availableMoves.size() == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean isLegalState() {
        //Sollte eigentlich nicht benötigt werden, solange das Spielfeld nur über advance() verändert wird.
        //Ich überprüfe den State vorerst nicht, da die Überprüfung nicht Notwendig sein sollte und das Programm nur verlangsamen würde.
        return true;
    }

    @Override
    public Types.WINNER getGameWinner() {
        assert isGameOver() : "Game is not yet over!";
        switch (winState) {
            case 1:
                return Types.WINNER.PLAYER_WINS;
            default:
                return Types.WINNER.PLAYER_LOSES;
        }
    }

    @Override
    public double getGameScore() {
        if (isGameOver()) {
            return -1 + (score / MAXSCORE);
        }
        else {
            switch (score) {
                case 0:
                    return 0;
                default:
                    double newScore = score/MAXSCORE;
                  /*  highestTileInCorner = false;
                    for (int i = 0; i < highestTiles.size(); i++) {
                        int row = highestTiles.get(i).getPosition().row;
                        int column = highestTiles.get(i).getPosition().column;
                        if(row == 0 && column == 0 ||
                                row == 0 && column == 3 ||
                                row == 3 && column == 0 ||
                                row == 3 && column == 3
                                ) {
                            highestTileInCorner = true;
                        }
                    }
                    if(highestTileInCorner) {
                        newScore += 1;
                    }*/
                    return newScore;
                }
        }
    }

    @Override
    public double getGameScore(StateObservation referingState) {
        assert (referingState instanceof StateObserver2048) : "referingState is not of class StateObserver2048";
        return this.getGameScore();
    }

    @Override
    public double getMinGameScore() {
        return REWARD_NEGATIVE;
    }

    @Override
    public double getMaxGameScore() {
        return REWARD_POSITIVE;
    }

    @Override
    public void advance(Types.ACTIONS action) {
        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";

        move(iAction);
    }

    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        ArrayList<Types.ACTIONS> availAct = new ArrayList<>();
        for(int viableMove : availableMoves) {
            availAct.add(Types.ACTIONS.fromInt(viableMove));
        }
        return availAct;
    }

    @Override
    public int getNumAvailableActions() {
        return availableMoves.size();
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

    public boolean isLegalAction(Types.ACTIONS action) {
        return availableMoves.contains(action.toInt());
    }

    public int getScore() {
        return score;
    }

    public Tile getTile(int row, int column) {
        return gameBoard[row][column].getTile();
    }

    public void addTile(Position position, int value) {
        emptyTiles.remove(position);
        Tile newTile = new Tile(value, position);
        updateHighestTile(newTile);
        gameBoard[position.row][position.column].setTile(newTile);
    }

    public void mergeTiles(Tile tileOne, Tile tileTwo) {
        tileOne.addValue(tileTwo.getValue());
        score += tileOne.getValue();
        if(tileOne.getValue() >= Config.WINNINGVALUE) {
            setWinState(1);
        }
        updateHighestTile(tileOne);
        tileTwo.getPosition().setTile(null);
        emptyTiles.add(tileTwo.getPosition());
    }

    public void updateHighestTile(Tile newTile) {
        if(highestTiles.size() == 0) {
            highestTiles.add(newTile);
        }
        else if(highestTiles.get(0).getValue() == newTile.getValue()) {
            highestTiles.add(newTile);
        }
        else if(highestTiles.get(0).getValue() < newTile.getValue()) {
            highestTiles.removeAll(highestTiles);
            highestTiles.add(newTile);
        }
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

    public void updateAvailableMoves() {
        availableMoves = new ArrayList<>();

        loop:
        for(int row = 0; row < Config.ROWS; row++) {
            for (int column = 1; column < Config.COLUMNS; column++) {
                if(getTile(row, column) == null){

                }
                else if (getTile(row, column - 1) == null || getTile(row, column).getValue() == getTile(row, column - 1).getValue()) {
                    availableMoves.add(0);
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
                    availableMoves.add(1);
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
                    availableMoves.add(2);
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
                    availableMoves.add(3);
                    break loop;
                }
            }
        }

        if(availableMoves.size() <= 0) {
            setWinState(-1);
        }

        setAvailableActions();
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

        updateAvailableMoves();
    }

    private void left() {
        if(availableMoves.contains(0)) {
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
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void up() {
        if(availableMoves.contains(1)) {
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
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void right() {
        if(availableMoves.contains(2)) {
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
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void down() {
        if(availableMoves.contains(3)) {
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
            System.out.println("availableMoves = " + availableMoves);
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

        updateAvailableMoves();
    }
}
