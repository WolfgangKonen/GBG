package gui;

import javax.swing.*;
import javax.swing.plaf.*;
import java.awt.event.*;
import java.awt.*;

public class TooltipInSwing extends JFrame {
 public TooltipInSwing() {
        super("TooltipInSwing");
        setSize(400, 300);
        getContentPane().setLayout(new FlowLayout());

        // globally
        UIManager.put("ToolTip.font",
           new FontUIResource("SansSerif", Font.BOLD, 18));
        JButton b1 = new JButton("tooltip 1");
        b1.setToolTipText("tool tip sansserif bold");

        getContentPane().add(b1);

        // only one
        String html =
            "<html><p><font color=\"#800080\" " +
            "size=\"4\" face=\"Verdana\">tool tip verdana" +
            "</font></p></html>";
        JButton b2 = new JButton("tooltip 2");
        b2.setToolTipText(html);

        getContentPane().add(b2);

        WindowListener wndCloser = new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
                }
            };
        addWindowListener(wndCloser);
        setVisible(true);
    }

    public static void main(String args[]){
     new TooltipInSwing();
     }
}