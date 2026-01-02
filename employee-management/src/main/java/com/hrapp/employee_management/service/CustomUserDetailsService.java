package com.hrapp.employee_management.service;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import com.hrapp.employee_management.repository.UserRepository;
import com.hrapp.employee_management.security.CustomUserDetails;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new UsernameNotFoundException("Account is deactivated.");
        }

        return new CustomUserDetails(user);
    }

    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException("Invalid authentication");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

}

