package uz.consortgroup.userservice.service.one_id;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import uz.consortgroup.core.api.v1.dto.oneid.OneIdProfile;
import uz.consortgroup.core.api.v1.dto.oneid.OneIdTokenRequest;
import uz.consortgroup.core.api.v1.dto.oneid.OneIdTokenResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.userservice.config.properties.OneIdProperties;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;
import uz.consortgroup.userservice.service.mintrud.MehnatAutoFillService;
import uz.consortgroup.userservice.util.AuthenticationUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneIdServiceImpl implements OneIdService {
    private final OneIdProperties properties;
    private final WebClient webClientWithTimeout;
    private final UserRepository userRepository;
    private final AuthenticationUtils authenticationUtils;
    private final MehnatAutoFillService mehnatAutoFillService;

    @Override
    public String buildAuthUrl() {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("scope", "openid")
                .toUriString();

        log.info("Generated OneID Auth URL: {}", url);
        return url;
    }

    @Override
    @Transactional
    public JwtResponse authorizeViaOneId(String code) {
        log.info("Authorizing user via OneID with code={}", code);

        OneIdTokenResponse tokens = exchangeCodeForTokens(code);
        log.info("Received OneID tokens: accessTokenExpiresIn={}s", tokens.getExpiresIn());

        OneIdProfile profile = fetchProfile(tokens.getAccessToken());
        log.info("Fetched OneID profile for userId={}, email={}", profile.getUserId(), profile.getEmail());

        User user = processUserFromOneId(tokens, profile);
        log.info("User processed and saved: userId={}, email={}", user.getId(), user.getEmail());

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        JwtResponse response = authenticationUtils.performAuthentication(user.getEmail(), authentication);
        log.info("JWT token generated for userId={}", user.getId());

        return response;
    }

    public OneIdTokenResponse exchangeCodeForTokens(String code) {
        log.info("Exchanging code for OneID tokens...");

        OneIdTokenRequest body = OneIdTokenRequest.builder()
                .grant_type("authorization_code")
                .code(code)
                .client_id(properties.getClientId())
                .client_secret(properties.getClientSecret())
                .redirect_uri(properties.getRedirectUri())
                .build();

        try {
            OneIdTokenResponse response = webClientWithTimeout
                    .post()
                    .uri(properties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(OneIdTokenResponse.class)
                    .block();

            log.info("Successfully retrieved token from OneID");
            return response;
        } catch (Exception e) {
            log.error("Failed to exchange code for tokens: {}", e.getMessage(), e);
            throw e;
        }
    }

    public OneIdProfile fetchProfile(String accessToken) {
        log.info("Fetching OneID profile using accessToken...");

        try {
            OneIdProfile profile = webClientWithTimeout
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(properties.getProfileUrl())
                            .queryParam("access_token", accessToken)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(OneIdProfile.class)
                    .block();

            log.info("Successfully fetched profile from OneID for userId={}", profile.getUserId());
            return profile;
        } catch (Exception e) {
            log.error("Failed to fetch OneID profile: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public User processUserFromOneId(OneIdTokenResponse tokens, OneIdProfile profile) {
        log.info("Processing user from OneID profile, userId={}", profile.getUserId());

        User user = userRepository.findByOneIdUserId(profile.getUserId())
                .map(existing -> {
                    existing.setFirstName(profile.getFirstName());
                    existing.setLastName(profile.getSurName());
                    existing.setMiddleName(profile.getMidName());
                    existing.setEmail(profile.getEmail());
                    existing.setPhoneNumber(profile.getPhoneNumber());
                    existing.setPinfl(profile.getPin());
                    return existing;
                })
                .orElseGet(() -> User.builder()
                        .oneIdUserId(profile.getUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getSurName())
                        .middleName(profile.getMidName())
                        .email(profile.getEmail())
                        .phoneNumber(profile.getPhoneNumber())
                        .pinfl(profile.getPin())
                        .language(Language.ENGLISH)
                        .role(UserRole.STUDENT)
                        .status(UserStatus.ACTIVE)
                        .isVerified(true)
                        .build());

        user.setOneIdAccessToken(tokens.getAccessToken());
        user.setOneIdRefreshToken(tokens.getRefreshToken());
        user.setOneIdExpiresAt(Instant.now().plusSeconds(tokens.getExpiresIn()));
        user.setOneIdTokenIssuedAt(Instant.now());
        user.setOneIdTokenUpdatedAt(Instant.now());

        user = userRepository.save(user);

        log.info("User saved with ID={}, email={}", user.getId(), user.getEmail());

        try {
            mehnatAutoFillService.tryFetchDataFromMehnat(user);
        } catch (Exception e) {
            log.warn("Mehnat autofill failed for userId={}: {}", user.getId(), e.getMessage());
        }

        return user;
    }
}
