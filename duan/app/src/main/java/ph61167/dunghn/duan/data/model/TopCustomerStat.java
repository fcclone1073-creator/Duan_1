package ph61167.dunghn.duan.data.model;

import com.google.gson.annotations.SerializedName;

public class TopCustomerStat {
    @SerializedName("user")
    private UserInfo user;
    @SerializedName("orderCount")
    private int orderCount;
    @SerializedName(value = "totalItems", alternate = {"totalQty"})
    private int totalItems;
    @SerializedName("revenue")
    private double revenue;

    public UserInfo getUser() { return user; }
    public int getOrderCount() { return orderCount; }
    public int getTotalItems() { return totalItems; }
    public double getRevenue() { return revenue; }

    public static class UserInfo {
        @SerializedName(value = "_id", alternate = {"id"})
        private String id;
        private String name;
        private String email;
        private String image;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getImage() { return image; }
    }
}
