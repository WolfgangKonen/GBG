package games.Poker;

import game.equipment.component.Card;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {

    private Image img = null;
    private int width;
    private int height;

     CardPanel(int width){

        this.width = width;
        this.height = (int) (width*1.4);
        reset();
    }

    public void setCard(String card){
        java.net.URL url = getClass().getResource("/images/cards/"+card);
        img = getToolkit().createImage(url);
        if(width>0)
            img = img.getScaledInstance(width, height, Image. SCALE_SMOOTH);
        this.repaint();
        this.validate();
    }

    public void reset(){
        setCard("2B.png");
    }

    public void inactive(){
        setCard("1B.png");
    }

    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, this);
    }

    public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
