package com.example.app_chat.activityes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.app_chat.R;
import com.example.app_chat.adapters.RecentConversionsAdapter;
import com.example.app_chat.databinding.ActivityMainBinding;
import com.example.app_chat.listeners.ConversionListener;
import com.example.app_chat.models.ChatMessage;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConversionListener{

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversation;
    private RecentConversionsAdapter recentConversionsAdapter;
    private FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
//        getToken();
        setListeners();
        listenConversations();

    }

    private void init() {
        // tạo ra 1 mảng để chứa các cuộc hội thoại của ng dùng
        conversation = new ArrayList<>();
        // khởi tạo rcv
        recentConversionsAdapter = new RecentConversionsAdapter(conversation, this);
        binding.conversionsRCV.setAdapter(recentConversionsAdapter);
        // khởi tạo FirebaseFirestore
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageCreateGroupChat.setOnClickListener(v -> createGroupChat());
//        binding.fabNewChat.setOnClickListener(v ->
//                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.bottomNav.setSelectedItemId(R.id.action_chat);
        binding.bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_chat:
                    return true;
                case R.id.action_friend:
                    startActivity(new Intent(getApplicationContext(), UsersActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                case R.id.action_user:
                    startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
            }
            return  false;
        });
    }

    private void loadUserDetails() {
//        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_COLLECTION_STATUS, "chat")
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_COLLECTION_STATUS, "chat")
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        try {
            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                    .whereEqualTo(Constants.KEY_COLLECTION_STATUS, "group")
                    .whereEqualTo(Constants.KEY_PEOPLE_ID+preferenceManager.getString(Constants.KEY_USER_ID), preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
        }catch (Exception e) {}


    }
    // khi có sự thay đổi của db, lập tức chạy đoạn code
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }
        if(value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
//                // nếu có thêm dữ liệu
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    if(documentChange.getDocument().getString(Constants.KEY_COLLECTION_STATUS).equals("chat")) {
                        chatMessage.senderId = senderId;
                        chatMessage.receiverId = receiverId;
                        // Nếu ng dùng là người gửi sẽ chạy đoạn lệnh này
                        if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                            chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                            chatMessage.receiverNickname = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NICKNAME);
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                            chatMessage.message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        }// ngược lại khi là người nhận sẽ chạy đoạn này
                        else {
                            chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                            chatMessage.receiverNickname = documentChange.getDocument().getString(Constants.KEY_SENDER_NICKNAME);
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                            chatMessage.message =  documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        }
                        if(chatMessage.conversionImage == null) {
                            chatMessage.conversionImage = Constants.IMAGE_AVATAR_DEFAULT;
                        }

                    }
                    else if(documentChange.getDocument().getString(Constants.KEY_COLLECTION_STATUS).equals("group")) {

                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_NAME_GROUP);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_IMAGE_GROUP);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_ID_GROUP);
                        if(preferenceManager.getString(Constants.KEY_USER_ID).equals(documentChange.getDocument().getString(Constants.KEY_SENDER_ID))) {
                            chatMessage.message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        }else {
                            chatMessage.message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME) + ": " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        }
                    }
                    chatMessage.statusMessage = documentChange.getDocument().getString(Constants.KEY_COLLECTION_STATUS);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.conversationId = documentChange.getDocument().getId();
                    conversation.add(chatMessage);
                } // nếu có thay đổi của dữ liệu trong 1 bản ghi nào đó
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversation.size(); i++) {

                        if(conversation.get(i).statusMessage.equals("chat")) {

                            try{
                                String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                                String receiverId = documentChange.getDocument().getString((Constants.KEY_RECEIVER_ID));
                                if(conversation.get(i).senderId.equals(senderId) && conversation.get(i).receiverId.equals(receiverId)) {
                                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                                        conversation.get(i).message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                    }else {
                                        conversation.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                    }
                                     conversation.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                                    break;
                                }
                            }catch (Exception e) {

                            }

                        }
                        if(conversation.get(i).statusMessage.equals("group")) {
                            try{
                                String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                                String id = conversation.get(i).senderId;

                                if(id.equals(senderId)) {
                                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                                        conversation.get(i).message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                    }else {
                                        conversation.get(i).message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME) + ": " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                    }
                                    conversation.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                                    break;
                                }
                            }catch (Exception e) {

                            }

                        }
                    }
                }
            }
            Collections.sort(conversation, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversionsAdapter.notifyDataSetChanged();
            binding.conversionsRCV.smoothScrollToPosition(0);
            binding.conversionsRCV.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void createGroupChat() {
        Intent intent = new Intent(getApplicationContext(), CreateGroupChatActivity.class);

        startActivity(intent);
    }
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                    preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }



    @Override
    public void onConversionCLicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        // chuyền thông tin của ng muốn nhắn tin qua activity chat
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}