package com.ransankul.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ransankul.chatapp.Adapter.AllUserAdapter;
import com.ransankul.chatapp.Model.AllUserModel;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityAllUserBinding;

import java.util.ArrayList;

public class AllUser extends AppCompatActivity {

    ActivityAllUserBinding binding;
    AllUserAdapter allUserAdapter;
    FirebaseDatabase database;
    ArrayList<AllUserModel> allUserArrayList;
    ArrayList<String> alluser;
    ArrayList<String> addeduser;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.alltoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        allUserArrayList = new ArrayList<>();
        alluser = new ArrayList<>();
        addeduser = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String  curr = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("userChats").child(curr).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addeduser.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    AllUserModel allUserModel = snap.getValue(AllUserModel.class);
                    String uid = String.valueOf(allUserModel.getUid(snap.getKey()));
                    Log.d("bjbhh",uid);
                    addeduser.add(uid);
                    Log.d("bjbhh",addeduser.get(0));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        allUserAdapter = new AllUserAdapter(this, allUserArrayList);
        binding.recy.setAdapter(allUserAdapter);
        binding.recy.showShimmerAdapter();

        allUserUidList();

        binding.allUserRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                allUserUidList();
                binding.allUserRefresh.setRefreshing(false);
            }
        });

        binding.alltoolbar.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.alltoolbar.searchView.clearFocus();
        binding.alltoolbar.searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.alltoolbar.ivapp.setVisibility(View.GONE);
                binding.alltoolbar.nameapp.setVisibility(View.GONE);
            }
        });
        binding.alltoolbar.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });

    }

    private void filterList(String Text) {
        ArrayList<AllUserModel> filteredArrayList = new ArrayList<>();
        for (AllUserModel allUserModel : allUserArrayList){
            if (allUserModel.getUserName().toLowerCase().contains(Text.toLowerCase())){
                filteredArrayList.add(allUserModel);
            }
        }
        if(filteredArrayList.isEmpty()){
            binding.tvnouser.setVisibility(View.VISIBLE);
            allUserAdapter.setFilteredArrayList(filteredArrayList);
        }else{
            binding.tvnouser.setVisibility(View.GONE);
            allUserAdapter.setFilteredArrayList(filteredArrayList);
        }



    }

    private void allUserUidList() {

        database.getReference().child("alluser").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUserArrayList.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    AllUserModel allUserModel = snap.getValue(AllUserModel.class);
                    if(!allUserModel.getUid(snap.getKey()).equals(FirebaseAuth.getInstance().getUid())){
                        allUserArrayList.add(allUserModel);
                        if(addeduser.size()>0){
                        for (String uid : addeduser) {
                            if (allUserModel.getUid(snap.getKey()).equals(uid)) {
                                allUserArrayList.remove(allUserModel);
                            }
                        }
                        }
                    }
                }
                allUserAdapter.notifyDataSetChanged();
                binding.recy.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.recy.hideShimmerAdapter();
                Toast.makeText(AllUser.this, "Something went wrong please try gain later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}