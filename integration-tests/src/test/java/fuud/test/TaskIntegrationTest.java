package fuud.test;

import fuud.test.infra.components.ClientComponent;
import fuud.test.infra.components.WorkerComponent;
import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static fuud.test.infra.EnvStarter.env;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TaskIntegrationTest {
    @Test
    public void testTaskSubmission() throws Exception {

        Cloud cloud = CloudFactory.createCloud();
        ClientComponent clientComponent = new ClientComponent();
        env(
                cloud,
                clientComponent,
                new WorkerComponent()
        );

        URI taskUri = URI.create("http://localhost:" + clientComponent.config.restPort + "/task");
        HttpResponse<String> response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .method("POST", HttpRequest.BodyPublishers.ofString("{ \"data\":\"my-data\"}"))
                        .header("Content-Type", "application/json")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes()))
                        .uri(taskUri)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertFalse(response.body().isBlank());
    }
}
