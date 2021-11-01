package games.Yavalath;

import tools.Types;

import java.awt.*;
import java.util.ArrayList;

/**
 * The type Utility functions yavalath.
 */
public class UtilityFunctionsYavalath {


    /**
     * Returns a value for losing or winning from the perspective of the last played tile.
     * Does this by checking if it has any neighbours that can produce a line.
     *
     * @param so       The StateObserver
     * @return {@link Types.WINNER#PLAYER_WINS} on 4 tile lines or
     *         {@link Types.WINNER#PLAYER_LOSES} on 3 tile lines
     */
    public static Types.WINNER getWinner(StateObserverYavalath so){
        TileYavalath[][] gb = so.getGameBoard();
        TileYavalath lastTile = so.getMoveList().get(0);

        if (lastTile==null) return null;
        int player = lastTile.getPlayer();

        ArrayList<TileYavalath> neighbours = getNeighbours(gb, lastTile);

        //No occupied tile in the neighbouring tile's means there can't be a winner
        if(!occupiedTileInNeighbours(neighbours)) return null;
        ArrayList<TileYavalath> playersNeighbouringTiles = new ArrayList<>();

        for(TileYavalath x : neighbours){
            if(x.getPlayer() == player) playersNeighbouringTiles.add(x);
        }
        int tileX = lastTile.getX();
        int tileY = lastTile.getY();

        int linelength; //Original tile + neighbour = always starts at 2


        for(TileYavalath x : playersNeighbouringTiles) {
            linelength = 2;
            int neighbourX = x.getX();
            int neighbourY = x.getY();

            int xDifference = tileX - neighbourX;
            int yDifference = tileY - neighbourY;

            //Check two tiles in direction of neighbour
            for (int i = 1; i < 3; i++) {
                int potentialTileX = neighbourX - i * xDifference;
                int potentialTileY = neighbourY - i * yDifference;
                if (isTileValid(potentialTileX, potentialTileY, gb)) {
                    if (gb[potentialTileX][potentialTileY].getPlayer() == player) {
                        linelength++;
                    } else break;
                }
            }

            //Check two tiles in the opposite direction from the neighbour
            for (int i = 1; i < 3; i++) {
                int potentialTileX = tileX + i * xDifference;
                int potentialTileY = tileY + i * yDifference;
                if (isTileValid(potentialTileX, potentialTileY, gb)) {
                    if (gb[potentialTileX][potentialTileY].getPlayer() == player) {
                        linelength++;
                    } else break;
                }

            }

            //While they don't really make much sense, lines of length 5 exist and will win the game
            if (linelength >= 4) {
                //System.out.println(player + " wins");
                return Types.WINNER.PLAYER_WINS;
            } else if (linelength == 3) {
                //System.out.println(player + " loses");
                return Types.WINNER.PLAYER_LOSES;
            }
        }
        if(so.getNumAvailableActions()==0) {
            return Types.WINNER.TIE;
        }
        return null;
    }

    /**
     * Checks if any of the tiles in the list is occupied by a player.
     *
     * @param neighbours    a list of tiles
     */
    private static boolean occupiedTileInNeighbours(ArrayList<TileYavalath> neighbours){
        for(TileYavalath x : neighbours){
            if(!(x.getPlayer() == ConfigYavalath.EMPTY)){
                return true;
            }
        }
        return false;
    }


    /**
     *  Creates a list of valid tiles that neighbour the last placed tile.
     *
     * @param board     The game board
     * @param lastTile  The last placed tile
     * @return  A list of tiles that neighbour the last placed tile
     */
    private static ArrayList<TileYavalath> getNeighbours(TileYavalath[][] board, TileYavalath lastTile){

        ArrayList<TileYavalath> neighbours = new ArrayList<>();
        int i = lastTile.getX();
        int j = lastTile.getY();

        if(isTileValid(i-1,j,board)) neighbours.add(board[i-1][j]);
        if(isTileValid(i-1,j-1,board)) neighbours.add(board[i-1][j-1]);
        if(isTileValid(i,j-1,board)) neighbours.add(board[i][j-1]);
        if(isTileValid(i,j+1,board)) neighbours.add(board[i][j+1]);
        if(isTileValid(i+1,j,board)) neighbours.add(board[i+1][j]);
        if(isTileValid(i+1,j+1,board)) neighbours.add(board[i+1][j+1]);

        return neighbours;
    }

    /**
     * Checks if the tile on the coordinates x,y is actually on the game board and a valid field
     * that a tile can be placed on.
     *
     * @param i x-coordinate of a tile
     * @param j y-coordinate of a tile
     * @param board The game board
     */
    private static boolean isTileValid(int i, int j, TileYavalath[][] board){
        if((i >= 0 && i <ConfigYavalath.BOARD_SIZE) && (j >= 0 && j < ConfigYavalath.BOARD_SIZE)){
            return board[i][j].getPlayer() != ConfigYavalath.INVALID_FIELD;
        }else return false;
    }


