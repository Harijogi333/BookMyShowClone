package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.SignupRequest;
import com.clone.BookMyShow.dto.UserResponse;
import com.clone.BookMyShow.entity.Role;
import com.clone.BookMyShow.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for AuthController with full request/response logging.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Slf4j
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;

    private SignupRequest signupRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPhone("9876543210");
        signupRequest.setPassword("password123");

        userResponse = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("POST /signup - Success path with response logging")
    void signup_Success() throws Exception {
        log.info("Starting AuthController POST /signup test...");
        
        when(userService.registerUser(any(SignupRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print()) // This logs the FULL HTTP request and response to the console
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        log.info("Finished AuthController test. Status: 201 Created.");
    }

    @Test
    @DisplayName("POST /signup - Fails when validation fails")
    void signup_ValidationError() throws Exception {
        log.info("Testing validation failure for /signup...");
        
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setName(""); // Trigger @NotBlank

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print()) // Log details of the 400 Bad Request error
                .andExpect(status().isBadRequest());
                
        log.info("Successfully caught validation error.");
    }
}
