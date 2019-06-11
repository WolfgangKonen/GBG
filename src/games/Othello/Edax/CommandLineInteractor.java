package games.Othello.Edax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class CommandLineInteractor 
{
	private ProcessBuilder processBuilder;
	private Process process;

	private CommandLineReader commandLineReader;
	private PrintWriter printWriter;

	private String regexPattern;
	private int regexGroup;

	public CommandLineInteractor(String path, String programName)
	{
		this(path, programName, "[\\s\\S]*", 0);
	}

	public CommandLineInteractor(String path, String programName, String regexResponsePattern, int regexResponseGroup)
	{
		processBuilder = new ProcessBuilder();
		processBuilder.command("cmd.exe", "/c", programName);
		processBuilder.directory(new File(path));

		regexPattern = regexResponsePattern;
		regexGroup = regexResponseGroup;

		start();
	}

	private void start()
	{
		try {
			process = processBuilder.start();
			new Thread(new SyncPipe(process.getErrorStream(), System.err));
			commandLineReader = new CommandLineReader(process.getInputStream(), regexPattern, regexGroup);
			new Thread(commandLineReader).start();
			printWriter = new PrintWriter(process.getOutputStream(), true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a command to the commandLine and awaits a response
	 * @param command The command to be sent
	 * @param timeOut The amount of time in milliseconds to wait for a response
	 * @return Response of the command line
	 */
	public String sendAndAwait(String command, long timeOut)
	{
		printWriter.println(command);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return commandLineReader.getLastMatch(timeOut);			
	}

	/**
	 * Sends a command to the command line and awaits a response with 5 seconds for the timeOut, see {@code sendAndAwait(String command, long timeOut)} for reference
	 * @param command The command to be sent
	 * @return Response of the command line
	 */
	public String sendAndAwait(String command)
	{
		return sendAndAwait(command, 5000);
	}
	
	/**
	 * Sends a command to the command line without expecting a response
	 * @param command The command to be sent
	 */
	public void sendCommand(String command)
	{
		printWriter.println(command);
	}

	/**
	 * Stops the process
	 */
	public void stop()
	{
		process.destroy();
	}
}

class SyncPipe implements Runnable
{
    public SyncPipe(InputStream istrm, OutputStream ostrm)
   {
      istrm_ = istrm;
      ostrm_ = ostrm;
    }
    public void run()
    {
        try
        {
            final byte[] buffer = new byte[1024];
            for (int length = 0; (length = istrm_.read(buffer)) != -1; )
            {
                ostrm_.write(buffer, 0, length);
                ostrm_.flush();
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