package com.snowapp.jjfunny.utils;

import android.content.ComponentName;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.snowapp.jjfunny.model.Destination;
import com.snowapp.jjfunny.navigator.FixFragmentNavigator;
import com.snowapp.libcommon.global.AppGlobals;
import com.snowapp.libnavannotation.FragmentDestination;

import java.util.HashMap;

/**
 * CreateTime: 2020-08-18
 * Author: snow
 * Description: This is NavGraphBuilder
 * 思想：创建自定义 NavGraphBuilder 来生成 NavGraph 对象，从而替代 xml 文件解析
 */
public class NavGraphBuilder {

    /**
     * 与 NavController 绑定，获取 NavigatorProvider 来控制导航
     *
     * @date 2020-08-18
     * @author snow
     * @param controller NavController
     * @return void
     */
    public static void build(FragmentActivity activity, NavController controller, int containerId) {
        NavigatorProvider provider = controller.getNavigatorProvider();

//        FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        FixFragmentNavigator fragmentNavigator = new FixFragmentNavigator(activity,
                activity.getSupportFragmentManager(), containerId);
        provider.addNavigator(fragmentNavigator);   // 自定义导航器（第五种）

        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);

        // 创建 NavGraph，其维护一个 destination 的 nodes 对象（HashMap）
        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        // 遍历 destination
        for (Destination value :
                destConfig.values()) {
            if (value.isFragment) {
                // 以下代码模仿自系统源码 FragmentNavigator.java / NavGraphNavigator.java / NavGraph.java
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setClassName(value.clazzName);
                destination.setId(value.id);
                destination.addDeepLink(value.pageUrl);

                // 添加destination结点
                navGraph.addDestination(destination);
            } else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setComponentName(new ComponentName(AppGlobals.getApplication().getPackageName(), value.clazzName));
                destination.setId(value.id);
                destination.addDeepLink(value.pageUrl);

                // 添加destination结点
                navGraph.addDestination(destination);
            }

            // 必须设置默认启动页的id
            if (value.asStarter) {
                navGraph.setStartDestination(value.id);
            }
        }

        // 将 navGraph 与 controller 绑定
        controller.setGraph(navGraph);
    }

}
