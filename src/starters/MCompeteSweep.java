package starters;

import controllers.PlayAgent;
import games.*;
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
     *     <li> agent files {@code <agtBase>_<i+k>.agt.zip} where {@code i} it the number of the run and k is selected
     *          in such a way that a yet unused filename is taken (see code around {@code agtPath} below). This for
     *          multiple concurrent jobs which should not write to a filename already written by another job.
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
        // we use "../" because we do not want to store in subdir "csv/" as printMultiTrainList usually does

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

                // save pa to a yet unused filename. This for multiple concurrent jobs which should not write to a
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
                fCsvName=MTrain.printMultiTrainList(trainCsvName, sTrainer.getMtList(), pa, arenaTrain, userTitle1, userTitle2);

                deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                elapsedTime += deltaTime;
            }

        }
        if (fCsvName!=null) System.out.println("[multiTrainSweepOthello] Results saved to "+fCsvName+".");
        System.out.println("[multiTrainSweepOthello] "+elapsedTime+" sec.");
        return pa;
    } // multiTrainSweepOthello

    // --- never used ---
//    /**
//     * Perform Othello multi-training-and-competition with MCTSWrapperAgent wrapped around agent {@code pa} vs. Edax2 with
//     * different depth levels. In each run, agent {@code pa} is trained anew. (We need param {@code pa} only to load it
//     * and to infer from this all the training params.) The Edax depth values to sweep are coded in array {@code depthArr}
//     * in this method. The wrapped agent plays both roles (parameter {@code p_MWrap = 0,1}).
//     * <p>
//     * Side effect: writes results of multi-competition to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
//     * This file has the columns: <br>
//     * {@code agtFile, run, competeNum, dEdax, iterMWrap, EPS, p_MWrap, c_puct, winrate, time, userValue2}. <br>
//     * The contents may be visualized with R-scripts found in {@code resources\R_plotTools}.
//     * <p>
//     * NOTE: This method can only be run under Windows, since edax.exe is a Windows executable.
//     *
//     * @param pa		    agent to wrap
//     * @param agtFile       agent filename, we use its {@code agtBase} (part w/o ".agt.zip") to form the new filenames
//     * @param maxGameNum    number of training episodes in each run
//     * @param iterMWrap	    number of MCTS wrapper iterations
//     * @param nruns	        number of runs, each run prints a line to csv. Should be 1 for deterministic agent {@code pa}
//     * @param arenaTrain    Arena object with train rights
//     * @param gb		    the game board, needed for start state selection
//     * @param csvName	    results of competition are written to this filename
//     * @return the last wrapped agent
//     */
//    public PlayAgent multiTrainCompeteSweepOthello(
//            PlayAgent pa, String agtFile, int maxGameNum, int iterMWrap, int nruns, Arena arenaTrain,
//            GameBoard gb, String csvName)
//    {
//        String userTitle1 = "time", userTitle2 = "user2";
//        DecimalFormat frm = new DecimalFormat("00");
//        double userValue1=0.0, userValue2=0.0;
//        PlayAgent qa=null;
//
//        SingleTrainer sTrainer = new SingleTrainer();
//        SingleCompetitor sCompetitor = new SingleCompetitor();
//
//        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
//        String subDir = arenaTrain.getGameBoard().getSubDir();
//        if (subDir != null) strDir += "/" + subDir;
//        tools.Utils.checkAndCreateFolder(strDir+"/multiTrain");
//        String agtBase = agtFile.split("\\.")[0];   // agent filename w/o ".agt.zip"
//
//        for (int i=0; i<nruns; i++) {
//
//            // train pa, adjust doTrainEvaluation, and add elements to mtList (evaluation results during train) + save it
//            pa = sTrainer.doSingleTraining(0, i, pa, arenaTrain, arenaTrain.m_xab, gb, maxGameNum, userValue1, userValue2);
//
//            // save pa in a yet unused filename. This for multiple concurrent jobs which should not write to a
//            // filename already written by another job. For single-threaded jobs (and no similar files present in
//            // dir multiTrain/), k=0 will be used.
//            int k=-1;
//            String agtPath;
//            File file;
//            do {
//                k++;
//                agtPath = strDir + "/multiTrain/" + agtBase + "_" + frm.format(i+k) + ".agt.zip";
//                file = new File(agtPath);
//            } while (file.exists());
//            arenaTrain.saveAgent(pa,agtPath);
//
//            // print the full list mtList after finishing training run i.
//            // We use "../" because we do not want to store in subdir "csv/" as printMultiTrainList usually does
//            String trainCsvName = "../multiTrain/"+agtBase+".csv";
//            MTrain.printMultiTrainList(trainCsvName,sTrainer.getMtList(), pa, arenaTrain, userTitle1, userTitle2);
//
//            // multi-competition of trained (and possibly wrapped) agent against Edax
//            qa = sCompetitor.doSingleCompetition(i, pa, agtFile, iterMWrap, arenaTrain, gb, userValue2, csvName);
//        } // for (i)
//
//        System.out.println("[multiTrainCompeteSweepOthello] "+sCompetitor.getElapsedTime()+" sec.");
//        return qa;
//    } // multiTrainCompeteSweepOthello

}
