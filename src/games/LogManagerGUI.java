package games;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.RubiksCube.StateObserverCube;
import tools.Types;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * The GUI for {@link LogManager}
 * 
 * @author Johannes Kutsch, TH Koeln, 2016
 * 
 * @see LogManager
 */
public class LogManagerGUI {
    private final LogManager logManager;
    private final GameBoard gameBoard;
    private PlayAgent paX;  // for InspectV mode

    private final boolean verbose = true;

    private final JFrame jFMain = new JFrame("Log Manager");

    private final JMenuBar jMBMain = new JMenuBar();
    private final JMenu jMOptions = new JMenu("Options");
    private JCheckBoxMenuItem jCBMILoggingEnabled;
    private JCheckBoxMenuItem jCBMIAdvancedLogging;
    private JCheckBoxMenuItem jCBMIVerbose;
    private JCheckBoxMenuItem jCBMIInspectV;
    private final JMenuItem jMICompile = new JMenuItem("Compile temporary Gamelog");
    private final JMenuItem jMIClear = new JMenuItem("Delete all temporary Gamelogs");
    private final JMenu jMLoad = new JMenu("Load");
    private final JMenuItem jMILoad = new JMenuItem("Load Gamelog");

    private final JPanel jPMain = new JPanel();
    private final JTextField jTFNextAction = new JTextField("0");
    private final JLabel jLNumberActions = new JLabel("/ 0");
    private final JButton jBJump = new JButton("Jump to Move");
    private final JButton jBNextAction = new JButton("no next action");
    private final JButton jBPreviousAction = new JButton("no previous action");

    private LogSessionContainer currentLog;
    private int counter;

    /**
     * The GUI for the logManager
     *
     * @param logManager a logManager, mostly used for settings and file paths
     * @param gameBoard a gameBoard, used to display a log
     */
    public LogManagerGUI(LogManager logManager, GameBoard gameBoard) {
        this.logManager = logManager;
        this.gameBoard = gameBoard;

        if (logManager.getInspectV()) {
            fetch_paX();
        }

        initialize();

    }

