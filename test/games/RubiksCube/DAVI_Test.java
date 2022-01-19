package games.RubiksCube;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.StateObservation;
import starters.SetupGBG;
import org.junit.Test;
import tools.ScoreTuple;
import tools.Types;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.OptionalDouble;

public class DAVI_Test {
    protected static Arena arenaTrain;
    protected static String filePath = null;
    protected static String savePath = null;
    protected Evaluator m_evaluatorQ = null;

    /**
     * An exemplary cube train and evaluation / analysis program:
     *      1) load an existing agent from agtFile and re-train it (if maxGameNum!=0) with CubeConfig.pMax=pMaxTrain
     *      2) evaluate agent with quick-eval-mode 1
     *      3) predict the value for a large number of states with given pTwist and collect results in HashMap hm.
     *         We want to know if states s with a true solution length larger than pMaxTrain (which were not seen during
     *         training) have automatically a predicted value V(s) smaller than for the trained levels.
     *         We accept in predict_value only states 'so' where the true length found by solveCube is not smaller than
     *         pTwist (in order to avoid pollution of vc.values with seemingly large values from lower-twisted cubes)
     *      4) for a given pTwist=6 we check the HashMap entry: Do the cubes with value>0.5 have actually a solution
     *         length of 6 or larger (as it should with the acceptance policy in step 3)? - We find that this is true.
     *         (If we skip the acceptance policy in step 3, there are quite a number of states with solution length
     *         5 or smaller)
     */
    @Test
    public void DAVI3Test() {
        int maxGameNum= 0;		// number of training episodes.
                                // If -1, take maxGameNum from loaded agent. If 0, do not retrain
        int pMaxTrain = 5;   // max number of twists during training
        int pMaxEval = 10;   // max number of twists during evaluation
        String selectedGame = "RubiksCube";
        String[] scaPar = SetupGBG.setDefaultScaPars(selectedGame);    // "2x2x2", "STICKER2", "HTM"
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame, scaPar,"",false,true);

        String csvName = "davi3test";
        String agtFile = "davi3-p05-200k-60-7t.agt.zip";
        //String agtFile = "davi3-p11-2000k-60-7t.agt.zip";
        setupPaths(agtFile,csvName);		// builds filePath

        // Step 1
        GameBoardCube gb = (GameBoardCube) arenaTrain.getGameBoard();
        PlayAgent pa = trainCube(agtFile,filePath,maxGameNum, pMaxTrain, gb );

        // Step 2
        int qem = 1;
        arenaTrain.m_xab.oPar[0].setpMaxRubiks(pMaxEval);
        m_evaluatorQ = arenaTrain.makeEvaluator(pa,gb,0,qem,1);
        m_evaluatorQ.eval(pa);

        // Step 3
        int pMax=8;
        int nump=200;
        HashMap<Integer,ValueContainer> hm = predict_value(pa,pMax,nump,gb);

