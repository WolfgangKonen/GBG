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

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

public class Experiment {

    private final String directory = "experiments";

    GameBoardKuhnPoker m_gb;
    ArenaKuhnPoker m_arena;
    DecimalFormat df = new DecimalFormat("###.###");

    public static void main(String[] args) throws IOException {
        Experiment ex = new Experiment();
        ex.experiment8();
    }

    public Experiment(){
        tools.Utils.checkAndCreateFolder(directory);
        m_arena = new ArenaKuhnPoker("Kuhn",false);
        m_gb = new GameBoardKuhnPoker(m_arena);
    }


    // How many games must be played to achieve
    private void experiment8() throws IOException {
        RandomAgent ra = new RandomAgent("random");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[] {ra,kuhnAgent};

        String experimentName = "experiment8";

        String filename = setOutput(experimentName);

        FileWriter resultsFile = new FileWriter(filename+"/results.csv");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");
        long start = System.currentTimeMillis();

        int iterations = 1000;
        int rounds = 5000;

        double[] variance = new double[m_gb.getNumPlayers()];
        double[] average = new double[m_gb.getNumPlayers()];
        ScoreTuple[] results = new ScoreTuple[rounds];
        double sWeight = 1 / (double) rounds;
        ScoreTuple scMean = new ScoreTuple(m_gb.getNumPlayers());


        System.out.println(rounds+" competitions with each "+iterations +" iterations.");
        resultsFile.append("Player0\tPlayer1");
        for(int i = 0;i<rounds;i++){
            ScoreTuple sc = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 0, null);
            scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
            results[i] = sc;
            resultsFile.append(df.format(sc.scTup[0])+"\t"+df.format(sc.scTup[1])+"\r\n");
        }
        resultsFile.close();
        for(int i=0;i<results.length;i++){
            for(int p=0;p<m_gb.getNumPlayers();p++){
                variance[p] += Math.pow(scMean.scTup[p]-results[i].scTup[p],2);
            }
        }
        for(int p=0;p<m_gb.getNumPlayers();p++){
            variance[p] /= rounds-1;
        }
        System.out.println("Averages:{");
        System.out.println("\tAverage[0] = "+df.format(scMean.scTup[0])+";");
        System.out.println("\tAverage[1] = "+df.format(scMean.scTup[1])+";");
        System.out.println("}");

        System.out.println("Variance:{");
        System.out.println("\tVariance[0] = "+df.format(variance[0])+";");
        System.out.println("\tVariance[1] = "+df.format(variance[1])+";");
        System.out.println("}");

        System.out.print("\n");
        long end = System.currentTimeMillis();
        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");
        printTimeDiff(end, start);
    }



    private void experiment1() throws FileNotFoundException {
        RandomAgent ra = new RandomAgent("random");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(ra,kuhnAgent,"experiment1",1000000);
    }

    private void experiment6() throws FileNotFoundException {
        MCAgentN mc = new MCAgentN( new ParMC());
        System.out.println("Comment: Changed MC to only simulate till round over.");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(mc,kuhnAgent,"experiment6",1000);
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
        parmc.setRolloutDepth(10);
        MCAgentN mc = new MCAgentN(parmc);
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(mc,kuhnAgent,"experiment5",1000);
    }

    private void experiment3() throws FileNotFoundException {
        MCTSExpectimaxAgt mctsex = new MCTSExpectimaxAgt("MCTSEX",new ParMCTSE());
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentFramework(mctsex,kuhnAgent,"experiment3",1000);
    }

    private void experiment7() throws FileNotFoundException{
        RandomAgent ra = new RandomAgent("random");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        experimentScale(ra,kuhnAgent,"experiment7",100000,1000);
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

        printTimeDiff(end, start);
    }

    private void experimentScale(PlayAgent toBeTested, PlayAgent evaluator, String name, int maxIterations, int steps) throws FileNotFoundException {
        setOutput(name);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");

        long start = System.currentTimeMillis();

        PlayAgent[] pavec = new PlayAgent[] {toBeTested,evaluator};

        ScoreTuple sc = myCompeteNPlayer2(new PlayAgtVector(pavec), m_gb.m_so, maxIterations, steps, 2);

        System.out.println("\n"+pavec[0].getName()+": "+sc.scTup[0]);
        System.out.println(pavec[1].getName() +": "+sc.scTup[1]);

        System.out.print("\n");

        long end = System.currentTimeMillis();

        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");

        printTimeDiff(end, start);
    }

    public void printTimeDiff(long end, long start){
        long diff = (end-start);

        long s = diff / 1000 % 60;
        long m = diff / (60 * 1000) % 60;
        long h = diff / (60 * 60 * 1000) % 24;


        System.out.printf("\nElapsed time: %02d:%02d:%02d%n ",h,m,s);
    }

    public String setOutput(String experiment) throws FileNotFoundException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        String filename = directory+"/"+experiment+"/"+dtf.format(now);
        tools.Utils.checkAndCreateFolder(filename);
        PrintStream fileOut = new PrintStream(filename+"/output.log");
        System.setOut(fileOut);
        return filename;
    }

    public String getFileName(String myName){
        return "";
    }

    public static ScoreTuple myCompeteNPlayer2(PlayAgtVector paVector, StateObservation startSO, int competeNum, int steps, int verbose) {
        int numPlayers = paVector.getNumPlayers();
        ScoreTuple sc, scMean = new ScoreTuple(numPlayers);
        double sWeight =  1 / (double) competeNum;
        double moveCount = 0.0;
        DecimalFormat frm = new DecimalFormat("#0.000");
        boolean nextMoveSilent = true;
        StateObservation so;
        Types.ACTIONS actBest;
        String sMsg;

        int values = competeNum/steps;
        if(competeNum%steps>0)
            values++;
        ScoreTuple[] scoresOverTime = new ScoreTuple[values];
        values = 0;

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

                    if((k+1)%steps==0) {
                        scoresOverTime[values] = new ScoreTuple(scMean);
                        for (int i=0; i<scoresOverTime[values].scTup.length; i++) scoresOverTime[values].scTup[i] = scoresOverTime[values].scTup[i]*competeNum/k;
                        values++;
                    }

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

        double[] variance = new double[numPlayers];

        for (int i = 0;i<scoresOverTime.length;i++){
           System.out.println("@"+i*steps+":"+scoresOverTime[i]);
        }

        return scMean;
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
