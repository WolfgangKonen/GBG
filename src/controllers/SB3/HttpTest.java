package controllers.SB3;

import environment.domain.DummyRLEnvironment;
import environment.domain.RLEnvironment;
import org.json.JSONObject;
import utils.Writer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTest {
    private SimpleHttpServer simpleHttpServer;

    private long startTime = 0;
    private long finishTime = 0;
    private int observationVectorSize;

    public HttpTest(int observationVectorSize) throws IOException {
        RLEnvironment rlEnvironment = new DummyRLEnvironment(observationVectorSize);
        this.simpleHttpServer = new SimpleHttpServer(this, rlEnvironment);
        this.observationVectorSize = observationVectorSize;
    }

    public void startTest() throws IOException, InterruptedException, URISyntaxException {
        this.startTime = System.currentTimeMillis();

        URI uri = new URI("http://127.0.0.1:8095/start");
        JSONObject requestBody = new JSONObject();
        requestBody.put("observationVectorSize", observationVectorSize);

        HttpRequest request  = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json; charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }

    public void testFinished(String massage) {
        this.finishTime = System.currentTimeMillis();
        long elapsedTime = this.finishTime - this.startTime;

        System.out.println(elapsedTime + " ms" + "; " + elapsedTime/1000.0 + " s");
        Writer.store(elapsedTime, "HttpTestResults" + observationVectorSize + ".txt");
        System.out.println(massage);
        simpleHttpServer.stopServer();

    }
}
