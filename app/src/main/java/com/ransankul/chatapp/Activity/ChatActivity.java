package com.ransankul.chatapp.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ransankul.chatapp.Adapter.MessageAdapter;
import com.ransankul.chatapp.Model.Message;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.ActivityChatBinding;
import com.ransankul.chatapp.encryptAnddecrypt.AES;
import com.ransankul.chatapp.encryptAnddecrypt.binary;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    private RelativeLayout layoutMain;
    private RelativeLayout layoutButtons;
    private RelativeLayout layoutContent;

    MessageAdapter adapter;
    ArrayList<Message> messages;
    RecyclerView recyclerView;

    String senderUid;
    String receiverUid;
    String token;
    String name;

    private boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;
        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("dp");

        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new MessageAdapter(this, messages, senderRoom, receiverRoom);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final EmojiPopup popup = EmojiPopup.Builder
                .fromRootView(findViewById(R.id.parentlayout)).build(binding.messageBox);

        binding.ivEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.toggle();
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();
                binding.binary.setVisibility(View.GONE);
                binding.binaryselect.setVisibility(View.GONE);
                binding.aesSelect.setVisibility(View.GONE);
                binding.tvLayout.setVisibility(View.GONE);
                binding.sendBtn.setVisibility(View.VISIBLE);
                binding.cut.setVisibility(View.GONE);
                binding.keyBox.setVisibility(View.GONE);
                binding.aes.setVisibility(View.GONE);

                if(messageTxt.isEmpty()){
                    Toast.makeText(ChatActivity.this, "please type a message", Toast.LENGTH_SHORT).show();
                    return;
                }

                String encryptionKey ="1";
                Date date = new Date();
                Message message = new Message(messageTxt, senderUid, encryptionKey, date.getTime());
                binding.messageBox.setText("");


                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                lastMsgObj.put("encryptionKey",encryptionKey);

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message);
                            }
                        });

            }
        });

        binding.binary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = binding.messageBox.getText().toString();

                if(temp.isEmpty()){
                    Toast.makeText(ChatActivity.this, "please type a message", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(temp.length() >= 100){
//                  Toast.makeText(ChatActivity.this,"Message length is not more than 100 in Binary Encryption",Toast.LENGTH_SHORT).show();
                    binding.messageBox.setError("Message length is less than 100");
                    return;
                }

                 String rv = binary.encode(temp);


                String encryptionKey ="2";
                Date date = new Date();
                Message message = new Message(rv, senderUid, encryptionKey, date.getTime());
                binding.messageBox.setText("");

                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                lastMsgObj.put("encryptionKey",encryptionKey);

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message);
                            }
                        });
            }
        });

        binding.binaryselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.binaryselect.setVisibility(View.GONE);
                binding.binary.setVisibility(View.VISIBLE);
                binding.tvLayout.setVisibility(View.GONE);
                binding.cut.setVisibility(View.GONE);
                binding.aesSelect.setVisibility(View.GONE);
                binding.aes.setVisibility(View.GONE);
                binding.sendBtn.setVisibility(View.GONE);
                binding.keyBox.setVisibility(View.GONE);
            }
        });

        binding.aes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = binding.messageBox.getText().toString();
                String key = binding.keyBox.getText().toString();

                if(temp.isEmpty()){
                    Toast.makeText(ChatActivity.this, "please type a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(key.isEmpty()){
                    Toast.makeText(ChatActivity.this, "please type a key", Toast.LENGTH_SHORT).show();
                    return;
                }

                AES aes = new AES();
                String enc = "" ;
                try {
                     enc = aes.AESencrypt(key.getBytes("UTF-16LE"), temp.getBytes("UTF-16LE"));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                String encryptionKey ="3";
                Date date = new Date();
                Message message = new Message(enc, senderUid, encryptionKey, date.getTime());
                binding.messageBox.setText("");
                binding.keyBox.setText("");

                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                lastMsgObj.put("encryptionKey",encryptionKey);

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message);
                            }
                        });
            }
        });

        binding.aesSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.aesSelect.setVisibility(View.GONE);
                binding.tvLayout.setVisibility(View.GONE);
                binding.cut.setVisibility(View.GONE);
                binding.binaryselect.setVisibility(View.GONE);
                binding.aes.setVisibility(View.VISIBLE);
                binding.binary.setVisibility(View.GONE);
                binding.sendBtn.setVisibility(View.GONE);
                binding.keyBox.setVisibility(View.VISIBLE);
            }
        });

        binding.sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                binding.sendBtn.setVisibility(view.GONE);
                binding.binaryselect.setVisibility(View.VISIBLE);
                binding.aesSelect.setVisibility(View.VISIBLE);
                binding.tvLayout.setVisibility(View.VISIBLE);
                binding.cut.setVisibility(View.VISIBLE);
                binding.keyBox.setVisibility(View.GONE);

                return true;
            }
        });
        binding.binary.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                binding.binary.setVisibility(view.GONE);
                binding.aes.setVisibility(view.GONE);
                binding.binaryselect.setVisibility(View.VISIBLE);
                binding.aesSelect.setVisibility(View.VISIBLE);
                binding.tvLayout.setVisibility(View.VISIBLE);
                binding.sendBtn.setVisibility(View.VISIBLE);
                binding.cut.setVisibility(View.GONE);
                binding.keyBox.setVisibility(View.GONE);

                return true;
            }
        });
        binding.aes.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                binding.aes.setVisibility(view.GONE);
                binding.binaryselect.setVisibility(View.VISIBLE);
                binding.aesSelect.setVisibility(View.VISIBLE);
                binding.tvLayout.setVisibility(View.VISIBLE);
                binding.sendBtn.setVisibility(View.VISIBLE);
                binding.cut.setVisibility(View.GONE);
                binding.binary.setVisibility(View.GONE);
                binding.keyBox.setVisibility(View.GONE);


                return true;
            }
        });

        binding.cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.cut.setVisibility(View.GONE);
                binding.binaryselect.setVisibility(View.GONE);
                binding.aesSelect.setVisibility(View.GONE);
                binding.tvLayout.setVisibility(View.GONE);
                binding.sendBtn.setVisibility(view.VISIBLE);
            }
        });



        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tvLayout.setVisibility(View.GONE);
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
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


}