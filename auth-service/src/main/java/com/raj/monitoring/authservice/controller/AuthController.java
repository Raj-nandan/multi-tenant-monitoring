package com.raj.monitoring.authservice.controller;

import com.raj.monitoring.authservice.dto.ApiResponse;
import com.raj.monitoring.authservice.dto.LoginRequest;
import com.raj.monitoring.authservice.dto.LoginResponse;
import com.raj.monitoring.authservice.dto.RegisterRequest;
import com.raj.monitoring.authservice.dto.RegisterResponse;
import com.raj.monitoring.authservice.security.JwtService;
import com.raj.monitoring.authservice.service.AuthService;
import com.raj.monitoring.authservice.service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        ApiResponse apiResponse = ApiResponse.builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        ApiResponse apiResponse = ApiResponse.builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        ApiResponse apiResponse = ApiResponse.builder()
                .success(true)
                .message("User authenticated successfully")
                .data(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            ApiResponse apiResponse = ApiResponse.builder()
                    .success(false)
                    .message("No token provided")
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(apiResponse);
        }

        String token = authorizationHeader.substring(7);

        // Get remaining token expiration time
        long remainingTimeSeconds = jwtService.extractExpirationTime(token);

        // Blacklist the token
        tokenBlacklistService.blacklistToken(token, remainingTimeSeconds);

        ApiResponse apiResponse = ApiResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .data(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
