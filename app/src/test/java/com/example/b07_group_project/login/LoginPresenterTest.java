package com.example.b07_group_project.login;

import com.example.b07_group_project.data.IUserRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for LoginPresenter (MVP).
 * Each test validates exactly one behavior.
 */
public class LoginPresenterTest {

    @Mock
    private LoginView view;

    @Mock
    private IUserRepository userRepository;

    private LoginPresenter presenter;

    @Before
    public void setUp() {
        // Initialize @Mock fields
        MockitoAnnotations.openMocks(this);
        presenter = new LoginPresenter(view, userRepository);
    }
    //Project Handout REQUIREMENTS!!!
    // 1) Parent login: only navigateToParent is called
    @Test
    public void onParentLoginClicked_navigatesToParent() {
        presenter.onParentLoginClicked();

        verify(view).navigateToParent();
        verify(view, never()).navigateToChild();
        verify(view, never()).navigateToProvider();
        // Presenter does not touch the model yet
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(view);
    }

    // 2) Child login: only navigateToChild is called
    @Test
    public void onChildLoginClicked_navigatesToChild() {
        presenter.onChildLoginClicked();

        verify(view).navigateToChild();
        verify(view, never()).navigateToParent();
        verify(view, never()).navigateToProvider();
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(view);
    }

    // 3) Provider login: only navigateToProvider is called
    @Test
    public void onProviderLoginClicked_navigatesToProvider() {
        presenter.onProviderLoginClicked();

        verify(view).navigateToProvider();
        verify(view, never()).navigateToParent();
        verify(view, never()).navigateToChild();
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(view);
    }

    // 4) After detach, presenter must not call the view at all
    @Test
    public void detach_preventsFurtherViewCalls() {
        presenter.detach();

        presenter.onParentLoginClicked();
        presenter.onChildLoginClicked();
        presenter.onProviderLoginClicked();

        // No interactions with view or model after detach
        verifyNoInteractions(view);
        verifyNoInteractions(userRepository);
    }
}


