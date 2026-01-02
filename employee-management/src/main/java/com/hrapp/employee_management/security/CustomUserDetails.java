package com.hrapp.employee_management.security;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public String getRole() {
        return user.getRole();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public String getName() { return user.getName(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.getStatus() == UserStatus.ACTIVE; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return user.getStatus() == UserStatus.ACTIVE; }
}
