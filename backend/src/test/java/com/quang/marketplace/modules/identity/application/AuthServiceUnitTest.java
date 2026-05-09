package com.quang.marketplace.modules.identity.application;

import com.quang.marketplace.shared.error.DuplicateEmailException;
import com.quang.marketplace.shared.error.InvalidCredentialsException;
import com.quang.marketplace.modules.identity.api.RegisterRequest;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    UserRepository userRepository;

    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void register_hashes_password_and_saves() {
        RegisterRequest req = new RegisterRequest("u1@example.com", "password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("u1@example.com", saved.getEmail());
        assertEquals("hashed-secret", saved.getPasswordHash());
        assertEquals("u1@example.com", resp.email());
    }

    @Test
    void register_duplicate_throws() {
        RegisterRequest req = new RegisterRequest("dup@example.com", "password123");
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(req));
    }

    @Test
    void login_invalid_credentials_throw() {
        RegisterRequest req = new RegisterRequest("nope@example.com", "password123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req, mock(HttpServletRequest.class)));
    }

    @Test
    void login_wrong_password_throws() {
        RegisterRequest req = new RegisterRequest("me@example.com", "password123");
        User u = new User("me@example.com", "stored-hash");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req, mock(HttpServletRequest.class)));
    }

    @Test
    void login_success_persists_security_context_in_session() {
        RegisterRequest req = new RegisterRequest("me2@example.com", "password123");
        User u = new User("me2@example.com", "stored-hash");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(httpRequest.getSession(true)).thenReturn(session);

        var resp = authService.login(req, httpRequest);

        verify(session).setAttribute(eq(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
        assertEquals("me2@example.com", resp.email());
    }
}
