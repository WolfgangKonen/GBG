package games.Othello;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {

	public static void main(String[] args)
	{		
		Process p = null;
		System.out.flush();
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("cmd.exe", "/c", "edax.exe");
			pb.directory(new File("agents\\Othello\\Edax"));
			p = pb.start();
			new Thread(new SyncPipe(p.getErrorStream(), System.err), System.currentTimeMillis() + "").start();
		    new Thread(new SyncPipe(p.getInputStream(), System.out), System.currentTimeMillis() + "").start();
		    
		    PrintWriter pw = new PrintWriter(p.getOutputStream(), true);
		   
		    pw.println("mode 1");
		    Thread.sleep(250);
		    for(int i = 0; i < 7; i++)
		    {
		    	pw.println("f4");
		    	Thread.sleep(250);		    	
		    }
//		    pw.println("f6");
//		    Thread.sleep(250);
//		    pw.println("c4");
//		    Thread.sleep(250);
		    pw.close();
		    p.waitFor();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			p.destroyForcibly();
		}
	}	
	
	public static class SyncPipe implements Runnable
	{
	public SyncPipe(InputStream istrm, OutputStream ostrm) {
	      istrm_ = istrm;
	      ostrm_ = ostrm;
	  }
	  public void run() {
		  try
	      {
	          final byte[] buffer = new byte[1024];
	          for (int length = 0; (length = istrm_.read(buffer)) != -1; )
	          {
//	              ostrm_.write(buffer, 0, length);
	        	  String str = new String(buffer, StandardCharsets.UTF_8);
	        	  Pattern test = Pattern.compile(".*[eE]dax plays ([A-z][0-8]).*");
	        	  
	        	  Matcher m = test.matcher(str);
	        	  if(m.find())
	        		  System.out.println(m.group(1));
	          }
	      }
	      catch (Exception e)
	      {
	          e.printStackTrace();
	      }
	  }
	  private final OutputStream ostrm_;
	  private final InputStream istrm_;
	}
}
