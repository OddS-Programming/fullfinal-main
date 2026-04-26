package com.devops.panel.service;

import com.devops.panel.dto.AuthResponse;
import com.devops.panel.dto.LoginRequest;
import com.devops.panel.dto.RegisterRequest;
import com.devops.panel.entity.Role;
import com.devops.panel.entity.User;
import com.devops.panel.repository.UserRepository;
import com.devops.panel.security.JwtService;
import com.devops.panel.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username already exists - {}", request.getUsername());
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists - {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        log.info("User registered: id={}, username={}", user.getId(), user.getUsername());
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(), user.getPassword(),
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new com.devops.panel.exception.ResourceNotFoundException("User not found"));
        String roleName = user.getRole() != null ? user.getRole().name() : Role.USER.name();
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(), user.getPassword(),
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + roleName))));
        log.info("User logged in: {}", user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(roleName)
                .userId(user.getId())
                .build();
    }
}
