package com.syber.hypoxia;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
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
import java.util.Random;

/**
 * Created by liangtg on 16-5-10.
 */
public class HypoxiaHistoryFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener {
    private TextView selectedDate, totalTime;
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
        selectedDate = get(R.id.selected_date);
        totalTime = get(R.id.total_time);
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
        barChart.getPaint(Chart.PAINT_INFO).setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                16,
                getResources().getDisplayMetrics()));
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setRenderer(new HypoxiaRender());
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
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.enableGridDashedLine(5, 5, 5);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(color);
        xAxis.setAxisLineColor(color);
    }

    @Subscribe
    public void withData(HypoxiaHistoryResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        if (event.isSuccess()) {
            int old = data.size();
            data.addAll(event.list);
            historyAdapter.notifyItemRangeInserted(old, event.list.size());
            page++;
            if (!event.list.isEmpty()) nextDelayRequest();
        } else {
            nextDelayRequest();
        }
    }

    private void nextDelayRequest() {
        barChart.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null == getView() || getActivity().isFinishing()) return;
                IRequester.getInstance().hypoxiaData(bus, page);
            }
        }, 500);
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
            holder.mode.setText("模式" + (item.training.trainingMode + 1));
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
            } else if (curProvider == this) {
                showToast("数据获取失败");
                progressBar.setVisibility(View.GONE);
                fillData();
            }
        }

        private void fillData() {
            HypoxiaChartResponse data = new HypoxiaChartResponse();
            Random random = new Random();
            for (int i = 0, j = random.nextInt(10) + 5; i < j; i++) {
                HypoxiaChartResponse.ChartItem item = new HypoxiaChartResponse.ChartItem();
                item.key = "12:0" + i;
                item.totallength = random.nextInt(50) + 50 + "";
                data.chart.add(item);
            }
            HypoxiaChartResponse.ChartTotal total = new HypoxiaChartResponse.ChartTotal();
            total.totallength = random.nextInt(100) + 50 + "";
            data.total.add(total);
            dataResponse = data;
            createData();
        }

        public void refresh() {
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<BarEntry> yVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                HypoxiaChartResponse.ChartItem item = dataResponse.chart.get(i);
                xVals.add(item.key);
                if (TextUtils.isEmpty(item.totallength)) item.totallength = "0";
                yVals.add(new BarEntry(Float.valueOf(item.totallength), i));
            }
            BarDataSet dataSet = new BarDataSet(yVals, "时长/分钟");
            dataSet.setColor(0xFFFEA846);
            dataSet.setHighLightAlpha(0);
            barData = new BarData(xVals, dataSet);
            barData.setDrawValues(false);
            resetData();
        }

        private void resetData() {
            progressBar.setVisibility(View.GONE);
            barChart.clear();
            barChart.resetTracking();
            if (barData.getYValCount() <= 0) {
                barChart.setNoDataText("您还没有进行过训练");
            } else {
                barChart.setData(barData);
            }
            barChart.animateY(500);
            barChart.invalidate();
            selectedDate.setText(String.format("%s~%s", startDate, endDate));
            ArrayList<HypoxiaChartResponse.ChartTotal> total = dataResponse.total;
            totalTime.setText(String.format("累计%s分钟", total.isEmpty() ? "0" : total.get(0).totallength));
        }

        public void updateChart() {
            progressBar.setVisibility(View.VISIBLE);
            selectedDate.setText(String.format("%s~%s", startDate, endDate));
            totalTime.setText("");
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
                resetData();
            }
        }

    }

    private class HypoxiaRender extends BarChartRenderer {
        int width;
        private Drawable drawable;

        public HypoxiaRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.hypoxia_bar, getActivity().getTheme());
            width = drawable.getIntrinsicWidth();
        }

        @Override
        protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
            mShadowPaint.setColor(dataSet.getBarShadowColor());
            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();
            // initialize the buffer
            BarBuffer buffer = mBarBuffers[index];
            buffer.setPhases(phaseX, phaseY);
            buffer.setBarSpace(dataSet.getBarSpace());
            buffer.setDataSet(index);
            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
            buffer.feed(dataSet);
            trans.pointValuesToPixel(buffer.buffer);

            // draw the bar shadow before the values
            if (mChart.isDrawBarShadowEnabled()) {

                for (int j = 0; j < buffer.size(); j += 4) {

                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) continue;

                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break;

                    c.drawRect(buffer.buffer[j], mViewPortHandler.contentTop(), buffer.buffer[j + 2], mViewPortHandler.contentBottom(), mShadowPaint);
                }
            }

            // if multiple colors
            if (dataSet.getColors().size() > 1) {

                for (int j = 0; j < buffer.size(); j += 4) {

                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) continue;

                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break;

                    // Set the color for the currently drawn value. If the index
                    // is
                    // out of bounds, reuse colors.
                    mRenderPaint.setColor(dataSet.getColor(j / 4));
//                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3], mRenderPaint);
                    drawable.setBounds((int) buffer.buffer[j], (int) buffer.buffer[j + 1], (int) buffer.buffer[j + 2], (int) buffer.buffer[j + 3]);
                    drawable.draw(c);
                }
            } else {

                mRenderPaint.setColor(dataSet.getColor());

                for (int j = 0; j < buffer.size(); j += 4) {

                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) continue;

                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break;

//                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3], mRenderPaint);
                    float v = buffer.buffer[j + 2] - buffer.buffer[j];
                    if (v > width) {
                        buffer.buffer[j] += (v - width) / 2;
                        buffer.buffer[j + 2] -= (v - width) / 2;
                    }
                    drawable.setBounds((int) buffer.buffer[j], (int) buffer.buffer[j + 1], (int) buffer.buffer[j + 2], (int) buffer.buffer[j + 3]);
                    drawable.draw(c);
                }
            }
        }
    }

}
