package com.admin;

import com.admin.repository.UserRepository;
import com.admin.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AdminDashboardApplicationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        // Verifies the Spring context starts up without errors
        assertThat(userRepository).isNotNull();
        assertThat(userService).isNotNull();
    }

    @Test
    void seedData_ShouldCreateDefaultUsers() {
        // After startup, default users should be seeded
        assertThat(userRepository.count()).isGreaterThan(0);
        assertThat(userRepository.findByUsername("admin")).isPresent();
        assertThat(userRepository.findByUsername("manager1")).isPresent();
    }
}
