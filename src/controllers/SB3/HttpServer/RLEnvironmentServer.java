package controllers.SB3.HttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controllers.SB3.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * Singleton. Host the HTTP Server with all the endpoints needed for the <STRONG>GBGEnvironmentClient</STRONG> to acces the games/ Environemts in GBG,
 * using the {@link RLEnvironmentService} to provide access to the business logic, handling the interactions with the game.
 */
public class RLEnvironmentServer
{
    private static final RLEnvironmentServer RL_ENVIRONMENT_SERVER;

    static {
        try {
            RL_ENVIRONMENT_SERVER = new RLEnvironmentServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpServer server;
    private RLEnvironmentService rlEnvironmentService;

    private StepHttpHandler stepHttpHandler;
    private  ResetHttpHandler resetHttpHandler;
    private TrainingFinishedHandler trainingFinishedHandler;
    private AvailableActionsHandler availableActionsHandler;
    private EvalHandler evalHandler;

    private RLEnvironmentServer() throws IOException {
        // Create an HttpServer instance
        this.server = HttpServer.create(new InetSocketAddress(SB3Config.PORT), 0);
        this.stepHttpHandler = new StepHttpHandler();
        this.resetHttpHandler = new ResetHttpHandler();
        this.trainingFinishedHandler = new TrainingFinishedHandler();
        this.availableActionsHandler = new AvailableActionsHandler();
        this.evalHandler = new EvalHandler();

        // Create context
        server.createContext("/step", stepHttpHandler);
        server.createContext("/reset", resetHttpHandler);
        server.createContext("/trainingFinished", trainingFinishedHandler);
        server.createContext("/availableActions", availableActionsHandler);
        server.createContext("/eval", evalHandler);

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server is running on port " + SB3Config.PORT);
    }

    public static RLEnvironmentServer getInstance() {
        return RL_ENVIRONMENT_SERVER;
    }

    // set current RLEnvironment before use
    public void setRlEnvironmentService(RLEnvironmentService rlEnvironmentService) {
        this.rlEnvironmentService = rlEnvironmentService;
    }
    public void stopServer() {
        server.stop(2);
    }

    // step http handler POST request
    class StepHttpHandler extends EnvironmentHttpHandler implements HttpHandler {

        public StepHttpHandler() {
            super();
        }

        private String invalidFieldsResponse() {
            JSONObject error = new JSONObject();
            error.put("error", "Invalid fields");
            JSONObject fields = new JSONObject();
            fields.put("action", "Integer required. Describes action to take.");
            error.put("details", fields);

            return error.toString();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_POST:
                    String response;
                    int responseCode = STATUS_OK;

                    try {
                        // get request body
                        JSONObject requestBody = inputStreamToJson(exchange.getRequestBody());

                        try {
                            int action = requestBody.getInt("action");

                            // step in environment
                            Transition transition = rlEnvironmentService.step(action);

                            // create responds
                            response = transition.toJson().toString();
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                            response = invalidFieldsResponse();
                            responseCode = BAD_REQUEST;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        responseCode = BAD_REQUEST;
                        response = invalidJson();
                    }


                    sendResponse(exchange, responseCode, response);
                    break;
                default:
                    sendMethodOptions(exchange);
                    break;
            }
        }
    }

    class AvailableActionsHandler extends EnvironmentHttpHandler implements HttpHandler {
        public AvailableActionsHandler() {
            super();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_GET:
                    try {
                        int[] availableActions = rlEnvironmentService.getAvailableActions();

                        // creat responds
                        String response = new JSONArray(availableActions).toString();

                        sendResponse(exchange, STATUS_OK, response);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                default:
                    sendMethodOptions(exchange);
                    break;
            }
        }
    }

    // reset http handler
    class ResetHttpHandler extends EnvironmentHttpHandler implements HttpHandler {
        public ResetHttpHandler() {
            super();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_POST:
                    try {
                        // reset environment
                        FirstObservation firstObservation = rlEnvironmentService.reset();

                        // creat responds
                        String response = firstObservation.toJson().toString();

                        sendResponse(exchange, STATUS_OK, response);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                default:
                    sendMethodOptions(exchange);
                    break;
            }
        }
    }

    class TrainingFinishedHandler extends EnvironmentHttpHandler implements HttpHandler {
        public TrainingFinishedHandler() {
            super();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_POST:
                    try {
                        // get request body
                        String requestBody = inputStreamToString(exchange.getRequestBody());
                        sendNoContent(exchange);
                        rlEnvironmentService.trainingFinished();
                    }
                    catch (IOException exception) {
                        sendResponse(exchange, BAD_REQUEST, invalidJson());
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                default:
                    sendMethodOptions(exchange);
                    break;
            }
        }
    }

    class EvalHandler extends EnvironmentHttpHandler implements HttpHandler {
        public EvalHandler() {
            super();
        }

        private String invalidFieldsResponse() {
            JSONObject error = new JSONObject();
            error.put("error", "Invalid fields");
            JSONObject fields = new JSONObject();
            fields.put("opponentName", "String. Name of opponent agent.");
            fields.put("numberOfGames", "int. Number of games to play in evaluation.");
            error.put("details", fields);

            return error.toString();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_POST:
                    String response = invalidJson();

                    int responseCode = STATUS_OK;

                    String opponentName = "Random";
                    int numberOfGames = 300;

                    try {
                        try {
                            try {
                                // get request body
                                JSONObject requestBody = inputStreamToJson(exchange.getRequestBody());
                                opponentName = requestBody.getString("opponentName");
                                //numberOfGames for each player on each playerPostion (e.g. X or O in TicTacToe). So Total number of games = numberOfGames * players.
                                numberOfGames = requestBody.getInt("numberOfGames");


                            } catch (JSONException exception) {
                                exception.printStackTrace();
                                response = invalidFieldsResponse();
                                responseCode = BAD_REQUEST;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            responseCode = BAD_REQUEST;
                            response = invalidJson();
                        }
                        EvalResults evalResults = rlEnvironmentService.evalWithDefaultOpponent(numberOfGames);
                        if (responseCode != BAD_REQUEST) {
                            response = evalResults.toJson().toString();
                        }

                        sendResponse(exchange, responseCode, response);
                    }
                    catch (IOException exception) {
                        String invalidJson = invalidJson();
                        sendResponse(exchange, BAD_REQUEST, invalidJson);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                default:
                    sendMethodOptions(exchange);
                    break;
            }
        }
    }
}