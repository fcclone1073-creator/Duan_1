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
import ph61167.dunghn.duan.data.remote.ApiService;
import ph61167.dunghn.duan.data.remote.request.FavoriteRequest;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.ProductsResponse;
import ph61167.dunghn.duan.databinding.ActivityHomeBinding;
import ph61167.dunghn.duan.ui.auth.LoginActivity;
import ph61167.dunghn.duan.ui.cart.CartActivity;
import ph61167.dunghn.duan.ui.wishlist.WishlistActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SessionManager sessionManager;
    private ProductAdapter productAdapter;
    private java.util.List<Product> allProducts = new java.util.ArrayList<>();
    private java.util.List<String> categories = new java.util.ArrayList<>();

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
        setupSearchAndFilter();
        fetchProducts();
        fetchCategories();
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
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, binding.ivLogout);
            android.view.Menu m = popup.getMenu();
            final int ID_PROFILE = 1;
            final int ID_SETTINGS = 2;
            final int ID_LOGOUT = 3;
            m.add(0, ID_PROFILE, 0, "Tài khoản cá nhân");
            m.add(0, ID_SETTINGS, 1, "Cài đặt");
            m.add(0, ID_LOGOUT, 2, "Đăng xuất");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == ID_PROFILE) {
                    Intent i = new Intent(this, ph61167.dunghn.duan.ui.users.UsersActivity.class);
                    startActivity(i);
                    return true;
                } else if (id == ID_SETTINGS) {
                    Toast.makeText(this, "Tính năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
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

        binding.ivCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        binding.ivFavorite.setOnClickListener(v -> {
            startActivity(new Intent(this, WishlistActivity.class));
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
                startActivity(new Intent(this, ph61167.dunghn.duan.ui.cart.CartActivity.class));
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_orders) {
                startActivity(new Intent(this, ph61167.dunghn.duan.ui.orders.OrdersActivity.class));
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_wallet) {
                Toast.makeText(this, "Tính năng ví đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == ph61167.dunghn.duan.R.id.nav_account) {
                Intent intent = new Intent(this, ph61167.dunghn.duan.ui.users.UsersActivity.class);
                startActivity(intent);
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

    private void setupSearchAndFilter() {
        // Search functionality
        if (binding.etSearch != null) {
            binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
                performSearch();
                return true;
            });
        }

        // Filter button
        if (binding.ivFilter != null) {
            binding.ivFilter.setOnClickListener(v -> showFilterDialog());
        }
    }

    private void performSearch() {
        String query = binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "";
        filterProducts(query, null);
    }

    private void filterProducts(String searchQuery, String categoryId) {
        java.util.List<Product> filtered = new java.util.ArrayList<>();
        
        for (Product product : allProducts) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (product.getName() != null && product.getName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchQuery.toLowerCase()));
            
            boolean matchesCategory = categoryId == null || 
                    categoryId.isEmpty() ||
                    (product.getCategory() != null && product.getCategory().getId() != null && 
                     product.getCategory().getId().equals(categoryId));
            
            if (matchesSearch && matchesCategory) {
                filtered.add(product);
            }
        }
        
        productAdapter.submitList(filtered);
    }

    private void showFilterDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Lọc sản phẩm");
        
        String[] categoryArray = new String[categories.size() + 1];
        categoryArray[0] = "Tất cả";
        for (int i = 0; i < categories.size(); i++) {
            categoryArray[i + 1] = categories.get(i);
        }
        
        builder.setItems(categoryArray, (dialog, which) -> {
            if (which == 0) {
                filterProducts(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "", null);
            } else {
                // Get category ID from the selected category name
                // This is simplified - you may need to fetch category IDs from API
                filterProducts(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "", null);
            }
        });
        builder.show();
    }

    private void fetchCategories() {
        // Extract unique categories from products
        java.util.Set<String> categorySet = new java.util.HashSet<>();
        for (Product product : allProducts) {
            if (product.getCategory() != null && product.getCategory().getName() != null) {
                categorySet.add(product.getCategory().getName());
            }
        }
        categories = new java.util.ArrayList<>(categorySet);
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
