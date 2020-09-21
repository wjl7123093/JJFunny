package com.snowapp.jjfunny.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.snowapp.jjfunny.R;
import com.snowapp.jjfunny.databinding.ActivityFeedDetailTypeImageBinding;
import com.snowapp.jjfunny.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.snowapp.jjfunny.model.Feed;
import com.snowapp.jjfunny.view.JJImageView;

/**
 * 图文详情页 处理器
 */
public class ImageViewHandler extends ViewHandler {

    protected ActivityFeedDetailTypeImageBinding mImageBinding;

    protected LayoutFeedDetailTypeImageHeaderBinding mHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);

        // 图文详情页样式
        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);
        mRecyclerView = mImageBinding.recyclerView;
        mInateractionBinding = mImageBinding.interactionLayout;

    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        // 图文详情页绑定 Feed
        mImageBinding.setFeed(mFeed);

        // 添加头部布局
        mHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        mHeaderBinding.setFeed(mFeed);
        JJImageView headerImage = mHeaderBinding.headerImage;
        headerImage.bindData(mFeed.width, mFeed.height, mFeed.width > mFeed.height ? 0 : 16, mFeed.cover);
        listAdapter.addHeaderView(mHeaderBinding.getRoot());

        // 监测 RecyclerView 的滑动事件
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 监测 top值 是否已经小于标题栏高度
                boolean visible = mHeaderBinding.getRoot().getTop() <= -mImageBinding.titleLayout.getMeasuredHeight();
                // 显示作者信息栏，隐藏标题
                mImageBinding.authorInfoLayout.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
                mImageBinding.title.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });
        handleEmpty(false);
    }
}










