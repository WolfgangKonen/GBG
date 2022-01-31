package ludiiInterface.general;

import game.Game;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.mcts.MCTS;
import search.mcts.backpropagation.MonteCarloBackprop;
import search.mcts.finalmoveselection.MaxAvgScore;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.ProgressiveHistory;
import utils.AIFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Short bare-bones example of loading different Ludii AIs
 */
public class LudiiAITest {

    static Game ludiiGame;

    public static void main(String[] args){

        //Create AI via the AIFactory from name
        AI PLAYER_1 = AIFactory.createAI("UCT");
        //Create an AI by directly constructing a MCTS AI and choosing the strategies
        AI PLAYER_2 = new MCTS(new ProgressiveHistory(),new RandomPlayout(200),new MonteCarloBackprop(),new MaxAvgScore());
        PLAYER_2.setFriendlyName("Custom MCTS");

        ludiiGame = GameLoader.loadGameFromName("Reversi.lud");
        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<AI>();
        ais.add(null); //Need to add a null entry, because Ludii starts player numbering on 1
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);


       ludiiGame.start(context);

       //Initialize the AIs
        for (int i = 1; i < ais.size(); i++) {
            ais.get(i).initAI(ludiiGame,i);
        }
        System.out.println(ais.get(1).friendlyName() + " vs. " + ais.get(2).friendlyName());

        final Model model = context.model();
        System.out.print("Playing:");
        while (!context.trial().over()){
            model.startNewStep(context,ais,0.1);
            System.out.print(".");
        }
        System.out.println("\n Winner: " +ais.get(context.trial().status().winner()).friendlyName());

    }
}
