package com.example.backend.service;

import com.example.backend.domain.ComUser;
import com.example.backend.repository.jpa.ComUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComUserDetailsService implements UserDetailsService {

    private final ComUserRepository comUserRepository;

    public ComUserDetailsService(ComUserRepository comUserRepository) {
        this.comUserRepository = comUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ComUser user = comUserRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(resolveAuthorities(user.getRole()))
                .build();
    }

    private List<SimpleGrantedAuthority> resolveAuthorities(String role) {
        String normalizedRole = normalizeRole(role);
        return List.of(new SimpleGrantedAuthority(normalizedRole));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        if (!"ROLE_ADMIN".equals(normalized) && !"ROLE_USER".equals(normalized)) {
            return "ROLE_USER";
        }
        return normalized;
    }
}
