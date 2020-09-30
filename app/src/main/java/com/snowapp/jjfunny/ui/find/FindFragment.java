package com.snowapp.jjfunny.ui.find;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.snowapp.jjfunny.model.SofaTab;
import com.snowapp.jjfunny.ui.find.TagListFragment;
import com.snowapp.jjfunny.ui.sofa.SofaFragment;
import com.snowapp.jjfunny.utils.AppConfig;
import com.snowapp.libnavannotation.FragmentDestination;

/**
 * 发现页面
 */
@FragmentDestination(pageUrl = "main/tabs/find")
public class FindFragment extends SofaFragment {

    @Override
    public Fragment getTabFragment(int position) {
        SofaTab.Tabs tab = getTabConfig().tabs.get(position);
        TagListFragment fragment = TagListFragment.newInstance(tab.tag);
        return fragment;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        String tagType = childFragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            // 切换为 "关注" 标签列表页面
            ViewModelProviders.of(childFragment).get(TagListViewModel.class)
                    .getSwitchTabLiveData().observe(this,
                    object -> viewPager2.setCurrentItem(1));
        }
    }

    @Override
    public SofaTab getTabConfig() {
        return AppConfig.getFindTabConfig();
    }
}