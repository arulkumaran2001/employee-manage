package com.hrapp.employee_management.security;

import com.hrapp.employee_management.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI().toLowerCase();
        if (requestPath.startsWith("/api/auth/") || request.getMethod().equalsIgnoreCase("OPTIONS") || requestPath.startsWith("/uploads/profile/"))  {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
            return;
        }

        String jwt = authHeader.substring(7);
        String username;
        try {
            username = jwtUtils.getUsernameFromToken(jwt);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
            return;
        }

        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: no username.");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found.");
                return;
            }

            if (!jwtUtils.validateToken(jwt)) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
                return;
            }

            List<String> roles = jwtUtils.getRolesFromToken(jwt);
            if (roles == null || roles.isEmpty()) {
                roles = List.of("EMPLOYEE");
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(String::toUpperCase)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            System.out.println("🔹 JWT username: " + username);
            System.out.println("🔹 Roles from token: " + roles);
            System.out.println("🔹 Authorities assigned: " + authorities);
            System.out.println("🔹 User enabled: " + userDetails.isEnabled());
            System.out.println("🔹 User account non-locked: " + userDetails.isAccountNonLocked());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> error = Map.of("error", message);
        mapper.writeValue(response.getWriter(), error);
    }
}
