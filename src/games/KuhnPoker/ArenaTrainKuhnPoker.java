package games.KuhnPoker;

import TournamentSystem.tools.TSGameDataTransfer;
import controllers.PlayAgent;
import games.*;
import tools.Types;

import java.io.IOException;

public class ArenaTrainKuhnPoker extends ArenaTrain   {

	public ArenaTrainKuhnPoker(String title, boolean withUI) {
		super(title,withUI);		
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
		gb = new GameBoardKuhnPoker(this);
		return gb;
	}

	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorKuhnPoker(pa,gb,stopEval,mode,verbose);
	}

	public Feature makeFeatureClass(int featmode) {
		return new FeatureKuhnPoker(featmode);
	}

	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsPoker();
	}

	public static void main(String[] args) throws IOException 
	{
		PokerLog.setup();
		ArenaTrainKuhnPoker t_Frame = new ArenaTrainKuhnPoker("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			String args_txt = "";
			for(String arg : args)
				args_txt = args_txt.concat("["+ arg +"]");
			throw new RuntimeException("[Arena.main] args="+args_txt+" not allowed.");
		}
	}
	@Override
	public String gameOverString(StateObservation so, String[] agentVec) {
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
}
