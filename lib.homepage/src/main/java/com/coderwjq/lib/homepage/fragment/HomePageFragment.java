package com.coderwjq.lib.homepage.fragment;

import android.graphics.Rect;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coderwjq.lib.homepage.R;
import com.coderwjq.lib.homepage.base.BaseFragment;
import com.coderwjq.lib.homepage.common.Constant;
import com.coderwjq.lib.homepage.manager.HomePageManager;
import com.coderwjq.lib.homepage.manager.SmoothScrollLayoutManager;
import com.coderwjq.lib.homepage.widget.HomeRecyclerView;
import com.coderwjq.lib.homepage.widget.NewsViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangjiaqi
 * @data: 2018/1/12
 */

public class HomePageFragment extends BaseFragment {
    private static final String TAG = "HomePageFragment";

    private HomeRecyclerView mRvHomePage;
    private TabLayout mTlNewsTitle;
    private SmoothScrollLayoutManager mLayoutManager;
    private HomePageAdapter mHomePageAdapter;

    private int mBottomViewPagerHeight;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_page;
    }

    @Override
    protected void initView(View rootView) {
        mTlNewsTitle = rootView.findViewById(R.id.tl_news_title);
        mRvHomePage = rootView.findViewById(R.id.rv_home_page);

        mHomePageAdapter = new HomePageAdapter();
        mLayoutManager = new SmoothScrollLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);

        mRvHomePage.setLayoutManager(mLayoutManager);
        mRvHomePage.setAdapter(mHomePageAdapter);
        mRvHomePage.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        mRvHomePage.addOnScrollListener(mOnScrollListener);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE && mTlNewsTitle.getVisibility() == View.VISIBLE) {
                View view = mLayoutManager.findViewByPosition(mHomePageAdapter.getItemCount() - 1);
                if (view != null) {
                    if (view.getY() <= Constant.TITLE_SHOW_RANGE) {
                        Log.i(TAG, "onScrollStateChanged: 自动吸顶");
                        mLayoutManager.smoothScrollToPosition(mRvHomePage, null, mHomePageAdapter.getItemCount() - 1);
                    }
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            View view = mLayoutManager.findViewByPosition(mHomePageAdapter.getItemCount() - 1);
            if (view != null) {
                Log.i(TAG, "view.getY: " + view.getY());
                if (view.getY() <= Constant.TITLE_SHOW_RANGE) {
                    mTlNewsTitle.setVisibility(View.VISIBLE);
                    mTlNewsTitle.setAlpha((Constant.TITLE_SHOW_RANGE - view.getY()) / (Constant.TITLE_SHOW_RANGE - mTlNewsTitle.getHeight()));
                    Log.i(TAG, "onScrolled: alpha" + (Constant.TITLE_SHOW_RANGE - view.getY()) / (Constant.TITLE_SHOW_RANGE - mTlNewsTitle.getHeight()));
                    // 可能原因，ViewHolder高度没有重新计算
                } else {
                    mTlNewsTitle.setVisibility(View.GONE);
                    mTlNewsTitle.setAlpha(0);
                }
            }
        }
    };

    class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HOME_PAGE_ITEM_COUNT = 10;
        private NewsPagerAdapter mNewsPagerAdapter;
        private NewsViewHolder mNewsViewHolder;

        public NewsViewHolder getNewsViewHolder() {
            return mNewsViewHolder;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                // 普通item
                return new NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_home_page_normal, null));
            } else {
                Log.i(TAG, "onCreateViewHolder: 创建新闻ViewHolder");
                return new NewsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_home_page_news, null));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof NormalViewHolder) {
                NormalViewHolder holder = (NormalViewHolder) viewHolder;
                holder.mTvContent.setText("item: " + position);
            } else if (viewHolder instanceof NewsViewHolder) {
                Log.i(TAG, "onBindViewHolder: 绑定新闻ViewHolder");
                NewsViewHolder holder = (NewsViewHolder) viewHolder;

                mNewsViewHolder = holder;

                if (mNewsPagerAdapter == null) {
                    mNewsPagerAdapter = new NewsPagerAdapter(getChildFragmentManager());
                }

                holder.mVpContainer.setAdapter(mNewsPagerAdapter);
                mTlNewsTitle.setupWithViewPager(holder.mVpContainer);
                mTlNewsTitle.getTabAt(0).setText("文本新闻");
                mTlNewsTitle.getTabAt(1).setText("视频新闻");

                // 设置ViewPager滚动监听
                holder.mVpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        HomePageManager.getInstance().setCurrentChannel(mNewsPagerAdapter.getItem(position));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

                calcNewsViewHolder();
            }
        }

        @Override
        public int getItemCount() {
            return HOME_PAGE_ITEM_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < HOME_PAGE_ITEM_COUNT - 1) {
                return 0;
            } else {
                return 1;
            }
        }

        class NormalViewHolder extends RecyclerView.ViewHolder {

            private final TextView mTvContent;
            private final LinearLayout mLlContainer;

            public NormalViewHolder(View itemView) {
                super(itemView);
                mTvContent = itemView.findViewById(R.id.tv_content);
                mLlContainer = itemView.findViewById(R.id.ll_container);

                mLlContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, mTvContent.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        class NewsViewHolder extends RecyclerView.ViewHolder {

            private final NewsViewPager mVpContainer;

            public NewsViewHolder(View itemView) {
                super(itemView);
                Log.i(TAG, "NewsViewHolder: 创建新闻的ViewHolder");
                mVpContainer = itemView.findViewById(R.id.vp_container);
            }

        }

        class NewsPagerAdapter extends FragmentPagerAdapter {
            private List<Fragment> mFragments = new ArrayList<>();

            public NewsPagerAdapter(FragmentManager fm) {
                super(fm);
                Log.d(TAG, "NewsPagerAdapter() called with: fm = [" + fm + "]");
                mFragments.add(new TextNewsFragment());
                mFragments.add(new VideoNewsFragment());

                HomePageManager.getInstance().setCurrentChannel(mFragments.get(0));
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        }
    }

    public void calcNewsViewHolder() {
        HomePageAdapter.NewsViewHolder holder = mHomePageAdapter.getNewsViewHolder();

        if (holder == null) {
            Log.i(TAG, "calcNewsViewHolder: NewsViewHolder...null");
            return;
        }

        Log.e(TAG, "fragment高度: " + getView().getMeasuredHeight());
        Log.e(TAG, "TabLayout高度: " + mTlNewsTitle.getMeasuredHeight());

        //应用区域
        Rect outRect1 = new Rect();
        mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        mBottomViewPagerHeight = getView().getMeasuredHeight() - mTlNewsTitle.getMeasuredHeight();

        // 设置ViewPager高度
        ViewGroup.LayoutParams layoutParams = holder.mVpContainer.getLayoutParams();
        layoutParams.height = mBottomViewPagerHeight;
        Log.e(TAG, "ViewPager高度: " + mBottomViewPagerHeight);
        holder.mVpContainer.setLayoutParams(layoutParams);
    }

    public void backToNormalState() {
        HomePageManager.getInstance().setNormalMode();

        if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
            mRvHomePage.smoothScrollToPosition(0);
        }
    }
}