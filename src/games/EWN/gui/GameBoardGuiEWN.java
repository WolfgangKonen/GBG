package games.EWN.gui;

import games.Arena;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.EWN.config.ConfigEWN;
import tools.Types;

import javax.swing.*;
import java.awt.*;

/**
 * GameBoardGuiEWN is build from three elements:
 * <pre>
 *     NORTH:   LegendGui
 *     CENTER:  BoardGui
 *     SOUTH:   JLabel (ErrorMessage)  </pre>
 * @see BoardGui
 * @see LegendGui
 */
public class GameBoardGuiEWN extends JFrame {

    private static final long serialVersionUID = 12L;

    private double[][] vGameState;
    /**
     * Reference to parent object {@link GameBoardEWN}
     */
    private GameBoardEWN m_gb = null;
    private BoardGui boardGui = null;
    private LegendGui legend;
    private JLabel errorMessage;
    private boolean selecting;
    private int SelectedTokenPos;

    public GameBoardGuiEWN(GameBoardEWN gb){
        super("Einstein Wuerfelt Nicht");
        this.m_gb = gb;
        this.selecting = true;
        this.vGameState = new double[ConfigEWN.BOARD_SIZE*ConfigEWN.BOARD_SIZE][3];
        StateObserverEWN so = (StateObserverEWN) gb.getStateObs();
        this.setLayout(new BorderLayout());
        this.boardGui = new BoardGui(gb,so);
        this.add(boardGui,BorderLayout.CENTER);
        this.legend  = new LegendGui(so.getNumPlayers() ,so.getNextNondeterministicAction().toInt());
        this.add(legend,BorderLayout.NORTH);
        this.initErrorMessage();
        this.add(errorMessage, BorderLayout.SOUTH);
        this.setMinimumSize(new Dimension(500,300));
        pack();
        setVisible(true);
    }

    public void initErrorMessage(){
        errorMessage = new JLabel(" ");
        errorMessage.setForeground(Color.RED);
        errorMessage.setFont(new Font("Arial", Font.PLAIN,Types.GUI_TITLEFONTSIZE));

    }

    /**
     * Resetting the game board to its starting state. {@link StateObserverEWN}
     * @param boardClear
     * @param vClear
     */
    public void clearBoard(boolean boardClear, boolean vClear){
        updateBoard((StateObserverEWN )this.m_gb.getStateObs(), false, true);
        selecting = true;
    }

    public int unSelect(){
        boardGui.unSelectToken();
        return -1;
    }

    public int hGameSelecting(int index){
        int y = index % ConfigEWN.BOARD_SIZE;
        int x = (index-y) / ConfigEWN.BOARD_SIZE;
        boardGui.selectToken(x, y);
        return index;
    }


    public void updateBoard(StateObserverEWN so, boolean withReset, boolean showValueOnGameboard){
        boardGui.updateBoard( so,withReset,showValueOnGameboard);
        if(so == null) return;
        legend.update(so.getPlayer(),so.getNextNondeterministicAction().toInt());

        boardGui.updateBoard(so,withReset,showValueOnGameboard);
    }

    public void showGameBoard(Arena arena, boolean alignToMain){
        this.setVisible(true);
        if (alignToMain) {
            // place window with game board to the right of the main window
            int x = arena.m_ArenaFrame.getX() + arena.m_xab.getWidth() + 18;
            int y = arena.m_ArenaFrame.getY();
            this.setLocation(x,y);
        }
    }
    public void enableInteraction(boolean enable){
    }

    public void destroy(){
        this.dispose();
    }

    public void tokenUpdateValue(int from, int to){
        int y = from % ConfigEWN.BOARD_SIZE;
        int x = (to-y) / ConfigEWN.BOARD_SIZE;
        //boardGui.getBoard()[x][y].updateValues(Helper.parseAction(from,to,ConfigEWN.BOARD_SIZE));
    }

    public void setError(String str)
    {
        this.errorMessage.setText(str);
    }
    public GameBoardEWN getM_gb(){
        return m_gb;
    }
}
