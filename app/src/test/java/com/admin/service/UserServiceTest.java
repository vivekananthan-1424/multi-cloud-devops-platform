package com.admin.service;

import com.admin.dto.UserDto;
import com.admin.model.Role;
import com.admin.model.User;
import com.admin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private UserDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .username("johndoe")
                .email("john@example.com")
                .password("encoded_pass")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        sampleDto = new UserDto();
        sampleDto.setFullName("John Doe");
        sampleDto.setUsername("johndoe");
        sampleDto.setEmail("john@example.com");
        sampleDto.setPassword("password123");
        sampleDto.setRole(Role.ROLE_USER);
        sampleDto.setEnabled(true);
    }

    // ─── Create User ──────────────────────────────────────────────────────────

    @Test
    void createUser_ShouldSucceed_WhenValidDto() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.createUser(sampleDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("johndoe");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrow_WhenUsernameExists() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrow_WhenEmailExists() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already in use");
    }

    // ─── Get User ─────────────────────────────────────────────────────────────

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── Get All Users ────────────────────────────────────────────────────────

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("johndoe");
    }

    // ─── Toggle Status ────────────────────────────────────────────────────────

    @Test
    void toggleUserStatus_ShouldFlipEnabled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.toggleUserStatus(1L);

        assertThat(sampleUser.isEnabled()).isFalse(); // was true, now false
        verify(userRepository).save(sampleUser);
    }

    // ─── Delete User ──────────────────────────────────────────────────────────

    @Test
    void deleteUser_ShouldDelete_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrow_WhenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    @Test
    void getDashboardStats_ShouldReturnCorrectKeys() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByEnabled(true)).thenReturn(8L);
        when(userRepository.countByEnabled(false)).thenReturn(2L);
        when(userRepository.countByRole(Role.ROLE_ADMIN)).thenReturn(1L);
        when(userRepository.countByRole(Role.ROLE_MANAGER)).thenReturn(2L);
        when(userRepository.countByRole(Role.ROLE_USER)).thenReturn(7L);

        Map<String, Long> stats = userService.getDashboardStats();

        assertThat(stats).containsKeys(
                "totalUsers", "activeUsers", "inactiveUsers",
                "adminCount", "managerCount", "userCount"
        );
        assertThat(stats.get("totalUsers")).isEqualTo(10L);
        assertThat(stats.get("activeUsers")).isEqualTo(8L);
    }
}
