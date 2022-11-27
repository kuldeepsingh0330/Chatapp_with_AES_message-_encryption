package com.ransankul.chatapp.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.ransankul.chatapp.Model.AllUserModel;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityProfileBinding;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;


public class profile extends AppCompatActivity {

    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new Dialog(profile.this);
        progressDialog.setContentView(R.layout.please_wait);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        progressDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        FirebaseDatabase.getInstance().getReference().child("alluser")
                .child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                        String name = snapshot.child("userName").getValue(String.class);
                        binding.tvUserNmae.setText(name);

                        String about = snapshot.child("about").getValue(String.class);
                        binding.about.setText(about);

                        String pic = snapshot.child("profileImage").getValue(String.class);
                        Glide.with(profile.this).load(pic).placeholder(R.drawable.avatar).into(binding.profileImage);
                    }

                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(profile.this, "Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                startActivityForResult(intent, 5);

                ImagePicker.Companion.with(profile.this)
                        .crop()
                        .cropOval()
                        .cropSquare()
                        .provider(ImageProvider.BOTH)
                        .createIntentFromDialog((Function1)(new Function1(){
                            public Object invoke(Object var1){
                                this.invoke((Intent)var1);
                                return Unit.INSTANCE;
                            }

                            public final void invoke(@NotNull Intent it){
                                Intrinsics.checkNotNullParameter(it,"it");
                                launcher.launch(it);
                            }
                        }));


            }
        });

        binding.btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.tvUserNmae.getText().toString();
                String about = binding.about.getText().toString();

                if(name.isEmpty()) {
                    binding.tvUserNmae.setError("Please type a name");
                    return;
                }else if(about.isEmpty()) {
                    binding.about.setError("Please type here something");
                    return;
                }

                progressDialog.show();
                if(selectedImage != null) {
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();


                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.tvUserNmae.getText().toString();

                                        AllUserModel allUser = new AllUserModel(name,about,imageUrl,uid,phone);

                                        database.getReference()
                                                .child("alluser")
                                                .child(uid)
                                                .setValue(allUser)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(profile.this, "updated Succesfully", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(profile.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                } else {
                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();

                    AllUserModel allUser = new AllUserModel(name,about,"No Image",uid,phone);

                    database.getReference()
                            .child("alluser")
                            .child(uid)
                            .setValue(allUser)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(profile.this, "updated Succesfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(profile.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }

            }
        });
    }


    ActivityResultLauncher<Intent> launcher=
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                if(result.getResultCode()==RESULT_OK){
                    Uri uri=result.getData().getData();
                    binding.profileImage.setImageURI(uri);
                    selectedImage = uri;
                    // Use the uri to load the image
                }else if(result.getResultCode()==ImagePicker.RESULT_ERROR){
                    // Use ImagePicker.Companion.getError(result.getData()) to show an error
                }
            });
}