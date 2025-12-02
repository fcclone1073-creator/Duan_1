package ph61167.dunghn.duan.ui.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.R;
import ph61167.dunghn.duan.databinding.ActivityProductDetailBinding;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    private String productId;
    private String productName;
    private double productPrice;
    private String productImage;
    private String productDescription;
    private int productStock;
    private double productRating;
    private int productSold;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        getIntentData();
        
        // Setup UI
        setupUI();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void getIntentData() {
        productId = getIntent().getStringExtra("product_id");
        productName = getIntent().getStringExtra("product_name");
        productPrice = getIntent().getDoubleExtra("product_price", 0);
        productImage = getIntent().getStringExtra("product_image");
        productDescription = getIntent().getStringExtra("product_description");
        productStock = getIntent().getIntExtra("product_stock", 0);
        productRating = getIntent().getDoubleExtra("product_rating", 4.5);
        productSold = getIntent().getIntExtra("product_sold", 0);
    }

    private void setupUI() {
        // Set product name
        binding.tvProductName.setText(productName);
        
        // Set product price
        binding.tvProductPrice.setText(currencyFormat.format(productPrice));
        binding.tvTotalPrice.setText(currencyFormat.format(productPrice * quantity));
        
        // Set product description
        binding.tvProductDescription.setText(productDescription);
        
        // Load product image
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this)
                    .load(productImage)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivProduct);
        }
        
        // Set rating
        binding.tvRating.setText(String.valueOf(productRating));
        
        // Set reviews count
        binding.tvReviews.setText("(" + formatNumber(productSold / 10) + " đánh giá)");
        
        // Set sold count
        binding.tvSold.setText("Đã bán " + formatSoldCount(productSold));
        
        // Set stock status
        if (productStock > 0) {
            binding.tvStock.setText("Còn " + productStock + " sản phẩm");
            binding.tvStock.setTextColor(getResources().getColor(R.color.success, null));
        } else {
            binding.tvStock.setText("Hết hàng");
            binding.tvStock.setTextColor(getResources().getColor(R.color.error, null));
        }
        
        // Set quantity
        binding.tvQuantity.setText(String.valueOf(quantity));
    }

    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Share button
        if (binding.btnShare != null) {
            binding.btnShare.setOnClickListener(v -> {
                Toast.makeText(this, "Chia sẻ sản phẩm", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Favorite button
        binding.btnFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        });
        
        // Decrease quantity
        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityUI();
            }
        });
        
        // Increase quantity
        binding.btnIncrease.setOnClickListener(v -> {
            if (quantity < productStock) {
                quantity++;
                updateQuantityUI();
            } else {
                Toast.makeText(this, "Số lượng không đủ", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Add to cart button
        binding.btnAddToCart.setOnClickListener(v -> {
            if (productStock > 0) {
                Toast.makeText(this, "Đã thêm " + quantity + " " + productName + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                // TODO: Call API to add to cart
            } else {
                Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            }
        });
        
        // View reviews
        if (binding.tvViewReviews != null) {
            binding.tvViewReviews.setOnClickListener(v -> {
                Toast.makeText(this, "Xem đánh giá sản phẩm", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateQuantityUI() {
        binding.tvQuantity.setText(String.valueOf(quantity));
        binding.tvTotalPrice.setText(currencyFormat.format(productPrice * quantity));
    }

    private String formatSoldCount(int count) {
        if (count >= 1000) {
            return String.format(Locale.US, "%.1fk", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private String formatNumber(int count) {
        if (count >= 1000) {
            return String.format(Locale.US, "%.1fk", count / 1000.0);
        }
        return String.valueOf(count);
    }
}

