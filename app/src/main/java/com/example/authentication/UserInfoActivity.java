package com.example.authentication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserInfoActivity extends AppCompatActivity {
    // Variables
    FirebaseUser mAuth;

    TextView tvUserInfo;
    Button btnSignout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Mappings
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        btnSignout = findViewById(R.id.btnSignout);
        tvUserInfo = findViewById(R.id.tvUserInfo);

        renderUserInformation();

        // Click Events
        btnSignout.setOnClickListener(v -> SignOut());
    }

    void renderUserInformation() {
        try {
            if (mAuth.getPhoneNumber() != null) {
                tvUserInfo.setText("Phone: " + mAuth.getPhoneNumber());
            } else {
                tvUserInfo.setText("Email: " + mAuth.getEmail());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error rendering user information");
        }
    }

    private void SignOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }
}