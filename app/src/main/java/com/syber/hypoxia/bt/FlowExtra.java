package com.syber.hypoxia.bt;

/**
 * Created by liangtg on 16-11-22.
 */

public class FlowExtra {

    public static final String KEY_SYS = "sys";
    public static final String KEY_DIA = "dia";
    public static final String KEY_PUL = "pul";
    public static final String KEY_ECG = "ecg";
    public static final String KEY_TIME = "time";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_MODE = "mode";
    public static final String KEY_ERROR = "error";
    public static final String KEY_PUL_ARRAY = "pul_array";
    public static final String KEY_ECG_ARRAY = "ecg_array";
    public static final int CONFIRM_OK = 1;
    public static final int CONFIRM_CANCEL = 2;
    public static final int REQUEST_BIND = 1;
    public static final int REQUEST_BINDED_OTHER = 2;
    public static final int REQUEST_MATCHED = 3;
    public static final int RESULT_BP = 4;
    public static final int RESULT_HR = 5;
    public static final int RESULT_ECG = 6;
    public static final int RESULT_RAW_PUL = 7;
    public static final int RESULT_RAW_ECG = 8;
    public static final int REQUEST_CONFIRM_DISCONNECT = 9;
    public static final int PROGRESS_BP = 10;
    public static final int RESULT_HYPOXIA = 11;
    public static final int REQUEST_END = 12;
    public static final int REPORT_STATE = 1000;
    public static final int REPORT_STATE_CONNECTED = REPORT_STATE + 1;
    public static final int REPORT_STATE_CONNECT_FAILED = REPORT_STATE + 2;
    public static final int REPORT_STATE_INFO = REPORT_STATE + 3;
    public static final int REPORT_STATE_DISCONNECT = REPORT_STATE + 4;
}
