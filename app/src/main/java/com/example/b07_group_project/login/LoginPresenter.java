package com.example.b07_group_project.login;

/**
 * Presenter for the role-selection screen.
 * Responsibility: receives button clicks from the View and decides navigation/auth flow.
 * Design principle: Encapsulation/Testability — no Android APIs; pure logic behind an interface.
 */

import com.example.b07_group_project.data.IUserRepository;
import com.example.b07_group_project.data.DummyUserRepository;
import com.example.b07_group_project.model.User;

public class LoginPresenter implements ILoginPresenter {

    private LoginView view;
    private final IUserRepository userRepository; // not used yet, but OK

    public LoginPresenter(LoginView view, IUserRepository repo) {
        this.view = view;
        this.userRepository = repo;
    }

    @Override
    public void onParentLoginClicked() {
        if (view != null) view.navigateToParent();
    }

    @Override
    public void onChildLoginClicked() {
        if (view != null) view.navigateToChild();
    }

    @Override
    public void onProviderLoginClicked() {
        if (view != null) view.navigateToProvider();
    }

    public void detach() { this.view = null; }
}


