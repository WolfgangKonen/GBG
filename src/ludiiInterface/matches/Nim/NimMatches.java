package ludiiInterface.matches.Nim;

import controllers.PlayAgent;
import game.Game;
import games.Arena;
import games.LogManager;
import games.Nim.StateObserverNim;
import ludiiInterface.games.Nim.SystemConversionNim;
import ludiiInterface.general.GBGAsLudiiAI;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import search.mcts.MCTS;
import search.mcts.backpropagation.AlphaGoBackprop;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.UCB1;
import search.mcts.selection.UCB1Tuned;
import starters.GBGBatch;
import starters.SetupGBG;
import tools.Types;
import tools.Utils;
import utils.AIFactory;
import utils.LudiiAI;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class runs Nim matches between GBG Agents and Ludii Agents.
 * Swapping playing order of the agents is possible.
 * The match can be replayed via GBGLaunch.
 *
 * @author Meltem Seven, 2022
 * */
public class NimMatches extends GBGBatch {
    static int GAMESNUMBER = 2;
    static double MAX_SECONDS = 1.0;

    // list of some available GBG agents; different agents are trained for different numberHeap configurations!
    static ArrayList<String> agents = new ArrayList<>(Arrays.asList(
            "Bouton.agt.zip", "TDNT4-20k.agt.zip", "TDNT4-20k-project.agt.zip",
            "TDNT4-60k-project.agt.zip", "TDNT4-10-4-60k-project.agt.zip", "TDNT4-20-5-60k-project.agt.zip" ));
    static String agtFile = agents.get(4); // chosen GBG agent
    static String[] scaPar;
    static int numberHeaps = 9;

    static final AI PLAYER_1 = new LudiiAI();
    static AI PLAYER_2;
    static Game ludiiGame;

    public static void main(String[] args) {
        PlayAgent pa;
        int winsPlayer1 = 0, winsPlayer2 = 0, ties = 0;

       // loading GBG agent
        scaPar = new String[]{"" + numberHeaps, "-1", "" + numberHeaps, "" + numberHeaps};
        arenaTrain = SetupGBG.setupSelectedGame("Nim", scaPar, "", false, true);
        pa = arenaTrain.loadAgent(agtFile);
        System.out.println(pa.getName()+"\n"+pa.stringDescr());

        PLAYER_2 = new GBGAsLudiiAI(numberHeaps,2);
        String friendlyNameGBG = agtFile.substring(0,agtFile.indexOf(".agt"));
        PLAYER_2.setFriendlyName(friendlyNameGBG);

        // loading the game
        List<String> options = Arrays.asList("Number Piles/"+numberHeaps, "End Rules/Last Mover Wins");
        ludiiGame = GameLoader.loadGameFromName("Nim.lud", options);

        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<>();
        ais.add(null); //
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

       // System.out.println(ludiiGame.metadata()); // print information about the game in Ludeme format

        for(int gameCounter = 0; gameCounter < GAMESNUMBER; gameCounter++){
            System.out.println("\nGAME " + (gameCounter+1));
            ludiiGame.start(context);

            // first reset game since context seems to make its own moves without prompt before game start
            context.trial().reset(ludiiGame);
            assert context.trial().generateCompleteMovesList().isEmpty() : "Invalid Ludii Context!";

            // initialize AIs
            ais.get(1).initAI(ludiiGame, 1);
            ((GBGAsLudiiAI)ais.get(2)).initAI(ludiiGame,2,pa);

            // print Player to AI assignments
            System.out.println("\nPlayer 1 is being played by Agent: "+context.state().playerToAgent(1)); // Which agent is playing as Player No 1?
            System.out.println("Agent 1 is playing as Player: "+context.state().currentPlayerOrder(1)); // As which Player is Agent 1 playing?


           // change Player Order every other game so that Agent 2 makes the first move
            if(gameCounter != 0 && gameCounter % 2 == 1){
                context.state().swapPlayerOrder(1,2); // swap the player order of Agent 1 & 2
                System.out.println("\nPLAYER ORDER HAS BEEN SWAPPED! Player 1 (Yellow) is now being played by Agent: "+context.state().playerToAgent(1));
                System.out.println("PLAYER ORDER HAS BEEN SWAPPED! Agent 1 is now playing as Player: "+context.state().currentPlayerOrder(1) + "\n");
            }

            final Model model = context.model();

            while(!context.trial().over()){
                model.startNewStep(context, ais, MAX_SECONDS);
            }

            String subDir = null;
            if(numberHeaps < 10) {
                subDir = "N0" + numberHeaps + "-S-01-M0" + numberHeaps + "\\" + PLAYER_2.friendlyName() + "_" + PLAYER_1.friendlyName() + "_" + numberHeaps;
            } else{
                subDir = "N" + numberHeaps + "-S-01-M" + numberHeaps + "\\" + PLAYER_2.friendlyName() + "_" + PLAYER_1.friendlyName() + "_" + numberHeaps;
            }
           useLogManager("Nim",subDir,context.trial().generateCompleteMovesList());


            if(context.trial().status().winner() == 0){
                System.out.println("Tie.");
                ties++;
            } else {
                System.out.println("\nWinner Game " + ": " + ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName());
                if(context.state().playerToAgent(context.trial().status().winner()) == 1){
                    winsPlayer1++; // i.e. PLAYER_1 / Agent 1
                } else
                    winsPlayer2++; // i.e. PLAYER_2 / Agent 2
            }

            String logFullFilePath = getLogPath(arenaTrain);
            logGameWithPass(context, logFullFilePath, ais, gameCounter+1, winsPlayer1, winsPlayer2, ties);
            logToCSV(context,ais,gameCounter,arenaTrain);

            System.out.println("Wins Agent 1 ("+ ais.get(1).friendlyName() + "): " + winsPlayer1);
            System.out.println("Wins Agent 2 ("+ ais.get(2).friendlyName() + "): " + winsPlayer2);
            System.out.println("Ties: " + ties);
            System.out.println("\n-----------");
        }

    }

