package com.example.magazynieruz_mobile.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.AppNotification;
import com.example.magazynieruz_mobile.data.NotificationDao;
import com.example.magazynieruz_mobile.util.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationAdapter adapter;
    private TextView textEmpty;
    private NotificationDao notificationDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> postNotifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) sendTestPush();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationDao = AppDatabase.getInstance(requireContext()).notificationDao();
        textEmpty = view.findViewById(R.id.textEmptyNotifs);

        RecyclerView recycler = view.findViewById(R.id.recyclerNotifications);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        Button btnTestPush = view.findViewById(R.id.btnTestPush);
        Button btnClear = view.findViewById(R.id.btnClearNotifs);

        btnTestPush.setOnClickListener(v -> requestNotifAndPush());
        btnClear.setOnClickListener(v -> AppDatabase.databaseExecutor.execute(() -> {
            notificationDao.clear();
            mainHandler.post(this::reload);
        }));

        NotificationHelper.ensureChannels(requireContext());
        reload();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) reload();
    }

    private void reload() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<AppNotification> all = notificationDao.getAll();
            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.update(all);
                textEmpty.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void requestNotifAndPush() {
        SharedPreferences prefs = requireContext().getSharedPreferences("magazynier_prefs", 0);
        if (!prefs.getBoolean("push", true)) {
            Toast.makeText(requireContext(), R.string.push_disabled, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                postNotifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        sendTestPush();
    }

    private void sendTestPush() {
        String title = getString(R.string.push_test_title);
        String text = getString(R.string.push_test_message);
        int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        NotificationHelper.postPush(requireContext(), id, title, text);

        AppNotification n = new AppNotification();
        n.title = title;
        n.message = text;
        n.createdAt = System.currentTimeMillis();
        AppDatabase.databaseExecutor.execute(() -> {
            notificationDao.insert(n);
            mainHandler.post(this::reload);
        });
    }
}
