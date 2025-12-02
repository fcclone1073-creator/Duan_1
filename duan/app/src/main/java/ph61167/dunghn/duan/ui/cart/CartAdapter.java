package ph61167.dunghn.duan.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.R;
import ph61167.dunghn.duan.data.model.CartItem;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartViewHolder> {

    private final OnItemDeletedListener onItemDeletedListener;
    private final OnQuantityChangedListener onQuantityChangedListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public CartAdapter(OnItemDeletedListener onItemDeletedListener, OnQuantityChangedListener onQuantityChangedListener) {
        super(new CartItemDiffCallback());
        this.onItemDeletedListener = onItemDeletedListener;
        this.onQuantityChangedListener = onQuantityChangedListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = getItem(position);
        holder.bind(item);
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProduct;
        private final TextView tvProductName;
        private final TextView tvProductPrice;
        private final TextView tvQuantity;
        private final ImageButton btnDelete;
        private final ImageButton btnDecrease;
        private final ImageButton btnIncrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
        }

        public void bind(CartItem item) {
            if (item.getProduct() != null) {
                tvProductName.setText(item.getProduct().getName());
                tvProductPrice.setText(currencyFormat.format(item.getProduct().getPrice()));
                tvQuantity.setText(String.valueOf(item.getQuantity()));

                // Load image
                if (item.getProduct().getImage() != null && !item.getProduct().getImage().isEmpty()) {
                    String imageUrl = item.getProduct().getImage();
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "http://10.0.2.2:3000" + imageUrl;
                    }
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.img)
                            .error(R.drawable.img)
                            .into(ivProduct);
                } else {
                    ivProduct.setImageResource(R.drawable.img);
                }

                btnDelete.setOnClickListener(v -> {
                    if (onItemDeletedListener != null) {
                        onItemDeletedListener.onItemDeleted(item.getProduct().getId());
                    }
                });

                btnDecrease.setOnClickListener(v -> {
                    int newQuantity = item.getQuantity() - 1;
                    if (newQuantity < 1) {
                        // If quantity would become 0, delete the item
                        if (onItemDeletedListener != null) {
                            onItemDeletedListener.onItemDeleted(item.getProduct().getId());
                        }
                    } else {
                        if (onQuantityChangedListener != null) {
                            onQuantityChangedListener.onQuantityChanged(item.getProduct().getId(), newQuantity);
                        }
                    }
                });

                btnIncrease.setOnClickListener(v -> {
                    int newQuantity = item.getQuantity() + 1;
                    Integer stock = item.getProduct().getStock();
                    if (stock != null && newQuantity > stock) {
                        return; // Cannot exceed stock
                    }
                    if (onQuantityChangedListener != null) {
                        onQuantityChangedListener.onQuantityChanged(item.getProduct().getId(), newQuantity);
                    }
                });
            }
        }
    }

    static class CartItemDiffCallback extends DiffUtil.ItemCallback<CartItem> {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getProduct() != null && newItem.getProduct() != null
                    && oldItem.getProduct().getId().equals(newItem.getProduct().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getQuantity() == newItem.getQuantity();
        }
    }

    public interface OnItemDeletedListener {
        void onItemDeleted(String productId);
    }

    public interface OnQuantityChangedListener {
        void onQuantityChanged(String productId, int newQuantity);
    }
}
