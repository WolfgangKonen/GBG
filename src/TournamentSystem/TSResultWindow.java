package TournamentSystem;

import TournamentSystem.tools.TSHeatmapDataTransfer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * This class generates a GUI to visualize the tournament statistics and measurements.
 * It's called in {@link TSAgentManager} after the tournament is finished or in
 * {@link TSSettingsGUI2} if you reopen the result window in the settings GUI.
 * <p>
 * This GUI was build with the IntelliJ GUI Designer.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSResultWindow extends JFrame {
    private JPanel mJPanel;
    private JTable tableMatrixWTL;
    private JTable tableMatrixSCR;
    private JLabel heatmapJL;
    private JTable tableAgentScore;
    private JTable tableTimeDetail;
    private JScrollPane jspWTL;
    private boolean showjspWTL = false;
    private JScrollPane jspSCR;
    private boolean showjspSCR = false;
    private JScrollPane jspHM;
    private JScrollPane jspASC;
    private boolean showjspASC = true;
    private JScrollPane jspTD;
    private boolean showjspTD = false;
    private JPanel scatterPlotJPanel;
    private JButton showHideTableTimeTableButton;
    private JButton showHideTableWTLButton;
    private JButton showHideTableSCRButton;
    private JButton showHideTableASCButton;
    private JButton hideAllTablesButton;
    private JButton showAllTablesButton;
    private JLabel startDateTSJL;
    private JScrollPane jspHM2;
    private JLabel heatmap2JL;
    private JButton openBiggerScatterPlotButton;
    private JLabel tableWTLLabel;
    private JLabel tableSCRLabel;
    private JLabel heatmapH1JL;
    private JLabel heatmap2H1JL;
    private JButton toggleAdvancedInfoTimeButton;
    private JButton openAdvancedHeatmapAnalysisButton;
    private boolean showAdvHMAnalysis = false;
    private JScrollPane jspHMadv1;
    private JLabel heatmapJLadv1;
    private JLabel heatmapJLadv1Title;
    private JScrollPane jspHMadv2;
    private JLabel heatmapJLadv2;
    private JLabel heatmapJLadv2Title;
    private JScrollPane jspHMadv3;
    private JLabel heatmapJLadv3;
    private JLabel heatmapJLadv3Title;

    private JFreeChart scatterPlot = null;
    private DefaultTableModel timeDetail, timeSimple = null;
    private boolean timeSimpleSelected = true;
    private boolean isSinglePlayerGame;

    private final String TAG = "[TSResultWindow] ";

    /**
     * create the result window with the tournament statistics with data provided from {@link TSAgentManager}
     *
     * @param m1          table matrix for game win/tie/loss results
     * @param m2          table matrix for game win/tie/loss scores
     * @param m3          table with agent score ranking
     * @param m4          table with detailed time measurements
     * @param m5          table with simplified time measurements
     * @param dataHM      heatmap data for all heatmaps
     * @param scatterPlot scatterplot XYPlot of the agents score vs round time
     * @param startDate   info String with date and TS settings
     */
    public TSResultWindow(DefaultTableModel m1, DefaultTableModel m2, DefaultTableModel m3, DefaultTableModel m4, DefaultTableModel m5,
                          TSHeatmapDataTransfer dataHM, JFreeChart scatterPlot, String startDate) {
        this(startDate, false);

        setTableMatrixWTL(m1);
        setTableMatrixSCR(m2);
        setTableAgentScore(m3);
        setTableTimeDetail(m4, m5);
        setHeatMap(dataHM);
        setScatterPlotASvT(scatterPlot);
    }

    /**
     * create the result window with the tournament statistics with data provided from {@link TSAgentManager}.
     * Use the setters provided with this class to set the calculated data for visualization.
     *
     * @param startDate        info String with date and TS settings
     * @param singlePlayerGame boolean if the game is a 1 player game
     */
    public TSResultWindow(String startDate, boolean singlePlayerGame) {
        super("Tournament Results");

        $$$setupUI$$$();

        showHideTableTimeTableButton.setVisible(false);
        showHideTableWTLButton.setVisible(false);
        tableWTLLabel.setVisible(false);
        showHideTableSCRButton.setVisible(false);
        tableSCRLabel.setVisible(false);
        showHideTableASCButton.setVisible(false);
        openBiggerScatterPlotButton.setVisible(false);
        heatmapH1JL.setVisible(false);
        heatmapJL.setVisible(false);
        heatmap2H1JL.setVisible(false);
        heatmap2JL.setVisible(false);
        jspHM.setVisible(false);
        jspHM2.setVisible(false);
        jspHMadv1.setVisible(false);
        heatmapJLadv1Title.setVisible(false);
        jspHMadv2.setVisible(false);
        heatmapJLadv2Title.setVisible(false);
        jspHMadv3.setVisible(false);
        heatmapJLadv3Title.setVisible(false);

        isSinglePlayerGame = singlePlayerGame;

        //Font lFont = new Font("Arial", Font.PLAIN, Types.GUI_DIALOGFONTSIZE);
        //tableMatrixWTL.setFont(lFont); // todo also need to set cell height according to font height
        startDateTSJL.setText(startDate);

        if (singlePlayerGame) {
            showjspTD = true;
            showAdvHMAnalysis = false;
            openAdvancedHeatmapAnalysisButton.setEnabled(false);
        } else {
            jspHMadv1.setVisible(showAdvHMAnalysis);
            heatmapJLadv1Title.setVisible(showAdvHMAnalysis);
            jspHMadv2.setVisible(showAdvHMAnalysis);
            heatmapJLadv2Title.setVisible(showAdvHMAnalysis);
            jspHMadv3.setVisible(showAdvHMAnalysis);
            heatmapJLadv3Title.setVisible(showAdvHMAnalysis);
        }
        jspWTL.setVisible(showjspWTL);
        jspSCR.setVisible(showjspSCR);
        jspASC.setVisible(showjspASC);
        jspTD.setVisible(showjspTD);

        JScrollPane scroll = new JScrollPane(mJPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //setContentPane(mJPanel);
        setContentPane(scroll);
        //setSize(1000,1000);
        pack();
        setVisible(true);

        showHideTableTimeTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showjspTD = !showjspTD;
                jspTD.setVisible(showjspTD);
                revalidate();
                repaint();
                pack();
            }
        });
        showHideTableWTLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showjspWTL = !showjspWTL;
                jspWTL.setVisible(showjspWTL);
                revalidate();
                repaint();
                pack();
            }
        });
        showHideTableSCRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showjspSCR = !showjspSCR;
                jspSCR.setVisible(showjspSCR);
                revalidate();
                repaint();
                pack();
            }
        });
        showHideTableASCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showjspASC = !showjspASC;
                jspASC.setVisible(showjspASC);
                revalidate();
                repaint();
                pack();
            }
        });
        hideAllTablesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showjspWTL = false;
                showjspSCR = false;
                showjspASC = false;
                showjspTD = false;
                jspWTL.setVisible(showjspWTL);
                jspSCR.setVisible(showjspSCR);
                jspASC.setVisible(showjspASC);
                jspTD.setVisible(showjspTD);
                revalidate();
                repaint();
                pack();
            }
        });
        showAllTablesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!singlePlayerGame) {
                    showjspWTL = true;
                    showjspSCR = true;
                }
                showjspASC = true;
                showjspTD = true;
                jspWTL.setVisible(showjspWTL);
                jspSCR.setVisible(showjspSCR);
                jspASC.setVisible(showjspASC);
                jspTD.setVisible(showjspTD);
                revalidate();
                repaint();
                pack();
            }
        });
        openBiggerScatterPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scatterPlot == null) {
                    System.out.println(TAG + "ScatterPlot data not available!");
                    return;
                }
                JFrame frame = new JFrame();
                ChartPanel scatterPlotASvTBig = new ChartPanel(scatterPlot);
                scatterPlotASvTBig.setPreferredSize(new Dimension(1250, 1000)); // plot size
                frame.setContentPane(scatterPlotASvTBig);
                frame.setTitle("Big scatter plot");
                frame.pack();
                frame.setVisible(true);
            }
        });
        toggleAdvancedInfoTimeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show or hide detailed time table
                timeSimpleSelected = !timeSimpleSelected;

                if (timeSimpleSelected) {
                    tableTimeDetail.setModel(timeSimple);
                } else {
                    tableTimeDetail.setModel(timeDetail);
                }
                tableTimeDetail.setPreferredScrollableViewportSize(
                        new Dimension(tableTimeDetail.getPreferredSize().width * 2, tableTimeDetail.getRowHeight() * tableTimeDetail.getRowCount()));
                DefaultTableCellRenderer renderer2 = (DefaultTableCellRenderer) tableTimeDetail.getDefaultRenderer(Object.class);
                renderer2.setHorizontalAlignment(JLabel.RIGHT);
                pack();
            }
        });
        openAdvancedHeatmapAnalysisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAdvHMAnalysis = !showAdvHMAnalysis;

                jspHMadv1.setVisible(showAdvHMAnalysis);
                heatmapJLadv1Title.setVisible(showAdvHMAnalysis);
                jspHMadv2.setVisible(showAdvHMAnalysis);
                heatmapJLadv2Title.setVisible(showAdvHMAnalysis);
                jspHMadv3.setVisible(showAdvHMAnalysis);
                heatmapJLadv3Title.setVisible(showAdvHMAnalysis);
                pack();
            }
        });
    } // public TSResultWindow(...)

    public void setTableMatrixWTL(DefaultTableModel m1) {
        showHideTableWTLButton.setVisible(true);
        tableWTLLabel.setVisible(true);

        tableMatrixWTL.setModel(m1);
        tableMatrixWTL.setPreferredScrollableViewportSize(
                new Dimension(tableMatrixWTL.getPreferredSize().width, tableMatrixWTL.getRowHeight() * tableMatrixWTL.getRowCount()));
        pack();
    }

    public void setTableMatrixSCR(DefaultTableModel m2) {
        showHideTableSCRButton.setVisible(true);
        tableSCRLabel.setVisible(true);

        tableMatrixSCR.setModel(m2);
        tableMatrixSCR.setPreferredScrollableViewportSize(
                new Dimension(tableMatrixSCR.getPreferredSize().width, tableMatrixSCR.getRowHeight() * tableMatrixSCR.getRowCount()));
        pack();
    }

    public void setTableAgentScore(DefaultTableModel m3) {
        showHideTableASCButton.setVisible(true);

        tableAgentScore.setModel(m3);
        tableAgentScore.setPreferredScrollableViewportSize(
                new Dimension(tableAgentScore.getPreferredSize().width, tableAgentScore.getRowHeight() * tableAgentScore.getRowCount()));
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) tableAgentScore.getDefaultRenderer(Object.class);
        renderer.setHorizontalAlignment(JLabel.CENTER);
        pack();
    }

    public void setTableTimeDetail(DefaultTableModel m4, DefaultTableModel m5) {
        timeDetail = m4;
        timeSimple = m5;
        showHideTableTimeTableButton.setVisible(true);

        if (timeSimpleSelected)
            tableTimeDetail.setModel(m5);
        else
            tableTimeDetail.setModel(m4);
        tableTimeDetail.setPreferredScrollableViewportSize(
                new Dimension(tableTimeDetail.getPreferredSize().width * 2, tableTimeDetail.getRowHeight() * tableTimeDetail.getRowCount()));
        DefaultTableCellRenderer renderer2 = (DefaultTableCellRenderer) tableTimeDetail.getDefaultRenderer(Object.class);
        renderer2.setHorizontalAlignment(JLabel.RIGHT);
        pack();
    }

    public void setHeatMap(TSHeatmapDataTransfer dataTransfer) {
        heatmapH1JL.setVisible(true);
        heatmapJL.setVisible(true);
        jspHM.setVisible(true);
        heatmap2H1JL.setVisible(true);
        heatmap2JL.setVisible(true);
        jspHM2.setVisible(true);

        heatmapJL.setText("");
        heatmapJL.setIcon(dataTransfer.scoreHeatmap);
        heatmapJLadv1.setText("");
        heatmapJLadv1.setIcon(dataTransfer.scoreHeatmapA1);
        heatmapJLadv2.setText("");
        heatmapJLadv2.setIcon(dataTransfer.scoreHeatmapA2);
        heatmapJLadv3.setText("");
        heatmapJLadv3.setIcon(dataTransfer.scoreHeatmapA3);
        heatmap2JL.setText("");
        heatmap2JL.setIcon(dataTransfer.scoreHeatmapSorted);
        pack();
    }

    public void setScatterPlotASvT(JFreeChart scatterPlot) {
        this.scatterPlot = scatterPlot;
        openBiggerScatterPlotButton.setVisible(true);

        ChartPanel scatterPlotASvT = new ChartPanel(scatterPlot);
        int width = 400;
        int height = 400;

        if (!isSinglePlayerGame) {
            if (heatmapJL.getWidth() > width) {
                width = heatmapJL.getWidth();
            }
            if (heatmapJL.getHeight() > height) {
                height = heatmapJL.getHeight();
            }
        }

        scatterPlotASvT.setPreferredSize(new Dimension(width, height)); // plot size
        scatterPlotJPanel.add(scatterPlotASvT);
        pack();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mJPanel = new JPanel();
        mJPanel.setLayout(new GridBagLayout());
        tableWTLLabel = new JLabel();
        tableWTLLabel.setText("<html><body><strong>Table with Win, Tie, Loss (WTL) Results</strong><br>\nRow agent (1st) plays against the column agent (2nd)<br>\nWin and loss are from the row agents perspective</body></html>");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(tableWTLLabel, gbc);
        tableSCRLabel = new JLabel();
        tableSCRLabel.setText("<html><body><strong>Table with game scores calculated from game WTL</body></html>\n");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(tableSCRLabel, gbc);
        heatmapH1JL = new JLabel();
        heatmapH1JL.setText("<html><body><strong>Heatmap visualisation of game scores</strong><br>\nwhite = worst | black = best score<br>\nblue = agent against itself<br>\ngreen = game was not played in specific tournament mode<br>\nrow agent (1st) plays against the column agent (2nd)</body></html>");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(heatmapH1JL, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("<html><body><strong>Ranking of Agents by overall Wins, Ties, Losses</strong></body></html>\n");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("<html><body><strong>Game time meassurements in [ms]</strong></body></html>");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 17;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label2, gbc);
        jspWTL = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspWTL, gbc);
        tableMatrixWTL = new JTable();
        jspWTL.setViewportView(tableMatrixWTL);
        jspSCR = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspSCR, gbc);
        tableMatrixSCR = new JTable();
        jspSCR.setViewportView(tableMatrixSCR);
        jspHM = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspHM, gbc);
        heatmapJL = new JLabel();
        heatmapJL.setText("HeatMap");
        jspHM.setViewportView(heatmapJL);
        jspASC = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspASC, gbc);
        tableAgentScore = new JTable();
        jspASC.setViewportView(tableAgentScore);
        jspTD = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 18;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspTD, gbc);
        tableTimeDetail = new JTable();
        jspTD.setViewportView(tableTimeDetail);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        mJPanel.add(spacer5, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("<html><body><strong>ScatterPlot AgentScore vs. RoundTime</strong><br>\nagent WinTieLoss score vs median roundtime</body></html>\n");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label3, gbc);
        scatterPlotJPanel = new JPanel();
        scatterPlotJPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(scatterPlotJPanel, gbc);
        showHideTableWTLButton = new JButton();
        showHideTableWTLButton.setText("Show/Hide WTL Table");
        showHideTableWTLButton.setToolTipText("Show or hide the WinTieLoss Table by clicking this button");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableWTLButton, gbc);
        showHideTableTimeTableButton = new JButton();
        showHideTableTimeTableButton.setText("Show/Hide Time Table");
        showHideTableTimeTableButton.setToolTipText("Show or hide the time measurement table by clicking this button");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableTimeTableButton, gbc);
        showHideTableSCRButton = new JButton();
        showHideTableSCRButton.setText("Show/Hide Score Table");
        showHideTableSCRButton.setToolTipText("Show or hide the Score Table by clicking this button");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableSCRButton, gbc);
        showHideTableASCButton = new JButton();
        showHideTableASCButton.setText("Show/Hide Ranking Table");
        showHideTableASCButton.setToolTipText("Show or hide the agent ranking table by clicking this button");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableASCButton, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 19;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer6, gbc);
        hideAllTablesButton = new JButton();
        hideAllTablesButton.setText("Hide all Tables");
        hideAllTablesButton.setToolTipText("Hide all tables by clicking this button");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 20;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(hideAllTablesButton, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 21;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer7, gbc);
        showAllTablesButton = new JButton();
        showAllTablesButton.setText("Show all Tables");
        showAllTablesButton.setToolTipText("Show all tables by clicking this button");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 20;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showAllTablesButton, gbc);
        startDateTSJL = new JLabel();
        startDateTSJL.setText("Turnament Start Date: xx.xx.xxxx xx:xx");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(startDateTSJL, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 1, 0);
        mJPanel.add(spacer8, gbc);
        jspHM2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspHM2, gbc);
        heatmap2JL = new JLabel();
        heatmap2JL.setText("HeatMapSorted");
        jspHM2.setViewportView(heatmap2JL);
        heatmap2H1JL = new JLabel();
        heatmap2H1JL.setText("<html><body><strong>Heatmap sorted by AgentScore</strong><br>\nrows are sorted with the best agent on top</body></html>");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(heatmap2H1JL, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        mJPanel.add(spacer9, gbc);
        openBiggerScatterPlotButton = new JButton();
        openBiggerScatterPlotButton.setText("Open bigger ScatterPlot");
        openBiggerScatterPlotButton.setToolTipText("Open the Agentscore vs. Roundtime plot in a new and bigger window");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(openBiggerScatterPlotButton, gbc);
        toggleAdvancedInfoTimeButton = new JButton();
        toggleAdvancedInfoTimeButton.setText("Toggle Advanced Time Info");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(toggleAdvancedInfoTimeButton, gbc);
        openAdvancedHeatmapAnalysisButton = new JButton();
        openAdvancedHeatmapAnalysisButton.setText("Toggle Advanced Heatmap Analysis");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(openAdvancedHeatmapAnalysisButton, gbc);
        jspHMadv1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspHMadv1, gbc);
        heatmapJLadv1 = new JLabel();
        heatmapJLadv1.setText("heatmapJLadv1");
        jspHMadv1.setViewportView(heatmapJLadv1);
        heatmapJLadv1Title = new JLabel();
        heatmapJLadv1Title.setText("Is Wab = Wba true? | green=yes ; red=no");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(heatmapJLadv1Title, gbc);
        jspHMadv2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspHMadv2, gbc);
        heatmapJLadv2 = new JLabel();
        heatmapJLadv2.setText("heatmapJLadv2");
        jspHMadv2.setViewportView(heatmapJLadv2);
        heatmapJLadv2Title = new JLabel();
        heatmapJLadv2Title.setText("Is Wab = 1-Wba true?");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(heatmapJLadv2Title, gbc);
        jspHMadv3 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspHMadv3, gbc);
        heatmapJLadv3 = new JLabel();
        heatmapJLadv3.setText("heatmapJLadv3");
        jspHMadv3.setViewportView(heatmapJLadv3);
        heatmapJLadv3Title = new JLabel();
        heatmapJLadv3Title.setText("Is Wab = Wba AND Wab = 1-Wba true?");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(heatmapJLadv3Title, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mJPanel;
    }
}
