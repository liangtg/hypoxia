package com.syber.hypoxia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.buffer.ScatterBuffer;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.renderer.BubbleChartRenderer;
import com.github.mikephil.charting.renderer.CandleStickChartRenderer;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;
import com.github.mikephil.charting.renderer.DataRenderer;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.renderer.ScatterChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.OxygenSaturationChartResponse;
import com.syber.hypoxia.data.OxygenSaturationHistoryResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * Created by liangtg on 16-5-18.
 */
public class OxygenSaturationFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener {
    private CombinedChart barChart;
    private TextView selectedDate, totalTimes;
    private RecyclerView allHistory;
    private ProgressBar progressBar;
    private Bus bus = new Bus();
    private int page = 0;

    private ArrayList<OxygenSaturationHistoryResponse.HistoryItem> data = new ArrayList<>();
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
        dayProvider = new ChartDataProvider(day, day, true);
        weekProvider = new ChartDataProvider(weekStart, weekEnd, false);
        monthProvider = new ChartDataProvider(monthStart, monthEnd, false);
        curProvider = dayProvider;
        bus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oxygen_saturation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        selectedDate = get(R.id.selected_date);
        totalTimes = get(R.id.total_times);
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
        IRequester.getInstance().spoData(bus, page);
        curProvider.updateChart();
    }

    private void initChart() {
        barChart.setNoDataText("");
        barChart.getPaint(Chart.PAINT_INFO).setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                16,
                getResources().getDisplayMetrics()));
        barChart.getLegend().setEnabled(false);
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
    public void withData(OxygenSaturationHistoryResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        if (event.isSuccess()) {
            data.addAll(event.list);
            historyAdapter.notifyDataSetChanged();
            page++;
            if (!event.list.isEmpty()) nextRequest();
        } else {
            nextRequest();
        }
    }

    private void nextRequest() {
        totalTimes.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null == getView() || getActivity().isFinishing()) return;
                IRequester.getInstance().bloodData(bus, page);
            }
        }, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_oxygen_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            OxygenSaturationHistoryResponse.HistoryItem item = data.get(position);
            holder.date.setText(item.spo2.Time_Test);
            holder.spo.setText(String.format("血氧%d%%", item.spo2.O2p));
            holder.rate.setText("心率" + item.spo2.HeartRate);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, spo, rate;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            spo = BaseViewHolder.get(itemView, R.id.high);
            rate = BaseViewHolder.get(itemView, R.id.rate);
        }
    }

    class ChartDataProvider {
        OxygenSaturationChartResponse dataResponse;
        boolean working = false;
        private String startDate, endDate;
        private CombinedData barData;
        private Bus bus = new Bus();
        private boolean max;

        public ChartDataProvider(String startDate, String endDate, boolean max) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.max = max;
            bus.register(this);
        }

        @Subscribe
        public void withData(OxygenSaturationChartResponse event) {
            if (getView() == null || getActivity().isFinishing()) return;
            working = false;
            if (event.isSuccess()) {
                dataResponse = event;
                if (curProvider == this) createData();
            } else if (curProvider == this) {
                progressBar.setVisibility(View.GONE);
                showToast("数据获取失败");
                fillData();
                createData();
            }
        }

        private void fillData() {
            OxygenSaturationChartResponse data = new OxygenSaturationChartResponse();
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                OxygenSaturationChartResponse.ChartItem item = new OxygenSaturationChartResponse.ChartItem();
                item.spO2Max = random.nextInt(10) + 130;
                item.spO2Avg = random.nextInt(10) + 120;
                item.spO2Min = random.nextInt(10) + 110;
                item.heartRateMax = random.nextInt(10) + 100;
                item.heartRateAvg = random.nextInt(10) + 90;
                item.heartRateMin = random.nextInt(10) + 80;
                item.key = "12:11";
                data.chart.add(item);
            }
            OxygenSaturationChartResponse.ChartTotal total = new OxygenSaturationChartResponse.ChartTotal();
            data.total.add(total);
            dataResponse = data;
        }

        public void refresh() {
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<Entry> rateYVals = new ArrayList<>();
            ArrayList<BarEntry> yVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                OxygenSaturationChartResponse.ChartItem item = dataResponse.chart.get(i);
                xVals.add(item.key);
                int sys, dia;
                if (max) {
                    rateYVals.add(new Entry(item.heartRateMax, i));
                    yVals.add(new BarEntry(item.spO2Max, i));
                } else {
                    yVals.add(new BarEntry(item.spO2Avg, i));
                    rateYVals.add(new Entry(item.heartRateAvg, i));
                }
            }
            BarDataSet dataSet = new BarDataSet(yVals, "");
            dataSet.setColor(0x80FFFFFF);
            dataSet.setHighLightAlpha(0);
            barData = new CombinedData(xVals);
            BarData candleData = new BarData(xVals, dataSet);
            barData.setData(candleData);
            ScatterDataSet scatterDataSet = new ScatterDataSet(rateYVals, "");
            scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            scatterDataSet.setScatterShapeSize(10);
            scatterDataSet.setColor(0xFFFF0000);
            scatterDataSet.setDrawHighlightIndicators(false);
            ScatterData scatterData = new ScatterData(xVals, scatterDataSet);
            barData.setData(scatterData);
            barData.setDrawValues(false);
            resetData();
        }

        private void resetData() {
            barChart.clear();
            barChart.resetTracking();
            if (barData.getYValCount() > 0) {
                barChart.setData(barData);
            } else {
                barChart.setNoDataText("您还没有测量过血氧");
            }
            SPOCombineRender renderer = new SPOCombineRender();
            barChart.setRenderer(renderer);
            renderer.initBuffers();
            barChart.invalidate();
            barChart.animateY(500);
            progressBar.setVisibility(View.GONE);
            ArrayList<OxygenSaturationChartResponse.ChartTotal> total = dataResponse.total;
            totalTimes.setText(String.format("累计%d次", total.isEmpty() ? 0 : total.get(0).totallTimes));
            selectedDate.setText(String.format("%s~%s", startDate, endDate));
        }

        public void updateChart() {
            progressBar.setVisibility(View.VISIBLE);
            selectedDate.setText(String.format("%s~%s", startDate, endDate));
            totalTimes.setText("");
            if (working) {
                barChart.clear();
                return;
            }
            if (null == dataResponse) {
                working = true;
                barChart.clear();
                IRequester.getInstance().spoChartData(bus, startDate, endDate);
            } else if (null == barData) {
                createData();
            } else {
                resetData();
            }
        }

    }

    private class IMarkerView extends MarkerView {
        private TextView sys, dia, pul;

        /**
         * Constructor. Sets up the MarkerView with a custom layout resource.
         *
         * @param context
         */
        public IMarkerView(Context context) {
            super(context, R.layout.marker_blood);
            sys = BaseViewHolder.get(this, R.id.sys);
            dia = BaseViewHolder.get(this, R.id.dia);
            pul = BaseViewHolder.get(this, R.id.pul);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            OxygenSaturationChartResponse.ChartItem item = curProvider.dataResponse.chart.get(e.getXIndex());
        }

        @Override
        public int getXOffset(float xpos) {
            if (xpos < getMeasuredWidth() / 2) {
                return (int) -xpos;
            } else if (xpos + getMeasuredWidth() / 2 > barChart.getMeasuredWidth()) {
                return (int) (xpos - barChart.getWidth() + getMeasuredWidth()) * -1;
            } else {
                return -getMeasuredWidth() / 2;
            }
        }

        @Override
        public int getYOffset(float ypos) {
            return (int) -ypos;
        }
    }

    private class SPOCombineRender extends CombinedChartRenderer {

        public SPOCombineRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
        }

        @Override
        protected void createRenderers(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {

            mRenderers = new ArrayList<DataRenderer>();

            CombinedChart.DrawOrder[] orders = chart.getDrawOrder();

            for (CombinedChart.DrawOrder order : orders) {

                switch (order) {
                    case BAR:
                        if (chart.getBarData() != null) mRenderers.add(new OxygenRender());
                        break;
                    case BUBBLE:
                        if (chart.getBubbleData() != null) mRenderers.add(new BubbleChartRenderer(chart, animator, viewPortHandler));
                        break;
                    case LINE:
                        if (chart.getLineData() != null) mRenderers.add(new LineChartRenderer(chart, animator, viewPortHandler));
                        break;
                    case CANDLE:
                        if (chart.getCandleData() != null) mRenderers.add(new CandleStickChartRenderer(chart, animator, viewPortHandler));
                        break;
                    case SCATTER:
                        if (chart.getScatterData() != null) mRenderers.add(new BPScatterRender());
                        break;
                }
            }
        }
    }


    private class BPScatterRender extends ScatterChartRenderer {
        int width, height;
        private Drawable drawable;

        public BPScatterRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.heart, getActivity().getTheme());
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
        }

        @Override
        protected void drawDataSet(Canvas c, IScatterDataSet dataSet) {
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();
            ScatterBuffer buffer = mScatterBuffers[0];
            buffer.setPhases(phaseX, phaseY);
            buffer.feed(dataSet);
            trans.pointValuesToPixel(buffer.buffer);
            int left, top;
            for (int i = 0; i < buffer.size(); i += 2) {
                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[i])) break;
                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[i]) || !mViewPortHandler.isInBoundsY(buffer.buffer[i + 1])) continue;
                left = (int) (buffer.buffer[i] - width / 2);
                top = (int) (buffer.buffer[i + 1] - height / 2);
                drawable.setBounds(left, top, left + width, top + height);
                drawable.draw(c);
            }
        }
    }

    private class OxygenRender extends BarChartRenderer {
        int width;
        private Drawable drawable;

        public OxygenRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.oxygen_bar, getActivity().getTheme());
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
