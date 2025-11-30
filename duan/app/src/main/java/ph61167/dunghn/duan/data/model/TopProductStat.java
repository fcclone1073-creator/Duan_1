package ph61167.dunghn.duan.data.model;

import com.google.gson.annotations.SerializedName;

public class TopProductStat {
    @SerializedName("product")
    private ProductInfo product;
    @SerializedName("totalQty")
    private int totalQty;
    @SerializedName("revenue")
    private double revenue;
    @SerializedName("orderCount")
    private int orderCount;

    public ProductInfo getProduct() { return product; }
    public int getTotalQty() { return totalQty; }
    public double getRevenue() { return revenue; }
    public int getOrderCount() { return orderCount; }

    public static class ProductInfo {
        @SerializedName("_id")
        private String id;
        private String name;
        private String image;
        private double price;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getImage() { return image; }
        public double getPrice() { return price; }
    }
}
