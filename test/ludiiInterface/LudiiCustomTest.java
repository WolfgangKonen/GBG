package ludiiInterface;

import controllers.PlayAgent;
import game.Game;
import games.Arena;
import games.Hex.HexConfig;
import ludiiInterface.general.GBGAsLudiiAI;
import org.junit.Test;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import starters.GBGBatch;
import starters.SetupGBG;
import utils.LudiiAI;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LudiiCustomTest extends GBGBatch {
    boolean LOGS = true;
    int GAMESNUMBER = 2;
    double MAX_SECONDS = 1.0;

    String selectedGame;
    String[] scaPar;
    String agtFile;

    static final AI PLAYER_1 = new LudiiAI();
    static AI PLAYER_2;
    static Game ludiiGame;

    @Test
    public void customOthelloGame() {
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 =0, ties = 0;

        selectedGame = "Othello";
        scaPar=new String[]{"", "", ""};
        agtFile = "TCL4-100_7_250k-lam05_P4_nPly2-FAm_C.agt.zip";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI(0);
        ludiiGame = GameLoader.loadGameFromName("Reversi.lud");

        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null); //Need to add a null entry, because Ludii starts player numbering on 1
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

        for (int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++) {
            ludiiGame.start(context);

            //Initialize the AIs
            ais.get(1).initAI(ludiiGame,1);
            ((GBGAsLudiiAI)ais.get(2)).initAI(ludiiGame,2,pa);

            final Model model = context.model();

            while (!context.trial().over()){
                model.startNewStep(context,ais,MAX_SECONDS);
            }
            if(context.trial().status().winner() == 0){
                System.out.println("Tie.");
                ties++;

            }else {
                System.out.println("Winner Game " + ": " + ais.get(context.trial().status().winner()).friendlyName());
                if(context.trial().status().winner() == 1){
                    winsPlayer1++;
                }else winsPlayer2++;
            }

            if(LOGS){
                String logFullFilePath = getLogPath(arenaTrain);
                logGame(context,logFullFilePath, ais);
            }
            System.out.println("Wins Player 1 ("+ ais.get(1).friendlyName() + "): " + winsPlayer1);
            System.out.println("Wins Player 2 ("+ ais.get(2).friendlyName() + "): " + winsPlayer2);
            System.out.println("Ties: " + ties);
        }
    }

    @Test
    public void customHexGame() {
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 =0, ties = 0;

        selectedGame = "Hex";
        scaPar=new String[]{"4", "", ""};
        agtFile = "td3new_10-6.agt.zip";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        List<String> options;
        HexConfig.BOARD_SIZE = 4;
        options = Arrays.asList("Board Size/"+HexConfig.BOARD_SIZE+"x"+HexConfig.BOARD_SIZE);
        PLAYER_2 = new GBGAsLudiiAI(2,HexConfig.BOARD_SIZE,2);
        ludiiGame = GameLoader.loadGameFromName("Hex.lud",options);

        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null); //Need to add a null entry, because Ludii starts player numbering on 1
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

        for (int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++) {
            ludiiGame.start(context);

            //Initialize the AIs
            ais.get(1).initAI(ludiiGame,1);
            ((GBGAsLudiiAI)ais.get(2)).initAI(ludiiGame,2,pa);

            final Model model = context.model();

            while (!context.trial().over()){
                model.startNewStep(context,ais,1.0);
            }
            if(context.trial().status().winner() == 0){
                System.out.println("Tie.");
                ties++;

            }else {
                System.out.println("Winner Game " + ": " + ais.get(context.trial().status().winner()).friendlyName());
                if(context.trial().status().winner() == 1){
                    winsPlayer1++;
                }else winsPlayer2++;
            }

            if(LOGS){
                String logFullFilePath = getLogPath(arenaTrain);
                logGame(context,logFullFilePath, ais);
            }
            System.out.println("Wins Player 1 ("+ ais.get(1).friendlyName() + "): " + winsPlayer1);
            System.out.println("Wins Player 2 ("+ ais.get(2).friendlyName() + "): " + winsPlayer2);
            System.out.println("Ties: " + ties);
        }
    }

    private String getLogPath(Arena arena) {
        String logFullFilePath;
        String logMainDir="logs\\";
        String logFile="logs.ludiilog";
        String logDirectory = logMainDir + "\\" + selectedGame;
        String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + selectedGame + "\\" + subDir;
        }
        logFullFilePath = logDirectory + "\\" + logFile;
        return logFullFilePath;
    }

    private static void logGame(Context context, String logfilePath, List<AI> ais){
        StringBuilder logMessage = new StringBuilder();

        if(context.trial().status().winner() == 0){
            logMessage.append("Tie: ");
        }else {
            logMessage.append("Winner: ").append(ais.get(context.trial().status().winner()).friendlyName()).append(": ");
        }
        for (int i = 1; i < ais.size(); i++) {
            logMessage.append(context.score(i)).append(":");
        }
        logMessage.append(" Moves: ");
        for(Move move : context.trial().generateCompleteMovesList()) {
            if(move.to() != -1) {
                logMessage.append(move.to()).append(", ");
            }
        }
        logMessage.append("\n");
        System.out.println(logMessage);

        try {
            final var fos = new FileOutputStream(logfilePath, true);
            fos.write(logMessage.toString().getBytes());
            fos.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
