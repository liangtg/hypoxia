package com.syber.hypoxia.data;

import com.google.gson.annotations.SerializedName;
import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-13.
 */
public class HypoxiaChartResponse extends BaseResponse {
    public ArrayList<ChartItem> chart = new ArrayList<>();
    public ArrayList<ChartItem> total = new ArrayList<>();

    public static class ChartItem {
        @SerializedName("Key")
        public String key;
        public String totallength;
    }

    public static class ChartTotal {
        public String totallength;
        public String totalltimes;
    }
}
