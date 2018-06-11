package cn.zhonggu.barsf.iota.explorer.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by ZhuDH on 2018/4/9.
 */
public class TimeUtil {
    private static final DateTimeFormatter DFM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
                .format(DFM);
    }

    public static long toMill(String time){
        return Timestamp.valueOf(LocalDateTime.from(DFM.parse(time))).getTime();
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(format(System.currentTimeMillis()));
        System.out.println(toMill("2018-04-09 07:06:43"));
    }

}
