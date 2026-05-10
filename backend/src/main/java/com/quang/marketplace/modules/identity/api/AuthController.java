package com.quang.marketplace.modules.identity.api;

import com.quang.marketplace.modules.identity.application.AuthService;
import com.quang.marketplace.shared.error.UnauthenticatedException;
import com.quang.marketplace.shared.security.CurrentUserProvider;
import com.quang.marketplace.shared.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(AuthService authService, CurrentUserProvider currentUserProvider) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthUserResponse login(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    @GetMapping("/me")
    public AuthUserResponse me() {

            UserPrincipal userPrincipal = currentUserProvider
                .getCurrentUser()
                .orElseThrow(() -> new UnauthenticatedException());

        return new AuthUserResponse(userPrincipal.getId(), userPrincipal.getEmail());
    }
}