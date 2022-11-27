package com.ransankul.chatapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityOtpBinding;

public class otpActivity extends AppCompatActivity {

    ActivityOtpBinding binding;
    public String verificationId;
    public Dialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        String  phoneNum = getIntent().getStringExtra("phoneNum");
        progressDialog = new Dialog(otpActivity.this);
        progressDialog.setContentView(R.layout.please_wait);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        progressDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        binding.tvNumber.setText("ChatApp send 6-digit OTP code on your phone number  "
                +  phoneNum  +"  . Enter below to verify your number");

        verificationId = getIntent().getStringExtra("verificationId");

        this.binding.btnverify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                progressDialog.show();
                if (otpActivity.this.binding.etotp.getText().toString().isEmpty()) {
                    Toast.makeText(otpActivity.this, "OTP is not Valid!", Toast.LENGTH_SHORT).show();
                } else if (otpActivity.this.verificationId != null) {
                    FirebaseAuth.getInstance().signInWithCredential(PhoneAuthProvider
                                    .getCredential(otpActivity.this.verificationId,
                                            otpActivity.this.binding.etotp.getText().toString().trim()) )
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                public void onComplete(Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(otpActivity.this, "Welcome...", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(otpActivity.this, profile.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        otpActivity.this.startActivity(intent);
                                        progressDialog.dismiss();
                                        return;
                                    }
                                    progressDialog.dismiss();
                                    Toast.makeText(otpActivity.this, "OTP is not Valid!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


    }
}