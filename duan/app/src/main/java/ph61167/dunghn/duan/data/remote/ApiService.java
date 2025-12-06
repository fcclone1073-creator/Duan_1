package ph61167.dunghn.duan.data.remote;

import java.util.List;

import ph61167.dunghn.duan.data.model.Product;
import ph61167.dunghn.duan.data.model.Order;
import ph61167.dunghn.duan.data.model.User;
import ph61167.dunghn.duan.data.remote.response.UsersListData;
import ph61167.dunghn.duan.data.model.OrderDetail;
import ph61167.dunghn.duan.data.remote.response.OrdersListData;
import ph61167.dunghn.duan.data.remote.request.LoginRequest;
import ph61167.dunghn.duan.data.remote.request.RegisterRequest;
import ph61167.dunghn.duan.data.remote.request.CartItemAddRequest;
import ph61167.dunghn.duan.data.remote.response.AuthData;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.model.CartData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.DELETE;

public interface ApiService {

    @POST("users/login")
    Call<BaseResponse<AuthData>> login(@Body LoginRequest request);

    @POST("users/register")
    Call<BaseResponse<AuthData>> register(@Body RegisterRequest request);

    @GET("products")
    Call<BaseResponse<List<Product>>> getProducts();

    @GET("orders")
    Call<BaseResponse<List<Order>>> getOrders();

    @GET("users/list")
    Call<BaseResponse<UsersListData>> getUsersList();

    @GET("users/detail/{id}")
    Call<BaseResponse<User>> getUserDetail(@retrofit2.http.Path("id") String id);

    @GET("orders/detail/{id}")
    Call<BaseResponse<OrderDetail>> getOrderDetail(@retrofit2.http.Path("id") String id);

    @GET("orders/user/{id}/list")
    Call<BaseResponse<OrdersListData>> getUserOrders(@retrofit2.http.Path("id") String userId);

    @POST("v1/cart/items")
    Call<BaseResponse<Object>> addCartItem(@Body CartItemAddRequest request);

    @GET("v1/cart")
    Call<BaseResponse<CartData>> getCart(@Query("userId") String userId);

    @DELETE("v1/cart/items/{productId}")
    Call<BaseResponse<CartData>> deleteCartItem(@Path("productId") String productId, @Query("userId") String userId);

    @GET("orders/user/{id}")
    Call<BaseResponse<List<Order>>> getOrdersByUser(@Path("id") String userId);
}

