package ph61167.dunghn.duan.ui.orders;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.data.model.OrderDetail;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityOrderDetailBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private OrderItemsAdapter itemsAdapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        itemsAdapter = new OrderItemsAdapter();
        binding.rvItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItems.setAdapter(itemsAdapter);

        String id = getIntent().getStringExtra("order_id");
        if (TextUtils.isEmpty(id)) {
            finish();
            return;
        }
        fetchDetail(id);
    }

    private void fetchDetail(String id) {
        ApiClient.getService().getOrderDetail(id).enqueue(new Callback<BaseResponse<OrderDetail>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderDetail>> call, Response<BaseResponse<OrderDetail>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OrderDetailActivity.this, "Không thể tải chi tiết đơn", Toast.LENGTH_SHORT).show();
                    return;
                }
                BaseResponse<OrderDetail> body = response.body();
                if (!body.isSuccess() || body.getData() == null) {
                    Toast.makeText(OrderDetailActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                bind(body.getData());
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderDetail>> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bind(OrderDetail detail) {
        binding.chipCode.setText("#" + detail.getId());
        binding.chipStatus.setText(detail.getStatus());
        binding.chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(getStatusColor(detail.getStatus())));
        binding.chipStatus.setTextColor(Color.WHITE);
        binding.tvCreatedAt.setText("Ngày đặt: " + safe(detail.getCreatedAt()));
        binding.tvTotalAmount.setText("Tổng cộng: " + currencyFormat.format(detail.getTotalAmount()));
        String receiver = detail.getUser() != null ? detail.getUser().getName() : "";
        String email = detail.getUser() != null ? detail.getUser().getEmail() : "";
        binding.tvReceiver.setText("Người nhận: " + safe(receiver));
        binding.tvEmail.setText("Email: " + safe(email));
        binding.tvAddress.setText("Địa chỉ: " + safe(detail.getShippingAddress()));
        itemsAdapter.submitList(detail.getItems());
    }

    private String safe(String s) {
        return TextUtils.isEmpty(s) ? "" : s;
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#424242");
        String s = status.toLowerCase(Locale.ROOT);
        if (s.contains("completed") || s.contains("hoàn")) {
            return Color.parseColor("#4CAF50");
        } else if (s.contains("cancel") || s.contains("hủy")) {
            return Color.parseColor("#F44336");
        } else {
            return Color.parseColor("#2196F3");
        }
    }
}

