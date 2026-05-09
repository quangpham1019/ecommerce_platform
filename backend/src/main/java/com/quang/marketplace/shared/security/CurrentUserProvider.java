package com.quang.marketplace.shared.security;

import java.util.Optional;

public interface CurrentUserProvider {

    /**
    * Returns current authenticated user, or empty if not authenticated.
    */
    Optional<UserPrincipal> getCurrentUser();

    /**
     * Returns current authenticated user's id, or null if not authenticated.
     */
    default Long getCurrentUserId() {
        return getCurrentUser()
            .map(UserPrincipal::getId)
            .orElse(null);
    }

    
    /**
     * True when a user is authenticated in the current context.
     */
    default boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }
}
