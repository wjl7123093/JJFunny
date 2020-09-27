package com.snowapp.jjfunny.ui.detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.snowapp.jjfunny.R;
import com.snowapp.jjfunny.databinding.LayoutCommentDialogBinding;
import com.snowapp.jjfunny.model.Comment;
import com.snowapp.jjfunny.ui.login.UserManager;
import com.snowapp.libcommon.global.AppGlobals;
import com.snowapp.libcommon.utils.PixUtils;
import com.snowapp.libcommon.view.ViewHelper;
import com.snowapp.libnetwork.ApiResponse;
import com.snowapp.libnetwork.ApiService;
import com.snowapp.libnetwork.JsonCallback;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {
    private LayoutCommentDialogBinding mBinding;
    private long itemId;
    private static final String KEY_ITEM_ID = "key_item_id";
    private commentAddListener mListener;

    public static CommentDialog newInstance(long itemId) {

        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();

        // 此处不能直接用 container，而应该用 ((ViewGroup) window.findViewById(android.R.id.content))
        // 原因不明。（暂时未搞清楚，整个App写完后再来学习）
        // 差别：container 会直接导致 ui 异常：commentDialog 不满屏，且距离软键盘会有一定距离透明区域
        //      ((ViewGroup) window.findViewById(android.R.id.content)) 会直接贴合软键盘上沿，正常显示
        // 注：container 用在 Fragment 中，正常显示。此处是 DialogFragment
//        mBinding = LayoutCommentDialogBinding.inflate(inflater, container, false);
        mBinding = LayoutCommentDialogBinding.inflate(inflater, ((ViewGroup) window.findViewById(android.R.id.content)), false);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        // 透明背景
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        // 圆角
        ViewHelper.setViewOutline(mBinding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);

        this.itemId = getArguments().getLong(KEY_ITEM_ID);
        // 此处需要主动聚焦，并弹出软键盘
        mBinding.getRoot().post(() -> showSoftInputMethod());

        return mBinding.getRoot();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_send:     // 发送评论
                publishComment();
                break;
            case R.id.comment_video:    // 播放视频

                break;
            case R.id.comment_delete:   // 删除评论

                break;
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputView.getText())) {
            return;
        }

        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("image_url", null)
                .addParam("video_url", null)
                .addParam("width", 0)
                .addParam("height", 0)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
//                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败:" + response.message);
//                        dismissLoadingDialog();
                    }
                });
    }

    private void onCommentSuccess(Comment body) {
        showToast("评论发布成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            if (mListener != null) {
                mListener.onAddComment(body);
            }
            dismiss();
        });
    }

    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(commentAddListener listener) {

        mListener = listener;
    }

    // 弹出软键盘
    private void showSoftInputMethod() {
        mBinding.inputView.setFocusable(true);
        mBinding.inputView.setFocusableInTouchMode(true);
        //请求获得焦点
        mBinding.inputView.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(mBinding.inputView, 0);
    }

}















