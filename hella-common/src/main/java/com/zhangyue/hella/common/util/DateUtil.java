package com.zhangyue.hella.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Description: 日期处理类 注意线程安全<br>
 * Copyright: Copyright (c) 2012 <br>
 * Company: www.renren.com
 * 
 * @author zhuhui{hui.zhu@renren-inc.com} 2012-12-25
 * @version 1.0
 */
public class DateUtil {

    public static String dateFormaterBySeconds(Date date) {
        SimpleDateFormat dateFormaterBySeconds = new SimpleDateFormat("yyyyMMddHHmmss");
        return date == null ? "" : dateFormaterBySeconds.format(date);
    }

    public static String dateFormaterByString(Date date) {
        SimpleDateFormat dateFormaterBySeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return date == null ? "" : dateFormaterBySeconds.format(date);
    }

    public static Date dateFormaterBySeconds(String date) throws ParseException {
        SimpleDateFormat dateFormaterBySeconds = new SimpleDateFormat("yyyyMMddHHmmss");
        return date == null ? null : dateFormaterBySeconds.parse(date);

    }

    /**
     * 格式化日期为“2012年 08.12日 01:05:59”
     * 
     * @param orlTime
     * @return
     */
    public static String parseCnDate(String orlTime) {
        if (orlTime == null || orlTime.length() <= 0) {
            return "";
        }// 2012 0808 10 53 29

        if (orlTime.length() < 12) {
            return "";
        }
        String sYear = orlTime.substring(0, 4);
        String sMonth = orlTime.substring(4, 6);
        String sDay = orlTime.substring(6, 8);

        String h = orlTime.substring(8, 10);
        String m = orlTime.substring(10, 12);
        String s = orlTime.substring(12, 14);
        return sYear + "年 " + sMonth + "." + sDay + "日 " + h + ":" + m + ":" + s;
    }

    public static String parseEnDate(String orlTime) {
        if (orlTime == null || orlTime.length() <= 0) {
            return "";
        }// 2012 0808 10 53 29

        if (orlTime.length() < 12) {
            return "";
        }
        String sYear = orlTime.substring(0, 4);
        String sMonth = orlTime.substring(4, 6);
        String sDay = orlTime.substring(6, 8);

        String h = orlTime.substring(8, 10);
        String m = orlTime.substring(10, 12);
        String s = orlTime.substring(12, 14);
        return sYear + "/" + sMonth + "/" + sDay + " " + h + ":" + m + ":" + s;
    }

    public static String getTodayDate() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormater.format(new Date());
    }

    public static String getYesDate() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        Date yesterDay = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        return dateFormater.format(yesterDay);
    }

    public static String getDateMinuteAgo(int min) {
        SimpleDateFormat dateFormaterBySeconds = new SimpleDateFormat("yyyyMMddHHmmss");
        Date yesterDay = new Date(System.currentTimeMillis() - 1000 * 60 * min);
        return dateFormaterBySeconds.format(yesterDay);
    }

    public static String getDateHourAgo(int hour) {
        SimpleDateFormat dateFormaterBySeconds = new SimpleDateFormat("yyyyMMddHHmmss");
        Date yesterDay = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * hour);
        String dateStr = dateFormaterBySeconds.format(yesterDay);
        return dateStr;
    }

    public static String getWeekAgo() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        Date weekAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7);
        return dateFormater.format(weekAgo);
    }

    public static String getMonthAgo() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        return dateFormater.format(calendar.getTime());
    }

    public static String getTomorrow() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        Date tomorrow = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        return dateFormater.format(tomorrow);
    }

    // 将时间秒转换成以小时为单位的数据
    public static String convertTimeOfSecond(long time) {
        int min = (int) (time / 60);
        int second = (int) (time % 60);
        if (min <= 60) {
            return min + "分钟 " + String.valueOf(second) + "秒";
        }
        int hour = min / 60;
        return hour + "小时 " + convertTimeOfSecond(time - hour * 3600);
    }
}
