package games;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by Johannes on 06.05.2017.
 */
public class LogManagerGUI {
    private LogManager logManager;
    private GameBoard gameBoard;

    private boolean verbose = true;

    private JFrame jFMain = new JFrame("Log Manager");

    private JMenuBar jMBMain = new JMenuBar();
    private JMenu jMOptions = new JMenu("Options");
    private JCheckBoxMenuItem jCBMILoggingEnabled;
    private JCheckBoxMenuItem jCBMIAdvancedLogging;
    private JMenuItem jMICompile = new JMenuItem("Compile temporary Gamelog");
    private JMenu jMLoad = new JMenu("Load");
    private JMenuItem jMILoad = new JMenuItem("Load Gamelog");

    private JPanel jPMain = new JPanel();
    private JTextField jTFNextAction = new JTextField("0");
    private JLabel jLNumberActions = new JLabel("/ 0");
    private JButton jBJump = new JButton("Jump to Action");
    private JButton jBNextAction = new JButton("no next action");
    private JButton jBPreviousAction = new JButton("no previous action");

    LogSessionContainer currentLog;
    int counter;

    /**
     * The GUI for the logManager
     *
     * @param logManager a logManager, mostly used for settings and filepaths
     * @param gameBoard a gameBoard, used to display a log
     */
    public LogManagerGUI(LogManager logManager, GameBoard gameBoard) {
        this.logManager = logManager;
        this.gameBoard = gameBoard;
        initialize();
    }

    //ToDo: bug vtable
    //ToDo: mails antworten
    //ToDo: bad saugen
    //ToDo: sport

    /**
     * creates a Menu Bar, Buttons...
     */
    private void initialize() {
        //Menu Bar
        jFMain.setJMenuBar(jMBMain);

        //options
        jMBMain.add(jMOptions);

        jCBMILoggingEnabled = new JCheckBoxMenuItem("Logging enabled", logManager.loggingEnabled);
        jCBMILoggingEnabled.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                logManager.loggingEnabled = jCBMILoggingEnabled.isSelected();
            }
        });

        jCBMIAdvancedLogging = new JCheckBoxMenuItem("Advanced logging", logManager.advancedLogging);
        jCBMIAdvancedLogging.addActionListener((e) ->
            {
                logManager.advancedLogging = jCBMIAdvancedLogging.isSelected();
        });

        jMICompile.addActionListener((e) ->
        {
            //compiles a temp log to .gamelog
            JFileChooser fileChooser = new JFileChooser(logManager.filePath + "\\temp");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Folder", "dir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            Action details = fileChooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);

            int success = fileChooser.showOpenDialog(null);

            if (success == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                LogSessionContainer logSessionContainer = logManager.generateLogSessionContainerFromFile(selectedFile.getPath());
                logManager.safeLogSessionContainer(logSessionContainer);

                if (verbose) {
                    System.out.println("LogManager: compiled temp log successfully");
                }
            }
        });

        jMOptions.add(jCBMILoggingEnabled);
        jMOptions.add(jCBMIAdvancedLogging);
        jMOptions.add(jMICompile);

        //load
        jMBMain.add(jMLoad);

        jMILoad.addActionListener((e) ->
        {
            //load a new .gamelog File

            JFileChooser fileChooser = new JFileChooser(logManager.filePath);
            String gameName = gameBoard.getStateObs().getName();
            fileChooser.setFileFilter(new FileNameExtensionFilter(gameName + " Gamelog", gameName + "_gamelog"));
            Action details = fileChooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);

            int succes = fileChooser.showOpenDialog(null);

            if (succes == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    FileInputStream fis = new FileInputStream(selectedFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    currentLog = (LogSessionContainer) ois.readObject();
                    fis.close();
                    ois.close();

                    loadBoard(0);
                    counter = 0;
                    jTFNextAction.setEnabled(true);
                    jLNumberActions.setText("/ " + (currentLog.stateObservations.size()));
                    jBJump.setEnabled(true);
                } catch (IOException | ClassNotFoundException ignore) {
                    if(verbose) {
                        System.out.println("LogManager: please only load Logs for your current game!");
                    }
                    //ignore.printStackTrace();
                }
            }
        });

        jMLoad.add(jMILoad);

        //main Panel
        jPMain.setLayout(new GridLayout(2, 2, 10, 10));
        jPMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jTFNextAction.setEnabled(false);

        jBJump.setEnabled(false);
        jBJump.setForeground(Color.black);
        jBJump.setBackground(Color.orange);
        jBJump.addActionListener((e) ->
        {
            //jump to a position in currentLog
            try {
                int position = Integer.parseInt(jTFNextAction.getText()) - 1;
                if (position < currentLog.stateObservations.size() && position >= 0) {
                    loadBoard(position);
                } else {
                    jTFNextAction.setText("" + (counter + 1));
                }
            } catch (NumberFormatException ignore) {
            }
        });

        jBPreviousAction.setEnabled(false);
        jBPreviousAction.setForeground(Color.black);
        jBPreviousAction.setBackground(Color.orange);
        jBPreviousAction.addActionListener((e) ->
                {
                    loadBoard(counter - 1);


                });

        jBNextAction.setEnabled(false);
        jBNextAction.setForeground(Color.black);
        jBNextAction.setBackground(Color.orange);
        jBNextAction.addActionListener((e) ->
        {
            loadBoard(counter + 1);

        });

        JPanel JPAction = new JPanel();
        JPAction.setLayout(new GridLayout(1, 2, 10, 10));

        JPAction.add(jTFNextAction);
        JPAction.add(jLNumberActions);

        jPMain.add(JPAction);
        jPMain.add(jBJump);

        jPMain.add(jBPreviousAction);
        jPMain.add(jBNextAction);

        jFMain.setSize(360, 150);
        jFMain.setLocation(465, 0);
        jFMain.add(jPMain);
        jFMain.setVisible(true);
    }

    /**
     * close the LogManagerGUI, firing a WINDOW_CLOSING event
     */
    public void close() {
        jFMain.dispatchEvent(new WindowEvent(jFMain, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * displays a specific position on the gameBoard
     * also changes advance/revert Buttons and NextAction Textfield on the GUI
     *
     * @param position the position in the currentLog
     */
    private void loadBoard(int position) {
        if(currentLog.stateObservations.size() > position && position >= 0) {
            counter = position;
            gameBoard.updateBoard(currentLog.stateObservations.get(position), true, true);
            jTFNextAction.setText("" + (position + 1));

            if(currentLog.stateObservations.size() > position + 1) {
                jBNextAction.setEnabled(true);
                jBNextAction.setText("advance (" + currentLog.actions.get(position+1) + ")");
            } else {
                jBNextAction.setEnabled(false);
                jBNextAction.setText("no next action");
            }

            if(position > 0) {
                jBPreviousAction.setEnabled(true);
                jBPreviousAction.setText("revert (" + currentLog.actions.get(position) + ")");
            } else {
                jBPreviousAction.setEnabled(false);
                jBPreviousAction.setText("no previous action");
            }
        }
    }
}
