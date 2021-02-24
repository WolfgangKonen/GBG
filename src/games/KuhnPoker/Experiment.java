package games.KuhnPoker;

import TournamentSystem.TSTimeStorage;
import controllers.ExpectimaxNAgent;
import controllers.MC.MCAgentN;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.StateObservation;
import games.XArenaFuncs;
import params.MCParams;
import params.ParMC;
import params.ParMCTSE;
import tools.ScoreTuple;
import tools.Types;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Experiment {

    private final String directory = "experiments";

    GameBoardKuhnPoker m_gb;
    ArenaKuhnPoker m_arena;

    public static void main(String[] args) throws IOException {
        Experiment ex = new Experiment();
        ex.experiment5();
    }

    public Experiment(){
        tools.Utils.checkAndCreateFolder(directory);
        m_arena = new ArenaKuhnPoker("Kuhn",false);
        m_gb = new GameBoardKuhnPoker(m_arena);
    }

    private void experiment2() throws FileNotFoundException {
        MCAgentN mc = new MCAgentN( new ParMC());
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(mc,kuhnAgent,"experiment2",1000);
    }

    private void experiment4() throws FileNotFoundException {
        ExpectimaxNAgent exmax = new ExpectimaxNAgent("Expectimax");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(exmax,kuhnAgent,"experiment4",1000);
    }

    private void experiment5() throws FileNotFoundException {
        ParMC parmc = new ParMC();
        parmc.setIterations(1000);
        parmc.setRolloutDepth(3);
        MCAgentN mc = new MCAgentN(parmc);
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(mc,kuhnAgent,"experiment5",1000);
    }

    private void experiment3() throws FileNotFoundException {
        MCTSExpectimaxAgt mctsex = new MCTSExpectimaxAgt("MCTSEX",new ParMCTSE());
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(mctsex,kuhnAgent,"experiment3",1000);
    }

    private void experimentFramework(PlayAgent toBeTested, PlayAgent evaluator, String name, int iterations) throws FileNotFoundException {
        setOutput(name);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");

        long start = System.currentTimeMillis();


        PlayAgent[] pavec = new PlayAgent[] {toBeTested,evaluator};

        ScoreTuple sc = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 1, null);

        System.out.println("\n"+pavec[0].getName()+": "+sc.scTup[0]);
        System.out.println(pavec[1].getName() +": "+sc.scTup[1]);

        System.out.print("\n");

        long end = System.currentTimeMillis();

        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");

        long diff = (end-start);

        long s = diff / 1000 % 60;
        long m = diff / (60 * 1000) % 60;
        long h = diff / (60 * 60 * 1000) % 24;


        System.out.printf("\nElapsed time: %02d:%02d:%02d%n ",h,m,s);
    }

    private void experiment1() throws FileNotFoundException {
        setOutput("experiment1");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");

        long start = System.currentTimeMillis();


        RandomAgent randomAgent = new RandomAgent("random");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[] {randomAgent,kuhnAgent};

        ScoreTuple sc = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, 10000, 1, null);

        System.out.println("\n"+pavec[0].getName()+": "+sc.scTup[0]);
        System.out.println(pavec[1].getName() +": "+sc.scTup[1]);

        System.out.print("\n");

        long end = System.currentTimeMillis();

        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");

        long diff = (end-start);

        long s = diff / 1000 % 60;
        long m = diff / (60 * 1000) % 60;
        long h = diff / (60 * 60 * 1000) % 24;


        System.out.printf("\nElapsed time: %02d:%02d:%02d%n ",h,m,s);
    }

    public void setOutput(String experiment) throws FileNotFoundException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        String filename = directory+"/"+experiment+"/"+dtf.format(now)+".log";
        tools.Utils.checkAndCreateFolder( directory+"/"+experiment);
        PrintStream fileOut = new PrintStream(filename);
        System.setOut(fileOut);
    }

    public String getFileName(String myName){
        return "";
    }

    public static ScoreTuple myCompeteNPlayer(PlayAgtVector paVector, StateObservation startSO, int competeNum,
                                            int verbose, TSTimeStorage[] nextTimes) {
        int numPlayers = paVector.getNumPlayers();
        ScoreTuple sc, scMean = new ScoreTuple(numPlayers);
        double sWeight = 1 / (double) competeNum;
        double moveCount = 0.0;
        DecimalFormat frm = new DecimalFormat("#0.000");
        boolean nextMoveSilent = true;
        StateObservation so;
        Types.ACTIONS actBest;
        String sMsg;

        String[] pa_string = new String[numPlayers];
        int[] gamesWon = new int[numPlayers];

        for (int i = 0; i < numPlayers; i++)
            pa_string[i] = paVector.pavec[i].stringDescr();

        switch (numPlayers) {
            case (1):
                sMsg = "Competition, " + String.format("%,d", competeNum) + " episodes: \n" + pa_string[0];
            case (2):
                sMsg = "Competition, " + String.format("%,d", competeNum) + " episodes: \n" +
                        "      X: " + pa_string[0] + " \n" + "   vs O: " + pa_string[1];
                break;
            default:
                sMsg = "Competition, " + String.format("%,d", competeNum)+ " episodes: \n";
                for (int n = 0; n < numPlayers; n++) {
                    sMsg = sMsg + "    P" + n + ": " + pa_string[n];
                    if (n < numPlayers - 1)
                        sMsg = sMsg + ", \n";
                }
                break;
        }
        if (verbose > 0)
            System.out.println(sMsg);

        for (int k = 0; k < competeNum; k++) {
            for (int i = 0; i < numPlayers; i++)
                paVector.pavec[i].resetAgent();

            int player = startSO.getPlayer();
            so = startSO.copy();

            while (true) {
                long startTNano = System.nanoTime();
                actBest = paVector.pavec[player].getNextAction2(so.partialState(), false, nextMoveSilent);
                so.advance(actBest);

                if (so.isGameOver()) {
                    sc = so.getGameScoreTuple();
                    scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
                    moveCount += so.getMoveCounter();

                    if(sc.scTup[0]>0)
                        gamesWon[0]++;
                    else
                        gamesWon[1]++;
                    break; // out of while

                } // if (so.isGameOver())
                if(so.isRoundOver()&&!so.isGameOver()) {
                    so.initRound();
                    assert !so.isRoundOver() : "Error: initRound() did not reset round-over-flag";
                }

                player = so.getPlayer();
            } // while(true)

        } // for (k)

        moveCount /= competeNum;

        if (verbose > 0) {
            if (verbose > 1) {
                System.out.print("Avg ScoreTuple for all players: ");
                System.out.println("   " + scMean.toStringFrm());
            }
            System.out.println("Avg # moves in " + String.format("%,d", competeNum) + " episodes = " + frm.format(moveCount));
        }

        return scMean;
    }
}
