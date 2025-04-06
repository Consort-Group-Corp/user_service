package uz.consortgroup.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.exception.UserNotFoundException;
import uz.consortgroup.user_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Searching user by email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> {
                    log.error("User with email {} not found", email);
                    return new UserNotFoundException("");
                });

        log.info("User found: {}", user.getEmail());

        return UserDetailsImpl.build(user);
    }
}
