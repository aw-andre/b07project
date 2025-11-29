package com.example.b07_group_project.model;

public class Child extends User {

    public Child(String userId, String name, String email, String password) {
        super(userId, name, email, "CHILD", password);
    }

    @Override
    public void navigateToHome() {
        System.out.println("Navigating to Child Home Screen...");
    }
}


