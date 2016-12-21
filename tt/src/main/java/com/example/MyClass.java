package com.example;


import java.io.IOException;

public class MyClass {
    public static void main(String[] args) throws IOException {
        System.out.println("11111");
        System.out.println("\n".getBytes().length);
        System.out.println(Base64.encodeToString("你好啊".getBytes(), Base64.DEFAULT));
        System.out.println("11111");
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

    private static class AThread extends Thread {
        private JJ data;

        public AThread(JJ data) {
            this.data = data;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                data.add++;
            }
            System.out.println(data.add);
        }
    }

    public static class JJ {
        public String a;
        public int add = 0;
    }

}
