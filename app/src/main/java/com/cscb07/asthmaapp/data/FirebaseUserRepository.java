package com.cscb07.asthmaapp.data;

/**
 * Placeholder implementation of IUserRepository for teammates.
 * Responsibility: handle actual Firebase Authentication and user retrieval.
 * Design principle: Abstraction — keeps Firebase details hidden from Presenter.
 */
public class FirebaseUserRepository implements IUserRepository {

    @Override
    public void authenticateUser(String role,
                                 String username,
                                 String password,
                                 AuthCallback callback) {
        // TODO: To be implemented by Firebase team.
        throw new UnsupportedOperationException(
                "Firebase logic to be added by teammates.");
    }
}

