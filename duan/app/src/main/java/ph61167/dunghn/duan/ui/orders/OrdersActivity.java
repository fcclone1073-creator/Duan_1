package ph61167.dunghn.duan.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.model.OrderDetail;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.OrdersListData;
import ph61167.dunghn.duan.databinding.ActivityOrdersBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private OrderAdapter orderAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        setupHeader();
        setupRecyclerView();
        setupRefresh();
        fetchOrders();
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter();
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(orderAdapter);
    }

    private void fetchOrders() {
        showLoading(true);
        String userId = sessionManager.getUserId();
        ApiClient.getService().getUserOrders(userId).enqueue(new Callback<BaseResponse<OrdersListData>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrdersListData>> call, Response<BaseResponse<OrdersListData>> response) {
                showLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OrdersActivity.this, "Không thể tải đơn hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                BaseResponse<OrdersListData> body = response.body();
                if (body.isSuccess()) {
                    List<OrderDetail> items = body.getData() != null ? body.getData().getItems() : null;
                    orderAdapter.submitList(items);
                    boolean isEmpty = items == null || items.isEmpty();
                    binding.viewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                } else {
                    Toast.makeText(OrdersActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrdersListData>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OrdersActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.swipeRefresh.setRefreshing(isLoading);
    }

    private void setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::fetchOrders);
    }
}
