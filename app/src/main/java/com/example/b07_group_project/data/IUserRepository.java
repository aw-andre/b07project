package com.example.b07_group_project.data;

/**
 * Database/Auth contract (Model boundary).
 * Responsibility: define how the Presenter requests authentication.
 * Design principle: Abstraction (Information hiding) — hides Firebase or any storage details.
 */

public interface IUserRepository {

    // Used by Firebase implementation later to notify success/failure
    interface AuthCallback {
        void onSuccess(String userId);
        void onError(String message);
    }

    // Contract: authenticate a user by role, username, and password
    void authenticateUser(String role, String username, String password, AuthCallback callback);
}
