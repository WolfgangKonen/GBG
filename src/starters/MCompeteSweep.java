package starters;

import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import games.*;
import games.Othello.Edax.Edax2;
import games.RubiksCube.StateObserverCube;
import params.ParEdax;
import params.ParOther;
import tools.Measure;
import tools.ScoreTuple;
import tools.Types;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

/**
 *  This class contains several Othello sweep routines (multi-training and multi-competition) that are called from
 *  {@link GBGBatch}.
 *  <p>
 *  When starting {@link SingleCompetitor#doSingleCompetition(int, PlayAgent, String, int, Arena, GameBoard, double, String)
 *  SingleCompetitor.doSingleCompetition}, an object {@code ArrayList<MCompete> mcList} is created and
 *  finally written with {@link MCompeteMWrap#printMultiCompeteList(String, ArrayList, PlayAgent, Arena, String, String)
 *  MCompeteMWrap.printMultiCompeteList} to file <b>{@code agents/<gameDir>/csv/<csvName>}</b> (usually {@code multiCompete.csv}).
 *
 * @see MCompeteMWrap
 * @see GBGBatch
 */
public class MCompeteSweep {
    protected Evaluator m_evaluatorQ = null;
    protected Evaluator m_evaluatorT = null;

    public MCompeteSweep() { }

    /**
     * Perform Othello multi-competition with MCTSWrapperAgent wrapped around agent {@code pa} vs. Edax2 with
     * different depth levels. The Edax depth values to sweep are coded in array {@code depthArr} in this method.
     * The wrapped agent plays both roles (parameter {@code p_MWrap = 0,1}).
     * <p>
     * Side effect: writes results of multi-competition to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code run, competeNum, dEdax, iterMWrap, EPS, p_MWrap, c_puct, winrate, time, userValue2}. <br>
     * The contents may be visualized with one of the R-scripts found in {@code resources\R_plotTools}.
     * <p>
     * NOTE: This method can only be run under Windows, since edax.exe is a Windows executable.
     *
     * @param pa		    agent to wrap
     * @param iterMWrap	    number of MCTS wrapper iterations
     * @param nruns	        number of runs, each run prints a line to csv. Should be 1 for deterministic agent {@code pa}
     * @param arenaTrain    Arena object with train rights
     * @param gb		    the game board, needed for start state selection
     * @param csvName	    results are written to this filename
     * @return the wrapped agent
     */
    public PlayAgent multiCompeteOthello(PlayAgent pa, int iterMWrap, int nruns, Arena arenaTrain,
                                         GameBoard gb, String csvName) {
        SingleCompetitor sCompetitor = new SingleCompetitor();
        PlayAgent qa=null;

        for (int i=0; i<nruns; i++) {
            // competition of trained (and possibly wrapped) agent against Edax
            qa = sCompetitor.doSingleCompetition(i, pa, pa.getName(), iterMWrap, arenaTrain, gb, 0.0, csvName);
        } // for (i)

        System.out.println("[multiCompeteSweepOthello] "+sCompetitor.getElapsedTime()+" sec.");
        return qa;
    } // multiCompeteOthello

    /**
     * Same as {@link #multiCompeteOthello(PlayAgent, int, int, Arena, GameBoard, String) multiCompeteOthello}, but
     * instead of a single agent {@code pa} we sweep over all agents that we find in directory
     * <b>{@code agents/Othello/<agtDir>}</b>.
     * <p>
     * Perform Othello multi-competition for all agents found in directory {@code agents/Othello/<agtDir>}, first with
     * the base agent, then wrapped by MCTSWrapperAgent with {@code iterMWrap} iterations. In both cases, compete
     * in both roles (parameter {@code p_MWrap = 0,1}) against Edax at different Edax depth levels.
     *  The Edax depth levels are coded in array {@code depthArr} in this method.
     * <p>
     * Side effect: writes results of multi-competition to <b>{@code agents/Othello/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code agtFile, run, competeNum, dEdax, iterMWrap, EPS, p_MWrap, c_puct, winrate, time, userValue2}. <br>
     * The contents may be visualized with R-scripts found in {@code resources\R_plotTools}.
     * <p>
     * NOTE: This method can only be run under Windows, since edax.exe is a Windows executable.
     *
     * @param iterMWrap	    number of MCTS wrapper iterations
     * @param agtDir        the directory where to search for agent files
     * @param arenaTrain    Arena object with train rights
     * @param gb		    the game board, needed for start state selection
     * @param csvName	    results are written to this filename
     * @return the last wrapped agent
     */
    public PlayAgent multiCompeteSweepOthello(int iterMWrap, String agtDir, Arena arenaTrain,
                                              GameBoard gb, String csvName) {
        SingleCompetitor sCompetitor = new SingleCompetitor();
        PlayAgent pa,qa=null;

        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        File directoryPath = new File(strDir+"/" + agtDir);
        String[] contents = directoryPath.list();   // an array with .agt.zip and .csv filenames
        assert Objects.requireNonNull(contents).length>0 : "Directory "+directoryPath +" is empty!";

        for (int i=0,i2=0; i<contents.length; i++) {
            if (!contents[i].split("\\.")[1].equals("csv")) {
                pa = arenaTrain.loadAgent(agtDir+"/" + contents[i]);
                // competition of trained (and possibly wrapped) agent against Edax
                qa = sCompetitor.doSingleCompetition(i2, pa, contents[i], iterMWrap, arenaTrain, gb, 0.0, csvName);
                i2++;   // increment only if it was not a .csv file
            }
        } // for (i)

        System.out.println("[multiCompeteSweepOthello] "+sCompetitor.getElapsedTime()+" sec.");
        return qa;
    } // multiCompeteSweepOthello

