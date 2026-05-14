package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.AuthResponse;
import com.clone.BookMyShow.dto.LoginRequest;
import com.clone.BookMyShow.dto.SignupRequest;
import com.clone.BookMyShow.dto.UserResponse;
import com.clone.BookMyShow.entity.Role;
import com.clone.BookMyShow.entity.User;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.repository.UserRepository;
import com.clone.BookMyShow.security.JwtUtils;
import com.clone.BookMyShow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public UserResponse registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email '" + signupRequest.getEmail() + "' is already registered.");
        }
        if (userRepository.existsByPhone(signupRequest.getPhone())) {
            throw new ResourceAlreadyExistsException("Phone number '" + signupRequest.getPhone() + "' is already registered.");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPhone(signupRequest.getPhone());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        String jwtToken = jwtUtils.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
