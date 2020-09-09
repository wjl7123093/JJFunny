package com.snowapp.libnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/**
 * @date 2020-08-19
 * @author snow
 * @description Json 转换器（将json字符串转换为指定类型）
 */
public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject object = JSON.parseObject(response);
        // 获取最外层 data（固定结构）
        JSONObject data = object.getJSONObject("data");
        if (null != data) {
            // 第二层 data（数据）
            Object data2 = data.get("data");
            return JSON.parseObject(data2.toString(), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clazz) {
        JSONObject object = JSON.parseObject(response);
        // 获取最外层 data（固定结构）
        JSONObject data = object.getJSONObject("data");
        if (null != data) {
            // 第二层 data（数据）
            Object data2 = data.get("data");
            return JSON.parseObject(data2.toString(), clazz);
        }
        return null;
    }
}
