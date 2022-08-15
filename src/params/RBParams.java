package params;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RBParams extends Frame{



    private static final long serialVersionUID = 1L;

    JLabel useRp_L;
    JLabel capacity_L;
    JLabel batchSize_L;
    JLabel pusher_L;
    JLabel selector_L;

    public Checkbox useRP_T;
    public JTextField capacity_T;
    public JTextField batchSize_T;
    private String[] pushers = new String[]{"All"};
    public JComboBox<String> pusher_cb;

    public JComboBox<String> selector_cb;
    private String[] selectors = new String[]{"Random"};

    Button ok;
    JPanel ePanel;
    RBParams e_par;

    public RBParams() {
        super("Buffer");
        useRP_T = new Checkbox();
        useRp_L = new JLabel("Use replay buffer");

        capacity_T = new JTextField("200"); //
        capacity_L = new JLabel("Buffer size");

        batchSize_L = new JLabel("Batch size");
        batchSize_T = new JTextField("1");

        selector_L = new JLabel("Selector");
        selector_cb = new JComboBox<>(selectors);

        pusher_L = new JLabel("Pusher");
        pusher_cb = new JComboBox<>(pushers);

        ok = new Button("OK");
        e_par = this;
        ePanel = new JPanel(); 	// put the inner buttons into panel oPanel. This
        // panel
        // can be handed over to a tab of a JTabbedPane
        // (see class TicTacToeTabs)

        useRp_L.setToolTipText("Use the replay buffer");
        capacity_L.setToolTipText("Amount of transitions in the buffer");
        batchSize_L.setToolTipText("The amount of transitions sampled from the buffer for each episode (Amount of avg. game length)");
        selector_L.setToolTipText("The policy used for selecting a transition from the replay buffer");
        pusher_L.setToolTipText("The policy used for adding transitions to the replay buffer");
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
        ePanel.add(batchSize_L);
        ePanel.add(batchSize_T);

        ePanel.add(selector_L);
        ePanel.add(selector_cb);
        ePanel.add(pusher_L);
        ePanel.add(pusher_cb);


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
    public void setCapacity(int value) {
        capacity_T.setText(value + "");
    }

    public boolean getUseRb() {
        return useRP_T.getState();
    }
    public void setUseRb(boolean value) {
        useRP_T.setState(value);
    }

    public void setBatchSize(int i){batchSize_T.setText(i+"");}
    public int getBatchSize(){return Integer.valueOf(batchSize_T.getText()).intValue();}

    public int getPusher(){return pusher_cb.getSelectedIndex();}
    public void setPusher(int i){pusher_cb.setSelectedIndex(i);}

    public int getSelector(){return selector_cb.getSelectedIndex();}
    public void setSelector(int i){ selector_cb.setSelectedIndex(i);}

    /**
     * Needed to restore the param tab with the parameters from a re-loaded
     * agent
     *
     * @param ep
     *            ParRB of the re-loaded agent
     */
    public void setFrom(ParRB ep) {
        this.setUseRb(ep.getUseRB());
        this.setCapacity(ep.getCapacity());
        this.setBatchSize(ep.getBatchSizeV());
        this.setSelector(ep.getSelectorV());
        this.setPusher(ep.getPusherV());
    }
}
