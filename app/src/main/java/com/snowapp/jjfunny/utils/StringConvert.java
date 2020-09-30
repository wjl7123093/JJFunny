package com.snowapp.jjfunny.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

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

    // 转换观看人数
    public static String convertTagFeedList(int num) {
        if (num < 10000) {
            return num + "人观看";
        } else {
            return num / 10000 + "万人观看";
        }
    }

    public static CharSequence convertSpannable(int count, String intro) {
        String countStr = String.valueOf(count);
        SpannableString ss = new SpannableString(countStr + intro);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new AbsoluteSizeSpan(16, true), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

}
