package com.example.magazynieruz_mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.magazynieruz_mobile.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "magazynier_prefs";
    private static final int[] NIGHT_MODES = {
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    };

    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int themeIndex = prefs.getInt("theme", 2);
        AppCompatDelegate.setDefaultNightMode(NIGHT_MODES[themeIndex]);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);

        Spinner spinnerTheme = view.findViewById(R.id.spinnerTheme);
        String[] themes = {"MOTYW — Jasny", "MOTYW — Ciemny", "MOTYW — Systemowy"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_spinner, themes);
        themeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerTheme.setAdapter(themeAdapter);
        spinnerTheme.setSelection(prefs.getInt("theme", 2), false);
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                prefs.edit().putInt("theme", position).apply();
                AppCompatDelegate.setDefaultNightMode(NIGHT_MODES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        SwitchMaterial switchPush = view.findViewById(R.id.switchPush);
        SwitchMaterial switchSms = view.findViewById(R.id.switchSms);
        SwitchMaterial switchEmail = view.findViewById(R.id.switchEmail);

        switchPush.setChecked(prefs.getBoolean("push", true));
        switchSms.setChecked(prefs.getBoolean("sms", true));
        switchEmail.setChecked(prefs.getBoolean("email", true));

        switchPush.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("push", checked).apply());
        switchSms.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("sms", checked).apply());
        switchEmail.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("email", checked).apply());

        Button btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Reset hasła — do implementacji", Toast.LENGTH_SHORT).show()
        );

        Button btnExportPdf = view.findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Eksport PDF — do implementacji", Toast.LENGTH_SHORT).show()
        );

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
