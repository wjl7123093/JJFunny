package com.snowapp.libnetwork;

import java.lang.reflect.Type;

/**
 * @date 2020-08-19
 * @author snow
 * @description 类型转换接口
 */
public interface Convert<T> {

    T convert(String response, Type type);

    T convert(String response, Class clazz);
}
