package com.snowapp.jjfunny.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.snowapp.jjfunny.model.BottomBar;
import com.snowapp.jjfunny.model.Destination;
import com.snowapp.jjfunny.model.SofaTab;
import com.snowapp.libcommon.global.AppGlobals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * CreateTime: 2020-08-18
 * Author: snow
 * Description: This is AppConfig
 */
public class AppConfig {

    /**
     * 解析 destination.json 文件并保存到该 HashMap 中
     */
    private static HashMap<String, Destination> sDestConfig;

    private static BottomBar sBottomBar;
    private static SofaTab sSofaTab, sFindTabConfig;

    /**
     * 解析 destination.json
     *
     * @date 2020-08-18
     * @author snow
     * @return HashMap
     */
    public static HashMap<String, Destination> getDestConfig() {
        if (null == sDestConfig) {
            String content = parseFile("destination.json");
            sDestConfig = JSON.parseObject(content,
                    new TypeReference<HashMap<String, Destination>>(){}.getType());
        }
        return sDestConfig;
    }

    /**
     * 解析 main_tabs_config.json
     *
     * @date 2020-08-18
     * @author snow
     * @return BottomBar
     */
    public static BottomBar getBottomBarConfig() {
        if (null == sBottomBar) {
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBar;
    }

    /**
     * 解析 sofa_tabs_config.json
     *
     * @date 2020-09-18
     * @author snow
     * @return SofaTab
     */
    public static SofaTab getSofaTabConfig() {
        if (null == sSofaTab) {
            String content = parseFile("sofa_tabs_config.json");
            sSofaTab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(sSofaTab.tabs, new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return o1.index < o2.index ? -1 : 1;
                }
            });
        }
        return sSofaTab;
    }

    /**
     * 解析 find_tabs_config.json
     *
     * @date 2020-09-30
     * @author snow
     * @return SofaTab
     */
    public static SofaTab getFindTabConfig() {
        if (sFindTabConfig == null) {
            String content = parseFile("find_tabs_config.json");
            sFindTabConfig = JSON.parseObject(content, SofaTab.class);
            Collections.sort(sFindTabConfig.tabs, new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return o1.index < o2.index ? -1 : 1;
                }
            });
        }
        return sFindTabConfig;
    }

    /**
     * 解析文件
     * @param fileName json 文件名
     * @return 解析后的字符串
     */
    private static String parseFile(String fileName) {
        AssetManager assets = AppGlobals.getApplication().getResources().getAssets();

        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            inputStream = assets.open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }

                if (null != reader) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }
}
