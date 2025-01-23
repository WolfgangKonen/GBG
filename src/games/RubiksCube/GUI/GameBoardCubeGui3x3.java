package games.RubiksCube.GUI;

import games.Arena;
import games.RubiksCube.CubeState;
import games.RubiksCube.GameBoardCube;
import games.RubiksCube.StateObserverCube;
import tools.Types;

import javax.swing.*;
import java.awt.*;

/**
 * GUI of the 3x3x3 Rubik's Cube in the 'flattened cube' representation.
 * <p>
 * The numbering in {@link CubeState#getFcol()} runs around the cube faces in the following order: U, L, F, D, R, B.
 * Within the first three faces, we start at the wbo-cubie; within the last three faces we start
 * at the ygr-cubie. Within each face we march around in counter-clockwise
 * orientation. This gives for the 3x3x3 Rubik's Cube the following numbering:
 * <pre>
 *              6  5  4
 *              7  U  3
 *              0  1  2
 *   10  9  8  16 23 22  36 35 34  46 45 44
 *   11	 L 15  17  F 21  37  R 33  47  B 43
 *   12 13 14  18 19 20  38 39 32  40 41 42
 *             28 27 26
 *             29  D 25
 *             30 31 24
 * </pre>
 */
public class GameBoardCubeGui3x3 extends GameBoardCubeGui {
    /**
     * For guiUpdateBoard: Which is the row index iarr and the column index jarr in the boardY * boardX JPanel[][] Board
     * for each of the cubie faces in CubeState.fcol[i], i=0,...,fcol.length-1.<br>
     * [These arrays will be set in constructor GameBoardCubeGui3x3.]
     */
    protected final int[] iarr, jarr;
    /**
     * Similar to {@link #iarr} and {@link #iarr}, {@code icen} and {@code jcen} have the row and column indices
     * of the 6 center cubies.
     */
    protected final int[] icen = {1,4,4,7,4, 4},
                          jcen = {4,1,4,4,7,10};

    protected final int TICGAMEHEIGHT=400;
    protected final int TICGAMEWIDTH=600;

    public GameBoardCubeGui3x3(GameBoardCube gb) {
        super(gb);
        boardX=12;
        boardY=9;
        buttonX=3;
        buttonY=6;
        // for guiUpdateBoard: which is the row index iarr and the column index jarr in the boardY * boardX JPanel[][] Board
        // for each of the cubie faces in CubeState.fcol[i], i=0,...,fcol.length-1
        //     i    =   00               08               16               24               32                40
        iarr = new int[]{2,2,2,1,0,0,0,1, 3,3,3,4,5,5,5,4, 3,4,5,5,5,4,3,3, 8,7,6,6,6,7,8,8, 5,4,3,3,3,4,5,5,  5, 5, 5, 4, 3, 3,3,4};
        jarr = new int[]{3,4,5,5,5,4,3,3, 2,1,0,0,0,1,2,2, 3,3,3,4,5,5,5,4, 5,5,5,4,3,3,3,4, 8,8,8,7,6,6,6,7,  9,10,11,11,11,10,9,9};

        initGameBoard();

        // ensure that ButtonBoard is already visible in the beginning,
        // if updateBoard() is configured in this way:
        this.updateBoard((StateObserverCube) gameBoardCube.getDefaultStartState(null), true, true);
    }

    protected JPanel InitBoard()
    {
        JPanel panel=new JPanel();
        panel.setLayout(new GridLayout(boardY,boardX,1,1));
        panel.setBackground(Types.GUI_BGCOLOR);
        int buSize = (int)(25*Types.GUI_SCALING_FACTOR_X);
        Dimension minimumSize = new Dimension(buSize,buSize); //controls the cube face sizes
        for(int i=0;i<boardY;i++){
            for(int j=0;j<boardX;j++){
                Board[i][j] = new JPanel();
                Board[i][j].setBackground(colTHK2);
                Board[i][j].setForeground(Color.white);
                Font font=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
                Board[i][j].setFont(font);
                Board[i][j].setPreferredSize(minimumSize);
                boolean v = (i==3 || i==4 || i==5 || j==3 || j==4 || j==5);
                Board[i][j].setVisible(v);
                panel.add(Board[i][j]);
            }
        }
        return panel;
    }

    /**
     * Update the play board and the associated action values (raw score*100) for the state
     * {@code m_gb.m_so}.
     * <p>
     * Color coding for the action buttons, if {@code showValueOnGameBoard==true}:<br>
     * color green = good move, high value, color red = bad move, low value
     *
     * @param enable as a side effect, all buttons Button[i][j]
     * 				 will be set into enabled state <code>enable</code>.
     * @param showValueOnGameboard if true, show the values on the action buttons. If false,
     * 				 clear any previous values.
     */
    protected void guiUpdateBoard(boolean enable, boolean showValueOnGameboard) {
        int i, fcol_i;
        int fcol_length = gameBoardCube.stateObserverCube.getCubeState().get_fcol_length();
        //                      U         L        F         D          R         B
        Color[] colors = {Color.white, colBlue, colOrang, colYellow, colGreen, colRed};        //{w,b,o,y,g,r}
        // iarr and jarr are set in constructors GameBoardCubeGui2x2 or GameBoardCubeGui3x3
        for (i = 0; i < fcol_length; i++) {
            fcol_i = gameBoardCube.stateObserverCube.getCubeState().get_fcol(i);
            Board[iarr[i]][jarr[i]].setEnabled(enable);
            Board[iarr[i]][jarr[i]].setBackground(colors[fcol_i]);
            Board[iarr[i]][jarr[i]].setForeground(Color.white);
        }
        for (i = 0; i < 6; i++) {
            Board[icen[i]][jcen[i]].setEnabled(enable);
            Board[icen[i]][jcen[i]].setBackground(colors[i]);
            Board[icen[i]][jcen[i]].setForeground(Color.white);
        }

        guiUpdateButton(enable,showValueOnGameboard);
    }

    public void showGameBoard(Arena ticGame, boolean alignToMain) {
        this.setVisible(true);
        if (alignToMain) {
            // place window with game board below the main window
            int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
            int y = ticGame.m_xab.getLocation().y;
            int ticgamewidth  = (int)(Types.GUI_SCALING_FACTOR_Y*TICGAMEWIDTH);
            int ticgameheight = (int)(Types.GUI_SCALING_FACTOR_Y*TICGAMEHEIGHT);
            if (ticGame.m_ArenaFrame!=null) {
                x = ticGame.m_ArenaFrame.getX();
                y = ticGame.m_ArenaFrame.getY() + ticGame.m_ArenaFrame.getHeight() +1;
                this.setSize(ticgamewidth,ticgameheight);
            }
            this.setLocation(x,y);
        }
    }

}
