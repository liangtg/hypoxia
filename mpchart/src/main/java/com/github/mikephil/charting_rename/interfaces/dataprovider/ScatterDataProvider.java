package com.github.mikephil.charting_rename.interfaces.dataprovider;

import com.github.mikephil.charting_rename.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}
