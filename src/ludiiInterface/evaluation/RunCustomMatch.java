package ludiiInterface.evaluation;

import ludiiInterface.gateway.GbgAsLudiiAgent;
import ludiiInterface.othello.useCases.moves.LudiiMoves;
import ludiiInterface.othello.useCases.state.AsStateObserverOthello;
import ludiiInterface.othello.useCases.state.GbgStateFromLudiiContext;
import util.AI;
import util.Context;
import util.GameLoader;
import util.Trial;
import util.model.Model;
import utils.LudiiAI;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ludiiInterface.Util.loadFileFromDialog;

public final class RunCustomMatch {
    static final String GAME_NAME = "Reversi.lud";
    static final int NUM_GAMES = 50;

    static final AI PLAYER_1 = new LudiiAI();
    static final AI PLAYER_2 = new GbgAsLudiiAgent();

    public static void main(final String[] args) {
        final var logFile = loadFileFromDialog("Logs schreiben in..");

        final List<AI> ais = new ArrayList<AI>();
        ais.add(null); // important
        ais.add(PLAYER_1);
        ais.add(PLAYER_2);

        final var game = GameLoader.loadGameFromName(GAME_NAME);
        final Context context = new Context(game, new Trial(game));

        for (int gameCounter = 0; gameCounter < NUM_GAMES; gameCounter++) {
            game.start(context);

            for (int p = 1; p < ais.size(); p++) {
                ais.get(p).initAI(game, p);
            }

            final Model model = context.model();

            while (!context.trial().over()) {
                model.startNewStep(context, ais, 1.0);
            }

            logFinishedGame(
                logFile,
                context,
                ais.get(context.trial().status().winner() - 1).friendlyName // -1 because Ludii players are not 0-based indexed
            );
        }
    }

    private static void logFinishedGame(final String path, final Context context, final String winner) {
        try {
            final var fos = new FileOutputStream(path, true);
            fos.write(logForGame(context, winner).getBytes());
            fos.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    private static String logForGame(final Context context, final String winner) {
        var log = "";

        if (!(gameIsOverInGbg(context) && gameIsOverInLudii(context))) {
            log += "not representative     =>     ";
        }

        log += "winner: " + winner;

        log += " | played Ludii moves: " + playedLudiiMoves(context);

        log += "\n";

        return log;
    }

    private static boolean gameIsOverInGbg(final Context context) {
        return new AsStateObserverOthello(
            new GbgStateFromLudiiContext(context)
        ).isGameOver();
    }

    private static boolean gameIsOverInLudii(final Context context) {
        return context.trial().over();
    }

    private static String playedLudiiMoves(final Context context) {
        return new LudiiMoves(context)
            .played()
            .entrySet()
            .stream()
            .map(e -> e.getKey().toInt() + "")
            .collect(Collectors.joining(", "));
    }
}
