package games.Othello.Edax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineReader implements Runnable
{
	private final InputStream inputStream;
	private final Pattern regexPattern;
	private final int regexGroup;
	private BufferedReader bufferedReader;
	
	private Semaphore matchSemaphore;
	
	private String lastMatch;
	
	/**
	 * Creates a CommandLineReader that will return the whole content of the CommandLine
	 * @param input The InputStream of the CommandLine
	 */
	public CommandLineReader(InputStream input)
	{
		this(input, "[\\s\\S]*", 0);
	}
	
	/**
	 * Creates a CommandLineReader that will only return information according to the given regex
	 * @param input The inputstream of the commandLine
	 * @param pattern The regex pattern the Reader is looking for
	 * @param group The regex group the Reader is looking for
	 */
	public CommandLineReader(InputStream input, String pattern, int group)
	{
		inputStream = input;
		regexPattern = Pattern.compile(pattern);
		regexGroup = group;
		
		bufferedReader = new BufferedReader(new InputStreamReader(input));
		
		matchSemaphore = new Semaphore(0);
	}
	
	/**
	 * The run method. Inherited from {@link Runnable}
	 */
	public void run()
	{
		try
		{
			String line;
			while((line = bufferedReader.readLine()) != null)
			{
				String str = line;

				Matcher m = regexPattern.matcher(str);
				if(m.find()) {
					lastMatch = m.group(regexGroup);
					matchSemaphore.release();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the last match written to the command line
	 * @param timeOut The time in milliseconds to wait for an match
	 * @return The last match
	 */
	public String getLastMatch(long timeOut)
	{
		try 
		{
			if(matchSemaphore.tryAcquire(timeOut, TimeUnit.MILLISECONDS))
			{	
				return lastMatch;
			}
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		throw new RuntimeException("TimeOut");
	}
	
	/**
	 * Returns the last match written to the command line, with a default timeout of 5 seconds
	 * @return the last match
	 */
	public String getLastMatch()
	{
		return getLastMatch(5000);
	}
}
