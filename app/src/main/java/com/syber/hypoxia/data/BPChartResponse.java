package com.syber.hypoxia.data;

import com.google.gson.annotations.SerializedName;
import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-13.
 */
public class BPChartResponse extends BaseResponse {
    public ArrayList<ChartItem> chart = new ArrayList<>();
    public ArrayList<ChartItem> total = new ArrayList<>();

    public static class ChartItem {
        @SerializedName("Key")
        public String key;
        @SerializedName("DiastolicMax")
        public int diastolicMax;
        @SerializedName("DiastolicMin")
        public int diastolicMin;
        @SerializedName("SystolicMax")
        public int systolicMax;
        @SerializedName("SystolicMin")
        public int systolicMin;
        @SerializedName("HeartRateMax")
        public int heartRateMax;
        @SerializedName("HeartRateMin")
        public int heartRateMin;
    }

    public static class ChartTotal {
        @SerializedName("DiastolicMax")
        public int diastolicMax;
        @SerializedName("DiastolicMin")
        public int diastolicMin;
        @SerializedName("SystolicMax")
        public int systolicMax;
        @SerializedName("SystolicMin")
        public int systolicMin;
        @SerializedName("HeartRateMax")
        public int heartRateMax;
        @SerializedName("HeartRateMin")
        public int heartRateMin;
        public int totalltimes;
    }
}
