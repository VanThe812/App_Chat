package com.example.app_chat.activityes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.app_chat.R;
import com.example.app_chat.adapters.ChatAdapter;
import com.example.app_chat.databinding.ActivityChatBinding;
import com.example.app_chat.models.ChatMessage;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        init();
        setListeners();
        listenMessage();

    }
    // tải thông tin cơ bản của người nhận tin nhắn và đưa và obj user
    private void  loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        if(receiverUser.receiverNickname != null) {
            binding.textName.setText(receiverUser.receiverNickname);
        }else {
            binding.textName.setText(receiverUser.name);
        }
        byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);

    }
    private void init() {

        binding.imageMore.setVisibility(View.INVISIBLE);
        constraintLayout = findViewById(R.id.layout_chat);
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRCV.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if(documentSnapshot.getString(Constants.KEY_BACKGROUND) != "" && documentSnapshot.getString(Constants.KEY_BACKGROUND) != null) {
                                byte[] bytes = Base64.decode(documentSnapshot.getString(Constants.KEY_BACKGROUND), Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                binding.imageBackground.setImageBitmap(bitmap);
                            }
                        }
                    }
                });
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if(documentSnapshot.getString(Constants.KEY_BACKGROUND) != "") {
                                byte[] bytes = Base64.decode(documentSnapshot.getString(Constants.KEY_BACKGROUND), Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                binding.imageBackground.setImageBitmap(bitmap);
                            }
                        }
                    }
                });
    }
    private void setListeners() {
        binding.imageback.setOnClickListener(v -> onBackPressed());
        binding.lauoutSend.setOnClickListener(v -> {
            if(!binding.inputMessage.getText().toString().trim().isEmpty()) {
                sendMessage();
            }
        });

        binding.imageInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
            User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        });
    }
    private void listenMessage() {
        if(receiverUser.Check == true) {
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_ID_GROUP, receiverUser.id)
                    .addSnapshotListener(eventListener);
        }else {
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                    .addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
        }

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        Toast.makeText(this, ""+receiverUser.Check, Toast.LENGTH_SHORT).show();
        if(error != null) {
            return;
        }
        if(value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    if(receiverUser.Check == true) {
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
//                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_ID_GROUP);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.datatime = getReadableDateTIme(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    }else {
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.datatime = getReadableDateTIme(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    }

                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0) {
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRCV.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRCV.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null) {
            checkForConversion();
        }
    };
    private void checkForConversion() {
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    private void sendMessage() {
        //thêm vào db chat
        HashMap<String, Object> message = new HashMap<>();
        if(receiverUser.Check == true) {
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_ID_GROUP, receiverUser.id);
        }else {
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        }
        
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        //
        if(receiverUser.Check == true) {
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            if(conversionId != null){
                updateConversion(binding.inputMessage.getText().toString());
            }
            else {
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_SENDER_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                conversion.put(Constants.KEY_BACKGROUND, "");
                conversion.put(Constants.KEY_SENDER_NICKNAME , null);
                conversion.put(Constants.KEY_RECEIVER_NICKNAME, null);
                conversion.put(Constants.KEY_COLLECTION_STATUS, "chat");
                addConversion(conversion);
            }
        }
        binding.inputMessage.setText(null);
    }
    private void updateConversion(String message) {
        if (receiverUser.Check == false) {
            DocumentReference documentReference =
                    database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversionId);
            documentReference.update(
                    Constants.KEY_LAST_MESSAGE, message,
                    Constants.KEY_TIMESTAMP, new Date(),
                    Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID),
                    Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME),
                    Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE),
                    Constants.KEY_RECEIVER_ID, receiverUser.id,
                    Constants.KEY_RECEIVER_NAME, receiverUser.name,
                    Constants.KEY_RECEIVER_IMAGE, receiverUser.image,
                    Constants.KEY_SENDER_NICKNAME, null,
                    Constants.KEY_RECEIVER_NICKNAME, null
            );
        }else if(receiverUser.Check == true) {

            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                    .whereEqualTo(Constants.KEY_ID_GROUP, receiverUser.id)
                    .addSnapshotListener((value, error) -> {
                        if(error != null) {
                            return;
                        }
                        if(value != null) {
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                                    DocumentReference documentReference =
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(documentChange.getDocument().getId());
                                    documentReference.update(
                                            Constants.KEY_LAST_MESSAGE, message,
                                            Constants.KEY_TIMESTAMP, new Date(),
                                            Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)
                                    );
                                }
                            }
                        }
                    });
        }

    }


    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());

    }

    // ------------------------------------------------------
    private String getReadableDateTIme(Date date) {
        return new SimpleDateFormat("MMMM dd, YYYY - hh:mm a", Locale.getDefault()).format(date);
    }
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //        binding.imageMore.setOnClickListener(view -> {
//            binding.imageMore.setVisibility(View.INVISIBLE);
//            binding.imageCamera.setVisibility(View.VISIBLE);
//            binding.imageImage.setVisibility(View.VISIBLE);
//
//            ConstraintSet constraintSet = new ConstraintSet();
//            constraintSet.clone(constraintLayout);
//            constraintSet.connect(
//                    R.id.inputMessage,
//                    ConstraintSet.START,
//                    R.id.imageImage,
//                    ConstraintSet.END
//            );
//            constraintSet.applyTo(constraintLayout);
//        });
//        binding.inputMessage.setOnClickListener(view -> {
//            if(binding.imageMore.getVisibility() != View.VISIBLE) {
//                binding.imageMore.setVisibility(View.VISIBLE);
//                binding.imageCamera.setVisibility(View.INVISIBLE);
//                binding.imageImage.setVisibility(View.INVISIBLE);
//
//                ConstraintSet constraintSet = new ConstraintSet();
//                constraintSet.clone(constraintLayout);
//                constraintSet.connect(
//                        binding.inputMessage.getId(),
//                        ConstraintSet.START,
//                        binding.imageMore.getId(),
//                        ConstraintSet.END
//                );
//                constraintSet.applyTo(constraintLayout);
//            }
//        });

}