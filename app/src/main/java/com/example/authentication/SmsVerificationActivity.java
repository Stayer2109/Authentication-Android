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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import utils.AndroidUtil;

public class SmsVerificationActivity extends AppCompatActivity {
    // Variables
    String phoneNumber;
    Long timeoutSecond = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    FirebaseAuth mAuth;

    ProgressBar progressBar;
    Button btnSubmitSmsOtp;
    EditText etSMSOTP;
    TextView btnSendOTPAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms_verification);

        // Mappings
        progressBar = findViewById(R.id.progressBar);
        btnSubmitSmsOtp = findViewById(R.id.btnSubmitSMS);
        etSMSOTP = findViewById(R.id.etSMSOTP);
        btnSendOTPAgain = findViewById(R.id.btnSendOTPAgain);
        mAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("phone");

        sendOtp(phoneNumber, false);

        // Onclick Events Listener
        btnSubmitSmsOtp.setOnClickListener(v -> {
            String enteredOtp = etSMSOTP.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
            signInWithPhoneAuthCredential(credential);
            setInProgress(true);
        });

        btnSendOTPAgain.setOnClickListener(v -> {
            sendOtp(phoneNumber, true);
            setInProgress(true);
        });
    }

    void sendOtp(String phoneNumber, boolean isResend) {
        startResendTimer();
        setInProgress(false);
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder().setPhoneNumber(phoneNumber).setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                setInProgress(false);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                AndroidUtil.showToast(SmsVerificationActivity.this, "OTP verification failed");
                setInProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                resendingToken = forceResendingToken;
                AndroidUtil.showToast(SmsVerificationActivity.this, "OTP sent successfully");
                setInProgress(false);
            }
        });

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        setInProgress(true);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(SmsVerificationActivity.this, UserInfoActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                } else {
                    AndroidUtil.showToast(SmsVerificationActivity.this, "OTP verification failed");
                }
            }
        });
    }

    void startResendTimer() {
        btnSendOTPAgain.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeoutSecond--;
                btnSendOTPAgain.setText("Resend OTP in " + timeoutSecond + " seconds");
                if (timeoutSecond == 0) {
                    timeoutSecond = 60L;
                    timer.cancel();
                    btnSendOTPAgain.setText("Resend OTP");

                    runOnUiThread(() -> {
                        btnSendOTPAgain.setOnClickListener(v -> {
                            btnSendOTPAgain.setEnabled(true);
                        });
                    });
                }
            }
        }, 0, 1000);
    }
}