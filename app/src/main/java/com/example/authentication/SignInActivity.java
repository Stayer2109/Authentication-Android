package com.example.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import utils.AndroidUtil;

public class SignInActivity extends AppCompatActivity {
    // Variables
    TextView clickableSignUp;
    EditText email, password;
    ProgressBar progressBar;
    Button btnLogin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // Mappings
        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        clickableSignUp = findViewById(R.id.clickableSignUp);
        clickableSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                AndroidUtil.showToast(getApplicationContext(), "Please enter email and password");
            } else {
                signInWithEmailPassword(emailText, passwordText);
            }
        });
    }

    private void signInWithEmailPassword(String email, String password) {
        setInProgress(true);

        // Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful, check if user is verified
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            redirectToSmsVerificationActivity(user.getPhoneNumber());
                        } else {
                            AndroidUtil.showToast(getApplicationContext(), "Please verify your email first.");
                            setInProgress(false);
                        }
                    } else {
                        AndroidUtil.showToast(getApplicationContext(), "Authentication failed. Check your credentials.");
                        setInProgress(false);
                    }
                });
    }

    private void redirectToSmsVerificationActivity(String phoneNumber) {
        Intent intent = new Intent(SignInActivity.this, SmsVerificationActivity.class);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
        finish();
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

}
