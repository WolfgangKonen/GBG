package ludiiInterface;

import controllers.PlayAgent;
import game.Game;
import games.Arena;
import games.Hex.HexConfig;
import games.Yavalath.ConfigYavalath;
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
import tools.Utils;
import utils.LudiiAI;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    /**
     * Play {@link #GAMESNUMBER} Othello episodes {@link #PLAYER_1}( {@code = }{@link LudiiAI}) vs.
     * {@link #PLAYER_2}( {@code = }{@link GBGAsLudiiAI}), where the GBG agent is loaded from {@code agtFile}
     * as specified in source code.
     * <p>
     * If {@link #LOGS}{@code = true}, then append
     * <pre>
     *    timestamp | Winner : Moves: ...  </pre>
     * as one-liner to log file {@code logs/<gameName>[/<subdir]/logs.ludiilog}.
     */
    @Test
    public void customOthelloGame() {
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 =0, ties = 0;

        selectedGame = "Othello";
        scaPar=new String[]{"", "", ""};
        agtFile = "TCL4-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI();
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

    /**
     * Play {@link #GAMESNUMBER} Hex episodes {@link #PLAYER_1}( {@code = }{@link LudiiAI}) vs.
     * {@link #PLAYER_2}( {@code = }{@link GBGAsLudiiAI}), where the GBG agent is loaded from {@code agtFile}
     * as specified in source code.
     * <p>
     * If {@link #LOGS}{@code = true}, then append
     * <pre>
     *    timestamp | Winner : Moves: ...  </pre>
     * as one-liner to log file {@code logs/<gameName>[/<subdir]/logs.ludiilog}.
     */
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
        PLAYER_2 = new GBGAsLudiiAI(HexConfig.BOARD_SIZE,2);
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

    /**
     * Play {@link #GAMESNUMBER} Yavalath episodes {@link #PLAYER_1}( {@code = }{@link LudiiAI}) vs.
     * {@link #PLAYER_2}( {@code = }{@link GBGAsLudiiAI}), where the GBG agent is loaded from {@code agtFile}
     * as specified in source code.
     * <p>
     * If {@link #LOGS}{@code = true}, then append
     * <pre>
     *    timestamp | Winner : Moves: ...  </pre>
     * as one-liner to log file {@code logs/<gameName>[/<subdir]/logs.ludiilog}.
     */
    @Test
    public void customYavalathGame() {
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 =0, ties = 0;

        selectedGame = "Yavalath";
        scaPar=new String[]{"2", "5", "False"};
        agtFile = "MCTStest.agt.zip";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        List<String> options;
        //options = Arrays.asList("Board Size/"+ConfigYavalath.getBoardSize()+"x"+ConfigYavalath.getBoardSize());
        PLAYER_2 = new GBGAsLudiiAI(); //(ConfigYavalath.getBoardSize(),2);
        ludiiGame = GameLoader.loadGameFromName("Yavalath.lud");

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

    /**
     * Play {@link #GAMESNUMBER} Connect Four episodes {@link #PLAYER_1}( {@code = }{@link LudiiAI}) vs.
     * {@link #PLAYER_2}( {@code = }{@link GBGAsLudiiAI}), where the GBG agent is loaded from {@code agtFile}
     * as specified in source code.
     * <p>
     * If {@link #LOGS}{@code = true}, then append
     * <pre>
     *    timestamp | Winner : Moves: ...  </pre>
     * as one-liner to log file {@code logs/<gameName>[/<subdir]/logs.ludiilog}.
     */
    @Test
    public void customC4Game(){
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 = 0, ties = 0;

        selectedGame ="ConnectFour";
        scaPar = new String[]{"","",""};
        agtFile= "1-AB-DL.agt.zip";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI();
        ludiiGame = GameLoader.loadGameFromName("Connect Four.lud");

        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null); //Need to add a null entry, because Ludii starts player numbering on 1
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

        for(int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++){
            ludiiGame.start(context);

            // Initialize the AIs
            ais.get(1).initAI(ludiiGame,1);
            ((GBGAsLudiiAI)ais.get(2)).initAI(ludiiGame,2,pa);

            final Model model = context.model();

            while(!context.trial().over()){
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

    /**
     * Play {@link #GAMESNUMBER} Nim episodes {@link #PLAYER_1}( {@code = }{@link LudiiAI}) vs.
     * {@link #PLAYER_2}( {@code = }{@link GBGAsLudiiAI}), where the GBG agent is loaded from {@code agtFile}
     * as specified in source code.
     * <p>
     * If {@link #LOGS}{@code = true}, then append
     * <pre>
     *    timestamp | Winner : Moves: ...  </pre>
     * as one-liner to log file {@code logs/<gameName>[/<subdir]/logs.ludiilog}.
     */
    @Test
    public void customNimGame(){
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 = 0, ties = 0;
        int numberHeaps = 5 ;

        boolean firstMoveLudii = true; // to decide whether Ludii or the GBG AI makes the first move

        selectedGame ="Nim";
        scaPar = new String[]{""+numberHeaps,"-1",""+numberHeaps};
        agtFile= "Bouton.agt.zip";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI(numberHeaps,2);

        List<String> options = Arrays.asList("Number Piles/"+numberHeaps, "End Rules/Last Mover Wins");
        ludiiGame = GameLoader.loadGameFromName("Nim.lud", options);

        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null); //Need to add a null entry, because Ludii starts player numbering on 1

        if(firstMoveLudii){ // if Ludii AI makes the first move, it needs to be first in ais list
            ais.add(PLAYER_1);
            ais.add(PLAYER_2);
        } else { // i.e. GBG AI makes first move and is first in the list
            ais.add(PLAYER_2);
            ais.add(PLAYER_1);
        }

        // whoever makes the first move is Player 1 in AI List, so index needs to be passed accordingly to initAI()
        int ludiiIndex = firstMoveLudii? 1 : 2;
        int gbgIndex = firstMoveLudii? 2 : 1;

        for(int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++){
            ludiiGame.start(context);
            context.trial().reset(ludiiGame); // needed because context makes moves on its own after being initialized

            //MovesList should be empty before the game start
            assert context.trial().generateCompleteMovesList().isEmpty() : "Invalid context!!";

            // Initialize the AIs
            ais.get(ludiiIndex).initAI(ludiiGame,ludiiIndex);
            ((GBGAsLudiiAI)ais.get(gbgIndex)).initAI(ludiiGame,gbgIndex,pa);

            final Model model = context.model();

            while(!context.trial().over()){
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
                logGameWithPass(context,logFullFilePath, ais);
            }
            System.out.println("Wins Player 1 ("+ ais.get(1).friendlyName() + "): " + winsPlayer1);
            System.out.println("Wins Player 2 ("+ ais.get(2).friendlyName() + "): " + winsPlayer2);
            System.out.println("Ties: " + ties);
        }

    }

    private String getLogPath(Arena arena) {
        String logFullFilePath;
        String logFile="logs.ludiilog";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + selectedGame;
        String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + selectedGame + "\\" + subDir;
        }
        Utils.checkAndCreateFolder(logDirectory);       // needed to create subDir if it does not exist yet
        logFullFilePath = logDirectory + "\\" + logFile;
        return logFullFilePath;
    }

    /**
     * generates String containing the current timestamp
     *
     * @return the timestamp
     */
    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//-SSS
        Date now = new Date();
        return sdfDate.format(now);
    }

    private static void logGame(Context context, String logfilePath, List<AI> ais){
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(getCurrentTimeStamp()+" | ");

        if(context.trial().status().winner() == 0){
            logMessage.append("Tie: ");
        }else {
            logMessage.append("Winner: ").append(ais.get(context.trial().status().winner()).friendlyName()).append(": ");
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

    private static void logGameWithPass(Context context, String logfilePath, List<AI> ais){
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(getCurrentTimeStamp()+" | ");

        if(context.trial().status().winner() == 0){
            logMessage.append("Tie: ");
        }else {
            logMessage.append("Winner: ").append(ais.get(context.trial().status().winner()).friendlyName()).append(": ");
        }
        logMessage.append(" Moves: ");
        for(Move move : context.trial().generateCompleteMovesList()) {
            logMessage.append(move.to()).append(", ");
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
