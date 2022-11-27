package com.ransankul.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ransankul.chatapp.Activity.ChatActivity;
import com.ransankul.chatapp.Activity.viewUserImage;
import com.ransankul.chatapp.Model.AllUserModel;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.DialogDeleteBinding;
import com.ransankul.chatapp.databinding.DialogUserDeleteBinding;
import com.ransankul.chatapp.databinding.DisplayUserBinding;
import com.ransankul.chatapp.encryptAnddecrypt.binary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class chatUserAdapter extends RecyclerView.Adapter<chatUserAdapter.MyViewHolder> {

   Context context;
   ArrayList<AllUserModel> chatUserArrayList;

    public chatUserAdapter(Context context, ArrayList<AllUserModel> chatUserArrayList) {
        this.context = context;
        this.chatUserArrayList = chatUserArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.display_user,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AllUserModel allUserModel = chatUserArrayList.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + allUserModel.getUid();
        String userid = allUserModel.getUid();




        FirebaseDatabase.getInstance().getReference().child("alluser").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("userName").getValue(String.class);
                String profileImage = snapshot.child("profileImage").getValue(String.class);
                holder.binding.userName.setText(userName);
                Glide.with(context).load(profileImage)
                        .placeholder(R.drawable.avatar)
                        .into(holder.binding.dp);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("name", userName);
                        intent.putExtra("dp", profileImage);
                        intent.putExtra("uid", allUserModel.getUid());
                        context.startActivity(intent);
                    }
                });

                holder.binding.dp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, viewUserImage.class);
                        intent.putExtra("profileImage", profileImage);
                        context.startActivity(intent);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        View v = LayoutInflater.from(context).inflate(R.layout.dialog_user_delete,null);
                        DialogUserDeleteBinding binding = DialogUserDeleteBinding.bind(v);
                        AlertDialog userDelete = new AlertDialog.Builder(context)
                                .setView(binding.getRoot())
                                .create();

                        userDelete.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                        userDelete.getWindow().getAttributes().windowAnimations = R.style.animation;
                        userDelete.setCanceledOnTouchOutside(false);

                        userDelete.show();
                        String rec_sen = allUserModel.getUid()+FirebaseAuth.getInstance().getUid();
                        String sen_rec = FirebaseAuth.getInstance().getUid()+allUserModel.getUid();
                        binding.tvDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseDatabase.getInstance().getReference().child("userChats")
                                        .child(FirebaseAuth.getInstance().getUid()).child(rec_sen).setValue(null);

                                FirebaseDatabase.getInstance().getReference().child("chats")
                                        .child(sen_rec).setValue(null);
                                userDelete.dismiss();

                            }
                        });

                        binding.tvCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                userDelete.dismiss();
                            }
                        });

                        return false;
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Something went wrong please try gain later", Toast.LENGTH_SHORT).show();


            }
        });
        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            String encryptionkey = snapshot.child("encryptionKey").getValue(String.class);

                            if(String.valueOf(encryptionkey).equals("1")) {
                                holder.binding.lastMsg.setText(lastMsg);
                            }else if (String.valueOf(encryptionkey).equals("2")){
                                String lastMessage = binary.decode(lastMsg);
                                holder.binding.lastMsg.setText(lastMessage);
                            }
                            else if (String.valueOf(encryptionkey).equals("3")){
                               holder.binding.lastMsg.setText("Message is encrypted click to view");
                            }

                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.lastmsgTime.setText(dateFormat.format(new Date(time)));
                        } else {
                            holder.binding.lastMsg.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Something went wrong please try gain later", Toast.LENGTH_SHORT).show();


                    }
                });
    }

    @Override
    public int getItemCount() {
        return chatUserArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        DisplayUserBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DisplayUserBinding.bind(itemView);
        }
    }
}
