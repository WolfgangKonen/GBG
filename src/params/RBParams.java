package params;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RBParams extends Frame{



    private static final long serialVersionUID = 1L;

    JLabel useRp_L;
    JLabel capacity_L;
    public Checkbox useRP_T;
    public JTextField capacity_T;
//	public Checkbox chooseS01;
//	public Checkbox learnRM;
//	public Checkbox rewardIsGameScore;

    Button ok;
    JPanel ePanel;
    RBParams e_par;

    public RBParams() {
        super("Other Parameter");

        capacity_T = new JTextField("200"); //
        useRP_T = new Checkbox();
        useRp_L = new JLabel("Use replay buffer");
        capacity_L = new JLabel("Amount of Transitions");
        ok = new Button("OK");
        e_par = this;
        ePanel = new JPanel(); 	// put the inner buttons into panel oPanel. This
        // panel
        // can be handed over to a tab of a JTabbedPane
        // (see class TicTacToeTabs)

        useRp_L.setToolTipText("Use the replay buffer");
        capacity_L.setToolTipText("Amount of transitions in the buffer");

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                e_par.setVisible(false);
            }
        });

        setLayout(new BorderLayout(10, 0)); // rows,columns,hgap,vgap
        ePanel.setLayout(new GridLayout(0, 4, 10, 10));

        ePanel.add(useRp_L);
        ePanel.add(useRP_T);
        ePanel.add(new Canvas());
        ePanel.add(new Canvas());

        ePanel.add(capacity_L);
        ePanel.add(capacity_T);
        ePanel.add(new Canvas());
        ePanel.add(new Canvas());

        ePanel.add(new Canvas());
        ePanel.add(new Canvas());
        ePanel.add(new Canvas());
        ePanel.add(new Canvas());


        add(ePanel, BorderLayout.CENTER);
        add(ok, BorderLayout.SOUTH);

        pack();
        setVisible(false);

    } // constructor OtherParams()

    public JPanel getPanel() {
        return ePanel;
    }

    public int getCapacity() {
        return Integer.valueOf(capacity_T.getText()).intValue();
    }

    public boolean getUseRb() {
        return useRP_T.getState();
    }

    public void setCapacity(int value) {
        capacity_T.setText(value + "");
    }

    public void setUseRb(boolean value) {
        useRP_T.setState(value);
    }

    /**
     * Needed to restore the param tab with the parameters from a re-loaded
     * agent
     *
     * @param ep
     *            ParOther of the re-loaded agent
     */
    public void setFrom(ParRB ep) {
        this.setUseRb(ep.getUseRB());
        this.setCapacity(ep.getCapacity());
    }
}
