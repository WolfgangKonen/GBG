package games.Poker;

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
import games.KuhnPoker.*;
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

    private final String directory = "PokerExperiments";

    GameBoardPoker m_gb;
    ArenaPoker m_arena;
    DecimalFormat df = new DecimalFormat("###.###");

    private static final String TD_NTUPLE_3 = "TD-Ntuple-3";
    private static final String TD_NTUPLE_4 = "TD-Ntuple-4";
    private static final String SARSA = "Sarsa";
    private static final String SARSA_4 = "Sarsa-4";
    private static final String QLEARN_4 = "Qlearn-4";
    private static final String EXPECTIMAX = "Expectimax-N";
    private static final String MCTSE = "MCTS Expectimax";
    private static final String POKER = "PokerAgent";
    private static final String KUHN = "Kuhn";
    private static final String RANDOM = "Random";
    private static final String MC = "MC-N";
    private static final String TDS = "TDS";

    public static void main(String[] args) throws Exception {
        Experiment ex = new Experiment();
        //ex.oneRoundChallenge(POKER,RANDOM, "AlphaPoker");
        ex.oneRoundChallenge(TD_NTUPLE_3,RANDOM, "AlphaPoker",1000);
        //ex.oneRoundChallenge(POKER,RANDOM, "AlphaPoker");
        //ex.oneRoundChallenge(MCTSE, RANDOM, "AlphaPoker");
        //ex.oneRoundChallenge(MC, RANDOM, "AlphaPoker");
        //ex.oneRoundChallenge(TDS, RANDOM, "AlphaPoker");
    }

    public Experiment(){
        tools.Utils.checkAndCreateFolder(directory);
        //m_arena = new ArenaKuhnPoker("Kuhn",false);
        //m_gb = new GameBoardKuhnPoker(m_arena);
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

        GameBoardPoker gb = new GameBoardPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsPoker();

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
        GameBoardPoker gb = new GameBoardPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsPoker();

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
        GameBoardPoker gb = new GameBoardPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsPoker();

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
        GameBoardPoker gb = new GameBoardPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsPoker();

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

        GameBoardPoker gb = new GameBoardPoker();
        String savePath = dir;
        savePath+="/agent";

        XNTupleFuncs xnf = new XNTupleFuncsPoker();

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

        GameBoardPoker gb = new GameBoardPoker();
        String savePath = dir;
        savePath+="/agent";

        // define parameters
        ParTD tdPar = new ParTD();
        ParOther oPar = new ParOther();

        int maxGameNum = 10000000;
        int gameNum;

        int stopTest = oPar.getStopTest();
        int stopEval = oPar.getStopEval();
        int qem = oPar.getQuickEvalMode();
        int verbose = 1;
        int tem = oPar.getTrainEvalMode();

        // frequency how often to evaluate
        int numEval = 1000;

        // setup the feature to be used for TDS
        int featmode = 0;
        Feature feature = new FeaturePoker(featmode);

        PlayAgent tds = new TDAgent(agentName, tdPar, oPar, feature, maxGameNum);
        tds.setMaxGameNum(maxGameNum);
        tds.setNumEval(numEval);
        tds.setGameNum(0);

        tds = trainAgent(tds,gb,savePath);

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

    public PlayAgent setupPokerAgent(String dir) throws IOException {
        String agentName = POKER;
        PokerAgent pokerAgent = new PokerAgent(agentName);
        return pokerAgent;
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

    /**
     * To determine the quality of an agent it might not be the best way to playout a complete game.
     * The results are difficult to compare and it might make things easier to play only one round.
     */
    public void oneRoundChallenge(String p0, String p1, String experiment) throws Exception {
        oneRoundChallenge(p0,p1,experiment,1000000);
    }

    public void oneRoundChallenge(String p0, String p1, String experiment, int playRounds) throws Exception {

        // the experitment name will be the foldername in /experiments
        String experimentName = experiment;

        // number of rounds played for evaluation for each position
 //       int playRounds = 1000000;

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
        String line = "position\tcards\tcards_opponent\tcommunity_cards\tmoves\tresult\r\n";
        resultsFile.append(line);

        // Setup Agents
        switch (p0){
            case POKER -> observedAgent = setupPokerAgent(folder);
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
            case POKER -> benchmarkAgent = setupPokerAgent(folder);
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

        for(int i=0;i<numPlayers;i++){
            means[i] = new ScoreTuple(numPlayers);
        }

        double sWeight = 1.0/playRounds;
        ScoreTuple scStart;

        // define startSO
        StateObserverPoker startSO = new StateObserverPoker();

        //StateObservation so;
        StateObserverPoker so;

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
                so = new StateObserverPoker();

                while (true) {
                    long startTNano = System.nanoTime();
                    actBest = qaVector.pavec[player].getNextAction2(so.partialState(), false, false, true);

                    so.advance(actBest, null);
                    if (so.isRoundOver()) {
                        sc = so.getGameScoreTuple();
                        // calculate "reward" as score change for the turn
                        sc.combine(scStart, ScoreTuple.CombineOP.DIFF, observedPlayer, 0);
                        means[z].combine(sc, ScoreTuple.CombineOP.AVG, observedPlayer, sWeight);

                        // Information:
                            // Position
                            // P1 : Hand
                            // P2 : Hand
                            // Aktionen SO:LastMoves
                            // Result
                        line = "";

                        line += "" + observedPlayer;
                        line += "\t" + so.getHoleCards(observedPlayer)[0].toString()+"/"+so.getHoleCards(observedPlayer)[1].toString();
                        line += "\t" + so.getHoleCards((observedPlayer+1)%2)[0].toString()+"/"+so.getHoleCards((observedPlayer+1)%2)[1].toString();
                        line += "\t";
                        PlayingCard[] cards = so.getCommunityCards();
                        for(int c = 0;c<cards.length;c++){
                            if(cards[c]!=null) {
                                if(c>0)
                                    line+="/";
                                line += cards[c].toString();
                            }
                        }
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
        }


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
        StateObserverPoker stateTest = new StateObserverPoker();

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
        StateObserverPoker startSO;
        startSO = stateTest;

        //StateObservation so;
        StateObserverPoker so;

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
                so = new StateObserverPoker();

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


}
