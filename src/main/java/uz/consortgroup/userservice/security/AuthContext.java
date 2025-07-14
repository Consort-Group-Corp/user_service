package uz.consortgroup.userservice.security;   // «infrastructure» / «security» слой

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthContext {
    public UUID getActorId() {
        return getPrincipal().getId();
    }

    public UserRole getActorRole() {
        return getPrincipal().getUserRole();
    }

    @AllAspect
    public HasAuthContext getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Unauthenticated request");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof HasAuthContext ctx) {
            return ctx;
        }

        throw new IllegalStateException("Principal is not recognized");
    }
}
