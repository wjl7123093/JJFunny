package com.snowapp.jjfunny.navigator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.FragmentNavigator;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;

/**
 * @date 2020-08-19
 * @author snow
 * @description 自定义 FragmentNavigator，目的是通过 hide() & show() 替代 replace(),
 *              从而避免 fragment 切换时导致的生命周期重启
 */
@Navigator.Name("fixfragment")
public class FixFragmentNavigator extends FragmentNavigator {
    private static final String TAG = "FixFragmentNavigator";
    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mContainerId;

    public FixFragmentNavigator(@NonNull Context context, @NonNull FragmentManager manager, int containerId) {
        super(context, manager, containerId);
        this.mContext = context;
        this.mFragmentManager = manager;
        this.mContainerId = containerId;
    }

    /**
     * 重写 navigate，目的是用 hide() & show() 去替换 replace(),避免 fragment 生命周期的重启
     *
     * @date 2020-08-19
     * @author snow
     */
    @Nullable
    @Override
    public NavDestination navigate(@NonNull Destination destination, @Nullable Bundle args, @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
        if (mFragmentManager.isStateSaved()) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already"
                    + " saved its state");
            return null;
        }
        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }
//        final Fragment frag = instantiateFragment(mContext, mFragmentManager,
//                className, args);
//        frag.setArguments(args);
        final FragmentTransaction ft = mFragmentManager.beginTransaction();

        int enterAnim = navOptions != null ? navOptions.getEnterAnim() : -1;
        int exitAnim = navOptions != null ? navOptions.getExitAnim() : -1;
        int popEnterAnim = navOptions != null ? navOptions.getPopEnterAnim() : -1;
        int popExitAnim = navOptions != null ? navOptions.getPopExitAnim() : -1;
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = enterAnim != -1 ? enterAnim : 0;
            exitAnim = exitAnim != -1 ? exitAnim : 0;
            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
        }

        // 获取当前正在显示的 fragment
        Fragment fragment = mFragmentManager.getPrimaryNavigationFragment();
        if (null != fragment) {
            ft.hide(fragment);
        }

        // 下一个fragment
        Fragment fragNext = null;
        String tag = String.valueOf(destination.getId());
        fragNext = mFragmentManager.findFragmentByTag(tag);
        if (null != fragNext) {
            ft.show(fragNext);
        } else { // fragNext 为空，则实例化
            fragNext = instantiateFragment(mContext, mFragmentManager,
                className, args);
            fragNext.setArguments(args);
            ft.add(mContainerId, fragNext, tag);
        }
//        ft.replace(mContainerId, frag);
        ft.setPrimaryNavigationFragment(fragNext);

        final @IdRes int destId = destination.getId();
        ArrayDeque<Integer> mBackStack = null;
        // 通过反射获取 mBackStack 对象
        try {
            Field field = FragmentNavigator.class.getDeclaredField("mBackStack");
            // 可以访问 private权限的字段
            field.setAccessible(true);
            // 在 FragmentNavigator.java 源码中可搜索 "mBackStack" 发现其对象类型为 ArrayDeque<Integer>
            mBackStack = (ArrayDeque<Integer>) field.get(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        final boolean initialNavigation = mBackStack.isEmpty();
        // TODO Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId;

        boolean isAdded;
        if (initialNavigation) {
            isAdded = true;
        } else if (isSingleTopReplacement) {
            // Single Top means we only want one instance on the back stack
            if (mBackStack.size() > 1) {
                // If the Fragment to be replaced is on the FragmentManager's
                // back stack, a simple replace() isn't enough so we
                // remove it from the back stack and put our replacement
                // on the back stack in its place
                mFragmentManager.popBackStack(
                        generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ft.addToBackStack(generateBackStackName(mBackStack.size(), destId));
            }
            isAdded = false;
        } else {
            ft.addToBackStack(generateBackStackName(mBackStack.size() + 1, destId));
            isAdded = true;
        }
        if (navigatorExtras instanceof Extras) {
            Extras extras = (Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                ft.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }
        ft.setReorderingAllowed(true);
        ft.commit();
        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId);
            return destination;
        } else {
            return null;
        }
    }

    /**
     * 获取 backStack 名称（参考 FragmentNavigator 中的实现）
     * @date 2020-08-19
     * @author snow
     * @param
     * @return BackStack 名称
     */
    private String generateBackStackName(int backStackIndex, int destId) {
        return backStackIndex + "-" + destId;
    }
}