    /**
     * sets up the LogManager and applies the moves from the Ludii move list one by one to a StateObserverNim.
     * The LogManager can then create a LogEntry for each one.
     *
     * @see LogManager
     * */
    private static void useLogManager(String gameName, String subDir, List<Move> moveList) {
        LogManager logManager = new LogManager();
        logManager.loggingEnabled = true; // set to true so that game is logged
        logManager.verbose = true; // set to true for it to print status updates to the console
        logManager.setSubDir(subDir);

        StateObserverNim stateObserverNim = new StateObserverNim();

        int sessionID = logManager.newLoggingSession(stateObserverNim); // creates session ey for the match

        int temp = moveList.get(0).to();
        int counter = 0;

        for(int i = 0; i < moveList.size(); i++){

            // counts number of times that one item is taken off the heap in one player turn (= how many items are taken off in total)
            if(moveList.get(i).to() == temp){
                counter++;
                continue;
            }

            // calculates GBG action equivalent to the Ludii version of moves
            int gbgActInt = temp * numberHeaps + (counter-1);

            // applies move-turned-to-action to StateObserverNim and then adds LogEntry
            Types.ACTIONS action = Types.ACTIONS.fromInt(gbgActInt);
            stateObserverNim.advance(action);
            logManager.addLogEntry(action,stateObserverNim,sessionID);

            // if move is a pass move, set next move as temp and reset the counter
            if(moveList.get(i).isPass()) {
                //int mover = moveList.get(i).who() % 2 == 1 ? 1 : 2;  // using number of turns so far to determine who made the move
                //stateObserverNim.setPlayer(systemConversionNim.getGBGPlayerFromLudii(mover) == 0 ? 1 : 0);

                temp = moveList.get(i+1).to();
                counter = 0;

                continue;
            }

            temp = moveList.get(i).to();
            counter = 1;

        }

        logManager.endLoggingSession(sessionID,gameName);
    }

    private static String getLogPath(Arena arena) {
        String logFullFilePath;
        String logFile="logs.ludiilog";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + "Nim";
        String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "Nim" + "\\" + subDir + "\\" + PLAYER_2.friendlyName() + "_" +PLAYER_1.friendlyName() + "_" + numberHeaps ;
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
        logMessage.append("\nGame " +gameCounter+ " of " +GAMESNUMBER+ "\n").append(getCurrentTimeStamp()+" | ");

        if(context.trial().status().winner() == 0){
            logMessage.append("Tie: ");
        }else {
            logMessage.append("Winner: ").append(ais.get(context.state().playerToAgent(context.trial().status().winner())).friendlyName()).append(" as Player ").append(context.trial().status().winner()).append(": ");
        }
        logMessage.append(" Moves: ");
        for(Move move : context.trial().generateCompleteMovesList()) {
            logMessage.append(move.to()).append(", ");
        }
        logMessage.append("\n");
        logMessage.append("Spiel beendet in " +context.trial().generateCompleteMovesList().size()+ " Zügen\n");
        System.out.println(logMessage);

        if(gameCounter == GAMESNUMBER){
            logMessage.append("\nWins " +PLAYER_1.friendlyName()+ " : " +winsPlayer1+ "\n");
            logMessage.append("Wins " +PLAYER_2.friendlyName()+ " : " +winsPlayer2+ "\n");
            logMessage.append("Ties: " +ties+ "\n");
            logMessage.append("\n-------------\n\n-------------\n");
        }

        try {
            final var fos = new FileOutputStream(logfilePath, true);
            fos.write(logMessage.toString().getBytes());
            fos.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void logToCSV(Context context,List<AI> ais, int gameCounter, Arena arena){
        String logPath;
        StringBuilder message = new StringBuilder();

        String logFile="logs.csv";
        String logMainDir="logs";
        String logDirectory = logMainDir + "\\" + "Nim";
        String subDir = arena.getGameBoard().getSubDir();
        if(subDir != null && !subDir.equals("")) {
            logDirectory = logMainDir + "\\" + "Nim" + "\\" + subDir + "\\" + PLAYER_2.friendlyName() + "_" +PLAYER_1.friendlyName() + "_" + numberHeaps;
        }
        Utils.checkAndCreateFolder(logDirectory); // needed to create subDir if it does not exist yet
        logPath = logDirectory + "\\" + logFile;

        message.append(getCurrentTimeStamp()+";");

        message.append(context.trial().numTurns()+";"); // in how many turns did the game finish

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
