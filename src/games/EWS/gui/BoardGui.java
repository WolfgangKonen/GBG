package games.EWS.gui;

import games.EWS.GameBoardEWS;
import games.EWS.StateObserverEWS;
import games.EWS.StateObserverHelper.Helper;
import games.EWS.StateObserverHelper.Token;
import games.Othello.Gui.Tile;
import games.RubiksCube.CubeState;
import games.StateObservation;
import tools.Types;

import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Simple panel which contains the board
 */
public class BoardGui extends JPanel {


    private StateObserverEWS m_so = null;
    private int size;
    private TileGui[][] board;
    private double[][] vGameState;
    private boolean drag;
    private int from = -1;
    private GameBoardEWS m_gb;

    public BoardGui(GameBoardEWS gb,StateObserverEWS so){
        super();
        m_gb = gb;
        drag=false;
        this.m_so = so;
        this.size = so.getSize();
        this.board = new TileGui[size][size];
        vGameState = new double[size][size];
        this.setLayout(new GridLayout(size,size,1,1));
        this.setBackground(Color.BLUE );
        initBoard();
    }

    public void initBoard(){

        for(int i = 0; i < size; i++){
            for(int k = 0; k < size; k++){
                TileGui tg = new TileGui(m_so.getGameState()[i][k], this);
                board[i][k] = tg;
                this.add(board[i][k]);
            }
        }
    }


    public GameBoardEWS getGameBoardEWS(){
        return this.m_gb;
    }

    public void clearBoard(boolean boardClear, boolean vClear){
        if(vClear) emptyVGameState();
    }



    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard){
    if(so != null){
        StateObserverEWS soT = (StateObserverEWS) so;
        // Update legend

        if(showValueOnGameboard && soT.getStoredValues() != null){
           emptyVGameState();
           for(int i = 0; i < soT.getStoredValues().length; i++){
               Types.ACTIONS act = soT.getStoredAction(i);
           }
        }
        updateComponents(soT, showValueOnGameboard);
    }

   }


   public void updateComponents(StateObserverEWS so, boolean showValueOnGameboard){
        for(int i = 0; i < size; i++){
            for(int k = 0; k<size; k++){
                // Update Token
                board[i][k].updateTile(so.getGameState()[i][k]);
            }
        }
        this.repaint();
   }

   private void emptyVGameState(){
       vGameState = new double[size][size];
       for(int i = 0; i < size; i++)
           for(int k = 0; k < size; k++)
               vGameState[i][k] = Double.NaN;
   }


   public int getFrom(){
        return from;
   }

   public void setFrom(int u){
        this.from = u;
   }


}

