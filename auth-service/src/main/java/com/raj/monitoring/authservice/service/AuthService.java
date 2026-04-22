package com.raj.monitoring.authservice.service;

import com.raj.monitoring.authservice.dto.LoginRequest;
import com.raj.monitoring.authservice.dto.LoginResponse;
import com.raj.monitoring.authservice.dto.RegisterRequest;
import com.raj.monitoring.authservice.dto.RegisterResponse;
import com.raj.monitoring.authservice.entity.Role;
import com.raj.monitoring.authservice.entity.User;
import com.raj.monitoring.authservice.exception.ResourceAlreadyExistsException;
import com.raj.monitoring.authservice.exception.ResourceNotFoundException;
import com.raj.monitoring.authservice.repository.UserRepository;
import com.raj.monitoring.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .message("User registered successfully")
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Try to authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Find user by username or email
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
