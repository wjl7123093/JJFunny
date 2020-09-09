package com.snowapp.libnetwork;

/**
 * @date 2020-08-19
 * @author snow
 * @description 网络请求返回值结构体类
 */
public class ApiResponse<T> {

    public boolean success;
    public int status;
    public String message;
    public T body;

}
