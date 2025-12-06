package ph61167.dunghn.duan.data.remote.request;

import java.util.List;

public class CreateOrderRequest {
    private String user;
    private List<OrderItemRequest> items;
    private ShippingAddressRequest shippingAddress;
    private String status;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String user, List<OrderItemRequest> items, ShippingAddressRequest shippingAddress) {
        this.user = user;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.status = "pending";
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public ShippingAddressRequest getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddressRequest shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class OrderItemRequest {
        private String product;
        private int quantity;
        private double price;

        public OrderItemRequest() {
        }

        public OrderItemRequest(String product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    public static class ShippingAddressRequest {
        private String fullName;
        private String phone;
        private String address;
        private String city;
        private String district;
        private String ward;

        public ShippingAddressRequest() {
        }

        public ShippingAddressRequest(String fullName, String phone, String address, String city, String district, String ward) {
            this.fullName = fullName;
            this.phone = phone;
            this.address = address;
            this.city = city;
            this.district = district;
            this.ward = ward;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getWard() {
            return ward;
        }

        public void setWard(String ward) {
            this.ward = ward;
        }
    }
}

