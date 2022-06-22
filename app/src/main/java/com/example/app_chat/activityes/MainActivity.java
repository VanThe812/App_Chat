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
        binding.imageSignOut.setOnClickListener(v -> signOut());
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
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
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
                        String image_default = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0o" +
                                "OjM9PDkzODdASFxOQERXRTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2P/2wBDARESEhgVGC8aGi9jQjhCY2NjY2Nj" +
                                "Y2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2P/wAARCACWAJYDASIAAhEBAxEB" +
                                "/8QAGwABAAIDAQEAAAAAAAAAAAAAAAUGAgQHAwH/xABBEAABAwIDBAUJBAgHAAAAAAABAAIDBBEFITEGEkFR" +
                                "EyJhcZEUFSMygaGxwdEkUlOiFjM1QmJyc7JDVIKSk8Lx/8QAFAEBAAAAAAAAAAAAAAAAAAAAAP/EABQRAQAA" +
                                "AAAAAAAAAAAAAAAAAAD/2gAMAwEAAhEDEQA/AOfoiICIiAiLJjHSPaxjS5zjYNaLklBiin6HZOvqQ187mUzD" +
                                "wdm7w+pCmqfZDD4y0zPmmIGYLt1p8M/egoyLpMOB4XALMooj/ON/43Xr5qw7/IUv/C36IOYoujTbPYVMbuo2" +
                                "NP8AAS34FRlRsZSub9mqZY3X/fAcPkgpiKVxDZ3EKBhe6MSxgXL4jvAd41UUgIiICIiAiIgIiICIrJs3s8ao" +
                                "trK1loBmyMj9Z2ns+KDTwbZ6pxO0rvQ01/1jhm7+Uce9XbD8MpMOj3aWENJFnPObnd5W01rWMDWNDWtFgALA" +
                                "BfUBERAREQEREBROK7P0eJBzw0Q1B/xWDXvHH4qWRBzLEsMqsMm6OpZYH1Xtza7uK011OqpYKyAw1MTZGHgR" +
                                "p2jkVz/G8Gmwmexu+Bx6kltew8igjEREBERARFkxjpHtYxpc5xsABmSgltnMI851u9KD5NFm/wDiPBq6A1rW" +
                                "MDWNDWtFgALABauFULMOw+KmbYlou9w/ecdSttAREQEXnPMyCMvee4cSoeoq5Kgm5szg0IJV9ZTsNnSi/Zn8" +
                                "FgMQp7+uR/pKhkQT0VRDNlHICeWhXqq4t2lr3xndlJeznxCCWRfGuDmhzSCDoQvqAteuooa+lfT1DbsdoeLT" +
                                "zHathEHLq+jloKySmmHWYcj94cCtdXna/DvKaAVcbfS0+ZsMyzj4a+KoyAiIgKf2PohU4oZ3tuynbvDlvHIf" +
                                "M+xQCvOxcHR4Q+UtAMspIPMCw+N0FgREQE0ReFc/cpJDxIt4oIusqDUTEg9QZNC10RAREQEREEhhlRZ3QuOR" +
                                "9XsKk1XmOLHteNWm6sIIIBGhQEREHxzWvYWvAc1wsQdCFzHEqU0WI1FMQQI3kNvru8D4WXT1SdtoNzE4Zg0B" +
                                "ssVr8yDn7iEFcREQF0nAIRBgdGwcYw//AHZ/Nc2XTsJ/ZFF/QZ/aEG2iIgLTxQE0otwcLrcXlVR9LTSMGtri" +
                                "3NBAoiICIiAiIgKwQAiCMHUNHwUHBH0szGZ9Y525KfQEREBVnbiEOoqabiyQs8Rf/qrMq9tt+yIv64/tcgo6" +
                                "IiAujbNymbAaVx1DS3wJHyXOVc9iKhrqGop895km/wCwi3yPigsyIiAiIgicQpjFJ0jR1He4rSVic0PaWuAI" +
                                "OoKiquhERLo3t3fuudYoNJERARfWguIAtnzNlJ0dAxtpJHNeeABuEH3DaYxt6V46ztByC3kRAREQFVduZSIa" +
                                "SEaOc5x9gAHxKtSoe2NQ2bGujbf0MbWHlfM/MIIJERAUrs1XNoMXjdIQI5R0bieF7WPiAopEHWUUTs5igxLD" +
                                "mh7r1EIDZL8eR9vxupR72xsL3mzRqUGS1KiviiyZ6R3YcvFadXXPmJay7We8rTQbMtbPLfr7o5NyWuviICIi" +
                                "Asmuc03aSDzBWKINyHEZmZPtIO3IqQp6qKcdV1nfdOqg19BIIINiOIQWJFHUeIaRznuf9VIoPKqqI6SmkqJT" +
                                "ZkbS4/RcvqZnVNTLO+wdI8vNuZN1adssUBDcOhdncOmt7h8/BVJAREQEREG5hWIyYZXMqI8wMnt+83iFb5cQ" +
                                "GIMZJGfQkXaPr2qiLcw7EH0UvF0Tj1m/MILUi86eojqYhJE7eafEHkV6ICIiAiIgIiICIiAsqnHDhlE4OG/I" +
                                "RaIHn29gWnXYhFRM6/WkOjAc/wDxVioqJKmYyyuu4+A7EGEkj5ZXSSOLnvJc4niSsURAREQEUhgNDFiWM0tH" +
                                "O57Y5XWcWEAjI6XW8/ZoNpqENr4zW1jYpG0/RPs1kjt1ri8AjUi97e3K4QKK1R7HwSSSyMxb7FDHI6Sc0zml" +
                                "royN5u4Tcix1F9CFi7Y9kVVJTVGJtjl8oNPCBA5wkdusc25Hq3D876W4oK5TVMtLIHwvLTxHA94U7TY5TyN9" +
                                "PeJw7CQV7foduU5ZLiDW4iKU1XkjYiRu3sOve2th9Rmvak2OpqmrqKaDEXVEkTJGG0D42smbazS4ggi+ts0H" +
                                "n51ovxvyu+iedaL8b8rvotWo2dhjw7EaqmxEzuoZNx0XQFrtWgkgm7cy7gfV4aDKn2c6fAG4iK5jZXwyztpz" +
                                "Gc2xus7rX5W4cfag2POtF+N+V30X1uKUTnACcXJtm0j5Laj2MpIqqGKrxeN28HNfHC3rh4aXWGZyFnXJA0tb" +
                                "PKm747UFzRQceOMhgijbA5xYwNJLraBeEuOVbwQwRx56gXPvQWGWWOFhfK8MaOJKh63HBYspBn+I4fAfVQ0s" +
                                "0kxvLI55GhcbrBBk97pHl73FzjqSbkrFEQEREBERBnDNLTytlgkfFI3Nr2OII7iFsnFa80DaHyubyZrt4R7x" +
                                "sOXsyvbS+aIgyqMZxOqdeevqHnozFnIc2m1x3GwvztmveLaTFoaV8MdZK0yPL3y75Mjrta3Mk8A0W4hEQa/n" +
                                "fEfIhRCtqBTAEdGJDaxFrd3ZovtRjOJ1Vunr6h9ozHnIc2nUHnfjfVEQPPWJmOSM19SWyPbI68hJLm6G+txY" +
                                "eA5BeTsRrnAh1bUEEPBBldmHm7hrxOvNEQes+M4nUOidNX1D3QtLWOMhuAb3z7QbX5LRREBERAREQEREBERA" +
                                "REQf/9k=";
                        chatMessage.conversionImage = image_default;
                    }
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.conversationId = documentChange.getDocument().getId();
                    conversation.add(chatMessage);
                } // nếu có thay đổi của dữ liệu trong 1 bản ghi nào đó
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversation.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString((Constants.KEY_RECEIVER_ID));
                        if(conversation.get(i).senderId.equals(senderId) && conversation.get(i).receiverId.equals(receiverId)) {
                            conversation.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversation.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
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

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(update)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    @Override
    public void onConversionCLicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        // chuyền thông tin của ng muốn nhắn tin qua activity chat
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}