    private void fetch_paX() {
        XArenaFuncs m_xfun = gameBoard.getArena().m_xfun;
        XArenaButtons m_xab = gameBoard.getArena().m_xab;
        PlayAgent[] paVector, qaVector;

        try {
            paVector = m_xfun.fetchAgents(m_xab);
            AgentBase.validTrainedAgents(paVector, gameBoard.getStateObs().getNumPlayers());
            qaVector = m_xfun.wrapAgents(paVector, m_xab, gameBoard.getStateObs());
            paX = qaVector[0];
        } catch (RuntimeException e) {
            gameBoard.getArena().showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }
    /**
     * creates a Menu Bar, Buttons...
     */
    private void initialize() {
        //Menu Bar
        jFMain.setJMenuBar(jMBMain);

        //options
        jMBMain.add(jMOptions);

        jCBMILoggingEnabled = new JCheckBoxMenuItem("Logging enabled", logManager.loggingEnabled);
        jCBMILoggingEnabled.addActionListener((e) ->
            {
                if(!logManager.running()) {
                    logManager.loggingEnabled = jCBMILoggingEnabled.isSelected();
                } else {
                    jCBMILoggingEnabled.setState(logManager.loggingEnabled);
                    JOptionPane.showMessageDialog(null, "Cant disable logging while logging is in progress.");
                }
            }
        );

        jCBMIAdvancedLogging = new JCheckBoxMenuItem("Advanced logging", logManager.advancedLogging);
        jCBMIAdvancedLogging.addActionListener((e) ->
            {
                if(!logManager.running()) {
                    logManager.advancedLogging = jCBMIAdvancedLogging.isSelected();
                } else {
                    jCBMIAdvancedLogging.setState(logManager.advancedLogging);
                    JOptionPane.showMessageDialog(null, "Cant change logging method while logging is in progress.");
                }
        });

        jCBMIVerbose= new JCheckBoxMenuItem("Verbose", logManager.verbose);
        jCBMIVerbose.addActionListener((e) ->
                logManager.verbose = jCBMIVerbose.isSelected());

        jCBMIInspectV= new JCheckBoxMenuItem("InspectV with Agent X", logManager.inspectV);
        jCBMIInspectV.addActionListener((e) ->
                logManager.inspectV = jCBMIInspectV.isSelected());

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
                logManager.saveLogSessionContainer(logSessionContainer, gameBoard.getArena().getGameName());

                if (verbose) {
                    System.out.println("LogManager: compiled temp log successfully");
                }
            }
        });

        jMIClear.addActionListener((e) ->
        {
            //delete all temporary log files
            purgeDirectory(new File(logManager.tempPath));

            if (verbose) {
                System.out.println("LogManager: cleared temp log successfully");
            }
        });

        jMOptions.add(jCBMILoggingEnabled);
        //jMOptions.add(jCBMIAdvancedLogging);  // we stay always with initial logManager.advancedLogging=true
        //jMOptions.add(jCBMIVerbose);          // we stay always with initial logManager.verbose=true
        jMOptions.add(jCBMIInspectV);
        jMOptions.add(jMICompile);
        jMOptions.add(jMIClear);

        //load
        jMBMain.add(jMLoad);

        jMILoad.addActionListener((e) ->
        {
            //load a new .gamelog File
            String gameName = gameBoard.getArena().getGameName();
            String logPath = logManager.filePath + "\\" + gameName;
            String subDir = logManager.getSubDir();
            if (subDir != null){
                File file = new File(logPath+"\\"+subDir);
                if (file.exists()) {
                    logPath += "\\" + subDir;
                }
            }
            JFileChooser fileChooser = new JFileChooser(logPath);

            fileChooser.setFileFilter(new FileNameExtensionFilter(gameName + " Gamelog", "gamelog"));
            Action details = fileChooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);

            int succes = fileChooser.showOpenDialog(null);

            if (succes == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    FileInputStream fis = new FileInputStream(selectedFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    LogSessionContainer tempLog = (LogSessionContainer) ois.readObject();
                    fis.close();
                    ois.close();

                    if (tempLog.stateObservations.get(0) instanceof StateObserverCube) {
                        //symptomatic fix for RubiksCube, where the elements can be StateObserverCube or StateObserverCubeCleared:
                        //it is however guaranteed that each object is an instance of (super-)class StateObserverCube:
                        if (!(gameBoard.getStateObs() instanceof StateObserverCube)){
                            JOptionPane.showMessageDialog(null, "Please only select logs for the current GameBoard.");
                            return;
                        }
                    } else {
                        //check if gameBoard and log have the same StateObserver Type
                        if(!tempLog.stateObservations.get(0).getClass().equals(gameBoard.getStateObs().getClass())) {
                            JOptionPane.showMessageDialog(null, "Please only select logs for the current GameBoard.");
                            return;
                        }
                    }
                    currentLog = tempLog;

                    gameBoard.clearBoard(true,true, null);
                    loadBoard(0);

                    fetch_paX();        // re-fetch agent X (in case it or its parameters have changed)

                    counter = 0;
                    jTFNextAction.setEnabled(true);
                    jLNumberActions.setText("/ " + (currentLog.stateObservations.size()));
                    jBJump.setEnabled(true);
                } catch (IOException | ClassNotFoundException io_gnore) {
                    if(verbose) {
                        System.out.println("LogManager: something went wrong, cant load the log.");
                        io_gnore.printStackTrace();
                    }
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
                jumpPosition()
        );
        jTFNextAction.addActionListener((e) ->
                jumpPosition()
        );

        jBPreviousAction.setEnabled(false);
        jBPreviousAction.setForeground(Color.black);
        jBPreviousAction.setBackground(Color.orange);
        jBPreviousAction.addActionListener((e) ->
                loadBoard(counter - 1));

        jBNextAction.setEnabled(false);
        jBNextAction.setForeground(Color.black);
        jBNextAction.setBackground(Color.orange);
        jBNextAction.addActionListener((e) ->
                loadBoard(counter + 1));

        JPanel JPAction = new JPanel();
        JPAction.setLayout(new GridLayout(1, 2, 10, 10));

        JPAction.add(jTFNextAction);
        JPAction.add(jLNumberActions);

        jPMain.add(jBJump);
        jPMain.add(JPAction);

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
     * move the LogManagerGUI to the front
     */
    public void show() {
        jFMain.setVisible(true);
        jFMain.setState(Frame.NORMAL);
        jFMain.toFront();
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
    		boolean showValue = gameBoard.getArena().m_xab.getShowValueOnGameBoard();
    		StateObservation currentState = currentLog.stateObservations.get(position);
    		if (logManager.getInspectV() && paX != null) {
    		    currentState.setAvailableActions();
    		    if (currentState.getNumAvailableActions()>0) {
                    paX.resetAgent();
                    Types.ACTIONS_VT paact = paX.getNextAction2(currentState,false,true);
                    currentState.storeBestActionInfo(paact);
                }
            }
            gameBoard.updateBoard(currentState, true, showValue);
            jTFNextAction.setText("" + (position + 1));

            if(currentLog.stateObservations.size() > position + 1) {
                jBNextAction.setEnabled(true);
                //jBNextAction.setText("advance (" + currentLog.actions.get(position+1) + ")");
                jBNextAction.setText("Advance");
            } else {
                jBNextAction.setEnabled(false);
                jBNextAction.setText("no next action");
            }

            if(position > 0) {
                jBPreviousAction.setEnabled(true);
                //jBPreviousAction.setText("revert (" + currentLog.actions.get(position) + ")");
                jBPreviousAction.setText("Revert");
            } else {
                jBPreviousAction.setEnabled(false);
                jBPreviousAction.setText("no previous action");
            }
        }
    }

    /**
     *  jump to the position in currentLog specified by {@link #jTFNextAction}
     */
    private void jumpPosition() {
        try {
            int position = Integer.parseInt(jTFNextAction.getText()) - 1;
            if (position < currentLog.stateObservations.size() && position >= 0) {
                loadBoard(position);
            } else {
                jTFNextAction.setText("" + (counter + 1));
            }
        } catch (NumberFormatException ignore) {
        }
    }

    void purgeDirectory(File dir) {
        for (File file: dir.listFiles()) {
            if (file.isDirectory()) purgeDirectory(file);
            file.delete();
        }
    }
}
