package com.annimon.plugin;

public class Plugin {

    public static final double GOLDEN_RATIO = 1.61803398875;

    public static int sum(int x, int y) {
        return x + y;
    }

    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}
