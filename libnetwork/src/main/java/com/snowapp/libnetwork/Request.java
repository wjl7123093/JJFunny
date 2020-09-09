package com.snowapp.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.snowapp.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @date 2020-08-19
 * @author snow
 * @description 网络请求封装类
 */
public abstract class Request<T, R extends Request> implements Cloneable{
    private static final String TAG = "Request";
    protected String mUrl;
    protected HashMap<String, String> headers = new HashMap<>();
    protected HashMap<String, Object> params = new HashMap<>();

    // 只访问缓存，即便本地没有，也不会访问网络
    public static final int CACHE_ONLY = 1;
    // 先访问缓存，同时发起网络请求，成功后缓存到本地
    public static final int CACHE_FIRST = 2;
    // 只访问网络，不做任何缓存
    public static final int NET_ONLY = 3;
    // 先访问网络，成功后缓存到本地
    public static final int NET_CACHE = 4;
    private String cacheKey;
    private Type mType;
    private Class mClazz;
    private int mCacheStrategy;

    public Request(String url) {
        mUrl = url;
    }

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {

    }

    public R addHeader(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }

    /**
     * 添加请求参数
     *
     * @date 2020-08-19
     * @author snow
     * @param value 8 种基本类型（需要判断）
     * @return
     */
    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }
        //int byte char short long double float boolean 和他们的包装类型，但是除了 String.class 所以要额外判断
        try {
            if (value.getClass() == String.class) {
                params.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");
                Class clazz = (Class) field.get(null);
                if (clazz.isPrimitive()) {  // 属于 8 种基本类型
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    /**
     * 设置返回体结构类型
     * @param type 返回体结构类型
     * @return
     */
    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }


    public R responseType(Class clazz) {
        this.mClazz = clazz;
        return (R) this;
    }

    public R cacheStrategy(int strategy) {
        mCacheStrategy = strategy;
        return (R) this;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry :
                headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 异步执行
     *
     * @date 2020-08-19
     * @author snow
     * @param callback 回调抽象类
     */
    @SuppressLint("RestrictedApi")
    public void execute(final JsonCallback<T> callback) {
        if (mCacheStrategy != NET_ONLY) {   // 并非只访问网络，则读取缓存
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null && response.body != null) {
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy != CACHE_ONLY) { // 并非只读取缓存，则异步网络请求
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> response = new ApiResponse<>();
                    response.message = e.getMessage();
                    callback.onError(response);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response, callback);
                    if (!apiResponse.success) {
                        callback.onError(apiResponse);
                    } else {
                        callback.onSuccess(apiResponse);
                    }
                }
            });
        }
    }

    /**
     * 获取缓存
     * @date 2020-08-21
     * @author snow
     * @return 缓存（返回结构体）
     */
    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    /**
     * 解析返回值，并返回 ApiResponse 结构体
     *
     * @date 2020-08-19
     * @author snow
     * @param response 返回值
     * @param callback 回调抽象类
     * @return 解析后的结构体
     */
    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            // 这 .toString() 会导致 convert 时 fastJson 无法解析
//            String content = response.body().toString();
            // 所以得调用 okhttp3 中的 string() 方法
            String content = response.body().string();
            if (success) {
                if (null != callback) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    // 将返回内容进行转换
                    result.body = (T) convert.convert(content, argument);
                } else if (null != mType) {
                    result.body = (T) convert.convert(content, mType);
                } else if (null != mClazz) {
                    result.body = (T) convert.convert(content, mClazz);
                } else {    // 这里涉及到 "泛型擦除" 的问题
                    Log.e(TAG, "parseResponse: 无法解析");
                }
            } else {
                message = content;
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            status = 0;
        }

        // 组装 ApiResponse 结构体
        result.success = success;
        result.status = status;
        result.message = message;

        // 保存缓存（存到数据库）
        if (mCacheStrategy != NET_ONLY && result.success && result.body != null && result.body instanceof Serializable) {
            saveCache(result.body);
        }
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return cacheKey;
    }

    /**
     * 同步执行
     *
     * @date 2020-08-20
     * @author snow
     * @description This is Request
     * @return
     */
    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步方法,response 返回值 类型必须设置");
        }

        if (mCacheStrategy == CACHE_ONLY) {
            return readCache();
        }

        if (mCacheStrategy != CACHE_ONLY) {
            try {
                Response response = getCall().execute();
                ApiResponse<T> result = parseResponse(response, null);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }

}
