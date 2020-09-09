package com.snowapp.jjfunny.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.snowapp.jjfunny.R;
import com.snowapp.jjfunny.model.BottomBar;
import com.snowapp.jjfunny.model.Destination;
import com.snowapp.jjfunny.utils.AppConfig;

import java.util.List;

/**
 * CreateTime: 2020-08-18
 * Author: snow
 * Description: This is AppBottomBar
 *
 * Mark1: BottomNavigationView 在添加按钮的时候，会给每个按钮进行排序。
 *        所以只有先将按钮先移除掉（removeAllViews())，然后再排序，再添加到对应位置上。
 *        所以如果要给按钮设置大小，必须先将按钮全部添加完成之后再来设置。否则无效。
 *
 * Mark2: 由于 BottomNavigationView 并没有暴露出设置 icon 大小的API，所以想直接设置 icon 大小是不行的。
 *        只有通过分析源码，找到 BottomNavigationView/BottomNavigationMenuView/BottomNavigationItemView
 *        在 BottomNavigationItemView 中有一个方法 setIconSize()。故只需要得到 BottomNavigationItemView,
 *        就可以设置 icon 大小了。
 *
 * Mark3: 判断 子View 的顺序，请在源码文件中搜索 addView 来进行判断。
 */
public class AppBottomBar extends BottomNavigationView {
    private static int[] sIcons = new int[]{R.drawable.icon_tab_home, R.drawable.icon_tab_sofa,
            R.drawable.icon_tab_publish, R.drawable.icon_tab_find, R.drawable.icon_tab_mine};

    public AppBottomBar(Context context) {
        this(context, null);
    }

    public AppBottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public AppBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        BottomBar bottomBar = AppConfig.getBottomBarConfig();

        // 生命二维数组，定义底部导航栏Tab按钮是否选中两种状态的颜色
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(bottomBar.activeColor), Color.parseColor(bottomBar.inActiveColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        //LABEL_VISIBILITY_LABELED:设置按钮的文本为一直显示模式
        //LABEL_VISIBILITY_AUTO:当按钮个数小于三个时一直显示，或者当按钮个数大于3个且小于5个时，被选中的那个按钮文本才会显示
        //LABEL_VISIBILITY_SELECTED：只有被选中的那个按钮的文本才会显示
        //LABEL_VISIBILITY_UNLABELED:所有的按钮文本都不显示
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
//        setSelectedItemId(bottomBar.selectTab);

        List<BottomBar.Tabs> tabs = bottomBar.tabs;
        // 添加导航栏按钮
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.enable)
                continue;

            int itemId = getItemId(tab.pageUrl);
            if (itemId < 0) {
                continue;
            }
            // 设置导航栏按钮
            MenuItem menuItem = getMenu().add(0, itemId, tab.index, tab.title);
            menuItem.setIcon(sIcons[tab.index]);

        }

        // 设置导航栏 icon 大小
        // 至于为什么不能在上面代码添加按钮的时候就一同设置大小，原因见顶部注释头 Mark1
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.enable) {
                continue;
            }

            int itemId = getItemId(tab.pageUrl);
            if (itemId < 0) {
                continue;
            }

            int iconSize = dp2px(tab.size);
            // 这里直接设置 icon 大小是不行的，原因请见顶部注释头 Mark2
            // 1 BottomNavigationMenuView 是 BottomNavigationView 的第一个子View。
            // 如何判断 子View 顺序请见顶部注释头 Mark3
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            // 2 BottomNavigationItemView[] 是一个数组, 而整个view数组就是 BottomNavigationMenuView 的子View
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            itemView.setIconSize(iconSize);

            // 给中间按钮着色
            if (TextUtils.isEmpty(tab.title)) {
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)));
                // 禁止掉点按时 上下浮动的效果
                itemView.setShifting(false);

                /**
                 * 如果想要禁止掉所有按钮的点击浮动效果。
                 * 那么还需要给选中和未选中的按钮配置一样大小的字号。
                 *
                 *  在MainActivity布局的AppBottomBar标签增加如下配置，
                 *  @style/active，@style/inActive 在style.xml中
                 *  app:itemTextAppearanceActive="@style/active"
                 *  app:itemTextAppearanceInactive="@style/inActive"
                 */
            }
        }

    }

    private int dp2px(int size) {
        float pxSize = getContext().getResources().getDisplayMetrics().density * size + 0.5f;
        return (int) pxSize;
    }

    private int getItemId(String pageUrl) {
        // 根据 pageUrl 获取 HashMap 的值
        Destination destination = AppConfig.getDestConfig().get(pageUrl);
        if (null == destination) {
            return -1;
        }
        return destination.id;
    }
}
