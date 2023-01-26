package ludiiInterface.matches.Othello;

import controllers.PlayAgent;
import game.Game;
import games.Arena;
import games.LogManager;
import games.Othello.StateObserverOthello;
import ludiiInterface.games.othello.SystemConversionOthello;
import ludiiInterface.general.GBGAsLudiiAI;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import starters.GBGBatch;
import starters.SetupGBG;
import tools.Types;
import tools.Utils;
import utils.LudiiAI;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class runs Othello matches between GBG Agents and Ludii Agents.
 * Swapping playing order of the agents and getting random 8-ply XOT openings is possible.
 * The match can be replayed via GBGLaunch.
 *
 * @author Meltem Seven, 2022
 * */
public class OthelloMatches extends GBGBatch {

    static int GAMESNUMBER = 2; // needs to be even for every agent to play every opening from every player perspective
    static double MAX_SECONDS = 1.0;

    // some available GBG agents
    static ArrayList<String> agents = new ArrayList<>(Arrays.asList(
            "edax2-d0.agt.zip", "edax2-d1.agt.zip", "edax2-d2.agt.zip", "edax2-d3.agt.zip", "edax2-d4.agt.zip", "edax2-d5.agt.zip", "edax2-d21.agt.zip",
            "TCL4-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip"));
    static String agtFile = agents.get(1); // chosen GBG agent
    static String[] scaPar;

    static final AI PLAYER_1 = new LudiiAI(); // chosen Ludii agent
    static AI PLAYER_2;
    static Game ludiiGame;

    static boolean opening = true;
    static List<Integer> openingMoves;

    public static void main(String[] args) {
        PlayAgent pa;
        int winsLudiiAI = 0, winsGBGAsLudiiAI = 0, ties = 0;

        StartPositionsOthello startPositionsOthello = new StartPositionsOthello();

        scaPar = new String[]{"","",""};
        arenaTrain = SetupGBG.setupSelectedGame("Othello", scaPar, "", false, true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI();
        String friendlyNameGBG = agtFile.substring(0, agtFile.indexOf(".agt"));
        PLAYER_2.setFriendlyName(friendlyNameGBG);

        // used to manipulate player and initialization order of agents
        int ludiiIndex = 0;
        int gbgIndex = 0;

        ludiiGame = GameLoader.loadGameFromName("Reversi.lud");
        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null);

        //System.out.println(ludiiGame.metadata()); // print information about the game in Ludeme format

        for(int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++){
            System.out.println("\nGAME " + (gameCounter+1));
            ludiiGame.start(context);

            if(opening) {
                if (gameCounter == 0 || gameCounter % 2 == 0) {

                    // get 8-ply opening
                    openingMoves = startPositionsOthello.getFromSmallList();
                    System.out.println("New opening sequence!");
                }

                // apply opening moves to the game
                for (Integer openingMove : openingMoves) {
                    for (Move move : ludiiGame.moves(context).moves()) {
                        if(move.to() == openingMove) {
                            ludiiGame.apply(context,move,false);
                            break;
                        }
                    }
                }
            }

            if(gameCounter == 0 || gameCounter % 2 == 0)
            {
                ais.add(PLAYER_1);
                ais.add(PLAYER_2);
                ludiiIndex = 1;
                gbgIndex = 2;
            } else {
                ais.add(PLAYER_2);
                ais.add(PLAYER_1);
                ludiiIndex = 2;
                gbgIndex = 1;
            }

            // initialize AIs
            ais.get(ludiiIndex).initAI(ludiiGame, ludiiIndex);
            ((GBGAsLudiiAI)ais.get(gbgIndex)).initAI(ludiiGame,gbgIndex,pa);

            // print Player to AI assignments
            System.out.println("\nPlayer 1 (Black) is being played by Agent " + ais.get(context.state().playerToAgent(1)).friendlyName()); // By which agent is Player 1 being played? (Which agent is playing PLayer No 1?)
            //System.out.println("Agent 1 is playing as Player: "+context.state().currentPlayerOrder(1)); // In the current player order, is PLayerID 1 first?

            final Model model = context.model();

            while(!context.trial().over()){
                model.startNewStep(context, ais, MAX_SECONDS);
            }

            String subDir = friendlyNameGBG + "_" + PLAYER_1.friendlyName();
            useLogManager("Othello",subDir,context.trial().generateCompleteMovesList());


            if(context.trial().status().winner() == 0){
                System.out.println("Tie.");
                ties++;
            } else {
                System.out.println("\nWinner Game " + ": " + ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName());
                if(Objects.equals(ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName(), PLAYER_1.friendlyName())){
                    winsLudiiAI++; // i.e. PLAYER_1
                } else
                    winsGBGAsLudiiAI++; // i.e. PLAYER_2
            }

            String logFullFilePath = getLogPath(subDir);
            logGameWithPass(context, logFullFilePath, ais, gameCounter+1, winsLudiiAI, winsGBGAsLudiiAI, ties);
            logToCSV(context,ais,subDir,gameCounter);

            System.out.println("Wins Agent ("+ ais.get(ludiiIndex).friendlyName() + ") : " + winsLudiiAI);
            System.out.println("Wins Agent ("+ ais.get(gbgIndex).friendlyName() + ") : " + winsGBGAsLudiiAI);
            System.out.println("Ties: " + ties);
            System.out.println("\n-----------");

            // remove AIs in preparation of next game
            ais.remove(PLAYER_1);
            ais.remove(PLAYER_2);
        }
    }

