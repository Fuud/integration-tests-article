package fuud.worker.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class WorkerServiceEndpoint {

    @PostMapping("/task")
    public WorkerResponseDto placeTask(@RequestBody ClientRequest request){
        WorkerResponseDto workerResponseDto = new WorkerResponseDto();
        workerResponseDto.setJobId(UUID.randomUUID().toString());
        return workerResponseDto;
    }

}
