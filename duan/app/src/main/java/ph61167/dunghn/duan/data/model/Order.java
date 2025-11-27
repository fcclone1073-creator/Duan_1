package ph61167.dunghn.duan.data.model;

import com.google.gson.annotations.SerializedName;

public class Order {

    @SerializedName("_id")
    private String id;
    private String code;
    private String firstProductName;
    private int quantity;
    private int itemsCount;
    private double totalAmount;
    private String status;
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFirstProductName() {
        return firstProductName;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

