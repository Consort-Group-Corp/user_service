package uz.consortgroup.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Attempting to load user by email: {}", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    log.info("User found for email: {}", email);
                    return UserDetailsImpl.build(user);
                })
                .orElseThrow(() -> {
                    log.warn("User with email {} not found", email);
                    return new UserNotFoundException(String.format("User with email %s not found", email));
                });
    }
}
