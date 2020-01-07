package indi.shine.boot.base.util;

import java.util.regex.Pattern;

/**
 * 对象类型判断
 * @author xiezhenxiang 2019/6/13
 **/
public class TypeCheckUtil {

    public static boolean intCheck(String str) {
        if (str == null ) {
            return false;
        }
        if (!Pattern.matches("^[-\\+]?[\\d]*$", str)) {
            return false;
        }
        return true;
    }

    public static boolean doubleCheck(String str) {
        if (str != null) {
            if (Pattern.matches("^(-?\\d+)(\\.\\d+)?$", str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * yyyy-MM-dd or yyyy-MM-dd HH:mm:ss
     * @author xiezhenxiang 2019/6/17
     **/
    public static boolean dateCheck(String str) {
        if (str != null) {
            String format = "^((1|2)\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
            if (Pattern.matches(format, str.toString())) {
                return true;
            }
            format = "^((1|2)\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) [0-2]\\d:[0-5]\\d:[0-5]\\d$";
            if (Pattern.matches(format, str.toString())) {
                return true;
            }
        }
        return false;
    }
}