package uz.consortgroup.userservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.security.HasAuthContext;


import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class UserDetailsImpl implements UserDetails, HasId, HasAuthContext {
    @Getter
    private final UUID id;
    private final String email;
    private final String password;

    @Getter
    private UserRole userRole;

    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        String pwdHash = user.getPassword() != null ? user.getPassword().getPasswordHash() : "";
        UserRole role = user.getRole();
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                pwdHash,
                role,
                List.of(new SimpleGrantedAuthority(role.name()))
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
