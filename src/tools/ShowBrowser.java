package tools;

import javax.swing.JOptionPane;

//import org.apache.commons.compress.utils.IOUtils;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Utility class to open URL. Display web page URL in the user's default browser
 * or a PDF URL in the user's default PDF viewer.
 * <p>
 * This class works also if the web page or PDF URL is contained in a JAR. Then the URL is copied
 * from the JAR to a temporary file and then displayed. <br> 
 * Precondition: The URL is in only one file, not distributed over several files. 
 */
public class ShowBrowser {

   static final String[] browsers = { "google-chrome", "firefox", "opera",
      "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
   static final String errMsg = "Error attempting to launch web browser";
   
   /**
    * Opens the specified web page in the user's default browser. <p>
    * 
    * NOTE: This mechanism works for PDFs as well (at least under Windows)
    * since the runtime file dispatcher looks at the file ending (e.g. .pdf) and 
    * connects it automatically with the right program (e.g. PDF viewer) to view it.
    * 
    * @param URL A web address (URL) of a web page (ex: "http://www.google.com/")
    * @param is	 the input stream for this URL
    */
   public static void openURL(java.net.URL URL, InputStream is) {
	   
	   String url = URL.toString();
       String osName = System.getProperty("os.name");
       String suffix = ".htm";
       
       if (url.startsWith("rsrc")) {
    	   // This happens if the main program is a JAR (not a Java application), then URL is a resource within the 
    	   // JAR file and starts with "rsrc:".
    	   // The browser cannot display it directly, instead we have to copy the content of URL to a local temp file.
    	   // We do this by copying the input stream of URL to the temp file and then display it. 
    	   //
    	   // Drawback: HTM URLs distributed over several files (e.g. base HTM with images in subdirectory) do only 
    	   // display the base HTM correctly, the images are missing. Workaround: Do not use HTM display, use instead 
    	   // the parallel PDF display. A PDF is always in one file.
    	   //
    	   // Method from: https://coderanch.com/t/551554/java/opening-html-file-packed-jar
           try {
        	   if (url.endsWith(".pdf")) suffix=".pdf";
        	   if (url.endsWith(".html")) suffix=".html";
         	    File temp = File.createTempFile("GBG_",suffix);
           	    Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
           	    temp.deleteOnExit();
                Desktop.getDesktop().browse(temp.toURI());    	   
           } catch (Exception e) {
        	   JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
           }
       }
       else
       {
    	   // This is the normal case, if main program is a Java application
    	   // Then URL starts with "file:..." and we can display it directly
    	   /*
    	    * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
    	    * Example Usage:<code><br> &nbsp; &nbsp;
    	    *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
    	    *    ShowBrowser.openURL(url);<br></code>
    	    * Latest Version: <a href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a><br>
    	    * 
    	    * Public Domain Software -- Free to Use as You Like
    	    * @author: Dem Pilafian
    	    * @version 3.1, June 6, 2010
    	    */
           try {  //attempt to use Desktop library from JDK 1.6+
               Class<?> d = Class.forName("java.awt.Desktop");
               d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
                  d.getDeclaredMethod("getDesktop").invoke(null),
                  new Object[] {java.net.URI.create(url)});
               }
           catch (Exception ignore) {  //library not available or failed
              try {
                  if (osName.startsWith("Mac OS")) {
                     Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                        "openURL", new Class[] {String.class}).invoke(null,
                        new Object[] {url});
                     }
                  else if (osName.startsWith("Windows"))
                     Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + url);
                  else { //assume Unix or Linux
                     String browser = null;
                     for (String b : browsers)
                        if (browser == null && Runtime.getRuntime().exec(new String[]
                              {"which", b}).getInputStream().read() != -1)
                           Runtime.getRuntime().exec(new String[] {browser = b, url});
                     if (browser == null)
                        throw new Exception(Arrays.toString(browsers));
                     }
                  }
               catch (Exception e) {
                  JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
               }
            } // catch
       } // else
   }
 }
