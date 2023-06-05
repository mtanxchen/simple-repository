package com.simple.repository.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间处理工具
 *
 * @author laiqx
 * @date 2022-12-07
 */
public class SimpleDateUtils {

    /**
     * 日期转字符串
     *
     * @param date       日期对象
     * @param dateFormat 日期格式
     * @return 返回日期字符串
     */
    public static String dateToStr(Date date, FormatType dateFormat) {
        return dateFormat.getFormat().format(date);
    }

    /**
     * 字符串转日期
     *
     * @param date       日期对象
     * @param dateFormat 日期格式
     * @return 返回日期字符串
     */
    public static Date strToDate(String date, FormatType dateFormat) {
        try {
            return dateFormat.getFormat().parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算俩个时间相差天数
     *
     * @param day  日期1
     * @param day2 日期2
     * @return 返回天数
     */
    public static Integer dimDd(Date day, Date day2) {
        if (null == day || null == day2) {
            return null;
        }
        Long dayTime = 24 * 3600 * 1000L;
        Long time = day2.getTime() - day.getTime();
        return (int) (Math.abs(time) / dayTime);
    }

    /**
     * 之前时间
     *
     * @param num  数值
     * @param type 时间类型
     * @return 返回当前时间num个type前的时间
     */
    public static Date beforeDate(Date date, int num, TimeType type) {
        long time = date.getTime() - num * type.millis;
        return new Date(time);
    }

    /**
     * 之后时间
     *
     * @param num  数值
     * @param type 时间类型
     * @return 返回当前时间num个type前的时间
     */
    public static Date afterDate(Date date, int num, TimeType type) {
        long time = date.getTime() + num * type.millis;
        return new Date(time);
    }

    /**
     * 获取当天开始时间
     *
     * @param date 时间
     * @return 返回开始时间
     */
    public static Date startDayTime(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取当天结束时间
     *
     * @param date 时间
     * @return 返回结束时间
     */
    public static Date endDayTime(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }


    /**
     * 时间类型
     */
    public enum TimeType {
        sec(SEC_MSEL),
        minute(MIN_MSEL),
        hour(HOUR_MSEL),
        day(DAY_MESL);
        public long millis;

        TimeType(long millis) {
            this.millis = millis;
        }
    }

    /**
     * 格式类型
     */
    public enum FormatType {
        DATE_FORMAT("yyyy-MM-dd HH:mm:ss"),
        DAY_FORMAT("yyyy-MM-dd"),
        YEAR_MONTH_FORMAT("yyyy-MM-dd"),
        MONTH_DAY_FORMAT("MM-dd"),
        SIMPLE_DATE_FORMAT("yyyyMMddHHmmss"),
        SIMPLE_DATE_FORMAT_HHMM("yyyyMMddHHmm"),
        SIMPLE_DAY_FORMAT("yyyyMMdd"),
        SIMPLE_YEAR_MONTH_FORMAT("yyyyMM"),
        SIMPLE_MONTH_DAY_FORMAT("MMdd");
        private SimpleDateFormat format;

        private String formatLabel;

        FormatType(String format) {
            this.format = new SimpleDateFormat(format);
            this.formatLabel = format;
        }

        public SimpleDateFormat getFormat() {
            return format;
        }

        public String getFormatLabel() {
            return formatLabel;
        }
    }

    /**
     * 1秒钟的毫秒数
     */
    public static final Long SEC_MSEL = 1000L;

    /**
     * 1分钟秒数
     */
    public static final Long MIN_SEC = 60L;

    /**
     * 1分钟毫秒数
     */
    public static final Long MIN_MSEL = 60 * 1000L;

    /**
     * 1小时秒数
     */
    public static final Long HOUR_SEC = 60 * 60L;

    /**
     * 1小时毫秒数
     */
    public static final Long HOUR_MSEL = 60 * 60 * 1000L;

    /**
     * 1天秒数
     */
    public static final Long DAY_SEC = 24 * 60 * 60L;

    /**
     * 1天毫秒数
     */
    public static final Long DAY_MESL = 24 * 60 * 60 * 1000L;

}
