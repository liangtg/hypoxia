package com.syber.hypoxia;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
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
public class HypoxiaChartFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, FragmentManager.OnBackStackChangedListener, View.OnClickListener {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private TextView selectedDate;
    private BarChart barChart;
    private ProgressBar progressBar;
    private Bus bus = new Bus();
    private ChartDataProvider dayProvider, weekProvider, monthProvider;
    private ChartDataProvider curProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createProvider();
        bus.register(this);
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    private void createProvider() {
        long now = System.currentTimeMillis();
        dayProvider = new ChartDataProvider(createCalendar(now), createCalendar(now), 1, Calendar.DAY_OF_YEAR, false);
        Calendar startDate = createCalendar(now), endDate = createCalendar(now);
        while (startDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) startDate.add(Calendar.DAY_OF_YEAR, -1);
        endDate.setTimeInMillis(startDate.getTimeInMillis());
        endDate.add(Calendar.DAY_OF_WEEK, 6);
        weekProvider = new ChartDataProvider(startDate, endDate, 7, Calendar.DAY_OF_YEAR, false);
        startDate = createCalendar(now);
        endDate = createCalendar(now);
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        monthProvider = new ChartDataProvider(startDate, endDate, 1, Calendar.MONTH, true);
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
        selectedDate = get(R.id.selected_date);
        progressBar = get(R.id.progress);
        barChart = get(R.id.chart);
        get(R.id.hypoxia_detail).setOnClickListener(this);
        get(R.id.add_hypoxia).setOnClickListener(this);
        get(R.id.refresh).setOnClickListener(this);
        get(R.id.last_period).setOnClickListener(this);
        get(R.id.next_period).setOnClickListener(this);
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

    class ChartDataProvider {
        HypoxiaChartResponse dataResponse;
        boolean working = false;
        private Calendar startDate;
        private Calendar endDate;
        private int period;
        private int periodField;
        private boolean max;
        private BarData barData;
        private Bus bus = new Bus();

        public ChartDataProvider(Calendar startDate, Calendar endDate, int period, int periodField, boolean max) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.period = period;
            this.periodField = periodField;
            this.max = max;
            bus.register(this);
        }

        private void nextPeriod() {
            if (working) return;
            startDate.add(periodField, period);
            endDate.add(periodField, period);
            if (max) {
                endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            refresh();
        }

        private void lastPeriod() {
            if (working) return;
            dataResponse = null;
            barData = null;
            startDate.add(periodField, period * -1);
            endDate.add(periodField, period * -1);
            if (max) {
                endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            refresh();
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
            progressBar.setVisibility(View.VISIBLE);
            if (working) return;
            working = true;
            IRequester.getInstance().hypoxiaChartData(bus, sdf.format(startDate.getTime()), sdf.format(endDate.getTime()));
        }

        void createData() {
            ArrayList<String> xVals = new ArrayList<>();
            ArrayList<BarEntry> yVals = new ArrayList<>();
            for (int i = 0; i < dataResponse.chart.size(); i++) {
                HypoxiaChartResponse.ChartItem item = dataResponse.chart.get(i);
                if (this == dayProvider) {
                    xVals.add(item.key.substring(11));
                } else {
                    xVals.add(item.key);
                }
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
        }

        public void updateChart() {
            progressBar.setVisibility(View.VISIBLE);
            selectedDate.setText(String.format("%s~%s", sdf.format(startDate.getTime()), sdf.format(endDate.getTime())));
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
