package uz.consortgroup.userservice.service.mintrud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.core.api.v1.dto.mintrud.LabourJobPositionResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MintrudIntegrationServiceImplTest {

    private WebClient.ResponseSpec responseSpecMock;

    private MintrudIntegrationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        responseSpecMock = mock(WebClient.ResponseSpec.class);

        service = new MintrudIntegrationServiceImpl(webClient);
        setField(service, "apiUrl", "http://dummy-url");
        setField(service, "apiToken", "dummy-token");

        when(webClient.post()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri("http://dummy-url")).thenReturn(bodySpecMock);
        when(bodySpecMock.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).thenReturn(bodySpecMock);
        when(bodySpecMock.header("Api-Token", "dummy-token")).thenReturn(bodySpecMock);
        when(bodySpecMock.body(any(BodyInserter.class))).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
    }

    @Test
    void getJobInfo_shouldReturnResult_whenResponseIsValid() {
        JobPositionResult result = JobPositionResult.builder()
                .name("John")
                .surname("Doe")
                .patronym("Smith")
                .build();

        LabourJobPositionResponse mockResponse = new LabourJobPositionResponse();
        mockResponse.setResult(result);

        when(responseSpecMock.bodyToMono(LabourJobPositionResponse.class)).thenReturn(Mono.just(mockResponse));

        JobPositionResult actual = service.getJobInfo("12345678901234");

        assertNotNull(actual);
        assertEquals("John", actual.getName());
        assertEquals("Doe", actual.getSurname());
        assertEquals("Smith", actual.getPatronym());
    }

    @Test
    void getJobInfo_shouldReturnNull_whenResponseIsNull() {
        when(responseSpecMock.bodyToMono(LabourJobPositionResponse.class)).thenReturn(Mono.empty());

        JobPositionResult actual = service.getJobInfo("12345678901234");

        assertNull(actual);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
