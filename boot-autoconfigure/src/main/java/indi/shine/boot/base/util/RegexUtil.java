package indi.shine.boot.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiezhenxiang 2019/8/6
 */
public class RegexUtil {

    /**
     * 正则表达式匹配两个指定字符串中间的内容
     */
    public static List<String> subRegex(String str, String regex){

        List<String> ls = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            ls.add(m.group());
        }
        return ls;
    }

}
