package controllers.SB3;

import agentIO.JFileChooserApprove;
import agentIO.ProgressTrackingObjectInputStream;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.SB3.HttpServer.SimpleHttpServer;
import game.rules.play.moves.nonDecision.effect.requirement.Do;
import games.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tools.Types;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class SB3Agent extends AgentBase implements PlayAgent, Serializable {
    private RLEnvironment rlEnvironment;
    private SimpleHttpServer simpleHttpServer;
    private XNTupleFuncs xnTupleFuncs;
    private List<PlayAgent> enemyAgents;
    private boolean oneHot;

    private GameBoard gameBoard;

    public SB3Agent(XNTupleFuncs xnTupleFuncs, List<String> enemyAgentsFilePaths, Arena arena, SimpleHttpServer simpleHttpServer, boolean oneHot) {
        this.xnTupleFuncs = xnTupleFuncs;
        this.oneHot = oneHot;
        enemyAgents = loadAgents(enemyAgentsFilePaths, arena);

        rlEnvironment = new RLEnvironmentConnector(this.xnTupleFuncs, enemyAgents, this, oneHot);
        this.gameBoard = arena.getGameBoard();

        //start server
        /*try {
            simpleHttpServer = new SimpleHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        this.simpleHttpServer = simpleHttpServer;
        simpleHttpServer.setRlEnvironment(rlEnvironment); // TODO: who owns the rl environment?
        // creat SB3 env and Agent
        createEnv(oneHot);
    }

    private static List<PlayAgent> loadAgents(List<String> enemyAgentsFilePaths, Arena arena) {
        /*
        List<PlayAgent> playAgents = new ArrayList<>();
        for(String path: enemyAgentsFilePaths) {
            playAgents.add(arena.loadAgent(path));
        }
        return playAgents; */
        List<PlayAgent> playAgents = new ArrayList<>();
        for(String path: enemyAgentsFilePaths) {
            try {
                playAgents.add(loadAgent(path));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return playAgents;
    }

    private static PlayAgent loadAgent(String enemyAgentsFilePaths) throws IOException {
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        File file = null;
        PlayAgent pa = null;

        JFileChooserApprove fc = new JFileChooserApprove();
        fc.setCurrentDirectory(new File(enemyAgentsFilePaths));

        try {
            file = new File(enemyAgentsFilePaths);
            fis = new FileInputStream(enemyAgentsFilePaths);
        } catch (IOException e) {
            System.out.println("[ERROR: Could not open file " + enemyAgentsFilePaths + " !]");
            //e.printStackTrace();
            throw e;
        }


        if (fis != null) {
            GZIPInputStream gs;
            try {
                gs = new GZIPInputStream(fis);
            } catch (IOException e1) {
                System.out.println("[ERROR: Could not create ZIP-InputStream for" + enemyAgentsFilePaths + " !]");
                throw e1;
            }

            long fileLength = estimateGZIPLength(file);
            final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
                    gs, new agentIO.IOProgress(fileLength));
            try {
                ois = new ObjectInputStream(ptis);
            } catch (IOException e1) {
                ptis.close();
                System.out.println("[ERROR: Could not create ObjectInputStream for" + enemyAgentsFilePaths + " !]");
                throw e1;
            }

//			final JDialog dlg = createProgressDialog(ptis, "Loading...");

            pa = transformObjectToPlayAgent(ois, fis, enemyAgentsFilePaths);

//			disposeProgressDialog(dlg);

        }

        return pa;
    }

    public static PlayAgent transformObjectToPlayAgent(ObjectInputStream ois, FileInputStream fis, String filePath/*,JDialog dlg*/)
    {
        PlayAgent pa;
        try {
            Object obj = ois.readObject();
            if (obj instanceof PlayAgent) {
                pa = (PlayAgent) obj;
                pa.setAgentFile(filePath);
                pa.instantiateAfterLoading();	// special treatment of agents after loading (if necessary)
                // [instantiateAfterLoading replaces completely the long and complicated switch statement we had here before (!)]
            } else {
//				disposeProgressDialog(dlg);
                System.out.println("ERROR: Agent class "+obj.getClass().getName()+" loaded from "
                            + filePath + " not processable" + "Unknown Agent Class");
                System.out.println("[ERROR: Could not load agent from "
                            + filePath + "!]");
                throw new ClassNotFoundException("ERROR: Unknown agent class");
            }

            // Some older agents on disk might not have ParOther m_oPar.
            // If this is the case, replace the null value with a default ParOther.
            if (pa.getParOther() == null) {
                ((AgentBase) pa).setDefaultParOther();
            }
            if (pa.getParReplay() == null) {
                ((AgentBase) pa).setDefaultParReplay();
            }
            if (pa.getParWrapper() == null) {
                ((AgentBase) pa).setDefaultParWrapper(pa.getParOther());
            }

//			disposeProgressDialog(dlg);
//			arenaGame.setProgress(null);
            System.out.println("Done.");
        } catch (IOException e) {
//			disposeProgressDialog(dlg);
            System.out.println("ERROR: " + e.getMessage() +
                        e.getClass().getName());
            System.out.println("[ERROR: Could not open file " + filePath + " !]");
            //e.printStackTrace();
            pa=null;
        } catch (ClassNotFoundException e) {
//			disposeProgressDialog(dlg);
            System.out.println("ERROR: Class not found: " + e.getMessage() +
                        e.getClass().getName());
            //e.printStackTrace();
            pa=null;
        } catch (AssertionError e) {
//			disposeProgressDialog(dlg);
            System.out.println("Instantiation failed: " + e.getMessage() + e.getClass().getName());
            //e.printStackTrace();
            pa=null;
        } finally {
            if (ois != null)
                try {
                    ois.close();
                } catch (IOException e) {
                }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                }
        }
        return pa;
    }

    public static int estimateGZIPLength(File f) {
        RandomAccessFile raf;
        int fileSize = 0;
        try {
            raf = new RandomAccessFile(f, "r");
            raf.seek(raf.length() - 4);
            byte[] bytes = new byte[4];
            raf.read(bytes);
            fileSize = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (fileSize < 0)
                fileSize += (1L << 32);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileSize;
    }


    @Override
    public boolean trainAgent(StateObservation stateObservation) {
       rlEnvironment.initiateNewGame(stateObservation);
        // http requst start training

        return true;
    }

    // Trains Agent for total number of time steps
    public void learn(int totalTimeSteps) {
        synchronized (this) {
            learnHttpRequest(totalTimeSteps);
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Erwacht!");
        }
    }

    private void learnHttpRequest(int totalTimeSteps) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("totalTimeSteps", totalTimeSteps);
        try {
            String response = postRequest("http://127.0.0.1:8095", "learn", requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: better
        }
    }

    private void createEnv(boolean oneHot) {
        int observationVectorSize = getObservationVectorSize();
        int actionSpaceSize = getStartSate().getNumAvailableActions();

        createEnvHttpRequest(observationVectorSize, actionSpaceSize);
    }

    private void createEnvHttpRequest(int observationVectorSize, int actionSpaceSize) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("observationVectorSize", observationVectorSize);
        requestBody.put("actionSpaceSize", actionSpaceSize);

        try {
            String response = postRequest("http://127.0.0.1:8095", "createEnv", requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: better
        }

    }

    private double predictHttpRequest(List<Double> observation) throws Exception {
        String response;
        JSONArray requestBody = new JSONArray(observation);

        System.out.println(requestBody.toString());
        response = postRequest("http://127.0.0.1:8095", "predict", requestBody);
        System.out.println(response);
        return Double.parseDouble(response);
    }

    private String postRequest(String host, String path, Object requestBody) throws Exception {
        URI uri = new URI(host + "/" + path);
        if(!(requestBody instanceof JSONObject || requestBody instanceof JSONArray)) {
            throw new Exception("requestBody must be of type JSONObject or JSONArray");
        }
        HttpRequest request  = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json; charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public StateObservation getStartSate() {
        StateObservation stateObservation = (this.getParOther().getChooseStart01())
                ? gameBoard.chooseStartState(this) : gameBoard.getDefaultStartState(null);
        return stateObservation;
    }

    private BoardVector getBoardVector(StateObservation stateObservation) {
        return oneHot ? xnTupleFuncs.getOneHotBoardVector(xnTupleFuncs.getStandardPerspectivesBoardVector(stateObservation)) :
                xnTupleFuncs.getBoardVector(stateObservation);
    }

    private int getObservationVectorSize() {
        return oneHot ? xnTupleFuncs.getOneHotSize() : xnTupleFuncs.getNumCells();
    }


    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent) {
        int[] boardVector = getBoardVector(sob).bvec;
        List<Double> observation= new ArrayList<>();
        for(int i : boardVector) {
            observation.add((double) i);
        }
        System.out.println(xnTupleFuncs.getBoardVector(sob));
        double action = 0;
        try {
            action = predictHttpRequest(observation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Types.ACTIONS_VT((int) action);
    }
}
