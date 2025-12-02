package ph61167.dunghn.duan.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

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
            // Set product name
            binding.tvProductName.setText(product.getName());
            
            // Format and set price
            String priceFormatted = format.format(product.getPrice());
            binding.tvProductPrice.setText(priceFormatted);
            
            // Load image from URL using Glide
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
            
            // Set rating if available
            if (binding.tvRating != null) {
                Double rating = product.getRating();
                binding.tvRating.setText(rating != null ? String.valueOf(rating) : "4.5");
            }
            
            // Set sold count
            if (binding.tvSold != null) {
                Integer soldCount = product.getSoldCount();
                String soldText = soldCount != null ? "Đã bán " + formatSoldCount(soldCount) : "Đã bán 0";
                binding.tvSold.setText("• " + soldText);
            }
            
            // Handle discount badge visibility
            if (binding.tvDiscount != null) {
                Double discount = product.getDiscount();
                if (discount != null && discount > 0) {
                    binding.tvDiscount.setVisibility(View.VISIBLE);
                    binding.tvDiscount.setText("-" + discount.intValue() + "%");
                } else {
                    binding.tvDiscount.setVisibility(View.GONE);
                }
            }
            
            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                } else {
                    // Open product detail
                    Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    intent.putExtra("product_name", product.getName());
                    intent.putExtra("product_price", product.getPrice());
                    intent.putExtra("product_image", product.getImage());
                    intent.putExtra("product_description", product.getDescription());
                    intent.putExtra("product_stock", product.getStock() != null ? product.getStock() : 0);
                    intent.putExtra("product_rating", product.getRating() != null ? product.getRating() : 4.5);
                    intent.putExtra("product_sold", product.getSoldCount() != null ? product.getSoldCount() : 0);
                    v.getContext().startActivity(intent);
                }
            });
            
            if (binding.ivAddCart != null) {
                binding.ivAddCart.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddToCartClick(product);
                    } else {
                        Toast.makeText(v.getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
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
        
        private String formatSoldCount(int count) {
            if (count >= 1000) {
                return String.format(Locale.US, "%.1fk", count / 1000.0);
            }
            return String.valueOf(count);
        }
    }
}
