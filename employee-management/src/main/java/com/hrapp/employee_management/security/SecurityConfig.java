package com.hrapp.employee_management.security;

import com.hrapp.employee_management.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOriginPatterns(java.util.Arrays.asList(
                            "http://localhost:4200",
                            "http://localhost:5173"
                    ));
                    config.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(java.util.Arrays.asList("*"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/api/auth/login", "/api/auth/forgot-password",
                                "/api/auth/reset-password", "/api/auth/change-password").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/uploads/profile/**").permitAll()

                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Attendance
                        .requestMatchers("/api/attendance/mark/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/me/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/override").hasRole("ADMIN")
                        .requestMatchers("/api/attendance/all/**").hasAnyRole("HR", "ADMIN")
                        .requestMatchers("/api/attendance/status/**").hasRole("HR")

                        // Users
                        .requestMatchers("/api/users/me", "/api/users/me/**").hasAnyRole("EMPLOYEE", "HR", "ADMIN")
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")

                        // HR/Admin
                        .requestMatchers("/api/hr/**").hasRole("HR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Leave
                        .requestMatchers("/api/leave/**").hasRole("HR")

                        // All other requests
                        .anyRequest().authenticated()
                )

                // ✅ Disable default form login (new style)
                .formLogin(form -> form.disable())

                // ✅ Make session stateless for JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//                .formLogin().disable();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        this is old for my reference
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .userDetailsService(customUserDetailsService)
//                .passwordEncoder(passwordEncoder())
//                .and()
//                .build();
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
