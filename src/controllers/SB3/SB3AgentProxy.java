package controllers.SB3;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.SB3.HttpServer.RLEnvironmentServer;
import games.*;
import org.json.JSONArray;
import org.json.JSONObject;
import params.ParOther;
import params.ParSB3;
import tools.ScoreTuple;
import tools.Types;

import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * This class acts as a Proxy for the SB3 Agents on the Python side.
 * It's meant to be used like a normal the normal agents in GBG and therefore implements {@link AgentBase} and {@link PlayAgent}.
 * <p>
 * It facilitates the communication between this Proxy and the SB3Agent using HTTP requests.
 * This class does not implement the learning or decision-making logic itself.
 * </p>
 */
public class SB3AgentProxy extends AgentBase implements PlayAgent, Serializable {
    transient private StateObservationVectorFuncs stateObservationVectorFuncs;
    private ParSB3 parSB3;
    transient private ParOther parOther; // TODO: remove?
    private String agentType;
    private String gameName;
    private UUID id;
    transient private XArenaFuncs.GameProgressor gameProgressor;
    transient private int moveCounter  = 0;
    transient private HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public SB3AgentProxy(String name, ParSB3 parSB3, ParOther oPar, StateObservationVectorFuncs stateObservationVectorFuncs, String gameName) {
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
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return true;
    }

    /**
     * Fills the parameter tab and loads the latest saved policy of this Agent.
     * @param n			use the param tabs of the {@code n}'th player
     * @param m_arena	member {@code m_xab} has the param tabs
     *
     */
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

    /**
     * Methode used by {@link params.SB3Params} for loading a specific saved policy on the python side from the parameter tab.
     * @param path
     */
    public void loadSB3PolicyFromPath(String path) {
        try {
            loadSB3AgentHttpRequest(id, agentType, gameName, path);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Don't use this methode. The SB3AgentProxy only support training using {@link #learn(XArenaFuncs.GameProgressor gameProgressor)}.
     * @param stateObservation
     * @return
     */
    @Override
    public boolean trainAgent(StateObservation stateObservation) {
      throw new RuntimeException("SB3AgentProxy only supports learn method for training.");
    }

    /**
     * Starts training for the number of steps provided through the parameter tab.
     * After this call the training loop gets handheld by SB3, so {@link games.XArenaFuncs.GameProgressor} is needed
     * to update the training progress on the GBG side.
     * <p>
     * Causes the current thread to wait and let SB3 do his thing while the {@link RLEnvironmentServer} thread stays online and serves the <STRONG>GBGEnvironmentClient</STRONG> on the python side,
     * by the methods provided by the {@link RLEnvironmentService}, who emulates a gymnasium environment and advances the game state.
     * </p>
     * @param gameProgressor
     */
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
        }
    }

