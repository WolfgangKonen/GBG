package games.KuhnPoker;

import controllers.PlayAgent;
import games.*;
import tools.Types;

import java.io.IOException;
import java.util.ArrayList;


public class ArenaKuhnPoker extends Arena   {
	public ArenaKuhnPoker(String title, boolean withUI) {
		super(title,withUI);		
	}

	public ArenaKuhnPoker(String title, boolean withUI, boolean withTrainRights) {
		super(title,withUI,withTrainRights);
	}

	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "KuhnPoker";
	}

	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardKuhnPoker(this);
		return gb;
	}


	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
		return new EvaluatorKuhnPoker(pa,gb, mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeatureKuhnPoker(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsKuhnPoker();
	}

//    public void performArenaDerivedTasks() {}


	@Override
	public String gameOverString(StateObservation so, ArrayList<String> agentVec) {
		String goStr="";
		StateObserverKuhnPoker sop = (StateObserverKuhnPoker) so;
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
		ArenaKuhnPoker t_Frame = new ArenaKuhnPoker("General Board Game Playing",true);

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
