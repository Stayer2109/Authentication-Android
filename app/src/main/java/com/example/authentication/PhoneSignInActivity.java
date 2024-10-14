package com.example.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class PhoneSignInActivity extends AppCompatActivity {
    // Variables
    String phoneNumber;
    Long timeoutSecond = 15L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    FirebaseAuth mAuth;

    ProgressBar progressBar;
    Button btnSubmitSmsOtp;
    EditText etSMSOTP;
    TextView btnSendOTPAgain, clickableEmail;
    EditText etPhoneNumber;
    Button btnSendOTP;
    Spinner spinnerCountryCode;  // New spinner variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in_phonenumber);

        // Mappings
        progressBar = findViewById(R.id.progressBar);
        btnSubmitSmsOtp = findViewById(R.id.btnSubmitSMS);
        etSMSOTP = findViewById(R.id.etSMSOTP);
        clickableEmail = findViewById(R.id.clickableEmail);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSendOTPAgain = findViewById(R.id.btnSendOTPAgain);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);  // Initialize spinner
        mAuth = FirebaseAuth.getInstance();

        btnSendOTPAgain.setEnabled(false);

        // Onclick Events Listener
        btnSendOTP.setOnClickListener(v -> {
            try {
                phoneNumber = etPhoneNumber.getText().toString();

                // Validate phone number (must be 10 digits)
                if (isValidPhoneNumber(phoneNumber)) {
                    // Concatenate country code and phone number
                    if (phoneNumber.matches("0\\d{9}")) {
                        phoneNumber = phoneNumber.substring(1);
                    }
                    String selectedCountryCode = spinnerCountryCode.getSelectedItem().toString();
                    String fullPhoneNumber = selectedCountryCode + phoneNumber;

                    // Send OTP
                    sendOtp(fullPhoneNumber, false);
                    setInProgress(true);
                } else {
                    AndroidUtil.showToast(PhoneSignInActivity.this, "Số điện thoại không hợp lệ. Vui lòng nhập đúng 10 chữ số.");
                }
            } catch (Exception e) {
                AndroidUtil.showToast(PhoneSignInActivity.this, "Something went wrong");
            }
        });

        btnSubmitSmsOtp.setOnClickListener(v -> {
            try {
                String enteredOtp = etSMSOTP.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
                signInWithPhoneAuthCredential(credential);
                setInProgress(true);
            } catch (Exception e) {
                AndroidUtil.showToast(PhoneSignInActivity.this, "Sai mã OTP");
            }
        });

        clickableEmail.setOnClickListener(v -> {
            startActivity(new Intent(PhoneSignInActivity.this, EmailSignInActivity.class));
            finish();
        });

        btnSendOTPAgain.setOnClickListener(v -> {
            // Re-validate and reformat the phone number before resending OTP
            phoneNumber = etPhoneNumber.getText().toString();

            if (isValidPhoneNumber(phoneNumber)) {
                // Concatenate country code and phone number
                if (phoneNumber.matches("0\\d{9}")) {
                    phoneNumber = phoneNumber.substring(1);  // Remove leading 0 for correct formatting
                }
                String selectedCountryCode = spinnerCountryCode.getSelectedItem().toString();
                String fullPhoneNumber = selectedCountryCode + phoneNumber;

                // Send OTP with the correct format
                sendOtp(fullPhoneNumber, true);  // Resend OTP with the properly formatted phone number
                setInProgress(true);
            } else {
                AndroidUtil.showToast(PhoneSignInActivity.this, "Số điện thoại không hợp lệ. Vui lòng nhập đúng 10 chữ số.");
            }
        });

    }

    // Validate phone number (checks if it has exactly 10 digits)
    boolean isValidPhoneNumber(@NonNull String phoneNumber) {
        return phoneNumber.matches("0\\d{9}") || phoneNumber.matches("\\d{9}");
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
                AndroidUtil.showToast(PhoneSignInActivity.this, e.getMessage());
                setInProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                resendingToken = forceResendingToken;
                AndroidUtil.showToast(PhoneSignInActivity.this, "Gửi mã OTP thành công");
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
                    Intent intent = new Intent(PhoneSignInActivity.this, UserInfoActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                } else {
                    AndroidUtil.showToast(PhoneSignInActivity.this, "Xác thực OTP thất bại");
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
                runOnUiThread(() -> btnSendOTPAgain.setText("Gửi lại mã OTP sau " + timeoutSecond + " giây"));

                if (timeoutSecond == 0) {
                    timeoutSecond = 15L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        btnSendOTPAgain.setText("Gửi lại mã OTP");
                        btnSendOTPAgain.setEnabled(true);
                    });
                }
            }
        }, 0, 1000);
    }
}
