package ph61167.dunghn.duan.ui.orders;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.data.model.OrderDetail;
import ph61167.dunghn.duan.databinding.ItemOrderHistoryBinding;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderDetail> orders = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void submitList(List<OrderDetail> data) {
        orders.clear();
        if (data != null) {
            orders.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHistoryBinding binding = ItemOrderHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position), currencyFormat);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final ItemOrderHistoryBinding binding;

        OrderViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OrderDetail order, NumberFormat format) {
            String code = order.getId();
            binding.chipCode.setText("#" + code);

            int total = order.getItems() != null ? order.getItems().size() : 0;
            int qty = (order.getItems() != null && total > 0) ? Math.max(order.getItems().get(0).getQuantity(), 1) : 1;
            int others = Math.max(total - 1, 0);
            String firstName = (order.getItems() != null && total > 0 && order.getItems().get(0).getProduct() != null)
                    ? order.getItems().get(0).getProduct().getName() : "Sản phẩm";
            String productSummary = others > 0
                    ? firstName + " × " + qty + " · " + others + " mặt hàng khác"
                    : firstName + " × " + qty;
            binding.tvProductSummary.setText(productSummary);

            binding.tvTotalAmount.setText("Tổng: " + format.format(order.getTotalAmount()));

            String status = order.getStatus();
            int statusColor = getStatusColor(status);
            binding.chipStatus.setText(status);
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(statusColor));
            binding.chipStatus.setTextColor(Color.WHITE);

            binding.tvOrderDate.setText("Ngày đặt: " + (order.getCreatedAt() != null ? order.getCreatedAt() : ""));

            binding.btnQuickAction.setText("Chi tiết đơn hàng");
            binding.btnQuickAction.setOnClickListener(v -> {
                android.content.Context ctx = v.getContext();
                android.content.Intent i = new android.content.Intent(ctx, OrderDetailActivity.class);
                i.putExtra("order_id", order.getId());
                ctx.startActivity(i);
            });
        }

        private int getStatusColor(String status) {
            if (status == null) return Color.parseColor("#424242");
            String s = status.toLowerCase(Locale.ROOT);
            if (s.contains("completed") || s.contains("hoàn")) {
                return Color.parseColor("#4CAF50"); // green
            } else if (s.contains("giao") || s.contains("shipping") || s.contains("đang")) {
                return Color.parseColor("#2196F3"); // blue_text
            } else if (s.contains("hủy") || s.contains("cancel")) {
                return Color.parseColor("#F44336"); // red
            } else {
                return Color.parseColor("#424242"); // dark_gray
            }
        }

        private String getActionText(String status) {
            if (status == null) return "Mua lại";
            String s = status.toLowerCase(Locale.ROOT);
            if (s.contains("giao") || s.contains("shipping") || s.contains("đang")) {
                return "Theo dõi đơn hàng";
            }
            return "Mua lại";
        }
    }
}
