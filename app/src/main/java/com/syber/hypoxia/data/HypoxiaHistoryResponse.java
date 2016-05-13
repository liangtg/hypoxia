package com.syber.hypoxia.data;

import com.google.gson.annotations.SerializedName;
import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-11.
 */
public class HypoxiaHistoryResponse extends BaseResponse {
    public ArrayList<HistoryItem> list = new ArrayList<>();

    public static class HistoryItem {
        public String user_name;// "12345678901",
        public String device_sn;// "140394",
        public String device_imei;// "123456789012345",
        public String first_activating;// "/Date(1459481050000)/",
        public String manufacturer;// "多美特",
        public String model;// "Unknow",
        public String err_no;// "08",
        public String err_desc;// "记忆操作失败",
        public String id;// null,
        public String device_id;// null,
        public String user_id;// null,
        public long time_start;// "/Date(1462793104000)/",
        public long time_end;// "/Date(1462793104000)/",
        public String time_upload;// "/Date(1462862448000)/",
        public String content_type;// 2,
        public String content;// "{\"Time_Start\":\"2016-05-09 19:25:04\",\"Time_End\":\"2016-05-09 19:25:04\",\"TrainingMode\":1}",
        public String origindata;// "40 55 50 52 41 43 54 43 3A 07 E0 05 09 13 19 04 07 E0 05 09 13 19 04 01 00 B4 00 05 00 03 00 05 10 45 4E 44 21",
        public String longitude;// 0,
        public String latitude;// 0,
        public String geohash;// null
        public Training training;
    }

    public static class Training {
        /**
         * 2016-5-26 11:29:44
         */
        @SerializedName("Time_Start")
        public String timeStart;
        @SerializedName("Time_End")
        public String timeEnd;
        @SerializedName("TrainingMode")
        public int trainingMode;
    }

}
