package indi.shine.boot.base.util;

/**
 * 算法工具类
 * @author xiezhenxiang 2019/8/1
 */
public class AlgorithmUtil {

    /**
     * 生成无符号整数型的hash算法
     **/
    public static Integer elfHash(String str) {

        int hash = 0;

        for (int i = 0; i < str.length(); i ++) {

            hash = (hash << 4) + str.charAt(i);
            long x = hash & 0xf0000000L;
            if (x != 0) {
                hash ^= (x >> 24);
                hash &= ~x;
            }
        }
        return (hash & 0x7FFFFFFF);
    }
}
