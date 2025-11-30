package ph61167.dunghn.duan.data.remote.response;

import java.util.List;

import ph61167.dunghn.duan.data.model.TopCustomerStat;

public class TopCustomersListData {
    private List<TopCustomerStat> items;
    private int limit;
    private Filter filter;

    public List<TopCustomerStat> getItems() { return items; }
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
