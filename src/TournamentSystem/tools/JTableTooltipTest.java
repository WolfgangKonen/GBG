package TournamentSystem.tools;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JTableTooltipTest {

    public static void main(String[] args) {
        new JTableTooltipTest();
    }

    public JTableTooltipTest() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        public TestPane() {
            setLayout(new BorderLayout());
            DefaultTableModel model = new DefaultTableModel(0, 10);
            for (int row = 0; row < 10; row++) {
                Object[] data = new Object[10];
                for (int col = 0; col < 10; col++) {
                    data[col] = row + "x" + col;
                }
                model.addRow(data);
            }

            JTable table = new JTable(model) {
                @Override
                public Point getToolTipLocation(MouseEvent event) {
                    return new Point(10, 10);
                }
            };
            table.setDefaultRenderer(Object.class, new TestCellRenderer());

            add(new JScrollPane(table));

        }

        public class TestCellRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setToolTipText("Banana @ " + value.toString());
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

        }

    }

}
