package controllers.SB3;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.SB3.HttpServer.SimpleHttpServer;
import games.*;
import org.json.JSONArray;
import org.json.JSONObject;
import params.ParOther;
import params.ParSB3;
import tools.Types;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class SB3Agent extends AgentBase implements PlayAgent, Serializable {
    transient private RLEnvironmentConnector rlEnvironment;
    transient private SimpleHttpServer simpleHttpServer = SimpleHttpServer.getInstance();
    transient private XNTupleFuncs xnTupleFuncs;
    transient private StateObservationVectorFuncs stateObservationVectorFuncs;
    transient private List<PlayAgent> enemyAgents;
    transient private XArenaFuncs xArenaFuncs;
    transient private XArenaButtons xArenaButtons;
    transient private Arena arena;
    private boolean selfPlay;
    private ParSB3 parSB3;
    transient private ParOther parOther; // TODO: remove?
    private String agentType;


    private int playerNumber;
    private UUID id;
    private int numberPlayers;


    transient private GameBoard gameBoard;

    public SB3Agent(String name, ParSB3 parSB3, ParOther oPar, XNTupleFuncs xnTupleFuncs, StateObservationVectorFuncs stateObservationVectorFuncs, XArenaButtons m_xab, Arena arena, XArenaFuncs xArenaFuncs, int playerNumber) {
        super(name);
        this.id = UUID.randomUUID();
        this.parSB3 = parSB3;
        this.parOther = oPar;
        this.xnTupleFuncs = xnTupleFuncs;
        this.stateObservationVectorFuncs = stateObservationVectorFuncs;
        this.selfPlay = parSB3.selfPlay;
        this.xArenaButtons = m_xab;
        this.arena = arena;
        this.xArenaFuncs = xArenaFuncs;
        this.playerNumber = playerNumber; //TODO: Initialize after loading
        this.agentType = parSB3.agentType;

        this.numberPlayers = arena.makeXNTupleFuncs().getNumPlayers(); //TODO in stateObservationVectorFuncs?

         // only when start training


        rlEnvironment = new RLEnvironmentConnector(this.xnTupleFuncs, this.stateObservationVectorFuncs, enemyAgents, this, selfPlay, playerNumber);
        this.gameBoard = m_xab.m_arena.getGameBoard();

        //start server
        /*try {
            simpleHttpServer = new SimpleHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        simpleHttpServer.setRlEnvironment(rlEnvironment); // TODO: who owns the rl environment?
        // creat SB3 env and Agent

    }

    private void initialize() {
        enemyAgents = loadAgents();
        setAgentState(AgentState.INIT);
        createSB3Agent(parSB3);
    }

    @Override
    public boolean instantiateAfterLoading() {
        return true;
    }

    @Override
    public void fillParamTabsAfterLoading(int n, Arena m_arena) {
        this.arena = m_arena;
        playerNumber = n;
        xnTupleFuncs = m_arena.makeXNTupleFuncs();
        stateObservationVectorFuncs = m_arena.makeStateObservationVectorFuncs();
        gameBoard = m_arena.getGameBoard();
        xArenaButtons = m_arena.m_xab;
        xArenaFuncs = m_arena.m_xfun;
        enemyAgents = loadAgents();
        rlEnvironment = new RLEnvironmentConnector(this.xnTupleFuncs, this.stateObservationVectorFuncs, enemyAgents, this, selfPlay, playerNumber);
        try {
            loadSB3AgentHttpRequest();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private List<PlayAgent> loadAgents() {
        /*
        List<PlayAgent> playAgents = new ArrayList<>();
        for(String path: enemyAgentsFilePaths) {
            playAgents.add(arena.loadAgent(path));
        }
        return playAgents; */

        /*
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
         */
        List<PlayAgent> enemies = new ArrayList<PlayAgent>();
        for(int n = 0; n < xnTupleFuncs.getNumPlayers(); n++) {
            try {
                if (n != playerNumber)
                    enemies.add(this.xArenaFuncs.fetchAgent(n, xArenaButtons.getSelectedAgent(n), xArenaButtons));
            } catch (Exception exception) {
                selfPlay = true;
                rlEnvironment.setSelfPlayTrue();
                System.out.println("Enemy Agent not Inizilaized. SB3 agent uses self play now.");
                arena.showMessage("Enemy Agent not Inizilaized. SB3 agent uses self play now.", "Enemy Agent not Initialized", JOptionPane.WARNING_MESSAGE);
            }
        }
        System.out.println("Enemies loaded: ");
        for (PlayAgent enemy: enemies) {
            System.out.println(enemy.getName());
        }
        System.out.println();
        return enemies;
    }

    @Override
    public boolean trainAgent(StateObservation stateObservation) {
       rlEnvironment.initiateNewGame(stateObservation);
        // http requst start training

        return true;
    }

    // Trains Agent for total number of time steps
    public void learn() {
        initialize();
        int totalTimeSteps = parSB3.trainTimeSteps;
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
        String path = "agents/" + id.toString() + "/learn";

        try {
            String response = postRequest("http://127.0.0.1:8095", path, requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSB3Agent(ParSB3 parSB3) {
        int actionSpaceSize = getStartSate().getAllAvailableActions().size();

        createSB3AgentHttpRequest(actionSpaceSize, parSB3.agentType, parSB3.parSB3Base, parSB3.parSB3Police, parSB3.parSB3Network);
    }

    private void createSB3AgentHttpRequest(int actionSpaceSize, String agentType, Map<String, Object> baseParameters, Map<String, Object> policyParameters, Map<String, Object> networkParameters) {
        JSONObject requestBody = new JSONObject();
        JSONObject environmentParameters = new JSONObject();
        requestBody.put("agent_id", id);
        requestBody.put("agentType", agentType);
        requestBody.put("baseParameters", baseParameters);
        requestBody.put("policyParameters", policyParameters);
        requestBody.put("networkParameters", networkParameters);

        environmentParameters.put("actionSpaceSize", actionSpaceSize);
        environmentParameters.put("observationRangeStarts", stateObservationVectorFuncs.getStateObservationVectorStarts());
        environmentParameters.put("observationRangeSizes", stateObservationVectorFuncs.getObservationVectorRanges());


        requestBody.put("environmentParameters", environmentParameters);
        System.out.println(requestBody.toString());

        try {
            String response = postRequest("http://127.0.0.1:8095", "agents", requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: better
        }

    }

    private double predictHttpRequest(int[] observation) throws Exception {
        String response;
        JSONArray requestBody = new JSONArray(observation);
        String path = "agents/"+ id.toString() + "/predict";

        System.out.println(requestBody.toString());
        response = postRequest("http://127.0.0.1:8095", path, requestBody);
        System.out.println(response);
        return Double.parseDouble(response);
    }

    private void saveSB3AgentHttpRequest() throws Exception {
        String response;
        String path = "agents/"+ id.toString() + "/save";

        response = postRequest("http://127.0.0.1:8095", path, new JSONObject());
        System.out.println(response);
    }

    private void loadSB3AgentHttpRequest() throws Exception {
        String response;
        JSONObject requestBody = new JSONObject();
        requestBody.put("agentType", this.agentType);
        String path = "agents/"+ id.toString() + "/load";

        System.out.println(requestBody.toString());
        response = postRequest("http://127.0.0.1:8095", path, requestBody);
        System.out.println(response);
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
        StateObservation stateObservation = gameBoard.getDefaultStartState(null);
        return stateObservation;
    }

    public int[] getPlayerVector(StateObservation stateObservation) {
        int[] vector = new int[stateObservation.getNumPlayers()];
        vector[stateObservation.getPlayer()] = 1;
        return vector;
    }

    private int[] getObservationVector(StateObservation stateObservation) {
        /*List<Double> observationVector = new ArrayList<>();
        BoardVector boardVector = useStandardPerspective ? xnTupleFuncs.getStandardPerspectivesBoardVector(stateObservation) :
                xnTupleFuncs.getBoardVector(stateObservation);
        if (oneHot) boardVector = xnTupleFuncs.getOneHotBoardVector(boardVector);

        for(int i: boardVector.bvec) {
            observationVector.add((double) i);
        }
        if (!useStandardPerspective) {
            for (int i: getPlayerVector(stateObservation)) {
                observationVector.add((double) i);
            }
        }

        return observationVector;*/
        return stateObservationVectorFuncs.getStateObservationVector(stateObservation);
    }


    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent) {
        int[] observation= getObservationVector(sob);

        System.out.println(Arrays.toString(observation));
        double action = 0;
        try {
            action = predictHttpRequest(observation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: get next best action instead of random
        List<Types.ACTIONS> validActions = sob.getAvailableActions();
        if (!validActions.contains(new Types.ACTIONS((int) action))) {
            System.out.println("Already occupied.");
            System.out.println("Tried action " + (int) action);
            return new Types.ACTIONS_VT(validActions.get(0).toInt());
        }

        return new Types.ACTIONS_VT((int) action);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        try {
            saveSB3AgentHttpRequest();
        }  catch (Exception exception) {
            System.out.println("Could not save SB3 agent on python side");
            exception.printStackTrace();
        }
        oos.defaultWriteObject();
    }
}
