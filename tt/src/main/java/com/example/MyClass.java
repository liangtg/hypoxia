package com.example;

import com.google.gson.Gson;

import java.io.IOException;

public class MyClass {
    public static void main(String[] args) throws IOException {
        JJ a = new Gson().fromJson("{\"a\":\"b\",}", JJ.class);
        System.out.println(a.toString());
    }


    public static String toHex(byte[] to) {
        if (null == to || to.length == 0) return "[]";
        StringBuffer sb = new StringBuffer("[" + Integer.toHexString(to[0] & 0xFF));
        for (int i = 1; i < to.length; i++) {
            sb.append(" " + Integer.toHexString(to[i] & 0xFF));
        }
        sb.append("]");
        return sb.toString();
    }

    public static class JJ {
        public String a;
    }

}
