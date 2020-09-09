package com.snowapp.libnetwork;

/**
 * @date 2020-08-19
 * @author snow
 * @description 网络请求回调类
 */
public abstract class JsonCallback<T> {
    public void onSuccess(ApiResponse<T> response) {

    }

    public void onError(ApiResponse<T> response) {

    }

    public void onCacheSuccess(ApiResponse<T> response) {

    }
}
