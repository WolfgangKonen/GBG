package TournamentSystem;

import TournamentSystem.Scoring.Elo.EloCalculator;
import TournamentSystem.Scoring.Glicko2.Glicko2RatingCalculator;
import TournamentSystem.Scoring.Glicko2.Glicko2RatingPeriodResults;
import controllers.PlayAgent;
import games.XArenaMenu;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This is the core of the GBG Tournament System.
 * This class manages the procedure, agents and data handling and is called from {@link TSSettingsGUI2}.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSAgentManager {
    private final String TAG = "[TSAgentManager] ";
    public TSResultStorage results;
    public JTextField gameNumJTF, numOfMovesJTF;
    private Glicko2RatingCalculator glicko2RatingSystem;
    private Glicko2RatingPeriodResults glicko2Results;
    private int gamesPlayed;
    private boolean autoSaveAfterTS;

    public static final float faktorWin = 1.0f;
    public static final float faktorTie = 0.5f;
    public static final float faktorLos = 0.0f;

    public TSAgentManager() {
        results = new TSResultStorage();
        results.mAgents = new ArrayList<>();

        glicko2RatingSystem = new Glicko2RatingCalculator(0.06, 0.5); // todo values?
        glicko2Results = new Glicko2RatingPeriodResults();

        gamesPlayed = 0;
        autoSaveAfterTS = false;
    }

    /**
     * save the start date to the result storage for result visualization
     */
    public void setResultsStartDate() {
        results.startDate = "Tournament Start Date: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * en/disable automatically saving the tournament results to disk after TS is done
     * @param b true:enable, false:disable (standard:disabled)
     */
    public void setAutoSaveAfterTS(boolean b) {
        autoSaveAfterTS = b;
    }

    /**
     * did the user enable auto save of results after TS is done
     * @return boolean if auto save enabled after TS
     */
    public boolean getAutoSaveAfterTS() {
        return autoSaveAfterTS;
    }

    /**
     * set number of rounds per game to be played by every pair of agents
     * @param num number of rounds per game
     */
    public void setNumberOfGames(int num) {
        results.numberOfGames = num;
    }

    /**
     * add a new agent to the tournament
     * @param name agent name
     * @param agent agent type
     * @param checkbox JCheckBox used in {@link TSSettingsGUI2}
     * @param hddAgent {@code true} if the agent was loaded from disk
     * @param playAgent instance of the hdd agent, {@code null} if its a standard agent
     */
    public void addAgent(String name, String agent, JCheckBox checkbox, boolean hddAgent, PlayAgent playAgent) {
        if (!results.lockedToCompete)
            results.mAgents.add(new TSAgent(name, agent, checkbox, hddAgent, playAgent, glicko2RatingSystem));
        else
            System.out.println(TAG+"ERROR :: manager is locked to compete, can not add new agent");
    }

    /**
     * number of agents already added to tournament
     * @return number of agents as int
     */
    public int getNumAgents() {
        return results.mAgents.size();
    }

    /**
     * disables all checkboxes in {@link TSSettingsGUI2}, to be used before start of tournament
     */
    public void disableAllAgentCheckboxen() {
        for (TSAgent tsa : results.mAgents)
            tsa.guiCheckBox.setEnabled(false);
        gameNumJTF.setEnabled(false);
        numOfMovesJTF.setEnabled(false);
    }

    /**
     * enables all checkboxes in {@link TSSettingsGUI2}, to be used after end of tournament
     */
    public void enableAllAgentCheckboxen() {
        for (TSAgent tsa : results.mAgents)
            tsa.guiCheckBox.setEnabled(true);
        gameNumJTF.setEnabled(true);
        numOfMovesJTF.setEnabled(true);
    }

    /**
     * get the number of agents selected via checkbox in {@link TSSettingsGUI2}
     * @return number of selected agents
     */
    public int getNumAgentsSelected() {
        int num = 0;
        for (TSAgent agent : results.mAgents)
        {
            if(agent.guiCheckBox.isSelected())
            {
                num++;
            }
        }
        return num;
    }

    /**
     * get the number of agents loaded from disk selected via checkbox in {@link TSSettingsGUI2}
     * @return number of selected hdd agents
     */
    public int getNumDiskAgents() {
        int num = 0;
        for (TSAgent t : results.mAgents)
            if (t.isHddAgent())
                num++;
        return num;
    }

    /**
     * get the names of the selected agents in {@link TSSettingsGUI2}
     * @return string array with names of selected agents
     */
    public String[] getNamesAgentsSelected() {
        String selectedAGents[] = new String[getNumAgentsSelected()]; // just selected agents
        int tmp = 0;
        for (TSAgent agent : results.mAgents) {
            if (agent.guiCheckBox.isSelected()) {
                selectedAGents[tmp++] = agent.getAgentType();
            }
        }
        return selectedAGents;
    }

    /**
     * set if all HDD agents in {@link TSSettingsGUI2} should be selected
     * @param selected selection state of hdd agent checkboxes
     */
    public void setAllHDDAgentsSelected(boolean selected) {
        for (TSAgent agent : results.mAgents) {
            if (agent.isHddAgent()) {
                agent.guiCheckBox.setSelected(selected);
            }
        }
    }

    /**
     * delete all HDD agents with a selected checkbox from tournament
     */
    public void deleteAllHDDAgentsSelected() {
        int numAgents = getNumDiskAgents();
        Iterator<TSAgent> i = results.mAgents.iterator();
        while (i.hasNext()) {
            TSAgent a = i.next();
            if (a.isHddAgent()) {
                if (a.guiCheckBox.isSelected()) {
                    a.guiCheckBox.setVisible(false);
                    System.out.println(TAG+"Deleted Agent "+a.getName());
                    i.remove();
                }
            }
        }
        System.out.println(TAG+"Number of Disk Agents was reduced from "+numAgents+" to "+getNumDiskAgents());
    }

    /**
     * get the gameplan with all agent pairs playing against each other with their names in a string array
     * @return string array with the names of agents playing against each other
     */
    public String[][] getGamePlan() {
        int internalGamePlan[][] = generateGamePlanInternal();
        String gamePlan[][] = new String[internalGamePlan.length][2]; // games to be played

        for (int i=0; i<internalGamePlan.length; i++) {
            gamePlan[i][0] = results.mAgents.get(internalGamePlan[i][0]).getName();
            gamePlan[i][1] = results.mAgents.get(internalGamePlan[i][1]).getName();
        }
        return gamePlan;
    }

    /**
     * get the IDs of the agents selected. the ID represents the agents position in arraylist {@code TSResultStorage.mAgents}
     * @return arraylist positions of selected agents in {@code TSResultStorage.mAgents}
     */
    private int[] getIDAgentsSelected() {
        int selectedAgents[] = new int[getNumAgentsSelected()]; // just selected agents
        int tmp = 0;
        for (int i=0; i<results.mAgents.size(); i++) {
            if (results.mAgents.get(i).guiCheckBox.isSelected()) {
                selectedAgents[tmp++] = i;
            }
        }
        return selectedAgents;
    }

    /**
     * generates the gameplan and playing pairs of agents identified by the ID (position in mAgent Arraylist)
     * @return gameplan with agent IDs
     */
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

    /**
     * print the gameplan with agent names to the console
     */
    public void printGamePlan() {
        String gamePlan[][] = getGamePlan();
        System.out.println(TAG+"+ GamePlan Info: +");
        System.out.println(TAG+"Games to play: "+gamePlan.length);
        System.out.println(TAG+"each Game is run "+results.numberOfGames+" time(s)");
        for (String round[] : gamePlan)
            System.out.println(TAG+"["+round[0]+"] vs ["+round[1]+"]");
        System.out.println(TAG+"+ End Info +");
    }

    /**
     * get agent by name
     * @param name name of agent
     * @return PlayAgent instance
     */
    public TSAgent getAgent(String name) {
        for (TSAgent agnt : results.mAgents)
            if (agnt.getName().equals(name))
                return agnt;
        return null;
    }

    /**
     * check is the tournament is locked to avoid changes of input data
     * @return boolean if tournament is locked
     */
    public boolean isLockedToCompete() {
        return results.lockedToCompete;
    }

    /**
     * lock the tournament system to prevent data from changes.
     * also the gameplan is calculated and measurements are prepared.
     */
    public void lockToCompete() {
        if (results.numberOfGames == -1) {
            System.out.println(TAG+"ERROR :: number of games was not set! using 1");
            results.numberOfGames = 1;
        }
        results.lockedToCompete = true;
        results.gamePlan = generateGamePlanInternal();
        results.gameResult = new int[results.gamePlan.length][3]; // is initialized with all zeros by JDK
        results.timeStorage = new TSTimeStorage[results.gamePlan.length][2];
        for (TSTimeStorage t[] : results.timeStorage) { // initialize all positions
            t[0] = new TSTimeStorage();
            t[1] = new TSTimeStorage();
        }
        results.nextGame = 0;
        gamesPlayed = 0;
    }

    /**
     * get the agent and measurement data for the next playing agents according to the gameplan.
     * <p>
     * you also need to get {@link TSAgentManager#getNextCompetitionTimeStorage()} to save the time measurements
     * @return TSAgent instances of the next playing agents
     */
    public TSAgent[] getNextCompetitionTeam() {
        TSAgent out[] = {results.mAgents.get(results.gamePlan[results.nextGame][0]), results.mAgents.get(results.gamePlan[results.nextGame][1])};
        results.tournamentDone = false;
        return out;
    }

    /**
     * get the time measurement data for the next playing agents according to the gameplan
     * @return time storage instance for the next game
     */
    public TSTimeStorage[] getNextCompetitionTimeStorage() {
        return results.timeStorage[results.nextGame];
    }

    /**
     * after to agents did their game use this method to save the game result.
     * this also prepares the time storage for the next game and updates the gameplan<p>
     * 0 : agent 1 wins<p>
     * 1 : tie<p>
     * 2 : agent 2 wins
     * @param type game result code
     */
    public void enterGameResultWinner(int type) {
        if (!results.lockedToCompete) {
            System.out.println(TAG+"ERROR :: manager ist not locked, cannot enter result. run lockToCompete() first");
            return;
        }
        if (type<0 || type>2) {
            System.out.println(TAG + "ERROR :: enterGameResultWinner(int type) wrong value for type [0;2] = " + type);
            return;
        }
        else {
            results.gameResult[results.nextGame][type] = results.gameResult[results.nextGame][type] + 1;

            TSAgent teamPlayed[] = getNextCompetitionTeam(); // save individual win or loss to the tsagent objects in magents list
            if (type == 0){
                teamPlayed[0].addWonGame();
                teamPlayed[1].addLostGame();
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerFIDE, +1, teamPlayed[1].mEloPlayerFIDE);
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerUSCF, +1, teamPlayed[1].mEloPlayerUSCF);
                glicko2Results.addResult(teamPlayed[0].mGlicko2Rating, teamPlayed[1].mGlicko2Rating);
            }
            if (type == 1){
                teamPlayed[0].addTieGame();
                teamPlayed[1].addTieGame();
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerFIDE, 0, teamPlayed[1].mEloPlayerFIDE);
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerUSCF, 0, teamPlayed[1].mEloPlayerUSCF);
                glicko2Results.addDraw(teamPlayed[0].mGlicko2Rating, teamPlayed[1].mGlicko2Rating);
            }
            if (type == 2){
                teamPlayed[0].addLostGame();
                teamPlayed[1].addWonGame();
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerFIDE, -1, teamPlayed[1].mEloPlayerFIDE);
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerUSCF, -1, teamPlayed[1].mEloPlayerUSCF);
                glicko2Results.addResult(teamPlayed[1].mGlicko2Rating, teamPlayed[0].mGlicko2Rating);
            }

            results.timeStorage[results.nextGame][0].roundFinished();
            results.timeStorage[results.nextGame][1].roundFinished();
        }

        if (results.gameResult[results.nextGame][0]+results.gameResult[results.nextGame][1]+results.gameResult[results.nextGame][2] == results.numberOfGames)
            results.nextGame++;

        gamesPlayed++;

        results.tournamentDone = false;
    }

    /**
     * returns if a next game is available according ot the gameplan
     * @return boolean if a next game is available
     */
    public boolean hastNextGame() {
        if (results.nextGame == results.gamePlan.length) {
            results.tournamentDone = true;
            return false;
        }
        else
            return true;
    }

    /**
     * print a summary of gameresults and basic statistics to the console
     */
    public void printGameResults() {
        if (results.gamePlan.length != results.gameResult.length) {
            System.out.println(TAG+"printGameResults() failed - gamePlan.length != gameResult.length");
            return;
        }
        System.out.println(TAG+"Info on individual games:");
        for (int i=0; i<results.gamePlan.length; i++) {
            System.out.print(TAG);
            System.out.print("Team: ");
            //System.out.print("["+gamePlan[i][0]+"] vs ["+gamePlan[i][1]+"] || ");
            System.out.print("["+results.mAgents.get(results.gamePlan[i][0]).getName()+"] vs ["+results.mAgents.get(results.gamePlan[i][1]).getName()+"] || ");
            System.out.print("Res.: Win1: "+results.gameResult[i][0]+" Tie: "+results.gameResult[i][1]+" Win2: "+results.gameResult[i][2]+" || ");
            System.out.print("Agt.1 average Time MS: "+results.timeStorage[i][0].getAverageTimeForGameMS()+" ");
            System.out.print("Agt.2 average Time MS: "+results.timeStorage[i][1].getAverageTimeForGameMS()+" ");
            System.out.print("");
            System.out.println();
        }
        System.out.println(TAG+"Info on individual Agents:");
        int[] selectedAgents = getIDAgentsSelected();
        for (int id : selectedAgents) {
            TSAgent a = results.mAgents.get(id);
            System.out.print(TAG);
            System.out.print("AgentName: "+a.getName()+" ");
            System.out.print("GamesWon: "+a.getCountWonGames()+" GamesTie: "+a.getCountTieGames()+" GamesLost: "+a.getCountLostGames()+" | ");
            System.out.print("AgentScore: "+a.getAgentScore()+" | ");
            System.out.print("FIDE-ELO: "+a.mEloPlayerFIDE.getEloRating()+" USCF-ELO: "+a.mEloPlayerUSCF.getEloRating());
            //System.out.print("ELO: "+a.mEloPlayerFIDE+" | "+a.mEloPlayerUSCF);
            System.out.println();
        }
    }

    /**
     * unlock the tournament system to enable data changes
     */
    public void unlockAfterComp() {
        results.lockedToCompete = false;
        /*
        gamePlan = null;
        gameResult = null;
        timeStorage = null;
        nextGame = 0;
        */
    }

    /**
     * returns if a tournament is running
     * @return boolean if tournament is running
     */
    public boolean isTournamentDone() {
        return results.tournamentDone;
    }

    /** +++++++++++++++++
     *  +++ STATISTIK +++
     *  +++++++++++++++++
     */

    /**
     * call this method after the tournament ran to process the measurement data to generate the statistics window
     */
    public void makeStats() {
        if (!results.tournamentDone) {
            System.out.println(TAG+"ERROR :: Stats Window cannot be opened, tournament data not available");
            return;
        }
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
                    rowData1[i][j+1] = "W:"+results.gameResult[game][0]+" | T:"+results.gameResult[game][1]+" | L:"+results.gameResult[game][2];
                    float score = 0;
                    score += results.gameResult[game][0] * faktorWin;
                    score += results.gameResult[game][1] * faktorTie;
                    score += results.gameResult[game][2] * faktorLos;
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
         * Table | Agent Score
         */
        String[] columnNames4 = {
                "Rank",
                "Agent",
                "Games Won",
                "Games Tie",
                "Games Lost",
                "WTL Score",
                "FIDE Elo",
                "USCF Elo",
                "Glicko2",
                "WonGameRatio"
        };
        Object[][] rowData4 = new Object[getNumAgentsSelected()][columnNames4.length];
        TSAgent[] rankAgents = new TSAgent[getNumAgentsSelected()];
        int[] selectedAgents = getIDAgentsSelected();
        for (int i=0; i<selectedAgents.length; i++) {
            rankAgents[i] = results.mAgents.get(selectedAgents[i]);
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

        glicko2RatingSystem.updateRatings(glicko2Results); // update glicko2 ratings once after TS

        // put data into table
        for (int i=0; i<rowData4.length; i++) {
            // "Rank"
            rowData4[i][0] = ""+(i+1);
            // "Agent"
            rowData4[i][1] = rankAgents[i].getName();
            // "Games Won"
            rowData4[i][2] = rankAgents[i].getCountWonGames();
            // "Games Tie"
            rowData4[i][3] = rankAgents[i].getCountTieGames();
            // "Games Lost"
            rowData4[i][4] = rankAgents[i].getCountLostGames();
            // "WTL Score"
            rowData4[i][5] = rankAgents[i].getAgentScore();
            // "FIDE Elo"
            rowData4[i][6] = rankAgents[i].mEloPlayerFIDE.getEloRating();
            // "USCF Elo"
            rowData4[i][7] = rankAgents[i].mEloPlayerUSCF.getEloRating();
            // "Glicko2"
            //rowData4[i][8] = rankAgents[i].mGlicko2Rating.getRating();
            NumberFormat formatter1 = new DecimalFormat("#0.00");
            rowData4[i][8] = formatter1.format(rankAgents[i].mGlicko2Rating.getRating());
            // "WonGameRatio"
            float w = rankAgents[i].getCountWonGames();
            float a = rankAgents[i].getCountAllGames();
            float f = w/a;
            NumberFormat formatter2 = new DecimalFormat("#0.00");
            rowData4[i][9] = formatter2.format(f*100)+"%";
        }

        //create table with data
        JTable tableAgentScore = new JTable(rowData4, columnNames4);
        DefaultTableModel defTableAgentScore = new DefaultTableModel(rowData4, columnNames4);
        // center align column entries
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)tableAgentScore.getDefaultRenderer(Object.class);
        renderer.setHorizontalAlignment( JLabel.CENTER );

        /**
         * Scatterplot | AgentScore vs Time
         */
        // https://www.boraji.com/jfreechart-scatter-chart-example
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        int[] selectedAgents2 = getIDAgentsSelected();
        for (int i=0; i<selectedAgents2.length; i++) {
            TSAgent tmp = results.mAgents.get(selectedAgents2[i]);
            ArrayList<Double> medianTimes = new ArrayList<>();
            double median;

            for (int gms=0; gms<results.timeStorage.length; gms++) { // spiele
                for (int cpl=0; cpl<results.timeStorage[0].length; cpl++) { // hin+rückrunde
                    for (int agt=0; agt<2; agt++) { // agent 1+2
                        if (results.gamePlan[gms][cpl] == selectedAgents2[i]) {
                            medianTimes.add(results.timeStorage[gms][cpl].getMedianRoundTimeMS());
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
        Object[][] rowData2 = new Object[results.gameResult.length*numAgentsPerRound][columnNames2.length];
        int pos = 0;
        for (int i=0; i<results.gameResult.length; i++) {
            for (int j=0; j<numAgentsPerRound; j++) {
                // "Spiel"
                rowData2[pos][0] = ""+(i+1);
                // "Agent Name"
                rowData2[pos][1] = results.mAgents.get(results.gamePlan[i][j]).getName();
                // "Agent Typ"
                rowData2[pos][2] = results.mAgents.get(results.gamePlan[i][j]).getAgentType();
                // "schnellster Zug"
                rowData2[pos][3] = ""+results.timeStorage[i][j].getMinTimeForGameMS()+"ms";
                // "langsamster Zug"
                rowData2[pos][4] = ""+results.timeStorage[i][j].getMaxTimeForGameMS()+"ms";
                // "durchschnittliche Zeit Zug"
                rowData2[pos][5] = ""+results.timeStorage[i][j].getAverageTimeForGameMS()+"ms";
                // "median Zeit Zug"
                rowData2[pos][6] = ""+results.timeStorage[i][j].getMedianTimeForGameMS()+"ms";
                // "durchschnittliche Zeit Runde"
                rowData2[pos][7] = ""+results.timeStorage[i][j].getAverageRoundTimeMS()+"ms";
                // "median Zeit Runde"
                rowData2[pos][8] = ""+results.timeStorage[i][j].getMedianRoundTimeMS()+"ms";

                pos++;
            }
        }

        //create table with data
        JTable tableTimeDetail = new JTable(rowData2, columnNames2);
        DefaultTableModel defTableTimeDetail = new DefaultTableModel(rowData2, columnNames2);
        // right align column entries
        DefaultTableCellRenderer renderer2 = (DefaultTableCellRenderer)tableTimeDetail.getDefaultRenderer(Object.class);
        renderer2.setHorizontalAlignment( JLabel.RIGHT );

        /**
         * TS Results in a window
         */

        TSResultWindow mTSRW = new TSResultWindow(defTableMatrixWTL, defTableMatrixSCR, defTableAgentScore, defTableTimeDetail, new ImageIcon(hm), scatterPlotASvT, results.startDate);
    }

    /**
     * load a saved {@link TSResultStorage} from disk, load it and open the result window {@link TSResultWindow}.
     * Gets called in {@link XArenaMenu#generateTournamentMenu()} via the top menu.
     * @param tsr instance of {@link TSResultStorage} loaded from disk. Is NULL if the FileChooser is closed
     *            without a file chosen
     */
    public void loadAndShowTSFromDisk(TSResultStorage tsr) {
        if (tsr == null)
            return;
        // load ts results from disk
        results = tsr;
        // visualize
        makeStats();
    }

    /**
     * returns the current tournament progress
     * @return [ rounds played , total number of rounds]
     */
    public int[] getTSProgress() {
        int[] i = {gamesPlayed, results.gamePlan.length*results.numberOfGames };
        return i;
    }
}
