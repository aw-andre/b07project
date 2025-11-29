package com.example.b07_group_project.login;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class LoginPresenterTest {

    private LoginView mockView;
    private LoginPresenter presenter;

    @Before
    public void setUp() {
        mockView = mock(LoginView.class);
        presenter = new LoginPresenter(mockView, null);
    }

    @Test
    public void parentClick_navigatesToParent() {
        presenter.onParentLoginClicked();
        verify(mockView).navigateToParent();
    }

    @Test
    public void childClick_navigatesToChild() {
        presenter.onChildLoginClicked();
        verify(mockView).navigateToChild();
    }

    @Test
    public void providerClick_navigatesToProvider() {
        presenter.onProviderLoginClicked();
        verify(mockView).navigateToProvider();
    }
}

