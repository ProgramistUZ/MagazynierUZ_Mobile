package com.example.magazynieruz_mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.magazynieruz_mobile.MainActivity;
import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.User;
import com.example.magazynieruz_mobile.data.UserDao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginFragment extends Fragment {

    private EditText editLogin;
    private EditText editPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editLogin = view.findViewById(R.id.editLogin);
        editPassword = view.findViewById(R.id.editPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = editLogin.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        AppDatabase.databaseExecutor.execute(() -> {
            User user = userDao.findByUsername(username);
            String hashedPassword = user != null ? hashPassword(password) : null;
            boolean passwordMatches = user != null && user.passwordHash.equals(hashedPassword);

            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (user == null) {
                    Toast.makeText(getContext(), R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!passwordMatches) {
                    Toast.makeText(getContext(), R.string.error_wrong_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
