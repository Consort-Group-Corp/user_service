package uz.consortgroup.userservice.service;

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
import uz.consortgroup.userservice.config.properties.OneIdProperties;
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

    public URI generateAuthUrl() {
        log.info("Генерация ссылки авторизации для One ID");
        return UriComponentsBuilder.fromHttpUrl(oneIdProperties.getAuthUrl())
                .queryParam("response_type", "one_code")
                .queryParam("client_id", oneIdProperties.getClientId())
                .queryParam("redirect_uri", oneIdProperties.getRedirectUri())
                .queryParam("scope", oneIdProperties.getScope())
                .queryParam("state", UUID.randomUUID().toString())
                .build()
                .toUri();
    }

    public Mono<ResponseEntity<?>> processCallback(String code) {
        log.info("Обработка коллбэка One ID, код: {}", code);
        return getToken(code)
                .flatMap(tokenResponse -> {
                    if (tokenResponse.getAccessToken() == null) {
                        log.error("Ошибка: токен One ID пустой!");
                        return Mono.just(ResponseEntity.badRequest().body("Ошибка: не получен токен"));
                    }
                    return getUserInfo(tokenResponse.getAccessToken())
                            .map(userInfo -> ResponseEntity.ok().body(userInfo)) // ✅ ResponseEntity<OneIdUserInfo>
                            .map(response -> (ResponseEntity<?>) response); // ✅ Приводим к ResponseEntity<?>
                })
                .onErrorResume(e -> {
                    log.error("Ошибка One ID: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body("Ошибка One ID: " + e.getMessage()));
                });
    }


    public Mono<OneIdTokenResponse> getToken(String authCode) {
        log.info("Запрос токена для кода: {}", authCode);

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

    public Mono<OneIdUserInfo> getUserInfo(String accessToken) {
        log.info("Запрос данных пользователя с токеном: {}", accessToken);

        return webClient.get()
                .uri(oneIdProperties.getUserInfoUrl())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(OneIdUserInfo.class)
                .doOnError(error -> log.error("Ошибка при получении данных пользователя: {}", error.getMessage()));
    }

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
