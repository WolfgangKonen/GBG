package TournamentSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TSResultWindow extends JFrame{
    private JPanel mJPanel;
    private JTable tableMatrixWTL;
    private JTable tableMatrixSCR;
    private JLabel heatmapJL;
    private JTable tableAgentScore;
    private JTable tableTimeDetail;
    private JScrollPane jspWTL;
    private JScrollPane jspSCR;
    private JScrollPane jspHM;
    private JScrollPane jspASC;
    private JScrollPane jspTD;

    public TSResultWindow(DefaultTableModel m1, DefaultTableModel m2, DefaultTableModel m3, DefaultTableModel m4, ImageIcon imageIcon) {
        super("Turnier Ergebnisse");

        tableMatrixWTL.setModel(m1);
        tableMatrixWTL.setPreferredScrollableViewportSize(
                new Dimension( tableMatrixWTL.getPreferredSize().width, tableMatrixWTL.getRowHeight() * tableMatrixWTL.getRowCount()));
        tableMatrixSCR.setModel(m2);
        tableMatrixSCR.setPreferredScrollableViewportSize(
                new Dimension( tableMatrixSCR.getPreferredSize().width, tableMatrixSCR.getRowHeight() * tableMatrixSCR.getRowCount()));
        tableAgentScore.setModel(m3);
        tableAgentScore.setPreferredScrollableViewportSize(
                new Dimension( tableAgentScore.getPreferredSize().width, tableAgentScore.getRowHeight() * tableAgentScore.getRowCount()));
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)tableAgentScore.getDefaultRenderer(Object.class);
        renderer.setHorizontalAlignment( JLabel.CENTER );
        tableTimeDetail.setModel(m4);
        tableTimeDetail.setPreferredScrollableViewportSize(
                new Dimension( tableTimeDetail.getPreferredSize().width*2, tableTimeDetail.getRowHeight() * tableTimeDetail.getRowCount()));
        DefaultTableCellRenderer renderer2 = (DefaultTableCellRenderer)tableTimeDetail.getDefaultRenderer(Object.class);
        renderer2.setHorizontalAlignment( JLabel.RIGHT );
        heatmapJL.setText("");
        heatmapJL.setIcon(imageIcon);

        setContentPane(mJPanel);
        //setSize(1000,1000);
        pack();
        setVisible(true);
        }

}
