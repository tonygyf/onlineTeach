package com.example.onlineteach.utils;

// 新的工具类：DateTimeUtils.java


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeUtils {

    public static Calendar convertLocalDateTimeToCalendar(LocalDateTime localDateTime) {
        return GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()));
    }
}