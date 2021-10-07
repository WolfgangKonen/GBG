package games.EWN;


import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.EWN.config.ConfigEWN;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import params.ParMCTSE;
import params.ParMaxN;
import tools.ScoreTuple;

import java.util.ArrayList;

public class EvaluatorEWN extends Evaluator {
    private static final long serialVersionUID = 12L;

    private MCTSExpectimaxAgt mctse;
    private RandomAgent randomAgent;
    private RandomAgent randomAgent2;
    private RandomAgent randomAgent3;


    protected static ArrayList<StateObserverEWN> diffStartList = null;
    protected static int NPLY_DS = 4;


    public EvaluatorEWN(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
        super(e_PlayAgent, gb, 1, stopEval);
        initEvaluator();
    }

    public EvaluatorEWN(PlayAgent e_PlayAgent, GameBoard gb, int stopEval  , int mode) {
        super(e_PlayAgent, gb, mode,stopEval);
        initEvaluator();
    }

    public EvaluatorEWN(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
        initEvaluator();
    }

    public void initEvaluator() {
        ParMaxN params = new ParMaxN();
        int maxNDepth = 4;
        params.setMaxNDepth(maxNDepth);
        randomAgent = new RandomAgent("Random");
        randomAgent2 = new RandomAgent("Random");
        randomAgent3 = new RandomAgent("Random");
        ParMCTSE paramsMC = new  ParMCTSE();

        mctse = new MCTSExpectimaxAgt("MCTSE",paramsMC);

    }

    private static ArrayList<StateObserverEWN> createDifferentStartingPositions(StateObserverEWN so, int k){
        // Creating multiple instances for the different starting positions
        for(int i = 0; i < k; k++){
            // call the random start state of observer
            StateObserverEWN copy = (StateObserverEWN) so.copy();
            diffStartList.add(copy);
        }
        return diffStartList;
    }

    @Override
    protected boolean evalAgent(PlayAgent playAgent) {
        m_PlayAgent  = playAgent;

        if(diffStartList == null){
            StateObserverEWN so = new StateObserverEWN();
            diffStartList = new ArrayList<>();

        }

        switch (m_mode){
            case -1:
                m_msg = "No evaluation done";
                lastResult = 0.0;
                return false;
            case 0: switch(ConfigEWN.NUM_PLAYERS){
                case 2: return evalAgainstOpponent(m_PlayAgent, randomAgent,false, 100) > 0.5;
                case 3:return evalAgainstOpponent(m_PlayAgent, randomAgent,randomAgent2,false, 100) > 0.0;
                case 4:return evalAgainstOpponent(m_PlayAgent, randomAgent, randomAgent2, randomAgent3,false, 100) > 0.0;
            };
            case 1: return evalAgainstOpponent(m_PlayAgent, mctse, false,100) > 0.0;
            default: return false;
        }
    }

    /**
     *
     * @param playAgent agent to be evaluated
     * @param opponent agent to be played against
     * @param diffStarts if true
     * @param numEpisodes number of episodes during evaluation
     * @return {@code ScoreTuple} Tuple which holds the average score for {@playAgent} and {@opponent}
     */
    private double evalAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts, int numEpisodes){
        StateObservation so = m_gb.getDefaultStartState();
        // Weight stuff we maybe need this later
        int N = ConfigEWN.NUM_PLAYERS;
        ScoreTuple scMean = new ScoreTuple(N);
        if (diffStarts) 	// start from all start states in diffStartList
        {
            double sWeight = 1 / (double) (numEpisodes * diffStartList.size());
            int count=0;
            ScoreTuple sc;
            for (int c=0; c<numEpisodes; c++) {
                for (StateObservation sd : diffStartList) {
                    sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), sd, 100, 0);
                    scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
                    count++;
                }
            }
            System.out.println("count = "+ count);
        }else {
            scMean= XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,opponent), so, numEpisodes,0);
        }
        lastResult = scMean.scTup[0];
        m_msg = playAgent.getName() + ": " + getPrintString() + lastResult;
        return lastResult;
    }
    /**
     *
     * @param playAgent agent to be evaluated
     * @param opponent agent to be played against or with
     * @param opponent2 agent to be played against or with
     * @param diffStarts if true
     * @param numEpisodes number of episodes during evaluation
     * @return {@code ScoreTuple} Tuple which holds the average score for {@playAgent} and {@opponent}
     */
    private double evalAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, PlayAgent opponent2, boolean diffStarts, int numEpisodes){
        StateObservation so = m_gb.getDefaultStartState();
        // Weight stuff we maybe need this later
        int N = ConfigEWN.NUM_PLAYERS;
        ScoreTuple scMean = new ScoreTuple(N);
        if (diffStarts) 	// start from all start states in diffStartList
        {
            double sWeight = 1 / (double) (numEpisodes * diffStartList.size());
            int count=0;
            ScoreTuple sc;
            for (int c=0; c<numEpisodes; c++) {
                for (StateObservation sd : diffStartList) {
                    sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent, opponent2), sd, 100, 0);
                    scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
                    count++;
                }
            }
        }else {
            scMean= XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,opponent, opponent2), so, numEpisodes,0);
        }
        lastResult = scMean.scTup[0];
        m_msg = playAgent.getName() + ": " + getPrintString() + lastResult;
        return lastResult;
    }


    private double evalAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, PlayAgent opponent2,PlayAgent opponent3, boolean diffStarts, int numEpisodes){
        StateObservation so = m_gb.getDefaultStartState();
        // Weight stuff we maybe need this later
        int N = ConfigEWN.NUM_PLAYERS;
        ScoreTuple scMean = new ScoreTuple(N);
        if (diffStarts) 	// start from all start states in diffStartList
        {
            double sWeight = 1 / (double) (numEpisodes * diffStartList.size());
            int count=0;
            ScoreTuple sc;
            for (int c=0; c<numEpisodes; c++) {
                for (StateObservation sd : diffStartList) {
                    sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(new PlayAgent[]{playAgent,opponent, opponent2, opponent3}), sd, 100, 0);
                    scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
                    count++;
                }
            }
        }else {
            scMean= XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(new PlayAgent[]{playAgent,opponent, opponent2, opponent3}), so, numEpisodes,0);
        }
        lastResult = scMean.scTup[0];
        m_msg = playAgent.getName() + ": " + getPrintString() + lastResult;
        return lastResult;
    }
    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0,1};
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        switch (m_mode) {
            case -1:return "no evaluation done ";
            case 0: return "success against Random (best is 1.0): ";
            case 1: return "success against Mctse (best is 1.0): ";
            default:
                return null;

        }

    }

    @Override
    public String getTooltipString() {
        return "<html>-1: none<br>"
                + " 0: vs. Random, best is 1.0"
                + "1: vs. Mcts, best is 1.0"
                + "</html>";

    }

    @Override
    public String getPlotTitle() {
        switch (m_mode) {
            case 0:
                return "success against Random";
            case 1: return "success against Mcts";
            default:
                return null;
        }


    }
}