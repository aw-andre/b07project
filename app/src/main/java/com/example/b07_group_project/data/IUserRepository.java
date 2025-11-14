package com.example.b07_group_project.data;

import com.example.b07_group_project.model.User;

/**
 * Database/Auth contract (Model boundary).
 * Responsibility: define how the Presenter or Activities request authentication
 * and user lookup, without exposing Firebase or storage details.
 * Design principle: Abstraction (Information hiding).
 */
public interface IUserRepository {
    /**
     * Authenticate a user by email, password, and expected role.
     *
     * @param email    user email (unique key)
     * @param password plain-text password (for dummy implementation)
     * @param role     expected role: "PARENT", "CHILD", "PROVIDER"
     * @return User if credentials are valid and role matches; null otherwise.
     */
    User authenticate(String email, String password, String role);

    /**
     * Find a user by email only.
     */
    User getByEmail(String email);

    /**
     * Create/persist a new user.
     * DummyUserRepository will just keep it in memory.
     */
    void createUser(User user);
}

