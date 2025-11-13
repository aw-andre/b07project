package com.example.b07_group_project.model;


public abstract class User {
    protected String userId;
    protected String name;
    protected String email;
    protected String role;

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    // Each subclass will decide how to show its home screen
    public abstract void navigateToHome();
}
