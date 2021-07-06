package games.EWN.gui;

import games.Arena;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverHelper.Token;
import games.EWN.constants.ConfigEWN;
import tools.Types;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        east = new JLabel("");
        south = new JLabel("",SwingConstants.CENTER);
        west = new JLabel("");
        token.setOpaque(true);
        this.add(north, BorderLayout.NORTH);
        this.add(east, BorderLayout.EAST);
        this.add(south, BorderLayout.SOUTH);
        this.add(west, BorderLayout.WEST);
        addListener();
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



    public void markAsSelected(){
        this.setBorder(BorderFactory.createLineBorder(Color.YELLOW,10));
    }

    public void unmarkAsSelected(){
        this.setBorder(BorderFactory.createEmptyBorder());
    }
    private void addListener(){
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GameBoardEWN gb = m_bg.getM_gb();
                Arena.Task aTaskState = gb.m_Arena.taskState;
                int index = ((TileGui) e.getSource()).getToken().getIndex();
                if(aTaskState == Arena.Task.PLAY) {
                    gb.hGameMove(index); // Human play
                }else if( aTaskState == Arena.Task.INSPECTV) {
                   // gb.inspectMove(index); // Inspect
                }

            }
        });
    }



}
