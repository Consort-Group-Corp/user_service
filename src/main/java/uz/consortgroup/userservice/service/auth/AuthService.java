package uz.consortgroup.userservice.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.userservice.exception.AuthenticationFailedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAuthService userAuthService;
    private final SuperAdminAuthService superAdminAuthService;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        log.info("Attempting to authenticate user with email: {}", email);
        try {
            JwtResponse response = userAuthService.authenticate(loginRequest);
            log.info("User authentication successful for email: {}", email);
            return response;
        } catch (Exception ex) {
            log.error("User authentication failed for email: {} — {}", email, ex.getMessage(), ex);
            throw new AuthenticationFailedException(email, ex);
        }
    }

    public JwtResponse authenticateSuperAdmin(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        log.info("Attempting to authenticate super admin with email: {}", email);
        try {
            JwtResponse response = superAdminAuthService.authenticate(loginRequest);
            log.info("Super admin authentication successful for email: {}", email);
            return response;
        } catch (Exception ex) {
            log.error("Super admin authentication failed for email: {} — {}", email, ex.getMessage(), ex);
            throw new AuthenticationFailedException(email, ex);
        }
    }
}

