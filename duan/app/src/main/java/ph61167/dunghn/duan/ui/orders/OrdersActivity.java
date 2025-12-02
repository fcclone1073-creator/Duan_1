package ph61167.dunghn.duan.ui.orders;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.model.Order;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityOrdersBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private SessionManager sessionManager;
    private OrdersAdapter ordersAdapter;
    private String currentStatusFilter = "all";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        setupRecyclerView();
        setupClickListeners();
        setupTabLayout();
        fetchOrders();
    }

    private void setupRecyclerView() {
        ordersAdapter = new OrdersAdapter();
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(ordersAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        if (binding.btnShopNow != null) {
            binding.btnShopNow.setOnClickListener(v -> {
                finish();
            });
        }
    }

    private void setupTabLayout() {
        if (binding.tabLayout != null) {
            binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    switch (position) {
                        case 0:
                            currentStatusFilter = "all";
                            break;
                        case 1:
                            currentStatusFilter = Order.STATUS_PENDING;
                            break;
                        case 2:
                            currentStatusFilter = Order.STATUS_SHIPPING;
                            break;
                        case 3:
                            currentStatusFilter = Order.STATUS_DELIVERED;
                            break;
                        case 4:
                            currentStatusFilter = Order.STATUS_CANCELLED;
                            break;
                    }
                    fetchOrders();
                }

                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
        }
    }

    private void fetchOrders() {
        showLoading(true);
        String userId = sessionManager.getUserId();
        if (userId == null) {
            showLoading(false);
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiClient.getService()
                .getOrdersByUser(userId)
                .enqueue(new Callback<BaseResponse<List<Order>>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<List<Order>>> call,
                            Response<BaseResponse<List<Order>>> response
                    ) {
                        showLoading(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmptyState(true);
                            Toast.makeText(OrdersActivity.this,
                                    "Không thể tải đơn hàng",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        BaseResponse<List<Order>> body = response.body();
                        if (body.isSuccess() && body.getData() != null) {
                            List<Order> orders = body.getData();
                            
                            // Filter by status
                            if (!"all".equals(currentStatusFilter)) {
                                java.util.List<Order> filtered = new java.util.ArrayList<>();
                                for (Order order : orders) {
                                    if (currentStatusFilter.equals(order.getStatus())) {
                                        filtered.add(order);
                                    }
                                }
                                orders = filtered;
                            }

                            if (orders != null && !orders.isEmpty()) {
                                ordersAdapter.submitList(orders);
                                showEmptyState(false);
                            } else {
                                ordersAdapter.submitList(java.util.Collections.emptyList());
                                showEmptyState(true);
                            }
                        } else {
                            ordersAdapter.submitList(java.util.Collections.emptyList());
                            showEmptyState(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<List<Order>>> call, Throwable t) {
                        showLoading(false);
                        showEmptyState(true);
                        Log.e("OrdersActivity", "Error fetching orders: " + t.getMessage(), t);
                        Toast.makeText(OrdersActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        binding.rvOrders.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        if (binding.emptyState != null) {
            binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        binding.rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrders();
    }
}
