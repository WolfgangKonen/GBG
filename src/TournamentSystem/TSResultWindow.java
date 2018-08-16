package TournamentSystem;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import tools.Types;

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
    private boolean showjspASC = !false;
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

    /**
     * create the result window with the tournament statistics with data provided from {@link TSAgentManager}
     *
     * @param m1          table matrix for game win/tie/loss results
     * @param m2          table matrix for game win/tie/loss scores
     * @param m3          table with agent score ranking
     * @param m4          table with detailed time measurements
     * @param imageIcon   heatmap with visualisation of WTL scores
     * @param imageIcon2  heatmap with visualisation of WTL scores sorted vertically by score
     * @param scatterPlot scatterplot XYPlot of the agents score vs round time
     * @param startDate   info String with date and TS settings
     */
    public TSResultWindow(DefaultTableModel m1, DefaultTableModel m2, DefaultTableModel m3, DefaultTableModel m4,
                          ImageIcon imageIcon, ImageIcon imageIcon2, JFreeChart scatterPlot, String startDate) {
        super("Turnier Ergebnisse");

        $$$setupUI$$$();

        Font lFont = new Font("Arial", Font.PLAIN, Types.GUI_DIALOGFONTSIZE);
        //tableMatrixWTL.setFont(lFont); // todo also need to set cell height according to font height
        startDateTSJL.setText(startDate);

        tableMatrixWTL.setModel(m1);
        tableMatrixWTL.setPreferredScrollableViewportSize(
                new Dimension(tableMatrixWTL.getPreferredSize().width, tableMatrixWTL.getRowHeight() * tableMatrixWTL.getRowCount()));

        tableMatrixSCR.setModel(m2);
        tableMatrixSCR.setPreferredScrollableViewportSize(
                new Dimension(tableMatrixSCR.getPreferredSize().width, tableMatrixSCR.getRowHeight() * tableMatrixSCR.getRowCount()));

        tableAgentScore.setModel(m3);
        tableAgentScore.setPreferredScrollableViewportSize(
                new Dimension(tableAgentScore.getPreferredSize().width, tableAgentScore.getRowHeight() * tableAgentScore.getRowCount()));
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) tableAgentScore.getDefaultRenderer(Object.class);
        renderer.setHorizontalAlignment(JLabel.CENTER);

        tableTimeDetail.setModel(m4);
        tableTimeDetail.setPreferredScrollableViewportSize(
                new Dimension(tableTimeDetail.getPreferredSize().width * 2, tableTimeDetail.getRowHeight() * tableTimeDetail.getRowCount()));
        DefaultTableCellRenderer renderer2 = (DefaultTableCellRenderer) tableTimeDetail.getDefaultRenderer(Object.class);
        renderer2.setHorizontalAlignment(JLabel.RIGHT);

        heatmapJL.setText("");
        heatmapJL.setIcon(imageIcon);

        heatmap2JL.setText("");
        heatmap2JL.setIcon(imageIcon2);

        ChartPanel scatterPlotASvT = new ChartPanel(scatterPlot);
        scatterPlotASvT.setPreferredSize(new Dimension(400, 300)); // plot size
        scatterPlotJPanel.add(scatterPlotASvT);

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
                showjspWTL = true;
                showjspSCR = true;
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
                JFrame frame = new JFrame();
                ChartPanel scatterPlotASvTBig = new ChartPanel(scatterPlot);
                scatterPlotASvTBig.setPreferredSize(new Dimension(1250, 1000)); // plot size
                frame.setContentPane(scatterPlotASvTBig);
                frame.setTitle("Big scatter plot");
                frame.pack();
                frame.setVisible(true);
            }
        });
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
        final JLabel label1 = new JLabel();
        label1.setText("Table with Win, Tie, Loss Results");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Table with game scores calculated from game WTL");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("<html><body>Heatmap visualisation of game scores<br>white = worst ; black = best | Y vs. X</body></html>");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Ranking of agents by overall Wins, Ties, Losses");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Game time meassurements in [ms]");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label5, gbc);
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
        gbc.gridy = 13;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(jspASC, gbc);
        tableAgentScore = new JTable();
        jspASC.setViewportView(tableAgentScore);
        jspTD = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 16;
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
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
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
        final JLabel label6 = new JLabel();
        label6.setText("ScatterPlot AgentScore vs. RoundTime");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label6, gbc);
        scatterPlotJPanel = new JPanel();
        scatterPlotJPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(scatterPlotJPanel, gbc);
        showHideTableWTLButton = new JButton();
        showHideTableWTLButton.setText("Show/Hide WTL Table");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableWTLButton, gbc);
        showHideTableTimeTableButton = new JButton();
        showHideTableTimeTableButton.setText("Show/Hide Time Table");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 15;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableTimeTableButton, gbc);
        showHideTableSCRButton = new JButton();
        showHideTableSCRButton.setText("Show/Hide Score Table");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableSCRButton, gbc);
        showHideTableASCButton = new JButton();
        showHideTableASCButton.setText("Show/Hide Ranking Table");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(showHideTableASCButton, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer6, gbc);
        hideAllTablesButton = new JButton();
        hideAllTablesButton.setText("Hide all Tables");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 18;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(hideAllTablesButton, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 19;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer7, gbc);
        showAllTablesButton = new JButton();
        showAllTablesButton.setText("Show all Tables");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 18;
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
        heatmap2JL.setText("Label");
        jspHM2.setViewportView(heatmap2JL);
        final JLabel label7 = new JLabel();
        label7.setText("Heatmap sorted by AgentScore");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label7, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        mJPanel.add(spacer9, gbc);
        openBiggerScatterPlotButton = new JButton();
        openBiggerScatterPlotButton.setText("Open bigger ScatterPlot");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(openBiggerScatterPlotButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mJPanel;
    }
}
