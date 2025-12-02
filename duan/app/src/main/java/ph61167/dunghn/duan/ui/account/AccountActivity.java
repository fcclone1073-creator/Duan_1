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
            Toast.makeText(this, "Chức năng chỉnh sửa hồ sơ đang phát triển", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Chức năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
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
