package controllers.SB3.HttpServer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import controllers.SB3.RLEnvironmentService;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Abstract class provides common functionality shared by the HTTP handlers.
 */
public abstract class EnvironmentHttpHandler {
    protected static final String HEADER_ALLOW = "Allow";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";

    protected static final Charset CHARSET = StandardCharsets.UTF_8;

    protected static final int NO_CONTENT = 204;
    protected static final int STATUS_OK = 200;
    protected static final int BAD_REQUEST = 400;
    protected static final int STATUS_METHOD_NOT_ALLOWED = 405;

    protected static final int NO_RESPONSE_LENGTH = -1;

    protected static final String METHOD_GET = "GET"; // TODO: create enum
    protected static final String METHOD_POST = "POST";
    protected static final String METHOD_OPTIONS = "OPTIONS";


    //protected RLEnvironmentService rlEnvironmentService;


    public EnvironmentHttpHandler() {
    }

    protected static String invalidJson() {
        JSONObject error = new JSONObject();
        error.put("error:", "Invalid Json.");
        return error.toString();
    }

    protected static String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);

        return responseStrBuilder.toString();
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));

        final byte[] responseBytes = response.getBytes(CHARSET);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    protected void sendMethodOptions(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set(HEADER_ALLOW, METHOD_POST);
        exchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
    }

    protected void sendNoContent(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        exchange.sendResponseHeaders(NO_CONTENT, NO_RESPONSE_LENGTH);
    }

    public void setRlEnvironmentService(RLEnvironmentService rlEnvironmentService) {
        //this.rlEnvironmentService = rlEnvironmentService;
    }

    protected static JSONObject inputStreamToJson(InputStream inputStream) throws UnsupportedEncodingException, IOException {
        return new JSONObject(inputStreamToString(inputStream));
    }
}
