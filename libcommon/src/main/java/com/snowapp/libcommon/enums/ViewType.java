package com.snowapp.libcommon.enums;

/**
 * Adapter 里 item 类型
 */
public enum ViewType {

    TYPE_IMAGE(1, "图片"),
    TYPE_VIDEO(2, "视频");

    public final int type;
    public final String value;

    ViewType(int type, String value) {
        this.type = type;
        this.value = value;
    }
}
