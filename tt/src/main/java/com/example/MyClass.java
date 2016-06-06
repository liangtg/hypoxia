package com.example;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyClass {
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        System.out.println(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        System.out.println(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        System.out.println(sdf.format(cal.getTime()));
        System.out.println(cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    }
}
