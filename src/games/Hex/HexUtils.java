package games.Hex;
import com.sun.istack.internal.Nullable;
import tools.Types;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import static games.Hex.HexConfig.PLAYER_NONE;
import static games.Hex.HexConfig.PLAYER_ONE;
import static games.Hex.HexConfig.PLAYER_TWO;
import static java.lang.Math.cos;
import static java.lang.Math.min;

//Hexagon math based on hexmech.java: https://gist.github.com/salamander2/4329783#file-hexmech-java
public class HexUtils {
    private static int OUTLINEWIDTH = HexConfig.HEX_SIZE/6; //width of the outer border of the game board in px

    public static Polygon createHexPoly(int i, int j, int borders, int boardSize, int polyHeight) {
        if (polyHeight == 0) {
            System.out.println("ERROR: size of hex has not been set");
            return new Polygon();
        }

        int y = getHexY(i, j, polyHeight, boardSize) + borders;
        int x = getHexX(i, j, polyHeight) + borders*2;

        double r = polyHeight/2f;	// radius of inscribed circle
        double t = (r / (Math.sqrt(3)));	// short side of 30 degree triangle outside of each hex
        double s = getSideLengthFromHeight(polyHeight);	// length of one side

        int[] xPoints = new int[] {(int) Math.round(x+t),     (int) Math.round(x+s+t),
                                   (int) Math.round(x+s+t+t), (int) Math.round(x+s+t),
                                   (int) Math.round(x+t),     x};

        int[] yPoints = new int[] {y,                         y,
                                   (int) Math.round(y+r),     (int) Math.round(y+r+r),
                                   (int) Math.round(y+r+r),   (int) Math.round(y+r)};

        return new Polygon(xPoints, yPoints, 6);
    }

