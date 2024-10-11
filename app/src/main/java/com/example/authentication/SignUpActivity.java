package com.example.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    // Variables
    TextView clickableSignIn;
    EditText etEmail, etPassword, etPhone;
    Button btnRegister;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Mappings
        mAuth = FirebaseAuth.getInstance();
        clickableSignIn = findViewById(R.id.clickableSignIn);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Sign In Text Click Event
        clickableSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, EmailSignInActivity.class));
            finish();
        });

        // Sign Up Button Click Event
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if (!checkInputFields(email, password)) {
                return;
            }

            // Sign Up Logic
            signUp(email, password);
        });
    }

    // check input fields
    private boolean checkInputFields(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill email and password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if email is valid
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Firebase Auth requires password to be at least 6 characters
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Use Firebase Auth to Sign Up
    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign Up Successful, Firebase's already checked duplicated email, send verification email
                        progressBar.setVisibility(ProgressBar.VISIBLE);
                        Toast.makeText(SignUpActivity.this,
                                "Sign Up Successful, Wait for sending email verification",
                                Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        progressBar.setVisibility(ProgressBar.GONE);
                                        if (verifyTask.isSuccessful()) {
                                            // Email sent, inform the user
                                            Toast.makeText(SignUpActivity.this,
                                                    "Verification email sent. " +
                                                            "Please verify before signing in.",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this,
                                                    EmailSignInActivity.class));
                                            finish();
                                        } else {
                                            // Failed to send verification email
                                            Toast.makeText(SignUpActivity.this,
                                                    "Failed to send verification email: "
                                                            + verifyTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Sign Up Failed, show error message from Firebase
                        Toast.makeText(SignUpActivity.this,
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}