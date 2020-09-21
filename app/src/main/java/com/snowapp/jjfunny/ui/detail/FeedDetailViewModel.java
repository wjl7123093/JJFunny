package com.snowapp.jjfunny.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.snowapp.jjfunny.AbsViewModel;
import com.snowapp.jjfunny.model.Comment;
import com.snowapp.jjfunny.ui.login.UserManager;
import com.snowapp.libnetwork.ApiResponse;
import com.snowapp.libnetwork.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 帖子详情 ViewModel
 */
public class FeedDetailViewModel extends AbsViewModel<Comment> {
    private long itemId;

    // 服务器接口根据最后一条 item 信息来控制分页
    // 所以选择 ItemKeyedDataSource
    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    class DataSource extends ItemKeyedDataSource<Integer, Comment> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Comment> callback) {
            loadData(params.requestedInitialKey, params.requestedLoadSize, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            if (params.key > 0) {
                loadData(params.key, params.requestedLoadSize, callback);
            }
        }

        private void loadData(Integer key, int requestedLoadSize, LoadCallback<Comment> callback) {
            ApiResponse<List<Comment>> response = ApiService.get("/comment/queryFeedComments")
                    .addParam("id", key)
                    .addParam("itemId", itemId)
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", requestedLoadSize)
                    .responseType(new TypeReference<ArrayList<Comment>>() {
                    }.getType())
                    .execute();

            List<Comment> list = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(list);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Comment item) {
            return item.id;
        }
    }
}
