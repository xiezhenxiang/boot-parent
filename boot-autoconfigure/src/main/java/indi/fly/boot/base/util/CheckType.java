package indi.fly.boot.base.util;

import java.util.regex.Pattern;

/**
 * 类型判断
 */
public class CheckType {

    public static boolean haveTest(Object str) {
        if (str == null)
            return false;
        if (str instanceof String)
            return !StringUtils.isNullOrEmpty(str.toString());
        return true;
    }

    public static boolean IntCheck(Object str) {
        if (str == null)
            return false;
        if (!Pattern.matches("^[-\\+]?[\\d]*$", str.toString()))
            return false;
        return true;
    }

    public static boolean whitDecimalPointIntCheck(Object str) {
        if (str == null)
            return false;
        if (!Pattern.matches("^\\d+(\\.(0)+)?$", str.toString()))
            return false;
        return true;
    }

    public static boolean doubleCheck(Object str) {
        if (str == null)
            return false;
        if (!Pattern.matches("^(-?\\d+)(\\.\\d+)?$", str.toString()))
            return false;
        return true;
    }

    public static boolean booleanCheck(Object str) {
        if (str == null)
            return false;
        if (str instanceof Boolean)
            return true;
        if (str.toString().equals("true") || str.toString().equals("false"))
            return true;
        return false;
    }


    //0000-00-00
    public static boolean dateCheck(Object str) {
        if (str == null)
            return false;
        if (!Pattern.matches("^((?:19|20)\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$", str.toString()))
            return false;
        return true;
    }

    //0000-00-00 00:00:00
    public static boolean dayCheck(Object str) {
        if (str == null)
            return false;
        if (!Pattern.matches("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s((([0-1][0-9])|(2?[0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$", str.toString()))
            return false;
        return true;
    }

    // 00:00:00
    public static boolean timeCheck(Object str) {
        if (str == null)
            return false;
        if (!Pattern.matches("([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?", str.toString()))
            return false;
        return true;
    }
}
