package com.quang.marketplace.modules.identity;

import com.quang.marketplace.modules.identity.application.AuthService;
import com.quang.marketplace.modules.identity.api.RegisterRequest;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceExceptionTest {

    @Mock
    UserRepository userRepository;

    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    void db_unavailable_while_loading_user_throws() {
        RegisterRequest req = new RegisterRequest("u1@example.com", "password123");

        when(userRepository.findByEmail(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new DataAccessResourceFailureException("DB down"));

        assertThrows(DataAccessResourceFailureException.class, () -> authService.login(req, org.mockito.Mockito.mock(HttpServletRequest.class)));
    }
}
