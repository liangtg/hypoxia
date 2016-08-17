package com.syber.hypoxia;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.hypoxia.data.HypoxiaChartResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by liangtg on 16-5-10.
 */
public class HypoxiaChartFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, FragmentManager.OnBackStackChangedListener, View.OnClickListener, OnChartValueSelectedListener {
    private int lastHight = -1;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private TextView selectedDate, lastPeriod, nextPeriod, highlightDate, highlightLength;
    private BarChart barChart;
    private ProgressBar progressBar;
    private Bus bus = new Bus();
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
        bus.register(this);
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
        return inflater.inflate(R.layout.fragment_hypoxia_chart, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        highlightDate = get(R.id.highlight_date);
        highlightLength = get(R.id.highlight_length);
        selectedDate = get(R.id.selected_date);
        progressBar = get(R.id.progress);
        barChart = get(R.id.chart);
        barChart.setOnChartValueSelectedListener(this);
        get(R.id.hypoxia_detail).setOnClickListener(this);
        get(R.id.add_hypoxia).setOnClickListener(this);
        get(R.id.refresh).setOnClickListener(this);
        lastPeriod = get(R.id.last_period);
        nextPeriod = get(R.id.next_period);
        lastPeriod.setOnClickListener(this);
        nextPeriod.setOnClickListener(this);
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
        xAxis.setYOffset(15);
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

    @Override
    public void onBackStackChanged() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            getFragmentManager().beginTransaction().show(this).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        curProvider.refresh();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.hypoxia_detail == id) {
            getFragmentManager().beginTransaction().hide(this).add(R.id.fragment_container,
                    new HypoxiaHistoryFragment(),
                    "hypoxia_history").addToBackStack("hypoxia_history").commit();
        } else if (R.id.add_hypoxia == id) {
            gotoActivity(AddTraingActivity.class);
        } else if (R.id.refresh == id) {
            curProvider.refresh();
        } else if (R.id.last_period == id) {
            curProvider.lastPeriod();
        } else if (R.id.next_period == id) {
            curProvider.nextPeriod();
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        lastHight = h.getXIndex();
        HypoxiaChartResponse.ChartItem item = curProvider.dataResponse.chart.get(h.getXIndex());
        highlightDate.setText(item.key);
        highlightLength.setText(item.totallength);
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
        HypoxiaChartResponse dataResponse;
        boolean working = false;
        private Calendar startDate;
        private Calendar endDate;
        private int period;
        private int periodField;
        private String format;
        private int lastText;
        private int nextText;
        private BarData barData;
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
        public void withData(HypoxiaChartResponse event) {
            if (getView() == null || getActivity().isFinishing()) return;
            working = false;
            if (event.isSuccess()) {
                dataResponse = event;
                if (curProvider == this) updateChart();
            } else if (curProvider == this) {
                showToast("数据获取失败");
                progressBar.setVisibility(View.GONE);
//                fillData();
            }
        }

        public void refresh() {
            if (working) return;
            progressBar.setVisibility(View.VISIBLE);
            working = true;
            barData = null;
            IRequester.getInstance().hypoxiaChartData(bus, sdf.format(startDate.getTime()), sdf.format(endDate.getTime()));
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
                barChart.highlightValue(new Highlight(0, 0), true);
            }
            barChart.animateY(500);
            barChart.invalidate();
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

    private class HypoxiaRender extends BarChartRenderer {
        int width;
        private Drawable drawable, highLight;
        private int highHalfWidth;

        public HypoxiaRender() {
            super(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.hypoxia_bar, getActivity().getTheme());
            width = drawable.getIntrinsicWidth();
            highLight = getResources().getDrawable(R.drawable.high_light_orange, getActivity().getTheme());
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
