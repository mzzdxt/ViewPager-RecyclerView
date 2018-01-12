package com.coderwjq.viewpager_recyclerview;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomePageManager.OnModeChangeListener {
    private static final String TAG = "MainActivity";
    public static final int TITLE_SHOW_RANGE = 300;

    private HomeRecyclerView mRvHomePage;
    private HomePageAdapter mHomePageAdapter;
    private TabLayout mTlNewsTitle;
    private LinearLayout mLlMenuBar;
    private Button mBtnBackHome;
    private SmoothScrollLayoutManager mLayoutManager;
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE && mTlNewsTitle.getVisibility() == View.VISIBLE) {
                if (mLayoutManager.findLastCompletelyVisibleItemPosition() != mHomePageAdapter.getItemCount() - 1) {
                    mLayoutManager.smoothScrollToPosition(mRvHomePage, null, mHomePageAdapter.getItemCount() - 1);
                } else {
                    mTlNewsTitle.setAlpha(1);
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            View view = mLayoutManager.findViewByPosition(mHomePageAdapter.getItemCount() - 1);
            if (view != null) {
                Log.d(TAG, "onScrolled: y=" + view.getY());
                if (view.getY() <= TITLE_SHOW_RANGE) {
                    mTlNewsTitle.setVisibility(View.VISIBLE);
                    mTlNewsTitle.setAlpha((TITLE_SHOW_RANGE - view.getY()) / (TITLE_SHOW_RANGE - mTlNewsTitle.getHeight()));
                } else {
                    mTlNewsTitle.setVisibility(View.GONE);
                    mTlNewsTitle.setAlpha(0);
                }
            }
        }
    };
    private int mBottomViewPagerHeight;
    private Button mBtnForwardOrRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomePageManager.getInstance().attatchModeChangeListener(this);

        mTlNewsTitle = findViewById(R.id.tl_news_title);
        mLlMenuBar = findViewById(R.id.ll_menu_bar);
        mBtnBackHome = findViewById(R.id.btn_back_home);
        mBtnForwardOrRefresh = findViewById(R.id.btn_forward_or_refresh);

        mHomePageAdapter = new HomePageAdapter();
        mLayoutManager = new SmoothScrollLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRvHomePage = findViewById(R.id.rv_home_page);
        mRvHomePage.setLayoutManager(mLayoutManager);
        mRvHomePage.setAdapter(mHomePageAdapter);
        mRvHomePage.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRvHomePage.addOnScrollListener(mOnScrollListener);

        mBtnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToNormalState();
            }
        });
        mBtnForwardOrRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HomePageManager.getInstance().isNormalMode()) {
                    Toast.makeText(MainActivity.this, "前进", Toast.LENGTH_SHORT).show();
                } else {
                    Fragment currentChannel = HomePageManager.getInstance().getCurrentChannel();

                    if (currentChannel instanceof TextNewsFragment) {
                        ((TextNewsFragment) currentChannel).refreshNews();
                    } else if (currentChannel instanceof VideoNewsFragment) {
                        ((VideoNewsFragment) currentChannel).refreshNews();
                    }
                }
            }
        });

        mBtnBackHome.setFocusableInTouchMode(true);
        mBtnBackHome.requestFocus();
    }

    private void backToNormalState() {
        HomePageManager.getInstance().setNormalMode();

        if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
            mRvHomePage.smoothScrollToPosition(0);
        }
    }

    @Override
    public void refreshMode(int currentMode) {
        if (currentMode == HomePageManager.HOME_PAGE_MODE_NORMAL) {
            mBtnForwardOrRefresh.setText("FORWARD");
            mBtnForwardOrRefresh.setBackgroundColor(Color.parseColor("#ff99cc00"));
        } else {
            mBtnForwardOrRefresh.setText("REFRESH");
            mBtnForwardOrRefresh.setBackgroundColor(Color.parseColor("#ffaa66cc"));
        }
    }

    class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HOME_PAGE_ITEM_COUNT = 10;
        private NewsAdapter mNewsAdapter;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                // 普通item
                return new NormalViewHolder(getLayoutInflater().inflate(R.layout.item_home_page_normal, null));
            } else {
                Log.i(TAG, "onCreateViewHolder: 创建新闻ViewHolder");
                return new NewsViewHolder(getLayoutInflater().inflate(R.layout.item_home_page_news, null));
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
                if (mNewsAdapter == null) {
                    mNewsAdapter = new NewsAdapter(getSupportFragmentManager());
                }

                holder.mVpContainer.setAdapter(mNewsAdapter);
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
                        HomePageManager.getInstance().setCurrentChannel(mNewsAdapter.getItem(position));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                Log.e(TAG, "屏幕高度: " + dm.heightPixels);
                Log.e(TAG, "菜单栏高度: " + mLlMenuBar.getMeasuredHeight());
                Log.e(TAG, "TabLayout高度: " + mTlNewsTitle.getMeasuredHeight());

                //应用区域
                Rect outRect1 = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
                // 状态栏高度 = 屏幕高度 - 应用区域高度
                int statusBar = dm.heightPixels - outRect1.height();
                Log.e(TAG, "状态栏高度: " + statusBar);
                mBottomViewPagerHeight = dm.heightPixels - mLlMenuBar.getMeasuredHeight() - mTlNewsTitle.getMeasuredHeight() - statusBar;

                // 设置ViewPager高度
                ViewGroup.LayoutParams layoutParams = holder.mVpContainer.getLayoutParams();
                layoutParams.height = mBottomViewPagerHeight;
                Log.e(TAG, "ViewPager高度: " + mBottomViewPagerHeight);
                holder.mVpContainer.setLayoutParams(layoutParams);
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
                        Toast.makeText(MainActivity.this, mTvContent.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        class NewsViewHolder extends RecyclerView.ViewHolder {

            private final NewsViewPager mVpContainer;

            public NewsViewHolder(View itemView) {
                super(itemView);
                mVpContainer = itemView.findViewById(R.id.vp_container);
            }

        }

        class NewsAdapter extends FragmentPagerAdapter {
            private List<Fragment> mFragments = new ArrayList<>();

            public NewsAdapter(FragmentManager fm) {
                super(fm);
                Log.d(TAG, "NewsAdapter() called with: fm = [" + fm + "]");
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

    @Override
    public void onBackPressed() {
        if (HomePageManager.getInstance().isNewsMode()) {
            backToNormalState();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing...
    }
}