package ph61167.dunghn.duan.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.util.Locale;

import ph61167.dunghn.duan.R;
import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.CartResponse;
import ph61167.dunghn.duan.databinding.ActivityProductDetailBinding;
import ph61167.dunghn.duan.ui.cart.CartActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

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
        if (productDescription != null && !productDescription.isEmpty()) {
            binding.tvProductDescription.setText(productDescription);
        }
        
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
        int reviewCount = productSold / 10;
        binding.tvReviews.setText("(" + formatNumber(reviewCount) + " đánh giá)");
        
        // Set sold count
        binding.tvSold.setText("Đã bán " + formatSoldCount(productSold));
        
        // Set stock status
        if (productStock > 0) {
            binding.tvStock.setText("Còn " + productStock + " sản phẩm");
            binding.tvStock.setTextColor(getResources().getColor(R.color.success, null));
        } else {
            binding.tvStock.setText("Hết hàng");
            binding.tvStock.setTextColor(getResources().getColor(R.color.error, null));
            binding.btnAddToCart.setEnabled(false);
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
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem sản phẩm: " + productName + " - " + currencyFormat.format(productPrice));
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ sản phẩm"));
            });
        }
        
        // Favorite button
        binding.btnFavorite.setOnClickListener(v -> {
            toggleFavorite();
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
                addToCart();
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

    private void addToCart() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getService()
                .addToCart(userId, new ph61167.dunghn.duan.data.remote.ApiService.AddToCartRequest(productId, quantity))
                .enqueue(new Callback<BaseResponse<CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<CartResponse>> call,
                            Response<BaseResponse<CartResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<CartResponse> body = response.body();
                            if (body.isSuccess()) {
                                Toast.makeText(ProductDetailActivity.this,
                                        "Đã thêm " + quantity + " " + productName + " vào giỏ hàng",
                                        Toast.LENGTH_SHORT).show();
                                
                                // Option to go to cart
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ProductDetailActivity.this);
                                builder.setTitle("Thêm vào giỏ hàng thành công");
                                builder.setMessage("Bạn có muốn xem giỏ hàng không?");
                                builder.setPositiveButton("Xem giỏ hàng", (dialog, which) -> {
                                    startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
                                });
                                builder.setNegativeButton("Tiếp tục mua sắm", (dialog, which) -> {
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(ProductDetailActivity.this,
                                        body.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CartResponse>> call, Throwable t) {
                        Toast.makeText(ProductDetailActivity.this,
                                "Lỗi khi thêm vào giỏ hàng: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleFavorite() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ph61167.dunghn.duan.data.remote.request.FavoriteRequest request = 
                new ph61167.dunghn.duan.data.remote.request.FavoriteRequest(userId, productId);

        ApiClient.getService()
                .addFavorite(request)
                .enqueue(new Callback<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>> call,
                            Response<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<ph61167.dunghn.duan.data.model.Favorite> body = response.body();
                            if (body.isSuccess()) {
                                binding.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                                Toast.makeText(ProductDetailActivity.this,
                                        "Đã thêm vào yêu thích",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Try to remove if already exists
                                removeFavorite();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>> call,
                            Throwable t
                    ) {
                        Toast.makeText(ProductDetailActivity.this,
                                "Lỗi khi thêm yêu thích",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFavorite() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        ApiClient.getService()
                .removeFavorite(userId, productId)
                .enqueue(new Callback<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>> call,
                            Response<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            binding.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                            Toast.makeText(ProductDetailActivity.this,
                                    "Đã xóa khỏi yêu thích",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<BaseResponse<ph61167.dunghn.duan.data.model.Favorite>> call,
                            Throwable t
                    ) {
                        // Ignore
                    }
                });
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
