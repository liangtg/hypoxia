package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-6-30.
 */
public class AdviceResponse extends BaseResponse {

    /**
     * data : [{"id":4,"doctor_id":1,"user_id":96,"doctor":"admin","usr":"高迎","content":"啦啦啦啦啦啦啦啦","time_create":"1466648980000"}]
     * total : 100
     */

    public int total;

    public ArrayList<DataBean> data = new ArrayList<>();

    /**
     * id : 4
     * doctor_id : 1
     * user_id : 96
     * doctor : admin
     * usr : 高迎
     * content : 啦啦啦啦啦啦啦啦
     * time_create : 1466648980000
     */
    public static class DataBean {
        public String id;
        public int doctor_id;
        public int user_id;
        public String doctor;
        public String usr;
        public String content;
        public long time_create;
        public boolean readed;
    }
}
