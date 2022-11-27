package com.ransankul.chatapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ransankul.chatapp.Adapter.chatUserAdapter;
import com.ransankul.chatapp.Model.AllUserModel;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    RecyclerView recyclerView;
    chatUserAdapter chatUserAdapter;
    FirebaseDatabase database;
    ArrayList<AllUserModel> uidArrayList;
    FirebaseAuth auth;
    String curr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");



        recyclerView = findViewById(R.id.recyclerViewMain);
        uidArrayList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        chatUserAdapter = new chatUserAdapter(this, uidArrayList);
        recyclerView.setAdapter(chatUserAdapter);
        binding.recyclerViewMain.showShimmerAdapter();

        curr = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userarray();

        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                userarray();
                binding.refresh.setRefreshing(false);
            }
        });

        binding.alluserfloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AllUser.class));
            }
        });
    }

    private void userarray() {
        database.getReference().child("userChats").child(curr).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uidArrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AllUserModel allUserModel = snap.getValue(AllUserModel.class);
                        allUserModel.getUid(snap.getKey());
                        uidArrayList.add(allUserModel);
                    }
                }
                int size = uidArrayList.size();
                if(size == 0){
                    binding.startChat.setVisibility(View.VISIBLE);
                }else {
                    binding.startChat.setVisibility(View.GONE);
                }
                chatUserAdapter.notifyDataSetChanged();
                binding.recyclerViewMain.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                binding.recyclerViewMain.hideShimmerAdapter();
                Toast.makeText(MainActivity.this, "Something went wrong please try gain later", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.man_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateProfile:
                startActivity(new Intent(MainActivity.this, profile.class));
                break;
        }
        return true;
    }
}