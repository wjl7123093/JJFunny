package com.snowapp.jjfunny.model;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.snowapp.jjfunny.BR;

import java.io.Serializable;

/**
 * @date 2020-08-21
 * @author snow
 * @description 用户操作（喜欢/分享/评论）统计
 */
public class Ugc extends BaseObservable implements Serializable {
    /**
     * likeCount : 103
     * shareCount : 10
     * commentCount : 10
     * hasFavorite : false
     * hasLiked : false
     * hasdiss : false
     * hasDissed : false
     */

    public int likeCount;
    public int shareCount;
    public int commentCount;
    public boolean hasFavorite;
    public boolean hasLiked;
    public boolean hasdiss;

    @Bindable
    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
        // 属性改变，重新执行数据绑定
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public boolean isHasFavorite() {
        return hasFavorite;
    }

    public void setHasFavorite(boolean hasFavorite) {
        this.hasFavorite = hasFavorite;
        // 属性改变，重新执行数据绑定
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        if (this.hasLiked == hasLiked) {
            return;
        }
        if (hasLiked) {
            likeCount = likeCount + 1;
            setHasdiss(false);  // 👍 和 👎 互斥
        } else {
            likeCount = likeCount - 1;
        }
        this.hasLiked = hasLiked;
        // 属性改变，重新执行数据绑定
        notifyPropertyChanged(BR._all );
    }

    @Bindable
    public boolean isHasdiss() {
        return hasdiss;
    }

    public void setHasdiss(boolean hasdiss) {
        if (this.hasdiss == hasdiss) {
            return;
        }
        if (hasdiss) {
            setHasLiked(false); // 👍 和 👎 互斥
        }
        this.hasdiss = hasdiss;
        // 属性改变，重新执行数据绑定
        notifyPropertyChanged(BR._all);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof Ugc))
            return false;
        Ugc newUgc = (Ugc) obj;
        return likeCount == newUgc.likeCount
                && shareCount == newUgc.shareCount
                && commentCount == newUgc.commentCount
                && hasFavorite == newUgc.hasFavorite
                && hasLiked == newUgc.hasLiked
                && hasdiss == newUgc.hasdiss;
    }
}
