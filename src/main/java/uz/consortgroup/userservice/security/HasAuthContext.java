package uz.consortgroup.userservice.security;

import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.UUID;

public interface HasAuthContext {
    UUID getId();
    UserRole getUserRole();
}