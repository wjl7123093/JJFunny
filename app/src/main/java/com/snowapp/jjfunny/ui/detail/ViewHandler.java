package com.snowapp.jjfunny.ui.detail;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.snowapp.jjfunny.R;
import com.snowapp.jjfunny.databinding.LayoutFeedDetailBottomInateractionBinding;
import com.snowapp.jjfunny.model.Comment;
import com.snowapp.jjfunny.model.Feed;
import com.snowapp.jjfunny.ui.MutableItemKeyedDataSource;
import com.snowapp.libcommon.utils.PixUtils;
import com.snowapp.libcommon.view.EmptyView;

/**
 * 帖子详情页 View 处理器
 */
public abstract class ViewHandler {
    private final FeedDetailViewModel viewModel;
    protected FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailBottomInateractionBinding mInateractionBinding;
    protected FeedCommentAdapter listAdapter;
    private CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity) {

        mActivity = activity;
        // 通过 ViewModelProviders 获取 FeedDetailViewModel 实例对象
        viewModel = ViewModelProviders.of(activity).get(FeedDetailViewModel.class);
    }


    // @CallSuper 保证父类中功能的正常
    @CallSuper
    public void bindInitData(Feed feed) {
        // 传递 LifeCycleOwner
        mInateractionBinding.setOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        listAdapter = new FeedCommentAdapter(mActivity) {
            @Override
            public void onCurrentListChanged(@Nullable PagedList<Comment> previousList, @Nullable PagedList<Comment> currentList) {
                boolean empty = currentList.size() <= 0;
                handleEmpty(!empty);
            }
        };
        mRecyclerView.setAdapter(listAdapter);

        viewModel.setItemId(mFeed.itemId);
        // 给 LiveData 对象注册观察者
        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                listAdapter.submitList(comments);
                handleEmpty(comments.size() > 0);
            }
        });
        // 输入评论内容
        mInateractionBinding.inputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出输入评论内容对话框
                showCommentDialog();
            }
        });
    }

    // 弹出评论对话框
    private void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = CommentDialog.newInstance(mFeed.itemId);
        }
        // 新增一条评论
        commentDialog.setCommentAddListener(comment -> {
            MutableItemKeyedDataSource<Integer, Comment> dataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) viewModel.getDataSource()) {
                @NonNull
                @Override
                public Integer getKey(@NonNull Comment item) {
                    return item.id;
                }
            };
            dataSource.data.add(comment);
            dataSource.data.addAll(listAdapter.getCurrentList());
            PagedList<Comment> pagedList = dataSource.buildNewPagedList(listAdapter.getCurrentList().getConfig());
            // submitList 提交新的 pagedList 不会造成界面卡顿
            // 因为 Paging 框架会使用 差分异算法 在指定位置上进行插入/更新/删除。其余位置不会变。
            listAdapter.submitList(pagedList);
        });
        commentDialog.show(mActivity.getSupportFragmentManager(), "comment_dialog");
    }

    private EmptyView mEmptyView;

    // 空布局
    public void handleEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                listAdapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = PixUtils.dp2px(40);
                mEmptyView.setLayoutParams(layoutParams);
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
            }
            listAdapter.addHeaderView(mEmptyView);
        }
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onBackPressed() {

    }
}
