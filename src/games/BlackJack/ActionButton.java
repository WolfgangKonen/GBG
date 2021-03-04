package games.BlackJack;

import javax.swing.*;

public class ActionButton extends JButton {
    int iAction;
    double actionValue;
    String name;

    public ActionButton(String text, int iAction){
        super(text);
        this.iAction = iAction;
        this.name = text;
    }

    public void setActionValue(double actionValue) {
        this.actionValue = actionValue;
    }

    public int getiAction() {
        return iAction;
    }

    public double getActionValue() {
        return actionValue;
    }

    @Override
    public String getName() {
        return name;
    }
}
