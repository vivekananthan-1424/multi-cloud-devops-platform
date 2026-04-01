package com.admin.config;

import com.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // ─── Authentication Provider ──────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─── HTTP Security & RBAC Rules ───────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // Actuator health endpoint (for Kubernetes liveness probes)
                .requestMatchers("/actuator/health").permitAll()
                // Admin-only pages
                .requestMatchers("/users/delete/**", "/users/create").hasRole("ADMIN")
                // Admin and Manager can view/edit users
                .requestMatchers("/users/**").hasAnyRole("ADMIN", "MANAGER")
                // All other pages require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)               // Prevent concurrent sessions
                .maxSessionsPreventsLogin(false)  // New login kicks old session
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // ─── Seed Default Users on Startup ────────────────────────────────────────

    @Bean
    public CommandLineRunner seedData(UserService userService) {
        return args -> userService.seedDefaultUsers();
    }
}