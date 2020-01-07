package indi.shine.boot.base.util;

import java.util.regex.Pattern;

public class StringUtil {

    /** check str null or empty */
    public static boolean verify(String str){
        return str != null && !str.isEmpty();
    }

    public static boolean isChinese(String str) {
        return Pattern.matches("[\\u4E00-\\u9FA5]+", str);
    }

    public static boolean isInt(String str) {
        if (str != null ) {
            return Pattern.matches("^[-\\+]?[\\d]*$", str);
        }
        return false;
    }

    public static boolean isDouble(String str) {
        if (str != null) {
            return Pattern.matches("^(-?\\d+)\\.(\\d+)?$", str);
        }
        return false;
    }

    /**
     * yyyy-MM-dd or yyyy-MM-dd HH:mm:ss
     **/
    public static boolean isDate(String str) {
        if (str != null) {
            return Pattern.matches("^\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,2}[ ]{0,1}\\d{0,2}[:]{0,1}\\d{0,2}[:]{0,1}\\d{0,2}[:]{0,1}\\d{0,3}$", str);
        }
        return false;
    }
}
