package com.example.app_chat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_chat.activityes.MainActivity;
import com.example.app_chat.databinding.ItemContainerRecentConversionBinding;
import com.example.app_chat.listeners.ConversionListener;
import com.example.app_chat.models.ChatMessage;
import com.example.app_chat.models.User;

import java.util.List;

public class RecentConversionsAdapter extends RecyclerView.Adapter<RecentConversionsAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    public RecentConversionsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConverdionImage(chatMessage.conversionImage));
            if(chatMessage.receiverNickname != null) {
                binding.textName.setText(chatMessage.receiverNickname);
            }else {
                binding.textName.setText(chatMessage.conversionName);
            }
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user= new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                user.conversationId = chatMessage.conversationId;
                user.receiverNickname = chatMessage.receiverNickname;
                if (chatMessage.statusMessage.equals("group")) {
                    user.Check = true;
                }else if(chatMessage.statusMessage.equals("chat")) {
                    user.Check = false;
                }
                conversionListener.onConversionCLicked(user);
            });
        }
    }

    private Bitmap getConverdionImage(String encededImage) {
        byte[] bytes = Base64.decode(encededImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
