package com.vendorcoinpay.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.vendorcoinpay.models.User;

public class SessionManager {
    private static final String PREF_NAME = "VendorCoinPaySession";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_CODE = "user_code";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CACHED_BALANCE = "cached_balance";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, User user) {
        editor.putString(KEY_TOKEN, token);
        if (user != null) {
            editor.putInt(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_CODE, user.getUserCode());
            editor.putString(KEY_FULL_NAME, user.getFullName());
            editor.putString(KEY_USERNAME, user.getUsername());
            editor.putString(KEY_PHONE, user.getPhone());
            editor.putInt(KEY_CACHED_BALANCE, user.getBalance());
        }
        editor.apply();
    }

    public void updateUserData(User user) {
        if (user != null) {
            editor.putInt(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_CODE, user.getUserCode());
            editor.putString(KEY_FULL_NAME, user.getFullName());
            editor.putString(KEY_USERNAME, user.getUsername());
            editor.putString(KEY_PHONE, user.getPhone());
            editor.putInt(KEY_CACHED_BALANCE, user.getBalance());
            editor.apply();
        }
    }

    public void updateBalance(int balance) {
        editor.putInt(KEY_CACHED_BALANCE, balance);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public String getUserCode() {
        return prefs.getString(KEY_USER_CODE, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "User");
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public int getCachedBalance() {
        return prefs.getInt(KEY_CACHED_BALANCE, 0);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
