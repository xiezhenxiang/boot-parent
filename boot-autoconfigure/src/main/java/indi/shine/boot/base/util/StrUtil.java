package indi.shine.boot.base.util;

import java.util.regex.Pattern;
/**
 * @author xiezhenxiang 2021/12/15
 **/
public class StrUtil {

    public static boolean isNotBlack(String... strArr){
        for (String str : strArr) {
            if (str == null || str.trim().isEmpty()) {
                return false;
            }
        }
        return true;
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

    public static boolean isFloat(String str) {
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
