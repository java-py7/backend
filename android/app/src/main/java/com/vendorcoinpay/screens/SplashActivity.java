package com.vendorcoinpay.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.vendorcoinpay.MainActivity;
import com.vendorcoinpay.R;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.User;
import com.vendorcoinpay.storage.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        // Allow splash logo to display briefly
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSession();
            }
        }, 1200);
    }

    private void checkSession() {
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        // Validate token with backend
        ApiService apiService = ApiClient.getService(this);
        apiService.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.updateUserData(response.body());
                    goToMain();
                } else {
                    // Token invalid or expired
                    sessionManager.logout();
                    goToLogin();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // If network is offline but user has saved session, allow opening Main
                goToMain();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
