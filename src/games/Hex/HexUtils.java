package games.Hex;
import com.sun.istack.internal.Nullable;
import tools.Types;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import static java.lang.Math.cos;
import static java.lang.Math.min;

//Hexagon math based on hexmech.java: https://gist.github.com/salamander2/4329783#file-hexmech-java
public class HexUtils {
    private static int OUTLINEWIDTH = 8; //width of the outer border of the game board in px

    public static Polygon createHexPoly(int i, int j, int borders, int boardSize, int polyHeight) {
        if (polyHeight == 0) {
            System.out.println("ERROR: size of hex has not been set");
            return new Polygon();
        }

        int y = getHexY(i, j, polyHeight, boardSize) + borders;
        int x = getHexX(i, j, polyHeight) + borders;

        int r = polyHeight/2;	// radius of inscribed circle
        int t = (int) (r / (Math.sqrt(3)));	// short side of 30 degree triangle outside of each hex
        int s = (int) getSideLengthFromHeight(polyHeight);	// length of one side

        int[] xPoints = new int[] {x+t, x+s+t, x+s+t+t, x+s+t, x+t,   x};
        int[] yPoints = new int[] {y,   y,     y+r,     y+r+r, y+r+r, y+r};
        return new Polygon(xPoints, yPoints, 6);
    }

    public static void drawHex(HexTile tile, Graphics2D g2, boolean highlight) {
        Polygon poly = tile.getPoly();

        Color cellColor = GameBoardHex.COLOR_CELL;
        if (tile.getPlayer() == HexConfig.PLAYER_ONE){
            cellColor = GameBoardHex.COLOR_PLAYER_ONE;
        } else if (tile.getPlayer() == HexConfig.PLAYER_TWO){
            cellColor = GameBoardHex.COLOR_PLAYER_TWO;
        }

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

    public static void drawTileValues(HexTile tile, Graphics2D g2, int boardSize, Color cellColor) {
        int i = tile.getCoords().x;
        int j = tile.getCoords().y;

        Polygon poly = tile.getPoly();
        int polyHeight = getPolyHeight(poly);

        int x = getHexX(i, j, polyHeight);
        int y = getHexY(i, j, polyHeight, boardSize);

        Color textColor = tile.getPlayer() == HexConfig.PLAYER_TWO ? Color.WHITE : Color.BLACK;

        if (cellColor != null) {
            g2.setColor(cellColor);
            g2.fillPolygon(poly);
        }

        g2.setColor(textColor);
        double tileValue = tile.getValue();
        String tileText = "";


        tileText = String.format("%.2f", tileValue);

        int width = g2.getFontMetrics().stringWidth(tileText);
        int height = g2.getFontMetrics().getHeight();

        int textX = (int) (x + (getSideLengthFromHeight(polyHeight) * 1.5) - (width / 2));
        int textY = y + (polyHeight / 2) + (height);

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

        int red =   Math.round(colorBad.getRed()   * inverse_percentage + colorGood.getRed()   * percentage);
        int blue =  Math.round(colorBad.getBlue()  * inverse_percentage + colorGood.getBlue()  * percentage);
        int green = Math.round(colorBad.getGreen() * inverse_percentage + colorGood.getGreen() * percentage);

        return new Color(red, green, blue, 255);
    }


    public static int getHexX(int i, int j, int polyHeight){
        return (int) ((i+j) * (polyHeight - (polyHeight * cos(30))));
    }

    public static int getHexY(int i, int j, int polyHeight, int boardSize){
        return (int) ((j-i) * (polyHeight/2)) + (boardSize * (polyHeight/2));
    }

    public static void drawOutlines(int boardSize, Color colorOne, Color colorTwo, Graphics2D g2, HexTile[][] board){
        Stroke strokeFill = new BasicStroke(1);
        Stroke strokeDraw = new BasicStroke(2);

        //Get polygon height from one of the polygons
        int polyHeight = 0;
        if (board.length > 0 && board[0].length > 0){
            polyHeight = getPolyHeight(board[0][0].getPoly());
        }

        int sHalf = Math.round((int)getSideLengthFromHeight(polyHeight) / 2);
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
            g2.setColor(colorOne);
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
            g2.setColor(colorOne);
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
            g2.setColor(colorTwo);
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
            g2.setColor(colorTwo);
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

    private static int isNextToEdge(HexTile tile){
        int player = tile.getPlayer();
        int x = tile.getCoords().x;
        int y = tile.getCoords().y;

        if (player == HexConfig.PLAYER_ONE){
            if (x == 0){
                return 1;
            } else if (x == HexConfig.BOARD_SIZE-1){
                return 2;
            }
        } else {
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

    private static boolean isValidTile(int x, int y) {
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
        return (height / 2) / (Math.sqrt(3)/2);
    }
}
