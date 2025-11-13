package com.example.b07demosummer2024;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@Runwith(MocitoJUnitRunner.class)

public class ExampleUnitTest {
    @Mock
    MainActicity view;

    @Mock
    Model model;

    @Test
    public void testPresenter() {
        when(view.getUsername()).thenReturn("abc");
        when(model.isFound("abc")).thenReturn(true);

        Presenter presenter = new Presenter(model, view);

        presenter.checkUsername();
        
        //Check if displayMessage has been invoked 2 times
        verfiy(view, times(2)).displayMessage("user found");
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}