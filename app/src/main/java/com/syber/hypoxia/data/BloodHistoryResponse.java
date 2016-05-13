package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-5-12.
 */
public class BloodHistoryResponse extends BaseResponse {
    public ArrayList<HistoryItem> list = new ArrayList<>();

    public static class HistoryItem {
    }
}
