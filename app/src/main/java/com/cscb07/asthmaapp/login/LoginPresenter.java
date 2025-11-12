package com.cscb07.asthmaapp.login;
/**
 * Presenter for the role-selection screen.
 * Responsibility: receives button clicks from the View and decides navigation/auth flow.
 * Design principle: Encapsulation/Testability — no Android APIs; pure logic behind an interface.
 */

import com.cscb07.asthmaapp.data.IUserRepository;

public class LoginPresenter implements ILoginPresenter {

    private LoginView view;
    private final IUserRepository userRepository; // reserved for later auth integration

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

    // lifecycle-safe detach if you add tests/leaks prevention later
    public void detach() { this.view = null; }
}

