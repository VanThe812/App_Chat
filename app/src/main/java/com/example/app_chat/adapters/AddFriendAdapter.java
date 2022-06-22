package com.example.app_chat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_chat.R;
import com.example.app_chat.databinding.ItemContainerFriendBinding;
import com.example.app_chat.databinding.ItemContainerRecentConversionBinding;
import com.example.app_chat.models.ChatMessage;
import com.example.app_chat.models.Friend;
import com.example.app_chat.models.User;
import com.example.app_chat.utilities.Constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder>{

    private final List<Friend> friends;
//    private final

    public AddFriendAdapter(List<Friend> friends) {
        this.friends = friends;
    }

    @NonNull
    @Override
    public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AddFriendViewHolder(ItemContainerFriendBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position) {
        holder.setData(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class AddFriendViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFriendBinding binding;

        AddFriendViewHolder(ItemContainerFriendBinding itemContainerFriendBinding) {
            super(itemContainerFriendBinding.getRoot());
            binding = itemContainerFriendBinding;
        }
        void setData(Friend friend) {
            binding.imageProfile.setImageBitmap(getConverdionImage(friend.friendImage));
            binding.textName.setText(friend.friendName);
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            database.collection(Constants.KEY_COLLECTION_FRIEND)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null) {
                            boolean check = false;
                            boolean check2 = false;
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                check = true;


                                if(friend.friendId.equals(documentSnapshot.getString(Constants.KEY_FRIEND_ID)) || friend.friendId2.equals(documentSnapshot.getString(Constants.KEY_FRIEND_ID))) {
                                    // neu ng dung la ng gui loi moi ket bn
                                    if(friend.friendId.equals(documentSnapshot.getString(Constants.KEY_FRIEND_ID))) {
                                        check2 = true;
                                        if(documentSnapshot.getString(Constants.KEY_INVITATION_STATUS).equals("2")) {
                                            binding.butonWait.setText(R.string.wait);
                                            binding.butonWait.setVisibility(View.VISIBLE);
                                            binding.butonAdd.setVisibility(View.INVISIBLE);
                                        }else if(documentSnapshot.getString(Constants.KEY_INVITATION_STATUS).equals("1")) {
                                            binding.butonAdd.setText("");
                                            binding.butonAdd.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    // neu ng dung la ng nhan loi moi ket bn
                                    if(friend.friendId2.equals(documentSnapshot.getString(Constants.KEY_FRIEND_ID))) {
                                        check2 = true;
                                        if(documentSnapshot.getString(Constants.KEY_INVITATION_STATUS).equals("2")) {

                                            binding.butonAdd.setText(R.string.confirm);
                                            binding.butonAdd.setVisibility(View.VISIBLE);
                                            binding.butonAdd.setOnClickListener(view -> {
                                                DocumentReference documentReference =
                                                        database.collection(Constants.KEY_COLLECTION_FRIEND)
                                                                .document(documentSnapshot.getId());
                                                documentReference.update(
                                                        Constants.KEY_INVITATION_STATUS, "1"
                                                );
                                                binding.butonAdd.setVisibility(View.INVISIBLE);
                                                Toast.makeText(view.getContext(), R.string.success_add_friend, Toast.LENGTH_SHORT).show();
                                            });
                                        }else if(documentSnapshot.getString(Constants.KEY_INVITATION_STATUS).equals("1")) {
                                            binding.butonAdd.setText("");
                                            binding.butonAdd.setVisibility(View.INVISIBLE);
                                        }

                                    }

                                }

                            }
                            if (!check || !check2) {
                                    binding.butonAdd.setText(R.string.add_friends);
                                    binding.butonAdd.setVisibility(View.VISIBLE);
                                    binding.butonAdd.setOnClickListener(view -> {
                                        HashMap<String, Object> addFriend = new HashMap<>();
                                        addFriend.put(Constants.KEY_FRIEND_ID, friend.friendId);
                                        addFriend.put(Constants.KEY_INVITATION_STATUS, "2");
                                        database.collection(Constants.KEY_COLLECTION_FRIEND).add(addFriend);
                                        binding.butonWait.setText(R.string.wait);
                                        binding.butonWait.setVisibility(View.VISIBLE);
                                        binding.butonAdd.setVisibility(View.INVISIBLE);
                                        Toast.makeText(view.getContext(), R.string.success_sent_friend, Toast.LENGTH_SHORT).show();
                                    });
                            }
                        }
                    });

        }
    }
    private Bitmap getConverdionImage(String encededImage) {
        byte[] bytes = Base64.decode(encededImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
