package com.snowapp.jjfunny.utils;

/**
 * @date 2020-08-21
 * @author snow
 * @description 字符串转换工具类
 */
public class StringConvert {

    /**
     * @date 2020-08-21
     * @author snow
     * @param count 点赞/分享/踩 数量
     * @return 数量转换
     */
    public static String convertFeedUgc(int count) {
        if (count < 10000) {
            return String.valueOf(count);
        }
        return count / 10000 + "万";
    }

}
