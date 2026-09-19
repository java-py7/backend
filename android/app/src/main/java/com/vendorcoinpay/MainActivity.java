package com.vendorcoinpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vendorcoinpay.screens.HistoryFragment;
import com.vendorcoinpay.screens.HomeFragment;
import com.vendorcoinpay.screens.PayFragment;
import com.vendorcoinpay.screens.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private HomeFragment homeFragment;
    private PayFragment payFragment;
    private HistoryFragment historyFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        homeFragment = new HomeFragment();
        payFragment = new PayFragment();
        historyFragment = new HistoryFragment();
        profileFragment = new ProfileFragment();

        // Default to Home
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
        }

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = homeFragment;
                } else if (itemId == R.id.nav_pay) {
                    selectedFragment = payFragment;
                } else if (itemId == R.id.nav_history) {
                    selectedFragment = historyFragment;
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = profileFragment;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                    return true;
                }
                return false;
            }
        });

        // Check if opened with pre-filled receiver code
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("scanned_receiver_code")) {
            String scannedCode = intent.getStringExtra("scanned_receiver_code");
            switchToPayTab(scannedCode);
        }
    }

    public void switchTab(int menuItemId) {
        bottomNavigation.setSelectedItemId(menuItemId);
    }

    public void switchToPayTab(String receiverCode) {
        if (payFragment == null) {
            payFragment = new PayFragment();
        }
        Bundle bundle = new Bundle();
        bundle.putString("receiver_code", receiverCode);
        payFragment.setArguments(bundle);

        bottomNavigation.setSelectedItemId(R.id.nav_pay);
    }
}
