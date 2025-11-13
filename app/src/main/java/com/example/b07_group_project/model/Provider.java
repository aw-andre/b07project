package com.example.b07_group_project.model;


public class Provider extends User {

    public Provider(String userId, String name, String email) {
        super(userId, name, email, "Provider");
    }

    @Override
    public void navigateToHome() {
        System.out.println("Navigating to Provider Home Screen...");
    }
}