    /**
     * Checks if the next player can win on his turn.
     * @param so The state-observer
     * @return The action that would lead to the win
     */
    public static Types.ACTIONS nextPlayerCanWin(StateObserverYavalath so){

        TileYavalath[][] gb = so.getGameBoard();

        //Need to call nextPlayer twice because it hasnt been called from advance yet
        TileYavalath tileToUse = so.getLastTileFromPlayer(so.getNextPlayerFrom(so.getNextPlayer()));

        if(tileToUse == null) return null;

        int playerToUse = tileToUse.getPlayer();

        ArrayList<TileYavalath> neighbours = getNeighbours(gb,tileToUse);

        int tileToUseX = tileToUse.getX();
        int tileToUseY = tileToUse.getY();

        for(TileYavalath neighbour : neighbours){

            int neighbourX = neighbour.getX();
            int neighbourY = neighbour.getY();

            int deltaX = tileToUseX - neighbourX;
            int deltaY = tileToUseY - neighbourY;

            //For neighbours that belong to the same player check if there is a single tile in that direction or behind with one free space in between
            if(neighbour.getPlayer() == playerToUse) {

                //Extrapolate the tile in the direction of the neighbour and the opposite direction
                int extrapolateX = neighbourX - 2 * deltaX;
                int extrapolateY = neighbourY - 2 * deltaY;

                int extrapolateOppositeX = tileToUseX + 2 * deltaX;
                int extrapolateOppositeY = tileToUseY + 2 * deltaY;

                //Check if there is a valid tile and if the player is the same
                if (isTileValid(extrapolateX, extrapolateY, gb)) {
                    if (gb[extrapolateX][extrapolateY].getPlayer() == playerToUse) {
                        int threateningTileX = neighbourX - deltaX;
                        int threateningTileY = neighbourY - deltaY;
                        //Check if the threatening tile is free
                        if(gb[threateningTileX][threateningTileY].getPlayer() == ConfigYavalath.EMPTY){
                            gb[threateningTileX][threateningTileY].setThreateningMove(true);
                            return new Types.ACTIONS(threateningTileX * ConfigYavalath.BOARD_SIZE + threateningTileY);
                        }

                    }
                }

                if (isTileValid(extrapolateOppositeX, extrapolateOppositeY, gb)) {
                    if (gb[extrapolateOppositeX][extrapolateOppositeY].getPlayer() == playerToUse) {
                        int threateningTileX = tileToUseX + deltaX;
                        int threateningTileY = tileToUseY + deltaY;
                        if(gb[threateningTileX][threateningTileY].getPlayer() == ConfigYavalath.EMPTY){
                            gb[threateningTileX][threateningTileY].setThreateningMove(true);
                            return new Types.ACTIONS(threateningTileX * ConfigYavalath.BOARD_SIZE + threateningTileY);
                        }
                    }
                }
                //If the neighbour is an empty space check if there's a 2 tile line behind it
            } else if(neighbour.getPlayer() == ConfigYavalath.EMPTY){
                int length = 0;
                for (int i = 1; i < 3; i++) {
                    int x = neighbourX - i * deltaX;
                    int y = neighbourY - i * deltaY;

                    if(isTileValid(x,y,gb)){
                        if(gb[x][y].getPlayer() == playerToUse){
                            length++;
                        } else break;
                    }

                }
                if (length == 2) {
                    gb[neighbourX][neighbourY].setThreateningMove(true);
                    return new Types.ACTIONS(neighbourX*ConfigYavalath.BOARD_SIZE + neighbourY);
                }
            }
        }
        //All other tiles belong to an enemy, dont need to check for those
        return null;
    }

    /**
     * Checks if the point clicked by the user is in the polygon of one of the game board tiles.
     * Returns that tile if true.
     *
     * @param clickPoint the click point
     * @param board      The game board
     * @return The clicked tile or null if none was clicked.
     */
    //@TODO Edge-case what happens if the border of 2 or more tiles was clicked? Does that need to be considered
    public static TileYavalath clickedTile(Point clickPoint, TileYavalath[][] board){

        Polygon tilePoly;

        for (TileYavalath[] tileRow:board) {
            for (TileYavalath tile:tileRow) {
                if(tile.getPlayer()!=ConfigYavalath.INVALID_FIELD){
                    tilePoly = tile.getPoly();
                    if(tilePoly.contains(clickPoint))return tile;
                }
            }
        }
        return null;
    }

    /**
     * Copies the game board.
     *
     * @param board The game board
     * @return The new copied game board
     */
    protected static TileYavalath[][] copyBoard(TileYavalath[][] board){
        TileYavalath[][] tempBoard = new TileYavalath[ConfigYavalath.BOARD_SIZE][ConfigYavalath.BOARD_SIZE];
        for(int i=0;i<ConfigYavalath.BOARD_SIZE;i++){
            for(int j=0;j<ConfigYavalath.BOARD_SIZE;j++){
                tempBoard[i][j] = board[i][j].copyTile();
            }
        }
        return tempBoard;
    }

}
