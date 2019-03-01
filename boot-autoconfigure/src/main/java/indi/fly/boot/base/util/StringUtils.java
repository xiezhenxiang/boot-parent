package indi.fly.boot.base.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isNullOrEmpty(String url){
        if(url==null || "".equals(url))
            return true;
        return false;
    }

    public static boolean check(String url){
        return !isNullOrEmpty(url);
    }

    public static boolean isChinese(String words) {
        Pattern chinesePattern = Pattern.compile("[\\u4E00-\\u9FA5]+");
        Matcher matcherResult = chinesePattern.matcher(words);
        return matcherResult.find();
    }
}
