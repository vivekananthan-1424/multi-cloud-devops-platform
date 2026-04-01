package com.admin.service;

import com.admin.dto.UserDto;
import com.admin.model.Role;
import com.admin.model.User;
import com.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Spring Security Authentication ───────────────────────────────────────

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(user.getRole().name()))
                .disabled(!user.isEnabled())
                .build();
    }

    // ─── CRUD Operations ──────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use: " + dto.getEmail());
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? dto.getRole() : Role.ROLE_USER)
                .enabled(dto.isEnabled())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserDto dto) {
        User user = getUserById(id);

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setEnabled(dto.isEnabled());

        // Only update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    // ─── Dashboard Statistics ─────────────────────────────────────────────────

    public Map<String, Long> getDashboardStats() {
        long totalUsers    = userRepository.count();
        long activeUsers   = userRepository.countByEnabled(true);
        long inactiveUsers = userRepository.countByEnabled(false);
        long adminCount    = userRepository.countByRole(Role.ROLE_ADMIN);
        long managerCount  = userRepository.countByRole(Role.ROLE_MANAGER);
        long userCount     = userRepository.countByRole(Role.ROLE_USER);

        return Map.of(
                "totalUsers",    totalUsers,
                "activeUsers",   activeUsers,
                "inactiveUsers", inactiveUsers,
                "adminCount",    adminCount,
                "managerCount",  managerCount,
                "userCount",     userCount
        );
    }

    // ─── Seed Data (called on startup) ────────────────────────────────────────

    @Transactional
    public void seedDefaultUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder()
                .fullName("Super Admin").username("admin")
                .email("admin@company.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN).enabled(true).build());

        userRepository.save(User.builder()
                .fullName("John Manager").username("manager1")
                .email("manager1@company.com")
                .password(passwordEncoder.encode("manager123"))
                .role(Role.ROLE_MANAGER).enabled(true).build());

        userRepository.save(User.builder()
                .fullName("Jane Doe").username("user1")
                .email("user1@company.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER).enabled(true).build());

        userRepository.save(User.builder()
                .fullName("Bob Smith").username("user2")
                .email("user2@company.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER).enabled(false).build());
    }
}
