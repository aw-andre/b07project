package com.example.b07_group_project.data;

import com.example.b07_group_project.model.User;

/**
 * Firebase storage/auth implementation.
 * This is a stub so that teammates can fill in Firebase logic later.
 * For now, all methods throw UnsupportedOperationException.
 */
public class FirebaseUserRepository implements IUserRepository {

    @Override
    public User authenticate(String email, String password, String role) {
        throw new UnsupportedOperationException("Firebase authentication not implemented yet.");
    }

    @Override
    public User getByEmail(String email) {
        throw new UnsupportedOperationException("Firebase lookup not implemented yet.");
    }

    @Override
    public void createUser(User user) {
        throw new UnsupportedOperationException("Firebase user creation not implemented yet.");
    }
}


