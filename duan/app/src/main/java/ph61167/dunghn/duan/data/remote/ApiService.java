package ph61167.dunghn.duan.data.remote;

import java.util.List;

import ph61167.dunghn.duan.data.model.Favorite;
import ph61167.dunghn.duan.data.model.Order;
import ph61167.dunghn.duan.data.model.Product;
import ph61167.dunghn.duan.data.model.User;
import ph61167.dunghn.duan.data.remote.request.CreateOrderRequest;
import ph61167.dunghn.duan.data.remote.request.FavoriteRequest;
import ph61167.dunghn.duan.data.remote.request.LoginRequest;
import ph61167.dunghn.duan.data.remote.request.RegisterRequest;
import ph61167.dunghn.duan.data.remote.response.AuthData;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.CartResponse;
import ph61167.dunghn.duan.data.remote.response.ProductsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTH APIs ====================
    @POST("users/login")
    Call<BaseResponse<AuthData>> login(@Body LoginRequest request);

    @POST("users/register")
    Call<BaseResponse<AuthData>> register(@Body RegisterRequest request);

    // ==================== USER APIs ====================
    @GET("users")
    Call<BaseResponse<List<User>>> getUsers();

    @GET("users/{id}")
    Call<BaseResponse<User>> getUserById(@Path("id") String userId);

    @PUT("users/{id}")
    Call<BaseResponse<User>> updateUser(@Path("id") String userId, @Body User user);

    @DELETE("users/{id}")
    Call<BaseResponse<User>> deleteUser(@Path("id") String userId);

    // ==================== PRODUCT APIs ====================
    @GET("products")
    Call<BaseResponse<ProductsResponse>> getProducts();

    @GET("products")
    Call<BaseResponse<ProductsResponse>> getProductsWithFilters(
            @Query("search") String search,
            @Query("category") String category,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("inStock") Boolean inStock,
            @Query("sortBy") String sortBy,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("products/{id}")
    Call<BaseResponse<Product>> getProductById(@Path("id") String productId);

    @POST("products")
    Call<BaseResponse<Product>> createProduct(@Body Product product);

    @PUT("products/{id}")
    Call<BaseResponse<Product>> updateProduct(@Path("id") String productId, @Body Product product);

    @DELETE("products/{id}")
    Call<BaseResponse<Product>> deleteProduct(@Path("id") String productId);

    // ==================== CART APIs ====================
    @GET("cart/user/{userId}")
    Call<BaseResponse<CartResponse>> getCart(@Path("userId") String userId);

    @POST("cart/user/{userId}/item")
    Call<BaseResponse<CartResponse>> addToCart(@Path("userId") String userId, @Body AddToCartRequest request);

    @PUT("cart/user/{userId}/item/{productId}")
    Call<BaseResponse<CartResponse>> updateCartItem(
            @Path("userId") String userId,
            @Path("productId") String productId,
            @Body UpdateCartItemRequest request
    );

    @DELETE("cart/user/{userId}/item/{productId}")
    Call<BaseResponse<CartResponse>> removeCartItem(
            @Path("userId") String userId,
            @Path("productId") String productId
    );

    @DELETE("cart/user/{userId}")
    Call<BaseResponse<CartResponse>> clearCart(@Path("userId") String userId);

    // ==================== ORDER APIs ====================
    @GET("orders")
    Call<BaseResponse<List<Order>>> getAllOrders();

    @GET("orders/user/{userId}")
    Call<BaseResponse<List<Order>>> getOrdersByUser(@Path("userId") String userId);

    @GET("orders/{id}")
    Call<BaseResponse<Order>> getOrderById(@Path("id") String orderId);

    @POST("orders")
    Call<BaseResponse<Order>> createOrder(@Body CreateOrderRequest request);

    @PUT("orders/{id}")
    Call<BaseResponse<Order>> updateOrder(@Path("id") String orderId, @Body Order order);

    @DELETE("orders/{id}")
    Call<BaseResponse<Order>> deleteOrder(@Path("id") String orderId);

    // ==================== FAVORITE APIs ====================
    @GET("favorites")
    Call<BaseResponse<List<Favorite>>> getAllFavorites();

    @GET("favorites/user/{userId}")
    Call<BaseResponse<List<Favorite>>> getFavoritesByUser(@Path("userId") String userId);

    @POST("favorites")
    Call<BaseResponse<Favorite>> addFavorite(@Body FavoriteRequest request);

    @DELETE("favorites/user/{userId}/product/{productId}")
    Call<BaseResponse<Favorite>> removeFavorite(
            @Path("userId") String userId,
            @Path("productId") String productId
    );

    @DELETE("favorites/{id}")
    Call<BaseResponse<Favorite>> deleteFavoriteById(@Path("id") String favoriteId);

    // ==================== REQUEST CLASSES ====================
    class AddToCartRequest {
        private String productId;
        private int quantity;

        public AddToCartRequest(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    class UpdateCartItemRequest {
        private int quantity;

        public UpdateCartItemRequest(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
