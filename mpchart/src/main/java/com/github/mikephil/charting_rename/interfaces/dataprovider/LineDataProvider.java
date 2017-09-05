package com.github.mikephil.charting_rename.interfaces.dataprovider;

import com.github.mikephil.charting_rename.components.YAxis;
import com.github.mikephil.charting_rename.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