    public static void drawHex(HexTile tile, Graphics2D g2, Color cellColor, boolean highlight) {
        Polygon poly = tile.getPoly();

        g2.setColor(cellColor);
        g2.fillPolygon(poly);

        if (highlight) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.RED);
        } else {
            g2.setColor(GameBoardHex.COLOR_GRID);
        }

        g2.drawPolygon(poly);
    }

    public static void drawTileValueText(HexTile tile, Graphics2D g2, int boardSize) {
        double tileValue = tile.getValue();
        if (Double.isNaN(tileValue)){
            return;
        }

        int i = tile.getCoords().x;
        int j = tile.getCoords().y;

        Polygon poly = tile.getPoly();
        int polyHeight = getPolyHeight(poly);

        int x = getHexX(i, j, polyHeight)+HexConfig.OFFSET;
        int y = getHexY(i, j, polyHeight, boardSize);

        Color textColor = tile.getPlayer() == PLAYER_ONE ? Color.WHITE : Color.BLACK;

        g2.setColor(textColor);

        String tileText = String.format("%.2f", tileValue);

        int width = g2.getFontMetrics().stringWidth(tileText);
        int height = g2.getFontMetrics().getHeight();

        int textX = (int) (x + (getSideLengthFromHeight(polyHeight) * 1.5) - (width / 2f));
        int textY = (int) (y + (polyHeight / 2f) + (height));

        g2.drawString("" + tileText, textX, textY);
    }

    public static Color calculateTileColor(double tileValue){
        double minVal = -1;
        double maxVal = 1;
        double difference = maxVal-minVal;
        float percentage = (float) ((tileValue-minVal)/difference);
        float inverse_percentage = 1-percentage;

        Color colorBad = Color.RED;
        Color colorGood = Color.GREEN;

        int red =   Math.min(Math.max(Math.round(colorBad.getRed()   * inverse_percentage
                + colorGood.getRed()   * percentage), 0), 255);
        int blue =  Math.min(Math.max(Math.round(colorBad.getBlue()  * inverse_percentage
                + colorGood.getBlue()  * percentage), 0), 255);
        int green = Math.min(Math.max(Math.round(colorBad.getGreen() * inverse_percentage
                + colorGood.getGreen() * percentage), 0), 255);

        return new Color(red, green, blue, 255);
    }


    public static int getHexX(int i, int j, int polyHeight){
        return (int) ((i+j) * (polyHeight - (polyHeight * cos(30))));
    }

    public static int getHexY(int i, int j, int polyHeight, int boardSize){
        return (int) (((j-i) * (polyHeight/2f)) + (boardSize * (polyHeight/2f)));
    }

    public static void drawOutlines(int boardSize, Color colorOne, Color colorTwo, Graphics2D g2, HexTile[][] board){
        Stroke strokeFill = new BasicStroke(1);
        Stroke strokeDraw = new BasicStroke(2);

        //Get polygon height from one of the polygons
        int polyHeight = 0;
        if (board.length > 0 && board[0].length > 0){
            polyHeight = getPolyHeight(board[0][0].getPoly());
        }

        int sHalf = Math.round((int)getSideLengthFromHeight(polyHeight) / 2f);
        int offset = (int) Math.round(OUTLINEWIDTH *0.6);

        for (int n=0; n<boardSize; n++){
            Polygon newPoly;
            Polygon currentHex;

            //Bottom left corner
            currentHex = board[0][n].getPoly();
            if (n==0) {
                int[] cx = new int[]{currentHex.xpoints[5] - OUTLINEWIDTH, currentHex.xpoints[5],
                        currentHex.xpoints[4], currentHex.xpoints[4] + sHalf,
                        currentHex.xpoints[4] + sHalf, currentHex.xpoints[4] - offset};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[5],
                        currentHex.ypoints[4], currentHex.ypoints[4], currentHex.ypoints[4] + OUTLINEWIDTH,
                        currentHex.ypoints[4] + OUTLINEWIDTH, currentHex.ypoints[4]};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[5]-sHalf, currentHex.xpoints[5],
                        currentHex.xpoints[4], currentHex.xpoints[4] + sHalf, currentHex.xpoints[4] + sHalf,
                        currentHex.xpoints[4] - offset, currentHex.xpoints[5] - offset, currentHex.xpoints[5]-sHalf};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[5], currentHex.ypoints[4],
                        currentHex.ypoints[4], currentHex.ypoints[4] + OUTLINEWIDTH,
                        currentHex.ypoints[4] + OUTLINEWIDTH, currentHex.ypoints[5]+ OUTLINEWIDTH,
                        currentHex.ypoints[5]+ OUTLINEWIDTH};
                newPoly = new Polygon(cx, cy, cx.length);
            }
            /*g2.setStroke(strokeDraw);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(newPoly);*/

            g2.setStroke(strokeFill);
            g2.setColor(colorTwo);
            g2.fillPolygon(newPoly);


            //Top right corner
            currentHex = board[boardSize-1][n].getPoly();
            if (n==boardSize-1) {
                int[] cx = new int[]{currentHex.xpoints[0]+sHalf, currentHex.xpoints[1]+offset,
                        currentHex.xpoints[2]+ OUTLINEWIDTH, currentHex.xpoints[2], currentHex.xpoints[1],
                        currentHex.xpoints[0]+sHalf};
                int[] cy = new int[]{currentHex.ypoints[0]- OUTLINEWIDTH, currentHex.ypoints[1]- OUTLINEWIDTH,
                        currentHex.ypoints[2], currentHex.ypoints[2], currentHex.ypoints[1], currentHex.ypoints[0]};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[0]+sHalf, currentHex.xpoints[1]+offset,
                        currentHex.xpoints[2]+offset, currentHex.xpoints[2]+sHalf, currentHex.xpoints[2]+sHalf,
                        currentHex.xpoints[2], currentHex.xpoints[1], currentHex.xpoints[0]+sHalf};
                int[] cy = new int[]{currentHex.ypoints[0]- OUTLINEWIDTH, currentHex.ypoints[1]- OUTLINEWIDTH,
                        currentHex.ypoints[2]- OUTLINEWIDTH, currentHex.ypoints[2]- OUTLINEWIDTH, currentHex.ypoints[2],
                        currentHex.ypoints[2], currentHex.ypoints[1], currentHex.ypoints[0]};
                newPoly = new Polygon(cx, cy, cx.length);
            }
            /*g2.setStroke(strokeDraw);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(newPoly);*/

            g2.setStroke(strokeFill);
            g2.setColor(colorTwo);
            g2.fillPolygon(newPoly);


            //Top left corner
            currentHex = board[n][0].getPoly();
            if (n==0) {
                int[] cx = new int[]{currentHex.xpoints[5], currentHex.xpoints[0], currentHex.xpoints[0]+sHalf,
                        currentHex.xpoints[0]+sHalf, currentHex.xpoints[0]-offset, currentHex.xpoints[5]- OUTLINEWIDTH};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[0], currentHex.ypoints[0],
                        currentHex.ypoints[0]- OUTLINEWIDTH, currentHex.ypoints[0]- OUTLINEWIDTH,
                        currentHex.ypoints[5]};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[5], currentHex.xpoints[0], currentHex.xpoints[0]+sHalf,
                        currentHex.xpoints[0]+sHalf, currentHex.xpoints[0]-offset, currentHex.xpoints[5]-offset,
                        currentHex.xpoints[5]-sHalf, currentHex.xpoints[5]-sHalf};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[0], currentHex.ypoints[0],
                        currentHex.ypoints[0]- OUTLINEWIDTH, currentHex.ypoints[0]- OUTLINEWIDTH,
                        currentHex.ypoints[5]- OUTLINEWIDTH, currentHex.ypoints[5]- OUTLINEWIDTH,
                        currentHex.ypoints[5]};
                newPoly = new Polygon(cx, cy, cx.length);
            }
            /*g2.setStroke(strokeDraw);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(newPoly);*/

            g2.setStroke(strokeFill);
            g2.setColor(colorOne);
            g2.fillPolygon(newPoly);

            //Bottom right corner
            currentHex = board[n][boardSize-1].getPoly();
            if (n==(boardSize-1)) {
                int[] cx = new int[]{currentHex.xpoints[3]-sHalf, currentHex.xpoints[3], currentHex.xpoints[2],
                        currentHex.xpoints[2]+ OUTLINEWIDTH, currentHex.xpoints[3]+offset, currentHex.xpoints[3]-sHalf};
                int[] cy = new int[]{currentHex.ypoints[3], currentHex.ypoints[3], currentHex.ypoints[2],
                        currentHex.ypoints[2], currentHex.ypoints[3]+ OUTLINEWIDTH,
                        currentHex.ypoints[3]+ OUTLINEWIDTH};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[3]-sHalf, currentHex.xpoints[3], currentHex.xpoints[2],
                        currentHex.xpoints[2]+sHalf, currentHex.xpoints[2]+sHalf, currentHex.xpoints[2]+offset,
                        currentHex.xpoints[3]+offset, currentHex.xpoints[3]-sHalf};
                int[] cy = new int[]{currentHex.ypoints[3], currentHex.ypoints[3], currentHex.ypoints[2],
                        currentHex.ypoints[2], currentHex.ypoints[2]+ OUTLINEWIDTH, currentHex.ypoints[2]+ OUTLINEWIDTH,
                        currentHex.ypoints[3]+ OUTLINEWIDTH, currentHex.ypoints[3]+ OUTLINEWIDTH};
                newPoly = new Polygon(cx, cy, cx.length);
            }
            /*g2.setStroke(strokeDraw);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(newPoly);*/

            g2.setStroke(strokeFill);
            g2.setColor(colorOne);
            g2.fillPolygon(newPoly);
        }
    }

    public static Point pxtoHex(int mx, int my, HexTile[][] board, int boardSize) {
        Point p = new Point(-1,-1);
        Point coords = new Point(mx, my);

        for (int i=0; i<boardSize; i++){
            for (int j=0; j<boardSize; j++){
                if (board[i][j].getPoly().contains(coords)){
                    p.x = i;
                    p.y = j;
                }
            }
        }

        return p;
    }

    @Nullable
    public static Types.WINNER getWinner(HexTile[][] board, HexTile lastPlacedTile){
        if (lastPlacedTile == null){
            //System.out.println("lastPlacedTile was null");
            return null;
        }
        int player = lastPlacedTile.getPlayer();
        LinkedList<HexTile> tilesToVisit = new LinkedList<>();
        ArrayList<HexTile> visitedTiles = new ArrayList<>();

        boolean firstEdgeReached = false;
        boolean secondEdgeReached = false;

        tilesToVisit.add(lastPlacedTile);

        while(tilesToVisit.size() > 0){
            HexTile currentTile = tilesToVisit.pop();
            //Add all neighbors that belong to the player and have not been visited
            ArrayList<HexTile> adjacentTiles = getAdjacentTiles(board, currentTile);
            for (HexTile currentNeighbor: adjacentTiles){
                if (currentNeighbor.getPlayer() == player &&
                        !tilesToVisit.contains(currentNeighbor) &&
                        !visitedTiles.contains(currentNeighbor)){
                    tilesToVisit.add(currentNeighbor);
                }
            }

            //Check if the tile is next to an edge belonging to the player
            switch (isNextToEdge(currentTile)){
                case 1:
                    firstEdgeReached = true;
                    break;
                case 2:
                    secondEdgeReached = true;
                    break;
            }

            if (firstEdgeReached && secondEdgeReached){
                return Types.WINNER.PLAYER_WINS;
            }

            visitedTiles.add(currentTile);
        }

        return null;
    }

    public static int[] getLongestChain(HexTile[][] board, int player){
        LinkedList<HexTile> tilesToVisit = new LinkedList<>();
        ArrayList<HexTile> visitedTiles = new ArrayList<>();

        int longestChain = 0;
        int neighborCount = 0;

        for (int i=0; i<HexConfig.BOARD_SIZE; i++){
            for (int j=0; j<HexConfig.BOARD_SIZE; j++){
                if (!visitedTiles.contains(board[i][j]) && board[i][j].getPlayer() == player){
                    tilesToVisit.add(board[i][j]);
                }

                int currentChainLength = 0;
                int currentChainMin = Integer.MAX_VALUE;
                int currentChainMax = Integer.MIN_VALUE;
                while(tilesToVisit.size() > 0){
                    HexTile currentTile = tilesToVisit.pop();
                    ArrayList<HexTile> adjacentTiles = getAdjacentTiles(board, currentTile);
                    for (HexTile currentNeighbor: adjacentTiles){
                        if (currentTile.getPlayer() == player && currentNeighbor.getPlayer() == PLAYER_NONE){
                            //Add currentNeighbor to count of neighboring free tiles
                            neighborCount += 1;
                        }
                        //Check if the tile has not been visited and is not yet marked to be visited...
                        if (!tilesToVisit.contains(currentNeighbor) &&
                                !visitedTiles.contains(currentNeighbor)) {
                            //If the tile belongs to the player, add it directly
                            if (currentNeighbor.getPlayer() == player) {
                                tilesToVisit.add(currentNeighbor);
                            } else if (currentNeighbor.getPlayer() == PLAYER_NONE) {
                                //If the tile does not belong to a player, check if there is an additional free tile
                                //where both free tiles connect the same two tiles that belong to the player
                                /*Example (X: tile of player; ?: free tile):
                                     ?
                                   /   \
                                  X     X
                                   \   /
                                     ?

                                 Also applies if one X is replaced by an edge with the same color as the player
                                 */
                                ArrayList<HexTile> neighborsNeighbors = getAdjacentTiles(board, currentNeighbor);
                                for (HexTile currentNeighborsNeighbor : neighborsNeighbors) {
                                    if (!currentNeighborsNeighbor.equals(currentTile)
                                            && adjacentTiles.contains(currentNeighborsNeighbor)
                                            &&currentNeighborsNeighbor.getPlayer() == PLAYER_NONE) {
                                        //If this evaluates to true, the tile is adjacent to currentTile AND
                                        //currentNeighbor AND does not belong to a player yet

                                        //Check if both free tiles are connected to an edge that belongs to the player
                                        if (isNextToEdge(currentNeighbor, player) > 0
                                                && isNextToEdge(currentNeighborsNeighbor, player) > 0){
                                            tilesToVisit.add(currentNeighbor);
                                            tilesToVisit.add(currentNeighborsNeighbor);
                                        } else {
                                            //Find a tile that is adjacent to both neighbors and also belongs
                                            //to the player, if such a tile exists
                                            ArrayList<HexTile> secondNeighborsAdjacentTiles =
                                                    getAdjacentTiles(board, currentNeighborsNeighbor);
                                            for (HexTile secondNeighborsNeighbor : secondNeighborsAdjacentTiles) {
                                                if (neighborsNeighbors.contains(secondNeighborsNeighbor)
                                                        && !secondNeighborsNeighbor.equals(currentTile)
                                                        && secondNeighborsNeighbor.getPlayer() == player) {
                                                    //If such a tile was found, add the two free adjacent tiles that
                                                    //connect the two separate chains.
                                                    //This treats the two chains as a single chain
                                                    tilesToVisit.add(currentNeighbor);
                                                    tilesToVisit.add(currentNeighborsNeighbor);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    if (player == HexConfig.PLAYER_TWO){
                        //For player two, x direction matters
                        currentChainMin = Math.min(currentTile.getCoords().x, currentChainMin);
                        currentChainMax = Math.max(currentTile.getCoords().x, currentChainMax);
                    } else if (player == PLAYER_ONE){
                        //For player one, y direction matters
                        currentChainMin = Math.min(currentTile.getCoords().y, currentChainMin);
                        currentChainMax = Math.max(currentTile.getCoords().y, currentChainMax);
                    }

                    currentChainLength = Math.max(currentChainLength, (currentChainMax-currentChainMin)+1);

                    visitedTiles.add(currentTile);
                }
                longestChain = Math.max(longestChain, currentChainLength);
            }
        }

        int [] rVal = new int[2];
        rVal[0] = longestChain;
        rVal[1] = neighborCount;

        return rVal;
    }

    private static int isNextToEdge(HexTile tile){
        int player = tile.getPlayer();
        return isNextToEdge(tile, player);
    }

    /**
     *
     * @param tile The tile to check
     * @param player The player whose edge is to be checked. Optional parameter, if missing,
     *               the player who the tile belongs to will be subject of the comparison.
     * @return 0, 1 or 2 -- 0 means no edge, 1 is the first edge, 2 is the second.
     */
    private static int isNextToEdge(HexTile tile, int player){
        int x = tile.getCoords().x;
        int y = tile.getCoords().y;

        if (player == HexConfig.PLAYER_TWO){
            if (x == 0){
                return 1;
            } else if (x == HexConfig.BOARD_SIZE-1){
                return 2;
            }
        } else if (player == PLAYER_ONE){
            if (y == 0){
                return 1;
            } else if (y == HexConfig.BOARD_SIZE-1){
                return 2;
            }
        }

        return 0;
    }

    private static ArrayList<HexTile> getAdjacentTiles(HexTile[][] board, HexTile tile){
        ArrayList<HexTile> adjacentTiles = new ArrayList<>();
        int x = tile.getCoords().x;
        int y = tile.getCoords().y;

        for (int i = -1; i<=1; i++){
            for (int j = -1; j<=1; j++){
                int neighborX = x+i;
                int neighborY = y+j;
                if (i != j && isValidTile(neighborX, neighborY)){
                    adjacentTiles.add(board[neighborX][neighborY]);
                }
            }
        }

        return adjacentTiles;
    }

    public static boolean isValidTile(int x, int y) {
        return (x >= 0 && x < HexConfig.BOARD_SIZE) && (y >= 0 && y < HexConfig.BOARD_SIZE);
    }

    static int getPolyHeight(Polygon poly){
        if (poly.xpoints.length == 6) {
            return poly.ypoints[4] - poly.ypoints[0];
        } else {
            return 0;
        }
    }

    static double getSideLengthFromHeight(int height){
        return ((float)height / 2f) / (Math.sqrt(3)/2f);
    }

    static HexTile[] boardToVector(HexTile[][] board){
        HexTile[] boardVector = new HexTile[board.length*board.length];

        for (int i=0, k=0; i<HexConfig.BOARD_SIZE; i++){
            for (int j=0; j<HexConfig.BOARD_SIZE; j++, k++){
                //No player: 0, player one: 1, player two: 2
                boardVector[k] = board[i][j];
            }
        }
        return boardVector;
    }

    static int getOpponent(int player){
        if (player == PLAYER_ONE){
            return PLAYER_TWO;
        } else {
            return PLAYER_ONE;
        }
    }
}
