package ph61167.dunghn.duan.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import ph61167.dunghn.duan.data.remote.response.AuthData;

public class SessionManager {

    private static final String PREF_NAME = "duan_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(AuthData data) {
        if (data == null || data.getUser() == null) return;

        preferences.edit()
                .putString(KEY_TOKEN, data.getToken())
                .putString(KEY_USER_ID, data.getUser().getId())
                .putString(KEY_USER_NAME, data.getUser().getName())
                .putString(KEY_USER_EMAIL, data.getUser().getEmail())
                .apply();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getUserId() != null;
    }

    @Nullable
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    @Nullable
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    @Nullable
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }

    @Nullable
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public void saveUserName(String name) {
        preferences.edit().putString(KEY_USER_NAME, name).apply();
    }

    public void saveUserEmail(String email) {
        preferences.edit().putString(KEY_USER_EMAIL, email).apply();
    }
}

