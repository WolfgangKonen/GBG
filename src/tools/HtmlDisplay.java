package tools;

import java.awt.Font;

// from http://www.java.happycodings.com/Java_Swing/code22.html +

import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;


public class HtmlDisplay extends JFrame implements HyperlinkListener
{
     public static void main(String[] args) {
         HtmlDisplay obj=new HtmlDisplay("HelpGUI.htm");
         obj.setSize(400,500);
         obj.setVisible(true);
     }
     
     URL url;
     JEditorPane html;
     Document doc;
     Font font; 
     private static final long serialVersionUID = 1L;
     
     public HtmlDisplay(String filename)
     {
    	 try{

    		 UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    		 // --- Different solutions for loading the HTML-file as resource --------
    		 // --- Most portable solution (works also in JAR): load HTM file as a resource ---
    		 // To make this work, have 
    		 //	   <classpathentry excluding="**/*.java" kind="src" path="resources"/>
    		 // in your file .classpath (Eclipse) or have "resources" in your CLASSPATH environment variable
    		 url=getClass().getResource("/"+filename);
    		 //--- Alternative 1: load resource from in working dir ---
    		 //String curDir = System.getProperty("user.dir"); // fetch the working dir 
    		 //curDir = curDir.replace('\\', '/');
    		 //url=new URL("file:///"+curDir+"/"+filename);
    		 //--- Alternative 2: absolute path to resource, not portable at all ---    		 
    		 //url=new URL("file:///C:/Dokumente%20und%20Einstellungen/wolfgang/Eigene%20Dateien/ProjectsWK/ReinforceLearn/TicTacToe/resources/"+filename);
    		 html=new JEditorPane();
    		 //System.out.println("Editor created");
    		 html.setEditable(false);
    		 //
    		 // this magic work-around from http://stackoverflow.com/questions/12542733/setting-default-font-in-jeditorpane
    		 // is needed so that the font change becomes effective (!!)
    		 html.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    		 font=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);			
    		 html.setFont(font);	
    		 if (url!=null) {
    			 html.setPage(url);
    		 } else {
    			 System.out.println("Warning: Could not find the resource /"+filename+" in the current classpath");
    		 }
    		 html.addHyperlinkListener(this);
    		 JScrollPane scroller = new JScrollPane();
    		 JViewport vp = scroller.getViewport();
    		 vp.add(html);
    		 //vp.setBackingStoreEnabled(true);
    		 vp.setSize(465,620);		
    		 vp.setBounds(0,0,465,620);

    		 //System.out.println("html created");
    		 getContentPane().add(scroller);
    		 //getContentPane().add(html);

    	 }catch(Exception e){e.printStackTrace();}

     }
     public void hyperlinkUpdate(HyperlinkEvent e) {
    	 try{

    		 if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
    		 {

    			 doc = html.getDocument();
    			 //System.out.println(e.getURL());
    			 html.setPage(e.getURL());
    			 //getToolkit().beep();
    			 //System.out.println("Listening");
    		 }

    	 }catch(Exception ex){
    		 // html.setDocument(doc);
    		 ex.printStackTrace();
    	 }
     }
}
