package tools;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * 
 * @author wolfgang
 *
 */
public class MessageBox {
	String[] test = {"1", "2"};
	public static void show (Component parent, String message, String title, 
							 int msgCode) 
	{
		Font font=new Font("Arial",1,(int)(1.2*Types.GUI_HELPFONTSIZE)); //16			
		show(parent, message, title, msgCode, font);
	}

	public static void show (Component parent, String message, String title, 
			 				 int msgCode, Font font) 
	{
		JLabel obj = new JLabel(message);
		obj.setFont(font);
		JOptionPane.showMessageDialog(parent, obj, title, msgCode);
	}
}
