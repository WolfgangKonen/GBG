package games.EWN.gui;

import games.Arena;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverHelper.Token;
import games.EWN.config.ConfigEWN;
import params.GridLayout2;
import tools.Types;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TileGui extends JPanel{

    private Token t = null;
    private BoardGui m_bg;
    private int base;

    private JLabel token, north, north_west, west, south_west, south, south_east, east, north_east;

    public TileGui(Token t, BoardGui bg){
        this.setLayout(new BorderLayout());
        this.m_bg = bg;
        this.base = ConfigEWN.BOARD_SIZE;
        this.add(initPanel(), BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(30,30));
        this.setBackground(Color.LIGHT_GRAY);
        this.setBackground(Types.GUI_BGCOLOR);
        addListener();
        updateTile(t);
        this.setBorder(BorderFactory.createLineBorder(Types.GUI_BGCOLOR,3));
        this.setVisible(true);
    }

    public BoardGui getBoardGui(){return this.m_bg;};

    public Token getToken(){
        return t;
    }


    public void emptyValues()
    {
        east.setText(" ");
        north.setText(" ");
        west.setText(" ");
        south.setText(" ");
        south_east.setText(" ");
        south_west.setText(" ");
        north_east.setText(" ");
        north_west.setText(" ");
    }


    private JPanel initPanel()
    {
        JPanel x = new JPanel(new GridLayout2(3,3));

        x.setBackground(Color.LIGHT_GRAY);
        x.setBackground(Types.GUI_BGCOLOR);
        north_west = new JLabel("   ", SwingConstants.CENTER);
        north_west.setOpaque(true);
        x.add(north_west,0);
        north = new JLabel("   ", SwingConstants.CENTER);
        north.setOpaque(true);
        x.add(north,1);
        north_east = new JLabel("   ", SwingConstants.CENTER);
        north_east.setOpaque(true);
        x.add(north_east,2);
        east = new JLabel("   ", SwingConstants.CENTER);
        east.setOpaque(true);
        x.add(east,3);
        token = new JLabel("   ",SwingConstants.CENTER);
        token.setFont(new Font("Serif",Font.BOLD,14));
        token.setOpaque(true);
        x.add(token,4);
        west = new JLabel("   ", SwingConstants.CENTER);
        west.setOpaque(true);
        x.add(west,5);
        south_west = new JLabel("   ", SwingConstants.CENTER);
        south_west.setOpaque(true);
        x.add(south_west,6);
        south = new JLabel("   ", SwingConstants.CENTER);
        south.setOpaque(true);
        x.add(south,7);
        south_east = new JLabel("   ", SwingConstants.CENTER);
        south_east.setOpaque(true);
        x.add(south_east,8);
        x.setVisible(true);
        return x;
    }

    public void updateValue(int index, double val, int size)
    {
        // Get the token index
        int pos = index-t.getIndex();
        String valParsed = String.valueOf((int) (val*100));
        if(pos == -ConfigEWN.BOARD_SIZE-1){
            north_west.setText(valParsed);
        }else if(pos == -ConfigEWN.BOARD_SIZE){
            north.setText(valParsed);
        }else if(pos == -ConfigEWN.BOARD_SIZE+1){
            north_east.setText(valParsed);
        }else if(pos == -1){
            east.setText(valParsed);
        }else if( pos == 1){
            west.setText(valParsed);
        }else if(pos == ConfigEWN.BOARD_SIZE-1){
            south_west.setText(valParsed);
        }else if(pos == ConfigEWN.BOARD_SIZE){
            south.setText(valParsed);
        }else{
            south_east.setText(valParsed);
        }
    }


    public void updateTile(Token t){
        this.t = t;
        int player = t.getPlayer();
        if(player == -1){
            token.setBackground(Types.GUI_BGCOLOR);
            south.setBackground(Types.GUI_BGCOLOR);
            north.setBackground(Types.GUI_BGCOLOR);
            north_west.setBackground(Types.GUI_BGCOLOR);
            north_east.setBackground(Types.GUI_BGCOLOR);
            south_west.setBackground(Types.GUI_BGCOLOR);
            south_east.setBackground(Types.GUI_BGCOLOR);
            east.setBackground(Types.GUI_BGCOLOR);
            west.setBackground(Types.GUI_BGCOLOR);
            token.setText("");
            east.setText(" ");
            north.setText(" ");
            west.setText(" ");
            south.setText(" ");
            south_east.setText(" ");
            south_west.setText(" ");
            north_east.setText(" ");
            north_west.setText(" ");
        }else {
            token.setBackground(Types.GUI_PLAYER_COLOR[player]);
            south.setBackground(Types.GUI_PLAYER_COLOR[player]);
            north.setBackground(Types.GUI_PLAYER_COLOR[player]);
            north_west.setBackground(Types.GUI_PLAYER_COLOR[player]);
            north_east.setBackground(Types.GUI_PLAYER_COLOR[player]);
            east.setBackground(Types.GUI_PLAYER_COLOR[player]);
            west.setBackground(Types.GUI_PLAYER_COLOR[player]);
            south_east.setBackground(Types.GUI_PLAYER_COLOR[player]);
            south_west.setBackground(Types.GUI_PLAYER_COLOR[player]);

            token.setText( String.valueOf(t.getValue() +1));
            if(player == 0){
                token.setForeground(Color.WHITE);
                south.setForeground(Color.WHITE);
                north.setForeground(Color.WHITE);
                north_west.setForeground(Color.WHITE);
                north_east.setForeground(Color.WHITE);
                east.setForeground(Color.WHITE);
                west.setForeground(Color.WHITE);
                south_west.setForeground(Color.WHITE);
                south_east.setForeground(Color.WHITE);
            }else {
                token.setForeground(Color.BLACK);
                south.setForeground(Color.BLACK);
                north.setForeground(Color.BLACK);
                north_west.setForeground(Color.BLACK);
                north_east.setForeground(Color.BLACK);
                east.setForeground(Color.BLACK);
                west.setForeground(Color.BLACK);
                south_west.setForeground(Color.BLACK);
                south_east.setForeground(Color.BLACK);
            }
        }
    }



    public void markAsSelected(){
        this.setBorder(BorderFactory.createLineBorder(Color.YELLOW,3));
    }

    public void unmarkAsSelected(){
        this.setBorder(BorderFactory.createLineBorder(t.getPlayer() >= 0 ? Types.GUI_PLAYER_COLOR[t.getPlayer()] : Types.GUI_BGCOLOR,3));
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
                    gb.inspectMove(index); // Inspect
                }

            }
        });
    }



}
