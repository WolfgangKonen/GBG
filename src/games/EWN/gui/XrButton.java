package games.EWN.gui;

import games.Arena;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import tools.Types;
import tools.Types.ACTIONS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;

/**
 * A {@link JButton} extension for a button with rounded corners. <br>
 * (from <a href="https://stackoverflow.com/questions/1007116/how-to-create-a-jbutton-extension-with-rounded-corners">
 *     https://stackoverflow.com/questions/1007116/how-to-create-a-jbutton-extension-with-rounded-corners</a>)
 *
 * This button class is used to increment or decrement the dice value in INSPECTV mode, see {@link #mouseClicked(MouseEvent)}
 */
public class XrButton extends JButton implements MouseListener {

    private static final long serialVersionUID = 9032198251140247116L;

    String text;
    GameBoardEWN m_gb;
    boolean mouseIn = false;

    public XrButton(String s, GameBoardEWN gb) {
        super(s);
        text = s;
        m_gb = gb;
        setBorderPainted(false);
        addMouseListener(this);
        setContentAreaFilled(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (getModel().isPressed()) {
            g.setColor(g.getColor());
            g2.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
        }
        super.paintComponent(g);

        if (mouseIn)
            g2.setColor(Color.red);
        else
            g2.setColor(new Color(128, 0, 128));

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(1, 1, (getWidth() - 3),
                (getHeight() - 3), 12, 8));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(4, getHeight() - 3, getWidth() - 4, getHeight() - 3);

        g2.dispose();
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());

        XrButton xrButton = new XrButton("XrButton",null);
        JButton jButton = new JButton("JButton");

        frame.getContentPane().add(xrButton);

        frame.getContentPane().add(jButton);

        frame.setSize(150, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Change member {@link StateObserverEWN} {@code m_so} in {@link GameBoardEWN}: increment or decrement {@code m_so}'s
     * dice value {@code nextNondeterministicAction} (only if it stays in range)
     * @param e (required by the interface, not needed here)
     */
    public void mouseClicked(MouseEvent e) {
        Arena.Task aTaskState = m_gb.m_Arena.taskState;
        if( aTaskState == Arena.Task.INSPECTV) {
            StateObserverEWN theState = (StateObserverEWN)m_gb.getStateObs();
            ACTIONS diceAction = theState.getNextNondeterministicAction();     // diceAction is one smaller than the value shown on game board
            if (text.equals("-") && diceAction.toInt()>0) {
                theState.advanceNondeterministic(new ACTIONS(diceAction.toInt()-1));
                m_gb.setActionReq(true);
            }
            if (text.equals("+") && diceAction.toInt()<theState.getNumAvailableRandoms()-1)  {
                theState.advanceNondeterministic(new ACTIONS(diceAction.toInt()+1));
                m_gb.setActionReq(true);
            }
            // only debug info:
            if (m_gb.isActionReq()) {
                StateObserverEWN newState = (StateObserverEWN)m_gb.getStateObs();
                System.out.println("OLD:"+diceAction.toInt()+",   NEW:"+newState.getNextNondeterministicAction().toInt());
            }

        }
    }

    public void mouseEntered(MouseEvent e) {
        mouseIn = true;
    }

    public void mouseExited(MouseEvent e) {
        mouseIn = false;
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}