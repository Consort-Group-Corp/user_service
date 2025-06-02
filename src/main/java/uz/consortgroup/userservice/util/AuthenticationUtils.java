package uz.consortgroup.userservice.util;

import lombok.RequiredArgsConstructor;
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
import uz.consortgroup.userservice.asspect.annotation.AllAspect;

@Component
@RequiredArgsConstructor
public class AuthenticationUtils {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @AllAspect
    public JwtResponse performAuthentication(LoginRequest loginRequest, UserDetails userDetails) {
        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
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

        return JwtResponse.builder()
                .token(jwt)
                .role(role)
                .build();
    }

    @AllAspect
    public JwtResponse performAuthentication(String email, Authentication authentication) {
        String jwt = jwtUtils.generateJwtToken(authentication);
        String authority = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow();

        UserRole role = UserRole.valueOf(authority);

        return JwtResponse.builder()
                .token(jwt)
                .role(role)
                .build();
    }
}