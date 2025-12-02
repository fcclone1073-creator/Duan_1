package ph61167.dunghn.duan.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.request.LoginRequest;
import ph61167.dunghn.duan.data.remote.response.AuthData;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityLoginBinding;
import ph61167.dunghn.duan.ui.home.HomeActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = binding.etAccount.getText() != null
                ? binding.etAccount.getText().toString().trim()
                : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(email)) {
            binding.etAccount.setError("Email không được để trống");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Mật khẩu không được để trống");
            return;
        }

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        showLoading(true);
        Log.d("LoginActivity", "Attempting login with email: " + email);
        
        ApiClient.getService()
                .login(new LoginRequest(email, password))
                .enqueue(new Callback<BaseResponse<AuthData>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<AuthData>> call,
                            Response<BaseResponse<AuthData>> response
                    ) {
                        showLoading(false);
                        
                        Log.d("LoginActivity", "Response code: " + response.code());
                        Log.d("LoginActivity", "Response successful: " + response.isSuccessful());
                        
                        // Xử lý response body (có thể có body ngay cả khi không successful)
                        BaseResponse<AuthData> body = response.body();
                        
                        if (body != null) {
                            Log.d("LoginActivity", "Response body - success: " + body.isSuccess() + ", message: " + body.getMessage());
                            
                            // Có response body
                            if (body.isSuccess() && body.getData() != null) {
                                // Đăng nhập thành công
                                Log.d("LoginActivity", "Login successful");
                                sessionManager.saveSession(body.getData());
                                Toast.makeText(LoginActivity.this,
                                        body.getMessage() != null ? body.getMessage() : "Đăng nhập thành công",
                                        Toast.LENGTH_SHORT).show();
                                navigateToHome();
                            } else {
                                // Đăng nhập thất bại (sai email/password)
                                String errorMsg = body.getMessage();
                                if (errorMsg == null || errorMsg.isEmpty()) {
                                    errorMsg = "Email hoặc mật khẩu không đúng";
                                }
                                Log.d("LoginActivity", "Login failed: " + errorMsg);
                                Toast.makeText(LoginActivity.this,
                                        errorMsg,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (!response.isSuccessful()) {
                            // Response không thành công - thử parse error body
                            String errorMsg = "Không thể đăng nhập. Vui lòng thử lại.";
                            
                            try {
                                // Thử đọc error body
                                okhttp3.ResponseBody errorBody = response.errorBody();
                                if (errorBody != null) {
                                    String errorBodyString = errorBody.string();
                                    Log.d("LoginActivity", "Error body: " + errorBodyString);
                                    
                                    // Thử parse JSON error response
                                    try {
                                        com.google.gson.JsonObject jsonObject = new com.google.gson.Gson()
                                                .fromJson(errorBodyString, com.google.gson.JsonObject.class);
                                        if (jsonObject.has("message")) {
                                            errorMsg = jsonObject.get("message").getAsString();
                                        } else if (jsonObject.has("error")) {
                                            errorMsg = jsonObject.get("error").getAsString();
                                        }
                                    } catch (Exception e) {
                                        Log.d("LoginActivity", "Could not parse error JSON");
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("LoginActivity", "Error reading error body: " + e.getMessage());
                            }
                            
                            // Fallback messages based on status code
                            if (errorMsg.equals("Không thể đăng nhập. Vui lòng thử lại.")) {
                                if (response.code() == 401) {
                                    errorMsg = "Email hoặc mật khẩu không đúng";
                                } else if (response.code() == 500) {
                                    errorMsg = "Lỗi server. Vui lòng thử lại sau.";
                                } else if (response.code() == 400) {
                                    errorMsg = "Thông tin đăng nhập không hợp lệ";
                                }
                            }
                            
                            Log.e("LoginActivity", "Response error: " + response.code() + " - " + errorMsg);
                            Toast.makeText(LoginActivity.this,
                                    errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Response thành công nhưng body null
                            Log.e("LoginActivity", "Response successful but body is null");
                            Toast.makeText(LoginActivity.this,
                                    "Không thể đăng nhập. Vui lòng thử lại.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<AuthData>> call, Throwable t) {
                        showLoading(false);
                        Log.e("LoginActivity", "Network error: " + t.getMessage(), t);
                        String errorMsg = "Lỗi kết nối";
                        if (t.getMessage() != null) {
                            if (t.getMessage().contains("Failed to connect") || t.getMessage().contains("Unable to resolve host")) {
                                errorMsg = "Không thể kết nối đến server. Kiểm tra kết nối mạng.";
                            } else {
                                errorMsg = "Lỗi kết nối: " + t.getMessage();
                            }
                        }
                        Toast.makeText(LoginActivity.this,
                                errorMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
    }

    private void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}

