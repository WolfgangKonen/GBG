package games.ZweiTausendAchtundVierzig;

import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by Johannes on 18.11.2016.
 */
public class StateObserver2048_parallel implements StateObservation{
    private Random random = new Random();
    protected List<Tile> emptyTiles = new ArrayList();
    protected List<Integer> availableMoves;
    private Tile[][] gameBoard;
    protected Types.ACTIONS[] actions;
    ExecutorService executorService;

    // 0 = running, 1 = won, -1 = lost
    private int winState;
    public int score;
    public int highestTileValue = Integer.MIN_VALUE;
    public boolean highestTileInCorner = false;

    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    public double storedMaxScore;

    public final static double MAXSCORE = 3932156;
    public final static double MINSCORE = 0;
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

    public StateObserver2048_parallel() {
        executorService = Executors.newWorkStealingPool();
        newBoard();
    }

    public StateObserver2048_parallel(int[][] values, int score, int winState, ExecutorService executorService) {
        this.executorService = executorService;
        gameBoard = new Tile[Config.ROWS][Config.COLUMNS];
        for(int row = 0; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++) {
                Tile newTile = new Tile(values[row][column], new Position(row,column));
                gameBoard[row][column] = newTile;
                updateHighestTile(newTile);
                if(values[row][column] == 0) {
                    emptyTiles.add(newTile);
                }
            }
        }
        this.score = score;
        this.winState = winState;

        updateAvailableMoves();
    }

    @Override
    public StateObserver2048_parallel copy() {
        return new StateObserver2048_parallel(toArray(), score, winState, executorService);
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
            double penalisation = Config.PENALISATION;

            if(Config.ADDSCORE) {
                penalisation += (score / MAXSCORE);
            }
            return penalisation;
        }
        else {

            highestTileInCorner = false;
            if(gameBoard[0][0].getValue() == highestTileValue || gameBoard[Config.ROWS-1][0].getValue() == highestTileValue || gameBoard[0][Config.COLUMNS-1].getValue() == highestTileValue || gameBoard[Config.ROWS-1][Config.COLUMNS-1].getValue() == highestTileValue) {
                highestTileInCorner = true;
            }

            switch (score) {
                case 0:
                    return 0;
                default:
                    double newScore = score/MAXSCORE;


                    if(highestTileInCorner) {
                        newScore *= Config.HIGHESTTILEINCORENERVALUE;
                    }

                    newScore *= Math.pow(Config.EMPTYTILEVALUE, emptyTiles.size());
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
        return gameBoard[row][column];
    }

    public void addTile(Tile tile, int value) {
        emptyTiles.remove(tile);
        tile.setValue(value);
        updateHighestTile(tile);
    }

    public void mergeTiles(Tile tileOne, Tile tileTwo) {
        tileOne.addValue(tileTwo.getValue());
        score += tileOne.getValue();
        if(tileOne.getValue() >= Config.WINNINGVALUE) {
            setWinState(1);
        }
        updateHighestTile(tileOne);
        tileTwo.setValue(0);
        emptyTiles.add(tileTwo);
    }

    public void updateHighestTile(Tile newTile) {
        if(newTile.getValue() > highestTileValue) {
            highestTileValue = newTile.getValue();
        }
    }

    public void moveTile(Tile oldTile, Tile newTile) {
        emptyTiles.remove(newTile);
        newTile.setValue(oldTile.getValue());

        emptyTiles.add(oldTile);
        oldTile.setValue(0);
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
                newBoard[row][column] = gameBoard[row][column].getValue();
            }
        }
        return newBoard;
    }

    public void updateAvailableMoves() {
        availableMoves = new ArrayList<>();

        List<Callable<Integer>> callables = Arrays.asList(
                () -> {
                    for(int row = 0; row < Config.ROWS; row++) {
                        for (int column = 1; column < Config.COLUMNS; column++) {
                            if(gameBoard[row][column].getValue() != 0) {
                                if (gameBoard[row][column - 1].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row][column - 1].getValue()) {
                                    //availableMoves.add(0);
                                    return 0;
                                }
                            }
                        }
                    }
                    return null;
                },
                () -> {
                    for(int row = 1; row < Config.ROWS; row++) {
                        for (int column = 0; column < Config.COLUMNS; column++) {
                            if(gameBoard[row][column].getValue() != 0) {
                                if (gameBoard[row - 1][column].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row - 1][column].getValue()) {
                                    //availableMoves.add(1);
                                    return 1;
                                }
                            }
                        }
                    }
                    return null;
                },
                () -> {
                    for(int row = 0; row < Config.ROWS; row++) {
                        for (int column = 0; column < Config.COLUMNS-1; column++) {
                            if(gameBoard[row][column].getValue() != 0) {
                                if (gameBoard[row][column + 1].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row][column + 1].getValue()) {
                                    //availableMoves.add(2);
                                    return 2;
                                }
                            }
                        }
                    }
                    return null;
                },
                () -> {
                    for(int row = 0; row < Config.ROWS-1; row++) {
                        for (int column = 0; column < Config.COLUMNS; column++) {
                            if(gameBoard[row][column].getValue() != 0) {
                                if (gameBoard[row+1][column].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row+1][column].getValue()) {
                                    //availableMoves.add(3);
                                    return 3;
                                }
                            }
                        }
                    }
                    return null;
                }
        );

        try {
            executorService.invokeAll(callables).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        }
                        catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }).filter(move -> move != null)
                    .forEach(move -> availableMoves.add(move));
        } catch (InterruptedException ignore) { }


        if(availableMoves.size() <= 0) {
            setWinState(-1);
        }

        setAvailableActions();
    }

    public void printBoard() {
        System.out.println("---------------------------------");
        for(Tile[] row: gameBoard)
        {
            System.out.print("|");
            for(Tile tile: row) {
                if(tile.getValue() != 0) {
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
            Tile tile = emptyTiles.get(random.nextInt(emptyTiles.size()));
            addTile(tile, Config.STARTINGVALUES[random.nextInt(Config.STARTINGVALUES.length)]);
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
                //Feld mit dem gemerged wird oder auf das geschoben wird
                Tile lastTile = getTile(row, 0);

                for (int column = 1; column < Config.COLUMNS; column++) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(row, lastTile.getPosition().getColumn()+1);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(row, lastTile.getPosition().getColumn()+1);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
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

                for (int row = 1; row < Config.ROWS; row++) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(lastTile.getPosition().getRow()+1, column);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(lastTile.getPosition().getRow()+1, column);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
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

                for (int column = Config.COLUMNS - 2; column >= 0; column--) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(row, lastTile.getPosition().getColumn()-1);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(row, lastTile.getPosition().getColumn()-1);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
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

                for (int row = Config.ROWS - 2; row >= 0; row--) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(lastTile.getPosition().getRow()-1, column);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(lastTile.getPosition().getRow()-1, column);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
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
        gameBoard = new Tile[Config.ROWS][Config.COLUMNS];
        for(int row = 0; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++) {
                Tile newTile = new Tile(0, new Position(row,column));
                gameBoard[row][column] = newTile;
                emptyTiles.add(newTile);
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