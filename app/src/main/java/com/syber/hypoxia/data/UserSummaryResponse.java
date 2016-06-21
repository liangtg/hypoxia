package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

/**
 * Created by liangtg on 16-6-8.
 */
public class UserSummaryResponse extends BaseResponse {

    /**
     * diastolic : 89
     * systolic : 120
     * heartrate : 72
     * time_test : 1465803519000
     * record_id : 15273
     */

    public PressureBean pressure;
    /**
     * mode : 0
     * time_start : 1465803390000
     * time_end : 1465805490000
     * record_id : 15271
     */

    public TrainingBean training;
    /**
     * o2p : 97
     * heartrate : 77
     * time_test : 1465887104000
     * record_id : 15281
     */

    public Spo2Bean spo2;
    /**
     * heartrate : 77
     * time_test : 1465887104000
     * record_id : 15281
     */

    public HeartrateBean heartrate;

    public static class PressureBean {
        public int diastolic;
        public int systolic;
        public int heartrate;
        public String time_test;
        public int record_id;
    }

    public static class TrainingBean {
        public int mode;
        public String time_start;
        public String time_end;
        public int record_id;
    }

    public static class Spo2Bean {
        public int o2p;
        public int heartrate;
        public String time_test;
        public int record_id;
    }

    public static class HeartrateBean {
        public int heartrate;
        public String time_test;
        public int record_id;
    }
}
