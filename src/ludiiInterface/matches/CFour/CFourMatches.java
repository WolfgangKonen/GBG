package ludiiInterface.matches.CFour;

import controllers.PlayAgent;
import game.Game;
import games.CFour.StateObserverC4;
import games.LogManager;
import ludiiInterface.games.CFour.SystemConversionC4;
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
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class runs Connect Four matches between GBG Agents and Ludii Agents.
 * Swapping playing order of the agents and getting random openings is possible.
 * The match can be replayed via GBGLaunch.
 *
 * @author Meltem Seven, 2022
 * */
public class CFourMatches extends GBGBatch {

    static int GAMESNUMBER = 20; // needs to be an even amount
    static double MAX_SECONDS = 1.0;

    // some available GBG agents
    static ArrayList<String> agents = new ArrayList<>(Arrays.asList(
            "0-MWrap1000-TCL-EXP-NT3.agt.zip", "1-AB.agt.zip", "9-AB-DL.agt.zip", "1-AB-DL2.agt.zip",
            "2-TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip", "3-MCTS10000-40.agt.zip"));
    static String agtFile = agents.get(2); // chosen GBG agent
    static String[] scaPar;

    static final AI PLAYER_1 = new LudiiAI(); // chosen Ludii agent
    static AI PLAYER_2;
    static Game ludiiGame;

    static boolean opening = false;
    static List<Integer> openingMoves = null;

    static double avgEpLengthLudii;

    static double avgEpLengthGBG;


    public static void main(String[] args) {
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 = 0, ties = 0;

        StartPositionsC4 startPositionsC4 = new StartPositionsC4();

        // loading GBG agent
        scaPar = new String[]{"","",""};
        arenaTrain = SetupGBG.setupSelectedGame("ConnectFour", scaPar, "", false, true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI();
        String friendlyNameGBG = agtFile.substring(0, agtFile.indexOf(".agt"));
        PLAYER_2.setFriendlyName(friendlyNameGBG);

        ludiiGame = GameLoader.loadGameFromName("Connect Four.lud");
        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null); //
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

        //System.out.println(ludiiGame.metadata());// print information about the game in Ludeme format

        for(int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++){
            System.out.println("\nGAME " + (gameCounter+1));
            ludiiGame.start(context);

            // get an opening of random but even amount of moves between 2 and 6 moves
            if(opening) {
                if (gameCounter == 0 || gameCounter % 2 == 0) {
                    openingMoves = startPositionsC4.getOpening((new Random().nextInt(3)+1) * 2);
                    System.out.println("New Opening Sequence with length: " + openingMoves.size());

                }

                // apply opening moves to game
                for (Integer openingMove : openingMoves) {
                    for (Move move : ludiiGame.moves(context).moves()) {
                        if (move.to() == openingMove) {
                            ludiiGame.apply(context, move, false);
                            break;
                        }
                    }
                }
                System.out.println("\nOpening Moves:");
                for(Move move : context.trial().generateCompleteMovesList()) {
                    System.out.println(move.to() + " made by Player " + move.mover());
                }
            }

            // initialize AIs
            ais.get(1).initAI(ludiiGame, 1);
            ((GBGAsLudiiAI)ais.get(2)).initAI(ludiiGame,2,pa);

            // print Player to AI assignments
            System.out.println("\nPlayer 1 (Yellow) is being played by Agent "+context.state().playerToAgent(1)); // By which agent is Player 1 (Yellow) being played? (Which agent is playing PLayer No 1?)
            System.out.println("Agent 1 is playing as Player): "+context.state().currentPlayerOrder(1)); // In the current player order, is PLayerID 1 first?

            // change Player Order every other game so that Agent 2 makes the first move
            if(gameCounter != 0 && gameCounter % 2 == 1){
                context.state().swapPlayerOrder(1,2); // swap the player order of Agent 1 & 2
                System.out.println("\nPLAYER ORDER HAS BEEN SWAPPED! Player 1 (Yellow) is now being played by Agent: "+context.state().playerToAgent(1));
                System.out.println("PLAYER ORDER HAS BEEN SWAPPED! Agent 1 is now playing as Player: "+context.state().currentPlayerOrder(1) + "\n");
            }

            final Model model = context.model();

            boolean breakHappened = false;

            String subDir = friendlyNameGBG + "_" + PLAYER_1.friendlyName();

            while(!context.trial().over()){
                try{
                    model.startNewStep(context, ais, MAX_SECONDS);
                    //System.out.println(PLAYER_1.estimateValue());
                    //System.out.println(PLAYER_1.generateAnalysisReport());
                } catch(Exception exception){
                    System.out.println(exception.toString());
                    crashLog(exception,context,subDir,gameCounter+1);
                    breakHappened = true;
                    break;
                }
            }
            if(breakHappened) continue;

            useLogManager("ConnectFour",subDir,context.trial().generateCompleteMovesList());

            if(context.trial().status().winner() == 0){
                System.out.println("Tie.");
                ties++;
            } else {
                System.out.println("\nWinner Game" + ": " + ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName());
                if(context.state().playerToAgent(context.trial().status().winner()) == 1){
                    winsPlayer1++; // i.e. PLAYER_1 / Agent 1
                    avgEpLengthLudii += context.trial().generateCompleteMovesList().size();
                } else {
                    winsPlayer2++; // i.e. PLAYER_2 / Agent 2
                    avgEpLengthGBG += context.trial().generateCompleteMovesList().size();
                }
            }

            if (gameCounter+ 1 == GAMESNUMBER) {
                avgEpLengthLudii = (winsPlayer1 != 0) ? (avgEpLengthLudii/winsPlayer1) : 0; //typecasting necessary
                avgEpLengthGBG = (winsPlayer2 != 0) ? (avgEpLengthGBG/winsPlayer2) : 0;
            }



            String logFullFilePath = getLogPath(subDir);
            logGame(context, logFullFilePath, ais, gameCounter+1, winsPlayer1, winsPlayer2, ties);
            logToCSV(context,ais,subDir,gameCounter);

            System.out.println("Wins Agent "+ ais.get(1).friendlyName() + " : " + winsPlayer1); // LudiiAI()
            System.out.println("Wins Agent "+ ais.get(2).friendlyName() + " : " + winsPlayer2); // GBGAsLudiiAI()
            System.out.println("Ties: " + ties);
            System.out.println("\n-----------");

        }

    }

