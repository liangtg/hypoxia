package com.syber.hypoxia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.data.HypoxiaChartResponse;
import com.syber.hypoxia.data.HypoxiaHistoryResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liangtg on 16-5-10.
 */
public class HypoxiaHistoryFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener {
    private BarChart barChart;
    private RecyclerView allHistory;
    private ProgressBar progressBar;
    private Bus bus = new Bus();
    private int page = 0;
    private ArrayList<HypoxiaHistoryResponse.HistoryItem> data = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private String day, weekStart, weekEnd, monthStart, monthEnd;
    private ChartDataProvider dayProvider, weekProvider, monthProvider;
    private ChartDataProvider curProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        day = sdf.format(cal.getTime());
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_YEAR, -1);
        weekStart = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        weekEnd = sdf.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        monthStart = sdf.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        monthEnd = sdf.format(cal.getTime());
        dayProvider = new ChartDataProvider(day, day);
        weekProvider = new ChartDataProvider(weekStart, weekEnd);
        monthProvider = new ChartDataProvider(monthStart, monthEnd);
        curProvider = dayProvider;
        bus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hypoxia_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        progressBar = get(R.id.progress);
        barChart = get(R.id.chart);
        allHistory = get(R.id.all_history);
        allHistory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        allHistory.setItemAnimator(new DefaultItemAnimator());
        historyAdapter = new HistoryAdapter();
        allHistory.setAdapter(historyAdapter);
        RadioGroup group = get(R.id.date_group);
        group.setOnCheckedChangeListener(this);
        initChart();
        IRequester.getInstance().hypoxiaData(bus, page);
        curProvider.updateChart();
    }

    private void initChart() {
        barChart.setNoDataText("");
        barChart.getAxisRight().setEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDescription("");
        YAxis yAxis = barChart.getAxisLeft();
        int color = 0xFFFFFFFF;
        yAxis.setAxisLineColor(color);
        yAxis.setDrawGridLines(true);
        yAxis.enableGridDashedLine(5, 5, 2);
        yAxis.setTextColor(color);
        yAxis.setGridColor(color);
        yAxis.setDrawZeroLine(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(5, 5, 5);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(color);
        xAxis.setAxisLineColor(color);
    }

    @Subscribe
    public void withData(HypoxiaHistoryResponse event) {
        if (getActivity().isFinishing()) return;
        progressBar.setVisibility(View.GONE);
        if (event.isSuccess()) {
            int old = data.size();
            data.addAll(event.list);
            historyAdapter.notifyItemRangeInserted(old, event.list.size());
            page++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        dayProvider.bus.unregister(dayProvider);
        weekProvider.bus.unregister(weekProvider);
        monthProvider.bus.unregister(monthProvider);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Logger.d(getResources().getResourceName(checkedId));
        if (R.id.day == checkedId) {
            curProvider = dayProvider;
        } else if (R.id.week == checkedId) {
            curProvider = weekProvider;
        } else if (R.id.month == checkedId) {
            curProvider = monthProvider;
        }
        curProvider.updateChart();
    }

    private class HistoryAdapter extends RecyclerView.Adapter<AdapterHolder> {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

        @Override
        public AdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hypoxia_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            HypoxiaHistoryResponse.HistoryItem item = data.get(position);
            holder.date.setText(dateFormat.format(new Date(item.time_start)));
            holder.time.setText(String.format("%s~%s", timeFormat.format(new Date(item.time_start)), timeFormat.format(new Date(item.time_end))));
            long minute = (item.time_end - item.time_start) / 1000 / 60;
            holder.minute.setText(String.format("%d分钟", minute));
            holder.mode.setText("模式" + item.training.trainingMode);
            holder.progressBar.setProgress((int) minute);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, time, minute, mode;
        ProgressBar progressBar;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            time = BaseViewHolder.get(itemView, R.id.time);
            minute = BaseViewHolder.get(itemView, R.id.minute);
            mode = BaseViewHolder.get(itemView, R.id.mode);
            progressBar = BaseViewHolder.get(itemView, R.id.progress);
        }
    }

    class ChartDataProvider {
        HypoxiaChartResponse dataResponse;
        boolean working = false;
        private String startDate, endDate;
        private BarData barData;
        private Bus bus = new Bus();

        public ChartDataProvider(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            bus.register(this);
        }

        @Subscribe
        public void withData(HypoxiaChartResponse event) {
            if (getView() == null || getActivity().isFinishing()) return;
            working = false;
            if (event.isSuccess()) {
                dataResponse = event;
                if (curProvider == this) createData();
            }
        }

        public void refresh() {
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<BarEntry> yVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                HypoxiaChartResponse.ChartItem item = dataResponse.chart.get(i);
                xVals.add(item.key);
                yVals.add(new BarEntry(Float.valueOf(item.totallength), i));
            }
            BarDataSet dataSet = new BarDataSet(yVals, "时长/分钟");
            dataSet.setDrawValues(false);
            dataSet.setColor(0xFFFEA846);
            barData = new BarData(xVals, dataSet);
            barChart.setData(barData);
            barChart.invalidate();
        }

        public void updateChart() {
            if (working) {
                barChart.clear();
                return;
            }
            if (null == dataResponse) {
                working = true;
                barChart.clear();
                IRequester.getInstance().hypoxiaChartData(bus, startDate, endDate);
            } else if (null == barData) {
                createData();
            } else {
                barChart.clear();
                barChart.setData(barData);
            }
        }

    }

}
