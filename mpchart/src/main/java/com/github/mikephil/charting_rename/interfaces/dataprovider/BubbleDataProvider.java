package com.github.mikephil.charting_rename.interfaces.dataprovider;

import com.github.mikephil.charting_rename.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}
