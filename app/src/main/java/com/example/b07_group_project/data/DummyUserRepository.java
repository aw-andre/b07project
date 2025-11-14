package com.example.b07_group_project.data;

import com.example.b07_group_project.model.Child;
import com.example.b07_group_project.model.Parent;
import com.example.b07_group_project.model.Provider;
import com.example.b07_group_project.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory user repository for testing.
 * This simulates a "database" using a HashMap keyed by email.
 */
public class DummyUserRepository implements IUserRepository {

    // Key: email, Value: User object
    private final Map<String, User> users = new HashMap<>();

    public DummyUserRepository() {

        // Hard-coded dummy users you can log in with:
        User parent = new Parent("p1", "Parent One", "parent@example.com", "parent123");
        User child = new Child("c1", "Child One", "child@example.com", "child123");
        User provider = new Provider("pr1", "Provider One", "provider@example.com", "provider123");

        users.put(parent.getEmail(), parent);
        users.put(child.getEmail(), child);
        users.put(provider.getEmail(), provider);
    }

    @Override
    public User authenticate(String email, String password, String role) {
        User user = users.get(email);

        if (user == null) {
            return null;  // email not found
        }
        if (!user.getPassword().equals(password)) {
            return null;  // wrong password
        }
        if (!user.getRole().equalsIgnoreCase(role)) {
            return null;  // correct user, but wrong role
        }

        return user; // success: email + password + role match
    }

    @Override
    public User getByEmail(String email) {
        return users.get(email);
    }

    @Override
    public void createUser(User user) {
        if (user != null && user.getEmail() != null) {
            users.put(user.getEmail(), user);
        }
    }
}


