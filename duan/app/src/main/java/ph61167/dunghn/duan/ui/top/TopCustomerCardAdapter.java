package ph61167.dunghn.duan.ui.top;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.data.model.TopCustomerStat;
import ph61167.dunghn.duan.databinding.ItemTopCustomerCardBinding;

public class TopCustomerCardAdapter extends RecyclerView.Adapter<TopCustomerCardAdapter.VH> {

    private final List<TopCustomerStat> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void submitList(List<TopCustomerStat> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopCustomerCardBinding b = ItemTopCustomerCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), position + 1, currencyFormat);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemTopCustomerCardBinding binding;
        VH(ItemTopCustomerCardBinding b) { super(b.getRoot()); this.binding = b; }

        void bind(TopCustomerStat s, int rank, NumberFormat fmt) {
            binding.tvRank.setText("#" + rank);
            String name = s.getUser() != null ? s.getUser().getName() : "";
            String email = s.getUser() != null ? s.getUser().getEmail() : "";
            binding.tvName.setText(name);
            binding.tvEmail.setText(email);
            binding.tvOrderCount.setText("Số đơn: " + s.getOrderCount());
            binding.tvTotalQty.setText("SL mua: " + s.getTotalItems());
            binding.tvRevenue.setText("Doanh thu: " + fmt.format(s.getRevenue()));
            String image = s.getUser() != null ? s.getUser().getImage() : null;
            Glide.with(binding.ivAvatar.getContext())
                    .load(image)
                    .placeholder(ph61167.dunghn.duan.R.drawable.img)
                    .error(ph61167.dunghn.duan.R.drawable.img)
                    .into(binding.ivAvatar);
        }
    }
}
