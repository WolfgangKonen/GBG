package starters;

import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.*;
import games.RubiksCube.*;
import params.ParTD;
import params.ParNT;
import params.ParOther;
import params.ParWrapper;
import tools.ScoreTuple;
import tools.Types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

public class MCubeIterSweep extends GBGBatch {

    protected Evaluator m_evaluatorQ = null;

    /**
     * Here we test how evaluation performance (solved-rate) of a trained agent evolves as a function of {@code iterMWrap}
     * (iterations in wrapper). Solved-rates are averaged over all twist levels in [{@code pMinEval}, {@code pMaxEval}].
     * <ol>
     *    <li> Load in turn each trained agent from {@code agtDir} to agent 0.
     *    <li> Evaluate agent 0 with quick-eval-mode 1.
     *    <li> Store results (eval time, perc. solved rate, ...) in an {@link MCube MCube}
     *         object and collect all {@link MCube MCube} objects in
     *         {@code ArrayList<MCube> mcList}.
     * </ol>
     *  Write {@code mcList} to file csvName.
     */
    public void symmIterTest3x3x3(int [] iterMWrapArr, int pMinEval, int pMaxEval, double c_puct,
                                  String agtDir, Arena arenaTrain, String csvName) {
//        int pMinEval = 10;   // min number of twists during evaluation        // now read from prop
//        int pMaxEval = 13;   // max number of twists during evaluation
        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        File directoryPath = new File(strDir+"/" + agtDir);
        String[] contents = directoryPath.list();   // an array with .agt.zip and .csv filenames
        assert Objects.requireNonNull(contents).length>0 : "Directory "+directoryPath +" is empty!";

        ArrayList<MCube> mcList = new ArrayList<>();
        for (String content : contents) {
            if (!content.split("\\.")[1].equals("csv")) {
                String agtFile = agtDir + "/" + content;
                setupPaths(agtFile, csvName);        // builds filePath

                symmIterTest(iterMWrapArr, pMinEval, pMaxEval, c_puct, csvName, agtFile, mcList);
            }
        } // for (i)

    }
    // same as symmIterTest3x3x3, but each pTwist-level separate
    public void symmIterSingle3x3x3(int [] iterMWrapArr, int pMinEval, int pMaxEval, double c_puct,
                                    String agtDir, Arena arenaTrain, String csvName) {
//        int pMinEval = 1;   // min number of twists during evaluation             // now read from prop
//        int pMaxEval = 18; // 13;   // max number of twists during evaluation
        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        File directoryPath = new File(strDir+"/" + agtDir);
        String[] contents = directoryPath.list();   // an array with .agt.zip and .csv filenames
        assert Objects.requireNonNull(contents).length>0 : "Directory "+directoryPath +" is empty!";

        ArrayList<MCube> mcList = new ArrayList<>();
        for (String content : contents) {
            if (!content.split("\\.")[1].equals("csv")) {   // skip any .csv files in agtDir
                String agtFile = agtDir + "/" + content;
                setupPaths(agtFile, csvName);        // builds filePath

                for (int pTwist = pMinEval; pTwist <= pMaxEval; pTwist++) {
                    symmIterTest(iterMWrapArr, pTwist, pTwist, c_puct, csvName, agtFile, mcList);
                }
            }
        } // for (content)

    }

