package games.Othello;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

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
			Thread error = new Thread(new SyncPipe(p.getErrorStream(), System.err), System.currentTimeMillis() + "");
			error.start();
		    Thread output = new Thread(new SyncPipe(p.getInputStream(), System.out), System.currentTimeMillis() + "");
		    output.start();
		    
		    PrintWriter pw = new PrintWriter(p.getOutputStream());
		    pw.write("mode 1");
		    pw.write("play f4");
		    pw.write("f3");
		    
//		    pw.println("D3");
//		    pw.flush();
//		    pw.print("");
//		    pw.print("D6");
		    pw.close();
		    p.waitFor();
//		    p.destroyForcibly();
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
	              ostrm_.write(buffer, 0, length);
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
