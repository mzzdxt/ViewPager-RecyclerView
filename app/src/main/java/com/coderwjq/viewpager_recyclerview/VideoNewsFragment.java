package com.coderwjq.viewpager_recyclerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author: wangjiaqi
 * @data: 2018/1/10
 */

public class VideoNewsFragment extends Fragment {
    private NewRecyclerView mRvNews;
    private SwipeRefreshLayout mSwipeToRefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_list, null);

        initView(rootView);

        return rootView;
    }


    private void initView(View rootView) {
        mRvNews = rootView.findViewById(R.id.rv_news);
        mRvNews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRvNews.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        NewsAdapter newsAdapter = new NewsAdapter(getContext(), "视频新闻");
        mRvNews.setAdapter(newsAdapter);

        mSwipeToRefresh = rootView.findViewById(R.id.swipe_to_refresh);
        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeToRefresh.setRefreshing(false);
            }
        });
    }
}
