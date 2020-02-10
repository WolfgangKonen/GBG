package gui;

import java.awt.*;
import javax.swing.JLabel;

import tools.Types;

public class StatusBar extends JLabel {

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of StatusBar */
    public StatusBar() {
        super();
        super.setPreferredSize(new Dimension(200, 16));
        super.setBackground(Types.GUI_BGCOLOR);
        setMessage("Ready");
    }
    
    public void setMessage(String message) {
        setText(" "+message);    
        super.repaint();
    }   
    
    public void setFont(Font font) {
        super.setFont(font);
    }
}
