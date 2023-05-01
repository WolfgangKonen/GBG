package games.Poker;

import controllers.PlayAgent;
import games.*;
import tools.Types;

import java.io.IOException;
import java.util.ArrayList;


public class ArenaPoker extends Arena   {

	public ArenaPoker(String title, boolean withUI) {
		super(title,withUI);		
	}

	public ArenaPoker(String title, boolean withUI, boolean withTrainRights) {
		super(title,withUI,withTrainRights);
	}

	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "Poker";
	}

	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardPoker(this);
		return gb;
	}


	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
		return new EvaluatorPoker(pa,gb, mode,0);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeaturePoker(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsPoker();
	}

//    public void performArenaDerivedTasks() {}


	@Override
	public String gameOverString(StateObservation so, ArrayList<String> agentVec) {
		String goStr="";
		StateObserverPoker sop = (StateObserverPoker) so;
		for(int i = 0;i<so.getNumPlayers();i++){
			if(sop.getChips()[i]>0) {
				goStr+= Types.GUI_PLAYER_NAME[i]+ " has won!";
				break;
			}
		}
		return goStr;
	}

	public static void main(String[] args) throws IOException 
	{
		PokerLog.setup();
		ArenaPoker t_Frame = new ArenaPoker("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			String args_txt = "";
			for(String arg : args)
				args_txt = args_txt.concat("["+ arg +"]");
			throw new RuntimeException("[Arena.main] args="+args_txt+" not allowed.");
		}
	}
	
}
