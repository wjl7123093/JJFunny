package com.snowapp.jjfunny.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.snowapp.jjfunny.model.Feed;
import com.snowapp.jjfunny.model.User;
import com.snowapp.jjfunny.ui.login.UserManager;
import com.snowapp.libcommon.global.AppGlobals;
import com.snowapp.libnetwork.ApiResponse;
import com.snowapp.libnetwork.ApiService;
import com.snowapp.libnetwork.JsonCallback;

/**
 * 处理 👍 / 👎 / 评论 / 分享 逻辑
 */
public class InteractionPresenter {
    // 点赞
    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";
    // 踩
    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        // 如果此时用户未登录，则 owner == null
        // 此处如果不对 owner == null 进行单独判断，则会报 Null 异常
//        if (!UserManager.get().isLogin()) {
//            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
//            loginLiveData.observe(owner, new Observer<User>() {
//                @Override
//                public void onChanged(User user) {
//                    if (user != null) {
//                        toggleFeedLikeInternal(feed);
//                    }
//                    loginLiveData.removeObserver(this);
//                }
//            });
//        }
//
//        toggleFeedLikeInternal(feed);

        // isLogin 方法里对 owner == null 进行了单独判断，所以不会报 Null 异常
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedLikeInternal(feed);
            }
        })) {
        } else {
            toggleFeedLikeInternal(feed);
        }
    }

    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKE)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasLiked(hasLiked);
                        }
                    }
                });

    }

    // 踩
    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        // 与 点赞 同理，需要单独判断 owner == null 的条件
//        if (!UserManager.get().isLogin()) {
//            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
//            loginLiveData.observe(owner, new Observer<User>() {
//                @Override
//                public void onChanged(User user) {
//                    if (user != null) {
//                        toggleFeedDissInternal(feed);
//                    }
//                    loginLiveData.removeObserver(this);
//                }
//            });
//        }
//
//        toggleFeedDissInternal(feed);

        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedDissInternal(feed);
            }
        })) {
        } else {
            toggleFeedDissInternal(feed);
        }
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasdiss(hasLiked);
                        }
                    }
                });

    }

    // 判断是否登录
    private static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()) {
            return true;
        } else {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            if (owner == null) {    // 从未登录过，则 owner 为空
                // 会跳转到登录界面（但原理没搞懂！！！？？？）
                liveData.observeForever(loginObserver(observer, liveData));
            } else {
                liveData.observe(owner, loginObserver(observer, liveData));
            }
            return false;
        }
    }

    @NonNull
    private static Observer<User> loginObserver(Observer<User> observer, LiveData<User> liveData) {
        return new Observer<User>() {
            @Override
            public void onChanged(User user) {
                liveData.removeObserver(this);
                if (user != null && observer != null) {
                    observer.onChanged(user);
                }
            }
        };
    }

}
