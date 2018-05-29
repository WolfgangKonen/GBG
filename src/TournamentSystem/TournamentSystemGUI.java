package TournamentSystem;

import games.GameBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TournamentSystemGUI
{
    //private LogManager logManager;
    private GameBoard gameBoard;
    private JFrame jFMain = new JFrame("Tournament System");

    public TournamentSystemGUI(/*LogManager logManager,*/ GameBoard gameBoard) {
        //this.logManager = logManager;
        this.gameBoard = gameBoard;

        initializeGUI();
    }

    private void initializeGUI()
    {
        JMenu menu;
        JMenuItem menuItem;

        // menu bar
        JMenuBar jmenuBarMain = new JMenuBar();
        jFMain.add(jmenuBarMain);

        // options menu
        menu = new JMenu("Options");
        menuItem = new JMenuItem("option#1");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //...
            }
        });
        menu.add(menuItem);
        jmenuBarMain.add(menu);

        // help menu
        menu = new JMenu("Help");
        menuItem = new JMenuItem("help#1");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //...
            }
        });
        menu.add(menuItem);
        jmenuBarMain.add(menu);


        // show and place window on screen
        jFMain.setSize(500, 500);
        jFMain.setLocation(500, 0);
        //jFMain.add(jPMain);
        jFMain.setVisible(true);
    }

    /**
     * move the LogManagerGUI to the front
     */
    public void show() {
        jFMain.setVisible(true);
        jFMain.setState(Frame.NORMAL);
        jFMain.toFront();
    }
}
