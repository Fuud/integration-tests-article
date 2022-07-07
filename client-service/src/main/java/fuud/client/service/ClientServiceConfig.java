package fuud.client.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("client-service")
public class ClientServiceConfig {
    private String workerUrl;

    public String getWorkerUrl() {
        return workerUrl;
    }

    public void setWorkerUrl(String workerUrl) {
        this.workerUrl = workerUrl;
    }
}
