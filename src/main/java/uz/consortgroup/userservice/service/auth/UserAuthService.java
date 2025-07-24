package uz.consortgroup.userservice.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.userservice.exception.AuthenticationFailedException;
import uz.consortgroup.userservice.service.impl.UserDetailsServiceImpl;
import uz.consortgroup.userservice.util.AuthenticationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationUtils authenticationUtils;

    public JwtResponse authenticate(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        log.info("Authenticating user with email: {}", email);

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            JwtResponse jwtResponse = authenticationUtils.performAuthentication(loginRequest, userDetails);
            log.info("User authentication successful for email: {}", email);
            return jwtResponse;
        } catch (AuthenticationFailedException ex) {
            log.error("User authentication failed for email: {} — {}", email, ex.getMessage(), ex);
            throw new AuthenticationFailedException(email, ex);
        }
    }
}
