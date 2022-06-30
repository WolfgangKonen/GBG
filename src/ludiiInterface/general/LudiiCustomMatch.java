package ludiiInterface.general;

import game.Game;
import games.Hex.HexConfig;
import games.Nim.NimConfig;
import ludiiInterface.Util;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import utils.LudiiAI;

import javax.swing.*;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LudiiCustomMatch {
    static String[] games = { "Othello", "Yavalath", "Hex", "ConnectFour"};
    static final AI PLAYER_1 = new LudiiAI();
    static AI PLAYER_2;
    static Game ludiiGame;


    public static void main(String[] args){
        int gamesNumber = 10;
        String logfilePath ="";

        JComboBox gameComboBox = new JComboBox(games);
        JOptionPane.showMessageDialog(null, gameComboBox,"Choose a game", JOptionPane.QUESTION_MESSAGE);
        String game = (String) gameComboBox.getSelectedItem();


        switch (game){
            case "Othello" -> {
                PLAYER_2 = new GBGAsLudiiAI();
                ludiiGame = GameLoader.loadGameFromName("Reversi.lud");

            }
            case "Yavalath" -> {
                PLAYER_2 = new GBGAsLudiiAI();
                ludiiGame = GameLoader.loadGameFromName("Yavalath.lud");
            }
            case "Hex" -> {
                String[] sizes = {"3","4","5","6","7","8"};
                JComboBox hexSize = new JComboBox(sizes);
                JOptionPane.showMessageDialog(null,hexSize,"Choose a board size", JOptionPane.QUESTION_MESSAGE);
                int size = Integer.parseInt((String) hexSize.getSelectedItem());

                List<String> options;
                switch (size){
                    case 3 -> {
                        HexConfig.BOARD_SIZE = 3;
                        options = Arrays.asList("Board Size/3x3");
                    }
                    case 4 -> {
                        HexConfig.BOARD_SIZE = 4;
                        options = Arrays.asList("Board Size/4x4");
                    }
                    case 5 -> {
                        HexConfig.BOARD_SIZE = 5;
                        options = Arrays.asList("Board Size/5x5");
                    }
                    case 6 -> {
                        HexConfig.BOARD_SIZE = 6;
                        options = Arrays.asList("Board Size/6x6");
                    }
                    case 7 -> {
                        HexConfig.BOARD_SIZE = 7;
                        options = Arrays.asList("Board Size/7x7");
                    }
                    case 8 -> {
                        HexConfig.BOARD_SIZE = 8;
                        options = Arrays.asList("Board Size/8x8");
                    }
                    default -> {
                        HexConfig.BOARD_SIZE = 6;
                        options = Arrays.asList("Board Size/6x6");
                    }

                }
                PLAYER_2 = new GBGAsLudiiAI(size,2);
                ludiiGame = GameLoader.loadGameFromName("Hex.lud",options);
            }
            case "ConnectFour" -> {
                PLAYER_2 = new GBGAsLudiiAI();
                ludiiGame = GameLoader.loadGameFromName("Connect Four.lud");
            }

        }


        final other.context.Context context = new Context(ludiiGame, new Trial(ludiiGame));

        gamesNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of games to be played.", gamesNumber));

        boolean logs = (JOptionPane.showConfirmDialog(null,"Do you want to log the results?", "Logging", JOptionPane.YES_NO_OPTION) == 0);
        if(logs){
            logfilePath = Util.loadFileFromDialog("Logs schreiben in: ");
        }

        final List<AI> ais = new ArrayList<AI>();
        ais.add(null); //Need to add a null entry, because Ludii starts player numbering on 1
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

        int winsPlayer1 = 0, winsPlayer2 =0, ties = 0;

        for (int gameCounter = 0; gameCounter < gamesNumber; gameCounter++) {
            ludiiGame.start(context);

            //Initialize the AIs
            for (int i = 1; i < ais.size(); i++) {
                ais.get(i).initAI(ludiiGame,i);
            }

            final Model model = context.model();

            while (!context.trial().over()){
                model.startNewStep(context,ais,0.1);
            }
            if(context.trial().status().winner() == 0){
                System.out.println("Tie.");
                ties++;

            }else {
                System.out.println("Winner Game " + gameCounter + ": " + ais.get(context.trial().status().winner()).friendlyName());
                if(context.trial().status().winner() == 1){
                    winsPlayer1++;
                }else winsPlayer2++;
            }


            if(logs){
                logGame(context,logfilePath, ais);
            }
            System.out.println("Wins Player 1 ("+ ais.get(1).friendlyName() + "): " + winsPlayer1);
            System.out.println("Wins Player 2 ("+ ais.get(2).friendlyName() + "): " + winsPlayer2);
            System.out.println("Ties: " + ties);
        }

    }

    private static void logGame(Context context, String logfilePath, List<AI> ais){
        String logMessage = "";

        if(context.trial().status().winner() == 0){
            logMessage += "Tie: ";
        }else {
            logMessage += "Winner: " + ais.get(context.trial().status().winner()).friendlyName() + ": ";
        }
        for (int i = 1; i < ais.size(); i++) {
            logMessage += context.score(i) + ":";
        }
        logMessage += " Moves: ";
        for(Move move : context.trial().generateCompleteMovesList()) {
            if(move.to() != -1) {
                logMessage += move.to() + ", ";
            }
        }
        logMessage += "\n";
        System.out.println(logMessage);

        try {
            final var fos = new FileOutputStream(logfilePath, true);
            fos.write(logMessage.getBytes());
            fos.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