    /**
     * Perform Othello multi-training. In each run, agent {@code pa} is constructed anew (to get different random tuples)
     * and then trained.
     * <p>
     * Side effect: writes results to directory {@code agents/Othello/multiTrain/}: <ul>
     *     <li> agent files {@code <agtBase>_<i>.agt.zip} where {@code i} it the number of the run
     *     <li> train csv file {@code <agtBase>.csv}
     * </ul>
     * The train csv file may be visualized with R-scripts found in {@code resources\R_plotTools}.
     * <p>
     * Why do we separate training and competition in different batch runs? - Training can be done on all kind of JVMs
     * (including Unix machines), but competition against Edax can only be done on Windows-based JVMs, because Edax
     * in GBG comes (currently) only in the form of a Windows program ({@code .exe}).
     * <p>
     * <strong>NEW</strong>: If {@code batchSizeArr == null} then do {@code nruns} training runs with agent {@code pa}.<br>
     * If {@code != null} then sweep replay buffer's parameter {@code batchSize} over all values given in {@code batchSizeArr}
     * and do {@code nruns} training runs for each value.
     *
     * @param pa		    loaded agent (maybe stub) from which all training params are inherited
     * @param agtFile       agent filename, we use its {@code agtBase} (part w/o ".agt.zip") to form the new filenames
     * @param maxGameNum    number of training episodes in each run
     * @param nruns	        number of training runs
     * @param arenaTrain    Arena object with train rights
     * @param gb		    the game board, needed for start state selection
     * @param batchSizeArr	either null or the RB batch size values to sweep over
     * @return the last trained agent
     */
    public PlayAgent multiTrainSweepOthello(
            PlayAgent pa, String agtFile, int maxGameNum, int nruns,
            Arena arenaTrain, GameBoard gb, int[] batchSizeArr)
    {
        String userTitle1 = "time";
        DecimalFormat frm = new DecimalFormat("00");
        double userValue1=0.0, userValue2=0.0;
        long startTime;
        double elapsedTime=0,deltaTime;

        SingleTrainer sTrainer = new SingleTrainer();

        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        tools.Utils.checkAndCreateFolder(strDir+"/multiTrain");
        String agtBase = agtFile.split("\\.")[0];

        int[] sweepArr = (batchSizeArr==null) ? new int[]{0} : batchSizeArr;
        String fCsvName="";
        String userTitle2 = (batchSizeArr==null) ? "user2" : "RB_batch";
        String trainCsvName = "../multiTrain/" + agtBase + ".csv";
        // (we use "../" because we do not want to store in subdir "csv/" as printMultiTrainList usually does)

        for (int s : sweepArr) {
            if (s==0) {
                arenaTrain.m_xab.rbPar[0].setUseRB(false);
            } else {
                arenaTrain.m_xab.rbPar[0].setUseRB(true);
                arenaTrain.m_xab.rbPar[0].setBatchSize(s);
            }
            userValue2 = s;

            for (int i=0; i<nruns; i++) {

                startTime = System.currentTimeMillis();

                // train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results during train)
                pa = sTrainer.doSingleTraining(0, i, pa, arenaTrain, arenaTrain.m_xab, gb, maxGameNum, userValue1, userValue2);
                arenaTrain.m_xab.setOParFrom(0,pa.getParOther());  // /WK/ Bug fix 2022-04-12

                // save pa in a yet unused filename. This for multiple concurrent jobs which should not write to a
                // filename already written by another job. For single-threaded jobs (and no similar files present in
                // dir multiTrain/), k=0 will be used.
                int k=-1;
                String agtPath;
                File file;
                do {
                    k++;
                    agtPath = strDir + "/multiTrain/" + agtBase + "_" + frm.format(i+k) + ".agt.zip";
                    file = new File(agtPath);
                } while (file.exists());
                arenaTrain.saveAgent(pa,agtPath);

                // print the full list mtList after finishing training run i
                fCsvName=MTrain.printMultiTrainList(trainCsvName, sTrainer.mtList, pa, arenaTrain, userTitle1, userTitle2);

                deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                elapsedTime += deltaTime;
            }

        }
        if (fCsvName!=null) System.out.println("[multiTrainSweepOthello] Results saved to "+fCsvName+".");
        System.out.println("[multiTrainSweepOthello] "+elapsedTime+" sec.");
        return pa;
    } // multiTrainSweepOthello

