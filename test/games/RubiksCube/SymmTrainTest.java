package games.RubiksCube;

import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.EvalResult;
import org.junit.Test;
import params.ParNT;
import params.ParOther;
import starters.MCube;
import starters.SetupGBG;
import java.util.ArrayList;

public class SymmTrainTest extends CubeTrain_Test {
    /**
     * An exemplary cube train and evaluation / analysis program: <br>
     * Here we test how training time and performance (solved-rate) evolves as a function of {@code nSym}
     * (number of symmetries used).
     * <ol>
     *    <li> Load agent 0 from agtFile to get all parameters. Specify for certain parameters (here:
     *         {@code USESYMMETRY}, {@code nSym})
     *         in {@code usesymArr} and {@code nsymArr} the settings for each round. Then do each round i:
     *    <li> Set {@code USESYMMETRY} and {@code nSym} according to the i-th array elements, re-construct and re-train agent 0
     *         with {@code CubeConfig.pMax=pMaxTrain}. Measure training time.
     *    <li> Evaluate agent 0 with quick-eval-mode 1 and {@code CubeConfig.pMax=pMaxEval}.
     *    <li> Store results (training time, perc. solved rate, ...) in an {@link MCube MCube}
     *         object and collect all {@link MCube MCube} objects in
     *         {@code ArrayList<MCube> mcList}.
     * </ol>
     *  Write {@code mcList} to file csvName.
     */
    @Test
    public void ColSymmTest2x2x2() {
        boolean[] usesymArr = {false, true, true, true, true};
        int[] nsymArr = {0, 2, 8, 16, 24};
        int maxGameNum = 100000;        // number of training episodes. If -1, take maxGameNum from loaded agent.
        int pMaxTrain = 16;   // max number of twists during training
        int pMaxEval = 16;   // max number of twists during evaluation
        String selectedGame = "RubiksCube";
        String[] scaPar = {"2x2x2", "STICKER2", "QTM"};
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame, scaPar, "", false, true);

        String csvName = "colSymmTest2x2x2.csv";
        String agtFile = "TCL4-p16-9000k-60-7t-lam05.agt.zip";
        setupPaths(agtFile, csvName);        // builds filePath

        colorSymTest(usesymArr, nsymArr, maxGameNum, pMaxTrain, pMaxEval, csvName, agtFile);

    }
    // Configure this run configuration with -ea -Xmx12096M, otherwise Heap Space Exception
    @Test
    public void ColSymmTest3x3x3() {
        boolean[] usesymArr = {false, true, true, true, true};
        int[] nsymArr = {0, 2, 8, 16, 24};
        int maxGameNum = 100000;        // number of training episodes. If -1, take maxGameNum from loaded agent.
        int pMaxTrain = 9;   // max number of twists during training
        int pMaxEval = 9;   // max number of twists during evaluation
        String selectedGame = "RubiksCube";
        String[] scaPar = {"3x3x3", "STICKER2", "QTM"};
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame, scaPar, "", false, true);

        String csvName = "colSymmTest3x3x3.csv";
        String agtFile = "TCL4-p9-3000k-120-7t.agt.zip";
        setupPaths(agtFile, csvName);        // builds filePath

        colorSymTest(usesymArr, nsymArr, maxGameNum, pMaxTrain, pMaxEval, csvName, agtFile);

    }

    private void colorSymTest(boolean[] usesymArr, int[] nsymArr, int maxGameNum, int pMaxTrain, int pMaxEval,
                              String csvName, String agtFile) {
        // Step 1) load agent to fill it with the appropriate parameter settings
        boolean res = arenaTrain.loadAgent(0, filePath);
        if (!res) {
            System.err.println("\n[ColSymmTest] Aborted (no agent found).");
            return;
        }
        PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];
        ParOther opar = pa.getParOther();
        opar.setNumEval(10000);
        assert (pa instanceof TDNTuple4Agt);
        TDNTuple4Agt ta = (TDNTuple4Agt) pa;
        ParNT ntpar = ta.getParNT();
        MCube mcube;
        ArrayList<MCube> mcList = new ArrayList<>();
        String s=null;

        for (int i=0; i<usesymArr.length; i++) {
            System.out.println("[ColSymTest] Starting run with USESYMMETRY="+usesymArr[i]+", nSym="+nsymArr[i]+" ...");
            ntpar.setUSESYMMETRY(usesymArr[i]);
            ntpar.setNSym(nsymArr[i]);

            arenaTrain.m_xab.ntPar[0] = ntpar;
            arenaTrain.m_xab.oPar[0] = opar;

            try {
                // Construct agent anew, i.e. call 'pa = new TDNTuple4Agt(...)' with the actual parameters from Param Tabs.
                // This is necessary to start with a fresh agent each time (and not to retrain the already existing pa
                // in its actual training state)
                String sAgent = arenaTrain.m_xab.getSelectedAgent(0);
                pa = arenaTrain.m_xfun.constructAgent(0,sAgent, arenaTrain.m_xab);
                if (pa==null) throw new RuntimeException("Could not construct AgentX = " + sAgent);
            }  catch(RuntimeException e) {
                e.printStackTrace(System.err);
            }

            // Step 2
            GameBoardCube gb = (GameBoardCube) arenaTrain.getGameBoard();
            pa = trainCube(pa,maxGameNum, pMaxTrain, gb);
            double trainSec = pa.getDurationTrainingMs()/1000.0;
            //System.out.println("[trainCube] training finished in "+trainSec+" sec.");

            // Step 3
            int qem = 1;
            arenaTrain.m_xab.oPar[0].setpMaxRubiks(pMaxEval);
            m_evaluatorQ = arenaTrain.makeEvaluator(pa,gb,0,qem,1);
            EvalResult eresQ = m_evaluatorQ.eval(pa);
            System.out.println("Avg.success: "+eresQ.getResult()+" for pMax="+pMaxEval);

            // Step 4
            mcube = new MCube(0, agtFile, pa.getGameNum(), nsymArr[i], 1, pMaxEval, eresQ.getResult(), 0.0, trainSec, 0,0);
            mcList.add(mcube);

            s = mcube.printMCubeList(csvName, mcList, pa, agtFile, arenaTrain, "", "");
            // just for safety: print results-so-far after each run
        }
        if (s!=null) System.out.println("Results written to "+s);
    }

}
