package games.RubiksCube;

import games.Arena;
import tools.Types;

import javax.swing.*;
import java.awt.*;

/**
 * GUI of the 2x2x2 Pocket Cube in the 'flattened cube' representation.
 * <p>
 * The numbering in {@link CubeState#fcol} runs around the cube faces in the following order: U, L, F, D, R, B.
 * Within the first three faces, we start at the wbo-cubie; within the last three faces we start
 * at the ygr-cubie. Within each face we march around in counter-clockwise
 * orientation. This gives for the 2x2x2 pocket cube the following numbering:
 * <pre>
 *         3  2
 *         0  1
 *   5  4  8 11 18 17 23 22
 *   6  7  9 10 19 16 20 21
 *        14 13
 *        15 12
 * </pre>
 */
public class GameBoardCubeGui2x2 extends GameBoardCubeGui {
    /**
     * For guiUpdateBoard: Which is the row index iarr and the column index jarr in the boardY * boardX JPanel[][] Board
     * for each of the cubie faces in CubeState.fcol[i], i=0,...,fcol.length-1.<br>
     * [These arrays will be set in constructor GameBoardCubeGui2x2.]
     */
    protected final int[] iarr,jarr;

    protected final int TICGAMEHEIGHT=280;

    public GameBoardCubeGui2x2(GameBoardCube gb) {
        super(gb);
        boardX=8;
        boardY=6;
        buttonX=3;
        buttonY=3;
        // for guiUpdateBoard: which is the row index iarr and the column index jarr in the boardY * boardX JPanel[][] Board
        // for each of the cubie faces in CubeState.fcol[i], i=0,...,fcol.length-1
        //     i    =   00       04       08       12       16       20
        iarr = new int[]{1,1,0,0, 2,2,3,3, 2,3,3,2, 5,4,4,5, 3,2,2,3, 3,3,2,2};
        jarr = new int[]{2,3,3,2, 1,0,0,1, 2,2,3,3, 3,3,2,2, 5,5,4,4, 6,7,7,6};

        initGameBoard();

        // ensure that ButtonBoard is already visible in the beginning,
        // if updateBoard() is configured in this way:
        this.updateBoard((StateObserverCube)m_gb.getDefaultStartState(), true, true);
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
                boolean v = (i==2 || i==3 || j==2 || j==3);
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
        int fcol_length = m_gb.m_so.getCubeState().get_fcol_length();
        //      U         L        F         D          R         B
        Color[] colors = {Color.white, colBlue, colOrang, colYellow, colGreen, colRed};        //{w,b,o,y,g,r}
        // iarr and jarr are set in constructors GameBoardCubeGui2x2 or GameBoardCubeGui3x3
        for (i = 0; i < fcol_length; i++) {
            fcol_i = m_gb.m_so.getCubeState().get_fcol(i);
            Board[iarr[i]][jarr[i]].setEnabled(enable);
            Board[iarr[i]][jarr[i]].setBackground(colors[fcol_i]);
            Board[iarr[i]][jarr[i]].setForeground(Color.white);
        }
        guiUpdateButton(enable,showValueOnGameboard);
    }

    public void showGameBoard(Arena ticGame, boolean alignToMain) {
        this.setVisible(true);
        if (alignToMain) {
            // place window with game board below the main window
            int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
            int y = ticGame.m_xab.getLocation().y;
            int ticgamewidth = ticGame.m_ArenaFrame.getWidth();
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
