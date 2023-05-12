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
import games.GameBoard;
import games.KuhnPoker.KuhnPokerAgent;
import games.StateObservation;
import org.json.JSONArray;
import org.json.JSONObject;
import params.*;
import tools.ScoreTuple;
import tools.Types;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ExperimentNew {

    private final String directory = "PokerExperimentsNew";

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
    private static final String LOAD = "LOAD";

    private static final boolean runRAnalysis = false;
    private static final String rScriptLocation = "C:\\Program Files\\R\\R-4.0.4\\bin\\rscript.exe";
    private static final String rAnalysisLocation = "C:\\Program Files\\R\\R-4.0.4\\bin\\rscript.exe";

    private boolean writeDownResults = true;
    private boolean saveResults = false;
    private int writeDownSteps = 5000;

    private XNTuplePoker xnTuple;
    private FeaturePoker feature;

    private boolean verbose;

    private int numberOfTrainingGames;
    private int numberOfBenchmarkGames;

    // Parameters
    private ParNT ntPar;
    private ParTD tdPar;
    private ParOther oPar;
    private ParMCTSE mctsePar;
    private ParMC mcPar;
    private ParRB rbPar;
    private ParWrapper wrPar;
    DateTimeFormatter dtf;

    public static void main(String[] args) throws Exception {
        ExperimentNew ex = new ExperimentNew();
        ex.doesTrainingYieldSameResults(SARSA_4);
    }

    public ExperimentNew(){
        // Housekeeping
        dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

        ntPar = new ParNT();
        oPar = new ParOther();
        // ReplayBuffer
        rbPar = new ParRB();
        wrPar = new ParWrapper();
        // DEFAULT TD
        tdPar = new ParTD();
        tdPar.setNormalize(true);
        tdPar.setLinearNet(false);

        // DEFAULT MCTSE
        mctsePar = new ParMCTSE();
        mctsePar.setNumIter(5000);
        mctsePar.setRolloutDepth(10);

        // DEFAULT MC
        mcPar = new ParMC();
        mcPar.setIterations(5000);
        mcPar.setRolloutDepth(10);

        // DEFAULT FEATURE
        int featmode = 0;
        feature = new FeaturePoker(featmode);

        // DEFAULT XNTUPLE
        xnTuple = new XNTupleFuncsPokerSummary();

        // Training Games
        numberOfTrainingGames = 4000000;
        numberOfBenchmarkGames = 4000000;

    }

    private void setSaveResults(boolean x){
        saveResults = x;
    }

    private void setWriteDownResults(boolean x){
        writeDownResults = x;
    }

    private void setWriteDownSteps(int x){
        writeDownSteps = x;
    }

    private ParNT getParNT(){
        return ntPar;
    }

    private ParTD getParTD(){
        return tdPar;
    }

    private ParOther getParOther(){
        return oPar;
    }

    public XNTuplePoker getXNTupleFunc(){
        return xnTuple;
    }

    public void setXnTupleFunc(XNTuplePoker func){
        xnTuple = func;
    }

    public FeaturePoker getFeaturePoker() { return feature; }

    public void setFeaturePoker(FeaturePoker f){
        feature = f;
    }

    public void findMostPromisingAgent() throws Exception {

        LocalDateTime startTime = LocalDateTime.now();

        // define and create new directory for the experiment
        String experimentDir = directory + "/mostPromisingAgent/" + dtf.format(startTime) + "/";
        tools.Utils.checkAndCreateFolder(experimentDir);

        // define resultfile and structure
        Writer resultsFile = new FileWriter(experimentDir + "/results.csv");
        String resultLine = "Agent\tXNTupleFunc\tp0\tp1\r\n";
        resultsFile.append(resultLine);

        String[] agents = {SARSA_4, QLEARN_4,TD_NTUPLE_4,TD_NTUPLE_3}; //SARSA

        XNTuplePoker[] funcs = {new XNTupleFuncsPokerSimple(),
                new XNTupleFuncsPokerAbstract(),
                new XNTupleFuncsPokerSimpleRanks(),
                new XNTupleFuncsPokerSummary()
        };

        PlayAgent benchmarkAgent = setupAgent(POKER);
        PlayAgent observedAgent;

        ScoreTuple[] scores = new ScoreTuple[0];

        for(String agent:agents) {
            for(XNTuplePoker xnTuple : funcs){
                setXnTupleFunc(xnTuple);
                observedAgent = setupAgent(agent);

                System.out.println(LocalDateTime.now()+": starting: "+ observedAgent.getName() +"-"+ xnTuple.getName());

                // train agent
                observedAgent = trainAgent(observedAgent, numberOfTrainingGames);

                // evaluate performance
                scores = evaluateAgent(observedAgent,benchmarkAgent,numberOfBenchmarkGames);

                resultLine = agent + "\t" +
                        xnTuple.getName() + "\t" +
                        scores[0].scTup[0] + "\t" +
                        scores[1].scTup[1] + "\r\n";

                resultsFile.append(resultLine);
            }
        }
        LocalDateTime endTime = LocalDateTime.now();

        resultsFile.close();

        //region Save Meta-Data
        JSONObject experimentOverview = new JSONObject();
        experimentOverview.put("startTime",dtf.format(startTime));
        experimentOverview.put("endTime",dtf.format(endTime));
        experimentOverview.put("totalTime",ChronoUnit.MILLIS.between(startTime,endTime));
        experimentOverview.put("numberOfTrainingGames",numberOfTrainingGames);
        experimentOverview.put("numberOfBenchmarkGames",numberOfBenchmarkGames);
        Writer infoFile = new FileWriter(experimentDir+"/info.json");
        infoFile.write(experimentOverview.toString());
        infoFile.close();
        //endregion

    }

    public void doesTrainingYieldSameResults(String agent) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();

        // define and create new directory for the experiment
        String experimentDir = directory + "/doesTrainingYieldSameResults/" + agent + "/" + dtf.format(startTime) ;
        tools.Utils.checkAndCreateFolder(experimentDir);

        // define resultfile and structure
        Writer resultsFile = new FileWriter(experimentDir + "/results.csv");
        String resultLine = "trainedGames\tbenchmarkGames\tp0\tp1\r\n";
        resultsFile.append(resultLine);


        ScoreTuple[] scores = new ScoreTuple[0];


        numberOfTrainingGames = 4000000;
        int numberOfRuns = 100;

        double avgTime = 0;

        // setup agents
        PlayAgent observedAgent = setupAgent(agent);
        PlayAgent benchmarkAgent = setupAgent(POKER);

        if(!observedAgent.isTrainable())
            throw new RuntimeException("Agent: '"+agent+"' can't be trained");

        LocalDateTime now;
        now = LocalDateTime.now();
        for(int i = 0;i<numberOfRuns;i++) {

            observedAgent = setupAgent(agent);
            // train till the number of training games is reached
            while (observedAgent.getGameNum() < numberOfTrainingGames) {

                System.out.println(LocalDateTime.now() + ": Start training for - " + numberOfTrainingGames + " rounds.");

                // train agent
                observedAgent = trainAgent(observedAgent, numberOfTrainingGames);

                System.out.println(LocalDateTime.now() + ": Start evaluating for - " + numberOfBenchmarkGames + " rounds.");

                // evaluate performance
                scores = evaluateAgent(observedAgent, benchmarkAgent, numberOfBenchmarkGames);

                resultLine = observedAgent.getGameNum() + "\t" +
                        numberOfBenchmarkGames + "\t" +
                        scores[0].scTup[0] + "\t" +
                        scores[1].scTup[1] + "\r\n";

                resultsFile.append(resultLine);

                avgTime = avgTime / (i + 1) * (i) + ChronoUnit.MILLIS.between(now, LocalDateTime.now()) / (i + 1);
                System.out.println(LocalDateTime.now() + ": just finished training round - " + i + " of "+numberOfRuns+
                        " expected time left: "+getTimeDifferrence((long)(avgTime/i*(numberOfRuns-i)))+ " ("+avgTime*1.0/1000 +"s per iteration)");
                now = LocalDateTime.now();
            }
        }
        LocalDateTime endTime = LocalDateTime.now();

        resultsFile.close();

        //region Save Meta-Data
        JSONObject experimentOverview = new JSONObject();
        experimentOverview.put("startTime",dtf.format(startTime));
        experimentOverview.put("endTime",dtf.format(endTime));

        JSONArray players = new JSONArray();

        JSONObject player = writeAgentToJson(observedAgent);
        player.put("position",0);
        players.put(player);

        player = writeAgentToJson(benchmarkAgent);
        player.put("position",1);
        players.put(player);
        experimentOverview.put("players",players);

        experimentOverview.put("totalTime",ChronoUnit.MILLIS.between(startTime,endTime));
        experimentOverview.put("benchmarkRounds",numberOfBenchmarkGames);
        experimentOverview.put("lastResultP0",scores[0].scTup[0]);
        experimentOverview.put("lastResultP1",scores[1].scTup[1]);

        Writer infoFile = new FileWriter(experimentDir+"/info.json");
        infoFile.write(experimentOverview.toString());
        infoFile.close();
        //endregion
    }

    public void howManyGamesToTrain(String agent) throws Exception {

        LocalDateTime startTime = LocalDateTime.now();

        // define and create new directory for the experiment
        String experimentDir = directory + "/howManyGamesToTrain/" + agent + "/" + dtf.format(startTime) ;
        tools.Utils.checkAndCreateFolder(experimentDir);

        // define resultfile and structure
        Writer resultsFile = new FileWriter(experimentDir + "/results.csv");
        String resultLine = "trainedGames\tbenchmarkGames\tp0\tp1\r\n";
        resultsFile.append(resultLine);

        // setup agents
        PlayAgent observedAgent = setupAgent(agent);
        PlayAgent benchmarkAgent = setupAgent(POKER);

        //
        int[] evaluation = {0,
                25000,
                50000,
                75000,
                100000,
                200000,
                500000,
                1000000,
                2000000,
                4000000,
                6000000,
                10000000,
                20000000,
                30000000,
                40000000};


        int eval = 0;
        int numberOfTrainingsTillEvaluation;
        ScoreTuple[] scores = new ScoreTuple[0];

        if(!observedAgent.isTrainable())
            throw new RuntimeException("Agent: '"+agent+"' can't be trained");

        numberOfTrainingGames = 40000000;

        // train till the number of training games is reached
        while(observedAgent.getGameNum() < numberOfTrainingGames) {
            numberOfTrainingsTillEvaluation = eval<evaluation.length?evaluation[eval++]-observedAgent.getGameNum():numberOfTrainingGames-observedAgent.getGameNum();


            System.out.println(LocalDateTime.now()+": Start training for - "+numberOfTrainingsTillEvaluation+" rounds.");

                    // train agent
            observedAgent = trainAgent(observedAgent, numberOfTrainingsTillEvaluation);


            System.out.println(LocalDateTime.now()+": Start evaluating for - "+numberOfBenchmarkGames+" rounds.");
            // evaluate performance
            scores = evaluateAgent(observedAgent,benchmarkAgent,numberOfBenchmarkGames);

            resultLine = observedAgent.getGameNum() + "\t" +
                    numberOfBenchmarkGames + "\t" +
                    scores[0].scTup[0] + "\t" +
                    scores[1].scTup[1] + "\r\n";

            resultsFile.append(resultLine);
        }
        LocalDateTime endTime = LocalDateTime.now();

        resultsFile.close();

        //region Save Meta-Data
        JSONObject experimentOverview = new JSONObject();
        experimentOverview.put("startTime",dtf.format(startTime));
        experimentOverview.put("endTime",dtf.format(endTime));

        JSONArray players = new JSONArray();

        JSONObject player = writeAgentToJson(observedAgent);
        player.put("position",0);
        players.put(player);

        player = writeAgentToJson(benchmarkAgent);
        player.put("position",1);
        players.put(player);
        experimentOverview.put("players",players);

        experimentOverview.put("totalTime",ChronoUnit.MILLIS.between(startTime,endTime));
        experimentOverview.put("benchmarkRounds",numberOfBenchmarkGames);
        experimentOverview.put("lastResultP0",scores[0].scTup[0]);
        experimentOverview.put("lastResultP1",scores[1].scTup[1]);

        Writer infoFile = new FileWriter(experimentDir+"/info.json");
        infoFile.write(experimentOverview.toString());
        infoFile.close();
        //endregion
    }

    //region Setup of Agents
    private PlayAgent setupAgent(String AgentName) throws Exception {
        if(AgentName == LOAD)
            throw new RuntimeException("Can't load an agent without a filepath.");
        return setupAgent(AgentName, null);
    }

    private PlayAgent setupAgent(String AgentName, String loadPath) throws Exception {
        PlayAgent agent;
        switch (AgentName){
            case POKER -> agent = setupPokerAgent();
            case RANDOM -> agent =  setupRandomAgent();
            case TD_NTUPLE_3 -> agent =  setupTDNTuple3Agent();
            case TD_NTUPLE_4 -> agent =  setupTDNTuple4Agent();
            case SARSA -> agent =  setupSarsaAgent();
            case SARSA_4 -> agent =  setupSarsa4Agent();
            case QLEARN_4 -> agent =  setupQLearn4Agent();
            case EXPECTIMAX -> agent =  setupExpectiMax();
            case MCTSE -> agent =  setupMCTSAgent();
            case MC -> agent =  setupMCNAgent();
            case TDS -> agent =  setupTDSAgent();
            case LOAD -> agent =  setupLoadAgent(loadPath);
            default -> throw new Exception("not supported agent for p0");
        }
        return agent;
    }

    private PlayAgent setupPokerAgent() {
        String agentName = POKER;
        PokerAgent pokerAgent = new PokerAgent(agentName);
        return pokerAgent;
    }

    private PlayAgent setupRandomAgent() throws IOException {
        String agentName = RANDOM;
        RandomAgent ra = new RandomAgent(agentName);
        return ra;
    }

    private PlayAgent setupExpectiMax(){
        String agentName = EXPECTIMAX;
        ExpectimaxNAgent expecti = new ExpectimaxNAgent(agentName);
        return expecti;
    }

    private PlayAgent setupLoadAgent(String filepath) throws IOException {
        LoadSaveGBG tdAgentIO = new LoadSaveGBG(directory, null);
        PlayAgent pa = tdAgentIO.loadGBGAgent(filepath);
        return pa;
    }

    private PlayAgent setupTDNTuple3Agent() throws Exception {
        String agentName = TD_NTUPLE_3;
        NTupleFactory ntupfac = new NTupleFactory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnTuple);
        PlayAgent pa = new TDNTuple3Agt(agentName, tdPar, ntPar, oPar, nTuples, xnTuple, numberOfTrainingGames);
        return pa;
    }

    private PlayAgent setupTDNTuple4Agent() throws Exception {
        String agentName = TD_NTUPLE_4;

        NTuple4Factory ntupfac = new NTuple4Factory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnTuple);
        PlayAgent pa = new TDNTuple4Agt(agentName,tdPar,ntPar, oPar, rbPar, wrPar, nTuples, xnTuple, numberOfTrainingGames);

        // return agent
        return pa;
    }

    private PlayAgent setupSarsaAgent() throws Exception {
        String agentName = SARSA;
        GameBoardPoker gb = new GameBoardPoker();
        NTupleFactory ntupfac = new NTupleFactory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnTuple);
        ArrayList<Types.ACTIONS> allAvailActions = gb.getDefaultStartState(null).getAllAvailableActions();
        PlayAgent pa = new SarsaAgt(agentName, tdPar, ntPar,
                oPar, nTuples, xnTuple, allAvailActions, numberOfTrainingGames);

        return pa;
    }

    private PlayAgent setupSarsa4Agent() throws Exception {
        String agentName = SARSA_4;
        GameBoardPoker gb = new GameBoardPoker();
        NTuple4Factory ntupfac = new NTuple4Factory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnTuple);
        ArrayList<Types.ACTIONS> allAvailActions = gb.getDefaultStartState(null).getAllAvailableActions();
        PlayAgent pa = new Sarsa4Agt(agentName, tdPar, ntPar,
                oPar, nTuples, xnTuple, allAvailActions, numberOfTrainingGames);

        // return agent
        return pa;
    }

    private PlayAgent setupQLearn4Agent() throws Exception {
        String agentName = QLEARN_4;

        GameBoardPoker gb = new GameBoardPoker();
        NTuple4Factory ntupfac = new NTuple4Factory();
        int[][] nTuples = ntupfac.makeNTupleSet(ntPar, xnTuple);
        ArrayList<Types.ACTIONS> allAvailActions = gb.getDefaultStartState(null).getAllAvailableActions();
        PlayAgent pa = new QLearn4Agt(agentName, tdPar, ntPar,
                oPar, nTuples, xnTuple, allAvailActions, numberOfTrainingGames);

        // return agent
        return pa;
    }

    private PlayAgent setupTDSAgent() throws IOException {
        String agentName = TDS;
        PlayAgent tds = new TDAgent(agentName, tdPar, oPar, feature, numberOfTrainingGames);
        tds.setMaxGameNum(numberOfTrainingGames);
        tds.setGameNum(0);
        return tds;
    }

    private PlayAgent setupMCNAgent() {
        String agentName = MC;
        MCAgentN mc = new MCAgentN(agentName,mcPar,oPar);
        return mc;
    }

    public PlayAgent setupMCTSAgent(){
        String agentName = MCTSE;
        MCTSExpectimaxAgt mctse = new MCTSExpectimaxAgt(agentName,mctsePar);
        return mctse;
    }

    //endregion

    public PlayAgent trainAgent(PlayAgent pa, int trainingGames) throws IOException {

        GameBoard gb = new GameBoardPoker();
        gb.initialize();

        double avgTime = 0;
        int steps = 500000;
        int est;
        LocalDateTime now;
        now = LocalDateTime.now();

        est = 0;
        for(int i = 1 ; i < trainingGames+1 ; i++) {
            StateObservation so;
            if( pa.getParOther().getChooseStart01()){
                so = gb.chooseStartState();
            }else{
                so = gb.getDefaultStartState(null);
            }
            pa.trainAgent(so);

            if(i%steps==0) {
                avgTime = avgTime / (est + 1) * (est) + ChronoUnit.MILLIS.between(now, LocalDateTime.now()) / (est + 1);
                est++;
                System.out.println(LocalDateTime.now() + ": just finished training round - " + i + " of "+trainingGames+
                        " expected time left: "+getTimeDifferrence((long)(avgTime/steps*(trainingGames-i)))+ " ("+avgTime*1.0/1000 +"s per iteration ["+steps+"])");
                now = LocalDateTime.now();
            }
        }

        // return agent
        return pa;
    }

    public ScoreTuple[] evaluateAgent(PlayAgent p0,PlayAgent p1, int numberOfGames){

        PlayAgent[] pavec = new PlayAgent[] {p0,p1};
        PlayAgtVector paVector = new PlayAgtVector(pavec);

        int numPlayers = paVector.getNumPlayers();
        ScoreTuple[] means = new ScoreTuple[numPlayers];

        for(int i=0;i<numPlayers;i++){
            means[i] = new ScoreTuple(numPlayers);
        }

        //StateObservation so;
        StateObserverPoker so;

        ScoreTuple sc;
        Types.ACTIONS actBest;

        int observedPlayer;

        PlayAgtVector qaVector;
        int player;

        double sWeight = 1.0/numberOfGames;


        double avgTime = 0;
        int steps = 500000;
        int est;

        LocalDateTime now;
        now = LocalDateTime.now();
        for(int z = 0;z<numPlayers;z++){

            qaVector = paVector.shift(z);
            observedPlayer = z;
            est = 0;

            for (int k = 1; k < numberOfGames+1; k++) {

                for (int i = 0; i < numPlayers; i++)
                    qaVector.pavec[i].resetAgent();

                so = new StateObserverPoker();
                player = so.getPlayer();

                while (true) {
                    actBest = qaVector.pavec[player].getNextAction2(so.partialState(), false, true);
                    so.advance(actBest, null);
                    if (so.isRoundOver()) {
                        sc = so.getGameScoreTuple();

                        means[z].combine(sc, ScoreTuple.CombineOP.AVG, observedPlayer, sWeight);

                        break;
                    }
                    player = so.getPlayer();
                } // while(true)

                if(k%steps==0) {
                    avgTime = avgTime / (est + 1) * (est) + ChronoUnit.MILLIS.between(now, LocalDateTime.now()) / (est + 1);
                    est++;
                    System.out.println(LocalDateTime.now() + ": just finished round - " + k + " of "+numberOfGames+" ("+(z+1)+"/"+numPlayers+")" +
                            " expected time left: "+getTimeDifferrence((long)(avgTime/steps*((-1*z+2)*numberOfGames-k)))+ " ("+avgTime*1.0/1000 +"s per iteration ["+steps+"])");
                    now = LocalDateTime.now();
                }
            } // for (k)

        }

        return means;
    }

    private String getTimeDifferrence(long time){
        long h = time/1000/60/60;
        long timeGuide = time - h * 1000 * 60 * 60;
        long m = timeGuide /1000/60 ;
        timeGuide = timeGuide - m * 1000 * 60;
        long s = timeGuide/1000;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    //region JSON
    public JSONObject writeAgentToJson(PlayAgent pa){
        JSONObject player = new JSONObject();
        player.put("name",pa.getName());

        JSONArray pars = new JSONArray();
        pars.put(parOtherToJSON(pa.getParOther()));

        String agentName = pa.getName();
        switch (agentName){
            case TD_NTUPLE_3:
                pars.put(parNTToJSON(((TDNTuple3Agt)pa).getParNT()));
                pars.put(parTDToJSON(((TDNTuple3Agt)pa).getParTD()));
                JSONObject tdnt3 = new JSONObject();
                tdnt3.put("maxGameNum",((TDNTuple3Agt)pa).getMaxGameNum());
                tdnt3.put("Feature",getXNTupleFunc().getClass().getSimpleName());
                tdnt3.put("Feature Description",getXNTupleFunc().getDescription());
                pars.put(tdnt3);
                break;
            case TD_NTUPLE_4:
                pars.put(parNTToJSON(((TDNTuple4Agt)pa).getParNT()));
                pars.put(parTDToJSON(((TDNTuple4Agt)pa).getParTD()));
                JSONObject tdnt4 = new JSONObject();
                tdnt4.put("maxGameNum",((TDNTuple4Agt)pa).getMaxGameNum());
                tdnt4.put("Feature",getXNTupleFunc().getClass().getSimpleName());
                tdnt4.put("Feature Description",getXNTupleFunc().getDescription());

                pars.put(tdnt4);
                break;
            case SARSA:
                pars.put(parNTToJSON(((SarsaAgt)pa).getParNT()));
                pars.put(parTDToJSON(((SarsaAgt)pa).getParTD()));
                JSONObject sarsa = new JSONObject();
                sarsa.put("maxGameNum",((SarsaAgt)pa).getMaxGameNum());
                sarsa.put("Feature",getXNTupleFunc().getClass().getSimpleName());
                sarsa.put("Feature Description",getXNTupleFunc().getDescription());
                pars.put(sarsa);
                break;
            case SARSA_4:
                pars.put(parNTToJSON(((Sarsa4Agt)pa).getParNT()));
                pars.put(parTDToJSON(((Sarsa4Agt)pa).getParTD()));
                JSONObject sarsa4 = new JSONObject();
                sarsa4.put("maxGameNum",((Sarsa4Agt)pa).getMaxGameNum());
                sarsa4.put("Feature",getXNTupleFunc().getClass().getSimpleName());
                sarsa4.put("Feature Description",getXNTupleFunc().getDescription());
                pars.put(sarsa4);
                break;
            case QLEARN_4:
                pars.put(parNTToJSON(((QLearn4Agt)pa).getParNT()));
                pars.put(parTDToJSON(((QLearn4Agt)pa).getParTD()));
                JSONObject qlearn4 = new JSONObject();
                qlearn4.put("maxGameNum",((QLearn4Agt)pa).getMaxGameNum());
                qlearn4.put("Feature",getXNTupleFunc().getClass().getSimpleName());
                qlearn4.put("Feature Description",getXNTupleFunc().getDescription());
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
    //endregion

    public void howManyGamesForMCN() throws Exception {
        LocalDateTime startTime = LocalDateTime.now();

        // define and create new directory for the experiment
        String experimentDir = directory + "/howManyGamesForMCN/" + dtf.format(startTime) + "/";
        tools.Utils.checkAndCreateFolder(experimentDir);

        // define resultfile and structure
        Writer resultsFile = new FileWriter(experimentDir + "/results.csv");
        String resultLine = "mcIterations\tgames\ttime\tp0\tp1\r\n";
        resultsFile.append(resultLine);


        int toEvaluate = 100000;
        int steps = toEvaluate/4;

        mcPar.setIterations(1000);

        PlayAgent benchmarkAgent = setupAgent(POKER);
        PlayAgent observedAgent = setupAgent(MC);

        double avgTime = 0;
        double avgScoreP0 = 0;
        double avgScoreP1 = 0;
        ScoreTuple[] scores = new ScoreTuple[0];

        LocalDateTime now = LocalDateTime.now();
        for(int i = 0 ; i < toEvaluate ; i+=steps){
            scores = evaluateAgent(observedAgent,benchmarkAgent,steps);
            avgTime = avgTime / (i + 1) * (i) + ChronoUnit.MILLIS.between(now, LocalDateTime.now()) / (i + 1);
            avgScoreP0 = avgScoreP0 / (i + 1) * (i) + scores[0].scTup[0] / (i + 1);
            avgScoreP1 = avgScoreP1 / (i + 1) * (i) + scores[1].scTup[1] / (i + 1);

            resultLine = mcPar.getNumIter() + "\t" +
                    steps*(i+1) + "\t" +
                    avgScoreP0 + "\t" +
                    avgScoreP1 + "\r\n";

            resultsFile.append(resultLine);
        }

        resultsFile.close();

    }

}
