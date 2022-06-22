package com.example.app_chat.activityes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.app_chat.adapters.AddFriendAdapter;
import com.example.app_chat.adapters.UsersAdapter;
import com.example.app_chat.databinding.ActivityAddFriendBinding;
import com.example.app_chat.databinding.ActivityMainBinding;
import com.example.app_chat.listeners.UserListener;
import com.example.app_chat.models.Friend;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {
    private ActivityAddFriendBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();

    }
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        loading(false);

    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        binding.inputSearch.setOnKeyListener((view, i, keyEvent) -> {
            if(!binding.inputSearch.getText().toString().isEmpty()) {
                getUsers();
            }
            return false;
        });

    }
    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        binding.textErrorMessage.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    // lấy ra id của người dùng hiện tại
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    // nếu ko có lỗi và có giá trị trả về sẽ chạy tiếp
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<Friend> friends = new ArrayList<>();
                        //
                        String textSearch = binding.inputSearch.getText().toString().trim();
                        if (textSearch.length() > 0)
                            textSearch = textSearch.substring(0, 1).toUpperCase() + textSearch.substring(1).toLowerCase();
                        // lấy dữ liệu trên db dùng 1 lần
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            // nếu là ng dùng thì sẽ next qua
                            if(currentUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }

//                            database.collection(Constants.KEY_COLLECTION_FRIEND)
//                                    .get()
//                                    .addOnCompleteListener(task1 -> {
//                                        if(task.isSuccessful() && task.getResult() != null) {
//
//                                            for (QueryDocumentSnapshot documentSnapshot1 : task1.getResult()) {
//
//                                                String id1 = currentUserId+documentSnapshot.getId();
//                                                String id2 = documentSnapshot.getId()+currentUserId;
//
//                                                if (id1.equals(documentSnapshot1.getString(Constants.KEY_FRIEND_ID)) || id2.equals(documentSnapshot1.getString(Constants.KEY_FRIEND_ID))) {
//                                                    if(documentSnapshot1.getString(Constants.KEY_INVITATION_STATUS).equals("1")) {
//                                                        showToast("sau");
//                                                        binding.textErrorMessage.setText(id1+ "|"+documentSnapshot1.getString(Constants.KEY_FRIEND_ID));
//                                                    }
//                                                }else {
//                                                    binding.textErrorMessage.setText("");
//                                                }
//                                            }
//                                        }else {
//                                            binding.textErrorMessage.setText("");
//                                        }
//                                    });

//                            if(!binding.textErrorMessage.getText().toString().equals("false")) {
                                if(documentSnapshot.getString(Constants.KEY_NAME) != null && documentSnapshot.getString(Constants.KEY_NAME).contains(textSearch)){
                                    Friend friend = new Friend();
                                    friend.friendName = documentSnapshot.getString(Constants.KEY_NAME);
                                    friend.friendImage = documentSnapshot.getString(Constants.KEY_IMAGE);
                                    friend.friendId = currentUserId+documentSnapshot.getId();
                                    friend.friendId2 = documentSnapshot.getId()+currentUserId;
                                    friends.add(friend);
                                }
//                            }


                        }
                        AddFriendAdapter addFriendAdapter = new AddFriendAdapter(friends);
                        binding.usersRcv.setAdapter(addFriendAdapter);
                        binding.usersRcv.setVisibility(View.VISIBLE);

                    }else {
                        showErrorMessage();
                    }
                });
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
}