    private static void crashLog(Exception exception, Context context, String subDir, int gameCounter){
        StringBuilder logMessage = new StringBuilder();
        String logFullFilePath;

        String logFile="logs.ludiilog.crash";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + "ConnectFour";
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "ConnectFour" + "\\" + subDir;
        }
        Utils.checkAndCreateFolder(logDirectory);       // needed to create subDir if it does not exist yet
        logFullFilePath = logDirectory + "\\" + logFile;


        logMessage.append("\nGame " +gameCounter+ " of " +GAMESNUMBER+ " crashed at " +getCurrentTimeStamp()+ "\n");

        logMessage.append("Moves: ");
        for(Move move : context.trial().generateCompleteMovesList()) {
            logMessage.append(move.to()).append(", ");
        }
        logMessage.append("\n");

        if(openingMoves != null) {
            logMessage.append("Opening Sequence of length " + openingMoves.size());
            logMessage.append("\n");
        }
        logMessage.append("Agent " +context.state().playerToAgent(1) + " was Player 1.\n").append("Agent " +context.state().playerToAgent(2) + " was Player 2.\n");
        if(context.trial().lastMove() != null)
            logMessage.append("The last valid move was made by Player " + context.trial().lastMove().mover() + "\n");

        exception.printStackTrace();

        //if(gameCounter == GAMESNUMBER) logMessage.append("-------------");

