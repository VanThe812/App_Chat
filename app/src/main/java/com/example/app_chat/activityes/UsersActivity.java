package com.example.app_chat.activityes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.app_chat.R;
import com.example.app_chat.adapters.UsersAdapter;
import com.example.app_chat.databinding.ActivityUsersBinding;
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

public class UsersActivity extends AppCompatActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setListeners();
        loadUserDetails();
    }

    private void setListeners() {
        binding.bottomNav.setSelectedItemId(R.id.action_friend);
        binding.bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_chat:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                case R.id.action_friend:

                    return true;
                case R.id.action_user:
                    startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
            }
            return  false;
        });
        binding.imageAddFriend.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AddFriendActivity.class)));
    }
    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // l???y ra data c???a t???t c??? ng?????i d??ng
//        .whereIn(Constants.KEY_EMAIL, friend)
        database.collection(Constants.KEY_COLLECTION_USERS)

                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    // l???y ra id c???a ng?????i d??ng hi???n t???i
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    // n???u ko c?? l???i v?? c?? gi?? tr??? tr??? v??? s??? ch???y ti???p
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        // l???y d??? li???u tr??n db d??ng 1 l???n
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            // n???u l?? ng d??ng th?? s??? next qua
                            if(currentUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            // th??m d??? li???u nh???ng ng kh??c v??o array users
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                        }
                        // ki???u tra m???ng c?? d??? li???u hay ko
                        if(users.size() > 0){
                            // ????a array users v?? RCV qua UsersAdapter
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRcv.setAdapter(usersAdapter);
                            binding.usersRcv.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}