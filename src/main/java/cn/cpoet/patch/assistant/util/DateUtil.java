package cn.cpoet.patch.assistant.util;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * 时间工具
 *
 * @author CPoet
 */
public abstract class DateUtil {

    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DateUtil() {
    }

    /**
     * 格式化时间
     *
     * @param temporal 时间
     * @return 格式化的时间
     */
    public static String formatDateTime(TemporalAccessor temporal) {
        return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(temporal);
    }
}
