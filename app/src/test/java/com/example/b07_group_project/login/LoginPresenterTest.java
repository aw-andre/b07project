package com.example.b07_group_project.login;

import com.example.b07_group_project.data.IUserRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Unit tests for LoginPresenter (MVP).
 * Each test validates one specific behavior.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock
    private LoginView view;

    @Mock
    private IUserRepository userRepository;

    private LoginPresenter presenter;

    @Before
    public void setUp() {
        presenter = new LoginPresenter(view, userRepository);
    }
    // parent
    @Test
    public void onParentLoginClicked_navigatesToParent() {
        presenter.onParentLoginClicked();

        verify(view).navigateToParent();
        verify(view, never()).navigateToChild();
        verify(view, never()).navigateToProvider();
        verifyNoInteractions(userRepository);
    }
    // child
    @Test
    public void onChildLoginClicked_navigatesToChild() {
        presenter.onChildLoginClicked();

        verify(view).navigateToChild();
        verify(view, never()).navigateToParent();
        verify(view, never()).navigateToProvider();
        verifyNoInteractions(userRepository);
    }
    // provider
    @Test
    public void onProviderLoginClicked_navigatesToProvider() {
        presenter.onProviderLoginClicked();

        verify(view).navigateToProvider();
        verify(view, never()).navigateToParent();
        verify(view, never()).navigateToChild();
        verifyNoInteractions(userRepository);
    }
    // viewing
    @Test
    public void detach_preventsFurtherViewCalls() {
        presenter.detach();

        presenter.onParentLoginClicked();
        presenter.onChildLoginClicked();
        presenter.onProviderLoginClicked();

        // After detach, presenter must not touch the view at all
        verifyNoInteractions(view);
        verifyNoInteractions(userRepository);
    }
}

