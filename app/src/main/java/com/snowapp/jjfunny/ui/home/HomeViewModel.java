package com.snowapp.jjfunny.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.snowapp.jjfunny.AbsViewModel;
import com.snowapp.jjfunny.model.Feed;
import com.snowapp.jjfunny.ui.MutableDataSource;
import com.snowapp.jjfunny.ui.login.UserManager;
import com.snowapp.libnetwork.ApiResponse;
import com.snowapp.libnetwork.ApiService;
import com.snowapp.libnetwork.JsonCallback;
import com.snowapp.libnetwork.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean withCache = true;
    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();
    private AtomicBoolean loadAfter = new AtomicBoolean(false); // 同步位标记

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }

    ItemKeyedDataSource<Integer, Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            // 加载初始化数据
            loadData(0, callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            // 加载分页数据
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
            // 能够向前加载数据
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    };

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (key > 0) {
            loadAfter.set(true);
        }
        // /feeds/queryHotFeedsList
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", key)
                .addParam("pageCount", 10)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());

        if (withCache) {
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.e("loadData", "onCacheSuccess: " + response.body.size());

                    MutableDataSource dataSource = new MutableDataSource<Integer, Feed>();
                    dataSource.data.addAll(response.body);

                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    cacheLiveData.postValue(pagedList); // 子线程用 postValue
                }
            });
        }

        try {
            Request newRequest = withCache ? request.clone() : request;
            newRequest.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = newRequest.execute();
            if (null == response)
                return;
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;

            callback.onResult(data);

            if (key > 0) {
                // 通过 liveData 发送数据，告诉 UI 层，是否应该主动关闭上拉加载分页的动画
                getBoundaryPageData().postValue(data.size() > 0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Log.e("loadData", "loadData: key: " + key);
    }

    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id, callback);
            }
        });
    }
}