package games.KuhnPoker;

import TournamentSystem.TSTimeStorage;
import agentIO.LoadSaveGBG;
import controllers.ExpectimaxNAgent;
import controllers.MC.MCAgentN;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import controllers.TD.ntuple4.NTuple4Factory;
import controllers.TD.ntuple4.QLearn4Agt;
import controllers.TD.ntuple4.Sarsa4Agt;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.*;
import org.json.JSONArray;
import org.json.JSONObject;
import params.*;
import tools.ScoreTuple;
import tools.Types;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Experiment {

    private final String directory = "experiments";

    GameBoardKuhnPoker m_gb;
    ArenaKuhnPoker m_arena;
    DecimalFormat df = new DecimalFormat("###.###");

    private static final String TD_NTUPLE_3 = "TD-Ntuple-3";
    private static final String TD_NTUPLE_4 = "TD-Ntuple-4";
    private static final String SARSA = "Sarsa";
    private static final String SARSA_4 = "Sarsa-4";
    private static final String QLEARN_4 = "Qlearn-4";
    private static final String EXPECTIMAX = "Expectimax-N";
    private static final String MCTSE = "MCTS Expectimax";
    private static final String KUHN = "Kuhn";
    private static final String RANDOM = "Random";
    private static final String MC = "MC-N";
    private static final String TDS = "TDS";

    public static void main(String[] args) throws Exception {
        Experiment ex = new Experiment();
        ex.oneRoundChallenge(EXPECTIMAX, KUHN, "CompleteRun");
//        ex.oneRoundChallenge(MCTSE, KUHN, "CompleteRun");
//        ex.oneRoundChallenge(MC, KUHN, "CompleteRun");

//        ex.oneRoundChallenge(TD_NTUPLE_3, KUHN, "CompleteRun");
//        ex.oneRoundChallenge(TD_NTUPLE_4, KUHN, "CompleteRun");
//        ex.oneRoundChallenge(SARSA_4, KUHN, "CompleteRun");
//        ex.oneRoundChallenge(QLEARN_4, KUHN, "CompleteRun");
        //ex.oneRoundChallenge(EXPECTIMAX, KUHN, "CompleteRun");
        //ex.oneRoundChallenge(MCTSE, KUHN, "CompleteRun");
        //ex.oneRoundChallenge(KUHN, KUHN, "CompleteRun");
        //ex.oneRoundChallenge(RANDOM, KUHN, "CompleteRun");
        //ex.oneRoundChallenge(MC, KUHN, "CompleteRun");
        //ex.oneRoundChallenge(TDS, KUHN, "CompleteRun");

    }

    public void debug() {
        StateObserverKuhnPoker so;
        so = new StateObserverKuhnPoker();
        MCTSExpectimaxAgt mctsex = new MCTSExpectimaxAgt("MCTSEX",new ParMCTSE());
        do {
            so = new StateObserverKuhnPoker();
        }while(so.getHoleCards(0)[0].getRank()!=9);
        Types.ACTIONS actBest = mctsex.getNextAction2(so.partialState(), false, false, true);
        System.out.println(actBest.toInt());
    }

    public void debug2() {
        StateObserverKuhnPoker so;
        do {
            so = new StateObserverKuhnPoker();
        }while(so.getHoleCards(1)[0].getRank()!=10);
        so.advance(Types.ACTIONS.fromInt(2), null);

        ParMC mcpar = new ParMC();
        mcpar.setIterations(1000);
        mcpar.setRolloutDepth(5);

        MCAgentN mc = new MCAgentN(mcpar);

        Types.ACTIONS act = mc.getNextAction2(so.partialState(), false, false, true);

    }

    public PlayAgent trainAgent(PlayAgent pa, GameBoard gb, String savePath) throws IOException {
        gb.initialize();
        ParOther oPar =  pa.getParOther();
        // todo: this should be the bare minimum - probably it's a good idea to add some additional analytics.
        while (pa.getGameNum() < pa.getMaxGameNum()) {
            StateObservation so;
            if(oPar.getChooseStart01()){
                so = gb.chooseStartState();
            }else{
                so = gb.getDefaultStartState(null);
            }
            pa.trainAgent(so);
        }

        // save agent
        LoadSaveGBG tdAgentIO = new LoadSaveGBG(directory,null);
        tdAgentIO.saveGBGAgent(pa,savePath);
        // return agent
        return pa;
    }

    public PlayAgent setupTDNTuple3Agent(String dir) throws Exception {
        String agentName = TD_NTUPLE_3;

        GameBoardKuhnPoker gb = new GameBoardKuhnPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsKuhnPoker();

        ParNT ntPar = new ParNT();
        ntPar.setNumTuple(4);
        ntPar.setMaxTupleLen(4);

        ParTD tdPar = new ParTD();

        ParOther oPar = new ParOther();

        int maxGameNum = 100000;

        NTupleFactory ntupfac = new NTupleFactory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnf);
        PlayAgent pa = new TDNTuple3Agt(agentName, tdPar, ntPar, oPar, nTuples, xnf, maxGameNum);

        pa = trainAgent(pa,gb,savePath);

        // return agent
        return pa;
    }

    public PlayAgent setupTDNTuple4Agent(String dir) throws Exception {
        String agentName = TD_NTUPLE_4;
        GameBoardKuhnPoker gb = new GameBoardKuhnPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsKuhnPoker();

        ParNT ntPar = new ParNT();
        ntPar.setNumTuple(4);
        ntPar.setMaxTupleLen(4);

        ParTD tdPar = new ParTD();

        ParOther oPar = new ParOther();

        ParRB rbPar = new ParRB();
        ParWrapper wrPar = new ParWrapper();

        int maxGameNum = 100000;

        NTuple4Factory ntupfac = new NTuple4Factory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnf);
        PlayAgent pa = new TDNTuple4Agt(agentName,tdPar,ntPar, oPar,rbPar, wrPar, nTuples, xnf, maxGameNum);

        pa = trainAgent(pa,gb,savePath);

        // return agent
        return pa;
    }

    public PlayAgent setupSarsaAgent(String dir) throws Exception {
        String agentName = SARSA;
        GameBoardKuhnPoker gb = new GameBoardKuhnPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsKuhnPoker();

        ParNT ntPar = new ParNT();
        ntPar.setNumTuple(4);
        ntPar.setMaxTupleLen(4);

        ParTD tdPar = new ParTD();

        ParOther oPar = new ParOther();

        int maxGameNum = 100000;


        NTupleFactory ntupfac = new NTupleFactory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnf);
        // int numOutputs =
        // m_xab.m_game.gb.getDefaultStartState().getAllAvailableActions().size();
        ArrayList<Types.ACTIONS> allAvailActions = gb.getDefaultStartState(null).getAllAvailableActions();
        PlayAgent pa = new SarsaAgt(agentName, tdPar, ntPar,
                oPar, nTuples, xnf, allAvailActions, maxGameNum);

        pa = trainAgent(pa,gb,savePath);

        // return agent
        return pa;
    }

    public PlayAgent setupSarsa4Agent(String dir) throws Exception {
        String agentName = SARSA_4;
        GameBoardKuhnPoker gb = new GameBoardKuhnPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsKuhnPoker();

        ParNT ntPar = new ParNT();
        ntPar.setNumTuple(4);
        ntPar.setMaxTupleLen(4);

        ParTD tdPar = new ParTD();

        ParOther oPar = new ParOther();

        int maxGameNum = 100000;

        NTuple4Factory ntupfac = new NTuple4Factory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnf);
        ArrayList<Types.ACTIONS> allAvailActions = gb.getDefaultStartState(null).getAllAvailableActions();
        PlayAgent pa = new Sarsa4Agt(agentName, tdPar, ntPar,
                oPar, nTuples, xnf, allAvailActions, maxGameNum);


        pa = trainAgent(pa,gb,savePath);
        // return agent
        return pa;
    }

    public PlayAgent setupQLearn4Agent(String dir) throws Exception {
        String agentName = QLEARN_4;

        GameBoardKuhnPoker gb = new GameBoardKuhnPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsKuhnPoker();

        ParNT ntPar = new ParNT();
        ntPar.setNumTuple(4);
        ntPar.setMaxTupleLen(4);

        ParTD tdPar = new ParTD();

        ParOther oPar = new ParOther();

        int maxGameNum = 100000;

        NTuple4Factory ntupfac = new NTuple4Factory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnf);
        ArrayList<Types.ACTIONS> allAvailActions = gb.getDefaultStartState(null).getAllAvailableActions();
        PlayAgent pa = new QLearn4Agt(agentName, tdPar, ntPar,
                oPar, nTuples, xnf, allAvailActions, maxGameNum);

        pa = trainAgent(pa,gb,savePath);
        // return agent
        return pa;
    }

    public PlayAgent setupTDSAgent(String dir) throws IOException {
        String agentName = TDS;

        GameBoardKuhnPoker gb = new GameBoardKuhnPoker();
        String savePath = dir;
        savePath+="/agent";

        // define parameters
        ParTD tdPar = new ParTD();
        ParOther oPar = new ParOther();

        int maxGameNum = 100000;
        int gameNum;

        //int stopTest = oPar.getStopTest();
        //int stopEval = oPar.getStopEval();
        int qem = oPar.getQuickEvalMode();
        int verbose = 1;
        int tem = oPar.getTrainEvalMode();

        // frequency how often to evaluate
        int numEval = 1000;

        // setup the feature to be used for TDS
        int featmode = 0;
        Feature feature = new FeatureKuhnPoker(featmode);

        PlayAgent tds = new TDAgent(agentName, tdPar, oPar, feature, maxGameNum);
        tds.setMaxGameNum(maxGameNum);
        tds.setNumEval(numEval);
        tds.setGameNum(0);

        tds = trainAgent(tds,gb,savePath);

        // Evaluator with quick evaluation
        Evaluator m_evaluatorQ = new EvaluatorKuhnPoker(tds,gb, qem,verbose);

        //// train
        //// basically we could utilize m_xfun.train(n,agentN, m_xab, gb) but unfortunatelly this is using a lot of Arena functions that woul require a lot of config stuff.
        //// so I will rewrite it for now which is not great for reusability but might be easier to start with.
//
        //gb.initialize();
        //// todo: this should be the bare minimum - probably it's a good idea to add some additional analytics.
        //while (tds.getGameNum() < tds.getMaxGameNum()) {
        //    StateObservation so;
        //    if(oPar.getChooseStart01()){
        //        so = gb.chooseStartState();
        //    }else{
        //        so = gb.getDefaultStartState();
        //    }
        //    tds.trainAgent(so);
        //    gameNum = tds.getGameNum();
//
        //    //this is purely for analytics purpose, right?
        //    // do i need it? (~line 702 XArenaFuncs)
        //    //if (gameNum % numEval == 0) {
        //    //    m_evaluatorQ.eval(tds); // todo: originially they used a wrapped agent is used. Why?
        //    //}
//
        //    // finish training before the max number of games.
        //    //if (stopTest > 0 && (gameNum - 1) % numEval == 0 && stopEval > 0) {
        //    //
        //    //}
        //}
//
        //// save agent
        //LoadSaveGBG tdAgentIO = new LoadSaveGBG(directory,null);
        //tdAgentIO.saveGBGAgent(tds,savePath);
        //// return agent
        return tds;
    }

    public PlayAgent setupMCNAgent(String dir) {
        String agentName = MC;
        ParMC mcpar = new ParMC();
        mcpar.setIterations(1000);
        mcpar.setRolloutDepth(5);

        MCAgentN mc = new MCAgentN(agentName,mcpar,new ParOther());
        return mc;
    }

    public PlayAgent setupMCTSAgent(String dir){
        String agentName = MCTSE;
        MCTSExpectimaxAgt mctse = new MCTSExpectimaxAgt(agentName,new ParMCTSE());
        return mctse;
    }

    public PlayAgent setupKuhnAgent(String dir) throws IOException {
        String agentName = KUHN;
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent(agentName);
        return kuhnAgent;
    }

    public PlayAgent setupRandomAgent(String dir) throws IOException {
        String agentName = RANDOM;
        RandomAgent ra = new RandomAgent(agentName);
        return ra;
    }

    public  PlayAgent setupExpectiMax(String dir){
        String agentName = EXPECTIMAX;
        ExpectimaxNAgent expecti = new ExpectimaxNAgent(agentName);
        return expecti;
    }

    public void debugCardDistribution(){
        int[][] overview = new int[2][3];

        for(int i=0;i<1000000;i++){
            StateObserverKuhnPoker so = new StateObserverKuhnPoker();
            overview[0][so.getHoleCards(0)[0].getRank()-9]++;
            overview[1][so.getHoleCards(1)[0].getRank()-9]++;
        }

        System.out.println("player 0");
        for(int i = 0;i<3;i++)
            System.out.println(i+9+":"+overview[0][i]);

        System.out.println();
        System.out.println("player 1");
        for(int i = 0;i<3;i++)
            System.out.println(i+9+":"+overview[1][i]);

    }

    /**
     * To determine the quality of an agent it might not be the best way to playout a complete game.
     * The results are difficult to compare and it might make things easier to play only one round.
     */
    public void oneRoundChallenge(String p0, String p1, String experiment) throws Exception {

        // the experitment name will be the foldername in /experiments
        String experimentName = experiment;

        // number of rounds played for evaluation for each position
        int playRounds = 100000; //10000; //1000000;   // /WK/

        PlayAgent observedAgent;
        PlayAgent benchmarkAgent;

        // add a folder for the current time if an experiment is executed again
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();

        // Setup the folder name and create it if it doesn't exist
        String folder = directory+"/"+experimentName+"/"+dtf.format(now);
        tools.Utils.checkAndCreateFolder(folder);

        // setup file writers for additional information and the actual results
        Writer resultsFile = new FileWriter(folder+"/results.csv");

        // information that will be gathered in the experiment
        String line = "position\tcard\tcard_opponent\tmoves\tresult\r\n";
        resultsFile.append(line);

        // Setup Agents
        switch (p0){
            case KUHN -> observedAgent = setupKuhnAgent(folder);
            case RANDOM -> observedAgent =  setupRandomAgent(folder);
            case TD_NTUPLE_3 -> observedAgent =  setupTDNTuple3Agent(folder);
            case TD_NTUPLE_4 -> observedAgent =  setupTDNTuple4Agent(folder);
            case SARSA -> observedAgent =  setupSarsaAgent(folder);
            case SARSA_4 -> observedAgent =  setupSarsa4Agent(folder);
            case QLEARN_4 -> observedAgent =  setupQLearn4Agent(folder);
            case EXPECTIMAX -> observedAgent =  setupExpectiMax(folder);
            case MCTSE -> observedAgent =  setupMCTSAgent(folder);
            case MC -> observedAgent =  setupMCNAgent(folder);
            case TDS -> observedAgent =  setupTDSAgent(folder);
            default -> throw new Exception("not supported agent for p0");
        }

        switch (p1){
            case KUHN -> benchmarkAgent = setupKuhnAgent(folder);
            case RANDOM -> benchmarkAgent =  setupRandomAgent(folder);
            case TD_NTUPLE_3 -> benchmarkAgent =  setupTDNTuple3Agent(folder);
            case TD_NTUPLE_4 -> benchmarkAgent =  setupTDNTuple4Agent(folder);
            case SARSA -> benchmarkAgent =  setupSarsaAgent(folder);
            case SARSA_4 -> benchmarkAgent =  setupSarsa4Agent(folder);
            case QLEARN_4 -> benchmarkAgent =  setupQLearn4Agent(folder);
            case EXPECTIMAX -> benchmarkAgent =  setupExpectiMax(folder);
            case MCTSE -> benchmarkAgent =  setupMCTSAgent(folder);
            case MC -> benchmarkAgent =  setupMCNAgent(folder);
            case TDS -> benchmarkAgent =  setupTDSAgent(folder);
            default -> throw new Exception("not supported agent for p1");
        }

        PlayAgent[] pavec = new PlayAgent[] {observedAgent,benchmarkAgent};

        PlayAgtVector paVector = new PlayAgtVector(pavec);
        int numPlayers = paVector.getNumPlayers();
        ScoreTuple[] means = new ScoreTuple[numPlayers];
        ScoreTuple[] sums = new ScoreTuple[numPlayers];
        ScoreTuple  shiftedTuple, scMean = new ScoreTuple(numPlayers);

        for(int i=0;i<numPlayers;i++){
            means[i] = new ScoreTuple(numPlayers);
            sums[i] = new ScoreTuple(numPlayers);
        }

        double sWeight = 1.0/playRounds;
        ScoreTuple scStart;

        // define startSO
        StateObserverKuhnPoker startSO = new StateObserverKuhnPoker();

        //StateObservation so;
        StateObserverKuhnPoker so;

        ScoreTuple sc;
        Types.ACTIONS actBest;

        scStart=startSO.getGameScoreTuple();

        int observedPlayer = 0;

        PlayAgtVector qaVector;

        // play the rounds
        for(int z = 0;z<numPlayers;z++){

            qaVector = paVector.shift(z);
            observedPlayer = z;

            for (int k = 0; k < playRounds; k++) {
                for (int i = 0; i < numPlayers; i++)
                    qaVector.pavec[i].resetAgent();

                int player = startSO.getPlayer();

                //initilizing a new (random) state
                so = new StateObserverKuhnPoker();

                while (true) {
                    long startTNano = System.nanoTime();
                    actBest = qaVector.pavec[player].getNextAction2(so.partialState(), false, false, true);

                    so.advance(actBest, null);
                    if (so.isRoundOver()) {
                        sc = so.getGameScoreTuple();
                        if (!StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY) {
                            // calculate "reward" as score change for the turn
                            // (if PLAY_ONE_ROUND_ONLY is true, we do not need this, because this switch subtracts the
                            // start chips already in StateObserverKuhnPoker)
                            sc.combine(scStart, ScoreTuple.CombineOP.DIFF, observedPlayer, 0);
                        }
                        means[z].combine(sc, ScoreTuple.CombineOP.AVG, observedPlayer, sWeight);
                        sums[z].combine(sc, ScoreTuple.CombineOP.SUM, observedPlayer, 0);

                        // Information:
                            // Position
                            // P1 : Hand
                            // P2 : Hand
                            // Aktionen SO:LastMoves
                            // Result
                        line = "";

                        line += "" + observedPlayer;
                        line += "\t" + so.getHoleCards(observedPlayer)[0].getRank();
                        line += "\t" + so.getHoleCards((observedPlayer+1)%2)[0].getRank();
                        line += "\t";

                        for (Integer move: so.getLastMoves()) {
                            line += move + "-";
                        }

                        line += "\t" + sc.scTup[observedPlayer];

                        line+="\r\n";
                        resultsFile.append(line);
                        break;
                    }
                    player = so.getPlayer();
                } // while(true)

            } // for (k)
        }

        Writer infoFile = new FileWriter(folder+"/info.json");
        JSONObject experimentOverview = new JSONObject();
        JSONArray players = new JSONArray();
        for(int i=0;i<pavec.length;i++){
            JSONObject player = writeAgentToJson(pavec[i]);
            player.put("position",i);
            players.put(player);
        }
        experimentOverview.put("players",players);

        experimentOverview.put("start",now.toString());
        experimentOverview.put("end",LocalDateTime.now().toString());
        experimentOverview.put("rounds",playRounds);
        for(int i=0;i<pavec.length;i++){
            experimentOverview.put("meanScoreForPosition"+i,means[i].scTup[i]);
            System.out.println("meanScoreForPosition "+i+": "+means[i].scTup[i]);
        }
        System.out.println("meanScore: "+(means[0].scTup[0]+means[1].scTup[1])/2);


        infoFile.write(experimentOverview.toString());

        resultsFile.close();
        infoFile.close();
    }

    public JSONObject writeAgentToJson(PlayAgent pa){
        JSONObject player = new JSONObject();
        player.put("name",pa.getName());

        JSONArray pars = new JSONArray();
        pars.put(parOtherToJSON(pa.getParOther()));

        //String agentName = pa.getClass().getSimpleName();
        String agentName = pa.getName();
        switch (agentName){
            case TD_NTUPLE_3:
                pars.put(parNTToJSON(((TDNTuple3Agt)pa).getParNT()));
                pars.put(parTDToJSON(((TDNTuple3Agt)pa).getParTD()));
                JSONObject tdnt3 = new JSONObject();
                tdnt3.put("maxGameNum",((TDNTuple3Agt)pa).getMaxGameNum());
                pars.put(tdnt3);
                break;
            case TD_NTUPLE_4:
                pars.put(parNTToJSON(((TDNTuple4Agt)pa).getParNT()));
                pars.put(parTDToJSON(((TDNTuple4Agt)pa).getParTD()));
                JSONObject tdnt4 = new JSONObject();
                tdnt4.put("maxGameNum",((TDNTuple4Agt)pa).getMaxGameNum());
                pars.put(tdnt4);
                break;
            case SARSA:
                pars.put(parNTToJSON(((SarsaAgt)pa).getParNT()));
                pars.put(parTDToJSON(((SarsaAgt)pa).getParTD()));
                JSONObject sarsa = new JSONObject();
                sarsa.put("maxGameNum",((SarsaAgt)pa).getMaxGameNum());
                pars.put(sarsa);
                break;
            case SARSA_4:
                pars.put(parNTToJSON(((Sarsa4Agt)pa).getParNT()));
                pars.put(parTDToJSON(((Sarsa4Agt)pa).getParTD()));
                JSONObject sarsa4 = new JSONObject();
                sarsa4.put("maxGameNum",((Sarsa4Agt)pa).getMaxGameNum());
                pars.put(sarsa4);
                break;
            case QLEARN_4:
                pars.put(parNTToJSON(((QLearn4Agt)pa).getParNT()));
                pars.put(parTDToJSON(((QLearn4Agt)pa).getParTD()));
                JSONObject qlearn4 = new JSONObject();
                qlearn4.put("maxGameNum",((QLearn4Agt)pa).getMaxGameNum());
                pars.put(qlearn4);
                break;
            case TDS:
                pars.put(parTDToJSON(((TDAgent)pa).getParTD()));
                JSONObject td = new JSONObject();
                td.put("maxGameNum",((TDAgent)pa).getMaxGameNum());
                pars.put(td);
                break;
            case MC:
                pars.put(parMCToJSON(((MCAgentN)pa).getParMC()));
                break;
            case MCTSE:
                pars.put(parMCTSEToJSON(((MCTSExpectimaxAgt)pa).getParMCTSE()));
                break;
            case KUHN:
                JSONObject kuhn = new JSONObject();
                kuhn.put("alpha",((KuhnPokerAgent)pa).getAlpha());
                pars.put(kuhn);
                break;
        }

        player.put("parameters",pars);
        return player;
    }

    public JSONObject parNTToJSON(ParNT pNt){
        JSONObject par = new JSONObject();
        par.put("Type","NT");
        par.put("TC",pNt.getTc());
        par.put("INIT",pNt.getTcInit());
        par.put("TCTransfer",pNt.getTcTransferMode());
        par.put("TCExpBeta",pNt.getTcBeta());
        par.put("TCAccumulate",pNt.getTcAccumulMode());
        par.put("AFTERSTATE",pNt.getAFTERSTATE());
        par.put("NTupleRandomness",pNt.getRandomness());
        par.put("NTupleFixed",pNt.getFixedNtupleMode());
        par.put("RandomWalk",pNt.getRandomWalk());
        par.put("PLOTWEIGHT",pNt.getPlotWeightMethod());
        par.put("NumberOfNTuples",pNt.getNtupleNumber());
        par.put("NTupleSize",pNt.getNtupleNumber());
        par.put("UseSymmetry",pNt.getUSESYMMETRY());
        par.put("nSym",pNt.getNSym());
        return par;
    }

    public JSONObject parTDToJSON(ParTD pTd){
        JSONObject par = new JSONObject();
        par.put("Type","TD");
        par.put("AlphaInit",pTd.getAlpha());
        par.put("AlphaFinal",pTd.getAlphaFinal());
        par.put("Lambda",pTd.getLambda());
        par.put("EpsilonInit",pTd.getEpsilon());
        par.put("EpsilonFinal",pTd.getEpsilonFinal());
        par.put("Gamma",pTd.getGamma());
        par.put("HorizonCut",pTd.getHorizonCut());
        par.put("Epochs",pTd.getEpochs());
        par.put("Eligibiltiy",pTd.getEligMode());
        par.put("FeatureSet",pTd.getFeatmode());
        par.put("OutputSigmoid",pTd.hasSigmoid());
        par.put("Normalize",pTd.getNormalize());
        par.put("StopOnRoundOver",pTd.hasStopOnRoundOver());
        par.put("LinearNet",pTd.hasLinearNet());
        return par;
    }
    public JSONObject parMCTSEToJSON(ParMCTSE pMcts){
        JSONObject par = new JSONObject();
        par.put("Type","MCTS");
        return par;
    }

    public JSONObject parMCToJSON(ParMC pMc){
        JSONObject par = new JSONObject();
        par.put("Type","MC");
        return par;
    }


    public JSONObject parOtherToJSON(ParOther pOther){
        JSONObject par = new JSONObject();
        par.put("Type","Other");
        return par;
    }

    public void gameChallenge() throws IOException {
        //Utils.checkAndCreateFolder(directory);
        //m_arena = new ArenaKuhnPoker("Kuhn",false);
        //m_gb = new GameBoardKuhnPoker(m_arena);
        StateObserverKuhnPoker stateTest = new StateObserverKuhnPoker();

        PlayAgent observedAgent;
        int playRounds = 1000;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();

        String experimentName = "newapproach";

        String folder = directory+"/"+experimentName+"/"+dtf.format(now);
        tools.Utils.checkAndCreateFolder(folder);

        Writer infoFile = new FileWriter(folder+"/info.txt");


        Writer resultsFile = new FileWriter(folder+"/results.csv");

        String line = "position\tresult\r\n";
        resultsFile.append(line);

        // setup agents
        // todo: make those a parameter of the function call
        RandomAgent ra = new RandomAgent("random");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

        ParMC mcpar = new ParMC();
        mcpar.setIterations(1000);
        mcpar.setRolloutDepth(5);

        MCAgentN mc = new MCAgentN(mcpar);

        KuhnPokerAgent evilKuhnAgent = new KuhnPokerAgent("evil-kuhn");
        MCTSExpectimaxAgt mctsex = new MCTSExpectimaxAgt("MCTSEX",new ParMCTSE());
        ExpectimaxNAgent expecti = new ExpectimaxNAgent("ExpectimaxN");


        MCAgentN mcStochastic = new MCAgentN(mcpar);
        //mcStochastic.setUseStochastic(true);

        //PlayAgent tds = setupTDSAgent(folder);

        observedAgent = expecti;
        try {
            //observedAgent = setupQLearn4Agent(folder);
            //observedAgent = setupSarsa4Agent(folder);
            observedAgent = setupTDNTuple3Agent(folder);
            //observedAgent = setupTDNTuple4Agent(folder);
            //observedAgent = alpha;
        }catch (Exception e){
            System.out.println("bummer");
        }

        PlayAgent[] pavec = new PlayAgent[] {kuhnAgent,observedAgent};
        infoFile.append("Kuhn - Alpha: 0.2"+"\n");
        infoFile.append("Player0: "+pavec[0].getName()+"\n");
        infoFile.append("Player1: "+pavec[1].getName()+"\n");

        PlayAgtVector paVector = new PlayAgtVector(pavec);
        int numPlayers = paVector.getNumPlayers();
        ScoreTuple  shiftedTuple, scMean = new ScoreTuple(numPlayers);
        double sWeight = 1.0/playRounds;
        ScoreTuple scStart;

        // define startSO
//        StateObservation startSO = m_gb.m_so;
        StateObserverKuhnPoker startSO;
        startSO = stateTest;

        //StateObservation so;
        StateObserverKuhnPoker so;

        ScoreTuple sc;
        Types.ACTIONS actBest;

        scStart=startSO.getGameScoreTuple();

        int observedPlayer = 0;

        PlayAgtVector qaVector;
        // play the rounds
        for(int z = 0;z<numPlayers;z++){

            qaVector = paVector.shift(z);
            observedPlayer = z;

            for (int k = 0; k < playRounds; k++) {
                for (int i = 0; i < numPlayers; i++)
                    qaVector.pavec[i].resetAgent();

                int player = startSO.getPlayer();
                //initilizing a new (random) state
                so = new StateObserverKuhnPoker();

                while (true) {
                    long startTNano = System.nanoTime();
                    actBest = qaVector.pavec[player].getNextAction2(so.partialState(), false, false, true);

                    so.advance(actBest, null);
                    if (so.isGameOver()) {
                        sc = so.getGameScoreTuple();
                        // calculate "reward"
                        //sc.combine(scStart, ScoreTuple.CombineOP.DIFF, observedPlayer, 0);
                        //scMean.combine(sc, ScoreTuple.CombineOP.AVG, observedPlayer, sWeight);

                        // Information:
                        // Player
                        // Score
                        line = "";
                        line += "" + observedPlayer;
                        line += "\t" + sc.scTup[observedPlayer];
                        line+="\r\n";
                        resultsFile.append(line);
                        break;
                    }
                    if(so.isRoundOver())
                        so.initRound();
                    player = so.getPlayer();
                } // while(true)

            } // for (k)
            //shiftedTuple = sc.shift(numPlayers - z);

        }
        resultsFile.close();
        infoFile.close();
    }


    public Experiment(){
        tools.Utils.checkAndCreateFolder(directory);
        m_arena = new ArenaKuhnPoker("Kuhn",false);
        m_gb = new GameBoardKuhnPoker(m_arena);
    }

    private void random_experiment_static(){
        try{
            RandomAgent ra = new RandomAgent("random");
            KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
            PlayAgent[] pavec = new PlayAgent[] {ra,kuhnAgent};

            String experimentName = "RandomAgent_Kuhn_static";
            doExperimentStuff(experimentName,pavec,1000000);
        }catch (Exception e){

        }
    }

    private void expecti_experiment_static(){
        try{
            ExpectimaxNAgent expecti = new ExpectimaxNAgent("ExpectimaxN");
            KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
            PlayAgent[] pavec = new PlayAgent[] {expecti,kuhnAgent};

            String experimentName = "Expectimax_Kuhn_static";
            doExperimentStuff(experimentName,pavec,1000000);
        }catch (Exception e){

        }
    }

    private void MC_experiment_static(){
        try{

            ParMC mcpar = new ParMC();
            mcpar.setIterations(1000);
            mcpar.setRolloutDepth(5);

            MCAgentN mc = new MCAgentN(mcpar);
            System.out.println("Comment: Changed MC to only simulate till round over.");
            KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
            PlayAgent[] pavec = new PlayAgent[] {mc,kuhnAgent};

            String experimentName = "MC_Kuhn_static";
            doExperimentStuff(experimentName,pavec,100000);
        }catch (Exception e){

        }
    }

    private void MCTS_experiment_static(){
        try{

            MCTSExpectimaxAgt mctsex = new MCTSExpectimaxAgt("MCTSEX",new ParMCTSE());
            KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");

            PlayAgent[] pavec = new PlayAgent[] {mctsex,kuhnAgent};

            String experimentName = "MCTS_Kuhn_static";
            doExperimentStuff(experimentName,pavec,1000);
        }catch (Exception e){

        }
    }


    private void doExperimentStuff(String experimentName,PlayAgent[] pavec, int games) throws IOException {


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();

        String folder = directory+"/"+experimentName+"/"+dtf.format(now);
        tools.Utils.checkAndCreateFolder(folder);
        PrintStream fileOut = new PrintStream(folder+"/output.log");
        System.setOut(fileOut);

        FileWriter resultsFile = new FileWriter(folder+"/results.csv");

        System.out.println(dtf.format(now) + ": Start");
        long start = System.currentTimeMillis();


        PlayAgent[] players = pavec;

        for (PlayAgent player : players) {
            resultsFile.append(player.getName()+"\t");
        }
        resultsFile.append("Rounds\r\n");

        String rs = "";
        for(int i = 0;i<games;i++){
            resultTuple rt = mySingleCompete(new PlayAgtVector(pavec), m_gb.m_so);
            for(int p = 0;p<players.length;p++){
                rs += df.format(rt.st.scTup[p])+"\t";
            }
            rs += df.format(rt.mc)+"\r\n";
        }
        resultsFile.append(rs);
        resultsFile.close();

        long end = System.currentTimeMillis();
        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");
        printTimeDiff(end, start);

    }


    // How many games must be played to achieve
    private void experiment11() throws IOException {
        ParMCTSE pars = new ParMCTSE();
        MCTSExpectimaxAgt mctsexp = new MCTSExpectimaxAgt("mctsexpectimax",pars);

        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[] {mctsexp,kuhnAgent};

        String experimentName = "experiment11";

        String filename = setOutput(experimentName);

        FileWriter resultsFile = new FileWriter(filename+"/results.csv");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");
        long start = System.currentTimeMillis();

        int iterations = 100;
        int rounds = 10;

        double[] variance = new double[m_gb.getNumPlayers()];
        double[] average = new double[m_gb.getNumPlayers()];
        resultTuple[] results = new resultTuple[rounds];
        double sWeight = 1 / (double) rounds;
        ScoreTuple scMean = new ScoreTuple(m_gb.getNumPlayers());


        System.out.println(df.format(rounds)+" competitions with each "+df.format(iterations) +" iterations.");
        resultsFile.append("Player0\tPlayer1\tRounds\r\n");
        for(int i = 0;i<rounds;i++){
            resultTuple st = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 0, null);
            ScoreTuple sc = st.st;
            scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
            results[i] = st;
            resultsFile.append(df.format(sc.scTup[0])+"\t"+df.format(sc.scTup[1])+"\t"+df.format(st.mc)+"\r\n");
        }
        resultsFile.close();
        double mcavg = 0;
        for(int i=0;i<results.length;i++){
            for(int p=0;p<m_gb.getNumPlayers();p++){
                variance[p] += Math.pow(scMean.scTup[p]-results[i].st.scTup[p],2);
            }
            mcavg += results[i].mc;
        }
        mcavg /= rounds;
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

        System.out.println("Rounds:" + df.format(mcavg));

        System.out.print("\n");
        long end = System.currentTimeMillis();
        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");
        printTimeDiff(end, start);
    }

    // How many games must be played to achieve
    private void experiment10() throws IOException {
        ExpectimaxNAgent expecti = new ExpectimaxNAgent("ExpectimaxN");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[] {expecti,kuhnAgent};

        String experimentName = "experiment10";

        String filename = setOutput(experimentName);

        FileWriter resultsFile = new FileWriter(filename+"/results.csv");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");
        long start = System.currentTimeMillis();

        int iterations = 10000;
        int rounds = 500;

        double[] variance = new double[m_gb.getNumPlayers()];
        double[] average = new double[m_gb.getNumPlayers()];
        resultTuple[] results = new resultTuple[rounds];
        double sWeight = 1 / (double) rounds;
        ScoreTuple scMean = new ScoreTuple(m_gb.getNumPlayers());


        System.out.println(df.format(rounds)+" competitions with each "+df.format(iterations) +" iterations.");
        resultsFile.append("Player0\tPlayer1\tRounds\r\n");
        for(int i = 0;i<rounds;i++){
            resultTuple st = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 0, null);
            ScoreTuple sc = st.st;
            scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
            results[i] = st;
            resultsFile.append(df.format(sc.scTup[0])+"\t"+df.format(sc.scTup[1])+"\t"+df.format(st.mc)+"\r\n");
        }
        resultsFile.close();
        double mcavg = 0;
        for(int i=0;i<results.length;i++){
            for(int p=0;p<m_gb.getNumPlayers();p++){
                variance[p] += Math.pow(scMean.scTup[p]-results[i].st.scTup[p],2);
            }
            mcavg += results[i].mc;
        }
        mcavg /= rounds;
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

        System.out.println("Rounds:" + df.format(mcavg));

        System.out.print("\n");
        long end = System.currentTimeMillis();
        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");
        printTimeDiff(end, start);
    }



    // How many games must be played to achieve
    private void experiment9() throws IOException {
        ParMC mcpar = new ParMC();
        mcpar.setIterations(1000);
        mcpar.setRolloutDepth(5);

        MCAgentN mc = new MCAgentN(mcpar);
        System.out.println("Comment: Changed MC to only simulate till round over.");
        KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("kuhn");
        PlayAgent[] pavec = new PlayAgent[] {mc,kuhnAgent};

        String experimentName = "experiment9";

        String filename = setOutput(experimentName);

        FileWriter resultsFile = new FileWriter(filename+"/results.csv");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Start");
        long start = System.currentTimeMillis();

        int iterations = 2000;
        int rounds = 100;

        double[] variance = new double[m_gb.getNumPlayers()];
        double[] average = new double[m_gb.getNumPlayers()];
        resultTuple[] results = new resultTuple[rounds];
        double sWeight = 1 / (double) rounds;
        ScoreTuple scMean = new ScoreTuple(m_gb.getNumPlayers());


        System.out.println(df.format(rounds)+" competitions with each "+df.format(iterations) +" iterations.");
        resultsFile.append("Player0\tPlayer1\tRounds\r\n");
        for(int i = 0;i<rounds;i++){
            resultTuple st = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 0, null);
            ScoreTuple sc = st.st;
            scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
            results[i] = st;
            resultsFile.append(df.format(sc.scTup[0])+"\t"+df.format(sc.scTup[1])+"\t"+df.format(st.mc)+"\r\n");
        }
        resultsFile.close();
        double mcavg = 0;
        for(int i=0;i<results.length;i++){
            for(int p=0;p<m_gb.getNumPlayers();p++){
                variance[p] += Math.pow(scMean.scTup[p]-results[i].st.scTup[p],2);
            }
            mcavg += results[i].mc;
        }
        mcavg /= rounds;
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

        System.out.println("Rounds:" + df.format(mcavg));

        System.out.print("\n");
        long end = System.currentTimeMillis();
        System.out.println(dtf.format(LocalDateTime.now()) + ": Done");
        printTimeDiff(end, start);
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

        int iterations = 1000000;
        int rounds = 100;

        double[] variance = new double[m_gb.getNumPlayers()];
        double[] average = new double[m_gb.getNumPlayers()];
        resultTuple[] results = new resultTuple[rounds];
        double sWeight = 1 / (double) rounds;
        ScoreTuple scMean = new ScoreTuple(m_gb.getNumPlayers());


        System.out.println(df.format(rounds)+" competitions with each "+df.format(iterations) +" iterations.");
        resultsFile.append("Player0\tPlayer1\tRounds\r\n");
        for(int i = 0;i<rounds;i++){
            resultTuple st = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 0, null);
            ScoreTuple sc = st.st;
            scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
            results[i] = st;
            resultsFile.append(df.format(sc.scTup[0])+"\t"+df.format(sc.scTup[1])+"\t"+df.format(st.mc)+"\r\n");
        }
        resultsFile.close();
        double mcavg = 0;
        for(int i=0;i<results.length;i++){
            for(int p=0;p<m_gb.getNumPlayers();p++){
                variance[p] += Math.pow(scMean.scTup[p]-results[i].st.scTup[p],2);
            }
            mcavg += results[i].mc;
        }
        mcavg /= rounds;
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

        System.out.println("Rounds:" + df.format(mcavg));

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

        experimentFramework(mctsex,kuhnAgent,"experiment3",1);
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

        ScoreTuple sc = myCompeteNPlayer(new PlayAgtVector(pavec), m_gb.m_so, iterations, 1, null).st;

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

    public String setOutput(String folder) throws FileNotFoundException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        String filename = directory+"/"+folder+"/"+dtf.format(now);
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
                actBest = paVector.pavec[player].getNextAction2(so.partialState(), false, false, nextMoveSilent);
                so.advance(actBest, null);

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

    class resultTuple {
        ScoreTuple st;
        double mc;
        resultTuple(ScoreTuple scoreTuple, double movecount){
            this.st  = scoreTuple;
            this.mc = movecount;
        }
    }



    public resultTuple myCompeteNPlayer(PlayAgtVector paVector, StateObservation startSO, int competeNum,
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
                actBest = paVector.pavec[player].getNextAction2(so.partialState(), false, false, nextMoveSilent);
                so.advance(actBest, null);

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
        resultTuple res = new resultTuple(scMean,moveCount);
        return res;
    }


    public resultTuple mySingleCompeteNPlayerAllRoles(PlayAgtVector paVector, StateObservation startSO, int competeNum,
                                                    int verbose) {
        int N = startSO.getNumPlayers();
        double sWeight = 1 / (double) N;
        ScoreTuple  shiftedTuple, scMean = new ScoreTuple(N);
        resultTuple rt;
        PlayAgtVector qaVector;
        double rounds = 0.0;
        for (int k = 0; k < N; k++) {
            qaVector = paVector.shift(k);
            // Play the Round

            rt = mySingleCompete(qaVector, startSO);

            shiftedTuple = rt.st.shift(N - k);
            scMean.combine(shiftedTuple, ScoreTuple.CombineOP.AVG, 0, sWeight);
            rounds += rt.mc/N;
        }
        return new resultTuple(scMean,rounds);
    }

    public resultTuple mySingleCompete(PlayAgtVector paVector, StateObservation startSO) {

        int numPlayers = paVector.getNumPlayers();
        ScoreTuple sc;
        double moveCount = 0.0;
        StateObservation so;
        Types.ACTIONS actBest;

        for (int i = 0; i < numPlayers; i++)
            paVector.pavec[i].resetAgent();

        int player = startSO.getPlayer();
        so = startSO.copy();

        while (true) {
            actBest = paVector.pavec[player].getNextAction2(so.partialState(), false, false, true);
            so.advance(actBest, null);

            if (so.isGameOver()) {
                sc = so.getGameScoreTuple();
                moveCount += so.getMoveCounter();
                break; // out of while
            }

            if(so.isRoundOver()&&!so.isGameOver()) {
                so.initRound();
                assert !so.isRoundOver() : "Error: initRound() did not reset round-over-flag";
            }

            player = so.getPlayer();
        } // while(true)

        resultTuple res = new resultTuple(sc,moveCount);
        return res;
    }
}
