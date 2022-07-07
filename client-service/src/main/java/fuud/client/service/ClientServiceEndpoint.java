package fuud.client.service;

import fuud.client.service.worker.WorkerResponseDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class ClientServiceEndpoint {
    private final ClientServiceConfig config;
    private final RestTemplate restTemplate;

    public ClientServiceEndpoint(ClientServiceConfig config, RestTemplateBuilder restTemplateBuilder) {
        this.config = config;
        this.restTemplate = restTemplateBuilder.build();
    }

    @PostMapping("/task")
    public String placeTask(@RequestBody ClientRequest request){
        return restTemplate.postForObject(config.getWorkerUrl(), request, WorkerResponseDto.class).getJobId();
    }

}
