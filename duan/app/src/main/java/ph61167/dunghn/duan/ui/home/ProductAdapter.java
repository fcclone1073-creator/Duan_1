package ph61167.dunghn.duan.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.R;
import ph61167.dunghn.duan.data.model.Product;
import ph61167.dunghn.duan.databinding.ItemProductBinding;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
        void onFavoriteClick(Product product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> data) {
        products.clear();
        if (data != null) {
            products.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position), currencyFormat, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product, NumberFormat format, OnProductClickListener listener) {
            binding.tvProductName.setText(product.getName());
            
            // Format price with VND currency
            String priceFormatted = format.format(product.getPrice());
            binding.tvProductPrice.setText(priceFormatted);
            
            // Set default image (you can use Glide/Picasso to load from URL)
            binding.ivProduct.setImageResource(R.drawable.img);
            
            // Show rating if available (default for now)
            if (binding.tvRating != null) {
                binding.tvRating.setText("4.5");
            }
            
            // Show sold count
            if (binding.tvSold != null) {
                binding.tvSold.setText("• Đã bán 0");
            }
            
            // Handle discount badge visibility
            if (binding.tvDiscount != null) {
                binding.tvDiscount.setVisibility(View.GONE);
            }
            
            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
            
            if (binding.ivAddCart != null) {
                binding.ivAddCart.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddToCartClick(product);
                    } else {
                        Toast.makeText(v.getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            if (binding.ivFavorite != null) {
                binding.ivFavorite.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFavoriteClick(product);
                    } else {
                        Toast.makeText(v.getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
