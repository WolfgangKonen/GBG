package games;

import tools.Types;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is responsible for logging games
 * 
 * @author Johannes Kutsch, TH Köln, Nov'16
 * 
 * @see LogManagerGUI
 */
public class LogManager {
    public boolean loggingEnabled = false;  //enables or disables logging
    public boolean advancedLogging = true; //if advancedLogging is enabled every new logEntry is saved to a temporary file
                                           //the log is not lost when a crash occurs
                                           //call generateLogSessionContainerFromFile(path of temp folder) to combine all temporary files and generate the log
    public boolean verbose = true;

    public String filePath = "logs";
    public String tempPath = "logs\\temp";
    private String subDir = null;

    private HashMap<Integer, Integer> counter = new HashMap<>(); //saves a counter for each sessionid
    private HashMap<Integer, List<LogContainer>> simpleLoggingContainers = new HashMap<>();

    /**
     * use this constructor when you only run one instance of LogManager
     * it uses "src\games\Logs" as default filePath
     * and "src\games\Logs\temp" as default tempPath
     */
    public LogManager() {
		tools.Utils.checkAndCreateFolder(filePath);
		tools.Utils.checkAndCreateFolder(tempPath);
    }

    /**
     * needed for running multiple instances LogManager at the same time
     * every LogManager needs a different temPath
     *
     * @param filePath the Path where finished logs are saved
     * @param tempPath the Path where temp files for advanced logging are saved
     */
    public LogManager(String filePath, String tempPath) {
        this.filePath = filePath;
        this.tempPath = tempPath;

		tools.Utils.checkAndCreateFolder(filePath);
		tools.Utils.checkAndCreateFolder(tempPath);
    }


