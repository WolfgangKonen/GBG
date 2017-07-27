package games.Hex;

import tools.Types;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import static games.Hex.HexConfig.*;
import static java.lang.Math.cos;

//Hexagon math based on hexmech.java: https://gist.github.com/salamander2/4329783#file-hexmech-java
public class HexUtils {

    /**
     * Creates a polygon object for the specified tile
     *
     * @param i          first index
     * @param j          second index
     * @param borders    top and left margin/offset
     * @param boardSize  length of one side of the game board in tiles
     * @param polyHeight size in px of each hex side to side
     * @return Polygon for specified tile
     */
    public static Polygon createHexPoly(int i, int j, int borders, int boardSize, int polyHeight) {
        int y = getHexY(i, j, polyHeight, boardSize) + borders;
        int x = getHexX(i, j, polyHeight) + borders * 2;

        double r = polyHeight / 2f;    // radius of inscribed circle
        double t = (r / (Math.sqrt(3)));    // short side of 30 degree triangle outside of each hex
        double s = getSideLengthFromHeight(polyHeight);    // length of one side

        int[] xPoints = new int[]{(int) Math.round(x + t), (int) Math.round(x + s + t),
                (int) Math.round(x + s + t + t), (int) Math.round(x + s + t),
                (int) Math.round(x + t), x};

        int[] yPoints = new int[]{y, y,
                (int) Math.round(y + r), (int) Math.round(y + r + r),
                (int) Math.round(y + r + r), (int) Math.round(y + r)};

        return new Polygon(xPoints, yPoints, 6);
    }

    /**
     * Draws a single hex tile
     *
     * @param tile      Tile to be drawn
     * @param g2        Graphics context
     * @param cellColor Color to draw the tile in
     * @param highlight If a red border surrounding the tile should be drawn
     */
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

    /**
     * Draws the tile value on top of the tile.
     * Tile value is transformed from [-1, +1] to [-1000, +1000].
     * Text color is calculated using luminosity function from: https://en.wikipedia.org/wiki/Relative_luminance
     * Text color function has been adjusted for better readability on deep red tiles.
     * Text is always exactly centered in the tile.
     *
     * @param tile      Tile for which value is to be drawn
     * @param g2        Graphics context
     * @param cellColor Color of cell, needed for text color calculation
     * @param boardSize Size of the board in tiles (one side)
     */
    public static void drawTileValueText(HexTile tile, Graphics2D g2, Color cellColor, int boardSize) {
        double tileValue = tile.getValue();
        if (Double.isNaN(tileValue)) {
            return;
        }

        int i = tile.getCoords().x;
        int j = tile.getCoords().y;

        Polygon poly = tile.getPoly();
        int polyHeight = getPolyHeight(poly);

        int x = getHexX(i, j, polyHeight) + HexConfig.OFFSET;
        int y = getHexY(i, j, polyHeight, boardSize);

        int luminance = (int) (0.8 * cellColor.getRed() + 0.7152 * cellColor.getGreen() + 0.0722 * cellColor.getBlue());
        int luminance_inverse = Math.max(255 - luminance, 0);
        Color textColor = new Color(luminance_inverse, luminance_inverse, luminance_inverse, 255);

        g2.setColor(textColor);

        String tileText = Long.toString(Math.round(tileValue * 1000));

        int width = g2.getFontMetrics().stringWidth(tileText);
        int height = g2.getFontMetrics().getHeight();

        int textX = (int) (x + (getSideLengthFromHeight(polyHeight) * 1.5) - (width / 2f));
        int textY = (int) (y + (polyHeight / 2f) + (height));

        g2.drawString(tileText, textX, textY);
    }

