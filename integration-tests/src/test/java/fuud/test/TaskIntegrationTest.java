package fuud.test;

import fuud.client.service.ClientServiceApplication;
import fuud.test.infra.ClassPathHelper;
import fuud.test.infra.Node;
import fuud.worker.service.WorkerServiceApplication;
import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.nanocloud.VX;
import org.gridkit.vicluster.ViProps;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TaskIntegrationTest {
    @Test
    public void testTaskSubmission() throws Exception {

        Cloud cloud = CloudFactory.createCloud();
        Node clientNode = new Node(cloud, "client");
        clientNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(clientNode).setLocalType();
        ClassPathHelper.getClasspathForArtifact("client-service")
                .forEach(classPathElement -> clientNode.x(VX.CLASSPATH).add(classPathElement));
        clientNode.exec(() -> ClientServiceApplication.main(new String[0]));

        Node workerNode = new Node(cloud, "worker");
        workerNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(workerNode).setLocalType();
        ClassPathHelper.getClasspathForArtifact("worker-service")
                .forEach(classPathElement -> workerNode.x(VX.CLASSPATH).add(classPathElement));
        workerNode.exec(() -> WorkerServiceApplication.main(new String[0]));


        HttpResponse<String> response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .method("POST", HttpRequest.BodyPublishers.ofString("{ \"data\":\"my-data\"}"))
                        .header("Content-Type", "application/json")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes()))
                        .uri(URI.create("http://localhost:8080/task"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertFalse(response.body().isBlank());
    }
}
