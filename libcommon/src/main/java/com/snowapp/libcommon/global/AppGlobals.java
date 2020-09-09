package com.snowapp.libcommon.global;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * CreateTime: 2020-08-18
 * Author: snow
 * Description: This is AppGlobals
 */
public class AppGlobals {
    private static Application sApplication;

    public static Application getApplication() {
        if (null == sApplication) {
            // 反射 ActivityThread.java 中的 currentApplication
            try {
                Method method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
                sApplication = (Application) method.invoke(null, null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        return sApplication;
    }
}
