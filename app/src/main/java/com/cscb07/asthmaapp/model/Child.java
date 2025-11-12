package com.cscb07.asthmaapp.model;

public class Child extends User {

    public Child(String userId, String name, String email) {
        super(userId, name, email, "Child");
    }

    @Override
    public void navigateToHome() {
        System.out.println("Navigating to Child Home Screen...");
    }
}

