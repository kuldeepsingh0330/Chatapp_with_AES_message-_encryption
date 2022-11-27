package com.ransankul.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ransankul.chatapp.Model.Message;
import com.ransankul.chatapp.R;
import com.ransankul.chatapp.databinding.DialogDeleteBinding;
import com.ransankul.chatapp.databinding.DialougeKeyBinding;
import com.ransankul.chatapp.databinding.MessageReceiveBinding;
import com.ransankul.chatapp.databinding.MessageSentBinding;
import com.ransankul.chatapp.encryptAnddecrypt.AES;
import com.ransankul.chatapp.encryptAnddecrypt.binary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messageArrayList;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;
//    FirebaseRemoteConfig remoteConfig;


    public MessageAdapter(Context context, ArrayList<Message> messageArrayList, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messageArrayList = messageArrayList;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.message_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);

        if(holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            if(String.valueOf(message.getEncryptionKey()).equals("1")) {
                viewHolder.binding.message.setText(message.getMessage());
            }else if(String.valueOf(message.getEncryptionKey()).equals("2")){
                String sendMsg = message.getMessage();
                String rv = binary.decode(sendMsg);
                viewHolder.binding.message.setText(rv);
            }else if(String.valueOf(message.getEncryptionKey()).equals("3")){

                viewHolder.binding.message.setText("Message is encrypted click to view");
                viewHolder.binding.message.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock,0,0,0);
                viewHolder.binding.message.setCompoundDrawablePadding(10);
            }


            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (String.valueOf(message.getEncryptionKey()).equals("3")){
                        View v = LayoutInflater.from(context).inflate(R.layout.dialouge_key, null);
                        DialougeKeyBinding binding = DialougeKeyBinding.bind(v);
                        AlertDialog keyDialog = new AlertDialog.Builder(context)
                                .setView(binding.getRoot())
                                .create();
                        keyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                        keyDialog.getWindow().getAttributes().windowAnimations = R.style.animation;
                        keyDialog.setCanceledOnTouchOutside(false);

                        keyDialog.show();
                       binding.btnKey.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               String sendMsg = message.getMessage();
                               String msgKey = binding.etKey.getText().toString();
                               AES aes = new AES();
                               try {
                                   binding.tvKey.setText(aes.AESdecrypt(msgKey, Base64.decode(sendMsg.getBytes("UTF-16LE"), Base64.DEFAULT)));
                                   binding.etKey.setVisibility(View.GONE);
                                   binding.btnKey.setVisibility(View.GONE);
                                   binding.tvmainkey.setVisibility(View.GONE);
                                   binding.tvKey.setVisibility(View.VISIBLE);
                               } catch (Exception e) {
                                   e.printStackTrace();
                                   binding.etKey.setError("Wrong Key");
                               }
                           }
                       });

                       binding.btncancel.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               keyDialog.dismiss();
                           }
                       });
                    }
                    else{
                        return;
                    }
                }
            });



            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
                    DialogDeleteBinding binding = DialogDeleteBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(binding.getRoot())
                            .create();

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });
        }else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;

            if(String.valueOf(message.getEncryptionKey()).equals("1")) {

                viewHolder.binding.message.setText(message.getMessage());
            }else if(String.valueOf(message.getEncryptionKey()).equals("2")){
                String sendMsg = message.getMessage();
                String rv = binary.decode(sendMsg);
                viewHolder.binding.message.setText(rv);
            }else if(String.valueOf(message.getEncryptionKey()).equals("3")){
                viewHolder.binding.message.setText("Message is encrypted click to view");
                viewHolder.binding.message.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.lock,0);
                viewHolder.binding.message.setCompoundDrawablePadding(10);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (String.valueOf(message.getEncryptionKey()).equals("3")){
                        View v = LayoutInflater.from(context).inflate(R.layout.dialouge_key, null);
                        DialougeKeyBinding binding = DialougeKeyBinding.bind(v);
                        AlertDialog keyDialog = new AlertDialog.Builder(context)
                                .setView(binding.getRoot())
                                .create();

                        keyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                        keyDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                        keyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                        keyDialog.setCanceledOnTouchOutside(false);

                        keyDialog.show();
                        binding.btnKey.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String sendMsg = message.getMessage();
                                String msgKey = binding.etKey.getText().toString();
                                AES aes = new AES();
                                try {
                                    binding.tvKey.setText(aes.AESdecrypt(msgKey, Base64.decode(sendMsg.getBytes("UTF-16LE"), Base64.DEFAULT)));
                                    binding.etKey.setVisibility(View.GONE);
                                    binding.btnKey.setVisibility(View.GONE);
                                    binding.tvmainkey.setVisibility(View.GONE);
                                    binding.tvKey.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    binding.etKey.setError("Wrong Key");
                                }
                            }
                        });

                        binding.btncancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                keyDialog.dismiss();
                            }
                        });
                    }
                    else{
                        return;
                    }
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
                    DialogDeleteBinding binding = DialogDeleteBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(binding.getRoot())
                            .create();;
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        MessageSentBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MessageSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        MessageReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MessageReceiveBinding.bind(itemView);
        }
    }
}
