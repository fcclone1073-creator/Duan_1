package ph61167.dunghn.duan.ui.top;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ph61167.dunghn.duan.data.model.TopCustomerStat;
import ph61167.dunghn.duan.data.remote.ApiClient;
import ph61167.dunghn.duan.data.remote.response.BaseResponse;
import ph61167.dunghn.duan.data.remote.response.TopCustomersListData;
import ph61167.dunghn.duan.databinding.ActivityTopCustomersBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopCustomersActivity extends AppCompatActivity {

    private ActivityTopCustomersBinding binding;
    private TopCustomerCardAdapter adapter;
    private int limit = 5;
    private String startIsoZ;
    private String endIsoZ;
    private final String status = "completed";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopCustomersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupDateRangeDefaults();
        setupLimitSpinner();
        setupQuickButtons();
        setupDatePicker();

        adapter = new TopCustomerCardAdapter();
        binding.rvTop.setLayoutManager(new GridLayoutManager(this, getSpanCount()));
        binding.rvTop.setAdapter(adapter);

        fetchTopCustomers();
    }

    private int getSpanCount() {
        int widthDp = getResources().getConfiguration().screenWidthDp;
        if (widthDp < 600) return 1;
        return 2;
    }

    private void fetchTopCustomers() {
        showLoading(true);
        ApiClient.getService().getTopCustomers(startIsoZ, endIsoZ, status, limit).enqueue(new Callback<BaseResponse<TopCustomersListData>>() {
            @Override
            public void onResponse(Call<BaseResponse<TopCustomersListData>> call, Response<BaseResponse<TopCustomersListData>> response) {
                showLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TopCustomersActivity.this, "Không thể tải top khách hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                BaseResponse<TopCustomersListData> body = response.body();
                if (body.isSuccess()) {
                    TopCustomersListData data = body.getData();
                    adapter.submitList(data != null ? data.getItems() : null);
                } else {
                    Toast.makeText(TopCustomersActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<TopCustomersListData>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(TopCustomersActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.rvTop.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void setupDateRangeDefaults() {
        setLast30Days();
    }

    private void setupQuickButtons() {
        binding.btnLast7Days.setOnClickListener(v -> { setLast7Days(); fetchTopCustomers(); });
        binding.btnThisMonth.setOnClickListener(v -> { setThisMonth(); fetchTopCustomers(); });
    }

    private void setupLimitSpinner() {
        String[] labels = new String[]{"5 (Mặc định)", "10", "20", "Tùy chỉnh"};
        final int[] values = new int[]{5, 10, 20, -1};
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLimit.setAdapter(a);
        binding.spLimit.setSelection(0);
        binding.spLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int v = values[position];
                if (v == -1) { showCustomLimitDialog(); return; }
                limit = v; fetchTopCustomers();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showCustomLimitDialog() {
        android.widget.EditText et = new android.widget.EditText(this);
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nhập số lượng Top")
                .setView(et)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        int v = Integer.parseInt(et.getText().toString().trim());
                        if (v > 0) { limit = v; fetchTopCustomers(); }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setLast7Days() {
        Calendar cal = Calendar.getInstance();
        java.util.Date end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        java.util.Date start = cal.getTime();
        startIsoZ = toIsoStartOfDayZ(start);
        endIsoZ = toIsoEndOfDayZ(end);
        binding.tvDateRange.setText("Thời gian: " + toDisplayDate(start) + " - " + toDisplayDate(end));
    }

    private void setLast30Days() {
        Calendar cal = Calendar.getInstance();
        java.util.Date end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -29);
        java.util.Date start = cal.getTime();
        startIsoZ = toIsoStartOfDayZ(start);
        endIsoZ = toIsoEndOfDayZ(end);
        binding.tvDateRange.setText("Thời gian: " + toDisplayDate(start) + " - " + toDisplayDate(end));
    }

    private void setThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        java.util.Date start = cal.getTime();
        cal = Calendar.getInstance();
        java.util.Date end = cal.getTime();
        startIsoZ = toIsoStartOfDayZ(start);
        endIsoZ = toIsoEndOfDayZ(end);
        binding.tvDateRange.setText("Thời gian: " + toDisplayDate(start) + " - " + toDisplayDate(end));
    }

    private String toIsoStartOfDayZ(java.util.Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(c.getTime());
    }

    private String toIsoEndOfDayZ(java.util.Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(c.getTime());
    }

    private String toDisplayDate(java.util.Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        return sdf.format(d);
    }

    private void setupDatePicker() {
        binding.tvDateRange.setOnClickListener(v -> showDateRangePicker());
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Chọn khoảng thời gian")
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            Long startMs = selection.first;
            Long endMs = selection.second;
            if (startMs == null || endMs == null) return;
            java.util.Date start = new java.util.Date(startMs);
            java.util.Date end = new java.util.Date(endMs);
            startIsoZ = toIsoStartOfDayZ(start);
            endIsoZ = toIsoEndOfDayZ(end);
            binding.tvDateRange.setText("Thời gian: " + toDisplayDate(start) + " - " + toDisplayDate(end));
            fetchTopCustomers();
        });
        picker.show(getSupportFragmentManager(), "date_range_picker");
    }
}
