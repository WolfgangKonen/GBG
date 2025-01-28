package controllers.SB3.HttpServer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controllers.SB3.FirstObservation;
import controllers.SB3.RLEnvironmentConnector;
import controllers.SB3.Transition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


// Singelton
public class SimpleHttpServer
{
    private static final SimpleHttpServer SIMPLE_HTTP_SERVER;

    static {
        try {
            SIMPLE_HTTP_SERVER = new SimpleHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpServer server;
    private RLEnvironmentConnector rlEnvironment;

    private StepHttpHandler stepHttpHandler;
    private  ResetHttpHandler resetHttpHandler;
    private TrainingFinishedHandler trainingFinishedHandler;

    private SimpleHttpServer() throws IOException {
        // Create an HttpServer instance
        this.server = HttpServer.create(new InetSocketAddress(8094), 0);
        this.stepHttpHandler = new StepHttpHandler();
        this.resetHttpHandler = new ResetHttpHandler();
        this.trainingFinishedHandler = new TrainingFinishedHandler();

        // Create context
        server.createContext("/step", stepHttpHandler);
        server.createContext("/reset", resetHttpHandler);
        server.createContext("/trainingFinished", trainingFinishedHandler);

        //server.createContext("/testComplete", new testCompleteHttpHandler(httpTest));

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server is running on port 8094");
    }

    public static SimpleHttpServer getInstance() {
        return SIMPLE_HTTP_SERVER;
    }

    public void setRlEnvironment(RLEnvironmentConnector rlEnvironment) {
        this.rlEnvironment = rlEnvironment;
        stepHttpHandler.setRlEnvironment(rlEnvironment);
        resetHttpHandler.setRlEnvironment(rlEnvironment);
        trainingFinishedHandler.setRlEnvironment(rlEnvironment);
    }
    public void stopServer() {
        server.stop(2);
    }

    // step http handler POST request
    static class StepHttpHandler extends EnvironmentHttpHandler implements HttpHandler {

        private RLEnvironmentConnector rlEnvironment;

        public StepHttpHandler() {
            super();
        }

        public void setRlEnvironment(RLEnvironmentConnector rlEnvironment) {
            this.rlEnvironment = rlEnvironment;
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
        public void handle(HttpExchange exchange) throws IOException
        {
            final Headers headers = exchange.getResponseHeaders();
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
                            Transition transition = this.rlEnvironment.step(action);

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

                    headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                    final byte[] rawResponseBody = response.getBytes(CHARSET);
                    exchange.sendResponseHeaders(responseCode, rawResponseBody.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(rawResponseBody);
                    outputStream.close();
                case METHOD_OPTIONS:
                    headers.set(HEADER_ALLOW, METHOD_POST);
                    exchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                default:
                    headers.set(HEADER_ALLOW, METHOD_POST);
                    exchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                    break;
            }
        }
    }

    // reset http handler
    static class ResetHttpHandler extends EnvironmentHttpHandler implements HttpHandler {

        private RLEnvironmentConnector rlEnvironment;

        public ResetHttpHandler() {
            super();
        }

        public void setRlEnvironment(RLEnvironmentConnector rlEnvironment) {
            this.rlEnvironment = rlEnvironment;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            final Headers headers = exchange.getResponseHeaders();
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_POST:
                    // reset environment
                    FirstObservation firstObservation = this.rlEnvironment.reset();

                    // creat responds
                    String response = firstObservation.toJson().toString();

                    headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                    final byte[] rawResponseBody = response.getBytes(CHARSET);
                    exchange.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(rawResponseBody);
                    outputStream.close();
                case METHOD_OPTIONS:
                    headers.set(HEADER_ALLOW, METHOD_POST);
                    exchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                default:
                    headers.set(HEADER_ALLOW, METHOD_POST);
                    exchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                    break;
            }
        }
    }

    static class TrainingFinishedHandler extends EnvironmentHttpHandler implements HttpHandler {
        private RLEnvironmentConnector rlEnvironment;

        public TrainingFinishedHandler() {
            super();
        }

        public void setRlEnvironment(RLEnvironmentConnector rlEnvironment) {
            this.rlEnvironment = rlEnvironment;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            final Headers headers = exchange.getResponseHeaders();
            final String requestMethod = exchange.getRequestMethod().toUpperCase();
            switch (requestMethod) {
                case METHOD_POST:
                    try {
                        // get request body
                        String requestBody = inputStreamToString(exchange.getRequestBody());
                        exchange.sendResponseHeaders(NO_CONTENT, NO_RESPONSE_LENGTH);
                        this.rlEnvironment.trainingFinished();
                    }
                    catch (IOException exception) {
                        final byte[] rawResponseBody = invalidJson().getBytes(CHARSET);

                        headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                        exchange.sendResponseHeaders(BAD_REQUEST, rawResponseBody.length);

                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(rawResponseBody);
                        outputStream.close();
                    }
                case METHOD_OPTIONS:
                    headers.set(HEADER_ALLOW, METHOD_POST);
                    exchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                default:
                    headers.set(HEADER_ALLOW, METHOD_POST);
                    exchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                    break;
            }
        }
    }

}
