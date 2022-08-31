package starters;

import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.Arena;
import games.Evaluator;
import games.RubiksCube.ArenaCube;
import games.RubiksCube.GameBoardCube;
import params.ParNT;
import params.ParOther;
import params.ParWrapper;
import tools.Types;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MCubeIterSweep extends GBGBatch {

    protected Evaluator m_evaluatorQ = null;

    /**
     * Here we test how evaluation performance (solved-rate) of a trained agent evolves as a function of {@code iterMWrap}
     * (number of symmetries used).
     * <ol>
     *    <li> Load trained agent from {@code agtFileArr} to agent 0. Specify for certain parameters (here:
     *         {@code pMinEval}, {@code pMaxEval},
     *         {@code iterMWrapArr} the settings for each round. Then do each round i:
     *    <li> Evaluate agent 0 with quick-eval-mode 1.
     *    <li> Store results (eval time, perc. solved rate, ...) in an {@link MCube MCube}
     *         object and collect all {@link MCube MCube} objects in
     *         {@code ArrayList<MCube> mcList}.
     * </ol>
     *  Write {@code mcList} to file csvName.
     */
    public void symmIterTest3x3x3(int [] iterMWrapArr, double c_puct, String agtDir, Arena arenaTrain, String csvName) {
//        String selectedGame = "RubiksCube";
//        String[] scaPar = {"3x3x3", "STICKER2", "QTM"};
//        arenaTrain = SetupGBG.setupSelectedGame(selectedGame, scaPar, "", false, true);
//        String csvName = "symmIterTest3x3x3-nSym00.csv";
//        String[] agtFileArr = {"multiTrain/TCL4-p13-3000k-120-7t-nSym08_00.agt.zip",
//                               "multiTrain/TCL4-p13-3000k-120-7t-nSym08_01.agt.zip"};
//        String[] agtFileArr = {"TCL4-p13-3000k-120-7t.agt.zip"};

        int pMinEval = 10;   // min number of twists during evaluation
        int pMaxEval = 13;   // max number of twists during evaluation
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
    public void symmIterSingle3x3x3(int [] iterMWrapArr, double c_puct, String agtDir, Arena arenaTrain, String csvName) {
        int pMinEval = 1;   // min number of twists during evaluation
        int pMaxEval = 13;   // max number of twists during evaluation
//        int pMinEval = 14;   // min number of twists during evaluation
//        int pMaxEval = 26;   // max number of twists during evaluation
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
        } // for (i)

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
        opar.setNumEval(10000);
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

}