    private void symmIterTest(int[] iterMWrapArr, int pMinEval, int pMaxEval, double c_puct,
                              String csvName, String agtFile, ArrayList<MCube> mcList) {
        // Step 1) load agent and fill it with the appropriate parameter settings
        assert (arenaTrain instanceof ArenaCube);
        boolean res = arenaTrain.loadAgent(0, filePath);
        if (!res) {
            System.err.println("\n[symmIterTest] Aborted (no agent found).");
            return;
        }
        PlayAgent qa, pa = arenaTrain.m_xfun.m_PlayAgents[0];
        assert (pa instanceof TDNTuple4Agt);
        TDNTuple4Agt ta = (TDNTuple4Agt) pa;
        ParNT ntpar = ta.getParNT();
        int nSym = ntpar.getNSym();
        ParOther opar = pa.getParOther();
        ParWrapper wpar = pa.getParWrapper();
        opar.setNumEval(10000);         // not relevant here: during training, the trained agent is evaluated every numEval episodes.
        opar.setpMinRubiks(pMinEval);
        opar.setpMaxRubiks(pMaxEval);
        MCube mcube;
        String s=null;

        for (int i=0; i<iterMWrapArr.length; i++) {
            int iterMWrap = iterMWrapArr[i];
            System.out.println("[symmIterTest] Starting run with iterMCTSWrap="+iterMWrap+" ...");
            opar.setWrapperMCTSIterations(iterMWrap);
            wpar.setWrapperMCTS_iterations(iterMWrap);

            arenaTrain.m_xab.oPar[0] = opar;
            if (iterMWrap == 0) qa = pa;
            else qa = new MCTSWrapperAgent(iterMWrap, c_puct,
                    new PlayAgentApproximator(pa),
                    "MCTS-wrapped " + pa.getName(),
                    100, opar);

            // Step 2) evaluate agent with actual iterMWrap
            long startTime = System.currentTimeMillis();
            GameBoardCube gb = (GameBoardCube) arenaTrain.getGameBoard();
            int qem = 1;
            m_evaluatorQ = arenaTrain.makeEvaluator(qa,gb,0,qem,1);
            m_evaluatorQ.eval(qa);
            double evalSec = (System.currentTimeMillis() - startTime)/1000.0;
            System.out.print("Avg.success: "+m_evaluatorQ.getLastResult()+" for pMin..pMax="+pMinEval+".."+pMaxEval);
            System.out.println("   (eval time: "+(int)evalSec+" sec)");

            // Step 3) write to CSV
            mcube = new MCube(i, agtFile, pa.getGameNum(), nSym, pMinEval, pMaxEval, m_evaluatorQ.getLastResult(), 0.0, evalSec, iterMWrap,0);
            mcList.add(mcube);

            s = mcube.printMCubeList(csvName, mcList, pa, agtFile, arenaTrain, "iterMWrap", "userVal2");
            // just for safety: print results-so-far to file after each run
        }
        if (s!=null) System.out.println("Results written to "+s);
    }

    /**
     * Evaluate a trained Rubik's Cube agent
     * @param scaPar    scalable parameters
     * @param agtFile   the agent to wrap
     * @param iterMWrapArr the iterations for MCTS wrapper
     * @param cPuctArr  parameters for c_puct
     * @param maxDepth  maximum tree depth in MCTS
     * @param ee        maximum evaluation episode length
     * @param pMin      min number of scrambling twists
     * @param pMax      max number of scrambling twists
     * @param nruns     number of runs
     * @param csvName   result file
     *
     * @see GBGBatch#batch10(int, Properties, String[], String, String, String) GBGBatch.batch10
     */
    public void evalRubiksCube(String[] scaPar,
                               String agtFile,
                               int[] iterMWrapArr,
                               double[] cPuctArr, int maxDepth, int ee,
                               int pMin, int pMax, int nruns,
                               String csvName) {
        long startTime;
        double elapsedTime=0,deltaTime;

        PlayAgent pa;
        PlayAgent qa;
        double[] epsArr = {1e-8}; // possible settings {1e-8, 0.0, -1e-8}, but we stick now (09/2022) always to 1e-8
        //double c_puct=1.0; //1.0; //10.0;     // Sep'2022: c_puct=1.0  was found to be in most cases better than 10.0.
                                                // With cPuctArr we evaluate now both values.
        String userTitle1 = "pTwist", userTitle2 = "EE";
        double percSolved;

        MRubiksMWrap mCompete;
        ArrayList<MRubiksMWrap> mcList = new ArrayList<>();

        GameBoardCube gb = new GameBoardCube(arenaTrain);		// needed for chooseStartState()

        for (int run=0; run<nruns; run++) {
            for (double c_puct : cPuctArr){
                System.out.println("*** Starting run " + run + "/" + (nruns - 1) +
                        " of evalRubiksCube for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] +
                        "}) ***");
                pa = arenaTrain.loadAgent(agtFile);

                ParOther oPar = new ParOther();
                ParWrapper wrPar = new ParWrapper();
                wrPar.setWrapperNPly(0);     // or >0 together with iterMWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParamsOfromWr(wrPar);

                for (double eps : epsArr) {
                    ConfigWrapper.EPS = eps;
                    for (int iterMCTSWrap : iterMWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator(pa),
                                "MCTS-wrapped", // +" " + pa.getName(),
                                maxDepth, oPar);
                        if (wrPar.getWrapperNPly() > 0) {
                            System.out.println("wrPar nPly = " + wrPar.getWrapperNPly());
                            qa = new MaxN2Wrapper(pa, wrPar.getWrapperNPly(), oPar);
                        }

                        startTime = System.currentTimeMillis();

                        double avgPercSolved = 0.0;
                        for (int p = pMin; p <= pMax; p++) {
                            EvalCubeParams ecp = new EvalCubeParams(p, p, ee, CubeConfig.EvalNmax);
                            EvaluatorCube m_eval = new EvaluatorCube(qa, gb, 0, 1, 0, ecp);
                            m_eval.evalAgent(qa);
                            percSolved = m_eval.getLastResult();
                            System.out.println(m_eval.getMsg());
                            mCompete = new MRubiksMWrap(run, agtFile, maxDepth, scaPar[0], iterMCTSWrap,
                                    eps, 0, c_puct, percSolved,
                                    p, ee);
                            mcList.add(mCompete);
                            avgPercSolved += percSolved;
                        }
                        avgPercSolved /= (pMax - pMin + 1);

                        deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                        elapsedTime += deltaTime;
                        System.out.println("... for EPS=" + eps + ", iter=" + iterMCTSWrap + ": " +
                                "avgPercSolved=" + avgPercSolved + ", time=" + deltaTime + " sec");

                        // print the full list mcList after finishing each  (iterMWrap)
                        // (overwrites the file written from previous (iterMWrap))
                        MRubiksMWrap.printMultiEvalList(csvName, mcList, pa, arenaTrain, userTitle1, userTitle2);
                    } // for (iterMWrap)
                } // for (eps)
            } // for (c_puct)
        } // for (run)

