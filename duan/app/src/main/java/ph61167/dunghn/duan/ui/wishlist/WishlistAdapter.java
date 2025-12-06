package ph61167.dunghn.duan.ui.wishlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.R;
import ph61167.dunghn.duan.data.model.Favorite;
import ph61167.dunghn.duan.databinding.ItemWishlistBinding;

public class WishlistAdapter extends ListAdapter<Favorite, WishlistAdapter.WishlistViewHolder> {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnProductClickListener onProductClickListener;
    private OnRemoveClickListener onRemoveClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;

    public WishlistAdapter(
            OnProductClickListener onProductClickListener,
            OnRemoveClickListener onRemoveClickListener,
            OnAddToCartClickListener onAddToCartClickListener
    ) {
        super(new FavoriteDiffCallback());
        this.onProductClickListener = onProductClickListener;
        this.onRemoveClickListener = onRemoveClickListener;
        this.onAddToCartClickListener = onAddToCartClickListener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishlistBinding binding = ItemWishlistBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new WishlistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ItemWishlistBinding binding;

        WishlistViewHolder(ItemWishlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Favorite favorite) {
            if (favorite.getProduct() == null) return;

            Favorite.ProductInfo product = favorite.getProduct();

            // Set product name
            binding.tvProductName.setText(product.getName());

            // Set product price
            binding.tvProductPrice.setText(currencyFormat.format(product.getPrice()));

            // Load product image
            String imageUrl = product.getImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.ivProduct);
            } else {
                binding.ivProduct.setImageResource(R.drawable.img);
            }

            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (onProductClickListener != null) {
                    onProductClickListener.onProductClick(favorite);
                }
            });

            binding.ivRemove.setOnClickListener(v -> {
                if (onRemoveClickListener != null) {
                    onRemoveClickListener.onRemoveClick(favorite);
                }
            });

            binding.btnAddToCart.setOnClickListener(v -> {
                if (onAddToCartClickListener != null) {
                    onAddToCartClickListener.onAddToCartClick(favorite);
                }
            });
        }
    }

    static class FavoriteDiffCallback extends DiffUtil.ItemCallback<Favorite> {
        @Override
        public boolean areItemsTheSame(@NonNull Favorite oldItem, @NonNull Favorite newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Favorite oldItem, @NonNull Favorite newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Favorite favorite);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(Favorite favorite);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Favorite favorite);
    }
}

