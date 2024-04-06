/**
 * @Author：ouyhjian
 * @Package：com.nainuo.wx.mp.myBusiness.horeBusiness.horeUtils
 * @Project：wx-mp
 * @name：TimestampUtils
 * @Date：2024/3/31 14:50
 * @Filename：TimestampUtils
 */
package com.nainuo.VO;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TimestampUtils {

    /**
     * 获取当前时间戳（精确到毫秒）
     *
     * @return 当前时间戳
     */
    public static long getCurrentTimestampMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（Java 8以后推荐使用Instant，精确到纳秒）
     *
     * @return 当前时间戳（Instant类型）
     */
    public static Instant getCurrentTimestamp() {
        return Instant.now();
    }

    /**
     * 将时间戳转换为Date对象
     *
     * @param timestamp 时间戳（毫秒）
     * @return Date对象
     */
    public static Date convertToUtilDate(long timestamp) {
        return new Date(timestamp*1000);
    }

    /**
     * 将时间戳转换为LocalDateTime对象
     *
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime对象
     */
    public static LocalDateTime convertToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * 将Date对象转换为时间戳
     *
     * @param date Date对象
     * @return 时间戳（秒）
     */
    public static long convertToDateTimestamp(Date date) {
        return date.getTime()/1000;
    }

    /**
     * 将LocalDateTime对象转换为时间戳
     *
     * @param dateTime LocalDateTime对象
     * @return 时间戳（毫秒）
     */
    public static long convertToLocalDateTimeTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取当前数据库时间戳（java.sql.Timestamp类型）
     *
     * @return 当前数据库时间戳
     */
    @Deprecated // 在新项目中推荐使用java.time包下的类，但对于旧版数据库API兼容可能仍需使用
    public static Timestamp getCurrentSqlTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
