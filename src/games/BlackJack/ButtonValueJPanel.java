package games.BlackJack;

import javax.swing.*;
import java.awt.*;

public class ButtonValueJPanel extends JPanel {

    ActionButton button;
    JPanel valuePanel;

    /**
     * Displays an ActionButton next to his evaluation score.
     * @param button the ActionButton (contains the assigned score)
     */
    public ButtonValueJPanel(ActionButton button){
        super();
        this.setLayout(new GridBagLayout());
        this.button = button;
        update();
    }

    /**
     * Updates the button/score panel.
     */
    public void update(){
        this.removeAll();
        GridBagConstraints c1 = new GridBagConstraints();

        c1.fill = GridBagConstraints.BOTH;
        c1.weighty = 1;
        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = .8;
        this.add(button, c1);


        if(valuePanel != null) {
            GridBagConstraints c2 = new GridBagConstraints();
            c2.weighty = 1;
            c2.fill = GridBagConstraints.HORIZONTAL;
            c2.gridy = 0;
            c2.gridx = 1;
            c2.weightx = .2;
            this.add(valuePanel, c2);
            valuePanel.setBorder(BorderFactory.createLineBorder(Color.black,1));
        }
    }

    public void setButton(ActionButton button) {
        this.button = button;
    }

    public void setValuePanel(JPanel valuePanel) {
        this.valuePanel = valuePanel;
    }

    public ActionButton getButton() {
        return button;
    }

    public JPanel getValuePanel() {
        return valuePanel;
    }
}
