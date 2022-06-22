package com.example.app_chat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_chat.R;
import com.example.app_chat.databinding.ItemContainerNicknameBinding;
import com.example.app_chat.databinding.ItemContainerUserBinding;
import com.example.app_chat.listeners.NicknameListener;
import com.example.app_chat.listeners.UserListener;
import com.example.app_chat.models.User;

import java.util.List;

public class NicknameAdapter extends RecyclerView.Adapter<NicknameAdapter.NicknameViewholder>{

    private final List<User> users;
    private final NicknameListener nicknameListener;

    public NicknameAdapter(List<User> users, NicknameListener nicknameListener) {
        this.users = users;
        this.nicknameListener = nicknameListener;
    }

    @NonNull
    @Override
    public NicknameViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerNicknameBinding itemContainerNicknameBinding = ItemContainerNicknameBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NicknameViewholder(itemContainerNicknameBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NicknameViewholder holder, int position) {
        holder.setNicknameData(users.get(position));


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class NicknameViewholder extends RecyclerView.ViewHolder {

        ItemContainerNicknameBinding binding;

        NicknameViewholder(ItemContainerNicknameBinding itemContainerNicknameBinding) {
            super(itemContainerNicknameBinding.getRoot());
            binding = itemContainerNicknameBinding;
        }
        void setNicknameData(User user) {

            if(user.receiverNickname == null) {
                binding.textNickname.setText(user.name);
                binding.textName.setText(R.string.set_a_nickname);
            }else {
                binding.textNickname.setText(user.receiverNickname);
                binding.textName.setText(user.name);
            }

            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> nicknameListener.onNicknameListener(user));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
