package games.RubiksCube;

import controllers.MC.MCAgentN;
import controllers.PlayAgent;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.Arena;
import games.Evaluator;
import games.StateObservation;
import params.MCParams;
import params.ParMC;
import starters.MCubeIterSweep;
import starters.SetupGBG;
import org.junit.Test;
import tools.Types;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class CubeTrain_Test {
    protected static Arena arenaTrain;
    protected static String filePath = null;
    protected static String savePath = null;
    protected Evaluator m_evaluatorQ = null;

    /**
     * An exemplary cube train and evaluation / analysis program:
     * <ol>
     *     <li> load an existing agent from agtFile and re-train it (if maxGameNum&gt;0) with CubeConfig.pMax=pMaxTrain
     *     <li> evaluate agent with quick-eval-mode 1
     *     <li> predict the value for a large number of states with given pTwist and collect results in HashMap hm.
     *      We want to know if states s with a true solution length larger than pMaxTrain (which were not seen during
     *      training) have automatically a predicted value V(s) smaller than for the trained levels.
     *      We accept in predict_value only states 'so' where the true length found by solveCube is not smaller than
     *      pTwist (in order to avoid pollution of vc.values with seemingly large values from lower-twisted cubes)
     *     <li> for a given pTwist=6 we check the HashMap entry: Do the cubes with value>0.5 have actually a solution
     *      length of 6 or larger (as it should with the acceptance policy in step 3)? - We find that this is true.
     *      (If we skip the acceptance policy in step 3, there are quite a number of states with solution length
     *      5 or smaller)
     * </ol>
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
        PlayAgent pa = trainCube(agtFile,filePath,maxGameNum, pMaxTrain, gb);

        // Step 2
        int qem = 1;
        arenaTrain.m_xab.oPar[0].setpMaxRubiks(pMaxEval);
        m_evaluatorQ = arenaTrain.makeEvaluator(pa,gb,0,qem,1);
        m_evaluatorQ.eval(pa);

        // Step 3
        int pMax=8;
        int nump=200;
        MCubeIterSweep mcis = new MCubeIterSweep();
        PrintWriter mtWriter = new PrintWriter(new OutputStreamWriter(System.out));
        HashMap<Integer, ValueContainer> hm = mcis.predict_value(pa,pMax,nump,gb,mtWriter);

        // Step 4
        int pTwist=6;
        int epiLength=20;
        ValueContainer vc = hm.get(pTwist);
        for (int n=0; n<vc.values.length; n++) {
            //if (vc.values[n]==vc.max.orElse(0)) {
            if (vc.values[n] >= 0.5) {
                StateObserverCube.solveCube(pa,vc.cubes[n],vc.values[n],epiLength,true);
            }
        }

    }

    /**
     * Another exemplary cube train and evaluation / analysis program:
     * <ol>
     *     <li> For existing agent from agtFile: only load (if maxGameNum==0) or load-and-retrain it (if maxGameNum&gt;0)
     *      with CubeConfig.pMax=pMaxTrain
     *     <li> evaluate agent with quick-eval-mode 1
     *     <li> predict the value for a large number of states with given pTwist and collect results in HashMap hm.
     *      We want to know if states s with a true solution length larger than pMaxTrain (which were not seen during
     *      training) have automatically a predicted value V(s) smaller than for the trained levels.
     *      We accept in predict_value only states 'so' where the true length found by solveCube is not smaller than
     *      pTwist (in order to avoid pollution of vc.values with seemingly large values from lower-twisted cubes)
     *     <li> for a given pTwist=6 we check the HashMap entry: Do the cubes with value>0.5 have actually a solution
     *      length of 6 or larger (as it should with the acceptance policy in step 3)? - We find that this is true.
     *      (If we skip the acceptance policy in step 3, there are quite a number of states with solution length
     *      5 or smaller)
     * </ol>
     */
    @Test
    public void valueFuncTest() {
        int maxGameNum= 0;   // 0; 100000;	3000000; 	// number of training episodes [0: no training, just load]
        int pMaxTrain =13;   // max number of twists during training
        int pMaxEval = 16;   // max number of twists during evaluation
        int epiLengthTrain=10;
        String selectedGame = "RubiksCube";
        String[] scaPar = SetupGBG.setDefaultScaPars(selectedGame);    // "2x2x2", "STICKER2", "HTM"
        scaPar[0] = "3x3x3"; // "2x2x2" or "3x3x3"
        scaPar[2] = "QTM";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame, scaPar,"",false,true);
        GameBoardCube gb = (GameBoardCube) arenaTrain.getGameBoard();

        String csvName = "mRubiks";
        String agtFile;
        if (scaPar[0].equals("2x2x2")) {
            agtFile = "TCL4-p16-3000k-60-7t-lam05.agt.zip";
        } else {
            agtFile = "TCL4-p13-3000k-120-7t.agt.zip";
            //agtFile = "TCL4-p13-ET16-3000k-120-7t-stub.agt.zip";
        }
        setupPaths(agtFile,csvName);		// builds filePath

        // Step 1: only load (maxGameNum==0) or load-and-retrain agent (if maxGameNum>0)
        PlayAgent pa;
        if (maxGameNum>0) System.out.println("Retrain agent for "+maxGameNum+" episodes ...");
        pa = trainCube(agtFile,filePath,maxGameNum, pMaxTrain, gb);

        // Step 2: evaluate agent
        int qem = 1;
        arenaTrain.m_xab.oPar[0].setpMaxRubiks(pMaxEval);
        arenaTrain.m_xab.oPar[0].setEpisodeLength(epiLengthTrain);
        m_evaluatorQ = arenaTrain.makeEvaluator(pa,gb,30,qem,1);
        m_evaluatorQ.eval(pa);

        // Step 3: check value function
        int nump=200;
        MCubeIterSweep mcis = new MCubeIterSweep();
        PrintWriter mtWriter = new PrintWriter(new OutputStreamWriter(System.out));
        if (pa instanceof TDNTuple4Agt) {
            TDNTuple4Agt ta = (TDNTuple4Agt) pa;
            mtWriter.println("\nValues for stepReward="+ta.getParTD().getStepReward()
                             +" and rewardPos="+ta.getParTD().getRewardPositive()+":\n");
        }
        //HashMap<Integer, ValueContainer> hm =
        mcis.predict_value(pa,pMaxEval,nump,gb,mtWriter);
        mtWriter.close();
    }

    /**
     * Load agent {@code pa} from {@code filePath}, adjust its parameters {@code maxGameNum, pMax}, retrain agent.
     * <p>
     *     WARNING: Agent {@code pa} is not re-constructed, training continues on the loaded agent!
     *
     * @param agtFile   the agent filename
     * @param filePath  full file path
     * @param maxGameNum how many episodes to train (if 0: do not retrain)
     * @param pMax      max. number of twists
     * @param gb        the game board, needed for chooseStartState
     * @return the trained agent
     */
    public PlayAgent trainCube(String agtFile, String filePath, int maxGameNum, int pMax, GameBoardCube gb) {

        // load agent to fill it with the appropriate parameter settings
        boolean res = arenaTrain.loadAgent(0, filePath);
        if (!res) {
            System.err.println("\n[trainCube] Aborted: Agent "+agtFile+" not found.");
            return null;
        }
        PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];

        return trainCube(pa,maxGameNum,pMax,gb);
    }

    /**
     * Take agent {@code pa}, adjust its parameters {@code maxGameNum, pMax} and retrain it
     * <p>
     *     WARNING: Agent {@code pa} is not re-constructed, training continues on the agent passed in!
     *
     * @param pa        the agent
     * @param maxGameNum how many episodes to train (if 0: do not train)
     * @param pMax      max. number of twists
     * @param gb        the game board, needed for chooseStartState
     * @return the trained agent
     */
    public PlayAgent trainCube(PlayAgent pa, int maxGameNum, int pMax, GameBoardCube gb) {


        long startTime = System.currentTimeMillis();

        if (maxGameNum!=-1) pa.setMaxGameNum(maxGameNum);   // if -1, take maxGameNum from loaded agent

        gb.initialize();                // this changes CubeConfig.pMin and .pMax and thus ...
        CubeConfig.pMax = pMax;         // ... we set CubeConfig.pMax, which is used in chooseStartState(pa), afterwards
        pa.setGameNum(0);
        while (pa.getGameNum()<pa.getMaxGameNum())
        {
            StateObservation so = gb.chooseStartState(pa);

            pa.trainAgent(so);

            if (pa.getGameNum() % pa.getParOther().getNumEval()==0)
                System.out.println("gameNum: "+pa.getGameNum());

        } // while

        double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[trainCube] training finished in "+elapsedTime+" sec. ");
        pa.incrementDurationTrainingMs((long)(elapsedTime*1000));

        return pa;
    } // trainCube

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

    @Test
    public void TestInstanceOf() {
        PlayAgent pa = null; //new MCAgentN(new ParMC());
        if (pa instanceof MCAgentN)
            System.out.println("instanceof MCAgentN");
        else
            System.out.println("a null pointer yields 'not instanceof'");
    }


}
