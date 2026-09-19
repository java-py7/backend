package com.vendorcoinpay.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.vendorcoinpay.MainActivity;
import com.vendorcoinpay.R;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.TokenResponse;
import com.vendorcoinpay.models.UserLoginRequest;
import com.vendorcoinpay.storage.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsernameOrPhone;
    private EditText etPassword;
    private AppCompatButton btnLogin;
    private ProgressBar pbLoginLoading;
    private TextView tvErrorMessage;
    private TextView tvGoToRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        etUsernameOrPhone = findViewById(R.id.etUsernameOrPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoginLoading = findViewById(R.id.pbLoginLoading);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String ident = etUsernameOrPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (ident.isEmpty()) {
            showError("Please enter your username or phone number.");
            etUsernameOrPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password.");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);
        tvErrorMessage.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getService(this);
        UserLoginRequest request = new UserLoginRequest(ident, password);

        apiService.login(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokenResp = response.body();
                    sessionManager.saveSession(tokenResp.getAccessToken(), tokenResp.getUser());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String error = ApiClient.getErrorMessage(response);
                    showError(error);
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                setLoading(false);
                showError("Unable to connect to server. Please check your network connection.");
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("");
            pbLoginLoading.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText(R.string.btn_login);
            pbLoginLoading.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }
}
