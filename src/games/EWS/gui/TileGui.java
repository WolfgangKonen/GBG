package games.EWS.gui;

import games.EWS.GameBoardEWS;
import games.EWS.StateObserverHelper.Token;
import metadata.graphics.board.Board;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TileGui extends JPanel{

    private Token t = null;
    private BoardGui m_bg;
    private JLabel token,north,east,south,west;

    public TileGui(Token t, BoardGui bg){
        this.setLayout(new BorderLayout());
        this.m_bg = bg;
        this.setMinimumSize(new Dimension(30,30));
        this.setBackground(Color.LIGHT_GRAY);
        this.setBackground(Types.GUI_BGCOLOR);
        this.token = new JLabel("",SwingConstants.CENTER);
        this.add(token, BorderLayout.CENTER);
        north = new JLabel("", SwingConstants.CENTER);
        token.setOpaque(true);
        east = new JLabel("");
        south = new JLabel("",SwingConstants.CENTER);
        west = new JLabel("");
        this.add(north, BorderLayout.NORTH);
        this.add(east, BorderLayout.EAST);
        this.add(south, BorderLayout.SOUTH);
        add(west, BorderLayout.WEST);
        this.addMouseListener(new Listener());
        updateTile(t);
        this.setVisible(true);
    }

    public BoardGui getBoardGui(){return this.m_bg;};

    public Token getToken(){
        return t;
    }

    public void updateTile(Token t){
        this.t = t;
        int player = t.getPlayer();
        if(player == -1){
            token.setBackground(Types.GUI_BGCOLOR);
            token.setText("");
        }else {
            token.setBackground(Types.GUI_PLAYER_COLOR[player]);
            token.setText( String.valueOf(t.getValue() +1));
            if(player == 0){
                token.setForeground(Color.WHITE);
            }else {
                token.setForeground(Color.BLACK);
            }
        }
    }

    private class Listener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            int index = ((TileGui) e.getSource()).getToken().getIndex();
            int from = ((TileGui) e.getSource()).getBoardGui().getFrom();
            if(from == -1){
                ((TileGui) e.getSource()).getBoardGui().setFrom(index);
            }else{
                ((TileGui) e.getSource()).getBoardGui().getGameBoardEWS().hGameMove(from,index);
                ((TileGui) e.getSource()).getBoardGui().setFrom(-1);
            }

        }

    }


}
