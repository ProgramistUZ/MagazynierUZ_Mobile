package com.example.magazynieruz_mobile.ui;

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
import androidx.viewpager2.widget.ViewPager2;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.User;
import com.example.magazynieruz_mobile.data.UserDao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterFragment extends Fragment {

    private EditText editLogin;
    private EditText editPassword;
    private EditText editConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editLogin = view.findViewById(R.id.editLogin);
        editPassword = view.findViewById(R.id.editPassword);
        editConfirmPassword = view.findViewById(R.id.editConfirmPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = editLogin.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), R.string.error_passwords_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        AppDatabase.databaseExecutor.execute(() -> {
            boolean taken = userDao.findByUsername(username) != null;
            if (taken) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), R.string.error_username_taken, Toast.LENGTH_SHORT).show();
                });
                return;
            }

            User user = new User();
            user.username = username;
            user.passwordHash = hashPassword(password);
            userDao.insertUser(user);

            mainHandler.post(() -> {
                if (!isAdded()) return;
                Toast.makeText(getContext(), R.string.success_registration, Toast.LENGTH_SHORT).show();
                ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
                viewPager.setCurrentItem(0);
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
