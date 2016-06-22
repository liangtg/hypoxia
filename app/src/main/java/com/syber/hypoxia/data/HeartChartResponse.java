package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-6-22.
 */
public class HeartChartResponse extends BaseResponse {
    public ArrayList<HeartChartResponse.ChartItem> chart = new ArrayList<>();

    public static class ChartItem {
        /**
         * Key : 2016-05-20
         * OprationTime : 1463094547000
         * HeartRateAvg : 51
         * HeartRateMax : 179
         * HeartRateMin : 40
         */

        public String Key;
        public String OprationTime;
        public float HeartRateAvg;
        public int HeartRateMax;
        public int HeartRateMin;
    }


}
