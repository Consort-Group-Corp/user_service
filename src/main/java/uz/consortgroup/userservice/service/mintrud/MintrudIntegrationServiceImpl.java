package uz.consortgroup.userservice.service.mintrud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.core.api.v1.dto.mintrud.JsonRpcRequest;
import uz.consortgroup.core.api.v1.dto.mintrud.LabourJobPositionResponse;

import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MintrudIntegrationServiceImpl implements MintrudIntegrationService {

    private final WebClient webClient;

    @Value("${mintrud.api.token}")
    private String apiToken;

    @Value("${mintrud.api.url}")
    private String apiUrl;

    @Override
    public JobPositionResult getJobInfo(String pinfl) {
        String requestId = UUID.randomUUID().toString();

        JsonRpcRequest request = JsonRpcRequest.builder()
                .id(requestId)
                .method("external.labour_get_job_positions")
                .params(Map.of("pin", pinfl))
                .build();

        log.info("Sending request to Mintrud API — requestId={}, pinfl={}", requestId, pinfl);

        try {
            LabourJobPositionResponse response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("Api-Token", apiToken)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(LabourJobPositionResponse.class)
                    .block();

            if (response != null && response.getResult() != null) {
                log.info("Successfully received job info from Mintrud API — requestId={}, pinfl={}", requestId, pinfl);
                return response.getResult();
            } else {
                log.warn("Mintrud API returned null or empty result — requestId={}, pinfl={}", requestId, pinfl);
                return null;
            }

        } catch (Exception e) {
            log.error("Error occurred while calling Mintrud API — requestId={}, pinfl={}, error={}", requestId, pinfl, e.getMessage(), e);
            return null;
        }
    }
}
