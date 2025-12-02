package ph61167.dunghn.duan.ui.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.model.Favorite;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.databinding.ActivityWishlistBinding;
import ph61167.dunghn.duan.ui.home.ProductDetailActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistActivity extends AppCompatActivity {

    private ActivityWishlistBinding binding;
    private SessionManager sessionManager;
    private WishlistAdapter wishlistAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        setupRecyclerView();
        setupClickListeners();
        fetchFavorites();
    }

    private void setupRecyclerView() {
        wishlistAdapter = new WishlistAdapter(
                this::onProductClick,
                this::onRemoveClick,
                this::onAddToCartClick
        );
        binding.rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvWishlist.setAdapter(wishlistAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void fetchFavorites() {
        showLoading(true);
        String userId = sessionManager.getUserId();
        if (userId == null) {
            showLoading(false);
            finish();
            return;
        }

        ApiClient.getService()
                .getFavoritesByUser(userId)
                .enqueue(new Callback<BaseResponse<List<Favorite>>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<List<Favorite>>> call,
                            Response<BaseResponse<List<Favorite>>> response
                    ) {
                        showLoading(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmptyState(true);
                            return;
                        }

                        BaseResponse<List<Favorite>> body = response.body();
                        if (body.isSuccess() && body.getData() != null && !body.getData().isEmpty()) {
                            wishlistAdapter.submitList(body.getData());
                            showEmptyState(false);
                        } else {
                            wishlistAdapter.submitList(java.util.Collections.emptyList());
                            showEmptyState(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<List<Favorite>>> call, Throwable t) {
                        showLoading(false);
                        showEmptyState(true);
                        Toast.makeText(WishlistActivity.this,
                                "Lỗi khi tải danh sách yêu thích",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onProductClick(Favorite favorite) {
        if (favorite.getProduct() == null) return;
        Favorite.ProductInfo product = favorite.getProduct();
        
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_image", product.getImage());
        intent.putExtra("product_description", "");
        intent.putExtra("product_stock", 0);
        intent.putExtra("product_rating", 4.5);
        intent.putExtra("product_sold", 0);
        startActivity(intent);
    }

    private void onRemoveClick(Favorite favorite) {
        String userId = sessionManager.getUserId();
        if (userId == null || favorite.getProduct() == null) return;

        ApiClient.getService()
                .removeFavorite(userId, favorite.getProduct().getId())
                .enqueue(new Callback<BaseResponse<Favorite>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<Favorite>> call,
                            Response<BaseResponse<Favorite>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<Favorite> body = response.body();
                            if (body.isSuccess()) {
                                Toast.makeText(WishlistActivity.this,
                                        "Đã xóa khỏi yêu thích",
                                        Toast.LENGTH_SHORT).show();
                                fetchFavorites(); // Refresh list
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Favorite>> call, Throwable t) {
                        Toast.makeText(WishlistActivity.this,
                                "Lỗi khi xóa",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onAddToCartClick(Favorite favorite) {
        if (favorite.getProduct() == null) return;
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        Favorite.ProductInfo product = favorite.getProduct();
        ApiClient.getService()
                .addToCart(userId, new ph61167.dunghn.duan.data.remote.ApiService.AddToCartRequest(
                        product.getId(), 1))
                .enqueue(new Callback<BaseResponse<ph61167.dunghn.duan.data.remote.response.CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<ph61167.dunghn.duan.data.remote.response.CartResponse>> call,
                            Response<BaseResponse<ph61167.dunghn.duan.data.remote.response.CartResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<ph61167.dunghn.duan.data.remote.response.CartResponse> body = response.body();
                            if (body.isSuccess()) {
                                Toast.makeText(WishlistActivity.this,
                                        "Đã thêm vào giỏ hàng",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<BaseResponse<ph61167.dunghn.duan.data.remote.response.CartResponse>> call,
                            Throwable t
                    ) {
                        Toast.makeText(WishlistActivity.this,
                                "Lỗi khi thêm vào giỏ hàng",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.rvWishlist.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvWishlist.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.rvWishlist.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFavorites();
    }
}

