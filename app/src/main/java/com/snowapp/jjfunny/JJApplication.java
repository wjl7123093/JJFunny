package com.snowapp.jjfunny;

import android.app.Application;

import com.snowapp.libnetwork.ApiService;

public class JJApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://123.56.232.18:8080/serverdemo", null);
//        ApiService.init("http://192.168.31.218:8080/serverdemo", null);
//        ApiService.init("http://192.168.101.103:8080/serverdemo", null);
    }
}
