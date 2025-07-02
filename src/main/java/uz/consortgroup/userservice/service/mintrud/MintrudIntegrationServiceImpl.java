package uz.consortgroup.userservice.service.mintrud;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.core.api.v1.dto.mintrud.JsonRpcRequest;
import uz.consortgroup.core.api.v1.dto.mintrud.LabourJobPositionResponse;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;

import org.springframework.http.HttpHeaders;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MintrudIntegrationServiceImpl implements MintrudIntegrationService {

    private final WebClient webClient;

    @Value("${mintrud.api.token}")
    private String apiToken;

    @Value("${mintrud.api.url}")
    private String apiUrl;

    @Override
    @AllAspect
    public JobPositionResult getJobInfo(String pinfl) {
        JsonRpcRequest request = JsonRpcRequest.builder()
                .id(UUID.randomUUID().toString())
                .method("external.labour_get_job_positions")
                .params(Map.of("pin", pinfl))
                .build();

        try {
            LabourJobPositionResponse response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("Api-Token", apiToken)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(LabourJobPositionResponse.class)
                    .block();

            return response != null ? response.getResult() : null;

        } catch (Exception e) {
            return null;
        }
    }
}
