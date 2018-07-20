package TournamentSystem;

import controllers.PlayAgent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.tc33.jheatchart.HeatChart;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TSAgentManager {
    public ArrayList<TSAgent> mAgents;
    private boolean lockedToCompete = false;
    private final String TAG = "[TSAgentManager] ";

    private int gamePlan[][] = null; // [ numGames ],[ [IDAgent1],[IDAgent2] ]
    private int gameResult[][] = null; // [ numGames ],[ [winAgent1],[tie],[winAgent2] ]
    public TSTimeStorage timeStorage[][] = null;
    private int nextGame = 0;
    private int numberOfGames = -1;

    public static final float faktorWin = 1.0f;
    public static final float faktorTie = 0.5f;
    public static final float faktorLos = 0.0f;

    public TSAgentManager() {
        mAgents = new ArrayList<>();
    }

    public void setNumberOfGames(int num) {
        numberOfGames = num;
    }

    public void addAgent(String name, String agent, JCheckBox checkbox, boolean hddAgent, PlayAgent playAgent) {
        if (!lockedToCompete)
            mAgents.add(new TSAgent(name, agent, checkbox, hddAgent, playAgent));
        else
            System.out.println(TAG+"ERROR :: manager is locked to compete, can not add new agent");
    }

    public int getNumAgents() {
        return mAgents.size();
    }

    public int getNumAgentsSelected() {
        int num = 0;
        for (TSAgent agent : mAgents)
        {
            if(agent.guiCheckBox.isSelected())
            {
                num++;
            }
        }
        return num;
    }

    public int getNumDiskAgents() {
        int num = 0;
        for (TSAgent t : mAgents)
            if (t.isHddAgent())
                num++;
        return num;
    }

    public String[] getNamesAgentsSelected() {
        String selectedAGents[] = new String[getNumAgentsSelected()]; // just selected agents
        int tmp = 0;
        for (TSAgent agent : mAgents) {
            if (agent.guiCheckBox.isSelected()) {
                selectedAGents[tmp++] = agent.getAgentType();
            }
        }
        return selectedAGents;
    }

    public String[][] getGamePlan() {
        int internalGamePlan[][] = generateGamePlanInternal();
        String gamePlan[][] = new String[internalGamePlan.length][2]; // games to be played

        for (int i=0; i<internalGamePlan.length; i++) {
            gamePlan[i][0] = mAgents.get(internalGamePlan[i][0]).getName();
            gamePlan[i][1] = mAgents.get(internalGamePlan[i][1]).getName();
        }
        return gamePlan;
    }

    private int[] getIDAgentsSelected() {
        int selectedAgents[] = new int[getNumAgentsSelected()]; // just selected agents
        int tmp = 0;
        for (int i=0; i<mAgents.size(); i++) {
            if (mAgents.get(i).guiCheckBox.isSelected()) {
                selectedAgents[tmp++] = i;
            }
        }
        return selectedAgents;
    }

    private int[][] generateGamePlanInternal() {
        int selectedAgents[] = getIDAgentsSelected();
        int gamePlan[][] = new int[getNumAgentsSelected()*(getNumAgentsSelected()-1)][2]; // games to be played
        int tmpGame = 0;
        for (int i=0; i<getNumAgentsSelected(); i++) {
            for (int j=0; j<getNumAgentsSelected(); j++) {
                if (i!=j) { // avoid agent to play against itself
                    gamePlan[tmpGame][0] = selectedAgents[i];
                    gamePlan[tmpGame++][1] = selectedAgents[j];
                }
            }
        }
        return gamePlan;
    }

    public void printGamePlan() {
        String gamePlan[][] = getGamePlan();
        System.out.println(TAG+"+ GamePlan Info: +");
        System.out.println(TAG+"Games to play: "+gamePlan.length);
        System.out.println(TAG+"each Game is run "+numberOfGames+" time(s)");
        for (String round[] : gamePlan)
            System.out.println(TAG+"["+round[0]+"] vs ["+round[1]+"]");
        System.out.println(TAG+"+ End Info +");
    }

    public TSAgent getAgent(String name) {
        for (TSAgent agnt : mAgents)
            if (agnt.getName().equals(name))
                return agnt;
        return null;
    }

    public boolean isLockedToCompete() {
        return lockedToCompete;
    }

    public void lockToCompete() {
        if (numberOfGames == -1) {
            System.out.println(TAG+"ERROR :: number of games was not set! using 1");
            numberOfGames = 1;
        }
        lockedToCompete = true;
        gamePlan = generateGamePlanInternal();
        gameResult = new int[gamePlan.length][3]; // is initialized with all zeros by JDK
        timeStorage = new TSTimeStorage[gamePlan.length][2];
        for (TSTimeStorage t[] : timeStorage) { // initialize all positions
            t[0] = new TSTimeStorage();
            t[1] = new TSTimeStorage();
        }
        nextGame = 0;
    }

    public TSAgent[] getNextCompetitionTeam() {
        TSAgent out[] = {mAgents.get(gamePlan[nextGame][0]), mAgents.get(gamePlan[nextGame][1])};
        return out;
    }

    public TSTimeStorage[] getNextCompetitionTimeStorage() {
        return timeStorage[nextGame];
    }

    public void enterGameResultWinner(int type) {
        if (!lockedToCompete) {
            System.out.println(TAG+"ERROR :: manager ist not locked, cannot enter result. run lockToCompete() first");
            return;
        }
        if (type<0 || type>2) {
            System.out.println(TAG + "ERROR :: enterGameResultWinner(int type) wrong value for type [0;2] = " + type);
            return;
        }
        else {
            gameResult[nextGame][type] = gameResult[nextGame][type] + 1;

            TSAgent teamPlayed[] = getNextCompetitionTeam(); // save individual win or loss to the tsagent objects in magents list
            if (type == 0){
                teamPlayed[0].addWonGame();
                teamPlayed[1].addLostGame();
            }
            if (type == 1){
                teamPlayed[0].addTieGame();
                teamPlayed[1].addTieGame();
            }
            if (type == 2){
                teamPlayed[0].addLostGame();
                teamPlayed[1].addWonGame();
            }

            timeStorage[nextGame][0].roundFinished();
            timeStorage[nextGame][1].roundFinished();
        }

        if (gameResult[nextGame][0]+gameResult[nextGame][1]+gameResult[nextGame][2] == numberOfGames)
            nextGame++;
    }

    public boolean hastNextGame() {
        if (nextGame==gamePlan.length)
            return false;
        else
            return true;
    }

    public void printGameResults() {
        if (gamePlan.length != gameResult.length) {
            System.out.println(TAG+"printGameResults() failed - gamePlan.length != gameResult.length");
            return;
        }
        System.out.println(TAG+"Info on individual games:");
        for (int i=0; i<gamePlan.length; i++) {
            System.out.print(TAG);
            System.out.print("Team: ");
            //System.out.print("["+gamePlan[i][0]+"] vs ["+gamePlan[i][1]+"] || ");
            System.out.print("["+mAgents.get(gamePlan[i][0]).getName()+"] vs ["+mAgents.get(gamePlan[i][1]).getName()+"] || ");
            System.out.print("Res.: Win1: "+gameResult[i][0]+" Tie: "+gameResult[i][1]+" Win2: "+gameResult[i][2]+" || ");
            System.out.print("Agt.1 average Time MS: "+timeStorage[i][0].getAverageTimeForGameMS()+" ");
            System.out.print("Agt.2 average Time MS: "+timeStorage[i][1].getAverageTimeForGameMS()+" ");
            System.out.print("");
            System.out.println();
        }
        System.out.println(TAG+"Info on individual Agents:");
        int[] selectedAgents = getIDAgentsSelected();
        for (int id : selectedAgents) {
            TSAgent a = mAgents.get(id);
            System.out.print(TAG);
            System.out.print("AgentName: "+a.getName()+" ");
            System.out.print("GamesWon: "+a.getCountWonGames()+" GamesTie: "+a.getCountTieGames()+" GamesLost: "+a.getCountLostGames()+" | ");
            System.out.print("AgentScore: "+a.getAgentScore());
            System.out.println();
        }
    }

    public void unlockAndClear() {
        lockedToCompete = false;
        gamePlan = null;
        gameResult = null;
        timeStorage = null;
        nextGame = 0;
    }

    /**
     *  +++ STATISTIK +++
     */

    public void makeStats() {
        // http://www.codejava.net/java-se/swing/a-simple-jtable-example-for-display
        /**
         * Table | WTL und Score
         */
        // headers for the table
        String agenten[] = getNamesAgentsSelected();
        String[] columnNames1 = new String[agenten.length+1]; //{ "Y vs X"//, "Agent#1", "Agent#2", "Agent#3" };
        columnNames1[0] = "Y vs X";
        System.arraycopy(agenten, 0, columnNames1, 1, agenten.length);

        final String empty = "null";
        int game = 0;
        Object[][] rowData1 = new Object[getNumAgentsSelected()][getNumAgentsSelected()+1];
        Object[][] rowData3 = new Object[getNumAgentsSelected()][getNumAgentsSelected()+1];
        double[][] rowDataHM = new double[getNumAgentsSelected()][getNumAgentsSelected()];
        for (int i=0; i<getNumAgentsSelected(); i++) {
            rowData1[i][0] = getNamesAgentsSelected()[i];
            rowData3[i][0] = getNamesAgentsSelected()[i];
            for (int j=0; j<getNumAgentsSelected(); j++) {
                if (i==j) {
                    rowData1[i][j+1] = empty;
                    rowData3[i][j+1] = empty;
                    rowDataHM[i][j] = -1;
                }
                else {
                    rowData1[i][j+1] = "W:"+gameResult[game][0]+" | T:"+gameResult[game][1]+" | L:"+gameResult[game][2];
                    float score = 0;
                    score += gameResult[game][0] * faktorWin;
                    score += gameResult[game][1] * faktorTie;
                    score += gameResult[game][2] * faktorLos;
                    rowData3[i][j+1] = ""+score;
                    rowDataHM[i][j] = score;
                    game++;
                }
            }
        }

        //create table with data
        JTable tableMatrixWTL = new JTable(rowData1, columnNames1);
        DefaultTableModel defTableMatrixWTL = new DefaultTableModel(rowData1, columnNames1);
        JTable tableMatrixSCR = new JTable(rowData3, columnNames1);
        DefaultTableModel defTableMatrixSCR = new DefaultTableModel(rowData3, columnNames1);

        /**
         * Score Heatmap
         */
        // create Score HeatMap
        HeatChart map = new HeatChart(rowDataHM);
        //map.setTitle("white = worst | black = best");
        //map.setXAxisLabel("X Axis");
        //map.setYAxisLabel("Y Axis");
        //Object[] tmpX = {"Agent1","Agent2","Agent3","Agent4","Agent5","Agent6"};
        //map.setXValues(tmpX);
        //Object[] tmpY = {"Agent1","Agent2","Agent3","Agent4"};
        //map.setYValues(tmpY);
        Object[] agentNames = getNamesAgentsSelected();
        map.setXValues(agentNames);
        map.setYValues(agentNames);
        map.setCellSize(new Dimension(25,25));
        //map.setTitleFont();
        Image hm = map.getChartImage();

        /**
         * Agent Score Table
         */
        String[] columnNames4 = {
                "Rank",
                "Agent",
                "Games Won",
                "Games Tie",
                "Games Lost",
                "WTL Score"
        };
        Object[][] rowData4 = new Object[getNumAgentsSelected()][columnNames4.length];
        TSAgent[] rankAgents = new TSAgent[getNumAgentsSelected()];
        int[] selectedAgents = getIDAgentsSelected();
        for (int i=0; i<selectedAgents.length; i++) {
            rankAgents[i] = mAgents.get(selectedAgents[i]);
        }

        // sort rankAgent array by agent WTL score
        /*
        Arrays.sort(rankAgents, new Comparator<TSAgent>() {
            @Override
            public int compare(final TSAgent entry1, final TSAgent entry2) {
                if (entry1.getAgentScore()>entry2.getAgentScore())
                    return -1;
                if (entry1.getAgentScore()<entry2.getAgentScore())
                    return +1;
                return 0;
            }
        });
        */
        Arrays.sort(rankAgents, (entry1, entry2) -> { // same as above
            if (entry1.getAgentScore()>entry2.getAgentScore())
                return -1;
            if (entry1.getAgentScore()<entry2.getAgentScore())
                return +1;
            return 0;
        });

        // put data into table
        for (int i=0; i<rowData4.length; i++) {
            // "Rank",
            rowData4[i][0] = ""+(i+1);
            // "Agent",
            rowData4[i][1] = rankAgents[i].getName();
            // "Games Won",
            rowData4[i][2] = rankAgents[i].getCountWonGames();
            // "Games Tie",
            rowData4[i][3] = rankAgents[i].getCountTieGames();
            // "Games Lost",
            rowData4[i][4] = rankAgents[i].getCountLostGames();
            // "WTL Score"
            rowData4[i][5] = rankAgents[i].getAgentScore();
        }

        //create table with data
        JTable tableAgentScore = new JTable(rowData4, columnNames4);
        DefaultTableModel defTableAgentScore = new DefaultTableModel(rowData4, columnNames4);
        // center align column entries
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)tableAgentScore.getDefaultRenderer(Object.class);
        renderer.setHorizontalAlignment( JLabel.CENTER );

        /**
         * Scatterplot AgentScore vs Time
         */
        // https://www.boraji.com/jfreechart-scatter-chart-example
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        int[] selectedAgents2 = getIDAgentsSelected();
        for (int i=0; i<selectedAgents2.length; i++) {
            TSAgent tmp = mAgents.get(selectedAgents2[i]);
            ArrayList<Double> medianTimes = new ArrayList<>();
            double median;

            for (int gms=0; gms<timeStorage.length; gms++) { // spiele
                for (int cpl=0; cpl<timeStorage[0].length; cpl++) { // hin+rückrunde
                    for (int agt=0; agt<2; agt++) { // agent 1+2
                        if (gamePlan[gms][cpl] == selectedAgents2[i]) {
                            medianTimes.add(timeStorage[gms][cpl].getMedianRoundTimeMS());
                        }
                    }
                }
            }

            double[] tmpD = new double[medianTimes.size()];

            for (int j=0; j<medianTimes.size(); j++)
                tmpD[j] = medianTimes.get(j);

            Arrays.sort(tmpD);

            if (tmpD.length % 2 == 0)
                median = (tmpD[tmpD.length/2] + tmpD[tmpD.length/2 - 1])/2;
            else
                median = tmpD[tmpD.length/2];

            XYSeries series1 = new XYSeries(tmp.getName());
            series1.add(median, tmp.getAgentScore());
            dataset.addSeries(series1);
        }

        // Create chart
        JFreeChart chart = ChartFactory.createScatterPlot(
                "", "Median Round Time [ms]", "Agent Score [WTL]", dataset);

        //Changes background color
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(230, 230, 230));

        // Create Panel
        ChartPanel scatterPlotASvT = new ChartPanel(chart);
        scatterPlotASvT.setPreferredSize(new Dimension(400,300)); // plot size
        //setContentPane(panel);

        /**
         * Table | Zeiten
         */
        // headers for the table
        String[] columnNames2 = {
                "Spiel",
                "Agent Name",
                "Agent Typ",
                "schnellster Zug",
                "langsamster Zug",
                //"durchschnittliche Zeit",
                "drchschn. Zug",
                "median Zug",
                "drchschn. Runde",
                "median Runde"
        };

        final int numAgentsPerRound = 2;
        Object[][] rowData2 = new Object[gameResult.length*numAgentsPerRound][columnNames2.length];
        int pos = 0;
        for (int i=0; i<gameResult.length; i++) {
            for (int j=0; j<numAgentsPerRound; j++) {
                // "Spiel"
                rowData2[pos][0] = ""+(i+1);
                // "Agent Name"
                rowData2[pos][1] = mAgents.get(gamePlan[i][j]).getName();
                // "Agent Typ"
                rowData2[pos][2] = mAgents.get(gamePlan[i][j]).getAgentType();
                // "schnellster Zug"
                rowData2[pos][3] = ""+timeStorage[i][j].getMinTimeForGameMS()+"ms";
                // "langsamster Zug"
                rowData2[pos][4] = ""+timeStorage[i][j].getMaxTimeForGameMS()+"ms";
                // "durchschnittliche Zeit Zug"
                rowData2[pos][5] = ""+timeStorage[i][j].getAverageTimeForGameMS()+"ms";
                // "median Zeit Zug"
                rowData2[pos][6] = ""+timeStorage[i][j].getMedianTimeForGameMS()+"ms";
                // "durchschnittliche Zeit Runde"
                rowData2[pos][7] = ""+timeStorage[i][j].getAverageRoundTimeMS()+"ms";
                // "median Zeit Runde"
                rowData2[pos][8] = ""+timeStorage[i][j].getMedianRoundTimeMS()+"ms";

                pos++;
            }
        }

        //create table with data
        JTable tableTimeDetail = new JTable(rowData2, columnNames2);
        DefaultTableModel defTableTimeDetail = new DefaultTableModel(rowData2, columnNames2);
        // right align column entries
        DefaultTableCellRenderer renderer2 = (DefaultTableCellRenderer)tableTimeDetail.getDefaultRenderer(Object.class);
        renderer2.setHorizontalAlignment( JLabel.RIGHT );

        /*
        // add the table to the frame
        JFrame frame = new JFrame();
        Container c  = frame.getContentPane();
        c.setLayout(new GridLayout(5,0));
        c.add(new JScrollPane(tableMatrixWTL));
        c.add(new JScrollPane(tableMatrixSCR));
        c.add(new JScrollPane(new JLabel(new ImageIcon(hm))));
        c.add(new JScrollPane(tableAgentScore));
        c.add(new JScrollPane(tableTimeDetail));

        frame.setTitle("Tournament Statistics");
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000,1000);
        //frame.pack();
        frame.setVisible(true);
        */

        /**
         * TS Results in a window
         */
        TSResultWindow mTSRW = new TSResultWindow(defTableMatrixWTL, defTableMatrixSCR, defTableAgentScore, defTableTimeDetail, new ImageIcon(hm), scatterPlotASvT);
    }

}
