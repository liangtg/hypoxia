package com.syber.hypoxia;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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
import com.syber.hypoxia.data.HeartChartResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by liangtg on 16-6-22.
 */
public class HeartRateChartFragment extends BaseFragment implements View.OnClickListener, OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener {
    private int lastHight = -1;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private CombinedChart barChart;
    private TextView selectedDate, highlightPul, highlightDate;
    private TextView lastPeriod, nextPeriod;
    private ProgressBar progressBar;
    private View abnormal;

    private ChartDataProvider dayProvider, weekProvider, monthProvider;
    private ChartDataProvider curProvider;
    private Handler handler = new Handler();
    private Runnable highlightRunnable = new Runnable() {
        @Override
        public void run() {
            if (getView() == null || getActivity().isFinishing() || barChart.isEmpty()) return;
            barChart.highlightValue(lastHight, 0);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createProvider();
    }

    private void createProvider() {
        long now = System.currentTimeMillis();
        dayProvider = new ChartDataProvider(createCalendar(now),
                createCalendar(now),
                1,
                Calendar.DAY_OF_YEAR,
                "%tF",
                R.string.last_day,
                R.string.next_day);
        Calendar startDate = createCalendar(now), endDate = createCalendar(now);
        while (startDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) startDate.add(Calendar.DAY_OF_YEAR, -1);
        endDate.setTimeInMillis(startDate.getTimeInMillis());
        endDate.add(Calendar.DAY_OF_WEEK, 6);
        weekProvider = new ChartDataProvider(startDate, endDate, 7, Calendar.DAY_OF_YEAR, "%tY-%3$d周", R.string.last_week, R.string.next_week);
        startDate = createCalendar(now);
        endDate = createCalendar(now);
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        monthProvider = new ChartDataProvider(startDate, endDate, 1, Calendar.MONTH, "%tY-%tm", R.string.last_month, R.string.next_month);
        curProvider = dayProvider;
    }

    private Calendar createCalendar(long time) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTimeInMillis(time);
        return calendar;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_heart_rate_chart, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        highlightDate = get(R.id.highlight_date);
        abnormal = get(R.id.abnormal);
        lastPeriod = get(R.id.last_period);
        nextPeriod = get(R.id.next_period);
        lastPeriod.setOnClickListener(this);
        nextPeriod.setOnClickListener(this);
        highlightPul = get(R.id.highlight_pul);
        selectedDate = get(R.id.selected_date);
        progressBar = get(R.id.progress);
        barChart = get(R.id.chart);
        barChart.setOnChartValueSelectedListener(this);
        get(R.id.bp_detail).setOnClickListener(this);
        get(R.id.add_bp).setOnClickListener(this);
        get(R.id.refresh).setOnClickListener(this);

        RadioGroup group = get(R.id.date_group);
        group.setOnCheckedChangeListener(this);
        initChart();
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
        xAxis.setYOffset(15);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.add_bp == id) {
            gotoActivity(AddBPActivity.class);
        } else if (R.id.refresh == id) {
            curProvider.refresh();
        } else if (R.id.bp_detail == id) {
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new HeartRateHistoryFragment(), "hr_history").addToBackStack(
                    "hr_history").commit();
        } else if (R.id.last_period == id) {
            curProvider.lastPeriod();
        } else if (R.id.next_period == id) {
            curProvider.nextPeriod();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        curProvider.refresh();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        lastHight = h.getXIndex();
        HeartChartResponse.ChartItem item = curProvider.dataResponse.chart.get(lastHight);
        highlightDate.setText(item.Key);
        int pul = dayProvider == curProvider ? item.HeartRateMax : (int) item.HeartRateAvg;
        highlightPul.setText(String.valueOf(pul));
        abnormal.setVisibility(View.GONE);
    }

    @Override
    public void onNothingSelected() {
        if (lastHight < 0) {
            if (!barChart.isEmpty()) lastHight = 0;
        } else {
            if (barChart.isEmpty()) {
                lastHight = -1;
            } else if (lastHight >= barChart.getXValCount()) {
                lastHight = barChart.getXValCount() - 1;
            }
        }
        if (lastHight >= 0) handler.post(highlightRunnable);
    }


    class ChartDataProvider {
        HeartChartResponse dataResponse;
        boolean working = false;
        private Calendar startDate;
        private Calendar endDate;
        private int period;
        private int periodField;
        private String format;
        private int lastText;
        private int nextText;
        private CombinedData barData;
        private Bus bus = new Bus();

        public ChartDataProvider(Calendar startDate, Calendar endDate, int period, int periodField, String format, int lastText, int nextText) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.period = period;
            this.periodField = periodField;
            this.format = format;
            this.lastText = lastText;
            this.nextText = nextText;
            bus.register(this);
        }

        private void nextPeriod() {
            if (working) return;
            dataResponse = null;
            barData = null;
            startDate.add(periodField, period);
            endDate.add(periodField, period);
            if (Calendar.MONTH == periodField) {
                endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            updateChart();
        }

        private void lastPeriod() {
            if (working) return;
            dataResponse = null;
            barData = null;
            startDate.add(periodField, period * -1);
            endDate.add(periodField, period * -1);
            if (Calendar.MONTH == periodField) {
                endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            updateChart();
        }

        @Subscribe
        public void withData(HeartChartResponse event) {
            if (getView() == null || getActivity().isFinishing()) return;
            working = false;
            if (event.isSuccess()) {
                dataResponse = event;
                if (curProvider == this) updateChart();
            } else if (curProvider == this) {
                progressBar.setVisibility(View.GONE);
                showToast("数据获取失败");
            }
        }

        public void refresh() {
            if (working) {
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            barData = null;
            working = true;
            IRequester.getInstance().heartChartData(bus, sdf.format(startDate.getTime()), sdf.format(endDate.getTime()));
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<Entry> rateYVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                HeartChartResponse.ChartItem item = dataResponse.chart.get(i);
                xVals.add(item.Key);
                if (dayProvider == curProvider) {
                    rateYVals.add(new Entry(item.HeartRateMax, i));
                } else {
                    rateYVals.add(new Entry(item.HeartRateAvg, i));
                }
            }
            LineData lineData = new LineData(xVals);
            LineDataSet rateSet = new LineDataSet(rateYVals, "");
            rateSet.setColor(0x80CF4C4C);
            rateSet.setCircleColor(0xFFCF4C4C);
            rateSet.setDrawCircleHole(false);
            rateSet.setDrawFilled(false);
            rateSet.setDrawHorizontalHighlightIndicator(false);
            rateSet.setDrawVerticalHighlightIndicator(false);
            lineData.addDataSet(rateSet);
            lineData.setDrawValues(false);
            barData = new CombinedData(xVals);
            barData.setData(lineData);
            resetData();
        }

        private void resetData() {
            barChart.clear();
            barChart.resetTracking();
            if (barData.getYValCount() > 0) {
                barChart.setData(barData);
                barChart.highlightValue(new Highlight(0, 0), true);
            } else {
                barChart.setNoDataText("您还没有测量过血压");
            }
            BPCombineRender renderer = new BPCombineRender();
            barChart.setRenderer(renderer);
            renderer.initBuffers();
            barChart.invalidate();
            barChart.animateY(500);
            progressBar.setVisibility(View.GONE);
        }

        public void updateChart() {
            progressBar.setVisibility(View.VISIBLE);
            selectedDate.setText(String.format(format, startDate.getTime(), endDate.getTime(), startDate.get(Calendar.WEEK_OF_YEAR)));
            lastPeriod.setText(lastText);
            nextPeriod.setText(nextText);
            if (working) {
                barChart.clear();
                return;
            }
            if (null == dataResponse) {
                barChart.clear();
                refresh();
            } else if (null == barData) {
                createData();
            } else {
                resetData();
            }
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
                        if (chart.getLineData() != null) mRenderers.add(new BPLineRender(chart, animator, viewPortHandler));
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

    private class BPLineRender extends LineChartRenderer {
        private int highHalfWidth;
        private Drawable highLight;

        public BPLineRender(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);
            highLight = getResources().getDrawable(R.drawable.high_light_red, getActivity().getTheme());
            highHalfWidth = highLight.getIntrinsicWidth() / 2;
        }

        @Override
        public void drawExtras(Canvas c) {
            super.drawExtras(c);
            Highlight[] indices = barChart.getHighlighted();
            if (null == indices) return;

            for (int i = 0; i < indices.length; i++) {

                ILineDataSet set = mChart.getLineData().getDataSetByIndex(indices[i].getDataSetIndex());

                if (set == null || !set.isHighlightEnabled()) continue;

                int xIndex = indices[i].getXIndex(); // get the
                // x-position

                if (xIndex > mChart.getXChartMax() * mAnimator.getPhaseX()) continue;

                final float yVal = set.getYValForXIndex(xIndex);
                if (Float.isNaN(yVal)) continue;

                float y = yVal * mAnimator.getPhaseY(); // get
                // the
                // y-position

                float[] pts = new float[]{xIndex, y};

                mChart.getTransformer(set.getAxisDependency()).pointValuesToPixel(pts);

                highLight.setBounds((int) (pts[0] - highHalfWidth),
                        0,
                        (int) (pts[0] + highHalfWidth),
                        (int) (mViewPortHandler.contentBottom() + highLight.getIntrinsicHeight() / 2));
                highLight.draw(c);
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
