package indi.shine.boot.base.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * @author xiezhenxiang 2019/5/14
 */
public class TimeUtil {

    // 时间戳转LocalDateTime
    public static LocalDateTime timestampToTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), TimeZone.getDefault().toZoneId());
    }
}
