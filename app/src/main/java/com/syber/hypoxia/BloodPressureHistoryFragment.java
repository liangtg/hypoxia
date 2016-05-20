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
import com.github.mikephil.charting.buffer.ScatterBuffer;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.renderer.BubbleChartRenderer;
import com.github.mikephil.charting.renderer.CandleStickChartRenderer;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;
import com.github.mikephil.charting.renderer.DataRenderer;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.renderer.ScatterChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
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
import java.util.Random;

/**
 * Created by liangtg on 16-5-10.
 */
public class BloodPressureHistoryFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener {
    private CombinedChart barChart;
    private TextView selectedDate, totalTimes;
    private RecyclerView allHistory;
    private ProgressBar progressBar;
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
        dayProvider = new ChartDataProvider(day, day, true);
        weekProvider = new ChartDataProvider(weekStart, weekEnd, false);
        monthProvider = new ChartDataProvider(monthStart, monthEnd, false);
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
        IRequester.getInstance().bloodData(bus, page);
        curProvider.updateChart();
    }

    private void initChart() {
        barChart.setNoDataText("");
        barChart.getPaint(Chart.PAINT_INFO).setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                16,
                getResources().getDisplayMetrics()));
        barChart.setMarkerView(new IMarkerView(getActivity()));
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
    public void withData(BloodHistoryResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        if (event.isSuccess()) {
            data.addAll(event.list);
            historyAdapter.notifyDataSetChanged();
            page++;
            if (!event.list.isEmpty()) nextDelayRequest();
        } else {
            nextDelayRequest();
        }
    }

    private void nextDelayRequest() {
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
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bloodpressure_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            BloodHistoryResponse.HistoryItem item = data.get(position);
            holder.date.setText(item.pressure.Time_Test);
            holder.high.setText("收缩压" + item.pressure.Systolic);
            holder.low.setText("舒张压" + item.pressure.Diastolic);
            holder.rate.setText("心率" + item.pressure.HeartRate);
            holder.abnormal.setVisibility((item.pressure.Systolic < 130 && item.pressure.Diastolic < 85) ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, high, low, rate;
        View abnormal;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            high = BaseViewHolder.get(itemView, R.id.high);
            low = BaseViewHolder.get(itemView, R.id.low);
            rate = BaseViewHolder.get(itemView, R.id.rate);
            abnormal = BaseViewHolder.get(itemView, R.id.state);
        }
    }

    class ChartDataProvider {
        BPChartResponse dataResponse;
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
        public void withData(BPChartResponse event) {
            if (getView() == null || getActivity().isFinishing()) return;
            working = false;
            if (event.isSuccess()) {
                dataResponse = event;
                if (curProvider == this) createData();
            } else if (curProvider == this) {
                progressBar.setVisibility(View.GONE);
                showToast("数据获取失败");
//                fillData();
//                createData();
            }
        }

        private void fillData() {
            BPChartResponse data = new BPChartResponse();
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                BPChartResponse.ChartItem item = new BPChartResponse.ChartItem();
                item.systolicMax = random.nextInt(10) + 130;
                item.systolicAvg = random.nextInt(10) + 120;
                item.systolicMin = random.nextInt(10) + 110;
                item.diastolicMax = random.nextInt(10) + 80;
                item.diastolicAvg = random.nextInt(10) + 70;
                item.diastolicMin = random.nextInt(10) + 60;
                item.heartRateMax = random.nextInt(10) + 100;
                item.heartRateAvg = random.nextInt(10) + 90;
                item.heartRateMin = random.nextInt(10) + 80;
                item.key = "12:11";
                data.chart.add(item);
            }
            BPChartResponse.ChartTotal total = new BPChartResponse.ChartTotal();
            total.totalltimes = random.nextInt(20) + 40;
            data.total.add(total);
            dataResponse = data;
        }

        public void refresh() {
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<Entry> rateYVals = new ArrayList<>();
            ArrayList<CandleEntry> yVals = new ArrayList<>();
            ArrayList<CandleEntry> yValsLow = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                BPChartResponse.ChartItem item = dataResponse.chart.get(i);
                int sys, dia;
                xVals.add(item.key);
                if (max) {
                    rateYVals.add(new Entry(item.heartRateMax, i));
                    sys = item.systolicMax;
                    dia = item.diastolicMax;
                    yVals.add(new CandleEntry(i, sys, dia, sys, dia));
                } else {
                    rateYVals.add(new Entry(item.heartRateAvg, i));
                    yVals.add(new CandleEntry(i, item.systolicMax, item.systolicMin, item.systolicMax, item.systolicMin));
                    yValsLow.add(new CandleEntry(i, item.diastolicMax, item.diastolicMin, item.diastolicMax, item.diastolicMin));
                }
            }
            CandleDataSet dataSet = new CandleDataSet(yVals, "收缩压");
            dataSet.setColor(0x80FFFFFF);
            dataSet.setDrawHighlightIndicators(false);
            barData = new CombinedData(xVals);
            CandleData candleData = new CandleData(xVals, dataSet);
            if (!max) {
                dataSet = new CandleDataSet(yValsLow, "舒张压");
                dataSet.setColor(0x80FFFFFF);
                dataSet.setDrawHighlightIndicators(false);
                candleData.addDataSet(dataSet);
            }
            barData.setData(candleData);
            ScatterDataSet scatterDataSet = new ScatterDataSet(rateYVals, "心率");
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
                barChart.setNoDataText("您还没有测量过血压");
            }
            BPCombineRender renderer = new BPCombineRender();
            barChart.setRenderer(renderer);
            renderer.initBuffers();
            barChart.invalidate();
            barChart.animateY(500);
            progressBar.setVisibility(View.GONE);
            ArrayList<BPChartResponse.ChartTotal> total = dataResponse.total;
            totalTimes.setText(String.format("累计%d次", total.isEmpty() ? 0 : total.get(0).totalltimes));
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
                IRequester.getInstance().bloodChartData(bus, startDate, endDate);
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
            BPChartResponse.ChartItem item = curProvider.dataResponse.chart.get(e.getXIndex());
            if (curProvider.max) {
                sys.setText(getString(R.string.sys_format, (int) item.systolicMax));
                dia.setText(getString(R.string.dia_format, (int) item.diastolicMax));
                pul.setText(getString(R.string.pul_format, (int) item.heartRateMax));
            } else {
                sys.setText(String.format("收缩压:%s~%s", item.systolicMin, item.systolicMax));
                dia.setText(String.format("舒张压:%s~%s", item.diastolicMin, item.diastolicMax));
                pul.setText(getString(R.string.pul_format, (int) item.heartRateAvg));
            }
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

    private class BPCombineRender extends CombinedChartRenderer {

        public BPCombineRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
        }

        @Override
        protected void createRenderers(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {

            mRenderers = new ArrayList<DataRenderer>();

            CombinedChart.DrawOrder[] orders = chart.getDrawOrder();

            for (CombinedChart.DrawOrder order : orders) {

                switch (order) {
                    case BAR:
                        if (chart.getBarData() != null) mRenderers.add(new BarChartRenderer(chart, animator, viewPortHandler));
                        break;
                    case BUBBLE:
                        if (chart.getBubbleData() != null) mRenderers.add(new BubbleChartRenderer(chart, animator, viewPortHandler));
                        break;
                    case LINE:
                        if (chart.getLineData() != null) mRenderers.add(new LineChartRenderer(chart, animator, viewPortHandler));
                        break;
                    case CANDLE:
                        if (chart.getCandleData() != null) mRenderers.add(new BPRender());
                        break;
                    case SCATTER:
                        if (chart.getScatterData() != null) mRenderers.add(new BPScatterRender());
                        break;
                }
            }
        }
    }

    private class BPRender extends CandleStickChartRenderer {
        int[] setDrawables = {R.drawable.high_pressure, R.drawable.low_pressure};
        private float[] mBodyBuffers = new float[4];
        private Drawable[] drawables = new Drawable[setDrawables.length];


        public BPRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
            for (int i = 0; i < setDrawables.length; i++) {
                drawables[i] = ResourcesCompat.getDrawable(getResources(), setDrawables[i], getActivity().getTheme());
            }
        }

        @Override
        protected void drawDataSet(Canvas c, ICandleDataSet dataSet) {

            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();
            float barSpace = dataSet.getBarSpace();
            boolean showCandleBar = dataSet.getShowCandleBar();

            int minx = Math.max(mMinX, 0);
            int maxx = Math.min(mMaxX + 1, dataSet.getEntryCount());

            mRenderPaint.setStrokeWidth(dataSet.getShadowWidth());

            // draw the body
            for (int j = minx,
                 count = (int) Math.ceil((maxx - minx) * phaseX + (float) minx); j < count; j++) {

                // get the entry
                CandleEntry e = dataSet.getEntryForIndex(j);

                final int xIndex = e.getXIndex();

                if (xIndex < minx || xIndex >= maxx) continue;

                final float open = e.getOpen();
                final float close = e.getClose();
                final float high = e.getHigh();
                final float low = e.getLow();

                if (showCandleBar) {
                    // calculate the body
                    mBodyBuffers[0] = xIndex - 0.5f + barSpace;
                    mBodyBuffers[1] = close * phaseY;
                    mBodyBuffers[2] = (xIndex + 0.5f - barSpace);
                    mBodyBuffers[3] = open * phaseY;
                    trans.pointValuesToPixel(mBodyBuffers);
                    int index = mChart.getData().getIndexOfDataSet(dataSet);
                    int intrinsicWidth = drawables[index].getIntrinsicWidth();
                    float width = mBodyBuffers[2] - mBodyBuffers[0];
                    if (width > intrinsicWidth) {
                        mBodyBuffers[0] += (width - intrinsicWidth) / 2;
                        mBodyBuffers[2] -= (width - intrinsicWidth) / 2;
                    }
                    // draw body differently for increasing and decreasing entry
                    if (open > close) { // decreasing

                        if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getDecreasingColor());
                        }
                        mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());
                        c.drawRect(mBodyBuffers[0], mBodyBuffers[3], mBodyBuffers[2], mBodyBuffers[1], mRenderPaint);
                        int left = (int) (mBodyBuffers[0] + (mBodyBuffers[2] - mBodyBuffers[0]) / 2 - intrinsicWidth / 2);
                        int top = (int) (mBodyBuffers[3] - drawables[index].getIntrinsicHeight() / 2);
                        drawables[index].setBounds(left, top, left + intrinsicWidth, top + drawables[index].getIntrinsicHeight());
                        drawables[index].draw(c);
                        top = (int) (mBodyBuffers[1] - drawables[index].getIntrinsicHeight() / 2);
                        if (2 == mChart.getData().getDataSetCount()) index = 1;
                        drawables[index].setBounds(left, top, left + intrinsicWidth, top + drawables[index].getIntrinsicHeight());
                        drawables[index].draw(c);
                    } else if (open < close) {
                        if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getIncreasingColor());
                        }
                        mRenderPaint.setStyle(dataSet.getIncreasingPaintStyle());
                        c.drawRect(mBodyBuffers[0], mBodyBuffers[1], mBodyBuffers[2], mBodyBuffers[3], mRenderPaint);
                    } else { // equal values
                        if (dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getNeutralColor());
                        }
                        c.drawLine(mBodyBuffers[0], mBodyBuffers[1], mBodyBuffers[2], mBodyBuffers[3], mRenderPaint);
                    }
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


}
