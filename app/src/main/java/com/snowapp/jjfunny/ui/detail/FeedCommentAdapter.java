package com.snowapp.jjfunny.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.snowapp.jjfunny.databinding.LayoutFeedCommentListItemBinding;
import com.snowapp.jjfunny.model.Comment;
import com.snowapp.jjfunny.ui.InteractionPresenter;
import com.snowapp.jjfunny.ui.MutableItemKeyedDataSource;
import com.snowapp.jjfunny.ui.login.UserManager;
import com.snowapp.jjfunny.ui.publish.PreviewActivity;
import com.snowapp.libcommon.extension.AbsPagedListAdapter;
import com.snowapp.libcommon.utils.PixUtils;

/**
 * 图文详情页 - 评论列表适配器
 */
public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        // 绑定评论列表
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        // 删除评论
        holder.mBinding.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.commentId)
                        .observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean success) {
                                if (success) {
                                    deleteAndRefreshList(item);
                                }
                            }
                        });
            }
        });
        // 单击评论封面，查看照片/视频
        holder.mBinding.commentCover.setOnClickListener(view -> {
            boolean isVideo = item.commentType == Comment.COMMENT_TYPE_VIDEO;
            PreviewActivity.startActivityForResult((Activity) mContext, isVideo ? item.videoUrl : item.imageUrl, isVideo, null);
        });
    }

    // 删除评论并刷新
    public void deleteAndRefreshList(Comment item) {
        MutableItemKeyedDataSource<Integer, Comment> dataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Comment item) {
                return item.id;
            }
        };
        PagedList<Comment> currentList = getCurrentList();
        // 过滤（把要删除的评论 过滤掉，再重新绑定 => 实现删除效果）
        for (Comment comment : currentList) {
            if (comment != item) {
                dataSource.data.add(comment);
            }
        }
        // 提交新的数据集
        PagedList<Comment> pagedList = dataSource.buildNewPagedList(getCurrentList().getConfig());
        submitList(pagedList);
    }

    // 新增评论并刷新
    public void addAndRefreshList(Comment comment) {
        PagedList<Comment> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Comment> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Comment item) {
                return item.id;
            }
        };
        mutableItemKeyedDataSource.data.add(comment);
        mutableItemKeyedDataSource.data.addAll(currentList);
        // submitList 提交新的 pagedList 不会造成界面卡顿
        // 因为 Paging 框架会使用 差分异算法 在指定位置上进行插入/更新/删除。其余位置不会变。
        PagedList<Comment> pagedList = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
        submitList(pagedList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            boolean self = item.author == null ? false : UserManager.get().getUserId() == item.author.userId;
            mBinding.labelAuthor.setVisibility(self ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(self ? View.VISIBLE : View.GONE);
            // 如果 imageUrl 不为空，显示图片
            if (!TextUtils.isEmpty(item.imageUrl)) {
                mBinding.commentExt.setVisibility(View.VISIBLE);
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(item.width, item.height, 0, PixUtils.dp2px(200), PixUtils.dp2px(200), item.imageUrl);
                if (!TextUtils.isEmpty(item.videoUrl)) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
                mBinding.commentExt.setVisibility(View.GONE);
            }
        }
    }
}
