package com.bjtu.zyj.jobadmin2.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String format(Date date, String pattern){

        Instant instant = date.toInstant();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
