package com.example.app_chat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_chat.databinding.ItemContainerNumberGroupChatBinding;
import com.example.app_chat.listeners.UserListener;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.example.app_chat.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersGroupChatAdapter extends RecyclerView.Adapter<UsersGroupChatAdapter.UsersGroupChatViewholder>{

    private final List<User> users;
    private final Context mContext;

    public UsersGroupChatAdapter(List<User> users, Context mContext) {
        this.users = users;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UsersGroupChatViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerNumberGroupChatBinding itemContainerNumberGroupChatBinding = ItemContainerNumberGroupChatBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UsersGroupChatViewholder(itemContainerNumberGroupChatBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersGroupChatViewholder holder, int position) {
        holder.setUserData(users.get(position));


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UsersGroupChatViewholder extends RecyclerView.ViewHolder {

        ItemContainerNumberGroupChatBinding binding;

        UsersGroupChatViewholder(ItemContainerNumberGroupChatBinding itemContainerNumberGroupChatBinding) {
            super(itemContainerNumberGroupChatBinding.getRoot());
            binding = itemContainerNumberGroupChatBinding;
        }
        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            user.Check = false;
            binding.getRoot().setOnClickListener(v -> {
                binding.radioChoose.setChecked(true);
            });
            binding.radioChoose.setOnCheckedChangeListener((compoundButton, status) -> {
                user.Check = status;
            });


        }

    }

    public ArrayList<User> getUserSelected() {
        ArrayList<User> usersNew = new ArrayList<>();
        for (int i = 0; i<users.size(); i++) {
            if(users.get(i).Check) {
                usersNew.add(users.get(i));
            }
        }
        PreferenceManager preferenceManager = new PreferenceManager(mContext);
        User user_new = new User();
        user_new.id = preferenceManager.getString(Constants.KEY_USER_ID);
        user_new.name = preferenceManager.getString(Constants.KEY_NAME);
        user_new.email = preferenceManager.getString(Constants.KEY_EMAIL);
        user_new.image = preferenceManager.getString(Constants.KEY_IMAGE);
        user_new.Check = true;
        users.add(user_new);

        usersNew.add(user_new);
        return usersNew;
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
