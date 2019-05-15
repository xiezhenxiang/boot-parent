package indi.fly.boot.base.util;

/**
 * @author xiezhenxiang 2019/5/15
 */
public class ClassUtil {
    // 获取项目所在的绝对路径
    public static String getProjectDir() {
        String path = ClassUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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
