package controllers.SB3;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.SB3.HttpServer.ServerConfig;
import controllers.SB3.HttpServer.SimpleHttpServer;
import games.*;
import org.json.JSONArray;
import org.json.JSONObject;
import params.ParOther;
import params.ParSB3;
import tools.Types;

import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
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
    transient private XArenaFuncs.GameProgressor gameProgressor;
    transient private int moveCounter  = 0;


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

        this.gameBoard = m_xab.m_arena.getGameBoard();

        setAgentState(AgentState.INIT);
    }

    private void initialize() {

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
        rlEnvironment = new RLEnvironmentConnector(this.stateObservationVectorFuncs, enemyAgents, this, playerNumber);
        try {
            loadSB3AgentHttpRequest();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private List<PlayAgent> loadAgents() {
        List<PlayAgent> enemies = new ArrayList<PlayAgent>();
        for(int n = 0; n < stateObservationVectorFuncs.getNumPlayers(); n++) {
            try {
                if (n != playerNumber && !selfPlay)
                    enemies.add(this.xArenaFuncs.fetchAgent(n, xArenaButtons.getSelectedAgent(n), xArenaButtons));
                else if (n != playerNumber) {
                    enemies.add(this);
                }
            } catch (Exception exception) {
                enemies.add(this);
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
      throw new RuntimeException("SB3 Agent only supports learn method for training.");
    }

    // Trains Agent for total number of time steps
    public void learn(XArenaFuncs.GameProgressor gameProgressor) {
        // initialize
        enemyAgents = loadAgents();
        this.gameProgressor = gameProgressor;

        rlEnvironment = new RLEnvironmentConnector(this.stateObservationVectorFuncs, enemyAgents, this, playerNumber);
        simpleHttpServer.setRlEnvironment(rlEnvironment);

        createSB3Agent(parSB3);

        //start learning
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
            String response = postRequest(path, requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSB3Agent(ParSB3 parSB3) {

        createSB3AgentHttpRequest(parSB3.agentType, parSB3.parSB3Base, parSB3.parSB3Police, parSB3.parSB3Network);
    }

    private void createSB3AgentHttpRequest(String agentType, Map<String, Object> baseParameters, Map<String, Object> policyParameters, Map<String, Object> networkParameters) {
        JSONObject requestBody = new JSONObject();
        JSONObject environmentParameters = new JSONObject();
        requestBody.put("agent_id", id);
        requestBody.put("agentType", agentType);
        requestBody.put("baseParameters", baseParameters);
        requestBody.put("policyParameters", policyParameters);
        requestBody.put("networkParameters", networkParameters);

        environmentParameters.put("actionSpaceSize", getStartSate().getAllAvailableActions().size()); // TODO not with get start state
        environmentParameters.put("observationRangeStarts", stateObservationVectorFuncs.getStateObservationVectorStarts());
        environmentParameters.put("observationRangeSizes", stateObservationVectorFuncs.getObservationVectorRanges());


        requestBody.put("environmentParameters", environmentParameters);
        System.out.println(requestBody.toString());


        postRequest("agents", requestBody);

    }

    private double predictHttpRequest(int[] observation) throws Exception {
        String response;
        JSONArray requestBody = new JSONArray(observation);
        String path = "agents/"+ id.toString() + "/predict";

        System.out.println(requestBody.toString());
        response = postRequest(path, requestBody);
        System.out.println(response);
        return Double.parseDouble(response);
    }

    private void saveSB3AgentHttpRequest() throws Exception {
        String response;
        String path = "agents/"+ id.toString() + "/save";

        response = postRequest(path, new JSONObject());
        System.out.println(response);
    }

    private void loadSB3AgentHttpRequest() throws Exception {
        String response;
        JSONObject requestBody = new JSONObject();
        requestBody.put("agentType", this.agentType);
        String path = "agents/"+ id.toString() + "/load";

        System.out.println(requestBody.toString());
        response = postRequest(path, requestBody);
        System.out.println(response);
    }

    private String postRequest(String path, Object requestBody) {
        String host = ServerConfig.HOST;
        URI uri;
        try {
             uri = new URI(host + "/" + path);
        } catch (URISyntaxException uriSyntaxException) {
            throw new RuntimeException(uriSyntaxException.getMessage());
        }
        if(!(requestBody instanceof JSONObject || requestBody instanceof JSONArray)) {
            throw new RuntimeException("requestBody must be of type JSONObject or JSONArray");
        }
        HttpRequest request  = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json; charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response;

        try {
            response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new RuntimeException(response.body());
            System.out.println(response);
        } catch (IOException | InterruptedException connectException) {
            System.out.println("Could not reach server under: " + ServerConfig.HOST);
            throw new RuntimeException("Could not reach server under: " + ServerConfig.HOST);
        }

        return response.body();
    }

    public StateObservation getStartSate() {
        StateObservation stateObservation = gameBoard.getDefaultStartState(null);
        return stateObservation;
    }

    private int[] getObservationVector(StateObservation stateObservation) {
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

    public StateObservation afterGame() {

        StateObservation stateObservation;
        if (gameProgressor != null) {
            stateObservation = gameProgressor.afterGame(this);
            incrementGameNum();
        } else {
            stateObservation = getStartSate();
        }

        moveCounter = 0;
        return stateObservation;
    }

    public void incrementMoves() {
        m_numTrnMoves++;
        moveCounter++;
    }

    @Override
    public int getMoveCounter() {
        return moveCounter;
    }
}
