package com.android.volley;


public class Log {
    public static final Object VERBOSE = false;

    public static void v(String tag, String s) {
        System.out.println(tag + " : " + s);
    }

    public static void e(String tag, String s, Throwable tr) {
        System.out.println(tag + " : " + s);
    }

    public static void d(String tag, String s) {
        System.out.println(tag + " : " + s);
    }

    public static void e(String tag, String s) {
        System.out.println(tag + " : " + s);
    }

    public static void wtf(String tag, String s) {
        System.out.println(tag + " : " + s);
    }

    public static boolean isLoggable(String tag, Object verbose) {
        return false;
    }

    public static void wtf(String tag, String s, Throwable tr) {
        System.out.println(tag + " : " + s);
    }
}
