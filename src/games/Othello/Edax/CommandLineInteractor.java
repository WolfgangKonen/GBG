package games.Othello.Edax;

import java.io.File;
import java.io.PrintWriter;
import java.util.PriorityQueue;

public class CommandLineInteractor implements Runnable
{
	private PriorityQueue<String> commandQ;
	private boolean keepRunning;
	private CommandLineReader commandLineReader;
	private String regexPattern;
	private int regexGroup;
	
	private ProcessBuilder pb;

	/**
	 * The CommandLineInteractor is used for an Interaction between Java Code and any CommandLineProgramm
	 * @param path The Path to the exe, relative to the project directory                                
	 * @param programName The Program name                                                               
	 */
	public CommandLineInteractor(String path, String programName)
	{
		this(path, programName, ".*", 0);
	}

	/**
	 * The CommandLineInteractor is used for an Interaction between Java Code and any CommandLineProgramm
	 * @param path The Path to the Exe, relative to the project directory
	 * @param programName The Program name
	 * @param responsePattern A regex to converte the commandline output to useful information
	 * @param responseGroup The RegexGroup that should be grabbed
	 */
	public CommandLineInteractor(String path, String programName, String responsePattern, int responseGroup)
	{
		commandQ = new PriorityQueue<String>();
		keepRunning = true;

		pb = new ProcessBuilder();
		pb.command("cmd.exe", "/c", programName);
		pb.directory(new File(path));
		
		regexPattern = responsePattern;
		regexGroup = responseGroup;
	}
	
	public void run()
	{
		Process p = null;
		PrintWriter pw = null;
		try {
			p = pb.start();
			
			commandLineReader = new CommandLineReader(p.getInputStream(), regexPattern, regexGroup);

			new Thread(commandLineReader).start();

			pw= new PrintWriter(p.getOutputStream(), true); // THIS LINE IS A BITCH!
	
			while(keepRunning)
			{
				if(commandQ.peek() != null)
				{
					pw.println(commandQ.remove());
					try {Thread.sleep(250);}catch(InterruptedException e) {e.printStackTrace();}
				}
			}
			
//			p.waitFor();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			p.destroyForcibly();
			keepRunning = false;
		}
	}

	/**
	 * Queues a command that gets written to the command line
	 * @param command
	 */
	public void queueCommand(String command)
	{
		commandQ.add(command);
	}

	/**
	 * Reads the last command line output, relative to the previously defined regex
	 * @return 
	 */
	public String readLastConsoleOutput()
	{
		return commandLineReader.getLastMatch();
	}
	
	/**
	 * Given a command it returns the reaction of the commandLine program
	 * @param command the given command
	 * @return the programs response
	 */
	public String doAction(String command)
	{
		commandQ.add(command);
		try {Thread.sleep(250);}catch(InterruptedException e) {e.printStackTrace();}
		return commandLineReader.getLastMatch();
	}
	
	/**
	 * Is not working as intendet i guess :(
	 */
	public void stop()
	{
		keepRunning = false;
		
	}
}
