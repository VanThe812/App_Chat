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
import com.example.app_chat.listeners.MemberListener;
import com.example.app_chat.listeners.NicknameListener;
import com.example.app_chat.models.User;

import java.util.List;

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.MemberGroupViewholder>{

    private final List<User> users;
    private final MemberListener memberListener;

    public MemberGroupAdapter(List<User> users, MemberListener memberListener) {
        this.users = users;
        this.memberListener = memberListener;
    }

    @NonNull
    @Override
    public MemberGroupViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerNicknameBinding itemContainerNicknameBinding = ItemContainerNicknameBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MemberGroupViewholder(itemContainerNicknameBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberGroupViewholder holder, int position) {
        holder.setNicknameData(users.get(position));


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MemberGroupViewholder extends RecyclerView.ViewHolder {

        ItemContainerNicknameBinding binding;

        MemberGroupViewholder(ItemContainerNicknameBinding itemContainerNicknameBinding) {
            super(itemContainerNicknameBinding.getRoot());
            binding = itemContainerNicknameBinding;
        }
        void setNicknameData(User user) {

            if(user.receiverNickname == null || user.receiverNickname == "") {
                binding.textNickname.setText(user.name);
                binding.textName.setText(R.string.set_a_nickname);
            }else {
                binding.textNickname.setText(user.receiverNickname);
                binding.textName.setText(user.name);
            }

            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> memberListener.MemberOnCLicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
