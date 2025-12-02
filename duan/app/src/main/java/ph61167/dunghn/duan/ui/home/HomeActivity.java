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
import ph61167.dunghn.duan.data.remote.response.ProductsResponse;
import ph61167.dunghn.duan.databinding.ActivityHomeBinding;
import ph61167.dunghn.duan.ui.auth.LoginActivity;
import ph61167.dunghn.duan.ui.cart.CartActivity;
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
        setupClickListeners();
        fetchProducts();
    }

    private void setupHeader() {
        String userName = sessionManager.getUserName();
        String greeting = userName != null && !userName.isEmpty()
                ? "Xin chào, " + userName + "!"
                : "Xin chào!";
        binding.tvUserGreeting.setText(greeting);
    }

    private void setupClickListeners() {
        binding.ivLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            navigateToLogin();
        });

        binding.ivCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        binding.ivFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Danh sách yêu thích", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to WishlistActivity
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == ph61167.dunghn.duan.R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_cart) {
                startActivity(new Intent(this, ph61167.dunghn.duan.ui.cart.CartActivity.class));
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_orders) {
                startActivity(new Intent(this, ph61167.dunghn.duan.ui.orders.OrdersActivity.class));
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_wallet) {
                Toast.makeText(this, "Tính năng ví đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_account) {
                startActivity(new Intent(this, ph61167.dunghn.duan.ui.account.AccountActivity.class));
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
                .enqueue(new Callback<BaseResponse<ProductsResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<ProductsResponse>> call,
                            Response<BaseResponse<ProductsResponse>> response
                    ) {
                        showProductLoading(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(HomeActivity.this,
                                    "Không thể tải sản phẩm",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        BaseResponse<ProductsResponse> body = response.body();
                        if (body.isSuccess() && body.getData() != null) {
                            List<Product> products = body.getData().getProducts();
                            if (products != null) {
                                productAdapter.submitList(products);
                            }
                        } else {
                            Toast.makeText(HomeActivity.this,
                                    body.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<ProductsResponse>> call, Throwable t) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // Set home item as selected when returning to this activity
        binding.bottomNavigation.setSelectedItemId(ph61167.dunghn.duan.R.id.nav_home);
    }
}
