package ph61167.dunghn.duan.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.data.model.CartData;
import ph61167.dunghn.duan.databinding.ItemCartBinding;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartData.Item> items = new ArrayList<>();
    private final java.util.HashSet<CartData.Item> selected = new java.util.HashSet<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnQuantityChangeListener quantityChangeListener;
    private OnDeleteClickListener deleteClickListener;
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnQuantityChangeListener {
        void onChange(CartData.Item item, int newQuantity);
    }

    public interface OnDeleteClickListener {
        void onDelete(CartData.Item item);
    }

    public interface OnSelectionChangeListener {
        void onChange();
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener l) {
        this.quantityChangeListener = l;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener l) {
        this.deleteClickListener = l;
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener l) {
        this.selectionChangeListener = l;
    }

    public void submitList(List<CartData.Item> data) {
        items.clear();
        selected.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public List<CartData.Item> getItems() { return items; }

    public List<CartData.Item> getSelectedItems() {
        List<CartData.Item> res = new ArrayList<>();
        for (CartData.Item it : items) if (selected.contains(it)) res.add(it);
        return res;
    }

    public void selectAll() {
        selected.clear();
        selected.addAll(items);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selected.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position), currencyFormat, quantityChangeListener, deleteClickListener, selected, selectionChangeListener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartData.Item item, NumberFormat fmt, OnQuantityChangeListener l, OnDeleteClickListener d, java.util.Set<CartData.Item> selectedSet, OnSelectionChangeListener selListener) {
            String name = item.getProduct() != null ? item.getProduct().getName() : "";
            String image = item.getProduct() != null ? item.getProduct().getImage() : null;
            binding.tvProductName.setText(name);
            binding.tvProductPrice.setText(fmt.format(item.getUnitPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
            Glide.with(binding.ivProduct.getContext())
                    .load(image)
                    .placeholder(ph61167.dunghn.duan.R.drawable.img)
                    .error(ph61167.dunghn.duan.R.drawable.img)
                    .into(binding.ivProduct);

            binding.cbSelect.setOnCheckedChangeListener(null);
            binding.cbSelect.setChecked(selectedSet.contains(item));
            binding.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedSet.add(item); else selectedSet.remove(item);
                if (selListener != null) selListener.onChange();
            });

            binding.btnDecrease.setOnClickListener(v -> {
                int q = Math.max(1, safeInt(binding.tvQuantity.getText().toString()) - 1);
                binding.tvQuantity.setText(String.valueOf(q));
                if (l != null) l.onChange(item, q);
            });
            binding.btnIncrease.setOnClickListener(v -> {
                int q = Math.max(1, safeInt(binding.tvQuantity.getText().toString()) + 1);
                binding.tvQuantity.setText(String.valueOf(q));
                if (l != null) l.onChange(item, q);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (d != null) d.onDelete(item);
            });
        }

        private int safeInt(String s) {
            try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 1; }
        }
    }
}
