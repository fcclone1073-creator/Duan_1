package ph61167.dunghn.duan.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDetail {
    @SerializedName("_id")
    private String id;
    private String status;
    private String createdAt;
    private double totalAmount;
    private String shippingAddress;
    private UserInfo user;
    private List<Item> items;

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public UserInfo getUser() {
        return user;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class UserInfo {
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class Item {
        private ProductInfo product;
        private double price;
        private int quantity;

        public ProductInfo getProduct() {
            return product;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class ProductInfo {
        private String image;
        private String name;

        public String getImage() {
            return image;
        }

        public String getName() {
            return name;
        }
    }
}

