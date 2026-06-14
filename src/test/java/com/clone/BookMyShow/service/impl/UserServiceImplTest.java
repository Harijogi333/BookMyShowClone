package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.SignupRequest;
import com.clone.BookMyShow.dto.UserResponse;
import com.clone.BookMyShow.entity.Role;
import com.clone.BookMyShow.entity.User;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.repository.UserRepository;
import com.clone.BookMyShow.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for UserServiceImpl with logging and detailed assertions.
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private UserServiceImpl userService;

    private SignupRequest signupRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPhone("9876543210");
        signupRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("9876543210");
        user.setRole(Role.USER);
        user.setIsActive(true);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void registerUser_Success() {
        log.info("Running test: registerUser_Success for email: {}", signupRequest.getEmail());

        // Arrange
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(signupRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse response = userService.registerUser(signupRequest);

        // Assert
        log.info("Verifying registration response data...");
        assertThat(response).as("The UserResponse should not be null").isNotNull();
        assertThat(response.getEmail()).as("Response email must match input").isEqualTo("test@example.com");
        assertThat(response.getRole()).as("Default role must be USER").isEqualTo(Role.USER);
        
        verify(userRepository, times(1)).save(any(User.class));
        log.info("Test registerUser_Success completed successfully.");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_EmailAlreadyExists() {
        log.info("Running test: registerUser_EmailAlreadyExists");

        // Arrange
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            userService.registerUser(signupRequest);
        }, "Should throw ResourceAlreadyExistsException when email is taken");

        verify(userRepository, never()).save(any(User.class));
        log.info("Verified that save was not called for duplicate email.");
    }

    @Test
    @DisplayName("Should throw exception when phone already exists")
    void registerUser_PhoneAlreadyExists() {
        log.info("Running test: registerUser_PhoneAlreadyExists");

        // Arrange
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(signupRequest.getPhone())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            userService.registerUser(signupRequest);
        }, "Should throw ResourceAlreadyExistsException when phone is taken");

        verify(userRepository, never()).save(any(User.class));
        log.info("Verified that save was not called for duplicate phone.");
    }
}