        try {
            final var fos = new FileOutputStream(logFullFilePath, true);
            final var ps = new PrintStream(fos);
            fos.write(logMessage.toString().getBytes());
            exception.printStackTrace(ps);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * sets up the LogManager and applies the moves from the Ludii move list one by one to a StateObserverC4.
     * The LogManager can then create a LogEntry for each one.
     *
     * @see LogManager
     * */
    private static void useLogManager(String gameName, String subDir, List<Move> moveList) {
        LogManager logManager = new LogManager();
        logManager.loggingEnabled = true; // set to true so that game is logged
        logManager.verbose = true; // set to true for it to print status updates to the console
        logManager.setSubDir(subDir);

        StateObserverC4 stateObserverC4 = new StateObserverC4();
        SystemConversionC4 systemConversionC4 = new SystemConversionC4(); // needed to translate moveList to GBG actions

        int sessionID = logManager.newLoggingSession(stateObserverC4); // creates session key for the match

        // applies move-turned-to-action to StateObserverC4 and then adds LogEntry
        for(Move ludiiMove : moveList){
            if(moveList.get(moveList.size()-1).to() == -1) continue;
            Types.ACTIONS action = Types.ACTIONS.fromInt(systemConversionC4.getGBGIndexFromLudii(ludiiMove.to()));
            stateObserverC4.advance(action);
            logManager.addLogEntry(action,stateObserverC4,sessionID);
        }

        logManager.endLoggingSession(sessionID,gameName);
    }

    private static String getLogPath(String subDir) {
        String logFullFilePath;
        String logFile="logs.ludiilog";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + "ConnectFour";
        //String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "ConnectFour" + "\\" + subDir;
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

    private static void logGame(Context context, String logfilePath, List<AI> ais, int gameCounter, int winsPlayer1, int winsPlayer2, int ties){
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\nGame " +gameCounter+ " of " +GAMESNUMBER+ "\n").append(getCurrentTimeStamp()+" | ");

        if(context.trial().status().winner() == 0){
            logMessage.append("Tie: \n");
            logMessage.append(ais.get(context.state().playerToAgent(1)).friendlyName() + " was Player 1\n");
            logMessage.append(ais.get(context.state().playerToAgent(2)).friendlyName() + " was Player 2\n");
        }else {
            logMessage.append("Winner: ").append(ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName()).append(" as Player ").append(context.trial().status().winner()).append(": ");
        }
        logMessage.append("Moves: ");
        for(Move move : context.trial().generateCompleteMovesList()) {
            logMessage.append(move.to()).append(", ");
        }
        logMessage.append("\n");
        logMessage.append("Game ended after total of moves: " +context.trial().generateCompleteMovesList().size()+ "\n");

        if(openingMoves != null) {
            logMessage.append("Opening Sequence of length " + openingMoves.size());
            logMessage.append("\n");
        }

        if(gameCounter == GAMESNUMBER){
            logMessage.append("\nWins " +PLAYER_1.friendlyName()+ " : " +winsPlayer1+ "\n");
            logMessage.append("Wins " +PLAYER_2.friendlyName()+ " : " +winsPlayer2+ "\n");
            logMessage.append("Ties: " +ties+ "\n\n");
            logMessage.append("Episodes in which " +ais.get(1).friendlyName()+ " won (" +winsPlayer1+ " Episode/s) took " + df.format(avgEpLengthLudii)+ " moves on average\n");
            logMessage.append("Episodes in which " +ais.get(2).friendlyName()+ " won (" +winsPlayer2+ " Episode/s) took " + df.format(avgEpLengthGBG)+ " moves on average");
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
        String logDirectory = logMainDir + "\\" + "ConnectFour";
        //String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "ConnectFour" + "\\" + subDir;
        }
        Utils.checkAndCreateFolder(logDirectory); // needed to create subDir if it does not exist yet
        logPath = logDirectory + "\\" + logFile;

        message.append(getCurrentTimeStamp()+";");
        if(openingMoves != null) {
            message.append(openingMoves.size() + ";");

            for (int m : openingMoves)
                message.append(m + " ");

            message.append(";");
        } else {
            message.append(" ; ;");
        }

        message.append(context.trial().generateCompleteMovesList().size()+";");

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
