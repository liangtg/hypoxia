package com.syber.base.data;

/**
 * Created by liangtg on 16-5-11.
 */
public class BaseResponse {
    public int code = -1;
    public String error;

    public BaseResponse defaultError(Exception e) {
        return this;
    }

    public boolean isSuccess() {
        return code < 300 && code > 99;
    }

    public Object getData() {
        return null;
    }

    public BaseResponse error(int code) {
        this.code = code;
        return this;
    }

}
