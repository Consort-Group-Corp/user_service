package uz.consortgroup.userservice.service.one_id;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import uz.consortgroup.core.api.v1.dto.user.request.OneIdUserInfoDto;
import uz.consortgroup.core.api.v1.dto.user.response.OneIdTokenResponseDto;
import uz.consortgroup.userservice.config.properties.OneIdProperties;
import uz.consortgroup.userservice.util.OneIdConstants;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneIdService {
    private final OneIdProperties oneIdProperties;
    private final WebClient webClient;

    /**
     * Генерация ссылки авторизации для One ID.
     */
    public URI generateAuthUrl() {
        log.info("Генерация ссылки авторизации для One ID");
        return UriComponentsBuilder.fromHttpUrl(oneIdProperties.getAuthUrl())
                .queryParam(OneIdConstants.RESPONSE_TYPE, oneIdProperties.getResponseType())
                .queryParam(OneIdConstants.CLIENT_ID, oneIdProperties.getClientId())
                .queryParam(OneIdConstants.REDIRECT_URI, oneIdProperties.getRedirectUri())
                .queryParam(OneIdConstants.SCOPE, oneIdProperties.getScope())
                .queryParam(OneIdConstants.STATE, UUID.randomUUID().toString())
                .build()
                .toUri();
    }

    /**
     * Обработка коллбэка One ID.
     */
    public Mono<ResponseEntity<?>> processCallback(String code) {
        log.info("Обработка коллбэка One ID, код: {}", code);
        return getToken(code)
                .flatMap(tokenResponse -> {
                    if (tokenResponse.getAccessToken() == null) {
                        log.error("Ошибка: токен One ID пустой!");
                        return Mono.just(ResponseEntity.badRequest().body("Ошибка: не получен токен"));
                    }
                    return getUserInfo(tokenResponse.getAccessToken())
                            .map(ResponseEntity::ok)
                            .map(response -> (ResponseEntity<?>) response);
                })
                .onErrorResume(e -> {
                    log.error("Ошибка One ID: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body("Ошибка One ID: " + e.getMessage()));
                });
    }

    /**
     * Получение токена по коду авторизации.
     */
    public Mono<OneIdTokenResponseDto> getToken(String authCode) {
        log.info("Запрос токена для кода: {}", authCode);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OneIdConstants.GRANT_TYPE, oneIdProperties.getGrantType());
        formData.add(OneIdConstants.CLIENT_ID, oneIdProperties.getClientId());
        formData.add(OneIdConstants.CLIENT_SECRET, oneIdProperties.getClientSecret());
        formData.add(OneIdConstants.REDIRECT_URI, oneIdProperties.getRedirectUri());
        formData.add(OneIdConstants.CODE, authCode);

        return webClient.post()
                .uri(oneIdProperties.getTokenUrl())
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(OneIdTokenResponseDto.class)
                .doOnError(error -> log.error("Ошибка при получении токена: {}", error.getMessage()));
    }

    /**
     * Получение информации о пользователе по токену.
     */
    public Mono<OneIdUserInfoDto> getUserInfo(String accessToken) {
        log.info("Запрос данных пользователя с токеном: {}", accessToken);

        return webClient.get()
                .uri(oneIdProperties.getUserInfoUrl())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(OneIdUserInfoDto.class)
                .doOnError(error -> log.error("Ошибка при получении данных пользователя: {}", error.getMessage()));
    }

    /**
     * Проверка валидности токена.
     */
    public Mono<Boolean> validateToken(String accessToken) {
        log.info("Проверка валидности токена");

        return webClient.get()
                .uri(oneIdProperties.getUserInfoUrl())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorResume(e -> {
                    log.error("Ошибка при проверке токена: {}", e.getMessage());
                    return Mono.just(false);
                });
    }
}
