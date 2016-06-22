package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-6-22.
 */
public class HeartHistoryResponse extends BaseResponse {
    public ArrayList<HeartHistoryResponse.HistoryItem> list = new ArrayList<>();

    public static class HistoryItem {

        /**
         * heartrate : 68
         * time_test : 2016-06-22 14:13:00
         * user_id : 96
         * record_id : 15295
         * fullname : 高迎
         * height : 186
         * weight : 92
         * sexstring : 男
         * bloodstring : Unknow
         * birthday : 0001-01-01
         */

        public int heartrate;
        public String time_test;
    }
}