    /**
     * Perform Othello multi-training-and-competition with MCTSWrapperAgent wrapped around agent {@code pa} vs. Edax2 with
     * different depth levels. In each run, agent {@code pa} is trained anew. (We need param {@code pa} only to load it
     * and to infer from this all the training params.) The Edax depth values to sweep are coded in array {@code depthArr}
     * in this method. The wrapped agent plays both roles (parameter {@code p_MWrap = 0,1}).
     * <p>
     * Side effect: writes results of multi-competition to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code agtFile, run, competeNum, dEdax, iterMWrap, EPS, p_MWrap, c_puct, winrate, time, userValue2}. <br>
     * The contents may be visualized with R-scripts found in {@code resources\R_plotTools}.
     * <p>
     * NOTE: This method can only be run under Windows, since edax.exe is a Windows executable.
     *
     * @param pa		    agent to wrap
     * @param agtFile       agent filename, we use its {@code agtBase} (part w/o ".agt.zip") to form the new filenames
     * @param maxGameNum    number of training episodes in each run
     * @param iterMWrap	    number of MCTS wrapper iterations
     * @param nruns	        number of runs, each run prints a line to csv. Should be 1 for deterministic agent {@code pa}
     * @param arenaTrain    Arena object with train rights
     * @param gb		    the game board, needed for start state selection
     * @param csvName	    results of competition are written to this filename
     * @return the last wrapped agent
     */
    public PlayAgent multiTrainCompeteSweepOthello(
            PlayAgent pa, String agtFile, int maxGameNum, int iterMWrap, int nruns, Arena arenaTrain,
            GameBoard gb, String csvName)
    {
        String userTitle1 = "time", userTitle2 = "user2";
        DecimalFormat frm = new DecimalFormat("00");
        double userValue1=0.0, userValue2=0.0;
        PlayAgent qa=null;

        SingleTrainer sTrainer = new SingleTrainer();
        SingleCompetitor sCompetitor = new SingleCompetitor();

        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        tools.Utils.checkAndCreateFolder(strDir+"/multiTrain");
        String agtBase = agtFile.split("\\.")[0];   // agent filename w/o ".agt.zip"

        for (int i=0; i<nruns; i++) {

            // train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results during train) + save it
            pa = sTrainer.doSingleTraining(0, i, pa, arenaTrain, arenaTrain.m_xab, gb, maxGameNum, userValue1, userValue2);

            // save pa in a yet unused filename. This for multiple concurrent jobs which should not write to a
            // filename already written by another job. For single-threaded jobs (and no similar files present in
            // dir multiTrain/), k=0 will be used.
            int k=-1;
            String agtPath;
            File file;
            do {
                k++;
                agtPath = strDir + "/multiTrain/" + agtBase + "_" + frm.format(i+k) + ".agt.zip";
                file = new File(agtPath);
            } while (file.exists());
            arenaTrain.saveAgent(pa,agtPath);

            // print the full list mtList after finishing training run i
            // (we use "../" because we do not want to store in subdir "csv/" as printMultiTrainList usually does)
            String trainCsvName = "../multiTrain/"+agtBase+".csv";
            MTrain.printMultiTrainList(trainCsvName,sTrainer.mtList, pa, arenaTrain, userTitle1, userTitle2);

            // multi-competition of trained (and possibly wrapped) agent against Edax
            qa = sCompetitor.doSingleCompetition(i, pa, agtFile, iterMWrap, arenaTrain, gb, userValue2, csvName);
        } // for (i)

        System.out.println("[multiTrainCompeteSweepOthello] "+sCompetitor.getElapsedTime()+" sec.");
        return qa;
    } // multiTrainCompeteSweepOthello

