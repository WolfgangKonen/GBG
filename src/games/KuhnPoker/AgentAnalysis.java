package games.KuhnPoker;

import controllers.ExpectimaxNAgent;
import controllers.MC.MCAgentN;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import org.json.JSONArray;
import org.json.JSONObject;
import params.ParMC;
import params.ParMCTSE;
import tools.ScoreTuple;
import tools.Types;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgentAnalysis {
    private final String directory = "experiments";

    DecimalFormat df = new DecimalFormat("###.###");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

    public static void main(String[] args) {
        AgentAnalysis agentAnalysis = new AgentAnalysis();
        agentAnalysis.MCNCompetition();
    }

    //<editor-fold desc="Random Agent">
    /**
     * Competition of a
     */
    public void randomAgentCompetition() {
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("Kuhn");
        randomAgentCompetition(kuhnAgent);
    }

    public void randomAgentCompetition(PlayAgent opponent){
        RandomAgent ra = new RandomAgent("random");

        PlayAgent[] pavec = new PlayAgent[]{ra, opponent};
        PlayAgtVector paVector = new PlayAgtVector(pavec);

        try {
            oneRoundCompetition(paVector, 1000000, "randomAgentVsKuhn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold desc="MC Agent">
    /**
     *
     */
    public void MCNCompetition() {
        JSONObject mcObj = new JSONObject();

        ParMC mcpar = new ParMC();
        int iterations = 1000;
        int rolloutDepth = 5;
        mcpar.setStopOnRoundOver(true);

        String comment = "";

        mcpar.setIterations(iterations);
        mcpar.setRolloutDepth(rolloutDepth);

        mcObj.put("Comment", comment);
        mcObj.put("iterations", iterations);
        mcObj.put("rolloutDepth", rolloutDepth);

        MCAgentN mc = new MCAgentN(mcpar);

        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[]{mc, kuhnAgent};
        PlayAgtVector paVector = new PlayAgtVector(pavec);

        try {
            oneRoundCompetition(paVector, 1000000, "McVsKuhn", mcObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold desc="MCTSE Agent">
    /**
     *
     */
    public void mctseAgentCompetition() {
        ParMCTSE parMCTSE = new ParMCTSE();
        parMCTSE.setStopOnRoundOver(true);

        JSONObject mctseObj = new JSONObject();

        String comment = "";
        mctseObj.put("Comment", comment);
        mctseObj.put("rolloutDepth", parMCTSE.getRolloutDepth());
        mctseObj.put("selectMode",parMCTSE.getSelectMode());

        MCTSExpectimaxAgt mctse = new MCTSExpectimaxAgt("MCTSE", parMCTSE);
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        PlayAgent[] pavec = new PlayAgent[]{mctse, kuhnAgent};
        PlayAgtVector paVector = new PlayAgtVector(pavec);

        try {
            oneRoundCompetition(paVector, 1000, "MctseVsKuhn", mctseObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold desc="ExpectiMaxN">
    /**
     *
     */
    public void expectimaxnAgentCompetition() {
        ExpectimaxNAgent expecti = new ExpectimaxNAgent("ExpectimaxN");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[]{expecti, kuhnAgent};

        PlayAgtVector paVector = new PlayAgtVector(pavec);

        JSONObject exObj = new JSONObject();
        String comment = "";
        exObj.put("Comment", comment);

        try {
            oneRoundCompetition(paVector, 1000, "expectimaxnVsKuhn",exObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    /**
     * Methods for playing the competition and store the results.
     */
    //<editor-fold desc="Competition Framework">
    /**
     *
     * @param paVector
     * @param numberOfCompetitions
     * @param experimentName
     * @throws IOException
     */
    public void oneRoundCompetition(PlayAgtVector paVector, int numberOfCompetitions, String experimentName) throws IOException {
        oneRoundCompetition(paVector,numberOfCompetitions,experimentName,null);
    }

    /**
     *
     * @param paVector
     * @param numberOfCompetitions
     * @param experimentName
     * @param additionalJSON
     * @throws IOException
     */
    public void oneRoundCompetition(PlayAgtVector paVector, int numberOfCompetitions, String experimentName, JSONObject additionalJSON) throws IOException {

        StateObserverKuhnPoker stateTest = new StateObserverKuhnPoker();

        // Creating a new directory to write the results-file to
        LocalDateTime now = LocalDateTime.now();
        String folder = directory+"/"+experimentName+"/"+dtf.format(now);
        tools.Utils.checkAndCreateFolder(folder);

        // Creating the output file
        Writer resultsFile = new FileWriter(folder+"/results.csv");

        JSONObject experiment = new JSONObject();
        experiment.put("start", LocalDateTime.now().toString());

        experiment.put("Game","Kuhn Poker");
        if(additionalJSON==null)
            experiment.put("Additional Information","-");
        else
            experiment.put("Additional Information",additionalJSON);
        experiment.put("numberOfCompetitions",numberOfCompetitions);
        experiment.put("ObservedAgent",paVector.pavec[0].getName());
        JSONArray agents = new JSONArray();
        for (int i = 0 ; i < paVector.pavec.length ; i++) {
            agents.put(paVector.pavec[i].getName());
        }
        experiment.put("agents",agents);
        String line = "position\tcard\tmoves\tresult\r\n";
        resultsFile.append(line);

        int numberOfPlayers = paVector.getNumPlayers();
        ScoreTuple shiftedTuple, scMean = new ScoreTuple(numberOfPlayers);
        double sWeight = 1.0/numberOfCompetitions;
        ScoreTuple scStart;

        //StateObservation so;
        StateObserverKuhnPoker so = new StateObserverKuhnPoker();

        ScoreTuple sc;
        Types.ACTIONS actBest;

        scStart = so.getGameScoreTuple();

        int observedPlayer = 0;

        PlayAgtVector qaVector;

        // play the rounds
        // JSONArray rounds = new JSONArray();
        for(int z = 0 ; z < numberOfPlayers ; z++){

            // iterating through all starting positions
            qaVector = paVector.shift(z);
            observedPlayer = z;

            for (int k = 0; k < numberOfCompetitions; k++) {
                // JSONObject round = new JSONObject();

                // resetting all agents
                for (int i = 0; i < numberOfPlayers; i++)
                    qaVector.pavec[i].resetAgent();

                //initilizing a new (random) state
                so = new StateObserverKuhnPoker();

                int player = so.getPlayer();

                while (true) {
                    actBest = qaVector.pavec[player].getNextAction2(so.partialState(), false, false, true);
                    so.advance(actBest, null);
                    if (so.isRoundOver()) {
                        sc = so.getGameScoreTuple();

                        // calculate "reward" as difference between gamescore as beginning of round and gamescore at the end
                        sc.combine(scStart, ScoreTuple.CombineOP.DIFF, observedPlayer, 0);

                                                line = "";
                        line += "" + observedPlayer;
                        line += "\t" + so.getHoleCards(observedPlayer)[0].getRank();
                        line += "\t";

                        /*
                        round.put("observedPlayer",observedPlayer);
                        round.put("holeCard",so.getHoleCards(observedPlayer)[0].getRank());

                        JSONArray moves = new JSONArray();
                        for (Integer move: so.getLastMoves()) {
                            line += move + "-";
                            moves.put(move);
                        }
                        round.put("moves",moves);
                        round.put("reward",sc.scTup[observedPlayer]);
                        */
                        line += "\t" + sc.scTup[observedPlayer];

                        line+="\r\n";
                        resultsFile.append(line);

                        break;
                    }
                    player = so.getPlayer();
                } // while(true)
                // rounds.put(round);

            } // for (k)

        }
        // experiment.put("rounds",rounds);
        resultsFile.close();

        experiment.put("end",LocalDateTime.now().toString());
        Writer metaFile = new FileWriter(folder+"/meta.json");
        metaFile.write(experiment.toString());
        metaFile.close();
    }
    //</editor-fold>

}
