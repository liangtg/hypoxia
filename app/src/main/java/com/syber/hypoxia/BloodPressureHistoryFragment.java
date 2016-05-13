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

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.data.BPChartResponse;
import com.syber.hypoxia.data.BloodHistoryResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by liangtg on 16-5-10.
 */
public class BloodPressureHistoryFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener {
    private CandleStickChart barChart;
    private RecyclerView allHistory;
    private Bus bus = new Bus();
    private int page = 0;

    private ArrayList<BloodHistoryResponse.HistoryItem> data = new ArrayList<>();
    private HistoryAdapter historyAdapter;
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
        return inflater.inflate(R.layout.fragment_bloodpressure_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        barChart = get(R.id.chart);
        barChart.setNoDataText("");
        barChart.setScaleEnabled(false);
        allHistory = get(R.id.all_history);
        allHistory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        allHistory.setItemAnimator(new DefaultItemAnimator());
        historyAdapter = new HistoryAdapter();
        allHistory.setAdapter(historyAdapter);
        RadioGroup group = get(R.id.date_group);
        group.setOnCheckedChangeListener(this);
        IRequester.getInstance().bloodData(bus, page);
    }

    @Subscribe
    public void withData(BloodHistoryResponse event) {
        if (getActivity().isFinishing()) return;
        if (event.isSuccess()) {
            data.addAll(event.list);
            historyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
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
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bloodpressure_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            BloodHistoryResponse.HistoryItem item = data.get(position);
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
        BPChartResponse dataResponse;
        boolean working = false;
        private String startDate, endDate;
        private CandleData barData;
        private Bus bus = new Bus();

        public ChartDataProvider(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            bus.register(this);
        }

        @Subscribe
        public void withData(BPChartResponse event) {
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
            ArrayList<CandleEntry> yVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                BPChartResponse.ChartItem item = dataResponse.chart.get(i);
                xVals.add(item.key);
                yVals.add(new CandleEntry(i, item.systolicMax, item.diastolicMin, item.systolicMax, item.diastolicMin));
            }
            CandleDataSet dataSet = new CandleDataSet(yVals, "");
            dataSet.setDrawValues(false);
            dataSet.setColor(0xFFFEA846);
            barData = new CandleData(xVals, dataSet);
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
                IRequester.getInstance().bloodChartData(bus, startDate, endDate);
            } else if (null == barData) {
                createData();
            } else {
                barChart.clear();
                barChart.setData(barData);
            }
        }

    }

}
