package com.example.app_chat.activityes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.app_chat.R;
import com.example.app_chat.adapters.NicknameAdapter;
import com.example.app_chat.databinding.ActivityNicknameBinding;
import com.example.app_chat.databinding.LayoutNicknameBinding;
import com.example.app_chat.listeners.NicknameListener;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NicknameActivity extends AppCompatActivity implements NicknameListener {

    private ActivityNicknameBinding binding;
    private PreferenceManager preferenceManager;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNicknameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getUsers();
        setListeners();
    }
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        conversationId = getIntent().getExtras().getString("conversationId");
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());

    }
    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        List<User> users = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .document(conversationId)
                .get()
                .addOnCompleteListener(runnable -> {
                    if(runnable.isSuccessful() && runnable.getResult() != null) {
                        User user = new User();
                        user.id = runnable.getResult().get(Constants.KEY_SENDER_ID).toString();
                        user.name = runnable.getResult().get(Constants.KEY_SENDER_NAME).toString();
                        try {
                            user.image = runnable.getResult().get(Constants.KEY_SENDER_IMAGE).toString();
                        }catch (Exception e)
                        {
                            user.image = Constants.IMAGE_AVATAR_DEFAULT;

                        }
                        try {
                            user.receiverNickname = runnable.getResult().get(Constants.KEY_SENDER_NICKNAME).toString();
                        }catch (Exception e) {
                            user.receiverNickname = null;
                        }
                        user.conversationId = conversationId;
                        users.add(user);

                        User user2 = new User();
                        user2.id = runnable.getResult().get(Constants.KEY_RECEIVER_ID).toString();
                        user2.name = runnable.getResult().get(Constants.KEY_RECEIVER_NAME).toString();
                        try {
                            user2.image = runnable.getResult().get(Constants.KEY_RECEIVER_IMAGE).toString();
                        }catch (Exception e)
                        {
                            user2.image = Constants.IMAGE_AVATAR_DEFAULT;

                        }
                        try {
                            user2.receiverNickname = runnable.getResult().get(Constants.KEY_RECEIVER_NICKNAME).toString();
                        }catch (Exception e) {
                            user2.receiverNickname = null;
                        }
                        user2.conversationId = conversationId;
                        users.add(user2);
                    }
                    if(users.size() > 0){
                        NicknameAdapter nicknameAdapter = new NicknameAdapter(users, this);
                        binding.NicknameRCV.setAdapter(nicknameAdapter);
                        binding.NicknameRCV.setVisibility(View.VISIBLE);
                        loading(false);
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void openFeedbackDialog(User user) {
        final Dialog dialog = new Dialog(this);
        LayoutNicknameBinding layoutNicknameBinding;
        layoutNicknameBinding = LayoutNicknameBinding.inflate(getLayoutInflater());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutNicknameBinding.getRoot());
        dialog.show();
        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        layoutNicknameBinding.textAttention.setText(getString(R.string.nickname_Attention1)+ " "+ user.name + " " +getString(R.string.nickname_Attention2));
        layoutNicknameBinding.inputNickname.setHint(user.name);

        layoutNicknameBinding.buttonCancel.setOnClickListener(v -> dialog.dismiss());
        layoutNicknameBinding.buttonSave.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            if(!layoutNicknameBinding.inputNickname.getText().toString().trim().isEmpty()) {
                database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                        .document(user.conversationId)
                        .get()
                        .addOnCompleteListener(runnable -> {
                            if(runnable.isSuccessful() && runnable.getResult() != null) {
                                if(user.id.equals(runnable.getResult().get(Constants.KEY_RECEIVER_ID).toString())) {
                                    DocumentReference documentReference =
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                                    .document(user.conversationId);
                                    documentReference.update(
                                            Constants.KEY_RECEIVER_NICKNAME, layoutNicknameBinding.inputNickname.getText().toString()
                                    );
                                }else if(user.id.equals(runnable.getResult().get(Constants.KEY_SENDER_ID))) {
                                    DocumentReference documentReference =
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                                    .document(user.conversationId);
                                    documentReference.update(
                                            Constants.KEY_SENDER_NICKNAME, layoutNicknameBinding.inputNickname.getText().toString()
                                    );
                                }
                            }
                            showToast(getString(R.string.success_nickname_change));
                        });

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
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNicknameListener(User user) {
        openFeedbackDialog(user);
    }
}