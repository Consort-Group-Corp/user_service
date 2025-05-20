package uz.consortgroup.userservice.service.impl.super_admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.SuperAdmin;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.repository.SuperAdminRepository;

@Service
@RequiredArgsConstructor
public class SuperAdminDetailsServiceImpl implements UserDetailsService {
    private final SuperAdminRepository superAdminRepository;

    @Override
    @AllAspect
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SuperAdmin superAdmin = superAdminRepository.findUserByEmail(email).orElseThrow(
                () -> new UserNotFoundException(String.format("SuperAdmin with email %s not found", email)));

        return SuperAdminDetailsImpl.buildSuperAdmin(superAdmin);
    }
}
