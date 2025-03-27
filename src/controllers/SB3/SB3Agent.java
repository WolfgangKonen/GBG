package controllers.SB3;

import agentIO.AgentLoader;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.SB3.HttpServer.ServerConfig;
import controllers.SB3.HttpServer.SimpleHttpServer;
import games.*;
import org.json.JSONArray;
import org.json.JSONObject;
import params.ParOther;
import params.ParSB3;
import params.SB3Params;
import tools.ScoreTuple;
import tools.Types;

import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SB3Agent extends AgentBase implements PlayAgent, Serializable {
    transient private StateObservationVectorFuncs stateObservationVectorFuncs;
    private ParSB3 parSB3;
    transient private ParOther parOther; // TODO: remove?
    private String agentType;
    private String gameName;
    private UUID id;
    transient private XArenaFuncs.GameProgressor gameProgressor;
    transient private int moveCounter  = 0;

    public SB3Agent(String name, ParSB3 parSB3, ParOther oPar, StateObservationVectorFuncs stateObservationVectorFuncs, String gameName) {
        super(name);
        this.id = UUID.randomUUID();
        this.parSB3 = parSB3;
        this.parOther = oPar;
        this.stateObservationVectorFuncs = stateObservationVectorFuncs;
        this.agentType = parSB3.agentType;
        this.gameName = gameName;

        parSB3.setSB3Agent(this);
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
        stateObservationVectorFuncs = m_arena.makeStateObservationVectorFuncs();

        super.fillParamTabsAfterLoading(n, m_arena);
        m_arena.m_xab.setSb3ParFrom(n, parSB3, this);

        try {
            loadSB3AgentHttpRequest(id, agentType, gameName, null);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadSB3PolicyFromPath(String path) {
        try {
            loadSB3AgentHttpRequest(id, agentType, gameName, path);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public boolean trainAgent(StateObservation stateObservation) {
      throw new RuntimeException("SB3 Agent only supports learn method for training.");
    }

    // Trains Agent for total number of time steps
    public void learn(XArenaFuncs.GameProgressor gameProgressor) {
        this.gameProgressor = gameProgressor;

        createSB3Agent(parSB3);

        //start learning
        int totalTimeSteps = parSB3.trainTimeSteps;
        synchronized (this) {
            learnHttpRequest(totalTimeSteps, parSB3.parSB3SelfPlay, parSB3.evaluationOptions);
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Erwacht!");
        }
    }

    private void learnHttpRequest(int totalTimeSteps, ParSB3.SelfPlayParameters selfPlayParams, ParSB3.EvaluationOptions evaluationOptions) {
        JSONObject requestBody = new JSONObject();
        JSONObject selfPlayParameters = new JSONObject(selfPlayParams);
        JSONObject evaluationOptionsJson = new JSONObject(evaluationOptions);

        requestBody.put("totalTimeSteps", totalTimeSteps);
        requestBody.put("selfPlayParameters", selfPlayParameters);
        requestBody.put("evaluationOptions", evaluationOptionsJson);
        System.out.println(evaluationOptionsJson);

        String path = "agents/" + id.toString() + "/learn";

        try {
            String response = postRequest(path, requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSB3Agent(ParSB3 parSB3) {
        createSB3AgentHttpRequest(parSB3.agentType, gameName, parSB3.parSB3Base, parSB3.parSB3Agent, parSB3.parSB3Network);
    }

    private void createSB3AgentHttpRequest(String agentType, String gameName, Map<String, Object> baseParameters, Map<String, Object> agentParameters, Map<String, Object> networkParameters) {
        JSONObject requestBody = new JSONObject();
        JSONObject environmentParameters = new JSONObject();

        requestBody.put("agent_id", id);
        requestBody.put("agentType", agentType);
        requestBody.put("gameName", gameName);
        requestBody.put("baseParameters", baseParameters);
        requestBody.put("agentParameters", agentParameters);
        requestBody.put("networkParameters", networkParameters);

        environmentParameters.put("actionSpaceSize", stateObservationVectorFuncs.actionSpaceSize); // TODO not with get start state
        environmentParameters.put("observationRangeStarts", stateObservationVectorFuncs.getStateObservationVectorStarts());
        environmentParameters.put("observationRangeSizes", stateObservationVectorFuncs.getObservationVectorRanges());

        requestBody.put("environmentParameters", environmentParameters);

        System.out.println(requestBody.toString());


        postRequest("agents", requestBody);

    }

    private ActionWithValues predictHttpRequest(int[] observation, int[] availableActions) throws Exception {
        JSONObject response;
        JSONArray observationJson = new JSONArray(observation);
        JSONArray availableActionsJson = new JSONArray(availableActions);
        JSONObject requestBody = new JSONObject();
        requestBody.put("observation", observationJson);
        requestBody.put("availableActions", availableActionsJson);
        String path = "agents/"+ id.toString() + "/predict";

        System.out.println(requestBody.toString());
        response = new JSONObject(postRequest(path, requestBody));
        System.out.println(response);

        JSONArray actionValuesJson = response.getJSONArray("actionValues");
        double[] actionValues = new double[actionValuesJson.length()];
        for(int i = 0; i < actionValuesJson.length(); i++) {
            actionValues[i] = actionValuesJson.getDouble(i);
        }


        int action = (int) response.get("action");

        return new ActionWithValues(action, actionValues);
    }

    private static class ActionWithValues {
        public int action;
        public double[] values;

        public ActionWithValues(int action, double[] values) {
            this.action = action;
            this.values = values;
        }

        public double getBestValue() {
            return Arrays.stream(values).max().getAsDouble();
        }
    }

    private int selfPlayHttpRequest(int[] observation, int[] availableActions, boolean deterministic) throws Exception {
        JSONObject response;
        JSONArray observationJson = new JSONArray(observation);
        JSONArray availableActionsJson = new JSONArray(availableActions);
        JSONObject requestBody = new JSONObject();
        requestBody.put("observation", observationJson);
        requestBody.put("availableActions", availableActionsJson);
        requestBody.put("deterministic", deterministic);
        String path = "agents/"+ id.toString() + "/selfPlay";

        System.out.println(requestBody.toString());
        response = new JSONObject(postRequest(path, requestBody));
        System.out.println(response.get("action"));
        return (int) response.get("action");
    }

    private void saveSB3AgentHttpRequest(UUID id, String gameName, String agentType) throws Exception {
        String response;
        JSONObject requestBody = new JSONObject();
        String path = "agents/"+ id.toString() + "/save";

        response = postRequest(path, requestBody);
        System.out.println(response);
    }

    private void loadSB3AgentHttpRequest(UUID id, String agentType, String gameName, String loadPath) throws Exception {
        String response;
        JSONObject requestBody = new JSONObject();
        requestBody.put("agentType", agentType);
        requestBody.put("gameName", gameName);
        if (loadPath != null) {
            requestBody.put("path", loadPath);
        }
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
        } catch (IOException | InterruptedException connectException) {
            if (connectException instanceof ConnectException && Objects.equals(((ConnectException) connectException).getMessage(), "Address already in use: no further information")) {
                System.out.println("Could not reach server under: " + ServerConfig.HOST);
                System.out.println(connectException);
                System.out.println("Trying again..");
                try {
                    Thread.sleep(300);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return postRequest(path, requestBody);
            }
            connectException.printStackTrace();
            throw new RuntimeException("Could not reach server under: " + ServerConfig.HOST);
        }

        return response.body();
    }

    private int[] getObservationVector(StateObservation stateObservation) {
        return stateObservationVectorFuncs.getStateObservationVector(stateObservation);
    }

    public int[] getAvailableActions(StateObservation stateObservation) {
        return stateObservationVectorFuncs.getAvailableActions(stateObservation);
    }


    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent) {
        int[] observation= getObservationVector(sob);
        int[] availableActions = getAvailableActions(sob);

        System.out.println(Arrays.toString(observation));
        ActionWithValues actionsWithValues = null;
        try {
            actionsWithValues = predictHttpRequest(observation, availableActions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // action from predictHttpRequest() should always be available, just for safety.
        List<Types.ACTIONS> validActions = sob.getAvailableActions();
        if (!validActions.contains(new Types.ACTIONS(actionsWithValues.action))) {
            System.out.println("Already occupied.");
            System.out.println("Tried action " + (int) actionsWithValues.action);
            return new Types.ACTIONS_VT(validActions.get(0).toInt());
        }

        ScoreTuple scoreTuple = new ScoreTuple(sob, actionsWithValues.getBestValue());
        return new Types.ACTIONS_VT(actionsWithValues.action, false, actionsWithValues.values, actionsWithValues.getBestValue(), scoreTuple);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        try {
            saveSB3AgentHttpRequest(id, gameName, agentType);
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
            throw new RuntimeException("gameProgressor should not be null");
        }

        moveCounter = 0;
        return stateObservation;
    }

    public void incrementMoves() {
        m_numTrnMoves++;
        moveCounter++;
    }


    /**
     * Action gets chosen by the probability of the
     * @param stateObservation
     * @return
     */
    public Types.ACTIONS_VT selfPlay(StateObservation stateObservation) {
        int[] observation= getObservationVector(stateObservation);
        int[] availableActions = getAvailableActions(stateObservation);

        System.out.println(Arrays.toString(observation));
        double action = 0;
        try {
            action = selfPlayHttpRequest(observation, availableActions, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // actions from selfPlayHttpRequest() should always be available, just for safety.
        List<Types.ACTIONS> validActions = stateObservation.getAvailableActions();
        if (!validActions.contains(new Types.ACTIONS((int) action))) {
            System.out.println("Already occupied.");
            System.out.println("Tried action " + (int) action);
            return new Types.ACTIONS_VT(validActions.get(0).toInt());
        }

        return new Types.ACTIONS_VT((int) action);
    }

    @Override
    public int getMoveCounter() {
        return moveCounter;
    }

    public String getAgentType() {
        return agentType;
    }

    public UUID getId() {
        return id;
    }
}
