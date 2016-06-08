package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

/**
 * Created by liangtg on 16-6-8.
 */
public class UserSummaryResponse extends BaseResponse {
    public HypoxiaHistoryResponse.Training training = new HypoxiaHistoryResponse.Training();
    public BloodHistoryResponse.Pressure pressure = new BloodHistoryResponse.Pressure();
    public OxygenSaturationHistoryResponse.TimeTest spo2 = new OxygenSaturationHistoryResponse.TimeTest();
}
