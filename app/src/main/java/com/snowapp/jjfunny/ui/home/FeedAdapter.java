package com.snowapp.jjfunny.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.snowapp.jjfunny.BR;
import com.snowapp.jjfunny.databinding.LayoutFeedTypeImageBinding;
import com.snowapp.jjfunny.databinding.LayoutFeedTypeVideoBinding;
import com.snowapp.jjfunny.model.Feed;
import com.snowapp.jjfunny.view.ListPlayerView;
import com.snowapp.libcommon.enums.ViewType;

public class FeedAdapter extends PagedListAdapter<Feed, FeedAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private String mCategory;
    private Context mContext;

    protected FeedAdapter(Context context, String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.mContext = context;
        this.mCategory = category;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        Feed feed = getItem(position);
        return feed.itemType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = null;
        if (viewType == ViewType.TYPE_IMAGE.type) {
            binding = LayoutFeedTypeImageBinding.inflate(inflater);
        } else {
            binding = LayoutFeedTypeVideoBinding.inflate(inflater);
        }
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;
        public ImageView feedImage;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            this.mBinding = binding;
        }

        public void bindData(Feed item) {
            //这里之所以手动绑定数据的原因是 图片 和视频区域都是需要计算的
            //而dataBinding的执行默认是延迟一帧的。
            //当列表上下滑动的时候 ，会明显的看到宽高尺寸不对称的问题

            mBinding.setVariable(BR.feed, item);
            mBinding.setVariable(BR.lifeCycleOwner, mContext);
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                // 图片
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                feedImage = imageBinding.feedImage;
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
//                imageBinding.setFeed(item);
//                imageBinding.interactionBinding.setLifecycleOwner((LifecycleOwner) mContext);
            } else if (mBinding instanceof  LayoutFeedTypeVideoBinding) {
                // 视频
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                listPlayerView = videoBinding.listPlayerView;
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
//                videoBinding.setFeed(item);
//                videoBinding.interactionBinding.setLifecycleOwner((LifecycleOwner) mContext);
            }
        }
    }
}
