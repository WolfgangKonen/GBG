package tools;

import java.awt.*;
import javax.swing.JLabel;

public class StatusBar extends JLabel {

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of StatusBar */
    public StatusBar() {
        super();
        super.setPreferredSize(new Dimension(200, 16));
        setMessage("Ready");
    }
    
    public void setMessage(String message) {
        setText(" "+message);        
    }        
}
