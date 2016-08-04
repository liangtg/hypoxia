package com.example;

public class MyClass {
    public static void main(String[] args) {
        int i = Integer.MAX_VALUE;
        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
        i += 1;
        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
        i = -1;
        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
        i = 1 << 7;
        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
        i -= 1;
        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
        i = (byte) -1;
        System.out.println("args = [" + i + "]" + Integer.toHexString(i));
    }
}
