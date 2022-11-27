package com.ransankul.chatapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityLoginBinding;

import java.util.concurrent.TimeUnit;

public class loginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    Dialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new Dialog(loginActivity.this);
        progressDialog.setContentView(R.layout.please_wait);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        progressDialog.getWindow().getAttributes().windowAnimations = R.style.animation;


        if(mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(loginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        this.binding.btnPhoneNumber.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (loginActivity.this.binding.etPhoneNumber.getText().toString().trim().isEmpty() ||
                        loginActivity.this.binding.etPhoneNumber.getText().toString().trim().length() != 10) {
                    Toast.makeText(loginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }  else {
                    progressDialog.show();
                    loginActivity.this.otpSend();
                }
            }
        });
    }

    public void otpSend() {
        this.mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            public void onVerificationCompleted(PhoneAuthCredential credential) {
            }

            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(loginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Toast.makeText(loginActivity.this, "OTP is successfully send.", Toast.LENGTH_SHORT).show();
                String enteredNumber = loginActivity.this.binding.ccp.getSelectedCountryCodeWithPlus().toString()+" " + loginActivity.this.binding.etPhoneNumber.getText().toString().trim();
                Intent intent = new Intent(loginActivity.this, otpActivity.class);
                intent.putExtra("phoneNum",enteredNumber);
                intent.putExtra("verificationId", verificationId);
                startActivity(intent);
                progressDialog.dismiss();
            }
        };

        String phoneNumber = this.binding.ccp.getSelectedCountryCodeWithPlus()
                            + this.binding.etPhoneNumber
                            .getText().toString();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}