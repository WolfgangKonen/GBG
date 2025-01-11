package params;

import games.Arena;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class SB3Params extends Frame implements Serializable {
    private static final String TIP_ONE_HOT = "Should the ObservationVector be one hot encoded?";

    private JLabel oneHotLabel;
    private JCheckBox oneHotCheckBox;

    JPanel mPanel;

    public SB3Params() {
        super("SB3 Parameter");

        oneHotLabel = new JLabel("one hot encode: ");
        oneHotCheckBox = new JCheckBox();
        oneHotCheckBox.setSelected(ParSB3.DEFAULT_ONE_HOT);



        // Set tool tips
        oneHotLabel.setToolTipText(TIP_ONE_HOT);

        this.mPanel = new JPanel();

        setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
        mPanel.setLayout(new GridLayout(0,2,10,10));

        mPanel.add(oneHotLabel);
        mPanel.add(oneHotCheckBox);

        pack();
        setVisible(false);
    }

    public JPanel getPanel() {
        return mPanel;
    }
}
