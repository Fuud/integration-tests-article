package fuud.test;

import fuud.client.service.ClientServiceApplication;
import fuud.worker.service.WorkerServiceApplication;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TaskIntegrationTest {
    @Test
    public void testTaskSubmission() throws Exception {
        ClientServiceApplication.main(new String[0]);
        WorkerServiceApplication.main(new String[0]);

        HttpResponse<String> response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .method("POST", HttpRequest.BodyPublishers.ofString("{ \"data\":\"my-data\"}"))
                        .header("Content-Type", "application/json")
                        .uri(URI.create("http://localhost:8080/task"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(response.statusCode(), 200);
        assertFalse(response.body().isBlank());
    }
}
