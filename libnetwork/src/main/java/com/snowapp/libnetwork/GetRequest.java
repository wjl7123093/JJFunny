package com.snowapp.libnetwork;

/**
 * @date 2020-08-19
 * @author snow
 * @description Get 请求
 */
public class GetRequest<T> extends Request<T, GetRequest> {

    public GetRequest(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        okhttp3.Request request = builder.get().url(UrlCreator.createUrlFromParams(mUrl, params)).build();
        return request;
    }
}
