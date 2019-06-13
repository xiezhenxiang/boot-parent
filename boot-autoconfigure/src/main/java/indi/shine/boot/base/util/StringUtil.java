package indi.shine.boot.base.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /** check str null or empty */
    public static boolean verify(String str){
        return str != null && !str.equals("");
    }

    /** check str include chinese words or not */
    private static  final  Pattern CH_PATTERN = Pattern.compile("[\\u4E00-\\u9FA5]+");
    public static boolean isChinese(String str) {
        Matcher matcherResult = CH_PATTERN.matcher(str);
        return matcherResult.find();
    }
}
