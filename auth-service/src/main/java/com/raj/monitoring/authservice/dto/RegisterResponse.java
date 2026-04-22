package com.raj.monitoring.authservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String id;
    private String username;
    private String email;
    private String role;
    private String message;
}
