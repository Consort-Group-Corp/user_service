package uz.consortgroup.userservice.service.impl.super_admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.consortgroup.userservice.entity.SuperAdmin;
import uz.consortgroup.userservice.service.impl.HasId;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SuperAdminDetailsImpl implements UserDetails, HasId {
    @Getter
    private final UUID id;
    private final String email;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public static SuperAdminDetailsImpl buildSuperAdmin(SuperAdmin superAdmin) {
        String password = superAdmin.getPasswordHash() != null ? superAdmin.getPasswordHash() : "";
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(
                        superAdmin.getUserRole() != null ? superAdmin.getUserRole().name() : "SUPER_ADMIN")
        );
        return new SuperAdminDetailsImpl(superAdmin.getId(), superAdmin.getEmail(), password, authorities);
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


