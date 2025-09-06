package uz.consortgroup.userservice.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.security.CustomAccessDeniedHandler;
import uz.consortgroup.userservice.service.impl.UserDetailsServiceImpl;
import uz.consortgroup.userservice.service.impl.super_admin.SuperAdminDetailsServiceImpl;
import uz.consortgroup.userservice.util.AuthEntryPointJwt;
import uz.consortgroup.userservice.util.AuthTokenFilter;
import uz.consortgroup.userservice.util.JwtUtils;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final SuperAdminDetailsServiceImpl superAdminDetailsService;
    private final JwtUtils jwtUtils;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService, superAdminDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider superAdminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(superAdminDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/*/verification").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/*/new-verification-code").permitAll()
                        .requestMatchers(HttpMethod.PUT,  "/api/v1/users/*/new-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/password/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/registration/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/device-tokens/**").permitAll()

                        // super-admin
                        .requestMatchers(HttpMethod.POST, "/api/v1/super-admin/**")
                        .hasAuthority(UserRole.SUPER_ADMIN.name())

                        // разрешённые POST без ролей (как было)
                        .requestMatchers(HttpMethod.POST, "/api/v1/user-notifications/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/device-tokens/**").permitAll()

                        // СПЕЦИФИЧНЫЕ ПРАВИЛА ПЕРЕД ОБЩИМ /api/v1/users/**
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/users/course-orders",
                                "/api/v1/users/course-orders/**"
                        ).hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.ADMIN.name(),
                                UserRole.MENTOR.name(),
                                UserRole.HR.name(),
                                UserRole.STUDENT.name()
                        )
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/search").hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.ADMIN.name(),
                                UserRole.MENTOR.name(),
                                UserRole.HR.name()
                        )

                        // ОБЩЕЕ правило на все остальные POST под /api/v1/users/**
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.ADMIN.name()
                        )

                        // HR / Mentor зоны
                        .requestMatchers(HttpMethod.POST, "/api/v1/hr/**").hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.ADMIN.name(),
                                UserRole.MENTOR.name(),
                                UserRole.HR.name()
                        )
                        .requestMatchers(HttpMethod.POST, "/api/v1/mentor/**").hasAnyAuthority(
                                UserRole.MENTOR.name(),
                                UserRole.ADMIN.name(),
                                UserRole.SUPER_ADMIN.name()
                        )

                        // всё остальное
                        .anyRequest().permitAll()
                );

        http.authenticationProvider(superAdminAuthenticationProvider());
        http.authenticationProvider(userAuthenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
