package games.EWN;

import java.io.FileWriter;
import java.io.IOException;


import controllers.*;
import controllers.PlayAgent.AgentState;
import games.*;
import params.ParMCTSE;
import starters.MTrainSweep;
import tools.Types;

public class Starter {

    private static Starter t_Batch = null;
    protected static Arena arenaTrain;
    protected static String filePath = null;
    protected static String savePath = null;
    protected MTrainSweep mTrainSweep;
    private PlayAgent agent;

    private int number_of_games = 10000;
    private int number_of_agents = 10;
    private double[] alpha_array = new double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
    private double[] alpha_final_array = new double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
    private double[] epsilon_array = new double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
    private double[] episolon_final_array = new double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
    private double[] lambda_array = new double[]{0.0,0.25,0.5,0.75,1.0};
    private double[] gamma_array = new double[]{0.0,0.01,0.02,0.05,0.1,0.2,0.4,0.8,0.99,1.0};

    private boolean[] TC = new boolean[]{true,false};

    private int[] TC_transfer = new int[]{0,1};
    private int[] TC_accumulate = new int[]{0,1};
    private double[] TC_INIT = new double[]{0.0001,0.0002,0.0004,0.0005,0.001,0.002,0.005,0.01,0.1};

    private boolean[] useBuffer = new boolean[]{true,false};
    private int buffersize_array[] = new int[]{15,30,60,120,240,720,3000};
    private int batchSize_array[] = new int[]{10,15,20,40,80,160};

    public static void main(String[] args) throws IOException {
        t_Batch = new Starter();
        String csvName = "Data.csv";
        ArenaEWN.setConfig("5x5 2-Player");
        ArenaEWN.setCellCoding("[0,1],[2,3],[4,5]");
        ArenaEWN.setRandomStartingPosition("True");
        arenaTrain = new ArenaEWN("", true, true);
        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
        String subDir = arenaTrain.getGameBoard().getSubDir();
        if (subDir != null) strDir += "/" + subDir;
        t_Batch.batch1();
        System.exit(0);
    }


    public Starter() {
        t_Batch = this;
        mTrainSweep = new MTrainSweep();
        ParMCTSE par = new ParMCTSE();
        agent = new RandomAgent("Random");
    }


    public void batch1() {
        for(int alpha = 0; alpha < alpha_array.length; alpha ++)
            for(int alpha_final= 0; alpha_final < alpha_final_array.length; alpha_final++)
                for(int epsilon_init = 0; epsilon_init < epsilon_array.length; epsilon_init++)
                    for (int epsilon_final = 0; epsilon_final < episolon_final_array.length; epsilon_final++)
                        for (int lambda = 0; lambda < lambda_array.length; lambda++)
                            for (int gamma = 0; gamma < gamma_array.length; gamma++)
                                for(int buffer = 0; buffer < useBuffer.length; buffer++)
                                    for(int bufferSize=0; bufferSize < (buffer == 0 ? buffersize_array.length : 1); bufferSize++)
                                        for(int batchSize = 0; batchSize < (buffer == 0 ? batchSize_array.length : 1); batchSize++){
                                            setXab(arenaTrain.m_xab,
                                                    alpha_array[alpha],
                                                    alpha_final_array[alpha_final],
                                                    epsilon_array[epsilon_init],
                                                    episolon_final_array[epsilon_final],
                                                    gamma_array[gamma],
                                                    lambda_array[lambda],
                                                    useBuffer[buffer],
                                                    buffersize_array[bufferSize],
                                                    batchSize_array[batchSize]
                                                    );
                                        }
    }




    private void setXab(XArenaButtons xab, double alpha, double alphaFinal, double epsilon, double epsilonFinal,double gamma,double lambda,
                                 boolean buffer, int bufferSize, int batchSize
    ){
        String agentName = "" +alpha +","+alphaFinal +","+epsilon +","+epsilonFinal +","+gamma +","+lambda +","+buffer +","+bufferSize +","+batchSize +"";
        xab.m_arena.taskState = Arena.Task.TRAIN;
        xab.tdPar[0].setAlpha(alpha);
        xab.tdPar[0].setAlphaFinal(alphaFinal);
        xab.tdPar[0].setEpsilon(epsilon);
        xab.tdPar[0].setEpsilonFinal(epsilonFinal);
        xab.tdPar[0].setGamma(gamma);
        xab.tdPar[0].setLambda(lambda);

        // replayBuffer
        xab.rbPar[0].setUseRB(buffer);
        if(buffer){
            xab.rbPar[0].setCapacity(bufferSize);
            xab.rbPar[0].setBatchSize(batchSize);
        }

        //others
        xab.tdPar[0].setFeatmode(0);
        xab.setSelectedAgent(0, "TD-Ntuple-4");
        xab.setGameNumber(number_of_games);
        xab.setTrainNumber(1);
        setupRun(xab,agentName);

    }

     private void setupRun(XArenaButtons xab,String agentName)
    {

        for(int i = 0; i < number_of_agents; i++){
            arenaTrain.m_xfun.m_PlayAgents[0] = arenaTrain.m_xfun.constructAgent(0, xab.getSelectedAgent(0), xab);
            arenaTrain.m_xfun.m_PlayAgents[0] = arenaTrain.m_xfun.train(0, xab.getSelectedAgent(0), xab, arenaTrain.getGameBoard());
            arenaTrain.m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
            try{
                System.out.println(agentName);
                LogtoCsv(arenaTrain.m_xfun.m_PlayAgents[0],agentName);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public PlayAgent getAgent(){
        return this.agent;
    }

    public void LogtoCsv(PlayAgent pa, String agentName) throws IOException{
        EvaluatorEWN eve = new EvaluatorEWN(pa,arenaTrain.getGameBoard(), 1);
        FileWriter pw = new FileWriter("./"+arenaTrain.getGameBoard().getSubDir()+"Result.csv",true);
        double r = eve.evaluateFree(pa,Starter.t_Batch.getAgent(), false,100);
        double r2 = eve.evaluateFree(Starter.t_Batch.getAgent(),pa, false,100);
        String result = agentName + "," + r + ","+r2+"\n";
        pw.append(result);
        pw.flush();
        pw.close();
    }

}
