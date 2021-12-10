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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextForgotPasswordEmail;
    private Button resetPasswordButton, backButton;
    private ProgressBar progressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextForgotPasswordEmail = (EditText) findViewById(R.id.editTextForgotPasswordEmail);
        resetPasswordButton = (Button) findViewById(R.id.resetPasswordButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        backButton = (Button) findViewById(R.id.resetBackButton);

        mAuth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

    }

    //Reset the user's password
    private void resetPassword() {
        String email = editTextForgotPasswordEmail.getText().toString().trim();

        //check if fields are filled
        if (email.isEmpty()) {
            editTextForgotPasswordEmail.setError("Email is required");
            editTextForgotPasswordEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextForgotPasswordEmail.setError("Please provide a valid email");
            editTextForgotPasswordEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //reset the user's password using Firebase
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Check your email to reset your password", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Whoops, something went wrong. Try Again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}