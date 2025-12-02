package ph61167.dunghn.duan.ui.orders;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ph61167.dunghn.duan.R;
import ph61167.dunghn.duan.data.model.Order;
import ph61167.dunghn.duan.databinding.ItemOrderBinding;

public class OrdersAdapter extends ListAdapter<Order, OrdersAdapter.OrderViewHolder> {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public OrdersAdapter() {
        super(new OrderDiffCallback());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            // Set order ID
            String orderId = order.getId();
            if (orderId != null && orderId.length() > 8) {
                binding.tvOrderId.setText("Đơn hàng #" + orderId.substring(orderId.length() - 8));
            } else {
                binding.tvOrderId.setText("Đơn hàng #" + orderId);
            }

            // Set order status
            String status = order.getStatus();
            String statusText = order.getStatusText();
            binding.tvOrderStatus.setText(statusText);
            
            // Set status color
            int statusColor = R.color.warning;
            if (Order.STATUS_DELIVERED.equals(status)) {
                statusColor = R.color.success;
            } else if (Order.STATUS_CANCELLED.equals(status)) {
                statusColor = R.color.error;
            } else if (Order.STATUS_SHIPPING.equals(status)) {
                statusColor = R.color.info;
            }
            binding.tvOrderStatus.setBackgroundColor(
                    binding.getRoot().getContext().getResources().getColor(statusColor, null)
            );

            // Set order date
            if (order.getCreatedAt() != null) {
                try {
                    Date date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(order.getCreatedAt());
                    if (date != null) {
                        binding.tvOrderDate.setText(dateFormat.format(date));
                    }
                } catch (Exception e) {
                    binding.tvOrderDate.setText(order.getCreatedAt());
                }
            }

            // Set first product image and name
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                Order.OrderItem firstItem = order.getItems().get(0);
                if (firstItem.getProduct() != null) {
                    binding.tvProductName.setText(firstItem.getProduct().getName());
                    
                    String imageUrl = firstItem.getProduct().getImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(binding.getRoot().getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.img)
                                .error(R.drawable.img)
                                .centerCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.ivProduct);
                    }

                    // Set item count
                    int totalItems = order.getItems().size();
                    if (totalItems > 1) {
                        binding.tvItemCount.setText("x" + firstItem.getQuantity() + " và " + (totalItems - 1) + " sản phẩm khác");
                    } else {
                        binding.tvItemCount.setText("x" + firstItem.getQuantity());
                    }
                }
            }

            // Set total amount
            binding.tvTotalAmount.setText(currencyFormat.format(order.getTotalAmount()));

            // View detail button
            binding.btnViewDetail.setOnClickListener(v -> {
                // TODO: Open order detail
                android.widget.Toast.makeText(v.getContext(),
                        "Chi tiết đơn hàng",
                        android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    static class OrderDiffCallback extends DiffUtil.ItemCallback<Order> {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getStatus() != null && oldItem.getStatus().equals(newItem.getStatus());
        }
    }
}
