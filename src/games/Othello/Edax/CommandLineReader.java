package games.Othello.Edax;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineReader implements Runnable
{
	private final InputStream inputStream;
	private final Pattern regexPattern;
	private final int regexGroup;
	
	private String lastMatch;
	
	/**
	 * Creates a CommandLineReader
	 * @param input The InputStream of the CommandLine
	 */
	public CommandLineReader(InputStream input)
	{
		this(input, ".*", 0);
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
	}
	
	public void run()
	{
		try
		{
			final byte[] buffer = new byte[1024];
			while (inputStream.read(buffer) != -1)
			{
				String str = new String(buffer, StandardCharsets.UTF_8);

				Matcher m = regexPattern.matcher(str);
				if(m.find()) {
					lastMatch = m.group(regexGroup);				
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
		return lastMatch;
	}
}
