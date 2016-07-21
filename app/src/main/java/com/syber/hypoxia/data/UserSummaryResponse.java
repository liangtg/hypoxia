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
    /**
     * cholsymbol : null
     * cholvalue : > 2.86
     * tgsymbol : null
     * tgvalue :  0.68
     * hdlsymbol : null
     * hdlvalue : >= 1.29
     * ldlsymbol : null
     * ldlvalue : < 1.25
     * time_test : -59011459200000
     * record_id : 15707
     */

    public BloodfatBean bloodfat;
    /**
     * sugarsymbol : null
     * sugarvalue : < 5.11
     * time_test : -59011459200000
     * record_id : 15693
     */

    public BloodsugarBean bloodsugar;
    /**
     * uricacidsymbol : null
     * uricacidvalue : >= 0.39
     * time_test : -59011459200000
     * record_id : 15694
     */

    public UricacidBean uricacid;

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

    public static class BloodfatBean {
        public String cholsymbol;
        public String cholvalue;
        public String tgsymbol;
        public String tgvalue;
        public String hdlsymbol;
        public String hdlvalue;
        public String ldlsymbol;
        public String ldlvalue;
        public String time_test;
        public int record_id;
    }

    public static class BloodsugarBean {
        public String sugarsymbol;
        public String sugarvalue;
        public String time_test;
        public int record_id;
    }

    public static class UricacidBean {
        public String uricacidsymbol;
        public String uricacidvalue;
        public String time_test;
        public int record_id;
    }
}
