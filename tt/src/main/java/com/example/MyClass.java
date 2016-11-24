package com.example;

public class MyClass {
    public static void main(String[] args) {
//        int i = Integer.MAX_VALUE;
//        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
//        i += 1;
//        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
//        i = -1;
//        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
//        i = 1 << 7;
//        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
//        i -= 1;
//        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
//        i = (byte) -1;
//        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
        String a = "55aa06bc00c1";
        System.out.println(toHex(a.getBytes()));
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

}
