package ph61167.dunghn.duan.data.remote.request;

public class FavoriteRequest {
    private String user;
    private String product;

    public FavoriteRequest() {
    }

    public FavoriteRequest(String user, String product) {
        this.user = user;
        this.product = product;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}