        System.out.println("[evalRubiksCube] "+elapsedTime+" sec.");
    } // evalRubiksCube

    /**
     * Perform Rubik's Cube multi-training. In each run, agent {@code pa} is constructed anew (to get different random tuples)
     * and then trained.
     * <p>
     * Side effect: writes results to directory {@code agents/RubiksCube/<subDir>/<trainOutDir>/}: <ul>
     *     <li> agent files {@code <agtBase>_<i+k>.agt.zip} where {@code i} it the number of the run and k is selected
     *          in such a way that a yet unused filename is taken (see code around {@code agtPath} below). This for
     *          multiple concurrent jobs which should not write to a filename already written by another job.
     *     <li> train csv file {@code <agtBase>*.csv}
     * </ul>
     * The train csv file may be visualized with R-scripts found in {@code resources\R_plotTools}.
     * <p>
     * <strong>NEW</strong>: Loop over {@code rewardPosArr}  and {@code stepRewardArr}
     *
     * @param pa		    loaded agent (maybe stub) from which all training params are inherited
     * @param agtFile       agent filename, we use its {@code agtBase} (part w/o ".agt.zip") to form the new filenames
     * @param maxGameNum    number of training episodes in each run
     * @param nruns	        number of training runs
     * @param rewardPosArr  values for {@link ParTD#getRewardPositive()}
     * @param stepRewardArr values for {@link ParTD#getStepReward()}
     * @param arenaTrain    Arena object with train rights
     * @param gb		    the game board, needed for start state selection
     * @param trainOutDir   where to store trained agents and train csv (e.g. "multiTrain")
     * @return the last trained agent
     */
    public PlayAgent multiTrainSweepCube(
            PlayAgent pa, String agtFile, int maxGameNum, int nruns,
            double[] rewardPosArr, double[] stepRewardArr,
            Arena arenaTrain, GameBoard gb, String trainOutDir)
    {
        DecimalFormat frm1 = new DecimalFormat("000");
        DecimalFormat frm2 = new DecimalFormat("00");
        double userValue1, userValue2;
        long startTime;
        double elapsedTime=0,deltaTime;
        int pMaxEval = 16;   // max number of twists during predict_value
        int nump = 200;      // number of cubes in predict_value

        SingleTrainer sTrainer = new SingleTrainer();

        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        tools.Utils.checkAndCreateFolder(strDir+"/" + trainOutDir);
        String agtBase = agtFile.split("\\.")[0];       // agtBase = agtFile w/o .agt.zip

        String userTitle1 = "rewardPos";
        String userTitle2 = "stepReward";
        String trainCsvName, fCsvName="";
        // we use "../" because we do not want to store in subdir "csv/" as printMultiTrainList usually does

        // vcResults.txt: ValueContainer results, see predict_value below
        PrintWriter mtWriter = null;
        String vcCsvName=trainOutDir + "/vcResults.txt";
        try {
            mtWriter = new PrintWriter(new FileWriter(strDir+"/"+vcCsvName,false));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        mtWriter.println("Start");
        mtWriter.close();

        for (double r : rewardPosArr)
        for (double s : stepRewardArr) {
            arenaTrain.m_xab.tdPar[0].setStepReward(s);
            arenaTrain.m_xab.tdPar[0].setRewardPositive(r);
            userValue1 = r;
            userValue2 = s;

            for (int i=0; i<nruns; i++) {

                startTime = System.currentTimeMillis();

                // train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results during train)
                pa = sTrainer.doSingleTraining(0, i, pa, arenaTrain, arenaTrain.m_xab, gb, maxGameNum, userValue1, userValue2);
                arenaTrain.m_xab.setOParFrom(0,pa.getParOther());  // /WK/ Bug fix 2022-04-12

                // save pa to a yet unused filename. This for multiple concurrent jobs which should not write to a
                // filename already written by another job. For single-threaded jobs (and no similar files present in
                // dir trainOutDir), k=0 will be used.
                int k=-1;
                String agtPath,agt_w_o_suffix;
                File file;
                do {
                    k++;    // start with k=0
                    agt_w_o_suffix = agtBase + "_sr" + frm1.format(s*100)
                            + "_rp" + frm1.format(r*10)+ "_" + frm2.format(i+k);
                    trainCsvName = "../" + trainOutDir + "/" + agt_w_o_suffix + ".csv";
                    agtPath = strDir + "/" + trainOutDir + "/" + agt_w_o_suffix + ".agt.zip";
                    file = new File(agtPath);
                } while (file.exists());
                arenaTrain.saveAgent(pa,agtPath);

                // print the full list mtList after finishing training run i
                fCsvName=MTrain.printMultiTrainList(trainCsvName, sTrainer.getMtList(), pa, arenaTrain, userTitle1, userTitle2);

                try {
                    mtWriter = new PrintWriter(new FileWriter(strDir+"/"+vcCsvName,true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mtWriter.println("\n*** run="+i+", stepReward="+s+", rewardPos="+r+" ***");
                predict_value(pa, pMaxEval, nump, (GameBoardCube) arenaTrain.getGameBoard(), mtWriter);
                mtWriter.close();

                deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                elapsedTime += deltaTime;
            }

        } // for r,s
        if (fCsvName!=null) System.out.println("[multiTrainSweepCube] Results saved to "+fCsvName+".");
        System.out.println("[multiTrainSweepCube] "+elapsedTime+" sec.");
        return pa;
    } // multiTrainSweepCube

    /**
     * Let the agent {@code pa} predict values for p-twisted cubes. <br>
     * For each cube we determine with
     * {@link StateObserverCube#solveCube(PlayAgent, StateObservation, double, int, boolean) solveCube}
     * the solution length (-1 if not solved)
     * @param pa    the agent
     * @param pMax  the number of twists is 1,...,pMax
     * @param nump  number of cubes for each p
     * @param gb    the game board
     * @param mtWriter  a Writer for diagnostic output (usually vcResults.txt)
     * @return a {@link HashMap} with a {@link ValueContainer} for every key p = {1,...,pMax} (number of twists)
     */
    public HashMap<Integer, ValueContainer> predict_value(PlayAgent pa, int pMax, int nump, GameBoardCube gb,
                                                             PrintWriter mtWriter) {
        HashMap<Integer,ValueContainer> hmSolved = new HashMap<>();
        HashMap<Integer,ValueContainer> hmUnsolved = new HashMap<>();
        int epiLengthEval=30;

        for (int p=1; p<=pMax; p++) {
            ValueContainer vc = new ValueContainer(nump);

            for (int n=0; n<nump; n++) {
                double val;
                int length;
                StateObservation so;

//                // accept only states so where the 'true' length found by solveCube is not smaller than p
//                // (in order to avoid pollution of vc.values with seemingly large values from lower-twist cubes)
//                while (length<p) {
//                    so = gb.chooseStartState(p);
//                    val = pa.getScoreTuple(so,new ScoreTuple(1)).scTup[0];
//                    length = solveCube(pa,so,val,20,gb,false);  // questionable, if we now return -1 if unsolved
//                    //length=p;     // alternative: skip acceptance policy
//                }
                // alternative: choose p-twist state just once and record solution length (-1 if unsolved)
                so = gb.chooseStartState(p);
                val = pa.getScoreTuple(so,new ScoreTuple(1)).scTup[0];
                length = StateObserverCube.solveCube(pa,so,val,epiLengthEval,false);

                vc.cubes[n] = so;
                vc.values[n] = val;
                vc.pSolve[n] = length;
            }
            ValueContainer vcSolved = vc.takeSolvedCubes();
            hmSolved.put(p,vcSolved);
            ValueContainer vcUnsolved = vc.takeUnsolvedCubes();
            hmUnsolved.put(p,vcUnsolved);
        }
        mtWriter.println("Value container solved cubes:");
        ValueContainer.printHMapVC(hmSolved,mtWriter);
        mtWriter.println("Value container unsolved cubes:");
        ValueContainer.printHMapVC(hmUnsolved,mtWriter);
        return hmSolved;
    }  // predict_value

}
