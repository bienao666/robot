package com.bienao.robot.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class JiNianRiUtils {
    public static int getLianAi(){
        return calculationLianAi("2022-12-11");
    }
    public static int getBirthdayJo(){
        try {
            return calculationBirthday("2009-03-09");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static int getBirthdayHui(){
        try {
            return calculationBirthday("2020-01-11");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 计算生日天数
    public static int calculationBirthday(String clidate) throws ParseException {
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cToday = Calendar.getInstance(); // 存今天
        Calendar cBirth = Calendar.getInstance(); // 存生日
        cBirth.setTime(myFormatter.parse(clidate)); // 设置生日
        cBirth.set(Calendar.YEAR, cToday.get(Calendar.YEAR)); // 修改为本年
        int days;
        if (cBirth.get(Calendar.DAY_OF_YEAR) < cToday.get(Calendar.DAY_OF_YEAR)) {
            // 生日已经过了，要算明年的了
            days = cToday.getActualMaximum(Calendar.DAY_OF_YEAR) - cToday.get(Calendar.DAY_OF_YEAR);
            days += cBirth.get(Calendar.DAY_OF_YEAR);
        } else {
            // 生日还没过
            days = cBirth.get(Calendar.DAY_OF_YEAR) - cToday.get(Calendar.DAY_OF_YEAR);
        }
        // 输出结果
        if (days == 0) {
            return 0;
        } else {
            return days;
        }
    }

    // 计算天数
    public static int calculationLianAi(String date) {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int day = 0;
        try {
            long time = System.currentTimeMillis() - simpleDateFormat.parse(date).getTime();
            day = (int) (time / 86400000L);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }
}
