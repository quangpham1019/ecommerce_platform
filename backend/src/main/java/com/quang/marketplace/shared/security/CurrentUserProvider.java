package com.quang.marketplace.shared.security;

public interface CurrentUserProvider {

    /**
     * Returns current authenticated user's id, or null if not authenticated.
     */
    Long getCurrentUserId();

    /**
     * True when a user is authenticated in the current context.
     */
    boolean isAuthenticated();
}
