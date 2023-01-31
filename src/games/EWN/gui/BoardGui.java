package games.EWN.gui;

import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.EWN.StateObserverHelper.Helper;
import games.EWN.config.ConfigEWN;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.Tile;
import tools.Types;

import javax.swing.*;
import java.awt.*;

/**
 * Simple panel which contains the board
 */
public class BoardGui extends JPanel {
    private StateObserverEWN m_so = null;
    private int size;
    private TileGui[][] board;
    private boolean drag;
    private int from = -1;
    private GameBoardEWN m_gb;

    public BoardGui(GameBoardEWN gb, StateObserverEWN so){
        super();
        m_gb = gb;
        this.size = so.getSize();
        drag=false;
        this.m_so = so;
        this.board = new TileGui[size][size];
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

    public TileGui[][] getBoard(){
        return board;
    }

    public void selectToken(int x, int y){
        board[x][y].markAsSelected();
    }

    public void unSelectToken(){
        for(TileGui[] trow: board){
            for(TileGui t: trow){
                t.unmarkAsSelected();
            }
        }
    }



    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard){
    if(so != null){
        StateObserverEWN soT = (StateObserverEWN) so;
        // Update legend
        for(TileGui[] tRow: board)
            for(TileGui t: tRow)
                t.emptyValues();
        if(showValueOnGameboard && soT.getStoredValues() != null){
           for(int i = 0; i < soT.getStoredValues().length; i++){
               Types.ACTIONS act = soT.getStoredAction(i);
               int[] indices = Helper.getIntsFromAction(act);
               int j = indices[0] % ConfigEWN.BOARD_SIZE;
               int ii = (indices[0]-j) / ConfigEWN.BOARD_SIZE;
               double x = soT.getStoredValues()[i];
               board[ii][j].updateValue(indices[1],x,size);
           }
        }
        updateComponents(soT, showValueOnGameboard);
    }

   }


   public void updateComponents(StateObserverEWN so, boolean showValueOnGameboard){
        for(int i = 0; i < size; i++){
            for(int k = 0; k<size; k++){
                // Update Token
                board[i][k].updateTile(so.getGameState()[i][k]);
            }
        }
        this.repaint();
   }

   public int getFrom(){
        return from;
   }

   public void setFrom(int u){
        this.from = u;
   }

   public GameBoardEWN getM_gb(){
        return m_gb;
    }
}

