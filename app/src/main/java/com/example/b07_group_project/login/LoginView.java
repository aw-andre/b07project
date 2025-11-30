package com.example.b07_group_project.login;

/**
 * View contract for the role-selection screen.
 * Responsibility: passive UI — only renders and navigates as instructed by the Presenter.
 * Design principle: Passivity/Decoupling — no business logic here.
 */

public interface LoginView {
    void navigateToParent();
    void navigateToChild();
    void navigateToProvider();
    void showError(String message);
}