    /**
     * sets up the LogManager and applies the moves from the Ludii move list one by one to a StateObserverOthello.
     * The LogManager can then create a LogEntry for each one.
     *
     * @see LogManager
     * */
    private static void useLogManager(String gameName, String subDir, List<Move> moveList) {
        LogManager logManager = new LogManager();
        logManager.loggingEnabled = true; // set to true so that game is logged
        logManager.verbose = true; // set to true for it to print status updates to the console
        logManager.setSubDir(subDir);

        StateObserverOthello stateObserverOthello = new StateObserverOthello();
        SystemConversionOthello systemConversionOthello = new SystemConversionOthello(); // needed to translate moveList to GBG actions

        int sessionID = logManager.newLoggingSession(stateObserverOthello);

        // remove the game set-up moves since the middle pieces are already set by the StateObserver
        moveList.remove(0);
        moveList.remove(0);
        moveList.remove(0);
        moveList.remove(0);

        // applies move-turned-to-action to StateObserverOthello and then adds LogEntry
        for(Move ludiiMove : moveList){
            if(ludiiMove.isPass()) continue;

            Types.ACTIONS action = Types.ACTIONS.fromInt(systemConversionOthello.getGBGIndexFromLudii(ludiiMove.to()));
            stateObserverOthello.advance(action);
            logManager.addLogEntry(action,stateObserverOthello,sessionID);
        }

        logManager.endLoggingSession(sessionID,gameName);
    }

    private static String getLogPath(String subDir) {
        String logFullFilePath;
        String logFile="logs.ludiilog";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + "Othello";
        //String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "Othello" + "\\" + subDir;
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

    private static void logGameWithPass(Context context, String logfilePath, List<AI> ais, int gameCounter, int winsPlayer1, int winsPlayer2, int ties){
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\nGame " +gameCounter+ " of " +GAMESNUMBER+ "\n");

        if(openingMoves != null) {
            if ((gameCounter-1) == 0 || (gameCounter-1) % 2 == 0) logMessage.append("New opening sequence!\n");
        }
        logMessage.append(getCurrentTimeStamp()+" | ");

        if(context.trial().status().winner() == 0){
            logMessage.append("Tie: ");
            logMessage.append(ais.get(context.state().playerToAgent(1)).friendlyName() + " was PLayer 1\n");
            logMessage.append(ais.get(context.state().playerToAgent(2)).friendlyName() + " was Player 2\n");
        }else {
            logMessage.append("Winner: ").append(ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName()).append(" as Player ").append(context.trial().status().winner()).append(": ");
        }
        logMessage.append(" Moves: ");
        for(Move move : context.trial().generateCompleteMovesList()) {
            logMessage.append(move.to()).append(", ");
        }
        logMessage.append("\n");
        logMessage.append("Game ended after total of moves: " +context.trial().generateCompleteMovesList().size()+ "\n");

        if(gameCounter == GAMESNUMBER){
            logMessage.append("\nWins " +PLAYER_1.friendlyName()+ " : " +winsPlayer1+ "\n");
            logMessage.append("Wins " +PLAYER_2.friendlyName()+ " : " +winsPlayer2+ "\n");
            logMessage.append("Ties: " +ties+ "\n");
           //logMessage.append("Episodes in which " +PLAYER_1.friendlyName()+ " won took " + df.format(avgEpLengthLudii)+ " moves on average\n");
            //logMessage.append("Episodes in which " +PLAYER_2.friendlyName()+ " won took " + df.format(avgEpLengthGBG)+ " moves on average");
            logMessage.append("\n-------------\n\n-------------\n");
        }

        System.out.println(logMessage);

        try {
            final var fos = new FileOutputStream(logfilePath, true);
            fos.write(logMessage.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logToCSV(Context context,List<AI> ais, String subDir, int gameCounter){
        String logPath;
        StringBuilder message = new StringBuilder();

        String logFile="logs.csv";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + "Othello";
        //String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "Othello" + "\\" + subDir;
        }
        Utils.checkAndCreateFolder(logDirectory); // needed to create subDir if it does not exist yet
        logPath = logDirectory + "\\" + logFile;

        message.append(getCurrentTimeStamp()+";");
        if(openingMoves != null) {
            for (int m : openingMoves)
                message.append(m + " ");

            message.append(";");
        } else {
            message.append(" ;");
        }

        message.append(context.trial().generateCompleteMovesList().size()-4+";"+context.trial().numTurns()+";");

        if(PLAYER_2.friendlyName().contains("edax2-d")) {
            message.append(PLAYER_2.friendlyName() + ";");
        }

        // insert append to which counts amount of passes in the episode

        if(context.trial().status().winner() != 0){
            message.append(ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName()+ ";");
            message.append(context.trial().status().winner()+";");
        }else
            message.append("Tie! ; ;");


        if(ais.get(context.state().playerToAgent(context.trial().status().winner())) == PLAYER_1) {
            message.append("1;0"); //Rating Ludii; Rating GBG
        } else if (ais.get(context.state().playerToAgent(context.trial().status().winner())) == PLAYER_2){
            message.append("0;1"); //Rating Ludii; Rating GBG
        } else {
            message.append("0,5;0,5"); //Rating Ludii; Rating GBG
        }
        message.append("\n");

        try {
            final var fos = new FileOutputStream(logPath, true);
            fos.write(message.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
