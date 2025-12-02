package ph61167.dunghn.duan.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.CartResponse;
import ph61167.dunghn.duan.databinding.ActivityCartBinding;
import ph61167.dunghn.duan.ui.checkout.CheckoutActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private SessionManager sessionManager;
    private CartAdapter cartAdapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        setupRecyclerView();
        setupClickListeners();
        fetchCart();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this::onItemDeleted, this::onQuantityChanged);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCheckout.setOnClickListener(v -> {
            if (cartAdapter.getItemCount() == 0) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navigate to checkout
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void fetchCart() {
        showLoading(true);
        String userId = sessionManager.getUserId();
        if (userId == null) {
            showLoading(false);
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiClient.getService()
                .getCart(userId)
                .enqueue(new Callback<BaseResponse<CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<CartResponse>> call,
                            Response<BaseResponse<CartResponse>> response
                    ) {
                        showLoading(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmptyState(true);
                            Toast.makeText(CartActivity.this,
                                    "Không thể tải giỏ hàng",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        BaseResponse<CartResponse> body = response.body();
                        if (body.isSuccess() && body.getData() != null) {
                            CartResponse cartData = body.getData();
                            if (cartData.getItems() != null && !cartData.getItems().isEmpty()) {
                                cartAdapter.submitList(cartData.getItems());
                                updateTotalPrice(cartData.getTotalAmount());
                                showEmptyState(false);
                            } else {
                                cartAdapter.submitList(java.util.Collections.emptyList());
                                updateTotalPrice(0);
                                showEmptyState(true);
                            }
                        } else {
                            cartAdapter.submitList(java.util.Collections.emptyList());
                            updateTotalPrice(0);
                            showEmptyState(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CartResponse>> call, Throwable t) {
                        showLoading(false);
                        showEmptyState(true);
                        Log.e("CartActivity", "Error fetching cart: " + t.getMessage(), t);
                        Toast.makeText(CartActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onItemDeleted(String productId) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        ApiClient.getService()
                .removeCartItem(userId, productId)
                .enqueue(new Callback<BaseResponse<CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<CartResponse>> call,
                            Response<BaseResponse<CartResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<CartResponse> body = response.body();
                            if (body.isSuccess() && body.getData() != null) {
                                CartResponse cartData = body.getData();
                                cartAdapter.submitList(cartData.getItems());
                                updateTotalPrice(cartData.getTotalAmount());
                                if (cartData.getItems() == null || cartData.getItems().isEmpty()) {
                                    showEmptyState(true);
                                }
                                Toast.makeText(CartActivity.this,
                                        "Đã xóa sản phẩm",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        fetchCart(); // Refresh cart
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CartResponse>> call, Throwable t) {
                        Toast.makeText(CartActivity.this,
                                "Lỗi khi xóa sản phẩm",
                                Toast.LENGTH_SHORT).show();
                        fetchCart(); // Refresh on error
                    }
                });
    }

    private void onQuantityChanged(String productId, int newQuantity) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        ApiClient.getService()
                .updateCartItem(userId, productId, new ph61167.dunghn.duan.data.remote.ApiService.UpdateCartItemRequest(newQuantity))
                .enqueue(new Callback<BaseResponse<CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<CartResponse>> call,
                            Response<BaseResponse<CartResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<CartResponse> body = response.body();
                            if (body.isSuccess() && body.getData() != null) {
                                CartResponse cartData = body.getData();
                                cartAdapter.submitList(cartData.getItems());
                                updateTotalPrice(cartData.getTotalAmount());
                            }
                        } else {
                            fetchCart(); // Refresh on error
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CartResponse>> call, Throwable t) {
                        Toast.makeText(CartActivity.this,
                                "Lỗi khi cập nhật số lượng",
                                Toast.LENGTH_SHORT).show();
                        fetchCart(); // Refresh on error
                    }
                });
    }

    private void updateTotalPrice(double totalAmount) {
        binding.tvTotalPrice.setText(currencyFormat.format(totalAmount));
        binding.tvSubtotal.setText(currencyFormat.format(totalAmount));
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.rvCart.setVisibility(View.INVISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        } else {
            binding.rvCart.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvCart.setVisibility(View.GONE);
            binding.btnCheckout.setEnabled(false);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.rvCart.setVisibility(View.VISIBLE);
            binding.btnCheckout.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCart();
    }
}

