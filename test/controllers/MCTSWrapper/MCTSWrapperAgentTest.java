package controllers.MCTSWrapper;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.stateApproximation.Approximator;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.*;
import games.CFour.GameBoardC4;
import games.Othello.GameBoardOthello;
import starters.GBGBatch;
import starters.MCompeteMWrap;
import org.junit.Test;
import params.ParMCTS;
import params.ParOther;
import tools.ScoreTuple;

import java.util.ArrayList;

public class MCTSWrapperAgentTest extends GBGBatch {
    String selectedGame;
    String[] scaPar;

    /**
     * Test the performance of MCTSWrapperAgent on ConnectFour: We run multi-compete episodes
     *     MCTS(iter=10000,treeDepth=40)    vs.    MCTSWrapper[agtFile](iter=iterMWrap, EPS \in {1e-8,0})
     * where MCTSWrapper plays 2nd. [If it plays 1st, it will always win, this is uninteresting.]
     * The win rates are tested against certain expectations:
     *     1) If iterMCTSWrap==0, then winrate[MCTSWrapper] <= 0.4 (not strict, sometimes only <= 0.6), for both EPS
     *     2) If iterMCTSWrap>=300, then winrate[MCTSWrapper] >= 0.95, for both EPS
     *  Results are written to mCompeteMCTS-vs-MWrap.csv for inspection.
     *  Computation time <400 sec for 8 iterMWrap times 2 EPS values and 10 episodes in each multi-compete.
     */
    @Test
    public void mctsC4Test() {
        long startTime;
        double elapsedTime;
        startTime = System.currentTimeMillis();

        innerMctsC4Test();

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[mctsC4Test] "+elapsedTime+" sec.");
    }

    public void innerMctsC4Test() {
        selectedGame = "ConnectFour";
        scaPar = GBGBatch.setDefaultScaPars(selectedGame);
        String csvFile = "mCompeteMCTS-vs-MWrap.csv";
        String[] agtFiles = {"TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip"};
        PlayAgent pa;
        PlayAgent qa;
        PlayAgent opponent;
        double[] epsArr = {1e-8, 0.0}; // {1e-8, 0.0};    // {1e-8, 0.0, -1.0};
        //double[] cpuctArr = {1.0}; //{0.2, 0.4, 0.6, 0.8, 1.0, 1.4, 2.0, 4.0, 10.0}; // {1.0};
        String userTitle1 = "user1", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        double winrate;     // the win rate of MCTSWrapperAgent
        double c_puct = 1.0;
        int numEpisodes=10;
        int[] iterMCTSWrapArr={0,50,100,200,300,500,600,800,1000};

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardC4 gb = new GameBoardC4(t_Game);		// needed for chooseStartState()

        ParMCTS parMCTS = new ParMCTS();
        parMCTS.setNumIter(10000);
        parMCTS.setTreeDepth(40);
        ParOther oPar = new ParOther();
        StateObservation so = gb.getDefaultStartState();
        opponent = new MCTSAgentT("MCTS",so, parMCTS, oPar);

        for (String agtFile : agtFiles) {
            setupPaths(agtFile, csvFile);     // builds filePath
            boolean res = t_Game.loadAgent(0, filePath);
            assert res : "\n[MCTSWrapperAgentTest] Aborted: agtFile = " + agtFile + " not found!";

            String sAgent = t_Game.m_xab.getSelectedAgent(0);
            pa = t_Game.m_xfun.fetchAgent(0, sAgent, t_Game.m_xab);

            for (double EPS : epsArr) {
                ConfigWrapper.EPS = EPS;
                //numEpisodes = (EPS<0) ? 10 : 1;
                for (int iterMCTSWrap : iterMCTSWrapArr) {
                    if (iterMCTSWrap == 0) qa = pa;
                    else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                            new PlayAgentApproximator(pa),
                            "MCTS-Wrapped " + pa.getName(),
                            -1);

                    ScoreTuple sc;
                    PlayAgtVector paVector = new PlayAgtVector(qa, opponent);
                    for (int p_MWrap : new int[]{1})       // {0,1}
                    {     // p_MWrap: whether MCTSWrapper is player 0 or player 1
                        sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MWrap), so, numEpisodes, 0, null);
                        winrate = (sc.scTup[p_MWrap] + 1) / 2;
                        System.out.println("(iterMW,EPS,p_MWrap) = (" + iterMCTSWrap + "," + EPS + "," + p_MWrap + "): " +
                                "  winrate = " + winrate);
                        mCompete = new MCompeteMWrap(0, numEpisodes, 0, iterMCTSWrap,
                                EPS, p_MWrap, c_puct, winrate,
                                userValue1, userValue2);
                        mcList.add(mCompete);
                        if (p_MWrap==1 && iterMCTSWrap==0) assert(winrate<=0.6) : "winrate is not <0.4 for iter==0!";
                        if (p_MWrap==1 && iterMCTSWrap>=500) assert(winrate>=0.95) : "winrate is not >0.95 for iter>=500!";
                    } // for (p_MWrap)

                    // print the full list mcList after finishing each  (p_MCTS)
                    // (overwrites the file written from previous (p_MCTS))
                    MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, t_Game, userTitle1, userTitle2);
                } // for (iterMCTSWrap)
            } // for (EPS)
        } // for (k)
    }

    /**
     * Measure whether getVTable2For is faster than getVTableFor.
     * Result: Is 5x faster, but has only negligible impact, because seldom called.
     */
    @Test
    public void getVTable_Test() {
        selectedGame = "Othello";
        scaPar = GBGBatch.setDefaultScaPars(selectedGame);
        t_Game = GBGBatch.setupSelectedGame(selectedGame, scaPar);   // t_Game is ArenaTrain object
        GameBoard gb = new GameBoardOthello(t_Game);        // needed for chooseStartState()
        StateObservation sob = gb.getDefaultStartState();
        MCTSNode mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
        Approximator approximator = new PlayAgentApproximator(new RandomAgent("rand"));
        MCTS mcts = new MCTS(approximator, 1.0, 50);
        MCTSWrapperAgent mwa = new MCTSWrapperAgent(100,1.0,approximator,"",50);
        int ncalls = 100000;
        long startTime;
        double elapsedTime;

        for (int i = 0; i < 100; i++) {
            mcts.search(mctsNode,0);
        }

        startTime = System.currentTimeMillis();
        for (int i = 0; i < ncalls; i++) {
            mwa.getVTable2For(mctsNode);
        }
        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[getVTable2For] "+elapsedTime+" sec.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i < ncalls; i++) {
            mwa.getVTableFor(mctsNode);
        }
        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[getVTableFor] "+elapsedTime+" sec.");

    }

}