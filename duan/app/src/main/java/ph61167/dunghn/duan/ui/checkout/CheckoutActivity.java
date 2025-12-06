package ph61167.dunghn.duan.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.data.local.SessionManager;
import ph61167.dunghn.duan.data.model.CartItem;
import ph61167.dunghn.duan.data.model.Order;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.ApiService;
import ph61167.dunghn.duan.data.remote.request.CreateOrderRequest;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.CartResponse;
import ph61167.dunghn.duan.databinding.ActivityCheckoutBinding;
import ph61167.dunghn.duan.ui.orders.OrdersActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private SessionManager sessionManager;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private CartResponse cartData;
    private String paymentMethod = "COD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        setupClickListeners();
        fetchCart();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEditAddress.setOnClickListener(v -> {
            showAddressDialog();
        });

        binding.cardPaymentMethod.setOnClickListener(v -> {
            showPaymentMethodDialog();
        });

        binding.btnPlaceOrder.setOnClickListener(v -> {
            placeOrder();
        });
    }

    private void fetchCart() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            finish();
            return;
        }

        ApiClient.getService()
                .getCart(userId)
                .enqueue(new Callback<BaseResponse<CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<CartResponse>> call,
                            Response<BaseResponse<CartResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<CartResponse> body = response.body();
                            if (body.isSuccess() && body.getData() != null) {
                                cartData = body.getData();
                                setupCartData();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CartResponse>> call, Throwable t) {
                        Toast.makeText(CheckoutActivity.this,
                                "Lỗi khi tải giỏ hàng",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupCartData() {
        if (cartData == null || cartData.getItems() == null) return;

        // Setup order items list
        // TODO: Add RecyclerView adapter for order items

        // Setup totals
        double subtotal = cartData.getTotalAmount();
        binding.tvSubtotal.setText(currencyFormat.format(subtotal));
        binding.tvTotal.setText(currencyFormat.format(subtotal));

        // Setup default address
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        if (userName != null) {
            binding.tvAddressName.setText(userName);
        }
        binding.tvAddress.setText("Chưa có địa chỉ. Vui lòng cập nhật.");
        binding.tvPhone.setText("Chưa có số điện thoại");

        // Setup payment method
        binding.tvPaymentMethod.setText("Thanh toán khi nhận hàng (COD)");
    }

    private void showAddressDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cập nhật địa chỉ giao hàng");

        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        android.widget.EditText etFullName = new android.widget.EditText(this);
        etFullName.setHint("Họ và tên");
        etFullName.setText(sessionManager.getUserName());

        android.widget.EditText etPhone = new android.widget.EditText(this);
        etPhone.setHint("Số điện thoại");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        android.widget.EditText etAddress = new android.widget.EditText(this);
        etAddress.setHint("Địa chỉ");
        etAddress.setMinLines(2);

        android.widget.EditText etCity = new android.widget.EditText(this);
        etCity.setHint("Thành phố");

        android.widget.EditText etDistrict = new android.widget.EditText(this);
        etDistrict.setHint("Quận/Huyện");

        android.widget.EditText etWard = new android.widget.EditText(this);
        etWard.setHint("Phường/Xã");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(etFullName);
        layout.addView(etPhone);
        layout.addView(etAddress);
        layout.addView(etCity);
        layout.addView(etDistrict);
        layout.addView(etWard);

        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String district = etDistrict.getText().toString().trim();
            String ward = etWard.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.tvAddressName.setText(fullName);
            binding.tvPhone.setText(phone);
            binding.tvAddress.setText(address + ", " + ward + ", " + district + ", " + city);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showPaymentMethodDialog() {
        String[] methods = {"Thanh toán khi nhận hàng (COD)", "Chuyển khoản ngân hàng", "Ví điện tử"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn phương thức thanh toán");
        builder.setItems(methods, (dialog, which) -> {
            paymentMethod = methods[which];
            binding.tvPaymentMethod.setText(paymentMethod);
        });
        builder.show();
    }

    private void placeOrder() {
        if (cartData == null || cartData.getItems() == null || cartData.getItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get address info
        String fullName = binding.tvAddressName.getText().toString();
        String phone = binding.tvPhone.getText().toString();
        String address = binding.tvAddress.getText().toString();

        if (fullName.contains("Chưa có") || address.contains("Chưa có")) {
            Toast.makeText(this, "Vui lòng cập nhật địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order items
        List<CreateOrderRequest.OrderItemRequest> orderItems = new ArrayList<>();
        for (CartItem item : cartData.getItems()) {
            if (item.getProduct() != null) {
                orderItems.add(new CreateOrderRequest.OrderItemRequest(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getProduct().getPrice()
                ));
            }
        }

        // Create shipping address
        String[] addressParts = address.split(", ");
        CreateOrderRequest.ShippingAddressRequest shippingAddress = 
                new CreateOrderRequest.ShippingAddressRequest(
                        fullName,
                        phone,
                        addressParts.length > 0 ? addressParts[0] : address,
                        addressParts.length > 3 ? addressParts[3] : "",
                        addressParts.length > 2 ? addressParts[2] : "",
                        addressParts.length > 1 ? addressParts[1] : ""
                );

        // Create order request
        CreateOrderRequest orderRequest = new CreateOrderRequest(userId, orderItems, shippingAddress);

        // Show loading
        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText("Đang xử lý...");

        ApiClient.getService()
                .createOrder(orderRequest)
                .enqueue(new Callback<BaseResponse<Order>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<Order>> call,
                            Response<BaseResponse<Order>> response
                    ) {
                        binding.btnPlaceOrder.setEnabled(true);
                        binding.btnPlaceOrder.setText("Đặt hàng");

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<Order> body = response.body();
                            if (body.isSuccess()) {
                                Toast.makeText(CheckoutActivity.this,
                                        "Đặt hàng thành công!",
                                        Toast.LENGTH_SHORT).show();

                                // Clear cart
                                clearCart();

                                // Navigate to orders
                                Intent intent = new Intent(CheckoutActivity.this, OrdersActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(CheckoutActivity.this,
                                        body.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CheckoutActivity.this,
                                    "Không thể đặt hàng. Vui lòng thử lại.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Order>> call, Throwable t) {
                        binding.btnPlaceOrder.setEnabled(true);
                        binding.btnPlaceOrder.setText("Đặt hàng");
                        Toast.makeText(CheckoutActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearCart() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        ApiClient.getService()
                .clearCart(userId)
                .enqueue(new Callback<BaseResponse<CartResponse>>() {
                    @Override
                    public void onResponse(
                            Call<BaseResponse<CartResponse>> call,
                            Response<BaseResponse<CartResponse>> response
                    ) {
                        // Cart cleared
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CartResponse>> call, Throwable t) {
                        // Ignore
                    }
                });
    }
}

