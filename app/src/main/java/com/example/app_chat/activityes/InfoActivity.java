package com.example.app_chat.activityes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.app_chat.R;
import com.example.app_chat.adapters.MemberGroupAdapter;
import com.example.app_chat.adapters.NicknameAdapter;
import com.example.app_chat.databinding.ActivityInfoBinding;
import com.example.app_chat.databinding.LayoutChangePasswordBinding;
import com.example.app_chat.databinding.LayoutMemberGroupBinding;
import com.example.app_chat.listeners.MemberListener;
import com.example.app_chat.models.ChatMessage;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class InfoActivity extends AppCompatActivity implements MemberListener {
    private ActivityInfoBinding binding;
    private User user;
    private String encodedImage;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        loadUserDetails();
        setListeners();
    }
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
    }
    private void  loadUserDetails() {
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        binding.textName.setText(user.name);
        byte[] bytes = Base64.decode(user.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.textCreateGroup.setText(getString(R.string.create_a_chat_group_with)+" "+user.name);
        if(user.Check == true) {
            binding.imageChangeImage.setVisibility(View.VISIBLE);
            binding.imageAddMemberGroup.setVisibility(View.VISIBLE);

            binding.imageDelete.setBackgroundResource(R.drawable.ic_out_group);
            binding.textDelete.setText(R.string.leave_group);

            binding.textCreateGroup.setText(R.string.group_member);
        }

    }
    public void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonBackground.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.buttonNickname.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NicknameActivity.class);
            intent.putExtra("conversationId", user.conversationId);
            intent.putExtra("status", user.Check);
            startActivity(intent);
        });
        binding.imageChangeImage.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(R.string.confirm);
            builder.setMessage(R.string.are_you_sure_change_picture_group_chat);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pickImageAvatar.launch(intent);
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        });
        binding.buttonDeleteChat.setOnClickListener(v -> {
            if(user.Check == true) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.confirm);
                builder.setMessage("Ban co chac chan muon roi khoi nhom chat?");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                .document(user.conversationId);
                        documentReference.update(
                                Constants.KEY_PEOPLE_ID+preferenceManager.getString(Constants.KEY_USER_ID), null,
                                Constants.KEY_PEOPLE_NAME+preferenceManager.getString(Constants.KEY_USER_ID), null,
                                Constants.KEY_PEOPLE_NICKNAME+preferenceManager.getString(Constants.KEY_USER_ID), null
                        );
                        showToast("Roi khoi nhom thanh cong");
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.confirm);
                builder.setMessage("Ban co chac chan muon xoa doan chat?");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                .document(user.conversationId);
                        documentReference.delete().
                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // inside on complete method we are checking
                                        // if the task is success or not.
                                        if (task.isSuccessful()) {
                                            database.collection(Constants.KEY_COLLECTION_CHAT)
                                                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                                    .whereEqualTo(Constants.KEY_RECEIVER_ID, user.id)
                                                    .addSnapshotListener(eventListener);
                                            database.collection(Constants.KEY_COLLECTION_CHAT)
                                                    .whereEqualTo(Constants.KEY_SENDER_ID, user.id)
                                                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                                    .addSnapshotListener(eventListener);
                                            showToast("Xoa doan chat thanh cong");
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            overridePendingTransition(0, 0);
                                            finish();
                                        }
                                    }
                                });

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            }
        });
        binding.buttonCreateGroupChat.setOnClickListener(v -> {
            if(user.Check == true) {
                final Dialog dialog = new Dialog(this);
                LayoutMemberGroupBinding layoutMemberGroupBinding;
                layoutMemberGroupBinding = LayoutMemberGroupBinding.inflate(getLayoutInflater());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(layoutMemberGroupBinding.getRoot());
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

                List<User> users = new ArrayList<>();
                database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                        .document(user.conversationId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful() && task.getResult() != null) {

                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if(task1.isSuccessful() && task1.getResult() != null) {
                                                for (QueryDocumentSnapshot documentSnapshot : task1.getResult()) {

                                                    try{
                                                        User user1 = new User();
                                                        if(task.getResult().get(Constants.KEY_PEOPLE_ID+documentSnapshot.getId()).equals(documentSnapshot.getId())) {
                                                            user1.id = documentSnapshot.getId();
                                                            user1.name = documentSnapshot.getString(Constants.KEY_NAME);
                                                            try {
                                                                user1.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                                                            }catch (Exception e)
                                                            {
                                                                user1.image = Constants.IMAGE_AVATAR_DEFAULT;
                                                            }
                                                            try {
                                                                user1.receiverNickname = task.getResult().get(Constants.KEY_PEOPLE_NICKNAME+documentSnapshot.getId()).toString();
                                                            }catch (Exception e) {
                                                                user1.receiverNickname = null;
                                                            }
                                                        }
                                                        user1.conversationId = user.conversationId;
                                                        user1.Check = true;
                                                        users.add(user1);

                                                    }catch (Exception e) {}
                                                }
                                                if(users.size() > 0){
                                                    MemberGroupAdapter memberGroupAdapter = new MemberGroupAdapter(users, this);
                                                    layoutMemberGroupBinding.RCVMember.setAdapter(memberGroupAdapter);
                                                    layoutMemberGroupBinding.RCVMember.setVisibility(View.VISIBLE);

                                                }
                                            }
                                        });

                            }


                        });

