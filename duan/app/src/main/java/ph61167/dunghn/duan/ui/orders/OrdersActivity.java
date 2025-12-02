package ph61167.dunghn.duan.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityOrdersBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private SessionManager sessionManager;
    private OrdersAdapter ordersAdapter;

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
        fetchOrders();
    }

    private void setupRecyclerView() {
        ordersAdapter = new OrdersAdapter();
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(ordersAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
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

        // TODO: Implement getOrders API
        Toast.makeText(this, "API đơn hàng đang được phát triển", Toast.LENGTH_SHORT).show();
        showLoading(false);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.rvOrders.setVisibility(View.INVISIBLE);
        } else {
            binding.rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrders();
    }
}

