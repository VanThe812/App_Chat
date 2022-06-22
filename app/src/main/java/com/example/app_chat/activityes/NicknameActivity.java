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
        // lấy ra data của tất cả người dùng
//        .whereIn(Constants.KEY_EMAIL, friend)
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
                            String image_default = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0o" +
                                    "OjM9PDkzODdASFxOQERXRTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2P/2wBDARESEhgVGC8aGi9jQjhCY2NjY2Nj" +
                                    "Y2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2P/wAARCACWAJYDASIAAhEBAxEB" +
                                    "/8QAGwABAAIDAQEAAAAAAAAAAAAAAAUGAgQHAwH/xABBEAABAwIDBAUJBAgHAAAAAAABAAIDBBEFITEGEkFR" +
                                    "EyJhcZEUFSMygaGxwdEkUlOiFjM1QmJyc7JDVIKSk8Lx/8QAFAEBAAAAAAAAAAAAAAAAAAAAAP/EABQRAQAA" +
                                    "AAAAAAAAAAAAAAAAAAD/2gAMAwEAAhEDEQA/AOfoiICIiAiLJjHSPaxjS5zjYNaLklBiin6HZOvqQ187mUzD" +
                                    "wdm7w+pCmqfZDD4y0zPmmIGYLt1p8M/egoyLpMOB4XALMooj/ON/43Xr5qw7/IUv/C36IOYoujTbPYVMbuo2" +
                                    "NP8AAS34FRlRsZSub9mqZY3X/fAcPkgpiKVxDZ3EKBhe6MSxgXL4jvAd41UUgIiICIiAiIgIiICIrJs3s8ao" +
                                    "trK1loBmyMj9Z2ns+KDTwbZ6pxO0rvQ01/1jhm7+Uce9XbD8MpMOj3aWENJFnPObnd5W01rWMDWNDWtFgALA" +
                                    "BfUBERAREQEREBROK7P0eJBzw0Q1B/xWDXvHH4qWRBzLEsMqsMm6OpZYH1Xtza7uK011OqpYKyAw1MTZGHgR" +
                                    "p2jkVz/G8Gmwmexu+Bx6kltew8igjEREBERARFkxjpHtYxpc5xsABmSgltnMI851u9KD5NFm/wDiPBq6A1rW" +
                                    "MDWNDWtFgALABauFULMOw+KmbYlou9w/ecdSttAREQEXnPMyCMvee4cSoeoq5Kgm5szg0IJV9ZTsNnSi/Zn8" +
                                    "FgMQp7+uR/pKhkQT0VRDNlHICeWhXqq4t2lr3xndlJeznxCCWRfGuDmhzSCDoQvqAteuooa+lfT1DbsdoeLT" +
                                    "zHathEHLq+jloKySmmHWYcj94cCtdXna/DvKaAVcbfS0+ZsMyzj4a+KoyAiIgKf2PohU4oZ3tuynbvDlvHIf" +
                                    "M+xQCvOxcHR4Q+UtAMspIPMCw+N0FgREQE0ReFc/cpJDxIt4oIusqDUTEg9QZNC10RAREQEREEhhlRZ3QuOR" +
                                    "9XsKk1XmOLHteNWm6sIIIBGhQEREHxzWvYWvAc1wsQdCFzHEqU0WI1FMQQI3kNvru8D4WXT1SdtoNzE4Zg0B" +
                                    "ssVr8yDn7iEFcREQF0nAIRBgdGwcYw//AHZ/Nc2XTsJ/ZFF/QZ/aEG2iIgLTxQE0otwcLrcXlVR9LTSMGtri" +
                                    "3NBAoiICIiAiIgKwQAiCMHUNHwUHBH0szGZ9Y525KfQEREBVnbiEOoqabiyQs8Rf/qrMq9tt+yIv64/tcgo6" +
                                    "IiAujbNymbAaVx1DS3wJHyXOVc9iKhrqGop895km/wCwi3yPigsyIiAiIgicQpjFJ0jR1He4rSVic0PaWuAI" +
                                    "OoKiquhERLo3t3fuudYoNJERARfWguIAtnzNlJ0dAxtpJHNeeABuEH3DaYxt6V46ztByC3kRAREQFVduZSIa" +
                                    "SEaOc5x9gAHxKtSoe2NQ2bGujbf0MbWHlfM/MIIJERAUrs1XNoMXjdIQI5R0bieF7WPiAopEHWUUTs5igxLD" +
                                    "mh7r1EIDZL8eR9vxupR72xsL3mzRqUGS1KiviiyZ6R3YcvFadXXPmJay7We8rTQbMtbPLfr7o5NyWuviICIi" +
                                    "Asmuc03aSDzBWKINyHEZmZPtIO3IqQp6qKcdV1nfdOqg19BIIINiOIQWJFHUeIaRznuf9VIoPKqqI6SmkqJT" +
                                    "ZkbS4/RcvqZnVNTLO+wdI8vNuZN1adssUBDcOhdncOmt7h8/BVJAREQEREG5hWIyYZXMqI8wMnt+83iFb5cQ" +
                                    "GIMZJGfQkXaPr2qiLcw7EH0UvF0Tj1m/MILUi86eojqYhJE7eafEHkV6ICIiAiIgIiICIiAsqnHDhlE4OG/I" +
                                    "RaIHn29gWnXYhFRM6/WkOjAc/wDxVioqJKmYyyuu4+A7EGEkj5ZXSSOLnvJc4niSsURAREQEUhgNDFiWM0tH" +
                                    "O57Y5XWcWEAjI6XW8/ZoNpqENr4zW1jYpG0/RPs1kjt1ri8AjUi97e3K4QKK1R7HwSSSyMxb7FDHI6Sc0zml" +
                                    "royN5u4Tcix1F9CFi7Y9kVVJTVGJtjl8oNPCBA5wkdusc25Hq3D876W4oK5TVMtLIHwvLTxHA94U7TY5TyN9" +
                                    "PeJw7CQV7foduU5ZLiDW4iKU1XkjYiRu3sOve2th9Rmvak2OpqmrqKaDEXVEkTJGG0D42smbazS4ggi+ts0H" +
                                    "n51ovxvyu+iedaL8b8rvotWo2dhjw7EaqmxEzuoZNx0XQFrtWgkgm7cy7gfV4aDKn2c6fAG4iK5jZXwyztpz" +
                                    "Gc2xus7rX5W4cfag2POtF+N+V30X1uKUTnACcXJtm0j5Laj2MpIqqGKrxeN28HNfHC3rh4aXWGZyFnXJA0tb" +
                                    "PKm747UFzRQceOMhgijbA5xYwNJLraBeEuOVbwQwRx56gXPvQWGWWOFhfK8MaOJKh63HBYspBn+I4fAfVQ0s" +
                                    "0kxvLI55GhcbrBBk97pHl73FzjqSbkrFEQEREBERBnDNLTytlgkfFI3Nr2OII7iFsnFa80DaHyubyZrt4R7x" +
                                    "sOXsyvbS+aIgyqMZxOqdeevqHnozFnIc2m1x3GwvztmveLaTFoaV8MdZK0yPL3y75Mjrta3Mk8A0W4hEQa/n" +
                                    "fEfIhRCtqBTAEdGJDaxFrd3ZovtRjOJ1Vunr6h9ozHnIc2nUHnfjfVEQPPWJmOSM19SWyPbI68hJLm6G+txY" +
                                    "eA5BeTsRrnAh1bUEEPBBldmHm7hrxOvNEQes+M4nUOidNX1D3QtLWOMhuAb3z7QbX5LRREBERAREQEREBERA" +
                                    "REQf/9k=";
                            user.image = image_default;

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
                            String image_default = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0o" +
                                    "OjM9PDkzODdASFxOQERXRTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2P/2wBDARESEhgVGC8aGi9jQjhCY2NjY2Nj" +
                                    "Y2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2P/wAARCACWAJYDASIAAhEBAxEB" +
                                    "/8QAGwABAAIDAQEAAAAAAAAAAAAAAAUGAgQHAwH/xABBEAABAwIDBAUJBAgHAAAAAAABAAIDBBEFITEGEkFR" +
                                    "EyJhcZEUFSMygaGxwdEkUlOiFjM1QmJyc7JDVIKSk8Lx/8QAFAEBAAAAAAAAAAAAAAAAAAAAAP/EABQRAQAA" +
                                    "AAAAAAAAAAAAAAAAAAD/2gAMAwEAAhEDEQA/AOfoiICIiAiLJjHSPaxjS5zjYNaLklBiin6HZOvqQ187mUzD" +
                                    "wdm7w+pCmqfZDD4y0zPmmIGYLt1p8M/egoyLpMOB4XALMooj/ON/43Xr5qw7/IUv/C36IOYoujTbPYVMbuo2" +
                                    "NP8AAS34FRlRsZSub9mqZY3X/fAcPkgpiKVxDZ3EKBhe6MSxgXL4jvAd41UUgIiICIiAiIgIiICIrJs3s8ao" +
                                    "trK1loBmyMj9Z2ns+KDTwbZ6pxO0rvQ01/1jhm7+Uce9XbD8MpMOj3aWENJFnPObnd5W01rWMDWNDWtFgALA" +
                                    "BfUBERAREQEREBROK7P0eJBzw0Q1B/xWDXvHH4qWRBzLEsMqsMm6OpZYH1Xtza7uK011OqpYKyAw1MTZGHgR" +
                                    "p2jkVz/G8Gmwmexu+Bx6kltew8igjEREBERARFkxjpHtYxpc5xsABmSgltnMI851u9KD5NFm/wDiPBq6A1rW" +
                                    "MDWNDWtFgALABauFULMOw+KmbYlou9w/ecdSttAREQEXnPMyCMvee4cSoeoq5Kgm5szg0IJV9ZTsNnSi/Zn8" +
                                    "FgMQp7+uR/pKhkQT0VRDNlHICeWhXqq4t2lr3xndlJeznxCCWRfGuDmhzSCDoQvqAteuooa+lfT1DbsdoeLT" +
                                    "zHathEHLq+jloKySmmHWYcj94cCtdXna/DvKaAVcbfS0+ZsMyzj4a+KoyAiIgKf2PohU4oZ3tuynbvDlvHIf" +
                                    "M+xQCvOxcHR4Q+UtAMspIPMCw+N0FgREQE0ReFc/cpJDxIt4oIusqDUTEg9QZNC10RAREQEREEhhlRZ3QuOR" +
                                    "9XsKk1XmOLHteNWm6sIIIBGhQEREHxzWvYWvAc1wsQdCFzHEqU0WI1FMQQI3kNvru8D4WXT1SdtoNzE4Zg0B" +
                                    "ssVr8yDn7iEFcREQF0nAIRBgdGwcYw//AHZ/Nc2XTsJ/ZFF/QZ/aEG2iIgLTxQE0otwcLrcXlVR9LTSMGtri" +
                                    "3NBAoiICIiAiIgKwQAiCMHUNHwUHBH0szGZ9Y525KfQEREBVnbiEOoqabiyQs8Rf/qrMq9tt+yIv64/tcgo6" +
                                    "IiAujbNymbAaVx1DS3wJHyXOVc9iKhrqGop895km/wCwi3yPigsyIiAiIgicQpjFJ0jR1He4rSVic0PaWuAI" +
                                    "OoKiquhERLo3t3fuudYoNJERARfWguIAtnzNlJ0dAxtpJHNeeABuEH3DaYxt6V46ztByC3kRAREQFVduZSIa" +
                                    "SEaOc5x9gAHxKtSoe2NQ2bGujbf0MbWHlfM/MIIJERAUrs1XNoMXjdIQI5R0bieF7WPiAopEHWUUTs5igxLD" +
                                    "mh7r1EIDZL8eR9vxupR72xsL3mzRqUGS1KiviiyZ6R3YcvFadXXPmJay7We8rTQbMtbPLfr7o5NyWuviICIi" +
                                    "Asmuc03aSDzBWKINyHEZmZPtIO3IqQp6qKcdV1nfdOqg19BIIINiOIQWJFHUeIaRznuf9VIoPKqqI6SmkqJT" +
                                    "ZkbS4/RcvqZnVNTLO+wdI8vNuZN1adssUBDcOhdncOmt7h8/BVJAREQEREG5hWIyYZXMqI8wMnt+83iFb5cQ" +
                                    "GIMZJGfQkXaPr2qiLcw7EH0UvF0Tj1m/MILUi86eojqYhJE7eafEHkV6ICIiAiIgIiICIiAsqnHDhlE4OG/I" +
                                    "RaIHn29gWnXYhFRM6/WkOjAc/wDxVioqJKmYyyuu4+A7EGEkj5ZXSSOLnvJc4niSsURAREQEUhgNDFiWM0tH" +
                                    "O57Y5XWcWEAjI6XW8/ZoNpqENr4zW1jYpG0/RPs1kjt1ri8AjUi97e3K4QKK1R7HwSSSyMxb7FDHI6Sc0zml" +
                                    "royN5u4Tcix1F9CFi7Y9kVVJTVGJtjl8oNPCBA5wkdusc25Hq3D876W4oK5TVMtLIHwvLTxHA94U7TY5TyN9" +
                                    "PeJw7CQV7foduU5ZLiDW4iKU1XkjYiRu3sOve2th9Rmvak2OpqmrqKaDEXVEkTJGG0D42smbazS4ggi+ts0H" +
                                    "n51ovxvyu+iedaL8b8rvotWo2dhjw7EaqmxEzuoZNx0XQFrtWgkgm7cy7gfV4aDKn2c6fAG4iK5jZXwyztpz" +
                                    "Gc2xus7rX5W4cfag2POtF+N+V30X1uKUTnACcXJtm0j5Laj2MpIqqGKrxeN28HNfHC3rh4aXWGZyFnXJA0tb" +
                                    "PKm747UFzRQceOMhgijbA5xYwNJLraBeEuOVbwQwRx56gXPvQWGWWOFhfK8MaOJKh63HBYspBn+I4fAfVQ0s" +
                                    "0kxvLI55GhcbrBBk97pHl73FzjqSbkrFEQEREBERBnDNLTytlgkfFI3Nr2OII7iFsnFa80DaHyubyZrt4R7x" +
                                    "sOXsyvbS+aIgyqMZxOqdeevqHnozFnIc2m1x3GwvztmveLaTFoaV8MdZK0yPL3y75Mjrta3Mk8A0W4hEQa/n" +
                                    "fEfIhRCtqBTAEdGJDaxFrd3ZovtRjOJ1Vunr6h9ozHnIc2nUHnfjfVEQPPWJmOSM19SWyPbI68hJLm6G+txY" +
                                    "eA5BeTsRrnAh1bUEEPBBldmHm7hrxOvNEQes+M4nUOidNX1D3QtLWOMhuAb3z7QbX5LRREBERAREQEREBERA" +
                                    "REQf/9k=";
                            user2.image = image_default;

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

    private void openFeedbackDialog(String conversationId) {
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

        int Attention1 = R.string.nickname_Attention1;
        layoutNicknameBinding.textAttention.setText("Only Diu sees this nickname in conversation");
        layoutNicknameBinding.inputNickname.setHint("Diu");

        layoutNicknameBinding.buttonCancel.setOnClickListener(v -> dialog.dismiss());
        layoutNicknameBinding.buttonSave.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

//            DocumentReference documentReference =
//                    database.collection(Constants.KEY_COLLECTION_CONVERSATION)
//                            .document(conversationId);
//            if()
//            documentReference.update(
//                    Constants.
            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                    .document(conversationId)
                    .get()
                    .addOnCompleteListener(runnable -> {
                        if(runnable.isSuccessful() && runnable.getResult() != null) {
//                            if()
                        }
                    });


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
    public void onNicknameListener(String conversationId) {
        openFeedbackDialog(conversationId);
    }
}