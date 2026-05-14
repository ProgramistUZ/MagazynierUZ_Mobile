package com.example.magazynieruz_mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.Product;
import com.example.magazynieruz_mobile.data.ProductDao;
import com.example.magazynieruz_mobile.service.FallDetectionService;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "magazynier_prefs";
    private static final int[] NIGHT_MODES = {
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    };

    private SharedPreferences prefs;
    private EditText editSmsNumber;
    private SwitchMaterial switchFall;

    private final ActivityResultLauncher<String> smsPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) sendSmsReport();
                else Toast.makeText(requireContext(), R.string.sms_permission_required, Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String[]> fallPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                startFallService();
            });

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

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);

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
        switchFall = view.findViewById(R.id.switchFall);

        switchPush.setChecked(prefs.getBoolean("push", true));
        switchSms.setChecked(prefs.getBoolean("sms", true));
        switchEmail.setChecked(prefs.getBoolean("email", true));
        switchFall.setChecked(prefs.getBoolean("fall_detection", false));

        switchPush.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("push", checked).apply());
        switchSms.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("sms", checked).apply());
        switchEmail.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("email", checked).apply());
        switchFall.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("fall_detection", checked).apply();
            if (checked) ensureFallService();
            else stopFallService();
        });

        editSmsNumber = view.findViewById(R.id.editSmsNumber);
        editSmsNumber.setText(prefs.getString("sms_number", ""));
        editSmsNumber.setOnFocusChangeListener((v, has) -> {
            if (!has) prefs.edit().putString("sms_number", editSmsNumber.getText().toString().trim()).apply();
        });

        Button btnSendSmsReport = view.findViewById(R.id.btnSendSmsReport);
        btnSendSmsReport.setOnClickListener(v -> requestAndSendSms());

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

    @Override
    public void onPause() {
        super.onPause();
        if (prefs != null && editSmsNumber != null) {
            prefs.edit().putString("sms_number", editSmsNumber.getText().toString().trim()).apply();
        }
    }

    private void requestAndSendSms() {
        if (!prefs.getBoolean("sms", true)) {
            Toast.makeText(requireContext(), R.string.sms_disabled, Toast.LENGTH_SHORT).show();
            return;
        }
        String number = editSmsNumber.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(requireContext(), R.string.sms_no_number, Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putString("sms_number", number).apply();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            smsPermLauncher.launch(Manifest.permission.SEND_SMS);
            return;
        }
        sendSmsReport();
    }

    private void sendSmsReport() {
        String number = prefs.getString("sms_number", "");
        if (TextUtils.isEmpty(number)) return;

        Handler mainHandler = new Handler(Looper.getMainLooper());
        ProductDao productDao = AppDatabase.getInstance(requireContext()).productDao();

        AppDatabase.databaseExecutor.execute(() -> {
            List<Product> lowStock = productDao.getLowStockProducts();

            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (lowStock.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.sms_no_low_stock, Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder sb = new StringBuilder("MagazynierUZ alert braków:\n");
                for (Product p : lowStock) {
                    sb.append(String.format(Locale.getDefault(),
                            "- %s: %d szt.\n", p.name, p.quantity));
                }
                String text = sb.toString().trim();

                try {
                    SmsManager sms = SmsManager.getDefault();
                    if (text.length() > 160) {
                        sms.sendMultipartTextMessage(number, null, sms.divideMessage(text), null, null);
                    } else {
                        sms.sendTextMessage(number, null, text, null, null);
                    }
                    Toast.makeText(requireContext(), R.string.sms_sent, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), R.string.sms_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void ensureFallService() {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            fallPermLauncher.launch(new String[]{Manifest.permission.POST_NOTIFICATIONS});
            return;
        }
        startFallService();
    }

    private void startFallService() {
        Intent i = new Intent(requireContext(), FallDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(i);
        } else {
            requireContext().startService(i);
        }
    }

    private void stopFallService() {
        requireContext().stopService(new Intent(requireContext(), FallDetectionService.class));
    }
}