    private class SingleCompetitor {

        protected ArrayList<MCompeteMWrap> mcList;
        protected double elapsedTime = 0.0;

        public SingleCompetitor() {
            this.mcList = new ArrayList<>();
        }

        public double getElapsedTime() { return elapsedTime; }

        public PlayAgent doSingleCompetition(int i, PlayAgent pa, String agtFile, int iterMWrap, Arena arenaTrain,
                                             GameBoard gb, double userValue2, String csvName ) {
            String userTitle1 = "time", userTitle2 = "user2";
            int numEpisodes;    // parameter for competeNPlayer
            int[] iterMWrapArr = (iterMWrap==0) ? new int[]{0} : new int[]{0,iterMWrap};
            int[] depthArr = {1,2,3,4,5,6,7,8,9};
            double[] epsArr =  {1e-8}; //  {1e-8, 0.0};  //  {1e-8, 0.0, -1e-8}; //
            double[] cpuctArr = {1.0}; //{0.2, 0.4, 0.6, 0.8, 1.0, 1.4, 2.0, 4.0, 10.0}; // {1.0};
            double winrate;
            long startTime;
            double deltaTime;

            PlayAgent qa = null;
            ParEdax parEdax = new ParEdax();

            MCompeteMWrap mCompete;
            for (int iter : iterMWrapArr) {
                for (int d : depthArr) {
                    parEdax.setDepth(d);
                    Edax2 edaxAgent = new Edax2("Edax", parEdax);
                    System.out.println("*** Starting run "+i+" multiCompete with Edax depth = " + edaxAgent.getParEdax().getDepth() + " ***");

                    for (double c_puct : cpuctArr) {
                        for (double EPS : epsArr) {
                            ConfigWrapper.EPS = EPS;
                            // if EPS<0 (random action selection in case of 1st visit), perform 5 episodes in competeNPlayer.
                            // Otherwise, the competition is deterministic and 1 episode is sufficient:
                            numEpisodes = (EPS < 0) ? 5 : 1;
                            if (iter == 0) qa = pa;
                            else qa = new MCTSWrapperAgent(iter, c_puct,
                                    new PlayAgentApproximator(pa),
                                    "MCTS-wrapped " + pa.getName(),
                                    -1, new ParOther());

                            StateObservation so = gb.getDefaultStartState();
                            ScoreTuple sc;
                            PlayAgtVector paVector = new PlayAgtVector(qa, edaxAgent);
                            for (int p_MWrap : new int[]{0, 1}) {     // p_MWrap: whether MCTSWrapper is player 0 or player 1
                                startTime = System.currentTimeMillis();
                                sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MWrap), so, numEpisodes, 0, null);
                                winrate = (sc.scTup[p_MWrap] + 1) / 2;
                                deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                                mCompete = new MCompeteMWrap(i, agtFile, numEpisodes, d, iter,
                                        EPS, p_MWrap, c_puct, winrate,
                                        deltaTime, userValue2);
                                System.out.println("EPS=" + EPS + ", iter=" + iter + ", dEdax=" + d + ", p=" + p_MWrap + ", winrate=" + winrate);
                                mcList.add(mCompete);
                                elapsedTime += deltaTime;
                            } // for (p_MWrap)
                        } // for (EPS)
                    } // for (c_puct)

                    // print the full list mcList after finishing each triple (p_MWrap,EPS,c_puct)
                    // (overwrites the file written from previous (p_MWrap,EPS,c_puct))
                    MCompeteMWrap.printMultiCompeteList(csvName, mcList, pa, arenaTrain, userTitle1, userTitle2);
                } // for (d)
            } // for (iter)

