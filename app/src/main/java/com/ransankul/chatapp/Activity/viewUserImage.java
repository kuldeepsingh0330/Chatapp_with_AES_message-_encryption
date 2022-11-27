package com.ransankul.chatapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityViewUserImageBinding;

public class viewUserImage extends AppCompatActivity {

    ActivityViewUserImageBinding activityViewUserImageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewUserImageBinding = ActivityViewUserImageBinding.inflate(getLayoutInflater());
        setContentView(activityViewUserImageBinding.getRoot());

        String profileImage = getIntent().getStringExtra("profileImage");

        Glide.with(viewUserImage.this).load(profileImage)
                .placeholder(R.drawable.avatar)
                .into(activityViewUserImageBinding.profileImage);

        activityViewUserImageBinding.popUserBAck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}