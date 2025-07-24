package uz.consortgroup.userservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationUtils {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public JwtResponse performAuthentication(LoginRequest loginRequest, UserDetails userDetails) {
        log.info("Authenticating user with email: {}", loginRequest.getEmail());

        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            log.warn("Authentication failed for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String jwt = jwtUtils.generateJwtToken(authentication);
        String authority = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow();

        UserRole role = UserRole.valueOf(authority);

        log.info("Authentication successful for user: {}, role: {}", loginRequest.getEmail(), role);

        return JwtResponse.builder()
                .token(jwt)
                .role(role)
                .build();
    }

    public JwtResponse performAuthentication(String email, Authentication authentication) {
        log.info("Generating JWT for authenticated user: {}", email);

        String jwt = jwtUtils.generateJwtToken(authentication);
        String authority = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow();

        UserRole role = UserRole.valueOf(authority);

        log.info("JWT generated successfully for user: {}, role: {}", email, role);

        return JwtResponse.builder()
                .token(jwt)
                .role(role)
                .build();
    }
}
