package params;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.Serializable;

public class ParSB3 implements Serializable {
    public static final boolean DEFAULT_ONE_HOT = true;


    /**
     * This member is only constructed when the constructor {@link #ParSB3(boolean)} (boolean) ParMCTS(boolean withUI)}
     * called with {@code withUI=true}. It holds the GUI for {@link ParSB3}.
     */
    private transient SB3Params msparams = null;

    public ParSB3(boolean withUI) {
        if (withUI)
            msparams = new SB3Params();
    }

    public JPanel getPanel() {
        if (msparams!=null)
            return msparams.getPanel();
        return null;
    }
}