            return qa;

        }

    }

    private class SingleTrainer {

        protected ArrayList<MTrain> mtList;
        protected boolean doTrainEvaluation;
        protected Measure oQ,oT;

        public SingleTrainer() {
            this.mtList = new ArrayList<>();         // needed for doSingleTraining
            this.oQ = new Measure();			     //
            this.oT = new Measure();			     //
        }

        public PlayAgent doSingleTraining(int n, int i, PlayAgent pa, Arena arenaTrain, XArenaButtons xab,
                                          GameBoard gb, int maxGameNum, double userValue1, double userValue2 ) { //throws IOException  {
            int numEval = xab.oPar[n].getNumEval();
            int gameNum;
            long actionNum, trnMoveNum;
            double totalTrainSec, elapsedTime;
            long elapsedMs;
            int stopEval = 0;
            MTrain mTrain;
            double evalQ, evalT=0.0;

            // Construct agent anew with the settings of xab. Necessary to build a new set of random n-tuples in each run
            // (if random n-tuple creation is specified)
            try {
                String sAgent = xab.getSelectedAgent(n);
                pa = arenaTrain.m_xfun.constructAgent(n,sAgent, xab);
                pa = arenaTrain.m_xfun.wrapAgent(pa, pa.getParOther(), null, gb.getDefaultStartState());
                if (pa==null) throw new RuntimeException("Could not construct AgentX = " + sAgent);
            }  catch(RuntimeException e)
            {
                e.printStackTrace(System.err);
                return pa;
            }


            int qem = xab.oPar[n].getQuickEvalMode();
            m_evaluatorQ = xab.m_arena.makeEvaluator(pa,gb,stopEval,qem,1);
            //
            // doTrainEvaluation flags whether Train Evaluator is executed:
            // Evaluator m_evaluatorT is only constructed and evaluated, if in tab 'Other pars'
            // the choice box 'Train Eval Mode' is not -1 ("none").
            int tem = xab.oPar[n].getTrainEvalMode();
            doTrainEvaluation = (tem!=-1);
            if (doTrainEvaluation)
                m_evaluatorT = xab.m_arena.makeEvaluator(pa,gb,stopEval,tem,1);

            System.out.println(pa.stringDescr());
            System.out.println(pa.stringDescr2());

            if (maxGameNum!=-1) pa.setMaxGameNum(maxGameNum);   // if -1, take maxGameNum from loaded agent
            pa.setGameNum(0);
            long startTime = System.currentTimeMillis();
            gb.initialize();
            while (pa.getGameNum()<pa.getMaxGameNum())
            {
                StateObservation so = (pa.getParOther().getChooseStart01())
                                    ? gb.chooseStartState(pa) : gb.getDefaultStartState();

                pa.trainAgent(so);

                gameNum = pa.getGameNum();
                int liveSignal = (so instanceof StateObserverCube) ? 10000 :
                                 (!pa.isWrapper()) ? 500 : 50;
                if (gameNum % liveSignal == 0) {
                    System.out.println("gameNum: "+gameNum);
                }
                if (gameNum % numEval==0 ) { //|| gameNum==1) {
                    elapsedMs = (System.currentTimeMillis() - startTime);
                    pa.incrementDurationTrainingMs(elapsedMs);
                    elapsedTime = (double)elapsedMs/1000.0;
                    // elapsedTime: time [sec] for the last numEval training games
                    System.out.println(pa.printTrainStatus()+", "+elapsedTime+" sec");
                    startTime = System.currentTimeMillis();

                    xab.setGameNumber(gameNum);

                    m_evaluatorQ.eval(pa);
                    evalQ = m_evaluatorQ.getLastResult();
                    if (doTrainEvaluation) {
                        m_evaluatorT.eval(pa);
                        evalT = m_evaluatorT.getLastResult();
                    }

                    // gather information for later printout to agents/gameName/csv/multiTrain.csv.
                    actionNum = pa.getNumLrnActions();
                    trnMoveNum = pa.getNumTrnMoves();
                    totalTrainSec = (double)pa.getDurationTrainingMs()/1000.0;
                    // totalTrainSec = time [sec] needed since start of training
                    // (only self-play, excluding evaluations)
                    mTrain = new MTrain(i,gameNum,evalQ,evalT,
                            actionNum,trnMoveNum,totalTrainSec,actionNum/totalTrainSec,
                            userValue1,userValue2);
                    mtList.add(mTrain);

                    elapsedMs = (System.currentTimeMillis() - startTime);
                    pa.incrementDurationEvaluationMs(elapsedMs);

                    startTime = System.currentTimeMillis();
                }	// if (gameNum%numEval==0 )
            } // while


            m_evaluatorQ.eval(pa);
            evalQ = m_evaluatorQ.getLastResult();
            oQ.add(evalQ);
            if (doTrainEvaluation) {
                m_evaluatorT.eval(pa);
                evalT = m_evaluatorT.getLastResult();
                oT.add(evalT);
            }

            elapsedMs = (System.currentTimeMillis() - startTime);
            pa.incrementDurationEvaluationMs(elapsedMs);

            return pa;
        }
    }

}
