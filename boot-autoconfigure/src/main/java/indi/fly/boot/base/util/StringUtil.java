package indi.fly.boot.base.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    // check str null or empty
    public static boolean verify(String str){
        return str != null && !str.equals("");
    }

    // check str include chinese words or not
    public static boolean isChinese(String str) {
        Pattern chinesePattern = Pattern.compile("[\\u4E00-\\u9FA5]+");
        Matcher matcherResult = chinesePattern.matcher(str);
        return matcherResult.find();
    }
}
