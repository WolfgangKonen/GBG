package games.EWS.gui;

import games.Arena;
import games.EWS.GameBoardEWS;
import games.EWS.StateObserverEWS;
import games.StateObservation;

import javax.swing.*;
import java.awt.*;

public class GameBoardGuiEWS extends JFrame {

    private static final long serialVersionUID = 12L;

    private double vGameState;
    /**
     * Reference to parent object {@link GameBoardEWS}
     */
    private GameBoardEWS m_gb = null;
    private BoardGui boardGui = null;
    private LegendGui legend;


    public GameBoardGuiEWS(GameBoardEWS gb){
        super("Einstein würfelt nicht");
        this.m_gb = gb;
        StateObserverEWS so = (StateObserverEWS) gb.getStateObs();
        this.setLayout(new BorderLayout());
        this.boardGui = new BoardGui(gb,so);
        this.add(boardGui,BorderLayout.CENTER);
        this.legend  = new LegendGui(so.getNumPlayers() ,so.getNextNondeterministicAction().toInt());
        this.add(legend,BorderLayout.NORTH);
        this.setMinimumSize(new Dimension(500,300));
        pack();
        setVisible(true);
    }

    /**
     * Resetting the game board to its starting state. {@link StateObserverEWS}
     * @param boardClear
     * @param vClear
     */
    public void clearBoard(boolean boardClear, boolean vClear){
        if(vClear)boardGui.clearBoard(boardClear, vClear);
        updateBoard(this.m_gb.getStateObs(), false, true);
    }

    public void initNewBoard(StateObserverEWS so){
        this.remove(boardGui);
        this.boardGui = new BoardGui(m_gb,so);
        this.add(boardGui,BorderLayout.CENTER);
        pack();

    }

    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard){
        legend.update(((StateObserverEWS) so).getPlayer(), ((StateObserverEWS) so).getNextNondeterministicAction().toInt());
        boardGui.updateBoard(((StateObserverEWS) so),withReset,showValueOnGameboard);
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

}
