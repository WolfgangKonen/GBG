package agentIO;

import java.io.IOException;

import controllers.PlayAgent;
import games.Arena;
import tools.Types;

/**
 *	A class to load a specific agent from disk. 
 *  <p>
 *	After construction, the agent is available with {@link #getAgent()}. If something goes 
 *  wrong with loading, then {@link #getAgent()} returns {@code null} and {@link #getLoadMsg()} has the error message.   
 *
 */
public class AgentLoader {
	private final PlayAgent pa;
	private final Arena m_Arena;
	private String loadMsg = "";

	/**
	 * 
	 * @param arena		the game arena, needed to access game-specific settings
	 * @param filename	the filename of the agent to load from the game-specific agent directory
	 * 					{@code agents/gameName/subdir/}. If null, set it to {@code TDReferee.agt.zip}. 
	 */
	public AgentLoader(Arena arena, String filename) {
		m_Arena = arena;
		if (filename==null) filename="TDReferee.agt.zip";
		pa = loadTDreferee(filename);
	}

	public String getLoadMsg() { return loadMsg; }
	public PlayAgent getAgent() { return pa; }
	
	private PlayAgent loadTDreferee(String filename) {
    	// try to load agent 'filename' from the game-specific agent directory
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+m_Arena.getGameName();
		String subDir = m_Arena.getGameBoard().getSubDir();
		PlayAgent tdreferee = null;
		if (subDir != null){
			strDir += "/"+subDir;
		}
		tools.Utils.checkAndCreateFolder(strDir);
		String filePath = strDir += "/"+filename;
		try {
			tdreferee = m_Arena.tdAgentIO.loadGBGAgent(filePath);			
		} catch(IOException e) {
			loadMsg = e.getMessage();			
		} catch(Exception e) {
			loadMsg = e.getMessage();
		}
		
		//if (tdreferee==null) loadMsg = "Could not load TDReferee.agt.zip";
		return tdreferee;
	}
	

}
