package ph61167.dunghn.duan.ui.orders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.data.model.OrderDetail;
import ph61167.dunghn.duan.databinding.ItemOrderDetailLineBinding;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ItemViewHolder> {

    private final List<OrderDetail.Item> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void submitList(List<OrderDetail.Item> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderDetailLineBinding binding = ItemOrderDetailLineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position), currencyFormat);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderDetailLineBinding binding;

        ItemViewHolder(ItemOrderDetailLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OrderDetail.Item item, NumberFormat fmt) {
            binding.tvName.setText(item.getProduct() != null ? item.getProduct().getName() : "");
            binding.tvUnitPrice.setText("Đơn giá: " + fmt.format(item.getPrice()));
            binding.tvQuantity.setText("Số lượng: " + item.getQuantity());
            binding.ivImage.setImageResource(ph61167.dunghn.duan.R.drawable.img);
        }
    }
}

