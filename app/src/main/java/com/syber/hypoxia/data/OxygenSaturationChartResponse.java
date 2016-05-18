package com.syber.hypoxia.data;

import com.google.gson.annotations.SerializedName;
import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-18.
 */
public class OxygenSaturationChartResponse extends BaseResponse {
    public ArrayList<ChartItem> chart = new ArrayList<>();
    public ArrayList<ChartTotal> total = new ArrayList<>();

    public static class ChartItem {
        @SerializedName("Key")
        public String key;
        @SerializedName("SpO2Max")
        public float spO2Max;
        @SerializedName("SpO2Avg")
        public float spO2Avg;
        @SerializedName("SpO2Min")
        public float spO2Min;
        @SerializedName("HeartRateMax")
        public float heartRateMax;
        @SerializedName("HeartRateAvg")
        public float heartRateAvg;
        @SerializedName("HeartRateMin")
        public float heartRateMin;
    }

    public static class ChartTotal {
        @SerializedName("SpO2Avg")
        public float spO2Avg;
        @SerializedName("HeartRateAvg")
        public float heartRateAvg;
        @SerializedName("TotallTimes")
        public int totallTimes;
    }

}
