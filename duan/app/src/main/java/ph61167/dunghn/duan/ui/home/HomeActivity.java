package ph61167.dunghn.duan.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.model.Product;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityHomeBinding;
import ph61167.dunghn.duan.ui.auth.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SessionManager sessionManager;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupRecyclerView();
        setupHeader();
        setupBottomNavigation();
        fetchProducts();
    }

    private void setupHeader() {
        String greeting = sessionManager.getUserName() != null
                ? "Xin chào, " + sessionManager.getUserName()
                : "Xin chào!";
        binding.tvUserGreeting.setText(greeting);

        binding.ivLogout.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, binding.ivLogout);
            android.view.Menu m = popup.getMenu();
            final int ID_PROFILE = 1;
            final int ID_SETTINGS = 2;
            final int ID_TOP_PRODUCTS = 3;
            final int ID_TOP_CUSTOMERS = 4;
            final int ID_LOGOUT = 5;
            m.add(0, ID_PROFILE, 0, "Tài khoản cá nhân");
            m.add(0, ID_SETTINGS, 1, "Cài đặt");
            m.add(0, ID_TOP_PRODUCTS, 2, "Báo cáo Top Sản phẩm");
            m.add(0, ID_TOP_CUSTOMERS, 3, "Báo cáo Top Khách hàng");
            m.add(0, ID_LOGOUT, 4, "Đăng xuất");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == ID_PROFILE) {
                    Intent i = new Intent(this, ph61167.dunghn.duan.ui.users.UsersActivity.class);
                    startActivity(i);
                    return true;
                } else if (id == ID_SETTINGS) {
                    Toast.makeText(this, "Tính năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == ID_TOP_PRODUCTS) {
                    Intent i = new Intent(this, ph61167.dunghn.duan.ui.top.TopProductsActivity.class);
                    startActivity(i);
                    return true;
                } else if (id == ID_TOP_CUSTOMERS) {
                    Intent i = new Intent(this, ph61167.dunghn.duan.ui.top.TopCustomersActivity.class);
                    startActivity(i);
                    return true;
                } else if (id == ID_LOGOUT) {
                    sessionManager.clearSession();
                    navigateToLogin();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        binding.ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, ph61167.dunghn.duan.ui.cart.CartActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == ph61167.dunghn.duan.R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_cart) {
                Intent intent = new Intent(this, ph61167.dunghn.duan.ui.cart.CartActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_orders) {
                Intent intent = new Intent(this, ph61167.dunghn.duan.ui.orders.OrdersActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_wallet) {
                // TODO: Chuyển đến trang ví
                Toast.makeText(this, "Tính năng ví đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_account) {
                Intent intent = new Intent(this, ph61167.dunghn.duan.ui.users.UsersActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(productAdapter);
    }

    private void fetchProducts() {
        showProductLoading(true);
        ApiClient.getService()
                .getProducts()
                .enqueue(new Callback<BaseResponse<List<Product>>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<List<Product>>> call,
                            Response<BaseResponse<List<Product>>> response
                    ) {
                        showProductLoading(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(HomeActivity.this,
                                    "Không thể tải sản phẩm",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        BaseResponse<List<Product>> body = response.body();
                        if (body.isSuccess()) {
                            productAdapter.submitList(body.getData());
                        } else {
                            Toast.makeText(HomeActivity.this,
                                    body.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<List<Product>>> call, Throwable t) {
                        showProductLoading(false);
                        Toast.makeText(HomeActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProductLoading(boolean isLoading) {
        binding.progressProducts.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.rvProducts.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

