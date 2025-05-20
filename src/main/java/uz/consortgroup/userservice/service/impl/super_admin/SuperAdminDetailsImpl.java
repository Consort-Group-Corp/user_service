package uz.consortgroup.userservice.service.impl.super_admin;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.consortgroup.userservice.entity.SuperAdmin;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class SuperAdminDetailsImpl implements UserDetails {
    private final String email;
    private final String password;
    private final List<GrantedAuthority> authorities;

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

    public static SuperAdminDetailsImpl buildSuperAdmin(SuperAdmin superAdmin) {
        String password = superAdmin.getPasswordHash() != null ? superAdmin.getPasswordHash() : "";
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(superAdmin.getUserRole() != null ? superAdmin.getUserRole().name() : "SUPER_ADMIN")
        );
        return new SuperAdminDetailsImpl(superAdmin.getEmail(), password, authorities);
    }
}