    /**
     * Calculate the color for a specific tile value.
     * Uses three color stops: Red for value -1, Yellow for value 0, Green for value +1.
     * Colors for values between -1 and 0 are interpolated between red and yellow.
     * Colors for values between 0 and +1 are interpolated between yellow and green.
     *
     * @param tileValue Value of the tile
     * @return Color the tile is supposed to be drawn in
     */
    public static Color calculateTileColor(double tileValue) {
        float percentage = (float) Math.abs(tileValue);
        float inverse_percentage = 1 - percentage;

        Color colorLow;
        Color colorHigh;
        Color colorNeutral = Color.YELLOW;
        int red, blue, green;

        if (tileValue < 0) {
            colorLow = colorNeutral;
            colorHigh = Color.RED;
        } else {
            colorLow = colorNeutral;
            colorHigh = Color.GREEN;
        }

        red = Math.min(Math.max(Math.round(colorLow.getRed() * inverse_percentage
                + colorHigh.getRed() * percentage), 0), 255);
        blue = Math.min(Math.max(Math.round(colorLow.getBlue() * inverse_percentage
                + colorHigh.getBlue() * percentage), 0), 255);
        green = Math.min(Math.max(Math.round(colorLow.getGreen() * inverse_percentage
                + colorHigh.getGreen() * percentage), 0), 255);

        return new Color(red, green, blue, 255);
    }

    /**
     * Calculate X offset in px of the tile inside the window
     *
     * @param i          first index of tile
     * @param j          second index of tile
     * @param polyHeight Height of each tile in px
     * @return X offset of tile
     */
    public static int getHexX(int i, int j, int polyHeight) {
        return (int) ((i + j) * (polyHeight - (polyHeight * cos(30))));
    }

    /**
     * Calculate Y offset in px of the tile inside the window
     *
     * @param i          first index of tile
     * @param j          second index of tile
     * @param polyHeight Height of each tile in px
     * @param boardSize  Size of board in tiles (one side)
     * @return Y offset of tile
     */
    public static int getHexY(int i, int j, int polyHeight, int boardSize) {
        return (int) (((j - i) * (polyHeight / 2f)) + (boardSize * (polyHeight / 2f)));
    }

