package indi.shine.boot.base.util;

import java.util.regex.Pattern;

/**
 * 对象类型判断
 * @author xiezhenxiang 2019/6/13
 **/
public class TypeCheckUtil {

    public static boolean IntCheck(Object str) {
        if (str == null) {
            return false;
        }
        if (!Pattern.matches("^[-\\+]?[\\d]*$", str.toString())) {
            return false;
        }
        return true;
    }

    public static boolean whitDecimalPointIntCheck(Object str) {
        if (str == null) {
            return false;
        }
        if (!Pattern.matches("^\\d+(\\.(0)+)?$", str.toString())) {
            return false;
        }
        return true;
    }

    public static boolean doubleCheck(Object str) {
        if (str == null) {
            return false;
        }
        if (!Pattern.matches("^(-?\\d+)(\\.\\d+)?$", str.toString())) {
            return false;
        }
        return true;
    }

    public static boolean booleanCheck(Object str) {
        if (str == null) {
            return false;
        }
        if (str instanceof Boolean) {
            return true;
        }
        if (str.toString().equals("true") || str.toString().equals("false")) {
            return true;
        }
        return false;
    }


    /** 0000-00-00 */
    public static boolean dateCheck(Object str) {
        if (str == null) {
            return false;
        }
        if (!Pattern.matches("^((?:19|20)\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$", str.toString())) {
            return false;
        }
        return true;
    }
}