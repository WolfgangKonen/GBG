package starters;

import controllers.ExpectimaxNWrapper;
import controllers.HumanPlayer;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.*;
import params.ParMaxN;
import params.ParOther;
import tools.Measure;
import tools.Types;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MTrainSweep {
    protected ArrayList<MTrain> mtList;
    protected boolean doTrainEvaluation;
    protected Measure oQ,oT;

    protected Evaluator m_evaluatorQ = null;
    protected Evaluator m_evaluatorT = null;

    public MTrainSweep() {
        this.mtList = new ArrayList<>();         // needed for doSingleTraining
        this.oQ = new Measure();			     //
        this.oT = new Measure();			     //
    }

    /**
     * Perform {@code trainNum} cycles of training and evaluation for PlayAgent, and perform
     * each self-play training with maxGameNum training games.
     * Both trainNum and maxGameNum are inferred from {@code xab}. <br>
     * Write trained agents to {@code <agentDir>/multiTrain/}, see below. <br>
     * Write results to {@code csvName}, see below.
     * <p>
     * Side effects: <ul>
     *     <li> writes results of multi-training to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     *      This file has the columns: <br>
     *      {@code run, gameNum, evalQ, evalT, actionNum, trnMoves, elapsedTime, movesSecond, userValue1, userValue2}. <br>
     *      The contents may be visualized with one of the R-scripts found in {@code resources\R_plotTools}.
     *     <li> writes trained agents to <b>{@code agents/<gameDir>/multiTrain/<agtBase>_<i+k>.agt.zip}</b>  where
     *     {@code i} it the number of the run and {@code k} is selected in such a way that a yet unused filename is taken
     *     (see code around {@code agtPath} below). This for multiple concurrent jobs which should not write to a
     *     filename already written by another job.
     * </ul>
     *
     * @param n			index of agent to train (usually n=0)
     * @param xab		used for reading parameter values from members *_par and for fetching the name
     * 					of agent <b>n</b>
     * @param gb		the game board, needed for evaluators and start state selection
     * @param csvName	results are written to this filename
     * @return the (last) trained agent
     * @throws IOException if something goes wrong with {@code csvName}, see below
     */
    public PlayAgent multiTrain_M(int n, String agtFile, Arena t_Game,
                                  XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
        DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
        DecimalFormat frm = new DecimalFormat("#0.000");
        String userTitle1 = "", userTitle2 = "";
        double userValue1, userValue2;
        doTrainEvaluation = false;

        int trainNum=xab.getTrainNumber();
        int maxGameNum=xab.getGameNumber();
        PlayAgent pa = null;

        System.out.println("*** Starting multiTrain_M with trainNum = "+trainNum+" ***");

        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + t_Game.getGameName();
        String subDir = t_Game.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        tools.Utils.checkAndCreateFolder(strDir+"/multiTrain");
        String agtBase = agtFile.split("\\.")[0];

        mtList = new ArrayList<MTrain>();
        oQ = new Measure();			// quick eval measure
        oT = new Measure();			// train eval measure

        for (int i=0; i<trainNum; i++) {

            xab.setTrainNumberText(trainNum, Integer.toString(i+1)+"/"+Integer.toString(trainNum) );

            // re-construct pa, train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results)
            pa = doSingleTraining(n,i,pa,t_Game,xab,gb,maxGameNum,0.0,0.0);
            xab.setOParFrom(0,pa.getParOther());  // /WK/ Bug fix 2022-04-12

            // save pa to a yet unused filename. This for multiple concurrent jobs which should not write to a
            // filename already written by another job. For single-threaded jobs (and no similar files present in
            // dir multiTrain/), k=0 will be used.
            int k=-1;
            String agtPath;
            File file;
            DecimalFormat frm2 = new DecimalFormat("00");
            do {
                k++;
                agtPath = strDir + "/multiTrain/" + agtBase + "_" + frm2.format(i+k) + ".agt.zip";
                file = new File(agtPath);
            } while (file.exists());
            t_Game.saveAgent(pa,agtPath);

            // print the full list mtList after finishing each (i)
            // (overwrites the file written from previous (i))
            pa.setAgentFile(agtFile);
            MTrain.printMultiTrainList(csvName,mtList, pa, t_Game, userTitle1, userTitle2);

            if (xab.m_arena.taskState!=Arena.Task.MULTTRN) {
                break; //out of for
            }
        } // for (i)

        if (m_evaluatorQ.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oC did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorQ.getPrintString()+frm3.format(oQ.getMean()) + " +- " + frm.format(oQ.getStd()));
        }
        if (doTrainEvaluation && m_evaluatorT.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oT did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorT.getPrintString()+frm3.format(oT.getMean()) + " +- " + frm.format(oT.getStd()));
        }

        xab.setTrainNumber(trainNum);
        return pa;

    } // multiTrain_M

    /**
     * Perform {@code trainNum * alphaArr.length} cycles of training and evaluation for PlayAgent, and perform
     * each self-play training with maxGameNum training games.
     * Both trainNum and maxGameNum are inferred from {@code xab}. <br>
     * Write results to {@code csvName}, see below.
     *
     * @param n			index of agent to train (usually n=0)
     * @param alphaArr	alpha values to sweep over
     * @param alphaFinalArr	alpha final values to sweep over
     * @param xab		used for reading parameter values from members *_par and for fetching the name
     * 					of agent <b>n</b>
     * @param gb		the game board, needed for evaluators and start state selection
     * @param csvName	results are written to this filename
     * @return the (last) trained agent
     * @throws IOException if something goes wrong with {@code csvName}, see below
     * <p>
     * Side effect: writes results of multi-training to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code run, gameNum, evalQ, evalT, actionNum, trnMoves, elapsedTime, movesSecond, userValue1, userValue2}. <br>
     * The contents may be visualized with one of the R-scripts found in {@code resources\R_plotTools}.
     */
    public PlayAgent multiTrainAlphaSweep(int n, double[] alphaArr, double[] alphaFinalArr, Arena t_Game,
                                          XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
        DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
        DecimalFormat frm = new DecimalFormat("#0.000");
        String userTitle1 = "alpha", userTitle2 = "alphaFinal";
        double userValue1, userValue2;
        doTrainEvaluation = false;

        int trainNum=xab.getTrainNumber();
        int maxGameNum=xab.getGameNumber();
        PlayAgent pa = null;

        System.out.println("*** Starting multiTrain with trainNum = "+trainNum+" ***");

        mtList = new ArrayList<MTrain>();
        oQ = new Measure();			// quick eval measure
        oT = new Measure();			// train eval measure

        for (int i=0; i<trainNum; i++) {
            for (int k=0; k<alphaArr.length; k++) {

                xab.setTrainNumberText(trainNum, Integer.toString(i+1)+"/"+Integer.toString(trainNum) );

                // sweep-specific code which varies alpha & alphaFinal for each k
                // and writes them to userValue1 & userValue2, resp.
                double alpha = alphaArr[k];
                double alphaFinal = alphaFinalArr[k];
                userValue1=alpha;
                userValue2=alphaFinal;
                xab.tdPar[0].setAlpha(alpha);
                xab.tdPar[0].setAlphaFinal(alphaFinal);

                // construct pa, train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results)
                pa = doSingleTraining(n,i,pa,t_Game,xab,gb,maxGameNum,userValue1,userValue2);

                // print the full list mtList after finishing each pair (i,k)
                // (overwrites the file written from previous (i,k))
                MTrain.printMultiTrainList(csvName,mtList, pa, t_Game, userTitle1, userTitle2);

                if (xab.m_arena.taskState!=Arena.Task.MULTTRN) {
                    break; //out of for
                }
            } // for (k)
        } // for (i)

        if (m_evaluatorQ.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oC did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorQ.getPrintString()+frm3.format(oQ.getMean()) + " +- " + frm.format(oQ.getStd()));
        }
        if (doTrainEvaluation && m_evaluatorT.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oT did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorT.getPrintString()+frm3.format(oT.getMean()) + " +- " + frm.format(oT.getStd()));
        }

        xab.setTrainNumber(trainNum);
        return pa;

    } // multiTrainAlphaSweep

    /**
     * Perform {@code trainNum * lambdaArr.length} cycles of training and evaluation for PlayAgent, and perform
     * each self-play training with maxGameNum training games.
     * Both trainNum and maxGameNum are inferred from {@code xab}. <br>
     * Write results to {@code csvName}, see below.
     *
     * @param n			index of agent to train (usually n=0)
     * @param lambdaArr	lambda values to sweep over
     * @param xab		used for reading parameter values from members *_par and for fetching the name
     * 					of agent <b>n</b>
     * @param gb		the game board, needed for evaluators and start state selection
     * @param csvName	results are written to this filename
     * @return the (last) trained agent
     * @throws IOException if something goes wrong with {@code csvName}, see below
     * <p>
     * Side effect: writes results of multi-training to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code run, gameNum, evalQ, evalT, actionNum, trnMoves, elapsedTime, movesSecond, userValue1, userValue2}. <br>
     * The contents may be visualized with one of the R-scripts found in {@code resources\R_plotTools}.
     */
    public PlayAgent multiTrainLambdaSweep(int n, double[] lambdaArr, Arena t_Game, XArenaButtons xab,
                                           GameBoard gb, String csvName) throws IOException {
        DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
        DecimalFormat frm = new DecimalFormat("#0.000");
        String userTitle1 = "lambda", userTitle2 = "null";
        double userValue1, userValue2;
        doTrainEvaluation = false;

        int trainNum=xab.getTrainNumber();
        int maxGameNum=xab.getGameNumber();
        PlayAgent pa = null;

        System.out.println("*** Starting multiTrain with trainNum = "+trainNum+" ***");

        mtList = new ArrayList<MTrain>();
        oQ = new Measure();			// quick eval measure
        oT = new Measure();			// train eval measure

        for (int i=0; i<trainNum; i++) {
            for (double lambda : lambdaArr) {

                xab.setTrainNumberText(trainNum, (i + 1) + "/" + trainNum);

                // sweep-specific code which varies lambda for each k
                // and writes them to userValue1 & userValue2, resp.
                userValue1 = lambda;
                userValue2 = 0;
                xab.tdPar[0].setLambda(lambda);

                // train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results)
                pa = doSingleTraining(n, i, pa, t_Game, xab, gb, maxGameNum, userValue1, userValue2);

                // print the full list mtList after finishing each pair (i,k)
                // (overwrites the file written from previous (i,k))
                MTrain.printMultiTrainList(csvName, mtList, pa, t_Game, userTitle1, userTitle2);

                if (xab.m_arena.taskState != Arena.Task.MULTTRN) {
                    break; //out of for
                }
            } // for (k)
        } // for (i)

        if (m_evaluatorQ.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oC did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorQ.getPrintString()+frm3.format(oQ.getMean()) + " +- " + frm.format(oQ.getStd()));
        }
        if (doTrainEvaluation && m_evaluatorT.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oT did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorT.getPrintString()+frm3.format(oT.getMean()) + " +- " + frm.format(oT.getStd()));
        }

        xab.setTrainNumber(trainNum);
        return pa;

    } // multiTrainLambdaSweep

    /**
     * Perform {@code trainNum * incAmountArr.length} cycles of training and evaluation for PlayAgent, and perform
     * each self-play training with maxGameNum training games. Currently only sensible for game RubiksCube, replayBuffer.
     * Both trainNum and maxGameNum are inferred from {@code xab}. <br>
     * Write results to {@code csvName}, see below.
     *
     * @param n			index of agent to train (usually n=0)
     * @param incAmountArr	incAmount values to sweep over
     * @param xab		used for reading parameter values from members *_par and for fetching the name
     * 					of agent <b>n</b>
     * @param gb		the game board, needed for evaluators and start state selection
     * @param csvName	results are written to this filename
     * @return the (last) trained agent
     * @throws IOException if something goes wrong with {@code csvName}, see below
     * <p>
     * Side effect: writes results of multi-training to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code run, gameNum, evalQ, evalT, actionNum, trnMoves, elapsedTime, movesSecond, userValue1, userValue2}. <br>
     * The contents may be visualized with one of the R-scripts found in {@code resources\R_plotTools}.
     */
    public PlayAgent multiTrainIncAmountSweep(int n, double[] incAmountArr, Arena t_Game, XArenaButtons xab,
                                              GameBoard gb, String csvName) throws IOException {
        DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
        DecimalFormat frm = new DecimalFormat("#0.000");
        String userTitle1 = "incAmount", userTitle2 = "null";
        double userValue1, userValue2;
        doTrainEvaluation = false;

        int trainNum=xab.getTrainNumber();
        int maxGameNum=xab.getGameNumber();
        PlayAgent pa = null;

        System.out.println("*** Starting multiTrainIncAmount with trainNum = "+trainNum+" ***");

        mtList = new ArrayList<MTrain>();
        oQ = new Measure();			// quick eval measure
        oT = new Measure();			// train eval measure

        for (int i=0; i<trainNum; i++) {
            for (double incAmount : incAmountArr) {

                xab.setTrainNumberText(trainNum, (i + 1) + "/" + trainNum);

                // sweep-specific code which varies incAmount for each k
                // and writes them to userValue1 & userValue2, resp.
                userValue1 = incAmount;
                userValue2 = 0;
                xab.oPar[0].setIncAmount(incAmount);

                // train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results)
                pa = doSingleTraining(n, i, pa, t_Game, xab, gb, maxGameNum, userValue1, userValue2);

                // print the full list mtList after finishing each pair (i,k)
                // (overwrites the file written from previous (i,k))
                MTrain.printMultiTrainList(csvName, mtList, pa, t_Game, userTitle1, userTitle2);

                if (xab.m_arena.taskState != Arena.Task.MULTTRN) {
                    break; //out of for
                }
            } // for (k)
        } // for (i)

        if (m_evaluatorQ.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oC did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorQ.getPrintString()+frm3.format(oQ.getMean()) + " +- " + frm.format(oQ.getStd()));
        }
        if (doTrainEvaluation && m_evaluatorT.getMode()!=(-1))
        // m_mode=-1 signals: 'no evaluation done' --> oT did not receive evaluation results
        {
            System.out.println("Avg. "+ m_evaluatorT.getPrintString()+frm3.format(oT.getMean()) + " +- " + frm.format(oT.getStd()));
        }

        xab.setTrainNumber(trainNum);
        return pa;

    } // multiTrainIncAmountSweep

    //
    // helper functions for multiTrainAlphaSweep & multiTrainLambdaSweep & multiTrainIncAmountSweep
    //

    public PlayAgent doSingleTraining(int n, int i, PlayAgent pa, Arena t_Game, XArenaButtons xab,
                                         GameBoard gb, int maxGameNum, double userValue1, double userValue2 ) { //throws IOException  {
        int numEval = xab.oPar[n].getNumEval();
        int gameNum;
        long actionNum, trnMoveNum;
        double totalTrainSec, elapsedTime;
        long elapsedMs;
        int stopEval = 0;
        MTrain mTrain;
        double evalQ=0.0, evalT=0.0;
        PlayAgent qa;

        try {
            String sAgent = xab.getSelectedAgent(n);
            pa = t_Game.m_xfun.constructAgent(n,sAgent, xab);
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

        pa.setMaxGameNum(maxGameNum);
        pa.setGameNum(0);
        long startTime = System.currentTimeMillis();
        gb.initialize();
        while (pa.getGameNum()<pa.getMaxGameNum())
        {
            StateObservation so = soSelectStartState(gb,xab.oPar[n].getChooseStart01(), pa);

            pa.trainAgent(so);

            gameNum = pa.getGameNum();
            if (gameNum%numEval==0 ) { //|| gameNum==1) {
                elapsedMs = (System.currentTimeMillis() - startTime);
                pa.incrementDurationTrainingMs(elapsedMs);
                elapsedTime = (double)elapsedMs/1000.0;
                // elapsedTime: time [sec] for the last numEval training games
                System.out.println(pa.printTrainStatus()+", "+elapsedTime+" sec");
                startTime = System.currentTimeMillis();

                xab.setGameNumber(gameNum);

                // construct 'qa' anew (possibly wrapped agent for eval)
                qa = wrapAgent(n, pa, xab.oPar[n], xab.maxnPar[n], gb.getStateObs());

                m_evaluatorQ.eval(qa);
                evalQ = m_evaluatorQ.getLastResult();
                if (doTrainEvaluation) {
                    m_evaluatorT.eval(qa);
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


        // construct 'qa' anew (possibly wrapped agent for eval)

        qa = wrapAgent(0, pa, xab.oPar[n], xab.maxnPar[n], gb.getStateObs());
        m_evaluatorQ.eval(qa);
        evalQ = m_evaluatorQ.getLastResult();
        oQ.add(evalQ);
        if (doTrainEvaluation) {
            m_evaluatorT.eval(qa);
            evalT = m_evaluatorT.getLastResult();
            oT.add(evalT);
        }


        elapsedMs = (System.currentTimeMillis() - startTime);
        pa.incrementDurationEvaluationMs(elapsedMs);

        return pa;
    }

    private StateObservation soSelectStartState(GameBoard gb, boolean chooseStart01, PlayAgent pa) {
        StateObservation so;
        if (chooseStart01) {
            so = gb.chooseStartState(pa);
        } else {
            so = gb.getDefaultStartState();
        }
        return so;
    }

    protected PlayAgent wrapAgent(int n, PlayAgent pa, ParOther oPar, ParMaxN mPar, StateObservation so)
    {
        PlayAgent qa;
        int nply = oPar.getWrapperNPly();
        mPar.setMaxNDepth(nply);
        if (nply>0 && !(pa instanceof HumanPlayer)) {
            if (so.isDeterministicGame()) {
                qa = new MaxN2Wrapper(pa,nply,oPar);	// oPar has other params
//				qa = new MaxNWrapper(pa,mPar,oPar);		// mPar has useMaxNHashMap
//				qa = new MaxNWrapper(pa,nply);			// always maxNHashMap==false    // OLD
            } else {
                qa = new ExpectimaxNWrapper(pa,nply);
            }
        } else {
            qa=pa;
        }
        return qa;
    }

}
