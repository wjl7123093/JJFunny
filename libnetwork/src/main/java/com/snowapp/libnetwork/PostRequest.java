package com.snowapp.libnetwork;

import java.util.Map;

import okhttp3.FormBody;

/**
 * @date 2020-08-19
 * @author snow
 * @description Post 请求
 */
public class PostRequest<T> extends Request<T, PostRequest> {

    public PostRequest(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry: params.entrySet()){
            bodyBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        okhttp3.Request request = builder.url(mUrl).post(bodyBuilder.build()).build();
        return request;
    }
}
