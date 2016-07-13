package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-7-13.
 */
public class BloodLipidResponse extends BaseResponse {

    public ArrayList<DataItem> list = new ArrayList<>();

    public static class DataItem {

        /**
         * 15703,
         **/
        public String record_id;
        /**
         * 96,
         **/
        public String user_id;
        /**
         * "高迎",
         **/
        public String fullname;
        /**
         * "1467777283000",
         **/
        public String bftime_test;
        /**
         * "2016-07-06 11:54:43"
         **/
        public String bftime_test_string;
        /**
         * "0.677", 总胆固醇测量值
         **/
        public String chol;
        /**
         * "0.15 ~ 0.42 mmol/L", 参考值
         **/
        public String cholref;
        /**
         * "+", 超标标识 " ↑ ":高于; " ↓ ":低于
         **/
        public String cholflag;
        /**
         * "0.677", 甘油三酯测量值
         **/
        public String tg;
        /**
         * "0.15 ~ 0.42 mmol/L", 参考值
         **/
        public String tgref;
        /**
         * "+", 超标标识 " ↑ ":高于; " ↓ ":低于
         **/
        public String tgflag;
        /**
         * "0.677", 高密度脂蛋白测量值
         **/
        public String hdl;
        /**
         * "0.15 ~ 0.42 mmol/L", 参考值
         **/
        public String hdlref;
        /**
         * "+", 超标标识 " ↑ ":高于; " ↓ ":低于
         **/
        public String hdlflag;
        /**
         * "0.677", 低密度脂蛋白测量值
         **/
        public String ldl;
        /**
         * "0.15 ~ 0.42 mmol/L", 参考值
         **/
        public String ldlref;
        /**
         * "+", 超标标识 " ↑ ":高于; " ↓ ":低于
         **/
        public String ldlflag;
        /**
         * "1467777283000",
         **/
        public String bstime_test;
        /**
         * "2016-07-06 11:54:43"
         **/
        public String bstime_test_string;
        /**
         * "0.677", 血糖测量值
         **/
        public String bloodsugar;
        /**
         * "0.15 ~ 0.42 mmol/L", 参考值
         **/
        public String bloodsugarref;
        /**
         * "+", 超标标识 " ↑ ":高于; " ↓ ":低于
         **/
        public String bloodsugarflag;
        /**
         * "1467777283000",
         **/
        public String uatime_test;
        /**
         * "2016-07-06 11:54:43"
         **/
        public String uatime_test_string;
        /**
         * "0.677", 血尿酸测量值
         **/
        public String uricacid;
        /**
         * "0.15 ~ 0.42 mmol/L", 参考值
         **/
        public String uricacidref;
        /**
         * "+", 超标标识 " ↑ ":高于; " ↓ ":低于
         **/
        public String uricacidflag;
    }
}
