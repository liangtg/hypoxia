package com.syber.hypoxia;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.renderer.BubbleChartRenderer;
import com.github.mikephil.charting.renderer.CandleStickChartRenderer;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.renderer.ScatterChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.OxygenSaturationChartResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by liangtg on 16-5-18.
 */
public class OxygenSaturationChartFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, FragmentManager.OnBackStackChangedListener, OnChartValueSelectedListener {
    private int lastHight = -1;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private CombinedChart barChart;
    private TextView selectedDate, lastPeriod, nextPeriod, highlightDate, highlightOxygen, highlightPul;
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
        setHasOptionsMenu(true);
        createProvider();
        getFragmentManager().addOnBackStackChangedListener(this);
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
        return inflater.inflate(R.layout.fragment_oxygen_saturation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        abnormal = get(R.id.abnormal);
        highlightDate = get(R.id.highlight_date);
        highlightOxygen = get(R.id.highlight_oxygen);
        highlightPul = get(R.id.highlight_pul);
        selectedDate = get(R.id.selected_date);
        progressBar = get(R.id.progress);
        barChart = get(R.id.chart);
        barChart.setOnChartValueSelectedListener(this);

        lastPeriod = get(R.id.last_period);
        nextPeriod = get(R.id.next_period);
        lastPeriod.setOnClickListener(this);
        nextPeriod.setOnClickListener(this);
        get(R.id.oxygen_detail).setOnClickListener(this);
        get(R.id.add_oxygen).setOnClickListener(this);
        get(R.id.refresh).setOnClickListener(this);

        RadioGroup group = get(R.id.date_group);
        group.setOnCheckedChangeListener(this);
        initChart();
        curProvider.updateChart();
    }

    @Override
    public void onResume() {
        super.onResume();
        curProvider.refresh();
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
    public void onDestroy() {
        super.onDestroy();
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
        if (R.id.refresh == id) {
            curProvider.refresh();
        } else if (R.id.oxygen_detail == id) {
            getFragmentManager().beginTransaction().hide(this).add(R.id.fragment_container,
                    new OxygenSaturationHistoryFragment(),
                    "oxygen_history").addToBackStack("oxygen_history").commit();
        } else if (R.id.add_oxygen == id) {
            gotoActivity(AddSPOActivity.class);
        } else if (R.id.last_period == id) {
            curProvider.lastPeriod();
        } else if (R.id.next_period == id) {
            curProvider.nextPeriod();
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            getFragmentManager().beginTransaction().show(this).commit();
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        lastHight = h.getXIndex();
        OxygenSaturationChartResponse.ChartItem item = curProvider.dataResponse.chart.get(lastHight);
        highlightDate.setText(item.key);
        int spo = (int) (dayProvider == curProvider ? item.spO2Max : item.spO2Avg);
        highlightOxygen.setText(String.valueOf(spo));
        int pul = (int) (dayProvider == curProvider ? item.heartRateMax : item.heartRateAvg);
        highlightPul.setText(String.valueOf(pul));
        abnormal.setVisibility(spo < 90 ? View.VISIBLE : View.GONE);
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
        OxygenSaturationChartResponse dataResponse;
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
        public void withData(OxygenSaturationChartResponse event) {
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
            progressBar.setVisibility(View.VISIBLE);
            if (working) {
                return;
            }
            barData = null;
            working = true;
            IRequester.getInstance().spoChartData(bus, sdf.format(startDate.getTime()), sdf.format(endDate.getTime()));
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<Entry> rateYVals = new ArrayList<>();
            ArrayList<BarEntry> yVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                OxygenSaturationChartResponse.ChartItem item = dataResponse.chart.get(i);
                xVals.add(item.key);
                if (dayProvider == curProvider) {
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
            LineDataSet lineDataSet = new LineDataSet(rateYVals, "");
            lineDataSet.setColor(0xFFFF0000);
            lineDataSet.setCircleColor(0xFFFF0000);
            lineDataSet.setCircleColorHole(0xFFFF0000);
            lineDataSet.setHighlightEnabled(false);
            barData.setData(new LineData(xVals, lineDataSet));
            barData.setDrawValues(false);
            resetData();
        }

        private void resetData() {
            barChart.clear();
            barChart.resetTracking();
            if (barData.getYValCount() > 0) {
                barChart.setData(barData);
                barChart.highlightValue(new Highlight(0, 0), true);
            } else {
                barChart.setNoDataText("您还没有测量过血氧");
            }
            SPOCombineRender renderer = new SPOCombineRender();
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

    private class SPOCombineRender extends CombinedChartRenderer {

        public SPOCombineRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
        }

        @Override
        protected void createRenderers(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {

            mRenderers = new ArrayList<>();

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

    private class OxygenLineRender extends LineChartRenderer {

        public OxygenLineRender(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);
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
        private Drawable drawable, highLight;
        private int highHalfWidth;

        public OxygenRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.oxygen_bar, getActivity().getTheme());
            width = drawable.getIntrinsicWidth();
            highLight = getResources().getDrawable(R.drawable.high_light_green, getActivity().getTheme());
            highHalfWidth = highLight.getIntrinsicWidth() / 2;
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
                    if (buffer.buffer[j + 3] > barChart.getViewPortHandler().contentBottom()) {
                        buffer.buffer[j + 3] = barChart.getViewPortHandler().contentBottom();
                    }
                    drawable.setBounds((int) buffer.buffer[j], (int) buffer.buffer[j + 1], (int) buffer.buffer[j + 2], (int) buffer.buffer[j + 3]);
                    drawable.draw(c);
                }
            }
        }

        @Override
        public void drawHighlighted(Canvas c, Highlight[] indices) {
        }

        @Override
        public void drawExtras(Canvas c) {
            Highlight[] indices = barChart.getHighlighted();
            if (null == indices) return;
            int setCount = mChart.getBarData().getDataSetCount();

            for (int i = 0; i < indices.length; i++) {

                Highlight h = indices[i];
                int index = h.getXIndex();

                int dataSetIndex = h.getDataSetIndex();
                IBarDataSet set = mChart.getBarData().getDataSetByIndex(dataSetIndex);

                if (set == null || !set.isHighlightEnabled()) continue;

                Transformer trans = mChart.getTransformer(set.getAxisDependency());

                // check outofbounds
                if (index >= 0 && index < (mChart.getXChartMax() * mAnimator.getPhaseX()) / setCount) {

                    BarEntry e = set.getEntryForXIndex(index);

                    if (e == null || e.getXIndex() != index) continue;

                    float groupspace = mChart.getBarData().getGroupSpace();

                    // calculate the correct x-position
                    float x = index * setCount + dataSetIndex + groupspace / 2f + groupspace * index;

                    mBarRect.set(x, mChart.getYChartMax(), x, mChart.getYChartMin());
                    trans.rectValueToPixel(mBarRect, mAnimator.getPhaseY());
                    highLight.setBounds((int) (mBarRect.left - highHalfWidth),
                            0,
                            (int) (mBarRect.left + highHalfWidth),
                            (int) mBarRect.bottom + highLight.getIntrinsicHeight() / 2);
                    highLight.draw(c);
                }
            }
        }
    }

}
