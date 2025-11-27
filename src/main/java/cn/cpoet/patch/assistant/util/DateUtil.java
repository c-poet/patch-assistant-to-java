package cn.cpoet.patch.assistant.util;

import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * 时间工具
 *
 * @author CPoet
 */
public abstract class DateUtil {

    public final static String DATE_PURE_FORMAT = "yyyyMMdd";
    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DateUtil() {
    }

    private static final class DateTimeFormatterHolder {
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    }

    private static final class DatePureFormatterHolder {
        private static final DateTimeFormatter DATE_PURE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PURE_FORMAT);
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatterHolder.DATE_TIME_FORMATTER;
    }

    public static DateTimeFormatter getDatePureFormatter() {
        return DatePureFormatterHolder.DATE_PURE_FORMATTER;
    }

    /**
     * {@link FileTime}转{@link LocalDateTime}
     *
     * @param fileTime 时间
     * @return 时间
     */
    public static LocalDateTime toLocalDateTime(FileTime fileTime) {
        return fileTime == null ? null : LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    /**
     * {@link LocalDateTime}转{@link FileTime}
     *
     * @param time 时间
     * @return 时间
     */
    public static FileTime toFileTime(LocalDateTime time) {
        return time == null ? null : FileTime.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * {@link LocalDateTime}转{@link FileTime}，为空的时间下取当前时间
     *
     * @param time 时间
     * @return 时间
     */
    public static FileTime toFileTimeOrCur(LocalDateTime time) {
        return time == null ? toFileTime(LocalDateTime.now()) : toFileTime(time);
    }

    /**
     * 格式化时间
     *
     * @param temporal 时间
     * @return 格式化的时间
     */
    public static String formatDateTime(TemporalAccessor temporal) {
        return getDateTimeFormatter().format(temporal);
    }

    /**
     * 格式化时间
     *
     * @param date 时间
     * @return 格式化的时间
     */
    public static String formatDateTime(Date date) {
        return new SimpleDateFormat(DATE_TIME_FORMAT).format(date);
    }

    /**
     * 获取当前时间串
     *
     * @return 当前时间串
     */
    public static String curDateTime() {
        return formatDateTime(LocalDateTime.now());
    }

    /**
     * 获取当前日期
     * <p>格式：{@link #DATE_PURE_FORMAT}</p>
     *
     * @return 当前日期
     */
    public static String curDatePure() {
        return getDatePureFormatter().format(LocalDate.now());
    }
}
