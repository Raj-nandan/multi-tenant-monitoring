package com.raj.monitoring.authservice.exception;

import com.raj.monitoring.authservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Authentication failed: " + ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("An unexpected error occurred: " + ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
