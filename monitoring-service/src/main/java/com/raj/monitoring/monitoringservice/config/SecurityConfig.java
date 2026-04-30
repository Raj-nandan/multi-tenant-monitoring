package com.raj.monitoring.monitoringservice.config;

import com.raj.monitoring.monitoringservice.security.JwtAuthenticationFilter;
import com.raj.monitoring.monitoringservice.util.TenantHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantHeaderFilter tenantHeaderFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, TenantHeaderFilter tenantHeaderFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantHeaderFilter = tenantHeaderFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/monitors/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(tenantHeaderFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, TenantHeaderFilter.class);

        return http.build();
    }
}
