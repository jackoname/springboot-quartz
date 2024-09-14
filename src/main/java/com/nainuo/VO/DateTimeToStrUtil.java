/**
 * @Author：ouyhjian
 * @Package：com.nainuo.VO
 * @Project：springboot-quartz
 * @name：DateTimeToStrUtil
 * @Date：2024/4/6 12:19
 * @Filename：DateTimeToStrUtil
 */
package com.nainuo.VO;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeToStrUtil {
    /**
     * 返回字符串
     *
     * 给原本的时间originDate加上自定义的时间
     * @param originDate 原本的时间
     * @param day 要加的天数
     * @param hour 要加的小时数
     * @param minute 要加的分钟数
     * @param second 要加的秒数
     * @return 返回加完时间后的时间str_goalDate
     */
    public static String addDateTimeToStr(Date originDate, int day, int hour, int minute, int second) {
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(originDate);
        cal.add(Calendar.DATE, day);// 24小时制,加天
        cal.add(Calendar.HOUR, hour);// 24小时制 ,加小时
        cal.add(Calendar.MINUTE, minute);// 24小时制,加分钟
        cal.add(Calendar.SECOND, second);// 24小时制,加秒

        String str_goalDate = dateFormate.format(cal.getTime());
        return str_goalDate;
    }

    /**
     * 返回java.util.Date
     *
     * 给原本的时间originDate加上自定义的时间
     * @param originDate 原本的时间
     * @param day 要加的天数
     * @param hour 要加的小时数
     * @param minute 要加的分钟数
     * @param second 要加的秒数
     * @return 返回加完时间后的时间goalDate
     */
    public static Date addDateTimeToDate(Date originDate, int day, int hour, int minute,int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(originDate);
        cal.add(Calendar.DATE, day);// 24小时制,加天
        cal.add(Calendar.HOUR, hour);// 24小时制 ,加小时
        cal.add(Calendar.MINUTE, minute);// 24小时制,加分钟
        cal.add(Calendar.SECOND, second);// 24小时制,加秒

        Date goalDate = cal.getTime();
        return goalDate;
    }

    public static Long getTimeDifference(Date date1) {
        // 定义时间格式
     //   Date date1 = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2); // 两天前
        Date date2 = new Date(); // 当前时间

        // 计算时间差（毫秒）
        long difference = date2.getTime() - date1.getTime();

        // 转换时间差为天、小时、分钟和秒
        long days = difference / (1000 * 60 * 60 * 24);
        long hours = (difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (difference % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (difference % (1000 * 60)) / 1000;

        // 输出结果
      //  System.out.println("Difference: " + days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds");

        return days;
    }

}
