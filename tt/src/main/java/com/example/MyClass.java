package com.example;

import java.io.IOException;

public class MyClass {
    public static void main(String[] args) throws IOException {
        JJ data = new JJ();
        AThread a1 = new AThread(data);
        AThread a2 = new AThread(data);
        AThread a3 = new AThread(data);
        AThread a4 = new AThread(data);
        System.out.println("start:" + data.add);
        a1.start();
        a2.start();
        a3.start();
        a4.start();
        try {
            a1.join();
            a2.join();
            a3.join();
            a4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end:" + data.add);
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
