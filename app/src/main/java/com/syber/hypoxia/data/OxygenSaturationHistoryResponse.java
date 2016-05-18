package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-18.
 */
public class OxygenSaturationHistoryResponse extends BaseResponse {
    public ArrayList<HistoryItem> list = new ArrayList<>();

    public static class HistoryItem {
        public TimeTest spo2;
        public long time_start;
    }

    public static class TimeTest {
        public String Time_Test;
        public int O2p;
        public int HeartRate;
    }

}
