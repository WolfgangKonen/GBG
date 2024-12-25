package controllers.SB3;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import environment.domain.FirstObservation;
import environment.domain.RLEnvironment;
import environment.domain.Transition;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


// Driver Class
public class SimpleHttpServer
{
    private HttpServer server;
    private HttpTest httpTest;
    private RLEnvironment rlEnvironment;

    public SimpleHttpServer(HttpTest httpTest, RLEnvironment rlEnvironment) throws IOException {
        this.httpTest = httpTest;
        this.rlEnvironment = rlEnvironment;
        // Create an HttpServer instance
        this.server = HttpServer.create(new InetSocketAddress(8094), 0);

        // Create step context
        server.createContext("/step", new stepHttpHandler(rlEnvironment));
        // Creat reset context
        server.createContext("/reset", new resetHttpHandler(rlEnvironment));

        server.createContext("/testComplete", new testCompleteHttpHandler(httpTest));

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server is running on port 8094");
    }

    private static void startTest() throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URI("http://127.0.0.1:8095/start");
        HttpRequest request  = HttpRequest.newBuilder().uri(uri).version(HttpClient.Version.HTTP_1_1).POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

    }

    public void stopServer() {
        server.stop(2);
    }

    // step http handler POST request
    static class stepHttpHandler extends EnvironmentHttpHandler implements HttpHandler {

        RLEnvironment rlEnvironment;

        public stepHttpHandler(RLEnvironment rlEnvironment) {
            super();

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

                            // creat responds
                            response = transition.toJson().toString();
                        }
                        catch (JSONException exception) {
                            response = invalidFieldsResponse();
                            responseCode = BAD_REQUEST;
                        }
                    }
                    catch (Exception e) {
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
    static class resetHttpHandler extends EnvironmentHttpHandler implements HttpHandler {

        RLEnvironment rlEnvironment;

        public resetHttpHandler(RLEnvironment rlEnvironment) {
            super();
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

    static class testCompleteHttpHandler extends EnvironmentHttpHandler implements HttpHandler {
            HttpTest httpTest;
        public testCompleteHttpHandler(HttpTest httpTest) {
            super();
            this.httpTest = httpTest;
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
                        this.httpTest.testFinished(requestBody);
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
