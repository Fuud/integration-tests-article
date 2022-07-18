package fuud.test;

import fuud.test.infra.components.ClientComponent;
import fuud.test.infra.components.WorkerComponent;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static fuud.test.infra.EnvStarter.env;
import static fuud.test.infra.EnvStarter.integrationTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TaskIntegrationTest {
    @Test
    public void testTaskSubmission() {
        integrationTest((cloud) -> {
            ClientComponent clientComponent = new ClientComponent();
            env(cloud, clientComponent, new WorkerComponent());

            URI taskUri = URI.create("http://localhost:" + clientComponent.getConfig().restPort + "/task");
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
        });
    }

    @Test
    public void testTaskSubmission_0() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_1() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_2() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_3() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_4() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_5() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_6() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_7() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_8() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_9() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_10() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_11() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_12() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_13() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_14() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_15() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_16() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_17() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_18() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_19() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_20() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_21() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_22() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_23() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_24() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_25() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_26() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_27() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_28() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_29() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_30() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_31() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_32() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_33() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_34() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_35() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_36() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_37() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_38() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_39() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_40() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_41() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_42() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_43() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_44() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_45() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_46() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_47() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_48() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_49() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_50() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_51() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_52() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_53() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_54() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_55() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_56() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_57() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_58() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_59() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_60() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_61() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_62() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_63() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_64() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_65() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_66() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_67() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_68() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_69() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_70() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_71() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_72() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_73() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_74() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_75() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_76() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_77() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_78() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_79() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_80() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_81() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_82() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_83() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_84() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_85() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_86() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_87() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_88() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_89() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_90() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_91() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_92() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_93() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_94() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_95() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_96() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_97() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_98() {
        testTaskSubmission();
    }

    @Test
    public void testTaskSubmission_99() {
        testTaskSubmission();
    }
}
