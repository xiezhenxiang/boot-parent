package com.boot.base.util;

public class StringUtils {
    public static boolean isNullOrEmpty(String url){
        if(url==null || "".equals(url))
            return true;
        return false;
    }
}
