package indi.shine.boot.base.util;

import org.apache.poi.ss.formula.functions.T;

/**
 * @author xiezhenxiang 2019/5/15
 */
public class ClassUtil {

    /**
     * 获取项目所在的绝对路径
     * @author xiezhenxiang 2019/6/13
     **/
    public static String getProjectDir(Class cls) {
        String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
        try{
            path = java.net.URLDecoder.decode(path, "UTF-8").replace("\\", "/");
        }catch (Exception e){
            e.printStackTrace();
        }
        int lastIndex = path.lastIndexOf("/", path.length() -2);
        path = path.substring(0, lastIndex + 1);
        return path;
    }
}
