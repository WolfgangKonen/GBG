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
     * @param gb       The game board
     * @param lastTile The last played tile
     * @return {@link Types.WINNER#PLAYER_WINS} on 4 tile lines or
     *         {@link Types.WINNER#PLAYER_LOSES} on 3 tile lines
     */
    public static Types.WINNER getWinner(TileYavalath[][] gb, TileYavalath lastTile){
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
