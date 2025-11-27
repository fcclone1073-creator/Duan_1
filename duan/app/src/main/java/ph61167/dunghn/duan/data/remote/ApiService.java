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
import ph61167.dunghn.duan.data.remote.response.AuthData;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

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
}

