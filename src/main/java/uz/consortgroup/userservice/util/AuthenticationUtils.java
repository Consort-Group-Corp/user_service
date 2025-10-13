package uz.consortgroup.userservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.List;
import java.util.stream.Collectors;

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

        List<UserRole> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::valueOf)
                .collect(Collectors.toList());

        log.info("Authentication successful for user: {}, roles: {}", loginRequest.getEmail(), roles);

        return JwtResponse.builder()
                .token(jwt)
                .role(roles.isEmpty() ? null : roles.getFirst())
                .build();
    }

    public JwtResponse performAuthentication(String email, org.springframework.security.core.Authentication authentication) {
        log.info("Generating JWT for authenticated user: {}", email);

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<UserRole> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::valueOf)
                .collect(Collectors.toList());

        log.info("JWT generated successfully for user: {}, roles: {}", email, roles);

        return JwtResponse.builder()
                .token(jwt)
                .role(roles.isEmpty() ? null : roles.getFirst())
                .build();
    }
}
