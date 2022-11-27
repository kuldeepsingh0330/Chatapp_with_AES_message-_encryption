package com.ransankul.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ransankul.chatapp.Activity.viewUserImage;
import com.ransankul.chatapp.Model.AllUserModel;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.DisplayAlluserBinding;
import com.ransankul.chatapp.databinding.PopUserBinding;

import java.util.ArrayList;

public class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.MyViewHolder>{

    Context context;
    ArrayList<AllUserModel> allUserArrayList;
    public AllUserAdapter(Context context, ArrayList<AllUserModel> allUserArrayList) {
        this.context = context;
        this.allUserArrayList = allUserArrayList;
    }

    public void setFilteredArrayList(ArrayList<AllUserModel> filteredArrayList) {
        this.allUserArrayList = filteredArrayList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.display_alluser,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AllUserModel allUser = allUserArrayList.get(position);
        holder.binding.tvUserNmae.setText(allUser.getUserName());
        holder.binding.tvAbout.setText(allUser.getAbout());
        Glide.with(context).load(allUser.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.dp);
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sel_cun = allUser.getUid()+currentUserUid;


        holder.binding.ivadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = allUser.getUid();
                AllUserModel allUserModel = new AllUserModel(uid);
                FirebaseDatabase.getInstance().getReference().child("userChats")
                        .child(currentUserUid).child(sel_cun).setValue(allUserModel);

                allUserArrayList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });



        holder.binding.dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewImage(allUser.getProfileImage());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String about = holder.binding.tvAbout.getText().toString();
                String name = holder.binding.tvUserNmae.getText().toString();
                String profile = allUser.getProfileImage();

                View viewPopUp = LayoutInflater.from(context).inflate(R.layout.pop_user,null);
                PopUserBinding popUserBinding = PopUserBinding.bind(viewPopUp);
                AlertDialog popuserAlertDialog = new AlertDialog.Builder(context)
                        .setView(viewPopUp)
                        .create();
                popuserAlertDialog.show();
                popuserAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                popuserAlertDialog.setCanceledOnTouchOutside(false);

                Glide.with(context).load(profile)
                        .placeholder(R.drawable.avatar)
                        .into(popUserBinding.popdp);

                popUserBinding.poptvUserNmae.setText(name);
                popUserBinding.poptvAbout.setText(about);


                popUserBinding.popUserBAck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popuserAlertDialog.dismiss();
                    }
                });

                popUserBinding.popdp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       viewImage(profile);
                    }
                });
            }
        });
    }

    private void viewImage(String profile) {
        Intent intent = new Intent(context, viewUserImage.class);
        intent.putExtra("profileImage",profile);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return allUserArrayList.size();
    }

    public class  MyViewHolder extends RecyclerView.ViewHolder{
        DisplayAlluserBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DisplayAlluserBinding.bind(itemView);
        }
    }
}
