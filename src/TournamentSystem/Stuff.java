package TournamentSystem;

import javax.swing.*;

@Deprecated
public class Stuff {

    public static void main(String[] args) { new Stuff(); }

    //--------------------------------------

    public Stuff() {
        makeJTable();
    }

    private void makeJTable() {
        // http://www.codejava.net/java-se/swing/a-simple-jtable-example-for-display
        //headers for the table
        String[] columns = new String[] {
                "X vs Y", "Agent#1", "Agent#2", "Agent#3"
        };

        //actual data for the table in a 2d array
        /*
        Object[][] data = new Object[][] {
                {1, "John",  40.0, false },
                {2, "Rambo", 70.0, false },
                {3, "Zorro", 60.0, true },
        };
        */
        String s = "W:1 | T:1 | L:1";
        Object[][] data = new Object[][] {
                {"Agent#1", "xxx",  s, s},
                {"Agent#2", s,  "xxx", s},
                {"Agent#3", s,  s, "xxx"},
        };
        //create table with data
        JTable table = new JTable(data, columns);

        //add the table to the frame
        JFrame frame = new JFrame();
        frame.add(new JScrollPane(table));

        frame.setTitle("Table Example");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
