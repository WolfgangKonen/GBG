package games.Othello.Edax;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineReader implements Runnable
{
	private final InputStream inputStream;
	private final Pattern regexPattern;
	private final int regexGroup;
	
	private Semaphore matchSemaphore;
	
	private String lastMatch;
	
	/**
	 * Creates a CommandLineReader
	 * @param input The InputStream of the CommandLine
	 */
	public CommandLineReader(InputStream input)
	{
		this(input, "[\\s\\S]*", 0);
	}
	
	/**
	 * Creates a CommandLineReader
	 * @param input The inputstream of the commandLine
	 * @param pattern The regex pattern the Reader is looking for
	 * @param group The regex group the Reader is looking for
	 */
	public CommandLineReader(InputStream input, String pattern, int group)
	{
		inputStream = input;
		regexPattern = Pattern.compile(pattern);
		regexGroup = group;
		
		matchSemaphore = new Semaphore(0);
	}
	
	public void run()
	{
		try
		{
			final byte[] buffer = new byte[2048];
			while (inputStream.read(buffer) != -1)
			{
				String str = new String(buffer, StandardCharsets.UTF_8);

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
	
	public String getLastMatch()
	{
		try 
		{
			if(matchSemaphore.tryAcquire(5000, TimeUnit.MILLISECONDS))
			{
				return lastMatch;
			}
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		return "";
	}
}
