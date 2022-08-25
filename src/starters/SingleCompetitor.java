package starters;

import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import games.Arena;
import games.GameBoard;
import games.Othello.Edax.Edax2;
import games.StateObservation;
import games.XArenaFuncs;
import params.ParEdax;
import params.ParOther;
import tools.ScoreTuple;

import java.util.ArrayList;

class SingleCompetitor {

    protected ArrayList<MCompeteMWrap> mcList;
    protected double elapsedTime = 0.0;

    public SingleCompetitor() {
        this.mcList = new ArrayList<>();
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public PlayAgent doSingleCompetition(int i, PlayAgent pa, String agtFile, int iterMWrap, Arena arenaTrain,
                                         GameBoard gb, double userValue2, String csvName) {
        String userTitle1 = "time", userTitle2 = "user2";
        int numEpisodes;    // parameter for competeNPlayer
        int[] iterMWrapArr = (iterMWrap == 0) ? new int[]{0} : new int[]{0, iterMWrap};
        int[] depthArr = {1, 2, 3, 4, 5, 6, 7, 8, 9};       // Edax depth
        double[] epsArr = {1e-8}; //  {1e-8, 0.0};  //  {1e-8, 0.0, -1e-8}; //
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
                System.out.println("*** Starting run " + i + " multiCompete with Edax depth = " + edaxAgent.getParEdax().getDepth() + " ***");

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