//                layoutChangePasswordBinding.buttonCancel.setOnClickListener(view -> dialog.dismiss());
//                layoutChangePasswordBinding.buttonSave.setOnClickListener(view -> {
//                    if(layoutChangePasswordBinding.inputPasswordOld.getText().toString().trim().isEmpty()) {
//                        showToast(getString(R.string.old_pass_cannot_blank));
//                    } else if(layoutChangePasswordBinding.inputPasswordNew.getText().toString().trim().isEmpty()) {
//                        showToast(getString(R.string.new_pass_cannot_blank));
//                    } else if(!layoutChangePasswordBinding.inputPasswordOld.getText().toString().trim().equals(preferenceManager.getString(Constants.KEY_PASSWORD))) {
//                        showToast(getString(R.string.old_pass_is_incorrect));
//                    } else {
//                        DocumentReference documentReference =
//                                database.collection(Constants.KEY_COLLECTION_USERS)
//                                        .document(preferenceManager.getString(Constants.KEY_USER_ID));
//                        documentReference.update(
//                                Constants.KEY_PASSWORD, layoutChangePasswordBinding.inputPasswordNew.getText().toString().trim()
//                        );
//                        preferenceManager.putString(Constants.KEY_PASSWORD, layoutChangePasswordBinding.inputPasswordNew.getText().toString().trim());
//                        showToast(getString(R.string.change_pass_success));
//                        dialog.dismiss();
//                    }
//                });
            }
        });
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if(error != null) {
            return;
        }
        if(value != null) {

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                database.collection(Constants.KEY_COLLECTION_CHAT)
                        .document(documentChange.getDocument().getId())
                        .delete();
            }

        }
    };
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void update_background() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, user.id)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            DocumentReference documentReference =
                                    database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(documentSnapshot.getId());
                            documentReference.update(
                                    Constants.KEY_BACKGROUND, encodedImage
                            );
                        }
                    }
                });
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_SENDER_ID, user.id)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            DocumentReference documentReference =
                                    database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(documentSnapshot.getId());
                            documentReference.update(
                                    Constants.KEY_BACKGROUND, encodedImage
                            );
                        }
                    }
                });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            encodedImage = encodeImage(bitmap);
                            update_background();
                            showToast(getString(R.string.success_background_update));
                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent> pickImageAvatar = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            encodedImage = encodeImage(bitmap);
                            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                    .document(user.conversationId)
                                    .update(
                                            Constants.KEY_IMAGE_GROUP, encodedImage
                                    );
                            showToast(getString(R.string.success_background_update));
                            binding.imageProfile.setImageBitmap(bitmap);
                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void MemberOnCLicked(User user) {

    }
}