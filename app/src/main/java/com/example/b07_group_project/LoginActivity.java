package com.example.b07_group_project;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.b07_group_project.data.DummyUserRepository;
import com.example.b07_group_project.data.IUserRepository;
import com.example.b07_group_project.login.ILoginPresenter;
import com.example.b07_group_project.login.LoginPresenter;
import com.example.b07_group_project.login.LoginView;
/**
 * Role-selection Activity (View).
 * Responsibility: wires UI and delegates all decisions to the Presenter.
 * Design principle: Passivity — no branching logic, only calls Presenter methods.
 */

public class LoginActivity extends AppCompatActivity implements LoginView {

    private ILoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        IUserRepository repo = new DummyUserRepository();
        presenter = new LoginPresenter(this, repo);

        Button parentBtn = findViewById(R.id.btnParent);
        Button childBtn = findViewById(R.id.btnChild);
        Button providerBtn = findViewById(R.id.btnProvider);

        parentBtn.setOnClickListener(v -> presenter.onParentLoginClicked());
        childBtn.setOnClickListener(v -> presenter.onChildLoginClicked());
        providerBtn.setOnClickListener(v -> presenter.onProviderLoginClicked());
    }

    @Override
    public void navigateToParent() {
        startActivity(new Intent(this, ParentLoginActivity.class));
    }

    @Override
    public void navigateToChild() {
        startActivity(new Intent(this, ChildLoginActivity.class));
    }

    @Override
    public void navigateToProvider() {
        startActivity(new Intent(this, ProviderLoginActivity.class));
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If you add a detach method to the presenter, call it here.
        // ((LoginPresenter) presenter).detach();
    }
}



