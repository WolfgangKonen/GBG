package games.Yavalath;

import controllers.MCTS.MCTSAgentT;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;

public class EvaluatorYavalath extends Evaluator {

    private RandomAgent randomAgent;
    private MaxNAgent maxNAgent;
    private MCTSAgentT mctsAgentT;
    protected static ArrayList<StateObserverYavalath> diffStartList;

    public EvaluatorYavalath(PlayAgent e_PlayAgent, GameBoard gb, int mode, int stopEval){
        super(e_PlayAgent,gb,mode,stopEval);
        initEvaluator();
    }

    public EvaluatorYavalath(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose){
        super(e_PlayAgent,gb,mode,stopEval,verbose);
        initEvaluator();
    }

    private void initEvaluator() {
        ParMaxN params = new ParMaxN();
        int maxNDepth = 1;
        params.setMaxNDepth(maxNDepth);
        randomAgent = new RandomAgent("Random");
        maxNAgent = new MaxNAgent("MaxN", params, new ParOther());
        mctsAgentT = new MCTSAgentT();

    }

    private ArrayList<StateObserverYavalath> addAll1PlyStates(ArrayList<StateObserverYavalath> diffStartList){
        StateObserverYavalath so = (StateObserverYavalath) m_gb.getDefaultStartState();
        for(int i=0;i<ConfigYavalath.BOARD_SIZE;i++){
            for(int j=0;j<ConfigYavalath.BOARD_SIZE;j++){
                if(Math.abs(j-i)>ConfigYavalath.BOARD_LENGTH){
                    break;
                }
                Types.ACTIONS a = new Types.ACTIONS(i*ConfigYavalath.BOARD_SIZE+j);
                StateObserverYavalath so_copy = so.copy();
                so_copy.advance(a);
                diffStartList.add(so_copy);

            }
        }
        diffStartList.add(so);
        return diffStartList;
    }

    @Override
    public boolean evalAgent(PlayAgent playAgent) {
        m_PlayAgent = playAgent;

        if(diffStartList==null){
            diffStartList = new ArrayList<StateObserverYavalath>();
            diffStartList = addAll1PlyStates(diffStartList);
        }

        switch(m_mode){
            case -1:
                m_msg = "No evaluation done ";
                lastResult = 0.0;
                return false;
            case 0: return evaluateAgainstOpponent(m_PlayAgent, randomAgent, false, 1) > 0.0;
            case 1: return evaluateAgainstOpponent(m_PlayAgent, maxNAgent, false, 1) > 0.0;
            case 2: return evaluateAgainstOpponent(m_PlayAgent, mctsAgentT,false,10) > 0.0;
            case 3: return evaluateAgainstOpponent(m_PlayAgent,maxNAgent,true,1) >0.0;
            case 4: return evaluateAgainstOpponent(m_PlayAgent, mctsAgentT,true, 1) > 0.0;
            default: return false;
        }

    }

    private double evaluateAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts, int numEpisodes) {
        StateObservation so = m_gb.getDefaultStartState();

        int n = m_gb.getStateObs().getNumPlayers();
        ScoreTuple scMean = new ScoreTuple(n);

        if(diffStarts){
            ScoreTuple sc;
            double scWeight = 1 / (double) diffStartList.size();
            for(StateObserverYavalath soYav : diffStartList){
                sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,opponent), soYav, numEpisodes,0);
                scMean.combine(sc, ScoreTuple.CombineOP.AVG,1,scWeight);
            }

        }else {
            scMean = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, numEpisodes, 0);
        }
        lastResult = scMean.scTup[0];
        m_msg = playAgent.getName()+": " +getPrintString() + lastResult;
        return  lastResult;

    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1,0,1,2,3,4};
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
        switch (m_mode){
            case -1: return "no evaluation done ";
            case 0:  return "success against Random (best is 1.0)";
            case 1: return "success against MaxN (best is 1.0)";
            case 2: return "success against MCTS (best is 1.0)";
            case 3: return "success against MaxN, diff starts (best is 1.0)";
            case 4: return "success aginst MCTS, diff starts (best is 1.0)";
            default: return null;
        }

    }

    @Override
    public String getTooltipString() {
        return "<html> -1: none<br>"
                + " 0: Random, best is 1.0<br>"
                + " 1: MaxN, best is 1.0<br>"
                + " 2: MCTS, best is 1.0<br>"
                + " 3: MaxN, diff starts, best is 1.0<br>"
                + " 4: MCTS, diff starts, best is 1.0<br>"
                +"</html>";
    }

    @Override
    public String getPlotTitle() {
        switch (m_mode){
            case 0: return "success against Random";
            case 1: return "success against MaxN";
            case 2: return "success against MCTS";
            case 3: return "success against MaxN, diff starts";
            case 4: return "success against MCTS, diff starts";
            default: return null;
        }
    }
}
