package com.quang.marketplace.modules.identity.application;

import com.quang.marketplace.modules.identity.api.AuthUserResponse;
import com.quang.marketplace.modules.identity.api.RegisterRequest;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import com.quang.marketplace.shared.error.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //TODO: use updateProfile method to allow users to update their profile information (first name, last name, etc.)
    public AuthUserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(normalizedEmail, passwordHash);
        User saved = userRepository.save(user);

        return new AuthUserResponse(saved.getId(), saved.getEmail());
    }

    public AuthUserResponse login(RegisterRequest request, HttpServletRequest httpRequest) {
        String normalizedEmail = request.email().trim().toLowerCase();

        var userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            throw new BusinessRuleException("Invalid credentials");
        }

        var user = userOpt.get();
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessRuleException("Invalid credentials");
        }

        // Establish a session-based Authentication where principal is UserPrincipal.
        var principal = new com.quang.marketplace.shared.security.UserPrincipal(user.getId(), user.getEmail());
        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Persist security context to the HTTP session so subsequent requests are authenticated
        httpRequest.getSession(true).setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        );

        return new AuthUserResponse(user.getId(), user.getEmail());
    }
}