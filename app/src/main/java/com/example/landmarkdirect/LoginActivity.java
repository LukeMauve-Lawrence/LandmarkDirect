package com.example.landmarkdirect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //variables
    private TextView registerButton, forgotPasswordButton;
    private EditText editTextLoginEmail, editTextLoginPassword;
    private Button loginButton;

    //Firebase
    private FirebaseAuth mAuth;

    //Progress bar for loading
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //onclick listeners for all buttons
        editTextLoginEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextLoginPassword);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        registerButton = (TextView) findViewById(R.id.textViewRegister);
        registerButton.setOnClickListener(this);

        forgotPasswordButton = (TextView) findViewById(R.id.textViewForgotPassword);
        forgotPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textViewRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                break;

            case R.id.loginButton:
                userLogin();
                break;
            case R.id.textViewForgotPassword:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                break;
        }
    }

    //try to log the user in
    private void userLogin() {
        String email = editTextLoginEmail.getText().toString().trim();
        String password = editTextLoginPassword.getText().toString().trim();

        //check if fields are filled
        if (email.isEmpty()) {
            editTextLoginEmail.setError("Email is required");
            editTextLoginEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextLoginEmail.setError("Please enter a valid email");
            editTextLoginEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextLoginPassword.setError("Min password length is 6 characters");
            editTextLoginPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //test login credentials, if correct, log user in and redirect them to the home page
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Incorrect credentials", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}