    /**
     * Adds a new log entry
     *
     * @param action the action that was used to advance stateObservation
     * @param stateObservation the advanced stateObservation
     * @param sessionid the id of the current logsession
     */
    public void addLogEntry(Types.ACTIONS action, StateObservation stateObservation, int sessionid) {
        //sessionid = -1 => session is invalid, started while logging was disabled
        if(loggingEnabled && sessionid != -1) {
            LogContainer logContainer = new LogContainer(action, stateObservation);

            if(advancedLogging) {
                if(!counter.containsKey(sessionid)) {
                	// this should normally not happen. It happens only if addLogEntry is
                	// called without a prior newLoggingSession. We print out a warning and 
                	// return. (This is less disruptive than throwing an exception.)
                	System.out.println("*** Logging: Invalid sessionid - cannot add log entry!! ***");
                	return;
                    //throw new RuntimeException("Invalid sessionid, start a new  logging session to get a valid sessionid");
                }

                try {
                    FileOutputStream fos = new FileOutputStream(tempPath + "\\temp_" + sessionid + "\\" + counter.get(sessionid) + ".temp");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(logContainer);
                    fos.close();
                    oos.close();
                    counter.put(sessionid, counter.get(sessionid) + 1);
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            } else {
                if(!simpleLoggingContainers.containsKey(sessionid)) {
                	// this should normally not happen. It happens only if addLogEntry is
                	// called without a prior newLoggingSession. We print out a warning and 
                	// return. (This is less disruptive than throwing an exception.)
                	System.out.println("*** Logging: Invalid sessionid - cannot add log entry!! ***");
                	return;
                    //throw new RuntimeException("Invalid Session ID");
                }

                simpleLoggingContainers.get(sessionid).add(logContainer);
            }
        }
    }

    /**
     * starts a new logging session, for example when a new game is started
     *
     * @param stateObservation the initial StateObservation
     */
    public int newLoggingSession(StateObservation stateObservation) {
        //find first empty sessionid
        int sessionid = -1;

        if(loggingEnabled) {
            sessionid = 0;
            if(advancedLogging) {
                while (counter.containsKey(sessionid)) {
                    sessionid++;
                }
            } else {
                while (simpleLoggingContainers.containsKey(sessionid)) {
                    sessionid++;
                }
            }

            LogContainer logContainer = new LogContainer(null, stateObservation.copy());

            if(advancedLogging) {
                while(tools.Utils.checkAndCreateFolder(tempPath + "\\temp_" + sessionid)) {
                    sessionid++;
                }

                counter.put(sessionid, 0);

                try {
                    FileOutputStream fos = new FileOutputStream(tempPath + "\\temp_" + sessionid + "\\" + counter.get(sessionid) + ".temp");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(logContainer);
                    fos.close();
                    oos.close();
                    counter.put(sessionid, counter.get(sessionid) + 1);
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            } else {
                simpleLoggingContainers.put(sessionid, new ArrayList<>());
                simpleLoggingContainers.get(sessionid).add(logContainer);
            }

            if(verbose) {
                System.out.println("LogManager: Starting new logging session with id: " + sessionid);
            }
        }

        return sessionid;
    }

    /**
     * ends a logging session, for example when a game is finished
     *
     * @param sessionid the id of the current logsession
     */
    public void endLoggingSession(int sessionid) {
        if (loggingEnabled) {
            LogSessionContainer logSessionContainer;

            if(verbose) {
                System.out.println("LogManager: Ending logging session with id: " + sessionid);
            }
            if(advancedLogging) {
                if(!counter.containsKey(sessionid)) {
                    throw new RuntimeException("Invalid Session ID");
                }

                logSessionContainer = generateLogSessionContainerFromFile(tempPath + "\\temp_" + sessionid);
                counter.remove(sessionid);
            } else {
                if(!simpleLoggingContainers.containsKey(sessionid)) {
                    throw new RuntimeException("Invalid Session ID");
                }

                logSessionContainer = new LogSessionContainer();
                List<LogContainer> logContainers = simpleLoggingContainers.get(sessionid);
                logContainers.forEach(logSessionContainer::addLogEntry);

                simpleLoggingContainers.remove(sessionid);
            }

            saveLogSessionContainer(logSessionContainer);
        }
    }

    /**
     * generates a LogSessionContainer from a directory that contains multiple files with one LogContainer each
     *
     * @param path the path of the directory
     * @return the LogSessionContainer
     */
    public LogSessionContainer generateLogSessionContainerFromFile(String path) {
        LogSessionContainer logSessionContainer = new LogSessionContainer();

        File sessionFolder = new File(path);
        if (sessionFolder.exists()) {
            int length = sessionFolder.listFiles().length;
            for (int i = 0; i < length; i++) {
                try {
                    FileInputStream fis = new FileInputStream(path + "\\" + i + ".temp");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    LogContainer logContainer = (LogContainer) ois.readObject();
                    fis.close();
                    ois.close();
                    logSessionContainer.addLogEntry(logContainer);
                    new File(path +  "\\" + i + ".temp").delete();
                } catch (IOException | ClassNotFoundException ignore) {
                    ignore.printStackTrace();
                }
            }
            sessionFolder.delete();
        }

        return logSessionContainer;
    }


    /**
     * saves a LogSessionContainer to a .gamelog file
     *
     * @param logSessionContainer the LogSessionContainer
     */
    public void saveLogSessionContainer(LogSessionContainer logSessionContainer) {
        if (logSessionContainer.stateObservations.size() > 0) {
            String saveDirectory = filePath + "\\" + logSessionContainer.stateObservations.get(0).getName();
            if(subDir != null && !subDir.equals("")) {
                saveDirectory = filePath + "\\" + logSessionContainer.stateObservations.get(0).getName() + "\\" + subDir;
                //System.out.println(saveDirectory);
            }

    		tools.Utils.checkAndCreateFolder(saveDirectory);

            String sessionFolderName = saveDirectory + "\\" + logSessionContainer.stateObservations.get(0).getName() + "_" + getCurrentTimeStamp();
            String sessionFolderNameSuffix = "";

            //test if File already exists
            int i = 1;
            File sessionFolder = new File(sessionFolderName + sessionFolderNameSuffix + ".gamelog");
            while (sessionFolder.exists()) {
                sessionFolderNameSuffix = " (" + i + ")";
                sessionFolder = new File(sessionFolderName + sessionFolderNameSuffix + ".gamelog");
                i++;
            }

            try {
                FileOutputStream fos = new FileOutputStream(sessionFolder);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(logSessionContainer);
                fos.close();
                oos.close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        } else {
            System.out.println("LogManager: Log not saved because it is empty");
        }
    }

    /**
     * generates String containing the current timestamp
     *
     * @return the timestamp
     */
    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//-SSS
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

//    /**
//     * checks if a folder exists
//     *
//     * @param filePath the folder Path
//     * @return true if the folder exists
//     */
//    private boolean checkFolder(String filePath) {
//        return new File(filePath).exists();
//    }

    public boolean running() {
        if(counter.size() > 0 || simpleLoggingContainers.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setSubDir(String subDir){
        this.subDir = subDir;
    }

    public String getSubDir(){
        return subDir;
    }

}

/**
 * The LogContainer is used to save a log entry, a log entry is a pair of an Action and the new StateObservation
 *
 * @author Johannes Kutsch, 30.04.2017
 */
class LogContainer implements Serializable {
    public Types.ACTIONS action;
    public StateObservation stateObservation;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

    public LogContainer(Types.ACTIONS action, StateObservation stateObservation) {
        this.action = action;
        this.stateObservation = stateObservation;
    }
}

/**
 * The LogSessionContainer is used to save a logsession, a log session consists of multiple Actions and StateObservations
 *
 * @author Johannes Kutsch, 30.04.2017
 */
class LogSessionContainer implements Serializable {
    public List<Types.ACTIONS> actions = new ArrayList<>();
    public List<StateObservation> stateObservations = new ArrayList<>();

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

    public void addLogEntry(LogContainer logContainer) {
        actions.add(logContainer.action);
        stateObservations.add(logContainer.stateObservation);
    }

    public void addLogEntry(Types.ACTIONS action, StateObservation stateObservation) {
        actions.add(action);
        stateObservations.add(stateObservation);
    }
}