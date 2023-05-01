package starters;

import controllers.PlayAgent;
import games.*;
import games.RubiksCube.StateObserverCube;
import tools.Measure;

import java.util.ArrayList;

public class SingleTrainer {

    protected ArrayList<MTrain> mtList;
    protected boolean doTrainEvaluation;
    protected Measure oQ, oT;
    protected Evaluator m_evaluatorQ = null;
    protected Evaluator m_evaluatorT = null;

    public SingleTrainer() {
        this.mtList = new ArrayList<>();         // needed for doSingleTraining
        this.oQ = new Measure();                 //
        this.oT = new Measure();                 //
    }

    public PlayAgent doSingleTraining(int n, int i, PlayAgent pa, Arena arenaTrain, XArenaButtons xab,
                                      GameBoard gb, int maxGameNum, double userValue1, double userValue2) { //throws IOException  {
        int numEval = xab.oPar[n].getNumEval();
        int gameNum;
        long actionNum, trnMoveNum;
        double totalTrainSec, elapsedTime;
        long elapsedMs;
        int stopEval = 0;
        MTrain mTrain;
        EvalResult eresQ, eresT;
        double evalQ, evalT = 0.0;

        // Construct agent anew with the settings of xab. Necessary to build a new set of random n-tuples in each run
        // (if random n-tuple creation is specified)
        try {
            String sAgent = xab.getSelectedAgent(n);
            pa = arenaTrain.m_xfun.constructAgent(n, sAgent, xab);
            pa = arenaTrain.m_xfun.wrapAgentTrain(pa, pa.getParOther(), pa.getParWrapper(), null, gb.getDefaultStartState());
            if (pa == null) throw new RuntimeException("Could not construct AgentX = " + sAgent);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return pa;
        }


        int qem = xab.oPar[n].getQuickEvalMode();
        m_evaluatorQ = xab.m_arena.makeEvaluator(pa, gb, qem, 1);
        //
        // doTrainEvaluation flags whether Train Evaluator is executed:
        // Evaluator m_evaluatorT is only constructed and evaluated, if in tab 'Other pars'
        // the choice box 'Train Eval Mode' is not -1 ("none").
        int tem = xab.oPar[n].getTrainEvalMode();
        doTrainEvaluation = (tem != -1);
        if (doTrainEvaluation)
            m_evaluatorT = xab.m_arena.makeEvaluator(pa, gb, tem, 1);

        System.out.println(pa.stringDescr());
        System.out.println(pa.stringDescr2());

        if (maxGameNum != -1) pa.setMaxGameNum(maxGameNum);   // if -1, take maxGameNum from loaded agent
        pa.setGameNum(0);
        long startTime = System.currentTimeMillis();
        gb.initialize();
        while (pa.getGameNum() < pa.getMaxGameNum()) {
            StateObservation so = (pa.getParOther().getChooseStart01())
                    ? gb.chooseStartState(pa) : gb.getDefaultStartState();

            pa.trainAgent(so);

            gameNum = pa.getGameNum();
            int liveSignal = (so instanceof StateObserverCube) ? 10000 :
                    (!pa.isWrapper()) ? 500 : 50;
            if (gameNum % liveSignal == 0) {
                int exploMode = pa.getParWrapper().getWrapperMCTS_ExplorationMode();	// just as sanity check
                System.out.println("gameNum: "+gameNum+"   EX"+exploMode);
            }
            if (gameNum % numEval == 0) { //|| gameNum==1) {
                elapsedMs = (System.currentTimeMillis() - startTime);
                pa.incrementDurationTrainingMs(elapsedMs);
                elapsedTime = (double) elapsedMs / 1000.0;
                // elapsedTime: time [sec] for the last numEval training games
                System.out.println(pa.printTrainStatus() + ", " + elapsedTime + " sec");
                startTime = System.currentTimeMillis();

                xab.setGameNumber(gameNum);

                eresQ = m_evaluatorQ.eval(pa);
                evalQ = eresQ.getResult();
                if (doTrainEvaluation) {
                    eresT = m_evaluatorT.eval(pa);
                    evalT = eresT.getResult();
                }

                // gather information for later printout to agents/gameName/csv/multiTrain.csv.
                actionNum = pa.getNumLrnActions();
                trnMoveNum = pa.getNumTrnMoves();
                totalTrainSec = (double) pa.getDurationTrainingMs() / 1000.0;
                // totalTrainSec = time [sec] needed since start of training
                // (only self-play, excluding evaluations)
                mTrain = new MTrain(i, gameNum, evalQ, evalT,
                        actionNum, trnMoveNum, totalTrainSec, actionNum / totalTrainSec,
                        userValue1, userValue2);
                mtList.add(mTrain);

                elapsedMs = (System.currentTimeMillis() - startTime);
                pa.incrementDurationEvaluationMs(elapsedMs);

                startTime = System.currentTimeMillis();
            }    // if (gameNum%numEval==0 )
        } // while


        eresQ = m_evaluatorQ.eval(pa);
        evalQ = eresQ.getResult();
        oQ.add(evalQ);
        if (doTrainEvaluation) {
            eresT = m_evaluatorT.eval(pa);
            evalT = eresT.getResult();
            oT.add(evalT);
        }

        elapsedMs = (System.currentTimeMillis() - startTime);
        pa.incrementDurationEvaluationMs(elapsedMs);
        pa.setAgentState(PlayAgent.AgentState.TRAINED);

        return pa;
    }

    public ArrayList<MTrain> getMtList() {
        return this.mtList;
    }

    public Measure getOQ() { return this.oQ; }
    public Measure getOT() { return this.oT; }
    public boolean getDoTrainEval() { return doTrainEvaluation; }
}
