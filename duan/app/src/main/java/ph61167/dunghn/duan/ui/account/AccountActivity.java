package ph61167.dunghn.duan.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.databinding.ActivityAccountBinding;
import ph61167.dunghn.duan.ui.auth.LoginActivity;
import ph61167.dunghn.duan.ui.orders.OrdersActivity;
import ph61167.dunghn.duan.ui.wishlist.WishlistActivity;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupUserInfo();
        setupClickListeners();
    }

    private void setupUserInfo() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name != null && !name.isEmpty()) {
            binding.tvUserName.setText(name);
            
            // Set avatar initial
            if (binding.tvAvatarInitial != null) {
                String initial = name.substring(0, 1).toUpperCase();
                binding.tvAvatarInitial.setText(initial);
            }
        } else {
            binding.tvUserName.setText("Người dùng");
        }
        
        if (email != null && !email.isEmpty()) {
            binding.tvUserEmail.setText(email);
        } else {
            binding.tvUserEmail.setText("Chưa có email");
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            navigateToLogin();
        });

        // Edit profile
        binding.llEditProfile.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        // My orders
        binding.llMyOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
        });

        // Favorites
        binding.llFavorites.setOnClickListener(v -> {
            startActivity(new Intent(this, WishlistActivity.class));
        });

        // Settings
        binding.llSettings.setOnClickListener(v -> {
            showChangePasswordDialog();
        });
    }

    private void showEditProfileDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa hồ sơ");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Tên");
        etName.setText(sessionManager.getUserName());
        etName.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        android.widget.EditText etEmail = new android.widget.EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(sessionManager.getUserEmail());
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        layout.addView(etName);
        layout.addView(etEmail);

        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(name, email);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateProfile(String name, String email) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ph61167.dunghn.duan.data.model.User user = new ph61167.dunghn.duan.data.model.User();
        user.setName(name);
        user.setEmail(email);

        ph61167.dunghn.duan.data.remote.ApiClient.getService()
                .updateUser(userId, user)
                .enqueue(new retrofit2.Callback<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>> call,
                            retrofit2.Response<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User> body = response.body();
                            if (body.isSuccess()) {
                                // Update session
                                sessionManager.saveUserName(name);
                                sessionManager.saveUserEmail(email);
                                
                                Toast.makeText(AccountActivity.this,
                                        "Cập nhật hồ sơ thành công",
                                        Toast.LENGTH_SHORT).show();
                                setupUserInfo(); // Refresh UI
                            } else {
                                Toast.makeText(AccountActivity.this,
                                        body.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>> call,
                            Throwable t
                    ) {
                        Toast.makeText(AccountActivity.this,
                                "Lỗi khi cập nhật hồ sơ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        android.widget.EditText etOldPassword = new android.widget.EditText(this);
        etOldPassword.setHint("Mật khẩu cũ");
        etOldPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etOldPassword.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        android.widget.EditText etNewPassword = new android.widget.EditText(this);
        etNewPassword.setHint("Mật khẩu mới");
        etNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPassword.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        android.widget.EditText etConfirmPassword = new android.widget.EditText(this);
        etConfirmPassword.setHint("Xác nhận mật khẩu mới");
        etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmPassword.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        layout.addView(etOldPassword);
        layout.addView(etNewPassword);
        layout.addView(etConfirmPassword);

        builder.setView(layout);
        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(newPassword);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void changePassword(String newPassword) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ph61167.dunghn.duan.data.model.User user = new ph61167.dunghn.duan.data.model.User();
        user.setPassword(newPassword);

        ph61167.dunghn.duan.data.remote.ApiClient.getService()
                .updateUser(userId, user)
                .enqueue(new retrofit2.Callback<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>> call,
                            retrofit2.Response<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User> body = response.body();
                            if (body.isSuccess()) {
                                Toast.makeText(AccountActivity.this,
                                        "Đổi mật khẩu thành công",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountActivity.this,
                                        body.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<ph61167.dunghn.duan.data.remote.response.BaseResponse<ph61167.dunghn.duan.data.model.User>> call,
                            Throwable t
                    ) {
                        Toast.makeText(AccountActivity.this,
                                "Lỗi khi đổi mật khẩu",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUserInfo(); // Refresh user info
    }
}
