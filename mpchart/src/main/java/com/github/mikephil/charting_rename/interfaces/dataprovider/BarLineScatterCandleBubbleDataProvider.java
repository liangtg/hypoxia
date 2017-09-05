package com.github.mikephil.charting_rename.interfaces.dataprovider;

import com.github.mikephil.charting_rename.components.YAxis.AxisDependency;
import com.github.mikephil.charting_rename.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting_rename.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    int getMaxVisibleCount();
    boolean isInverted(AxisDependency axis);
    
    int getLowestVisibleXIndex();
    int getHighestVisibleXIndex();

    BarLineScatterCandleBubbleData getData();
}
