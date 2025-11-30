package ph61167.dunghn.duan.data.remote.response;

import java.util.List;

import ph61167.dunghn.duan.data.model.TopProductStat;

public class TopProductsListData {
    private List<TopProductStat> items;
    private int limit;
    private Filter filter;

    public List<TopProductStat> getItems() { return items; }
    public int getLimit() { return limit; }
    public Filter getFilter() { return filter; }

    public static class Filter {
        private String status;
        private String start;
        private String end;

        public String getStatus() { return status; }
        public String getStart() { return start; }
        public String getEnd() { return end; }
    }
}
