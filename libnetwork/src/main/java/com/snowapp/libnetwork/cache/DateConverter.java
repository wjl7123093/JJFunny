package com.snowapp.libnetwork.cache;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * @date 2020-08-20
 * @author snow
 * @description 日期转换器
 */
public class DateConverter {
    @TypeConverter
    public static Long date2Long(Date date) {
        return date.getTime();
    }

    @TypeConverter
    public static Date long2Date(Long data) {
        return new Date(data);
    }
}
