package com.boot.base.util;

/**
 * 字符串相关
 */
public class StringUnit {
    public static boolean haveTest(String str) {
        if (str == null)
            return false;
        if ("".equals(str))
            return false;
        return true;
    }

}