    /**
     * Draw outlines at the edges of the outer tiles.
     * There is most likely a better way to implement this, but since it works...
     *
     * @param boardSize Size of board in tiles (one side)
     * @param colorOne  Color of first player
     * @param colorTwo  Color of second player
     * @param g2        Graphics context
     * @param board     Game board array
     */
    public static void drawOutlines(int boardSize, Color colorOne, Color colorTwo, Graphics2D g2, HexTile[][] board) {
        Stroke strokeFill = new BasicStroke(1);

        int outline = HexConfig.OUTLINE_WIDTH;

        //Get polygon height from one of the polygons
        int polyHeight = 0;
        if (board.length > 0 && board[0].length > 0) {
            polyHeight = getPolyHeight(board[0][0].getPoly());
        }

        int sHalf = Math.round((int) getSideLengthFromHeight(polyHeight) / 2f);
        int offset = (int) Math.round(outline * 0.6);

        for (int n = 0; n < boardSize; n++) {
            Polygon newPoly;
            Polygon currentHex;

            //Bottom left corner
            currentHex = board[0][n].getPoly();
            if (n == 0) {
                int[] cx = new int[]{currentHex.xpoints[5] - outline, currentHex.xpoints[5],
                        currentHex.xpoints[4], currentHex.xpoints[4] + sHalf,
                        currentHex.xpoints[4] + sHalf, currentHex.xpoints[4] - offset};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[5],
                        currentHex.ypoints[4], currentHex.ypoints[4], currentHex.ypoints[4] + outline,
                        currentHex.ypoints[4] + outline, currentHex.ypoints[4]};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[5] - sHalf, currentHex.xpoints[5],
                        currentHex.xpoints[4], currentHex.xpoints[4] + sHalf, currentHex.xpoints[4] + sHalf,
                        currentHex.xpoints[4] - offset, currentHex.xpoints[5] - offset, currentHex.xpoints[5] - sHalf};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[5], currentHex.ypoints[4],
                        currentHex.ypoints[4], currentHex.ypoints[4] + outline,
                        currentHex.ypoints[4] + outline, currentHex.ypoints[5] + outline,
                        currentHex.ypoints[5] + outline};
                newPoly = new Polygon(cx, cy, cx.length);
            }

            g2.setStroke(strokeFill);
            g2.setColor(colorTwo);
            g2.fillPolygon(newPoly);


            //Top right corner
            currentHex = board[boardSize - 1][n].getPoly();
            if (n == boardSize - 1) {
                int[] cx = new int[]{currentHex.xpoints[0] + sHalf, currentHex.xpoints[1] + offset,
                        currentHex.xpoints[2] + outline, currentHex.xpoints[2], currentHex.xpoints[1],
                        currentHex.xpoints[0] + sHalf};
                int[] cy = new int[]{currentHex.ypoints[0] - outline, currentHex.ypoints[1] - outline,
                        currentHex.ypoints[2], currentHex.ypoints[2], currentHex.ypoints[1], currentHex.ypoints[0]};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[0] + sHalf, currentHex.xpoints[1] + offset,
                        currentHex.xpoints[2] + offset, currentHex.xpoints[2] + sHalf, currentHex.xpoints[2] + sHalf,
                        currentHex.xpoints[2], currentHex.xpoints[1], currentHex.xpoints[0] + sHalf};
                int[] cy = new int[]{currentHex.ypoints[0] - outline, currentHex.ypoints[1] - outline,
                        currentHex.ypoints[2] - outline, currentHex.ypoints[2] - outline, currentHex.ypoints[2],
                        currentHex.ypoints[2], currentHex.ypoints[1], currentHex.ypoints[0]};
                newPoly = new Polygon(cx, cy, cx.length);
            }

            g2.setStroke(strokeFill);
            g2.setColor(colorTwo);
            g2.fillPolygon(newPoly);


            //Top left corner
            currentHex = board[n][0].getPoly();
            if (n == 0) {
                int[] cx = new int[]{currentHex.xpoints[5], currentHex.xpoints[0], currentHex.xpoints[0] + sHalf,
                        currentHex.xpoints[0] + sHalf, currentHex.xpoints[0] - offset, currentHex.xpoints[5] - outline};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[0], currentHex.ypoints[0],
                        currentHex.ypoints[0] - outline, currentHex.ypoints[0] - outline,
                        currentHex.ypoints[5]};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[5], currentHex.xpoints[0], currentHex.xpoints[0] + sHalf,
                        currentHex.xpoints[0] + sHalf, currentHex.xpoints[0] - offset, currentHex.xpoints[5] - offset,
                        currentHex.xpoints[5] - sHalf, currentHex.xpoints[5] - sHalf};
                int[] cy = new int[]{currentHex.ypoints[5], currentHex.ypoints[0], currentHex.ypoints[0],
                        currentHex.ypoints[0] - outline, currentHex.ypoints[0] - outline,
                        currentHex.ypoints[5] - outline, currentHex.ypoints[5] - outline,
                        currentHex.ypoints[5]};
                newPoly = new Polygon(cx, cy, cx.length);
            }

            g2.setStroke(strokeFill);
            g2.setColor(colorOne);
            g2.fillPolygon(newPoly);

            //Bottom right corner
            currentHex = board[n][boardSize - 1].getPoly();
            if (n == (boardSize - 1)) {
                int[] cx = new int[]{currentHex.xpoints[3] - sHalf, currentHex.xpoints[3], currentHex.xpoints[2],
                        currentHex.xpoints[2] + outline, currentHex.xpoints[3] + offset, currentHex.xpoints[3] - sHalf};
                int[] cy = new int[]{currentHex.ypoints[3], currentHex.ypoints[3], currentHex.ypoints[2],
                        currentHex.ypoints[2], currentHex.ypoints[3] + outline,
                        currentHex.ypoints[3] + outline};
                newPoly = new Polygon(cx, cy, cx.length);
            } else {
                int[] cx = new int[]{currentHex.xpoints[3] - sHalf, currentHex.xpoints[3], currentHex.xpoints[2],
                        currentHex.xpoints[2] + sHalf, currentHex.xpoints[2] + sHalf, currentHex.xpoints[2] + offset,
                        currentHex.xpoints[3] + offset, currentHex.xpoints[3] - sHalf};
                int[] cy = new int[]{currentHex.ypoints[3], currentHex.ypoints[3], currentHex.ypoints[2],
                        currentHex.ypoints[2], currentHex.ypoints[2] + outline, currentHex.ypoints[2] + outline,
                        currentHex.ypoints[3] + outline, currentHex.ypoints[3] + outline};
                newPoly = new Polygon(cx, cy, cx.length);
            }

            g2.setStroke(strokeFill);
            g2.setColor(colorOne);
            g2.fillPolygon(newPoly);
        }
    }

    /**
     * @param mx        X coordinate in pixels inside window
     * @param my        Y coordinate in pixels inside window
     * @param board     Game board array
     * @param boardSize Size of board in tiles (one side)
     * @return The coordinates of the Hex tile that was clicked
     */
    public static Point pxtoHex(int mx, int my, HexTile[][] board, int boardSize) {
        Point p = new Point(-1, -1);
        Point coords = new Point(mx, my);

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getPoly().contains(coords)) {
                    p.x = i;
                    p.y = j;
                }
            }
        }

        return p;
    }

    /**
     * Checks if the chain that contains the last placed stone touches both edges.
     * Other chains, especially those by the opponent, don't have to be checked each time.
     *
     * @param board          Game board array
     * @param lastPlacedTile Tile that the last stone was placed on
     * @return Either null or 1 as int - null if game is not over, 1 if player that placed the last stone has won
     */
    public static Types.WINNER getWinner(HexTile[][] board, HexTile lastPlacedTile) {
        if (lastPlacedTile == null) {
            //System.out.println("lastPlacedTile was null");
            return null;
        }
        int player = lastPlacedTile.getPlayer();
        LinkedList<HexTile> tilesToVisit = new LinkedList<>();
        ArrayList<HexTile> visitedTiles = new ArrayList<>();

        boolean firstEdgeReached = false;
        boolean secondEdgeReached = false;

        tilesToVisit.add(lastPlacedTile);

        while (tilesToVisit.size() > 0) {
            HexTile currentTile = tilesToVisit.pop();
            //Add all neighbors that belong to the player and have not been visited
            ArrayList<HexTile> adjacentTiles = getAdjacentTiles(board, currentTile);
            for (HexTile currentNeighbor : adjacentTiles) {
                if (currentNeighbor.getPlayer() == player &&
                        !tilesToVisit.contains(currentNeighbor) &&
                        !visitedTiles.contains(currentNeighbor)) {
                    tilesToVisit.add(currentNeighbor);
                }
            }

            //Check if the tile is next to an edge belonging to the player
            switch (isNextToEdge(currentTile)) {
                case 1:
                    firstEdgeReached = true;
                    break;
                case 2:
                    secondEdgeReached = true;
                    break;
            }

            if (firstEdgeReached && secondEdgeReached) {
                return Types.WINNER.PLAYER_WINS;
            }

            visitedTiles.add(currentTile);
        }

        return null;
    }

    /**
     * Generates feature vector 0.
     * Quite slow and training does not work.
     * Use not recommended.
     *
     * @param board  Game board array
     * @param player Player for which the features are to be generated
     * @return Feature vector 0
     */
    public static double[] getFeature0ForPlayer(HexTile[][] board, int player) {
        LinkedList<HexTile> tilesToVisit = new LinkedList<>();
        ArrayList<HexTile> visitedTiles = new ArrayList<>();
        ArrayList<HexTile> freeNeighborTiles = new ArrayList<>();

        int longestChain = 0;
        int virtualConnections = 0;

        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                if (!visitedTiles.contains(board[i][j]) && board[i][j].getPlayer() == player) {
                    tilesToVisit.add(board[i][j]);
                }
            }
        }

        int currentChainLength = 0;
        int currentVirtualConnections = 0;
        int currentChainMin = Integer.MAX_VALUE;
        int currentChainMax = Integer.MIN_VALUE;
        while (tilesToVisit.size() > 0) {
            HexTile currentTile = tilesToVisit.pop();
            ArrayList<HexTile> adjacentTiles = getAdjacentTiles(board, currentTile);
            for (HexTile currentNeighbor : adjacentTiles) {
                //Check if the tile has not been visited and is not yet marked to be visited...
                if (!tilesToVisit.contains(currentNeighbor) &&
                        !visitedTiles.contains(currentNeighbor)) {
                    //If the tile belongs to the player, add it directly
                    if (currentNeighbor.getPlayer() == player) {
                        tilesToVisit.add(currentNeighbor);
                    } else if (currentNeighbor.getPlayer() == PLAYER_NONE) {
                        //Add currentNeighbor to count of neighboring free tiles
                        if (!freeNeighborTiles.contains(currentNeighbor)) {
                            freeNeighborTiles.add(currentNeighbor);
                        }
                        //If the tile does not belong to a player, check if there is an additional free tile
                        //where both free tiles connect the same two tiles that belong to the player
/*                                Example (X: tile of player; ?: free tile):
                                     ?
                                   /   \
                                  X     X
                                   \   /
                                     ?

                                 Also applies if one X is replaced by an edge with the same color as the player*/

                        ArrayList<HexTile> neighborsNeighbors = getAdjacentTiles(board, currentNeighbor);
                        for (HexTile currentNeighborsNeighbor : neighborsNeighbors) {
                            if (!currentNeighborsNeighbor.equals(currentTile)
                                    && adjacentTiles.contains(currentNeighborsNeighbor)
                                    && currentNeighborsNeighbor.getPlayer() == PLAYER_NONE) {
                                //If this evaluates to true, the tile is adjacent to currentTile

                                //Check if both tiles are connected to an edge that belongs to the player
                                if (isNextToEdge(currentNeighbor, player) > 0
                                        && isNextToEdge(currentNeighborsNeighbor, player) > 0) {
                                    tilesToVisit.add(currentNeighbor);
                                    tilesToVisit.add(currentNeighborsNeighbor);
                                    virtualConnections++;
                                    currentVirtualConnections++;
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
                                            virtualConnections++;
                                            currentVirtualConnections++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }

            if (player == HexConfig.PLAYER_TWO) {
                //For player two, x direction matters
                currentChainMin = Math.min(currentTile.getCoords().x, currentChainMin);
                currentChainMax = Math.max(currentTile.getCoords().x, currentChainMax);
            } else if (player == PLAYER_ONE) {
                //For player one, y direction matters
                currentChainMin = Math.min(currentTile.getCoords().y, currentChainMin);
                currentChainMax = Math.max(currentTile.getCoords().y, currentChainMax);
            }

            currentChainLength = Math.max(currentChainLength, (currentChainMax - currentChainMin) + 1 - currentVirtualConnections);

            visitedTiles.add(currentTile);
        }
        longestChain = Math.max(longestChain, currentChainLength);

        double[] rVal = new double[3];
        rVal[0] = (double) longestChain / (double) HexConfig.BOARD_SIZE;
        rVal[1] = (double) freeNeighborTiles.size() / (double) (HexConfig.TILE_COUNT + 1);
        rVal[2] = (double) virtualConnections / (double) HexConfig.TILE_COUNT;

        return rVal;
    }

    /**
     * Similar to feature mode 0, but a bit faster and could potentially be improved.
     * Generates a list of tile connections for the player's tiles.
     * Recognizes direct, weak and virtual connections.
     *
     * @param board  Game board array
     * @param player Player for whom to generate the features
     * @return Feature vector 3
     */
    public static double[] getFeature3ForPlayer(HexTile[][] board, int player) {
        LinkedList<HexTile> tilesToVisit = new LinkedList<>();
        ArrayList<HexTile> freeNeighborTiles = new ArrayList<>();
        ArrayList<ArrayList<HexTile>> connections = new ArrayList<>();

        //Two dummy tiles to use as edges
        HexTile edge1 = new HexTile(-1, -1);
        HexTile edge2 = new HexTile(-2, -2);

        //Get a list of all tiles the player owns
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                if (board[i][j].getPlayer() == player) {
                    tilesToVisit.add(board[i][j]);
                }
            }
        }

        //Visit every tile the player owns
        while (tilesToVisit.size() > 0) {
            HexTile currentTile = tilesToVisit.pop();
            int edge = isNextToEdge(currentTile, player);
            if (edge > 0) {
                HexTile neighboringEdge = edge == 1 ? edge1 : edge2;
                connections = addConnection(connections, currentTile, neighboringEdge, null);
                continue;
            }
            ArrayList<HexTile> adjacentTiles = getAdjacentTiles(board, currentTile);
            for (HexTile neighbor : adjacentTiles) {
                if (neighbor.getPlayer() == getOpponent(player)) {
                    continue;
                }

                if (neighbor.getPlayer() == player) {
                    connections = addConnection(connections, currentTile, neighbor, null);
                    continue;
                }

                if (neighbor.getPlayer() == PLAYER_NONE && !freeNeighborTiles.contains(neighbor)) {
                    freeNeighborTiles.add(neighbor);
                }

                int neighborEdge = isNextToEdge(neighbor, player);
                HexTile connectingTile = null;
                if (neighborEdge == 1) {
                    connectingTile = edge1;
                } else if (neighborEdge == 2) {
                    connectingTile = edge2;
                }

                if (connectingTile != null) {
                    connections = addConnection(connections, currentTile, neighbor, connectingTile);
                } else {
                    ArrayList<HexTile> neighborsNeighbors = getAdjacentTiles(board, neighbor);
                    for (HexTile neighborsNeighbor : neighborsNeighbors) {
                        if (neighborsNeighbor == currentTile) {
                            continue;
                        }
                        connectingTile = null;
                        if (neighborsNeighbor.getPlayer() == player) {
                            connectingTile = neighborsNeighbor;
                        }

                        if (connectingTile != null) {
                            connections = addConnection(connections, currentTile, neighbor, connectingTile);
                        }
                    }
                }
            }
        }

        int longestChain = 0;

        ArrayList<ArrayList<HexTile>> chains = generateChains(connections);
        for (ArrayList<HexTile> chain : chains) {
            int currentChainMin = Integer.MAX_VALUE;
            int currentChainMax = Integer.MIN_VALUE;

            for (HexTile currentTile : chain) {
                if (player == HexConfig.PLAYER_TWO) {
                    //For player two, x direction matters
                    currentChainMin = Math.min(currentTile.getCoords().x, currentChainMin);
                    currentChainMax = Math.max(currentTile.getCoords().x, currentChainMax);
                } else if (player == PLAYER_ONE) {
                    //For player one, y direction matters
                    currentChainMin = Math.min(currentTile.getCoords().y, currentChainMin);
                    currentChainMax = Math.max(currentTile.getCoords().y, currentChainMax);
                }
            }
            longestChain = Math.max(longestChain, (currentChainMax - currentChainMin) + 1);
            //System.out.println(chain);
        }

        int virtualConnections = 0;
        int weakLinks = 0;
        int directConnections = 0;

        //Check all conections again and remove virtual and weak connections that are not needed due to both tiles that
        //are connected being in a longer, direct chain
        Iterator<ArrayList<HexTile>> iter = connections.iterator();
        while (iter.hasNext()) {
            ArrayList<HexTile> connection = iter.next();
            int connectionSize = connection.size();
            if (connectionSize > 2 && areConnected(chains, connection.get(0), connection.get(2))) {
                iter.remove();
                continue;
            }
            if (connectionSize == 3) {
                weakLinks++;
            } else if (connectionSize == 4) {
                virtualConnections++;
            } else if (connectionSize == 2) {
                directConnections++;
            } else {
                System.out.println("Unexpected connection length: " + connectionSize);
            }
        }

        double[] rVal = new double[5];
        rVal[0] = (double) longestChain;
        rVal[1] = (double) freeNeighborTiles.size();
        rVal[2] = (double) virtualConnections;
        rVal[3] = (double) weakLinks;
        rVal[4] = (double) directConnections;

        double[] ownTilesInSlice = new double[board.length];
        double[] enemyTilesInSlice = new double[board.length];

        return rVal;
    }

    /**
     * Used for feature mode 3.
     * Adds a new connection to the ArrayList and also checks for duplicates.
     * Upgrades connections from weak to direct or weak to virtual if necessary.
     * Direct connections only contain two tiles, weak connections always contain three tiles, virtual connections
     * contain four tiles.
     *
     * @param connections    Current list of connections
     * @param currentTile    Subject tile that is being looked at
     * @param neighbor       Tile that is in some way connected to the subject tile
     * @param connectingTile Intermediary tile that connects the subject and the neighbor. Always an unused tile.
     * @return
     */
    private static ArrayList<ArrayList<HexTile>> addConnection(ArrayList<ArrayList<HexTile>> connections, HexTile currentTile, HexTile neighbor, HexTile connectingTile) {
        boolean matchFound = false;
        for (ArrayList<HexTile> connection : connections) {
            if (connectingTile != null) {
                if (connection.contains(currentTile) && connection.contains(connectingTile)) {
                    //Add this tile to the list of connecting tiles, but only if connection is not direct
                    matchFound = true;
                    if (!connection.contains(neighbor) && connection.size() > 2) {
                        connection.add(neighbor);
                    }
                }
            } else {
                //Only the case, if its a direct connection
                if (connection.contains(currentTile) && connection.contains(neighbor)) {
                    matchFound = true;
                    //Delete indirect connecting tiles if they exist
                    connection.removeIf(tile -> !tile.equals(currentTile) && !tile.equals(neighbor));
                }
            }
        }

        if (!matchFound) {
            ArrayList<HexTile> newConnection = new ArrayList<>();
            newConnection.add(currentTile);
            newConnection.add(neighbor);
            if (connectingTile != null) {
                newConnection.add(connectingTile);
            }
            connections.add(newConnection);
        }

        return connections;
    }

    /**
     * Checks if there exists a chain that contains both tiles
     *
     * @param chains List of chains
     * @param tile1  First tile
     * @param tile2  Second tile
     * @return True or False, depending on if the tiles are in the same chain or not
     */
    private static boolean areConnected(ArrayList<ArrayList<HexTile>> chains, HexTile tile1, HexTile tile2) {
        for (ArrayList<HexTile> chain : chains) {
            if (chain.contains(tile1) && chain.contains(tile2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generates a list of chains from the list of connections.
     * Does so by stitching together all direct connections.
     *
     * @param connections List of connections
     * @return List of chains
     */
    private static ArrayList<ArrayList<HexTile>> generateChains(ArrayList<ArrayList<HexTile>> connections) {
        ArrayList<ArrayList<HexTile>> chains = new ArrayList<>();

        for (ArrayList<HexTile> connection : connections) {
            if (connection.size() != 2) {
                continue;
            }

            boolean matchFound = false;
            for (ArrayList<HexTile> chain : chains) {
                boolean tile1Contained = chain.contains(connection.get(0));
                boolean tile2Contained = chain.contains(connection.get(1));

                if (tile1Contained || tile2Contained) {
                    matchFound = true;
                    boolean merged = false;
                    if (!tile1Contained) {
                        for (ArrayList<HexTile> chain2 : chains) {
                            if (chain2.contains(connection.get(0))) {
                                chain.removeAll(chain2);
                                chain.addAll(chain2);
                                chain2.removeAll(chain);
                                merged = true;
                            }
                        }
                        if (!merged) {
                            chain.add(connection.get(0));
                        }
                    } else if (!tile2Contained) {
                        for (ArrayList<HexTile> chain2 : chains) {
                            if (chain2.contains(connection.get(1))) {
                                chain.removeAll(chain2);
                                chain.addAll(chain2);
                                chain2.removeAll(chain);
                                merged = true;
                            }
                        }
                        if (!merged) {
                            chain.add(connection.get(1));
                        }
                    }
                }
            }

            if (!matchFound) {
                ArrayList<HexTile> newChain = new ArrayList<>();
                newChain.add(connection.get(0));
                newChain.add(connection.get(1));
                chains.add(newChain);
            }
        }

        //The above code can leave empty chains in the array list, remove them
        chains.removeIf(chain -> chain.size() == 0);

        return chains;
    }

    /**
     * Counts all the tiles that belong to the player given as parameter
     *
     * @param board  Game board array
     * @param player Player to check tile count for
     * @return Tile count for player
     */
    public static int getTileCountForPlayer(HexTile[][] board, int player) {
        int tileCount = 0;
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                if (board[i][j].getPlayer() == player) {
                    tileCount++;
                }
            }
        }

        return tileCount;
    }

    /**
     * Checks if the tile is next to an edge the player owning the tile has to reach
     *
     * @param tile The tile to check
     * @return 0, 1 or 2 -- 0 means no edge, 1 is the first edge, 2 is the second.
     */
    private static int isNextToEdge(HexTile tile) {
        return isNextToEdge(tile, tile.getPlayer());
    }

    /**
     * Checks if the tile is next to an edge of the player that is given as parameter
     *
     * @param tile   The tile to check
     * @param player The player whose edge is to be checked. Optional parameter, if missing,
     *               the player who the tile belongs to will be subject of the comparison.
     * @return 0, 1 or 2 -- 0 means no edge, 1 is the first edge, 2 is the second.
     */
    private static int isNextToEdge(HexTile tile, int player) {
        int x = tile.getCoords().x;
        int y = tile.getCoords().y;

        if (player == HexConfig.PLAYER_TWO) {
            if (x == 0) {
                return 1;
            } else if (x == HexConfig.BOARD_SIZE - 1) {
                return 2;
            }
        } else if (player == PLAYER_ONE) {
            if (y == 0) {
                return 1;
            } else if (y == HexConfig.BOARD_SIZE - 1) {
                return 2;
            }
        }

        return 0;
    }

    /**
     * Generates a list of all neighboring tiles for the given tile
     *
     * @param board Game board array
     * @param tile  Subject tile
     * @return List of neighbors
     */
    private static ArrayList<HexTile> getAdjacentTiles(HexTile[][] board, HexTile tile) {
        ArrayList<HexTile> adjacentTiles = new ArrayList<>();
        int x = tile.getCoords().x;
        int y = tile.getCoords().y;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborX = x + i;
                int neighborY = y + j;
                if (i != j && isValidTile(neighborX, neighborY)) {
                    adjacentTiles.add(board[neighborX][neighborY]);
                }
            }
        }

        return adjacentTiles;
    }

    /**
     * Checks if the tile exists by checking if it is inside the bounds of the game board
     *
     * @param x index 1
     * @param y index 2
     * @return True or False, depending on if the tile exists
     */
    public static boolean isValidTile(int x, int y) {
        return (x >= 0 && x < HexConfig.BOARD_SIZE) && (y >= 0 && y < HexConfig.BOARD_SIZE);
    }

    /**
     * Calculates height of the polygon by subtracting window coordinates of two opposite vertices
     *
     * @param poly Subject polygon
     * @return Height of polygon
     */
    static int getPolyHeight(Polygon poly) {
        if (poly.xpoints.length == 6) {
            return poly.ypoints[4] - poly.ypoints[0];
        } else {
            return 0;
        }
    }

    /**
     * Calculates the length of a single side of the polygon
     *
     * @param height Height of the polygon
     * @return Side length of polygon
     */
    static double getSideLengthFromHeight(int height) {
        return ((float) height / 2f) / (Math.sqrt(3) / 2f);
    }

    /**
     * Converts the two-dimensional board array to a one-dimensional vector.
     * Leftmost tile on board (coordinates: 0, 0) is the first element.
     * Bottom tile on board (coordinates: 0, n) is the n-th element.
     * Top tile on board (coordinates: n, 0) is the (n^2 - n + 1)-th element.
     * Rightmost tile on board (coordinates: n, n) is the (n^2)-th element.
     *
     * @param board
     * @return
     */
    static HexTile[] boardToVector(HexTile[][] board) {
        HexTile[] boardVector = new HexTile[board.length * board.length];

        for (int i = 0, k = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++, k++) {
                boardVector[k] = board[i][j];
            }
        }
        return boardVector;
    }

    /**
     * @param player Subject player
     * @return Opponent player of subject
     */
    static int getOpponent(int player) {
        if (player == PLAYER_ONE) {
            return PLAYER_TWO;
        } else {
            return PLAYER_ONE;
        }
    }
}
