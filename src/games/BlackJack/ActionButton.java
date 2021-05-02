package games.BlackJack;

import javax.swing.*;

public class ActionButton extends JButton {
    int iAction;
    double actionValue;
    String name;

    /**
     * Wraps an action and a button. This simplifies the assignement of an evaluation value made from an agent for this action.
     * @param text text of button
     * @param iAction action as integer
     */
    public ActionButton(String text, int iAction){
        super(text);
        this.iAction = iAction;
        this.name = text;
    }

    /**
     * Assigns an evaluation value to this action
     * @param actionValue Evaluation value made from Agent for this action
     */
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
