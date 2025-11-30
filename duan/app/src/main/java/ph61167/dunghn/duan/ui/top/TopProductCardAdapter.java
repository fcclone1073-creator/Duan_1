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

import ph61167.dunghn.duan.data.model.TopProductStat;
import ph61167.dunghn.duan.databinding.ItemTopProductCardBinding;

public class TopProductCardAdapter extends RecyclerView.Adapter<TopProductCardAdapter.VH> {

    private final List<TopProductStat> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void submitList(List<TopProductStat> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopProductCardBinding b = ItemTopProductCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), position + 1, currencyFormat);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemTopProductCardBinding binding;
        VH(ItemTopProductCardBinding b) { super(b.getRoot()); this.binding = b; }

        void bind(TopProductStat s, int rank, NumberFormat fmt) {
            binding.tvRank.setText("#" + rank);
            binding.tvName.setText(s.getProduct() != null ? s.getProduct().getName() : "");
            binding.tvPrice.setText(fmt.format(s.getProduct() != null ? s.getProduct().getPrice() : 0));
            binding.tvTotalQty.setText("SL bán: " + s.getTotalQty());
            binding.tvRevenue.setText("Doanh thu: " + fmt.format(s.getRevenue()));
            binding.tvOrderCount.setText("Số lần bán: " + s.getOrderCount());
            String image = s.getProduct() != null ? s.getProduct().getImage() : null;
            Glide.with(binding.ivImage.getContext())
                    .load(image)
                    .placeholder(ph61167.dunghn.duan.R.drawable.img)
                    .error(ph61167.dunghn.duan.R.drawable.img)
                    .into(binding.ivImage);
        }
    }
}
