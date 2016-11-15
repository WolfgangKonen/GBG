package tools;

/**
 * 
 * Get the progress of longer lasting actions. Especially needed for
 * competitions etc.
 * 
 * @author Markus Thill
 */
public interface Progress {
	/**
	 * @return Progress of the current action in percent (0-100)
	 */
	public int getProgress();

	public String getStatusMessage();
}
