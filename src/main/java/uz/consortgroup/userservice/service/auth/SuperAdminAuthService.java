package uz.consortgroup.userservice.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.service.impl.super_admin.SuperAdminDetailsServiceImpl;
import uz.consortgroup.userservice.util.AuthenticationUtils;

@Service
@RequiredArgsConstructor
public class SuperAdminAuthService {
    private final SuperAdminDetailsServiceImpl superAdminDetailsService;
    private final AuthenticationUtils authenticationUtils;

    @AllAspect
    public JwtResponse authenticate(LoginRequest loginRequest) {
        UserDetails userDetails = superAdminDetailsService.loadUserByUsername(loginRequest.getEmail());
        return authenticationUtils.performAuthentication(loginRequest, userDetails);
    }
}