package com.example.b07_group_project.model;

public class Parent extends User {

    public Parent(String userId, String name, String email, String password) {
        super(userId, name, email, "PARENT", password);
    }

    @Override
    public void navigateToHome() {
        System.out.println("Navigating to Parent Home Screen...");
    }
}