        // Step 4
        int pTwist=6;
        int epiLength=20;
        ValueContainer vc = hm.get(pTwist);
        for (int n=0; n<vc.values.length; n++) {
            //if (vc.values[n]==vc.max.orElse(0)) {
            if (vc.values[n] >= 0.5) {
                solveCube(pa,vc.cubes[n],vc.values[n],epiLength,gb,true);
            }
        }

    }

    public PlayAgent trainCube(String agtFile, String filePath, int maxGameNum, int pMax, GameBoardCube gb) {

        // load agent to fill it with the appropriate parameter settings
        boolean res = arenaTrain.loadAgent(0, filePath);
        if (!res) {
            System.err.println("\n[DAVI_Test.batch5] Aborted (no agent found).");
            return null;
        }
        PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];

        long startTime = System.currentTimeMillis();

        if (maxGameNum!=-1) pa.setMaxGameNum(maxGameNum);   // if -1, take maxGameNum from loaded agent


        gb.initialize();                // this changes CubeConfig.pMin and .pMax and thus ...
        CubeConfig.pMax = pMax;         // we set CubeConfig.pMax, which is used in chooseStartState(pa), afterwards
        pa.setGameNum(0);
        while (pa.getGameNum()<pa.getMaxGameNum())
        {
            StateObservation so = gb.chooseStartState(pa);

            pa.trainAgent(so);

        } // while

        double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[DAVI_Test.batch5] training finished in "+elapsedTime+" sec. ");

        return pa;
    } // trainCube

    /**
     * Let the agent {@code pa} predict values for p-twisted cubes. <br>
     * For each cube we check with {@link #solveCube(PlayAgent, StateObservation, double, int, GameBoardCube, boolean) solveCube}
     * whether the solution length is really p and do not accept cubes with smaller solution length.
     * @param pa    the agent
     * @param pMax  the number of twists is 1,...,pMax
     * @param nump  number of cubes for each p
     * @param gb    the game board
     * @return a HashMap with a ValueContainer for every key p = {1,...,pMax} (number of twists)
     */
    protected HashMap<Integer,ValueContainer> predict_value(PlayAgent pa, int pMax, int nump, GameBoardCube gb) {
        HashMap<Integer,ValueContainer> hm = new HashMap<>();
        for (int p=1; p<=pMax; p++) {
            ValueContainer vc = new ValueContainer(nump);

            for (int n=0; n<nump; n++) {
                double val=1.0;
                int length = 0;
                StateObservation so=null;

                // accept only states so where the 'true' length found by solveCube is not smaller than p
                // (in order to avoid pollution of vc.values with seemingly large values from lower-twist cubes)
                while (length<p) {
                    so = gb.chooseStartState(p);
                    val = pa.getScoreTuple(so,new ScoreTuple(1)).scTup[0];
                    length = solveCube(pa,so,val,20,gb,false);
                    //length=p;     // alternative: skip acceptance policy
                }
                vc.cubes[n] = so;
                vc.values[n] = val;
                vc.pSolve[n] = length;
            }
            vc.calcMeanStd();
            hm.put(p,vc);
        }
        printHMapVC(hm);
        return hm;
    }

    /**
     * A set of {@code cubes} with their predicted {@code values} and solution length {@code pSolve}. <br>
     * This object is also capable to calculate mean, std, min and max of array {@code values}
     */
    static class ValueContainer {
        public double[] values;
        private final double[] sq_dev;
        public StateObservation[] cubes;
        public int[] pSolve;
        public double mean;
        public double std;
        public OptionalDouble min;
        public OptionalDouble max;

        ValueContainer(int nump) {
            values=new double[nump];
            sq_dev = new double[nump];
            cubes = new StateObservation[nump];
            pSolve = new int[nump];
        }

        public void calcMeanStd() {
            mean = Arrays.stream(values).sum()/ values.length;
            for (int n=0; n<values.length; n++) sq_dev[n] = Math.pow(values[n]-mean,2);
            std = Math.sqrt(Arrays.stream(sq_dev).sum()/(sq_dev.length-1));
            min = Arrays.stream(values).min();
            max = Arrays.stream(values).max();

        }
    }

    public static void printHMapVC(HashMap<Integer,ValueContainer> hm) {
        DecimalFormat form = new DecimalFormat("000");
        DecimalFormat form2 = new DecimalFormat("0000");
        DecimalFormat fper = new DecimalFormat("00.0000");
        System.out.println("  p,  num:     mean    stdev      min      max");
        for (Integer p : hm.keySet()) {
            ValueContainer vc = hm.get(p);
            System.out.println(form.format(p) + ", " + form2.format(vc.values.length) + ":  "
                    + fper.format(vc.mean) + ", "
                    + fper.format(vc.std) + ", "
                    + fper.format(vc.min.orElse(Double.NaN)) + ", "
                    + fper.format(vc.max.orElse(Double.NaN))
            );
        }
    }

    protected int solveCube(PlayAgent pa, StateObservation so_in,
                             double value, int epiLength,
                             GameBoardCube gb, boolean verbose)
    {
        StateObservation so = so_in.copy();
        so.resetMoveCounter();

        pa.resetAgent();			// needed if pa is MCTSWrapperAgent

        while (!so.isGameOver() && so.getMoveCounter()<epiLength) {
            so.advance(pa.getNextAction2(so.partialState(), false, true));
        }

        if (verbose) {
            if (so.isGameOver())
                System.out.print("Solved cube with value " +value+  " in  " + so.getMoveCounter() + " twists.\n");
            else
                System.out.print("Could not solve cube with value " +value+  " in  " + epiLength + " twists.\n");
        }

        return so.getMoveCounter();
    } // solveCube

    protected static void setupPaths(String agtFile, String csvName){
        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;

        filePath = strDir + "/" + agtFile; //+ "tdntuple3.agt.zip";

        savePath = csvName.replaceAll("csv", "agt.zip");
        savePath = strDir + "/" + savePath;
    }

    @Test
    public void TestValueContainer() {
        ValueContainer vc = new ValueContainer(5);
        for (int i=0;i<vc.values.length; i++) vc.values[i]=i;
        vc.calcMeanStd();
        double myStd = Math.sqrt(10.0/(vc.values.length-1));
        assert(vc.mean==2);
        assert(vc.std==myStd);
    }


}
