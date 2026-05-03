package com.quang.marketplace.modules.identity.application;

import com.quang.marketplace.modules.identity.api.AuthUserResponse;
import com.quang.marketplace.modules.identity.api.RegisterRequest;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import com.quang.marketplace.shared.error.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}