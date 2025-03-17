package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import uz.consortgroup.userservice.config.OneIdProperties;
import uz.consortgroup.userservice.dto.OneIdTokenResponse;
import uz.consortgroup.userservice.dto.OneIdUserInfo;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneIdService {
    private final OneIdProperties oneIdProperties;
    private final WebClient webClient;

    /**
     * Генерация URL для авторизации через One ID.
     */
    public URI generateAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl(oneIdProperties.getAuthUrl())
                .queryParam("response_type", "one_code")
                .queryParam("client_id", oneIdProperties.getClientId())
                .queryParam("redirect_uri", oneIdProperties.getRedirectUri())
                .queryParam("scope", oneIdProperties.getScope())
                .queryParam("state", UUID.randomUUID().toString())
                .build()
                .toUri();
    }

    /**
     * Запрос токена по коду авторизации.
     */
    public Mono<OneIdTokenResponse> getToken(String authCode) {
        log.info("Запрашиваем токен в One ID...");

        // Создаём MultiValueMap и добавляем параметры
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", oneIdProperties.getGrantType());
        formData.add("client_id", oneIdProperties.getClientId());
        formData.add("client_secret", oneIdProperties.getClientSecret());
        formData.add("redirect_uri", oneIdProperties.getRedirectUri());
        formData.add("code", authCode);

        return webClient.post()
                .uri(oneIdProperties.getTokenUrl())
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(OneIdTokenResponse.class)
                .doOnError(error -> log.error("Ошибка при получении токена: {}", error.getMessage()));
    }

    /**
     * Получение информации о пользователе через accessToken.
     */
    public Mono<OneIdUserInfo> getUserInfo(String accessToken) {
        log.info("Запрашиваем информацию о пользователе...");

        return webClient.get()
                .uri(oneIdProperties.getUserInfoUrl())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(OneIdUserInfo.class)
                .doOnError(error -> log.error("Ошибка при получении данных пользователя: {}", error.getMessage()));
    }
}
