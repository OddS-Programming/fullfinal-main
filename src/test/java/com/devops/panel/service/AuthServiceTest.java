package com.devops.panel.service;

import com.devops.panel.dto.LoginRequest;
import com.devops.panel.dto.RegisterRequest;
import com.devops.panel.entity.Role;
import com.devops.panel.entity.User;
import com.devops.panel.exception.BadRequestException;
import com.devops.panel.repository.UserRepository;
import com.devops.panel.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;

    @Test
    void register_WhenUsernameFree_CreatesUserAndReturnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@mail.com");
        req.setPassword("password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User saved = User.builder().id(1L).username("newuser").email("new@mail.com").password("encoded").role(Role.USER).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var result = authService.register(req);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void register_WhenUsernameExists_ThrowsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setEmail("e@mail.com");
        req.setPassword("pass");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
    }
}
