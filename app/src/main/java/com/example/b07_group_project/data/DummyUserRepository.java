package com.example.b07_group_project.data;



public class DummyUserRepository implements IUserRepository {
    @Override
    public void authenticateUser(String role, String username, String password, AuthCallback callback) {
        // No-op for now; teammates will replace with Firebase implementation later.
        // Can simulate success by calling callback.onSuccess("fakeUserId") if needed during tests.
    }
}

