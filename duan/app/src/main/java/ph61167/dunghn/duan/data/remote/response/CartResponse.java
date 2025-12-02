package ph61167.dunghn.duan.data.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ph61167.dunghn.duan.data.model.CartItem;

public class CartResponse {
    @SerializedName("_id")
    private String id;
    private String user;
    private List<CartItem> items;
    private double totalAmount;
    private String createdAt;
    private String updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }
}
