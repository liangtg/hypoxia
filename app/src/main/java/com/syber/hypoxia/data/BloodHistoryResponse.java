package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-12.
 */
public class BloodHistoryResponse extends BaseResponse {
    public ArrayList<HistoryItem> list = new ArrayList<>();

    @Override
    public String toString() {
        return "BloodHistoryResponse{" +
                "list=" + list +
                '}';
    }

    public static class HistoryItem {
        /**
         * 12345678901
         **/
        public String user_name;
        /**
         * 160301000005619
         **/
        public String device_sn;
        /**
         * 460010120512490
         **/
        public String device_imei;
        /**
         * 多美特
         **/
        public String manufacturer;
        /**
         * Unknow
         **/
        public String model;
        /****/
        public String err_no;
        /****/
        public String err_desc;
        /**
         * 0
         **/
        public int traininglength;
        /**
         * 1459481050000
         **/
        public String first_activating;

        /**
         * null
         **/
        public String training;
        /**
         * 13486
         **/
        public String id;
        /**
         * 1
         **/
        public String device_id;
        /**
         * 79
         **/
        public String user_id;
        /**
         * 1463131956000
         **/
        public String time_start;
        /**
         * null
         **/
        public String time_end;
        /**
         * 1463068857000
         **/
        public String time_upload;
        /**
         * 1
         **/
        public String content_type;
        /**
         * {\"Time_Test\":\"2016-05-13 17:32:36\",\"Diastolic\":108,\"Systolic\":104,\"HeartRate\":169}
         **/
        public String content;
        /**
         * 40 55 50 50 52 45 53 53 3A 07 E0 05 0D 11 20 24 00 6C 00 68 00 A9 00 45 4E 44 21
         **/
        public String origindata;
        /**
         * 39.959129431
         **/
        public String longitude;
        /**
         * 116.432117125
         **/
        public String latitude;
        /**
         * uzurvrcrgpyrzpupcxfrbrgzzzzzzzzz
         **/
        public String geohash;

        public Pressure pressure;

        @Override
        public String toString() {
            return "HistoryItem{" +
                    "user_name='" + user_name + '\'' +
                    ", device_sn='" + device_sn + '\'' +
                    ", device_imei='" + device_imei + '\'' +
                    ", manufacturer='" + manufacturer + '\'' +
                    ", model='" + model + '\'' +
                    ", err_no=" + err_no +
                    ", err_desc=" + err_desc +
                    ", traininglength=" + traininglength +
                    ", first_activating='" + first_activating + '\'' +
                    ", training=" + training +
                    ", id=" + id +
                    ", device_id=" + device_id +
                    ", user_id=" + user_id +
                    ", time_start='" + time_start + '\'' +
                    ", time_end=" + time_end +
                    ", time_upload='" + time_upload + '\'' +
                    ", content_type=" + content_type +
                    ", content='" + content + '\'' +
                    ", origindata='" + origindata + '\'' +
                    ", longitude=" + longitude +
                    ", latitude=" + latitude +
                    ", geohash='" + geohash + '\'' +
                    ", pressure=" + pressure +
                    '}';
        }
    }

    public static class Pressure {
        /**
         * 2016-05-13 17:32:36
         **/
        public String Time_Test;
        /**
         * 108
         **/
        public int Diastolic;
        /**
         * 104
         **/
        public int Systolic;
        /**
         * 169
         **/
        public int HeartRate;

        @Override
        public String toString() {
            return "Pressure{" +
                    "Time_Test='" + Time_Test + '\'' +
                    ", Diastolic=" + Diastolic +
                    ", Systolic=" + Systolic +
                    ", HeartRate=" + HeartRate +
                    '}';
        }
    }

}
