package uz.consortgroup.userservice.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAuthService userAuthService;
    private final SuperAdminAuthService superAdminAuthService;

    @AllAspect
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        return userAuthService.authenticate(loginRequest);
    }

    @AllAspect
    public JwtResponse authenticateSuperAdmin(LoginRequest loginRequest) {
        return superAdminAuthService.authenticate(loginRequest);
    }
}