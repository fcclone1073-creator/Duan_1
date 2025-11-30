package ph61167.dunghn.duan.ui.cart;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.model.CartData;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityCartBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private CartAdapter cartAdapter;
    private SessionManager sessionManager;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        setupRecyclerView();
        setupSelectAll();
        fetchCart();

        binding.btnCheckout.setOnClickListener(v -> {
            Toast.makeText(this, "Tiến hành thanh toán", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter();
        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(cartAdapter);
        cartAdapter.setOnQuantityChangeListener((item, newQuantity) -> {
            // Optional: call API update quantity here
            // Cập nhật tạm thời số lượng để tính tổng
            itemQuantityUpdate(item, newQuantity);
            updateTotal();
        });
        cartAdapter.setOnDeleteClickListener(item -> deleteItem(item));
        cartAdapter.setOnSelectionChangeListener(this::updateTotal);
    }

    private void fetchCart() {
        showLoading(true);
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getService().getCart(userId).enqueue(new Callback<BaseResponse<CartData>>() {
            @Override
            public void onResponse(Call<BaseResponse<CartData>> call, Response<BaseResponse<CartData>> response) {
                showLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CartActivity.this, "Không thể tải giỏ hàng (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                BaseResponse<CartData> body = response.body();
                if (body.isSuccess() && body.getData() != null) {
                    CartData data = body.getData();
                    cartAdapter.submitList(data.getItems());
                    binding.cbSelectAll.setChecked(false);
                    updateTotal();
                    boolean isEmpty = data.getItems() == null || data.getItems().isEmpty();
                    binding.rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                } else {
                    Toast.makeText(CartActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<CartData>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal() {
        double total = 0;
        for (CartData.Item it : cartAdapter.getSelectedItems()) {
            total += it.getUnitPrice() * it.getQuantity();
        }
        binding.tvTotalPrice.setText(currencyFormat.format(total));
    }

    private void setupSelectAll() {
        binding.cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cartAdapter.selectAll();
            } else {
                cartAdapter.clearSelection();
            }
            updateTotal();
        });
    }

    private void itemQuantityUpdate(CartData.Item target, int newQuantity) {
        for (CartData.Item it : cartAdapter.getItems()) {
            if (it == target) {
                try {
                    java.lang.reflect.Field f = it.getClass().getDeclaredField("quantity");
                    f.setAccessible(true);
                    f.setInt(it, newQuantity);
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void deleteItem(CartData.Item item) {
        String userId = sessionManager.getUserId();
        String productId = item.getProduct() != null ? item.getProduct().getId() : null;
        if (userId == null || productId == null) {
            Toast.makeText(this, "Thiếu thông tin xóa item", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        ApiClient.getService().deleteCartItem(productId, userId).enqueue(new Callback<BaseResponse<CartData>>() {
            @Override
            public void onResponse(Call<BaseResponse<CartData>> call, Response<BaseResponse<CartData>> response) {
                showLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CartActivity.this, "Không thể xóa (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                BaseResponse<CartData> body = response.body();
                if (body.isSuccess() && body.getData() != null) {
                    CartData data = body.getData();
                    cartAdapter.submitList(data.getItems());
                    binding.tvTotalPrice.setText(currencyFormat.format(data.getTotalAmount()));
                    Toast.makeText(CartActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<CartData>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.rvCart.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }
}
