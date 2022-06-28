package com.example.app_chat.activityes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.app_chat.R;
import com.example.app_chat.adapters.UsersGroupChatAdapter;
import com.example.app_chat.databinding.ActivityAddFriendBinding;
import com.example.app_chat.databinding.ActivityCreateGroupChatBinding;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CreateGroupChatActivity extends AppCompatActivity {
    private ActivityCreateGroupChatBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private UsersGroupChatAdapter usersGroupChatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getUser();
        setListener();
    }
    private void init() {
        loading(false);
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

    }

    private void getUser() {
        loading(true);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    // lấy ra id của người dùng hiện tại
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    // nếu ko có lỗi và có giá trị trả về sẽ chạy tiếp
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        // lấy dữ liệu trên db dùng 1 lần
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            // nếu là ng dùng thì sẽ next qua
                            if(currentUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            // thêm dữ liệu những ng khác vào array users
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                        }
                        // kiểu tra mảng có dữ liệu hay ko
                        if(users.size() > 0){
                            // Đưa array users và RCV qua UsersAdapter
                            usersGroupChatAdapter = new UsersGroupChatAdapter(users, getApplicationContext());
                            binding.usersRcv.setAdapter(usersGroupChatAdapter);
                            binding.usersRcv.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> finish());
        binding.textSave.setOnClickListener(v -> createGroupChat());
    }
    private void createGroupChat() {
        if(!binding.inputNameGroup.getText().toString().trim().isEmpty()) {
            if(usersGroupChatAdapter.getUserSelected().size() > 0) {
                ArrayList<User> UserSelected = usersGroupChatAdapter.getUserSelected();
                // groupChat
                HashMap<String, Object> groupChat = new HashMap<>();
                groupChat.put(Constants.KEY_NUMBER_PEOPLE, UserSelected.size()-1);
                groupChat.put(Constants.KEY_NAME_GROUP, binding.inputNameGroup.getText().toString().trim());
                groupChat.put(Constants.KEY_IMAGE_GROUP, Constants.IMAGE_GROUP_CHAT_DEFAULT);
                for (int i = 0; i < UserSelected.size(); i++) {
                    groupChat.put(Constants.KEY_PEOPLE_ID+UserSelected.get(i).id, UserSelected.get(i).id);
                    groupChat.put(Constants.KEY_PEOPLE_NAME+UserSelected.get(i).id, UserSelected.get(i).name);
                    groupChat.put(Constants.KEY_PEOPLE_NICKNAME+UserSelected.get(i).id, "");
                }
                database.collection(Constants.KEY_COLLECTION_GROUP_CHAT)
                        .add(groupChat)
                        .addOnSuccessListener(documentReference -> {
                            // conversion
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(Constants.KEY_ID_GROUP, documentReference.getId());
                            conversion.put(Constants.KEY_NAME_GROUP, binding.inputNameGroup.getText().toString().trim());
                            conversion.put(Constants.KEY_IMAGE_GROUP, Constants.IMAGE_GROUP_CHAT_DEFAULT);
                            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                            conversion.put(Constants.KEY_LAST_MESSAGE, getString(R.string.create_a_group));
                            conversion.put(Constants.KEY_TIMESTAMP, new Date());
                            conversion.put(Constants.KEY_BACKGROUND, "");
                            conversion.put(Constants.KEY_SENDER_NICKNAME , null);
                            conversion.put(Constants.KEY_COLLECTION_STATUS, "group");
                            for (int i = 0; i < UserSelected.size(); i++) {
                                conversion.put(Constants.KEY_PEOPLE_ID+UserSelected.get(i).id, UserSelected.get(i).id);
                                conversion.put(Constants.KEY_PEOPLE_NAME+UserSelected.get(i).id, UserSelected.get(i).name);
                                conversion.put(Constants.KEY_PEOPLE_NICKNAME+UserSelected.get(i).id, "");
                            }


                            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                            .add(conversion);

                            showToast(getString(R.string.success_create_chat_group));
                            finish();
                        })
                        .addOnFailureListener(exception -> {
                            showToast(exception.getMessage());
                        });




            }
        } else {
            showToast(getString(R.string.name_group_not_left_blank));
        }


    }
    // -----------------------------------------------------------------------
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
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}