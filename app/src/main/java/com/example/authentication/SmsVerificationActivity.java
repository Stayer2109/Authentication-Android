package com.example.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SmsVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms_verification);

        Button btnSubmitSmsOtp = findViewById(R.id.btnSubmitSMS);
        btnSubmitSmsOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sau khi xác nhận OTP SMS, chuyển đến trang đăng nhập
                Intent intent = new Intent(SmsVerificationActivity.this, UserInfoActivity.class);
                startActivity(intent);
            }
        });
    }
}