package com.snowapp.jjfunny.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.snowapp.jjfunny.R;
import com.snowapp.libcommon.utils.PixUtils;

public class ListPlayerView extends FrameLayout {
    private View bufferView;
    private JJImageView cover, blur;
    private ImageView playBtn;
    private String mCategory;
    private String mVideoUrl;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        bufferView = findViewById(R.id.buffer_view);
        cover = findViewById(R.id.cover);
        blur = findViewById(R.id.blur_background);
        playBtn = findViewById(R.id.play_btn);
    }

    /**
     * 数据绑定
     *
     * @date 2020-08-21
     * @author snow
     * @param category 页面声明标识
     * @return
     */
    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;

        cover.setImageUrl(cover, coverUrl, false);
        if (widthPx < heightPx) {
            // 加载高斯模糊背景
            blur.setBlurImageUrl(coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx, heightPx);
    }

    /**
     * 给控件（ListPlayerView, Cover, BlurImageView, PlayBtn）设置尺寸
     * @date 2020-08-21
     * @author snow
     */
    private void setSize(int widthPx, int heightPx) {
        int maxWidth = PixUtils.getScreenWidth();
        // 定义最大高度 == 屏幕宽度
        int maxHeight = maxWidth;

        // 根据 widthPx 和 heightPx 进行计算后最终确定的 ListPlayerView 宽高值
        int finalWidth = maxWidth;
        int finalHeight = 0;

        int coverWidth;
        int coverHeight;
        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            finalHeight = coverHeight = (int) (heightPx / (widthPx * 1.0f / maxWidth));
        } else {
            finalHeight = coverHeight = maxHeight;
            coverWidth = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        }

        // 设置 ListPlayerView 的宽高
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = finalWidth;
        layoutParams.height = finalHeight;
        setLayoutParams(layoutParams);

        // 设置 高斯模糊 宽高
        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        blurParams.width = finalWidth;
        blurParams.height = finalHeight;
        blur.setLayoutParams(blurParams);

        // 设置 封面 宽高
        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        // 设置 播放按钮 位置
        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);

    }


}