    /**
     * Sends the HTTP request to start training the SB3 Agent.
     * @param totalTimeSteps
     * @param selfPlayParams
     * @param evaluationOptions
     */
    private void learnHttpRequest(int totalTimeSteps, ParSB3.SelfPlayParameters selfPlayParams, ParSB3.EvaluationOptions evaluationOptions) {
        JSONObject requestBody = new JSONObject();
        JSONObject selfPlayParameters = new JSONObject(selfPlayParams);
        JSONObject evaluationOptionsJson = new JSONObject(evaluationOptions);

        requestBody.put("totalTimeSteps", totalTimeSteps);
        requestBody.put("selfPlayParameters", selfPlayParameters);
        requestBody.put("evaluationOptions", evaluationOptionsJson);

        String path = "agents/" + id.toString() + "/learn";

        try {
            String response = postRequest(path, requestBody);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes an HTTP request to creat a new SB3 agent by providing all the parameters about the agent and the game environment needed for that.
     * @param parSB3
     */
    private void createSB3Agent(ParSB3 parSB3) {
        Map<String, Object> environmentParameters = new HashMap<>();
        environmentParameters.put("actionSpaceSize", stateObservationVectorFuncs.getActionSpaceSize());
        environmentParameters.put("observationRangeStarts", stateObservationVectorFuncs.getStateObservationVectorStarts());
        environmentParameters.put("observationRangeSizes", stateObservationVectorFuncs.getObservationVectorRanges());

        createSB3AgentHttpRequest(parSB3.agentType, gameName, parSB3.parSB3Base, parSB3.parSB3Agent, parSB3.parSB3Network, environmentParameters);
    }

    private void createSB3AgentHttpRequest(String agentType, String gameName, Map<String, Object> baseParameters, Map<String, Object> agentParameters, Map<String, Object> networkParameters, Map<String, Object> environmentParameters) {
        JSONObject requestBody = new JSONObject();

        requestBody.put("agent_id", id);
        requestBody.put("agentType", agentType);
        requestBody.put("gameName", gameName);
        requestBody.put("baseParameters", baseParameters);
        requestBody.put("agentParameters", agentParameters);
        requestBody.put("networkParameters", networkParameters);
        requestBody.put("environmentParameters", environmentParameters);

        postRequest("agents", requestBody);
    }

    /**
     * Sends HTTP request to predict the best next action.
     * @param observation
     * @param availableActions
     * @return {@link ActionWithValues}, the best available action and values of available actions, with not available actions cut out; Order stays the same.
     * @throws Exception
     */
    private ActionWithValues predictHttpRequest(int[] observation, int[] availableActions) throws Exception {
        JSONObject response;
        JSONArray observationJson = new JSONArray(observation);
        JSONArray availableActionsJson = new JSONArray(availableActions);
        JSONObject requestBody = new JSONObject();
        requestBody.put("observation", observationJson);
        requestBody.put("availableActions", availableActionsJson);
        String path = "agents/"+ id.toString() + "/predict";

        response = new JSONObject(postRequest(path, requestBody));

        JSONArray actionValuesJson = response.getJSONArray("actionValues");
        double[] actionValues = new double[actionValuesJson.length()];
        for(int i = 0; i < actionValuesJson.length(); i++) {
            actionValues[i] = actionValuesJson.getDouble(i);
        }


        int action = (int) response.get("action");

        return new ActionWithValues(action, actionValues, availableActions);
    }

    private static class ActionWithValues {
        /**
         * Chosen Action by SB3 Agent.
         */
        public int action;
        /**
         * Values of available actions.
         */
        public double[] values;

        private double chosenActionValue;

        public ActionWithValues(int action, double[] values, int[] availableActions) {
            this.action = action;
            this.values = values;

            for (int i = 0; i < availableActions.length; i++) {
                if (availableActions[i] == action) {
                    chosenActionValue = values[i];
                    return;
                }
            }
        }

        public double getChosenValue() {
            return chosenActionValue;
        }
    }

    /**
     * Returns an action from a random self play policy (can include older policies).
     * If deterministic is true returns the action with the highest value else returns an action chosen with the value as a probability.
     * @param observation
     * @param availableActions
     * @param deterministic
     * @return
     * @throws Exception
     */
    private int selfPlayHttpRequest(int[] observation, int[] availableActions, boolean deterministic) throws Exception {
        JSONObject response;
        JSONArray observationJson = new JSONArray(observation);
        JSONArray availableActionsJson = new JSONArray(availableActions);
        JSONObject requestBody = new JSONObject();
        requestBody.put("observation", observationJson);
        requestBody.put("availableActions", availableActionsJson);
        requestBody.put("deterministic", deterministic);
        String path = "agents/"+ id.toString() + "/selfPlay";

        response = new JSONObject(postRequest(path, requestBody));
        return (int) response.get("action");
    }

    /**
     * Saves the current SB3 Agents policy.
     * @param id
     * @param gameName
     * @param agentType
     * @throws Exception
     */
    private void saveSB3AgentHttpRequest(UUID id, String gameName, String agentType) throws Exception {
        String response;
        JSONObject requestBody = new JSONObject();
        String path = "agents/"+ id.toString() + "/save";

        response = postRequest(path, requestBody);
        System.out.println(response);
    }

    /**
     * Loads a specific saved policy for this agent. If loadPath is Null returns the latest policy.
     * @param id
     * @param agentType
     * @param gameName
     * @param loadPath
     * @throws Exception
     */
    private void loadSB3AgentHttpRequest(UUID id, String agentType, String gameName, String loadPath) throws Exception {
        String response;
        JSONObject requestBody = new JSONObject();
        requestBody.put("agentType", agentType);
        requestBody.put("gameName", gameName);
        if (loadPath != null) {
            requestBody.put("path", loadPath);
        }
        String path = "agents/"+ id.toString() + "/load";

        response = postRequest(path, requestBody);
        System.out.println(response);
    }

    /**
     * Sends a generic POST HTTP request.
     * @param path
     * @param requestBody
     * @return
     */
    private String postRequest(String path, Object requestBody) {
        String host = SB3Config.SB3Server.HOST;
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
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new RuntimeException(response.body());
        } catch (IOException | InterruptedException connectException) {
            if (connectException instanceof ConnectException && Objects.equals(((ConnectException) connectException).getMessage(), "Address already in use: no further information")) {
                System.out.println("Could not reach server under: " + SB3Config.SB3Server.HOST);
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
            throw new RuntimeException("Could not reach server under: " + SB3Config.SB3Server.HOST);
        }

        return response.body();
    }

    private int[] getObservationVector(StateObservation stateObservation) {
        return stateObservationVectorFuncs.getStateObservationVector(stateObservation);
    }

    public int[] getAvailableActions(StateObservation stateObservation) {
        return stateObservationVectorFuncs.getAvailableActions(stateObservation);
    }

    /**
     * Asks the SB3 agent to predict the best possible action for the given StateObservation.
     * @param sob            current game state (is returned unchanged)
     * @param random        allow random action selection with probability m_epsilon
     * @param deterministic whether to act deterministic (if several actions have best value, return the 1st one)
     * @param silent        whether to be silent
     * @return
     */
    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent) {
        int[] observation= getObservationVector(sob);
        int[] availableActions = getAvailableActions(sob);

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

        ScoreTuple scoreTuple = new ScoreTuple(sob, actionsWithValues.getChosenValue());
        return new Types.ACTIONS_VT(actionsWithValues.action, false, actionsWithValues.values, actionsWithValues.getChosenValue(), scoreTuple);
    }

    /**
     * Gets called when SB3AgentProxy gets serialized, so while saving this agent. Sends a save agent HTTP requests to the python side, to save the latest policy.
     * @param oos
     * @throws IOException
     */
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

    /**
     * Gets called by {@link RLEnvironmentService} after a Game/ Episode has finished, to use the {@link games.XArenaFuncs.GameProgressor} to update the game stats as well as retrieving a new stateObservation.
     * @return
     */
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

    /**
     * Gets called by {@link RLEnvironmentService} after each step to increment the move counter.
     */
    public void incrementMoves() {
        m_numTrnMoves++;
        moveCounter++;
    }


    /**
     * Should only be used during training for SelfPlay. Returns an action from a random self play policy (can include older policies).
     * Action gets chosen by chance using their value, greater variance in opponent strategies.
     * @param stateObservation
     * @return
     */
    public Types.ACTIONS_VT selfPlay(StateObservation stateObservation) {
        int[] observation= getObservationVector(stateObservation);
        int[] availableActions = getAvailableActions(stateObservation);

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
