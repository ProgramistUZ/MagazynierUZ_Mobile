package com.example.magazynieruz_mobile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.magazynieruz_mobile.ui.HomeFragment;
import com.example.magazynieruz_mobile.ui.NotificationsFragment;
import com.example.magazynieruz_mobile.ui.ScanFragment;
import com.example.magazynieruz_mobile.ui.SettingsFragment;
import com.example.magazynieruz_mobile.ui.WarehouseFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private String username;
    private Fragment activeFragment;
    private Fragment homeFragment;
    private Fragment warehouseFragment;
    private Fragment scanFragment;
    private Fragment notificationsFragment;
    private Fragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = getIntent().getStringExtra("username");

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            homeFragment = HomeFragment.newInstance(username);
            warehouseFragment = new WarehouseFragment();
            scanFragment = new ScanFragment();
            notificationsFragment = new NotificationsFragment();
            settingsFragment = new SettingsFragment();

            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment);
            tx.add(R.id.fragmentContainer, notificationsFragment, "notifications").hide(notificationsFragment);
            tx.add(R.id.fragmentContainer, scanFragment, "scan").hide(scanFragment);
            tx.add(R.id.fragmentContainer, warehouseFragment, "warehouse").hide(warehouseFragment);
            tx.add(R.id.fragmentContainer, homeFragment, "home");
            tx.commit();
            activeFragment = homeFragment;
        } else {
            FragmentManager fm = getSupportFragmentManager();
            homeFragment = fm.findFragmentByTag("home");
            warehouseFragment = fm.findFragmentByTag("warehouse");
            scanFragment = fm.findFragmentByTag("scan");
            notificationsFragment = fm.findFragmentByTag("notifications");
            settingsFragment = fm.findFragmentByTag("settings");
            activeFragment = homeFragment != null && homeFragment.isVisible() ? homeFragment
                    : warehouseFragment != null && warehouseFragment.isVisible() ? warehouseFragment
                    : scanFragment != null && scanFragment.isVisible() ? scanFragment
                    : notificationsFragment != null && notificationsFragment.isVisible() ? notificationsFragment
                    : settingsFragment;
        }

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return switchTo(homeFragment);
            if (id == R.id.nav_warehouse) return switchTo(warehouseFragment);
            if (id == R.id.nav_scan) return switchTo(scanFragment);
            if (id == R.id.nav_notifications) return switchTo(notificationsFragment);
            if (id == R.id.nav_settings) return switchTo(settingsFragment);
            return false;
        });
    }

    private boolean switchTo(Fragment target) {
        if (target == null || target == activeFragment) return true;
        getSupportFragmentManager()
                .beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
        return true;
    }
}
